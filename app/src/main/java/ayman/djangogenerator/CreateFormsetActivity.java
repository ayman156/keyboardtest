package ayman.djangogenerator;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import android.os.Bundle;
import android.content.Intent;
import android.content.Context;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class CreateFormsetActivity extends Activity {
    
    private DatabaseHelper dbHelper;
    private long projectId;
    private Spinner parentModelSpinner, childModelSpinner;
    private EditText relationshipNameEdit, extraFieldsEdit, prefixEdit;
    private CheckBox canDeleteCheck;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_formset);
        
        dbHelper = new DatabaseHelper(this);
        projectId = getIntent().getLongExtra("PROJECT_ID", -1);
        
        // تهيئة عناصر UI
        parentModelSpinner = findViewById(R.id.parent_model_spinner);
        childModelSpinner = findViewById(R.id.child_model_spinner);
        relationshipNameEdit = findViewById(R.id.relationship_name);
        extraFieldsEdit = findViewById(R.id.extra_fields);
        prefixEdit = findViewById(R.id.prefix);
        canDeleteCheck = findViewById(R.id.can_delete);
        
        // تحميل النماذج
        loadModels();
        
        // زر الحفظ
        Button saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFormset();
            }
        });
    }
    
    private void loadModels() {
        // جلب كل النماذج في المشروع
        List<ModelObj> models = dbHelper.getModelsObjectsByProject(projectId);
        
        // Adapter مخصص لعرض الاسم مع ID
        ArrayAdapter<ModelObj> adapter = new ArrayAdapter<ModelObj>(
                this, android.R.layout.simple_spinner_item, models) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view;
                ModelObj model = getItem(position);
                textView.setText(model.name + " (ID: " + model.id + ")");
                return view;
            }
            
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                ModelObj model = getItem(position);
                textView.setText(model.name + " (ID: " + model.id + ")");
                return view;
            }
        };
        
        parentModelSpinner.setAdapter(adapter);
        childModelSpinner.setAdapter(adapter);
    }
    
    private void saveFormset() {
        ModelObj parentModel = (ModelObj) parentModelSpinner.getSelectedItem();
        ModelObj childModel = (ModelObj) childModelSpinner.getSelectedItem();
        
        if (parentModel == null || childModel == null) {
            Toast.makeText(this, "Please select both models", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (parentModel.id == childModel.id) {
            Toast.makeText(this, "Parent and child cannot be the same model", 
                Toast.LENGTH_SHORT).show();
            return;
        }
        
        FormsetConfig formset = new FormsetConfig();
        formset.setParentModelId(parentModel.id);
        formset.setChildModelId(childModel.id);
        
        // إذا لم يدخل اسم العلاقة، نستخدم اسم النموذج الابن
        String relName = relationshipNameEdit.getText().toString().trim();
        if (relName.isEmpty()) {
            relName = childModel.name.toLowerCase() + "_set";
        }
        formset.setRelationshipName(relName);
        
        // Extra fields
        try {
            formset.setExtraFields(Integer.parseInt(extraFieldsEdit.getText().toString()));
        } catch (NumberFormatException e) {
            formset.setExtraFields(1);
        }
        
        formset.setCanDelete(canDeleteCheck.isChecked());
        
        // Prefix
        String prefix = prefixEdit.getText().toString().trim();
        if (prefix.isEmpty()) {
            prefix = childModel.name.toLowerCase();
        }
        formset.setPrefix(prefix);
        
        long id = dbHelper.addFormset(formset);
        
        if (id > 0) {
            Toast.makeText(this, "Formset saved successfully!", Toast.LENGTH_SHORT).show();
            
            // عرض خيارات التوليد
            showGenerationOptions(parentModel, childModel);
        } else {
            Toast.makeText(this, "Error saving formset", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showGenerationOptions(ModelObj parentModel, ModelObj childModel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Generate Django Code");
        builder.setMessage("Generate for: " + parentModel.name + " → " + childModel.name);
        
        builder.setPositiveButton("Full Package", (dialog, which) -> {
            try {
                generateFullCode();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        
        builder.setNegativeButton("Views Only", (dialog, which) -> {
            try {
                generateViewsOnly();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        
        builder.setNeutralButton("Just Save", null);
        builder.show();
    }
    
    private void generateFullCode() throws JSONException {
        JSONObject projectJson = dbHelper.generateProjectJSON(projectId);
        
        String viewsCode = DjangoFormsetGenerator.generateViewsCode(projectJson);
        String formsCode = DjangoFormsetGenerator.generateFormsCode(projectJson);
        String templatesCode = DjangoFormsetGenerator.generateTemplateCode(projectJson);
        String urlsCode = DjangoFormsetGenerator.generateUrlsCode(projectJson);
        
        // حفظ الأكواد في ملفات منفصلة
        saveCodeToFiles(viewsCode, formsCode, templatesCode, urlsCode);
        
        // عرض نافذة النجاح
        Toast.makeText(this, "Code generated successfully!", Toast.LENGTH_LONG).show();
        finish(); // العودة للشاشة السابقة
    }
    
    private void generateViewsOnly() throws JSONException {
        JSONObject projectJson = dbHelper.generateProjectJSON(projectId);
        String viewsCode = DjangoFormsetGenerator.generateViewsCode(projectJson);
        
        // عرض الكود
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Generated Views Code");
        builder.setMessage(viewsCode.substring(0, Math.min(viewsCode.length(), 2000)));
        builder.setPositiveButton("OK", null);
        builder.show();
    }
    
    private void saveCodeToFiles(String viewsCode, String formsCode, 
                                String templatesCode, String urlsCode) {
        try {
            ((ClipboardManager) getSystemService(getApplicationContext().CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", viewsCode));
            // لسنا بحاجة لحفظ في ملفات حالياً، يمكن إضافة هذه الميزة لاحقاً
            // يمكنك إضافة كود حفظ الملفات هنا عندما ترغب
            Toast.makeText(this, "Code generated (file saving not implemented yet)", 
                    Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
        }
    }
}