package ayman.djangogenerator;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonToErdConverter {
    
    public interface ConversionCallback {
        void onSuccess(String htmlFilePath);
        void onError(String errorMessage);
    }
    
    public static void convertJsonToErdHtml(Context context, String jsonString, String outputFileName, ConversionCallback callback) {
        try {
            JSONObject projectJson = new JSONObject(jsonString);
            String htmlContent = generateErdHtml(projectJson);
            
            // حفظ الملف في الذاكرة الداخلية للتطبيق
            File outputDir = context.getFilesDir();
            File htmlFile = new File(outputDir, outputFileName + ".html");
            
            try (FileOutputStream fos = new FileOutputStream(htmlFile)) {
                fos.write(htmlContent.getBytes("UTF-8"));
                callback.onSuccess(htmlContent);
            } catch (IOException e) {
                callback.onError("خطأ في حفظ الملف: " + e.getMessage());
            }
            
        } catch (JSONException e) {
            callback.onError("خطأ في تحليل JSON: " + e.getMessage());
        }
    }
    
    private static String generateErdHtml(JSONObject projectJson) throws JSONException {
        StringBuilder html = new StringBuilder();
        
        // استخراج بيانات المشروع
        String projectName = projectJson.getString("project_name");
        String description = projectJson.getString("description");
        
        // استخراج التطبيقات والموديلات
        JSONArray apps = projectJson.getJSONArray("apps");
        List<AppData> appDataList = new ArrayList<>();
        Set<String> relations = new HashSet<>();
        
        // تحليل البيانات
        for (int i = 0; i < apps.length(); i++) {
            JSONObject app = apps.getJSONObject(i);
            String appName = app.getString("name");
            String verboseName = app.getString("verbose_name");
            
            JSONArray models = app.getJSONArray("models");
            List<ModelData> modelDataList = new ArrayList<>();
            
            for (int j = 0; j < models.length(); j++) {
                JSONObject model = models.getJSONObject(j);
                String modelName = model.getString("name");
                JSONObject modelOptions = model.getJSONObject("model_options");
                String modelVerboseName = modelOptions.getString("verbose_name");
                
                JSONArray fields = model.getJSONArray("fields");
                List<FieldData> fieldDataList = new ArrayList<>();
                
                for (int k = 0; k < fields.length(); k++) {
                    JSONObject field = fields.getJSONObject(k);
                    String fieldName = field.getString("name");
                    String fieldType = field.getString("type");
                    JSONObject fieldOptions = field.getJSONObject("field_options");
                    
                    FieldData fieldData = new FieldData(fieldName, fieldType, fieldOptions);
                    fieldDataList.add(fieldData);
                    
                    // اكتشاف العلاقات
                    if (fieldType.equals("ForeignKey") && fieldOptions.has("to")) {
                        String toField = fieldOptions.getString("to");
                        relations.add(appName + "." + modelName + " --> " + toField);
                    }
                }
                
                ModelData modelData = new ModelData(modelName, modelVerboseName, fieldDataList);
                modelDataList.add(modelData);
            }
            
            AppData appData = new AppData(appName, verboseName, modelDataList);
            appDataList.add(appData);
        }
        /*
        // بناء HTML
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='ar' dir='rtl'>\n");
        html.append("<head>\n");
        html.append("    <meta charset='UTF-8'>\n");
        html.append("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("    <title>").append(projectName).append(" - ERD Diagram</title>\n");
        html.append("    <style>\n");
        html.append("        * {\n");
        html.append("            margin: 0;\n");
        html.append("            padding: 0;\n");
        html.append("            box-sizing: border-box;\n");
        html.append("            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n");
        html.append("        }\n");
        html.append("        body {\n");
        html.append("            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n");
        html.append("            min-height: 100vh;\n");
        html.append("            padding: 20px;\n");
        html.append("            direction: rtl;\n");
        html.append("        }\n");
        html.append("        .container {\n");
        html.append("            max-width: 1400px;\n");
        html.append("            margin: 0 auto;\n");
        html.append("            background: rgba(255, 255, 255, 0.95);\n");
        html.append("            border-radius: 20px;\n");
        html.append("            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);\n");
        html.append("            overflow: hidden;\n");
        html.append("        }\n");
        html.append("        .header {\n");
        html.append("            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n");
        html.append("            color: white;\n");
        html.append("            padding: 30px;\n");
        html.append("            text-align: center;\n");
        html.append("            border-bottom: 5px solid #4a5568;\n");
        html.append("        }\n");
        html.append("        .header h1 {\n");
        html.append("            font-size: 2.8em;\n");
        html.append("            margin-bottom: 10px;\n");
        html.append("            text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);\n");
        html.append("        }\n");
        html.append("        .header p {\n");
        html.append("            font-size: 1.2em;\n");
        html.append("            opacity: 0.9;\n");
        html.append("        }\n");
        html.append("        .diagram-container {\n");
        html.append("            padding: 30px;\n");
        html.append("            display: grid;\n");
        html.append("            grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));\n");
        html.append("            gap: 25px;\n");
        html.append("        }\n");
        html.append("        .app-section {\n");
        html.append("            background: #f8fafc;\n");
        html.append("            border-radius: 15px;\n");
        html.append("            padding: 20px;\n");
        html.append("            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);\n");
        html.append("            border: 2px solid #e2e8f0;\n");
        html.append("            transition: transform 0.3s ease, box-shadow 0.3s ease;\n");
        html.append("        }\n");
        html.append("        .app-section:hover {\n");
        html.append("            transform: translateY(-5px);\n");
        html.append("            box-shadow: 0 15px 40px rgba(0, 0, 0, 0.15);\n");
        html.append("        }\n");
        html.append("        .app-title {\n");
        html.append("            background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);\n");
        html.append("            color: white;\n");
        html.append("            padding: 15px;\n");
        html.append("            border-radius: 10px;\n");
        html.append("            margin-bottom: 20px;\n");
        html.append("            text-align: center;\n");
        html.append("            font-size: 1.3em;\n");
        html.append("            font-weight: bold;\n");
        html.append("        }\n");
        html.append("        .model-card {\n");
        html.append("            background: white;\n");
        html.append("            border-radius: 12px;\n");
        html.append("            padding: 20px;\n");
        html.append("            margin-bottom: 20px;\n");
        html.append("            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);\n");
        html.append("            border-left: 5px solid #48bb78;\n");
        html.append("        }\n");
        html.append("        .model-header {\n");
        html.append("            display: flex;\n");
        html.append("            justify-content: space-between;\n");
        html.append("            align-items: center;\n");
        html.append("            margin-bottom: 15px;\n");
        html.append("            padding-bottom: 10px;\n");
        html.append("            border-bottom: 2px solid #e2e8f0;\n");
        html.append("        }\n");
        html.append("        .model-name {\n");
        html.append("            font-size: 1.4em;\n");
        html.append("            color: #2d3748;\n");
        html.append("            font-weight: bold;\n");
        html.append("        }\n");
        html.append("        .model-verbose {\n");
        html.append("            font-size: 1em;\n");
        html.append("            color: #718096;\n");
        html.append("            background: #edf2f7;\n");
        html.append("            padding: 5px 10px;\n");
        html.append("            border-radius: 20px;\n");
        html.append("        }\n");
        html.append("        .field-list {\n");
        html.append("            list-style: none;\n");
        html.append("        }\n");
        html.append("        .field-item {\n");
        html.append("            display: flex;\n");
        html.append("            justify-content: space-between;\n");
        html.append("            align-items: center;\n");
        html.append("            padding: 12px;\n");
        html.append("            margin-bottom: 8px;\n");
        html.append("            background: #f7fafc;\n");
        html.append("            border-radius: 8px;\n");
        html.append("            border-right: 4px solid #4299e1;\n");
        html.append("            transition: background 0.3s ease;\n");
        html.append("        }\n");
        html.append("        .field-item:hover {\n");
        html.append("            background: #ebf8ff;\n");
        html.append("        }\n");
        html.append("        .field-name {\n");
        html.append("            font-weight: bold;\n");
        html.append("            color: #2d3748;\n");
        html.append("        }\n");
        html.append("        .field-type {\n");
        html.append("            background: #c6f6d5;\n");
        html.append("            color: #22543d;\n");
        html.append("            padding: 4px 12px;\n");
        html.append("            border-radius: 15px;\n");
        html.append("            font-size: 0.9em;\n");
        html.append("            font-weight: bold;\n");
        html.append("        }\n");
        html.append("        .foreign-key {\n");
        html.append("            background: #fed7d7 !important;\n");
        html.append("            color: #742a2a !important;\n");
        html.append("        }\n");
        html.append("        .relations-section {\n");
        html.append("            background: #fffaf0;\n");
        html.append("            border-radius: 15px;\n");
        html.append("            padding: 25px;\n");
        html.append("            margin: 30px;\n");
        html.append("            border: 2px dashed #d69e2e;\n");
        html.append("        }\n");
        html.append("        .relations-title {\n");
        html.append("            color: #744210;\n");
        html.append("            font-size: 1.5em;\n");
        html.append("            margin-bottom: 20px;\n");
        html.append("            text-align: center;\n");
        html.append("        }\n");
        html.append("        .relation-item {\n");
        html.append("            background: white;\n");
        html.append("            padding: 15px;\n");
        html.append("            margin-bottom: 10px;\n");
        html.append("            border-radius: 10px;\n");
        html.append("            border-left: 4px solid #d69e2e;\n");
        html.append("            display: flex;\n");
        html.append("            align-items: center;\n");
        html.append("            justify-content: space-between;\n");
        html.append("        }\n");
        html.append("        .arrow {\n");
        html.append("            color: #d69e2e;\n");
        html.append("            font-size: 1.2em;\n");
        html.append("            margin: 0 10px;\n");
        html.append("        }\n");
        html.append("        @media (max-width: 768px) {\n");
        html.append("            .diagram-container {\n");
        html.append("                grid-template-columns: 1fr;\n");
        html.append("            }\n");
        html.append("            .header h1 {\n");
        html.append("                font-size: 2em;\n");
        html.append("            }\n");
        html.append("        }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        */
        
                // بناء HTML كامل بعرض سطح المكتب
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='ar' dir='rtl'>\n");
        html.append("<head>\n");
        html.append("    <meta charset='UTF-8'>\n");
        html.append("    <meta name='viewport' content='width=1024, initial-scale=1.0'>\n");
        html.append("    <title>").append(projectName).append(" - ERD Diagram</title>\n");
        html.append("    <style>\n");
        html.append("        * {\n");
        html.append("            margin: 0;\n");
        html.append("            padding: 0;\n");
        html.append("            box-sizing: border-box;\n");
        html.append("            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n");
        html.append("        }\n");
        html.append("        body {\n");
        html.append("            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n");
        html.append("            min-height: 100vh;\n");
        html.append("            padding: 10px; /* تقليل الحواف الخارجية لزيادة المساحة */\n");
        html.append("            direction: rtl;\n");
        html.append("            width: 100%;\n");
        html.append("        }\n");
        html.append("        .container {\n");
        html.append("            width: 98%; /* جعل الحاوية تأخذ 98% من عرض الشاشة */\n");
        html.append("            max-width: 100%; /* إلغاء الحد الأقصى الثابت */\n");
        html.append("            margin: 0 auto;\n");
        html.append("            background: rgba(255, 255, 255, 0.95);\n");
        html.append("            border-radius: 15px;\n");
        html.append("            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);\n");
        html.append("            overflow: hidden;\n");
        html.append("        }\n");
        html.append("        .header {\n");
        html.append("            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n");
        html.append("            color: white;\n");
        html.append("            padding: 40px 20px;\n");
        html.append("            text-align: center;\n");
        html.append("            border-bottom: 5px solid rgba(0,0,0,0.1);\n");
        html.append("        }\n");
        html.append("        .header h1 {\n");
        html.append("            font-size: 3em;\n");
        html.append("            margin-bottom: 10px;\n");
        html.append("            text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);\n");
        html.append("        }\n");
        html.append("        .header p {\n");
        html.append("            font-size: 1.3em;\n");
        html.append("            opacity: 0.9;\n");
        html.append("        }\n");
        html.append("        .diagram-container {\n");
        html.append("            padding: 25px;\n");
        html.append("            display: grid;\n");
        // تعديل الـ Grid ليستوعب عدد أكبر من الأعمدة في الشاشات الواسعة جداً
        html.append("            grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));\n");
        html.append("            gap: 20px;\n");
        html.append("        }\n");
        html.append("        .app-section {\n");
        html.append("            background: #f8fafc;\n");
        html.append("            border-radius: 15px;\n");
        html.append("            padding: 20px;\n");
        html.append("            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);\n");
        html.append("            border: 1px solid #e2e8f0;\n");
        html.append("            transition: transform 0.3s ease;\n");
        html.append("        }\n");
        html.append("        .app-section:hover {\n");
        html.append("            transform: translateY(-5px);\n");
        html.append("        }\n");
        html.append("        .app-title {\n");
        html.append("            background: linear-gradient(135deg, #4299e1 0%, #3182ce 100%);\n");
        html.append("            color: white;\n");
        html.append("            padding: 12px;\n");
        html.append("            border-radius: 8px;\n");
        html.append("            margin-bottom: 15px;\n");
        html.append("            text-align: center;\n");
        html.append("            font-size: 1.2em;\n");
        html.append("            font-weight: bold;\n");
        html.append("        }\n");
        html.append("        .model-card {\n");
        html.append("            background: white;\n");
        html.append("            border-radius: 12px;\n");
        html.append("            padding: 15px;\n");
        html.append("            margin-bottom: 15px;\n");
        html.append("            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);\n");
        html.append("            border-right: 5px solid #48bb78; /* تغيير الجانب ليتناسب مع RTL */\n");
        html.append("        }\n");
        html.append("        .model-header {\n");
        html.append("            display: flex;\n");
        html.append("            justify-content: space-between;\n");
        html.append("            align-items: center;\n");
        html.append("            margin-bottom: 12px;\n");
        html.append("            padding-bottom: 8px;\n");
        html.append("            border-bottom: 1px solid #edf2f7;\n");
        html.append("        }\n");
        html.append("        .model-name {\n");
        html.append("            font-size: 1.3em;\n");
        html.append("            color: #2d3748;\n");
        html.append("            font-weight: bold;\n");
        html.append("        }\n");
        html.append("        .model-verbose {\n");
        html.append("            font-size: 0.9em;\n");
        html.append("            color: #718096;\n");
        html.append("            background: #edf2f7;\n");
        html.append("            padding: 3px 10px;\n");
        html.append("            border-radius: 15px;\n");
        html.append("        }\n");
        html.append("        .field-list {\n");
        html.append("            list-style: none;\n");
        html.append("        }\n");
        html.append("        .field-item {\n");
        html.append("            display: flex;\n");
        html.append("            justify-content: space-between;\n");
        html.append("            align-items: center;\n");
        html.append("            padding: 10px;\n");
        html.append("            margin-bottom: 6px;\n");
        html.append("            background: #fcfcfc;\n");
        html.append("            border-radius: 6px;\n");
        html.append("            border-left: 3px solid #e2e8f0;\n");
        html.append("        }\n");
        html.append("        .field-item:hover {\n");
        html.append("            background: #ebf8ff;\n");
        html.append("        }\n");
        html.append("        .field-name {\n");
        html.append("            font-weight: 600;\n");
        html.append("            color: #4a5568;\n");
        html.append("        }\n");
        html.append("        .field-type {\n");
        html.append("            background: #c6f6d5;\n");
        html.append("            color: #22543d;\n");
        html.append("            padding: 2px 10px;\n");
        html.append("            border-radius: 12px;\n");
        html.append("            font-size: 0.85em;\n");
        html.append("        }\n");
        html.append("        .foreign-key {\n");
        html.append("            background: #fed7d7 !important;\n");
        html.append("            color: #742a2a !important;\n");
        html.append("        }\n");
        html.append("        .relations-section {\n");
        html.append("            background: #fffaf0;\n");
        html.append("            border-radius: 15px;\n");
        html.append("            padding: 25px;\n");
        html.append("            margin: 25px;\n");
        html.append("            border: 2px dashed #d69e2e;\n");
        html.append("        }\n");
        html.append("        .relations-title {\n");
        html.append("            color: #744210;\n");
        html.append("            font-size: 1.6em;\n");
        html.append("            margin-bottom: 20px;\n");
        html.append("            text-align: center;\n");
        html.append("        }\n");
        html.append("        .relation-item {\n");
        html.append("            background: white;\n");
        html.append("            padding: 12px 20px;\n");
        html.append("            margin-bottom: 10px;\n");
        html.append("            border-radius: 8px;\n");
        html.append("            border-right: 4px solid #d69e2e;\n");
        html.append("            display: inline-flex; /* لجعل العلاقات تصطف بجانب بعضها إذا سمح العرض */\n");
        html.append("            margin-left: 10px;\n");
        html.append("            align-items: center;\n");
        html.append("            box-shadow: 0 2px 5px rgba(0,0,0,0.05);\n");
        html.append("        }\n");
        html.append("        @media (max-width: 768px) {\n");
        html.append("            .container { width: 95%; }\n");
        html.append("            .diagram-container {\n");
        html.append("                grid-template-columns: 1fr;\n");
        html.append("            }\n");
        html.append("            .header h1 { font-size: 2em; }\n");
        html.append("        }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
         /*
        // الهيدر
        html.append("    <div class='container'>\n");
        html.append("        <div class='header'>\n");
        html.append("            <h1>").append(projectName).append("</h1>\n");
        html.append("            <p>").append(description).append("</p>\n");
        html.append("            <p style='margin-top: 15px; font-size: 1em;'>مخطط قاعدة البيانات (ERD Diagram)</p>\n");
        html.append("        </div>\n");
        
        // مخطط ERD
        html.append("        <div class='diagram-container'>\n");
        
        // عرض التطبيقات والموديلات
        for (AppData app : appDataList) {
            html.append("            <div class='app-section'>\n");
            html.append("                <div class='app-title'>\n");
            html.append("                    ").append(app.verboseName).append(" (").append(app.name).append(")\n");
            html.append("                </div>\n");
            
            for (ModelData model : app.models) {
                html.append("                <div class='model-card'>\n");
                html.append("                    <div class='model-header'>\n");
                html.append("                        <span class='model-name'>").append(model.name).append("</span>\n");
                html.append("                        <span class='model-verbose'>").append(model.verboseName).append("</span>\n");
                html.append("                    </div>\n");
                html.append("                    <ul class='field-list'>\n");
                
                for (FieldData field : model.fields) {
                    String typeClass = field.type.equals("ForeignKey") ? "foreign-key" : "";
                    html.append("                        <li class='field-item'>\n");
                    html.append("                            <span class='field-name'>").append(field.name).append("</span>\n");
                    html.append("                            <span class='field-type ").append(typeClass).append("'>\n");
                    html.append("                                ").append(field.type).append("\n");
                    html.append("                            </span>\n");
                    html.append("                        </li>\n");
                }
                
                html.append("                    </ul>\n");
                html.append("                </div>\n");
            }
            
            html.append("            </div>\n");
        }
        
        html.append("        </div>\n");
        
        // قسم العلاقات
        if (!relations.isEmpty()) {
            html.append("        <div class='relations-section'>\n");
            html.append("            <h2 class='relations-title'>العلاقات بين الجداول</h2>\n");
            
            for (String relation : relations) {
                String[] parts = relation.split(" --> ");
                html.append("            <div class='relation-item'>\n");
                html.append("                <span>").append(parts[0]).append("</span>\n");
                html.append("                <span class='arrow'>⟶</span>\n");
                html.append("                <span>").append(parts[1]).append("</span>\n");
                html.append("            </div>\n");
            }
            
            html.append("        </div>\n");
        }
        
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
        */
                // الهيدر والحاوية الرئيسية
        html.append("    <div class='container'>\n");
        html.append("        <div class='header'>\n");
        html.append("            <h1>").append(projectName).append("</h1>\n");
        html.append("            <p>").append(description).append("</p>\n");
        html.append("            <p style='margin-top: 15px; font-size: 1.1em; font-weight: 300; opacity: 0.8;'>مخطط كيانات قاعدة البيانات (ERD Diagram)</p>\n");
        html.append("        </div>\n");
        
        // مخطط ERD - سيعرض التطبيقات بجانب بعضها في الشاشات الواسعة
        html.append("        <div class='diagram-container'>\n");
        
        for (AppData app : appDataList) {
            html.append("            <div class='app-section'>\n");
            html.append("                <div class='app-title'>\n");
            html.append("                    ").append(app.verboseName).append(" <small style='display:block; font-size: 0.7em; opacity: 0.9;'>(").append(app.name).append(")</small>\n");
            html.append("                </div>\n");
            
            for (ModelData model : app.models) {
                html.append("                <div class='model-card'>\n");
                html.append("                    <div class='model-header'>\n");
                html.append("                        <span class='model-name'>").append(model.name).append("</span>\n");
                html.append("                        <span class='model-verbose'>").append(model.verboseName).append("</span>\n");
                html.append("                    </div>\n");
                html.append("                    <ul class='field-list'>\n");
                
                for (FieldData field : model.fields) {
                    // تمييز مفتاح الربط بلون مختلف
                    String typeClass = field.type.equals("ForeignKey") ? "foreign-key" : "";
                    String icon = field.type.equals("ForeignKey") ? "🔗 " : "🔹 ";
                    
                    html.append("                        <li class='field-item'>\n");
                    html.append("                            <span class='field-name'>").append(icon).append(field.name).append("</span>\n");
                    html.append("                            <span class='field-type ").append(typeClass).append("'>\n");
                    html.append("                                ").append(field.type).append("\n");
                    html.append("                            </span>\n");
                    html.append("                        </li>\n");
                }
                
                html.append("                    </ul>\n");
                html.append("                </div>\n");
            }
            html.append("            </div>\n");
        }
        html.append("        </div>\n");
        
        // قسم العلاقات - تم تحسينه ليتناسب مع العرض الواسع
        if (!relations.isEmpty()) {
            html.append("        <div class='relations-section'>\n");
            html.append("            <h2 class='relations-title'>الروابط والعلاقات بين الجداول</h2>\n");
            html.append("            <div style='display: flex; flex-wrap: wrap; justify-content: center; gap: 10px;'>\n");
            
            for (String relation : relations) {
                String[] parts = relation.split(" --> ");
                if (parts.length == 2) {
                    html.append("            <div class='relation-item'>\n");
                    html.append("                <strong style='color: #2d3748;'>").append(parts[0]).append("</strong>\n");
                    html.append("                <span class='arrow'>&nbsp; ⟵ 🔗 ⟶ &nbsp;</span>\n");
                    html.append("                <strong style='color: #2d3748;'>").append(parts[1]).append("</strong>\n");
                    html.append("            </div>\n");
                }
            }
            
            html.append("            </div>\n");
            html.append("        </div>\n");
        }
        
        html.append("    </div>\n"); // نهاية الـ container
        html.append("    <footer style='text-align: center; padding: 20px; color: white; opacity: 0.7;'>تم إنشاء المخطط تلقائياً بواسطة نظام إدارة المشروع</footer>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();

    }
    
    // كلاسات مساعدة
    private static class AppData {
        String name;
        String verboseName;
        List<ModelData> models;
        
        AppData(String name, String verboseName, List<ModelData> models) {
            this.name = name;
            this.verboseName = verboseName;
            this.models = models;
        }
    }
    
    private static class ModelData {
        String name;
        String verboseName;
        List<FieldData> fields;
        
        ModelData(String name, String verboseName, List<FieldData> fields) {
            this.name = name;
            this.verboseName = verboseName;
            this.fields = fields;
        }
    }
    
    private static class FieldData {
        String name;
        String type;
        JSONObject options;
        
        FieldData(String name, String type, JSONObject options) {
            this.name = name;
            this.type = type;
            this.options = options;
        }
    }
}