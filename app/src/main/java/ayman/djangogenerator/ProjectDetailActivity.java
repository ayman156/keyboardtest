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
public class ProjectDetailActivity extends AppCompatActivity {
    
    private TextView projectTitleTextView;
    private ListView appsListView;
    private Button addAppButton, backButton, generateButton;
    
    private DatabaseHelper dbHelper;
    private long projectId;
    private String projectName;
    private List<App> appsList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);
        
        dbHelper = new DatabaseHelper(this);
        
        // الحصول على بيانات المشروع
        Intent intent = getIntent();
        projectId = intent.getLongExtra("PROJECT_ID", -1);
        projectName = intent.getStringExtra("PROJECT_NAME");
        
        initViews();
        setupListeners();
        loadApps();
    }
    
    private void initViews() {
        projectTitleTextView = findViewById(R.id.project_title_textview);
        appsListView = findViewById(R.id.apps_list_view);
        addAppButton = findViewById(R.id.add_app_button);
        backButton = findViewById(R.id.back_button);
        generateButton = findViewById(R.id.generate_button);
        
        projectTitleTextView.setText(projectName + " - التطبيقات");
    }
    
    private void setupListeners() {
        addAppButton.setOnClickListener(v -> {
            Intent intent = new Intent(ProjectDetailActivity.this, AddAppActivity.class);
            intent.putExtra("PROJECT_ID", projectId);
            startActivityForResult(intent, 1);
        });
        
        backButton.setOnClickListener(v -> finish());
        
        generateButton.setOnClickListener(v -> {
            try {
                JSONObject json = dbHelper.generateProjectJSON(projectId);
                // عرض معاينة JSON
                Intent intent = new Intent(this, JSONPreviewActivity.class);
                intent.putExtra("JSON_DATA", json.toString());
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        
        appsListView.setOnItemClickListener((parent, view, position, id) -> {
            App app = appsList.get(position);
            Intent intent = new Intent(ProjectDetailActivity.this, AppDetailActivity.class);
            intent.putExtra("APP_ID", app.getId());
            intent.putExtra("APP_NAME", app.getName());
            startActivity(intent);
        });
        
        appsListView.setOnItemLongClickListener((parent, view, position, id) -> {
            App app = appsList.get(position);
            showAppOptions(app);
            return true;
        });
    }
    
    private void loadApps() {
        appsList = dbHelper.getAppsByProject(projectId);
        
        if (appsList.isEmpty()) {
            /*
            String[] emptyMessage = {"لا توجد تطبيقات، أضف تطبيقاً جديداً"};
            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, emptyMessage);
            appsListView.setAdapter(adapter);
            */
        } else {
            String[] appNames = new String[appsList.size()];
            for (int i = 0; i < appsList.size(); i++) {
                App app = appsList.get(i);
                appNames[i] = app.getName() + (app.getVerboseName() != null ? 
                    "\n(" + app.getVerboseName() + ")" : "");
            }
            
            android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, appNames);
            appsListView.setAdapter(adapter);
        }
    }
    
    private void showAppOptions(App app) {
       // String[] options = {"تعديل التطبيق", "حذف التطبيق", "إضافة نموذج", "عرض النماذج"};
        String[] options = {"تعديل التطبيق", "حذف التطبيق", "إضافة نموذج", "عرض النماذج","إستيراد model"};
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("خيارات التطبيق: " + app.getName())
               .setItems(options, (dialog, which) -> {
                   switch (which) {
                       case 0: // تعديل
                           editApp(app);
                           break;
                       case 1: // حذف
                           deleteApp(app);
                           break;
                       case 2: // إضافة نموذج
                           addModel(app);
                           break;
                       case 3: // عرض النماذج
                           viewModels(app);
                           break;
                        case 4:
                        impom(app);
                         break;   
                   }
               })
               .show();
    }
    private void impom(App app){
        Intent intent = new Intent(this, ConmodelActivity.class);
        intent.putExtra("APP_ID", app.getId());
        intent.putExtra("APP_NAME", app.getName());
        startActivityForResult(intent, 2);
        
    }
    
    private void editApp(App app) {
        Intent intent = new Intent(this, AddAppActivity.class);
        intent.putExtra("appname", app.getName());
        intent.putExtra("appv", app.getVerboseName());
        intent.putExtra("APP_ID", app.getId());
        intent.putExtra("PROJECT_ID", projectId);
        startActivityForResult(intent, 2);
    }
    
    private void deleteApp(App app) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("تأكيد الحذف")
            .setMessage("هل أنت متأكد من حذف التطبيق '" + app.getName() + "'؟")
            .setPositiveButton("نعم", (dialog, which) -> {
                dbHelper.deleteApp(app.getId());
                loadApps();
                Toast.makeText(this, "تم حذف التطبيق", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("لا", null)
            .show();
    }
    
    private void addModel(App app) {
        Intent intent = new Intent(this, AddModelActivity.class);
        intent.putExtra("APP_ID", app.getId());
        startActivityForResult(intent, 3);
    }
    
    private void viewModels(App app) {
        Intent intent = new Intent(this, AppDetailActivity.class);
        intent.putExtra("APP_ID", app.getId());
        intent.putExtra("APP_NAME", app.getName());
        startActivity(intent);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadApps();
        }
    }
}