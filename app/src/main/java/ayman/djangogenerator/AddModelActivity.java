package ayman.djangogenerator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
public class AddModelActivity extends AppCompatActivity {
    
    private EditText modelNameEditText;
    private LinearLayout optionsContainer;
    private Button saveButton, cancelButton;
    
    private DatabaseHelper dbHelper;
    private DjangoModel model;
    private boolean isEditMode = false;
    private long appId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_model);
        
        dbHelper = new DatabaseHelper(this);
        
        initViews();
        setupListeners();
        
        Intent intent = getIntent();
        appId = intent.getLongExtra("APP_ID", -1);
        
        if (intent.hasExtra("MODEL_ID")) {
            isEditMode = true;
            long modelId = intent.getLongExtra("MODEL_ID", -1);
            // البحث عن النموذج في قاعدة البيانات
            List<DjangoModel> models = dbHelper.getModelsByApp(appId);
            model = null;
            for (DjangoModel m : models) {
                if (m.getId() == modelId) {
                    model = m;
                    break;
                }
            }
            if (model == null) {
                model = new DjangoModel();
                model.setId(modelId);
                model.setAppId(appId);
            }
            loadModelData();
        } else {
            model = new DjangoModel();
            model.setAppId(appId);
        }
    }
    
    private void initViews() {
        modelNameEditText = findViewById(R.id.model_name_edittext);
        optionsContainer = findViewById(R.id.options_container);
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_button);
        
        // إضافة خيارات افتراضية للنموذج
        addDefaultOptions();
    }
    
    private void addDefaultOptions() {
        String[] options = {
            "verbose_name",
            "verbose_name_plural",
            "ordering",
            "abstract",
            "proxy",
            "template"
        };
        
        for (String option : options) {
            if (option.equals("abstract") || option.equals("proxy")) {
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(option);
                checkBox.setTag(option);
                optionsContainer.addView(checkBox);
            } else {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                
                EditText editText = new EditText(this);
                editText.setHint(option);
                editText.setTag(option);
                editText.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
                
                row.addView(editText);
                optionsContainer.addView(row);
            }
        }
    }
    
    private void setupListeners() {
        saveButton.setOnClickListener(v -> saveModel());
        cancelButton.setOnClickListener(v -> finish());
    }
    
    private void loadModelData() {
        if (model != null) {
            modelNameEditText.setText(model.getName());
            
            // تحميل الخيارات
            JSONObject options = model.getOptions();
            for (int i = 0; i < optionsContainer.getChildCount(); i++) {
                View child = optionsContainer.getChildAt(i);
                if (child instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) child;
                    String tag = checkBox.getTag().toString();
                    try {
                        if (options.has(tag)) {
                            checkBox.setChecked(options.getBoolean(tag));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (child instanceof LinearLayout) {
                    LinearLayout row = (LinearLayout) child;
                    if (row.getChildAt(0) instanceof EditText) {
                        EditText editText = (EditText) row.getChildAt(0);
                        String tag = editText.getTag().toString();
                        try {
                            if (options.has(tag)) {
                                editText.setText(options.getString(tag));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    
    private void saveModel() {
        String name = modelNameEditText.getText().toString().trim();
        
        if (name.isEmpty()) {
            Toast.makeText(this, "الرجاء إدخال اسم النموذج", Toast.LENGTH_SHORT).show();
            return;
        }
        
        model.setName(name);
        
        // جمع الخيارات
        JSONObject options = new JSONObject();
        for (int i = 0; i < optionsContainer.getChildCount(); i++) {
            View child = optionsContainer.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                String tag = checkBox.getTag().toString();
                try {
                    options.put(tag, checkBox.isChecked());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (child instanceof LinearLayout) {
                LinearLayout row = (LinearLayout) child;
                if (row.getChildAt(0) instanceof EditText) {
                    EditText editText = (EditText) row.getChildAt(0);
                    String tag = editText.getTag().toString();
                    String value = editText.getText().toString().trim();
                    if (!value.isEmpty()) {
                        try {
                            // محاولة تحويل إلى boolean أو عدد إذا أمكن
                            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                                options.put(tag, Boolean.parseBoolean(value.toLowerCase()));
                            } else {
                                try {
                                    options.put(tag, Integer.parseInt(value));
                                } catch (NumberFormatException nfe) {
                                    options.put(tag, value);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        
        model.setOptions(options);
        
        if (isEditMode) {
            dbHelper.updateModel(model);
            Toast.makeText(this, "تم تعديل النموذج بنجاح", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.addModel(model);
            Toast.makeText(this, "تم إضافة النموذج بنجاح", Toast.LENGTH_SHORT).show();
        }
        
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}