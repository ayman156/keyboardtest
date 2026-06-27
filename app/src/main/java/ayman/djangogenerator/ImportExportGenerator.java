package ayman.djangogenerator;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.*;
import java.util.*;

public class ImportExportGenerator {
    private String projectName;
    private JSONObject projectData;
    private File projectDirectory;

    public ImportExportGenerator(Context context, String jsonString) throws JSONException {
        this.projectData = new JSONObject(jsonString);
        this.projectName = projectData.getString("project_name");
        this.projectDirectory = new File(FileUtil.getExternalStorageDir(), "django_projects/" + projectName);
    }

    /**
     * الدالة الرئيسية لتشغيل مولد الاستيراد والتصدير
     */
    public void generate() throws Exception {
        if (!projectDirectory.exists()) return;

        // 1. تحديث الإعدادات لإضافة مكتبة import_export
        updateSettings();

        JSONArray apps = projectData.getJSONArray("apps");
        for (int i = 0; i < apps.length(); i++) {
            JSONObject app = apps.getJSONObject(i);
            
            // 2. إنشاء ملف resources.py لكل تطبيق
            createResourcesFile(app);
            
            // 3. تحديث ملف admin.py ليدعم واجهة الاستيراد والتصدير
            updateAdminFile(app);
        }
    }

    private void updateSettings() throws Exception {
        File settingsFile = new File(projectDirectory, projectName + "/settings.py");
        if (!settingsFile.exists()) return;

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(settingsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
                if (line.contains("INSTALLED_APPS = [")) {
                    sb.append("    'import_export',\n");
                }
            }
        }
        writeFile(settingsFile, sb.toString());
    }

    private void createResourcesFile(JSONObject app) throws Exception {
        String appName = app.getString("name");
        File file = new File(projectDirectory, appName + "/resources.py");
        StringBuilder sb = new StringBuilder();
        
        sb.append("from import_export import resources\n")
          .append("from .models import *\n\n");

        JSONArray models = app.getJSONArray("models");
        for (int i = 0; i < models.length(); i++) {
            String modelName = models.getJSONObject(i).getString("name");
            sb.append("class ").append(modelName).append("Resource(resources.ModelResource):\n")
              .append("    class Meta:\n")
              .append("        model = ").append(modelName).append("\n\n");
        }
        writeFile(file, sb.toString());
    }

    private void updateAdminFile(JSONObject app) throws Exception {
        String appName = app.getString("name");
        File file = new File(projectDirectory, appName + "/admin.py");
        StringBuilder sb = new StringBuilder();
        
        // استيراد المكتبات اللازمة للـ Admin
        sb.append("from django.contrib import admin\n")
          .append("from import_export.admin import ImportExportModelAdmin\n")
          .append("from .models import *\n")
          .append("from .resources import *\n\n");

        JSONArray models = app.getJSONArray("models");
        for (int i = 0; i < models.length(); i++) {
            String modelName = models.getJSONObject(i).getString("name");
            // استخدام ImportExportModelAdmin بدلاً من admin.ModelAdmin التقليدي
            sb.append("@admin.register(").append(modelName).append(")\n")
              .append("class ").append(modelName).append("Admin(ImportExportModelAdmin):\n")
              .append("    resource_class = ").append(modelName).append("Resource\n\n");
        }
        writeFile(file, sb.toString());
    }

    private void writeFile(File file, String content) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
}
