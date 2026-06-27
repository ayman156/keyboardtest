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

public class AppDetailActivity extends AppCompatActivity {
    
    private TextView appTitleTextView;
    private ListView modelsListView;
    private Button addModelButton;
    private Button backButton;
    
    private DatabaseHelper dbHelper;
    private long appId;
    private String appName;
    private List<DjangoModel> modelsList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_detail);
        
        dbHelper = new DatabaseHelper(this);
        
        Intent intent = getIntent();
        appId = intent.getLongExtra("APP_ID", -1);
        appName = intent.getStringExtra("APP_NAME");
        
        initViews();
        setupListeners();
        loadModels();
    }
    
    private void initViews() {
        appTitleTextView = findViewById(R.id.app_title_textview);
        modelsListView = findViewById(R.id.models_list_view);
        addModelButton = findViewById(R.id.add_model_button);
        backButton = findViewById(R.id.back_button);
        
        appTitleTextView.setText(appName + " - النماذج");
    }
    
    private void setupListeners() {
        addModelButton.setOnClickListener(v -> {
            Intent intent = new Intent(AppDetailActivity.this, AddModelActivity.class);
            intent.putExtra("APP_ID", appId);
            startActivityForResult(intent, 1);
        });
        
        backButton.setOnClickListener(v -> finish());
        
        modelsListView.setOnItemClickListener((parent, view, position, id) -> {
            DjangoModel model = modelsList.get(position);
            Intent intent = new Intent(AppDetailActivity.this, ModelDetailActivity.class);
            intent.putExtra("MODEL_ID", model.getId());
            intent.putExtra("MODEL_NAME", model.getName());
            startActivity(intent);
        });
        
        modelsListView.setOnItemLongClickListener((parent, view, position, id) -> {
            DjangoModel model = modelsList.get(position);
            showModelOptions(model);
            return true;
        });
    }
    
    private void loadModels() {
        modelsList = dbHelper.getModelsByApp(appId);
        
        if (modelsList.isEmpty()) {
            /*
            String[] emptyMessage = {"لا توجد نماذج، أضف نموذجاً جديداً"};
            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, emptyMessage);
            modelsListView.setAdapter(adapter);
            */
        } else {
            String[] modelNames = new String[modelsList.size()];
            for (int i = 0; i < modelsList.size(); i++) {
                DjangoModel model = modelsList.get(i);
                modelNames[i] = model.getName();
                
                // الحصول على عدد الحقول
                List<Field> fields = dbHelper.getFieldsByModel(model.getId());
                if (fields.size() > 0) {
                    modelNames[i] += " (" + fields.size() + " حقول)";
                }
            }
            
            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, modelNames);
            modelsListView.setAdapter(adapter);
        }
    }
    
    private void showModelOptions(DjangoModel model) {
        String[] options = {"تعديل النموذج", "حذف النموذج", "إضافة حقل", "عرض الحقول"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("خيارات النموذج: " + model.getName())
               .setItems(options, (dialog, which) -> {
                   switch (which) {
                       case 0: // تعديل
                           editModel(model);
                           break;
                       case 1: // حذف
                           deleteModel(model);
                           break;
                       case 2: // إضافة حقل
                           addField(model);
                           break;
                       case 3: // عرض الحقول
                           viewFields(model);
                           break;
                   }
               })
               .show();
    }
    
    private void editModel(DjangoModel model) {
        Intent intent = new Intent(this, AddModelActivity.class);
        intent.putExtra("MODEL_ID", model.getId());
        intent.putExtra("APP_ID", appId);
        startActivityForResult(intent, 2);
    }
    
    private void deleteModel(DjangoModel model) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("تأكيد الحذف")
            .setMessage("هل أنت متأكد من حذف النموذج '" + model.getName() + "'؟")
            .setPositiveButton("نعم", (dialog, which) -> {
                dbHelper.deleteModel(model.getId());
                loadModels();
                Toast.makeText(this, "تم حذف النموذج", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("لا", null)
            .show();
    }
    
    private void addField(DjangoModel model) {
        Intent intent = new Intent(this, AddFieldActivity.class);
        intent.putExtra("MODEL_ID", model.getId());
        startActivityForResult(intent, 3);
    }
    
    private void viewFields(DjangoModel model) {
        Intent intent = new Intent(this, ModelDetailActivity.class);
        intent.putExtra("MODEL_ID", model.getId());
        intent.putExtra("MODEL_NAME", model.getName());
        startActivity(intent);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadModels();
        }
    }
}