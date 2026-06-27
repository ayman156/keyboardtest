package ayman.djangogenerator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class AddAppActivity extends AppCompatActivity {
    
    private EditText appNameEditText, verboseNameEditText;
    private Button saveButton, cancelButton;
    
    private DatabaseHelper dbHelper;
    private App app;
    private boolean isEditMode = false;
    private long projectId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_app);
        
        dbHelper = new DatabaseHelper(this);
        
        initViews();
        setupListeners();
        
        Intent intent = getIntent();
        projectId = intent.getLongExtra("PROJECT_ID", -1);
        
        if (intent.hasExtra("APP_ID")) {
            isEditMode = true;
            long appId = intent.getLongExtra("APP_ID", -1);
            // يجب البحث عن التطبيق في قاعدة البيانات
            // سأقوم بإنشاء طريقة في DatabaseHelper للحصول على تطبيق محدد
            // للتبسيط، سأقوم بإنشاء تطبيق جديد
            String appn = getIntent().getStringExtra("appname");
            String apv = getIntent().getStringExtra("appv");
            app = new App();
            app.setId(appId);
            app.setProjectId(projectId);
            app.setName(appn);
            app.setVerboseName(apv);
            loadAppData();
        } else {
            app = new App();
            app.setProjectId(projectId);
        }
    }
    
    private void initViews() {
        appNameEditText = findViewById(R.id.app_name_edittext);
        verboseNameEditText = findViewById(R.id.verbose_name_edittext);
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_button);
    }
    
    private void setupListeners() {
        saveButton.setOnClickListener(v -> saveApp());
        cancelButton.setOnClickListener(v -> finish());
    }
    
    private void loadAppData() {
        if (app != null) {
            appNameEditText.setText(app.getName());
            verboseNameEditText.setText(app.getVerboseName());
        }
    }
    
    private void saveApp() {
        String name = appNameEditText.getText().toString().trim();
        String verboseName = verboseNameEditText.getText().toString().trim();
        
        if (name.isEmpty()) {
            Toast.makeText(this, "الرجاء إدخال اسم التطبيق", Toast.LENGTH_SHORT).show();
            return;
        }
        
        app.setName(name);
        app.setVerboseName(verboseName.isEmpty() ? name : verboseName);
        
        if (isEditMode) {
            dbHelper.updateApp(app);
            Toast.makeText(this, "تم تعديل التطبيق بنجاح", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.addApp(app);
            Toast.makeText(this, "تم إضافة التطبيق بنجاح", Toast.LENGTH_SHORT).show();
        }
        
        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}