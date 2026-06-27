package ayman.djangogenerator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

public class ModelParser {

    public static String parsePythonModelToJson(String pythonCode) {
        try {
            JSONObject root = new JSONObject();
            JSONArray modelsArray = new JSONArray();

            Pattern classPattern = Pattern.compile("class\\s+(\\w+)\\s*\\(.*?\\):([\\s\\S]*?)(?=\\nclass|\\Z)");
            Matcher classMatcher = classPattern.matcher(pythonCode);

            while (classMatcher.find()) {
                JSONObject modelObj = new JSONObject();
                String className = classMatcher.group(1);
                String classBody = classMatcher.group(2);

                modelObj.put("name", className);
                modelObj.put("model_options", parseModelOptions(classBody));
                modelObj.put("fields", parseFields(classBody, pythonCode));

                modelsArray.put(modelObj);
            }

            root.put("models", modelsArray);
            return root.toString(4);

        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private static JSONObject parseModelOptions(String body) throws JSONException {
        JSONObject options = new JSONObject();
        
        options.put("verbose_name", extractRegex(body, "verbose_name\\s*=\\s*['\"](.+?)['\"]", "non"));
        options.put("verbose_name_plural", extractRegex(body, "verbose_name_plural\\s*=\\s*['\"](.+?)['\"]", "non"));
        options.put("db_table", extractRegex(body, "db_table\\s*=\\s*['\"](.+?)['\"]", "non"));
        options.put("abstract", body.contains("abstract = True"));
        
        return options;
    }

    private static JSONArray parseFields(String classBody, String fullCode) throws JSONException {
        JSONArray fieldsArray = new JSONArray();
        Pattern fieldPattern = Pattern.compile("^\\s*(\\w+)\\s*=\\s*models\\.(\\w+)\\((.*?)\\)", Pattern.MULTILINE);
        Matcher fieldMatcher = fieldPattern.matcher(classBody);

        while (fieldMatcher.find()) {
            JSONObject fieldObj = new JSONObject();
            String fieldName = fieldMatcher.group(1);
            String fieldType = fieldMatcher.group(2);
            String fieldArgs = fieldMatcher.group(3);

            fieldObj.put("name", fieldName);
            fieldObj.put("type", fieldType);
            fieldObj.put("field_options", parseFieldOptions(fieldType, fieldArgs, fullCode));

            fieldsArray.put(fieldObj);
        }
        return fieldsArray;
    }

    private static JSONObject parseFieldOptions(String fieldType, String args, String fullCode) throws JSONException {
        JSONObject opts = new JSONObject();
        
        // 1. استخراج العلاقة (to) - تم التعديل لجلب المسار الكامل core.Country
        if (fieldType.matches("ForeignKey|OneToOneField|ManyToManyField")) {
            // النمط يبحث عن النص الأول سواء كان بين علامات تنصيص أو لا
            Pattern toPattern = Pattern.compile("(?:to\\s*=\\s*)?['\"]?([\\w\\.]+)['\"]?");
            Matcher toMatcher = toPattern.matcher(args.trim());
            if (toMatcher.find()) {
                String target = toMatcher.group(1);
                // تم إزالة سطر التقطيع (substring) ليبقى الاسم كاملاً
                opts.put("to", target);
            }
        }

        // 2. استخراج المعاملات المسماة بما فيها default
        String[] keys = {"verbose_name", "max_length", "null", "blank", "unique", "on_delete", "choices", "default"};
        
        for (String key : keys) {
            Pattern p = Pattern.compile(key + "\\s*=\\s*(['\"].+?['\"]|[\\w\\.]+)");
            Matcher m = p.matcher(args);
            if (m.find()) {
                String value = m.group(1).replace("'", "").replace("\"", "");

                if (key.equals("choices")) {
                    opts.put("choices", getChoicesAsString(value, fullCode));
                } else if (value.equalsIgnoreCase("True")) {
                    opts.put(key, true);
                } else if (value.equalsIgnoreCase("False")) {
                    opts.put(key, false);
                } else {
                    opts.put(key, value);
                }
            }
        }

        // 3. استخراج verbose_name الموضعي (Positional Argument)
        if (!opts.has("verbose_name")) {
            if (!fieldType.matches("ForeignKey|OneToOneField|ManyToManyField")) {
                // يبحث عن أول قيمة نصية إذا لم تكن متبوعة بـ '='
                Matcher positionalMatcher = Pattern.compile("^\\s*['\"](.+?)['\"](?!\\s*=)").matcher(args);
                if (positionalMatcher.find()) {
                    opts.put("verbose_name", positionalMatcher.group(1));
                }
            }
        }

        if (!opts.has("verbose_name")) opts.put("verbose_name", "non");

        return opts;
    }

    private static String extractRegex(String source, String regex, String defaultValue) {
        Matcher m = Pattern.compile(regex).matcher(source);
        return m.find() ? m.group(1) : defaultValue;
    }

    private static String getChoicesAsString(String choicesName, String fullCode) {
        try {
            Pattern p = Pattern.compile(choicesName + "\\s*=\\s*\\[([\\s\\S]*?)\\]");
            Matcher m = p.matcher(fullCode);
            if (m.find()) {
                List<String> values = new ArrayList<>();
                Matcher pairMatcher = Pattern.compile("\\(['\"].+?['\"]\\s*,\\s*['\"](.+?)['\"]\\)").matcher(m.group(1));
                while (pairMatcher.find()) {
                    values.add(pairMatcher.group(1));
                }
                return String.join(", ", values);
            }
        } catch (Exception e) {
            return choicesName;
        }
        return choicesName;
    }
}
