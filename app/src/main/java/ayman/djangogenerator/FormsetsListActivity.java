package ayman.djangogenerator;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FormsetsListActivity extends Activity {
    
    private DatabaseHelper dbHelper;
    private long projectId;
    private ListView formsetsListView;
    private List<FormsetConfig> formsetsList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formsets_list);
        
        dbHelper = new DatabaseHelper(this);
        projectId = getIntent().getLongExtra("PROJECT_ID", -1);
        
        formsetsListView = findViewById(R.id.formsets_list);
        loadFormsets();
        
        // زر إضافة جديد
        Button addBtn = findViewById(R.id.add_formset_btn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 
                 Toast.makeText(FormsetsListActivity.this, 
                    "Use back button and create new formset", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /*
    private void loadFormsets() {
    // 1. تهيئة القائمة الأساسية التي ستعرض في الـ ListView
    formsetsList = new ArrayList<>();
    
    // 2. جلب كل الموديلات المرتبطة بهذا المشروع
    List<ModelObj> models = dbHelper.getModelsObjectsByProject(projectId);
    
    // 3. الدوران على كل موديل لجلب الـ Formsets الخاصة به من قاعدة البيانات
    for (ModelObj model : models) {
        // نستخدم دالة في dbHelper تجلب قائمة FormsetConfig بناءً على ID الموديل الأب
        // ملاحظة: تأكد أن اسم الدالة في الـ DatabaseHelper لديك هو getFormsetsByModelId أو ما يشبهه
        List<FormsetConfig> modelFormsets = dbHelper.getFormsetsByModelId(model.id);
        
        if (modelFormsets != null && !modelFormsets.isEmpty()) {
            formsetsList.addAll(modelFormsets);
        }
    }
    
    // 4. تجهيز النصوص التي ستظهر للمستخدم في القائمة
    List<String> displayItems = new ArrayList<>();
    for (FormsetConfig formset : formsetsList) {
        String parentName = dbHelper.getModelNameById(formset.getParentModelId());
        String childName = dbHelper.getModelNameById(formset.getChildModelId());
        
        // التحقق من أن الأسماء ليست فارغة لتجنب الـ NullPointerException
        String displayName = (parentName != null ? parentName : "Unknown") 
                           + " → " 
                           + (childName != null ? childName : "Unknown");
        displayItems.add(displayName);
    }
    
    // 5. إذا كانت القائمة فارغة تماماً، نعرض رسالة تنبيه
    if (displayItems.isEmpty()) {
        displayItems.add("No formsets created yet");
    }
    
    // 6. ربط البيانات بالـ ListView عبر الـ Adapter
    ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_list_item_1, displayItems);
    formsetsListView.setAdapter(adapter);
    
    // 7. معالجة النقر على العناصر (فقط إذا كانت القائمة تحتوي بيانات حقيقية)
    formsetsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // نتحقق أن النقر تم على عنصر بيانات وليس على رسالة "No formsets"
            if (!formsetsList.isEmpty() && position < formsetsList.size()) {
                FormsetConfig formset = formsetsList.get(position);
                showFormsetOptions(formset);
            }
        }
    });
}

  */
    
    
    
    
    private void loadFormsets() {
    // 1. جلب كل الـ Formsets الخاصة بالمشروع مباشرة (أداء أسرع بكثير)
    formsetsList = dbHelper.getAllFormsetsForProject(projectId);
    
    // 2. تجهيز القائمة للعرض
    List<String> displayItems = new ArrayList<>();
    
    if (formsetsList == null || formsetsList.isEmpty()) {
        displayItems.add("No formsets created yet");
    } else {
        for (FormsetConfig formset : formsetsList) {
            // جلب أسماء الموديلات لعرضها
            String parentName = dbHelper.getModelNameById(formset.getParentModelId());
            String childName = dbHelper.getModelNameById(formset.getChildModelId());
            
            String displayName = (parentName != null ? parentName : "Unknown") 
                               + " → " 
                               + (childName != null ? childName : "Unknown");
            displayItems.add(displayName);
        }
    }
    
    // 3. تحديث الـ ListView
    ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_list_item_1, displayItems);
    formsetsListView.setAdapter(adapter);
    
    // 4. معالجة النقر
    formsetsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // نتأكد أننا لم نضغط على رسالة "No formsets"
            if (formsetsList != null && !formsetsList.isEmpty() && position < formsetsList.size()) {
                FormsetConfig formset = formsetsList.get(position);
                showFormsetOptions(formset);
            }
        }
    });
}

    
    
    
    /*
    private void loadFormsets() {
        // جمع كل الـ Formsets من جميع النماذج في المشروع
        formsetsList = new ArrayList<FormsetConfig>();
        
        // جلب كل النماذج في المشروع
        List<ModelObj> models = dbHelper.getModelsObjectsByProject(projectId);
        
        for (ModelObj model : models) {
            
            List<FormsetConfig> modelFormsets = new ArrayList<FormsetConfig>();
            // formsetsList.addAll(modelFormsets);
        }
        
        // إنشاء Adapter مبسط
        List<String> displayItems = new ArrayList<String>();
        for (FormsetConfig formset : formsetsList) {
            String parentName = dbHelper.getModelNameById(formset.getParentModelId());
            String childName = dbHelper.getModelNameById(formset.getChildModelId());
            displayItems.add(parentName + " → " + childName);
        }
        
        if (displayItems.isEmpty()) {
            displayItems.add("No formsets created yet");
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, displayItems);
        formsetsListView.setAdapter(adapter);
        
        // عند النقر على عنصر
        formsetsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (formsetsList.size() > position) {
                    FormsetConfig formset = formsetsList.get(position);
                    showFormsetOptions(formset);
                }
            }
        });
    }*/
    
    private void showFormsetOptions(FormsetConfig formset) {
        String[] options = {
            "Generate Code",
            "Delete Formset"
        };
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Formset Options");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Generate Code
                    generateFormsetCode(formset);
                    break;
                case 1: // Delete
                    deleteFormset(formset);
                    break;
            }
        });
        builder.show();
    }
    
    private void generateFormsetCode(FormsetConfig formset) {
        try {
            JSONObject projectJson = dbHelper.generateProjectJSON(projectId);
            String viewsCode = DjangoFormsetGenerator.generateViewsCode(projectJson);
            
            // عرض الكود
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Generated Views Code");
            builder.setMessage(viewsCode);
            
            builder.setPositiveButton("OK", null);
            builder.show();
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private void deleteFormset(FormsetConfig formset) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Formset");
        builder.setMessage("Are you sure you want to delete this formset?");
        
        builder.setPositiveButton("Delete", (dialog, which) -> {
            boolean deleted = dbHelper.deleteFormset(formset.getId());
            if (deleted) {
                Toast.makeText(this, "Formset deleted", Toast.LENGTH_SHORT).show();
                loadFormsets(); // تحديث القائمة
            } else {
                Toast.makeText(this, "Error deleting formset", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadFormsets(); // تحديث القائمة عند العودة
    }
}