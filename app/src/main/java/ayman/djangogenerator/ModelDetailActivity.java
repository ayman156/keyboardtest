package ayman.djangogenerator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class ModelDetailActivity extends AppCompatActivity {
    
    private TextView modelTitleTextView;
    private ListView fieldsListView;
    private Button addFieldButton, backButton;
    
    private DatabaseHelper dbHelper;
    private long modelId;
    private String modelName;
    private List<Field> fieldsList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_detail);
        
        dbHelper = new DatabaseHelper(this);
        
        Intent intent = getIntent();
        modelId = intent.getLongExtra("MODEL_ID", -1);
        modelName = intent.getStringExtra("MODEL_NAME");
        
        initViews();
        setupListeners();
        loadFields();
    }
    
    private void initViews() {
        modelTitleTextView = findViewById(R.id.model_title_textview);
        fieldsListView = findViewById(R.id.fields_list_view);
        addFieldButton = findViewById(R.id.add_field_button);
        backButton = findViewById(R.id.back_button);
        
        modelTitleTextView.setText(modelName + " - الحقول");
    }
    
    private void setupListeners() {
        addFieldButton.setOnClickListener(v -> {
            Intent intent = new Intent(ModelDetailActivity.this, AddFieldActivity.class);
            intent.putExtra("MODEL_ID", modelId);
            startActivityForResult(intent, 1);
        });
        
        backButton.setOnClickListener(v -> finish());
        
        fieldsListView.setOnItemClickListener((parent, view, position, id) -> {
            Field field = fieldsList.get(position);
            Intent intent = new Intent(ModelDetailActivity.this, AddFieldActivity.class);
            intent.putExtra("FIELD_ID", field.getId());
            intent.putExtra("MODEL_ID", modelId);
            startActivityForResult(intent, 2);
        });
        
        fieldsListView.setOnItemLongClickListener((parent, view, position, id) -> {
            Field field = fieldsList.get(position);
            showFieldOptions(field);
            return true;
        });
    }
    
    private void loadFields() {
        fieldsList = dbHelper.getFieldsByModel(modelId);
        
        if (fieldsList.isEmpty()) {
            /*
            String[] emptyMessage = {"لا توجد حقول، أضف حقلاً جديداً"};
            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, emptyMessage);
            fieldsListView.setAdapter(adapter);
            */
        } else {
            String[] fieldNames = new String[fieldsList.size()];
            for (int i = 0; i < fieldsList.size(); i++) {
                Field field = fieldsList.get(i);
                fieldNames[i] = field.getName() + " : " + field.getType();
                
                // عرض بعض الخيارات
                try {
                    JSONObject options = field.getOptions();
                    if (options.has("max_length")) {
                        fieldNames[i] += " (" + options.getInt("max_length") + ")";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, fieldNames);
            fieldsListView.setAdapter(adapter);
        }
    }
    
    private void showFieldOptions(Field field) {
        String[] options = {"تعديل الحقل", "حذف الحقل", "نقل لأعلى", "نقل لأسفل"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("خيارات الحقل: " + field.getName())
               .setItems(options, (dialog, which) -> {
                   switch (which) {
                       case 0: // تعديل
                           editField(field);
                           break;
                       case 1: // حذف
                           deleteField(field);
                           break;
                       case 2: // نقل لأعلى
                           moveFieldUp(field);
                           break;
                       case 3: // نقل لأسفل
                           moveFieldDown(field);
                           break;
                   }
               })
               .show();
    }
    
    private void editField(Field field) {
        Intent intent = new Intent(this, AddFieldActivity.class);
        intent.putExtra("FIELD_ID", field.getId());
        intent.putExtra("MODEL_ID", modelId);
        startActivityForResult(intent, 2);
    }
    
    private void deleteField(Field field) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("تأكيد الحذف")
            .setMessage("هل أنت متأكد من حذف الحقل '" + field.getName() + "'؟")
            .setPositiveButton("نعم", (dialog, which) -> {
                dbHelper.deleteField(field.getId());
                loadFields();
                Toast.makeText(this, "تم حذف الحقل", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("لا", null)
            .show();
    }
    
    private void moveFieldUp(Field field) {
        // تحديث ترتيب الحقول
        int currentOrder = field.getOrder();
        if (currentOrder > 0) {
            // البحث عن الحقل السابق
            for (Field f : fieldsList) {
                if (f.getOrder() == currentOrder - 1) {
                    f.setOrder(currentOrder);
                    dbHelper.updateField(f);
                    break;
                }
            }
            field.setOrder(currentOrder - 1);
            dbHelper.updateField(field);
            loadFields();
        }
    }
    
    private void moveFieldDown(Field field) {
        int currentOrder = field.getOrder();
        if (currentOrder < fieldsList.size() - 1) {
            // البحث عن الحقل التالي
            for (Field f : fieldsList) {
                if (f.getOrder() == currentOrder + 1) {
                    f.setOrder(currentOrder);
                    dbHelper.updateField(f);
                    break;
                }
            }
            field.setOrder(currentOrder + 1);
            dbHelper.updateField(field);
            loadFields();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadFields();
        }
    }
}