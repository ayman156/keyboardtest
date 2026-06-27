package ayman.djangogenerator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class AddFieldActivity extends AppCompatActivity {

    private EditText fieldNameEditText;
    private Spinner fieldTypeSpinner;
    private LinearLayout optionsContainer;
    private Button saveButton, cancelButton;

    private DatabaseHelper dbHelper;
    private Field field;
    private boolean isEditMode = false;
    private long modelId;

    // أنواع الحقول المتاحة في Django
    private static final String[] FIELD_TYPES = {
            "CharField", "TextField", "IntegerField", "DecimalField", "BooleanField",
            "DateField", "DateTimeField", "EmailField", "FileField",
            "ImageField", "ForeignKey", "OneToOneField", "ManyToManyField"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addfield);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupListeners();

        Intent intent = getIntent();
        modelId = intent.getLongExtra("MODEL_ID", -1);

        if (intent.hasExtra("FIELD_ID")) {
            isEditMode = true;
            long fieldId = intent.getLongExtra("FIELD_ID", -1);
            loadFieldFromDb(fieldId);
        } else {
            field = new Field();
            field.setModelId(modelId);
        }
    }

    private void initViews() {
        fieldNameEditText = findViewById(R.id.field_name_edittext);
        fieldTypeSpinner = findViewById(R.id.field_type_spinner);
        optionsContainer = findViewById(R.id.options_container);
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_button);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, FIELD_TYPES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fieldTypeSpinner.setAdapter(adapter);
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> saveField());
        cancelButton.setOnClickListener(v -> finish());

        fieldTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshOptionsUI(FIELD_TYPES[position]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * بناء واجهة الخيارات بناءً على نوع الحقل المختار
     */
    private void refreshOptionsUI(String fieldType) {
        optionsContainer.removeAllViews();

        // 1. الخيار الأساسي لجميع الحقول
        addInputRow("verbose_name", "الاسم الوصفي (verbose_name)", "");

        // 2. منطق الحقول النصية
        if (fieldType.equals("CharField")) {
            addInputRow("max_length", "أقصى طول للحروف (max_length)", "255");
            addInputRow("choices", "قائمة الاختيار (A,B,C)", "");
        }

        // 3. منطق العلاقات (الربط مع نماذج المشروع الحالي)
        if (fieldType.equals("ForeignKey") || fieldType.equals("OneToOneField") || fieldType.equals("ManyToManyField")) {
            
            // جلب معرف المشروع الحالي بناءً على الـ Model الحالي
            long currentProjectId = dbHelper.getProjectIdByModelId(modelId);
            
            // جلب قائمة النماذج في هذا المشروع فقط بتنسيق (app_name.ModelName)
            List<String> projectModels = dbHelper.getModelsByProjectId(currentProjectId);
            
            // إضافة خيارات افتراضية عامة
            if (!projectModels.contains("self")) projectModels.add(0, "self");
            if (!projectModels.contains("auth.User")) projectModels.add("auth.User");

            // عرض Spinner بدلاً من EditText لاختيار النموذج المرتبط
            addSpinnerRowFromList("to", "النموذج المرتبط (Target Model)", projectModels);
            
            addInputRow("related_name", "اسم العلاقة العكسية (related_name)", "");

            if (!fieldType.equals("ManyToManyField")) {
                addSpinnerRow("on_delete", "سلوك الحذف (on_delete)", 
                        new String[]{"models.CASCADE", "models.SET_NULL", "models.PROTECT", "models.SET_DEFAULT"});
            }
        }

        // 4. الخيارات المنطقية المشتركة
        addCheckBoxRow("null", "يسمح بـ NULL في قاعدة البيانات");
        addCheckBoxRow("blank", "يسمح بتركه فارغاً (Blank)");
        addCheckBoxRow("unique", "قيمة فريدة (Unique)");

        // 5. خيارات إضافية
        addInputRow("default", "القيمة الافتراضية (default)", "");
        addInputRow("help_text", "نص المساعدة", "");

        if (isEditMode && field != null) {
            applySavedOptionsToUI();
        }
    }

    // --- توابع مساعدة لبناء الـ UI ---

    private void addInputRow(String key, String hint, String defaultValue) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setText(defaultValue);
        editText.setTag(key);
        optionsContainer.addView(editText);
    }

    private void addCheckBoxRow(String key, String label) {
        CheckBox cb = new CheckBox(this);
        cb.setText(label);
        cb.setTag(key);
        optionsContainer.addView(cb);
    }

    private void addSpinnerRow(String key, String label, String[] items) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setPadding(10, 10, 0, 0);
        Spinner sp = new Spinner(this);
        sp.setTag(key);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        sp.setAdapter(adapter);
        optionsContainer.addView(tv);
        optionsContainer.addView(sp);
    }

    // دالة جديدة لإضافة Spinner من قائمة ديناميكية (List)
    private void addSpinnerRowFromList(String key, String label, List<String> items) {
        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setPadding(10, 20, 0, 0);
        Spinner sp = new Spinner(this);
        sp.setTag(key);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp.setAdapter(adapter);
        optionsContainer.addView(tv);
        optionsContainer.addView(sp);
    }

    // --- منطق حفظ ومعالجة البيانات ---

    private void saveField() {
        String name = fieldNameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "يرجى تسمية الحقل", Toast.LENGTH_SHORT).show();
            return;
        }

        field.setName(name);
        field.setType(fieldTypeSpinner.getSelectedItem().toString());

        JSONObject options = new JSONObject();
        try {
            for (int i = 0; i < optionsContainer.getChildCount(); i++) {
                View view = optionsContainer.getChildAt(i);
                if (view.getTag() != null) {
                    String key = view.getTag().toString();
                    if (view instanceof CheckBox) {
                        options.put(key, ((CheckBox) view).isChecked());
                    } else if (view instanceof EditText) {
                        String val = ((EditText) view).getText().toString();
                        if (!val.isEmpty()) options.put(key, val);
                    } else if (view instanceof Spinner) {
                        options.put(key, ((Spinner) view).getSelectedItem().toString());
                    }
                }
            }
        } catch (JSONException e) { e.printStackTrace(); }

        field.setOptions(options);

        if (isEditMode) dbHelper.updateField(field);
        else dbHelper.addField(field);

        setResult(RESULT_OK);
        finish();
    }

    private void loadFieldFromDb(long fieldId) {
        // نستخدم getFieldsByModel لجلب الحقل المطلوب للتعديل
        List<Field> fields = dbHelper.getFieldsByModel(modelId);
        for (Field f : fields) {
            if (f.getId() == fieldId) {
                field = f;
                fieldNameEditText.setText(field.getName());
                for (int i = 0; i < FIELD_TYPES.length; i++) {
                    if (FIELD_TYPES[i].equals(field.getType())) {
                        fieldTypeSpinner.setSelection(i);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void applySavedOptionsToUI() {
        JSONObject options = field.getOptions();
        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            View view = optionsContainer.getChildAt(i);
            if (view.getTag() != null) {
                String key = view.getTag().toString();
                if (options.has(key)) {
                    try {
                        if (view instanceof CheckBox) {
                            ((CheckBox) view).setChecked(options.getBoolean(key));
                        } else if (view instanceof EditText) {
                            ((EditText) view).setText(options.getString(key));
                        } else if (view instanceof Spinner) {
                            String val = options.getString(key);
                            Spinner sp = (Spinner) view;
                            Adapter adapter = sp.getAdapter();
                            for(int j=0; j<adapter.getCount(); j++) {
                                if(adapter.getItem(j).toString().equals(val)) {
                                    sp.setSelection(j);
                                    break;
                                }
                            }
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                }
            }
        }
    }
}
