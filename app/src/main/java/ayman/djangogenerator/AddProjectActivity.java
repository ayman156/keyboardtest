package ayman.djangogenerator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddProjectActivity extends AppCompatActivity {
    
    private EditText projectNameEditText, projectDescriptionEditText;
    private LinearLayout settingsContainer;
    private Button saveButton, cancelButton, advancedSettingsButton;
    
    private DatabaseHelper dbHelper;
    private Project project;
    private boolean isEditMode = false;

    // متغيرات الإعدادات المتقدمة (قيم افتراضية)
    private String selectedDatabase = "django.db.backends.sqlite3";
    private String dbName = "my_database.db", dbUser = "", dbPass = "", dbHost = "localhost", dbPort = "";
    private String djangoVersion = "5.2.11";
    private boolean useWhiteNoise = false;
    private boolean useDRF = false;
    private boolean supportExcel = false;
    private boolean crispy = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);
        
        dbHelper = new DatabaseHelper(this);
        initViews();
        setupListeners();
        
        Intent intent = getIntent();
        if (intent.hasExtra("PROJECT_ID")) {
            isEditMode = true;
            long projectId = intent.getLongExtra("PROJECT_ID", -1);
            project = dbHelper.getProject(projectId);
            if (project != null) {
                loadProjectData();
            }
        } else {
            project = new Project();
            project.setSettings(new JSONObject());
        }
        PasswordGuard.checkAndShowLock(AddProjectActivity.this, "000", new PasswordGuard.PasswordListener() {
	@Override
	public void onCorrectPassword() {
		// يتم تنفيذ الكود هنا فقط إذا كان الباسورد صحيحاً 
		// أو إذا كان المستخدم قد سجل دخوله بنجاح في وقت سابق.
	}
});
    }
    
    private void initViews() {
        projectNameEditText = findViewById(R.id.project_name_edittext);
        projectDescriptionEditText = findViewById(R.id.project_description_edittext);
        settingsContainer = findViewById(R.id.settings_container);
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_button);
        advancedSettingsButton = findViewById(R.id.advanced_settings_button);
        
        addDefaultSettings();
    }
    
    private void addDefaultSettings() {
        String[] defaultApps = {
            "jazzmin", "django.contrib.admin", "django.contrib.auth", "django.contrib.contenttypes",
            "django.contrib.sessions", "django.contrib.messages", "django.contrib.staticfiles"
        };
        for (String app : defaultApps) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(app);
            checkBox.setTag(app);
            checkBox.setChecked(true);
            settingsContainer.addView(checkBox);
        }
    }
    
    private void setupListeners() {
        saveButton.setOnClickListener(v -> saveProject());
        cancelButton.setOnClickListener(v -> finish());
        advancedSettingsButton.setOnClickListener(v -> showAdvancedSettingsDialog());
    }

    private void showAdvancedSettingsDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_advanced_settings, null);
        
        Spinner dbSpinner = view.findViewById(R.id.db_spinner);
        LinearLayout dbContainer = view.findViewById(R.id.db_credentials_container);
        EditText dbNameInp = view.findViewById(R.id.db_name_input);
        EditText dbUserInp = view.findViewById(R.id.db_user_input);
        EditText dbPassInp = view.findViewById(R.id.db_password_input);
        EditText dbHostInp = view.findViewById(R.id.db_host_input);
        EditText dbPortInp = view.findViewById(R.id.db_port_input);
        EditText versionInp = view.findViewById(R.id.version_input);
        CheckBox whiteNoiseCb = view.findViewById(R.id.whitenoise_checkbox);
        CheckBox drfCb = view.findViewById(R.id.drf_checkbox);
        CheckBox excelCb = view.findViewById(R.id.excel_checkbox);
        Button applyBtn = view.findViewById(R.id.apply_advanced_settings);
        CheckBox crispy_c = view.findViewById(R.id.crispy);

        //final List<String> dbOptions = Arrays.asList("sqlite3", "postgresql", "mysql", "oracle");
        final List<String> dbOptions = Arrays.asList(
    "django.db.backends.sqlite3", 
    "django.db.backends.postgresql", 
    "django.db.backends.mysql", 
    "django.db.backends.oracle"
);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dbOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dbSpinner.setAdapter(adapter);

        // تعبئة الحقول بالقيم الحالية المخزنة في المتغيرات
        dbSpinner.setSelection(dbOptions.indexOf(selectedDatabase));
        dbNameInp.setText(dbName);
        dbUserInp.setText(dbUser);
        dbPassInp.setText(dbPass);
        dbHostInp.setText(dbHost);
        dbPortInp.setText(dbPort);
        versionInp.setText(djangoVersion);
        whiteNoiseCb.setChecked(useWhiteNoise);
        drfCb.setChecked(useDRF);
        excelCb.setChecked(supportExcel);
        crispy_c.setChecked(crispy);

        dbSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                if (dbOptions.get(position).equals("django.db.backends.sqlite3")) {
                    dbContainer.setVisibility(View.GONE);
                } else {
                    dbContainer.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        applyBtn.setOnClickListener(v -> {
            selectedDatabase = dbSpinner.getSelectedItem().toString();
            dbName = dbNameInp.getText().toString();
            dbUser = dbUserInp.getText().toString();
            dbPass = dbPassInp.getText().toString();
            dbHost = dbHostInp.getText().toString();
            dbPort = dbPortInp.getText().toString();
            djangoVersion = versionInp.getText().toString();
            useWhiteNoise = whiteNoiseCb.isChecked();
            useDRF = drfCb.isChecked();
            supportExcel = excelCb.isChecked();
            crispy = crispy_c.isChecked();
            dialog.dismiss();
            Toast.makeText(this, "تم حفظ الإعدادات المتقدمة", Toast.LENGTH_SHORT).show();
        });

        dialog.setContentView(view);
        dialog.show();
    }
    
    private void loadProjectData() {
        if (project == null) return;
        projectNameEditText.setText(project.getName());
        projectDescriptionEditText.setText(project.getDescription());
        
        try {
            JSONObject settings = project.getSettings();
            if (settings == null) return;

            // 1. تحميل التطبيقات
            JSONArray installedApps = settings.optJSONArray("installed_apps");
            if (installedApps != null) {
                for (int i = 0; i < settingsContainer.getChildCount(); i++) {
                    if (settingsContainer.getChildAt(i) instanceof CheckBox) {
                        CheckBox cb = (CheckBox) settingsContainer.getChildAt(i);
                        cb.setChecked(false);
                        for (int j = 0; j < installedApps.length(); j++) {
                            if (installedApps.getString(j).equals(cb.getTag())) cb.setChecked(true);
                        }
                    }
                }
            }

            // 2. تحميل بيانات قاعدة البيانات من الكائن الفرعي "database"
            if (settings.has("database")) {
                JSONObject dbObj = settings.getJSONObject("database");
                selectedDatabase = dbObj.optString("engine", "sqlite3");
                dbName = dbObj.optString("name", "my_database");
                dbUser = dbObj.optString("user", "");
                dbPass = dbObj.optString("password", ""); // يتوافق مع استخراجك
                dbHost = dbObj.optString("host", "localhost");
                dbPort = dbObj.optString("port", "");
            }

            // 3. تحميل الإعدادات الأخرى
            djangoVersion = settings.optString("django_version", "4.2");
            useWhiteNoise = settings.optBoolean("use_whitenoise", false);
            useDRF = settings.optBoolean("use_drf", false);
            supportExcel = settings.optBoolean("support_excel", false);
            crispy = settings.optBoolean("crispy", false);

        } catch (JSONException e) { e.printStackTrace(); }
    }
    
    private void saveProject() {
        String name = projectNameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "أدخل اسم المشروع", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject settings = new JSONObject();

            // 1. جمع التطبيقات
            JSONArray apps = new JSONArray();
            for (int i = 0; i < settingsContainer.getChildCount(); i++) {
                if (settingsContainer.getChildAt(i) instanceof CheckBox) {
                    CheckBox cb = (CheckBox) settingsContainer.getChildAt(i);
                    if (cb.isChecked()) apps.put(cb.getTag().toString());
                }
            }
          //  if (useDRF) apps.put("rest_framework");
         //   if (supportExcel) apps.put("import_export");
            settings.put("installed_apps", apps);

            // 2. إنشاء كائن قاعدة البيانات (هذا ما تطلبه دالة الاستخراج لديك)
            JSONObject database = new JSONObject();
            database.put("engine", selectedDatabase);
            database.put("name", dbName);
            database.put("user", dbUser);
            database.put("password", dbPass); // استخدمنا password هنا
            database.put("host", dbHost);
            database.put("port", dbPort);
            
            settings.put("database", database); // إضافته ككائن فرعي

            // 3. باقي الإعدادات
            settings.put("django_version", djangoVersion);
            settings.put("use_whitenoise", useWhiteNoise);
            settings.put("use_drf", useDRF);
            settings.put("support_excel", supportExcel);
            settings.put("crispy", crispy);

            project.setName(name);
            project.setDescription(projectDescriptionEditText.getText().toString());
            project.setSettings(settings);

            if (isEditMode) dbHelper.updateProject(project);
            else dbHelper.addProject(project);

            setResult(RESULT_OK);
            finish();
            
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "خطأ في حفظ البيانات", Toast.LENGTH_SHORT).show();
        }
    }
}
