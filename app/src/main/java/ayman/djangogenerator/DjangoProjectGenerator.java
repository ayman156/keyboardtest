package ayman.djangogenerator;

import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DjangoProjectGenerator {
    
    private Context context;
    private String projectName;
    private JSONObject projectData;
    private File projectDirectory;
    
    public DjangoProjectGenerator(Context context, String jsonString) throws JSONException {
        this.context = context;
        this.projectData = new JSONObject(jsonString);
        this.projectName = projectData.getString("project_name");
    }
    
    public File generateProject() throws Exception {
        // إنشاء مجلد المشروع
        projectDirectory = new File(FileUtil.getExternalStorageDir(),"django_projects/" + projectName);
        if (!projectDirectory.exists()) {
            projectDirectory.mkdirs();
        }
        
        // إنشاء هيكل المجلدات
        createProjectStructure();
        
        // إنشاء الملفات
        createSettingsFile();
        createUrlsFile();
        
        // إنشاء التطبيقات
        createApps();
        
        // إنشاء ملف requirements.txt
        createRequirementsFile();
        
        // إنشاء ملف manage.py
        createManagePy();
        
        
        // ضغط المشروع
        return createZipArchive();
    }
    
    private void createProjectStructure() {
        new File(projectDirectory, projectName).mkdir();
        new File(projectDirectory, "templates").mkdir();
        new File(projectDirectory, "static").mkdir();
        new File(projectDirectory, "static/css").mkdir();
        new File(projectDirectory, "static/js").mkdir();
        new File(projectDirectory, "static/images").mkdir();
    }
    
    private void createSettingsFile() throws Exception {
        File settingsFile = new File(projectDirectory, 
                                   projectName + "/settings.py");
        
        JSONObject settings = projectData.getJSONObject("settings");
        JSONArray installedApps = settings.getJSONArray("installed_apps");
        JSONObject database = settings.getJSONObject("database");
        
        StringBuilder content = new StringBuilder();
        content.append("import os\n")
               .append("from pathlib import Path\n\n")
               .append("BASE_DIR = Path(__file__).resolve().parent.parent\n\n")
               .append("SECRET_KEY = 'django-insecure-your-secret-key-here'\n\n")
               .append("DEBUG = True\n\n")
               .append("ALLOWED_HOSTS = []\n\n")
               .append("INSTALLED_APPS = [\n");
        
        // إضافة التطبيقات المثبتة مسبقاً
        for (int i = 0; i < installedApps.length(); i++) {
            content.append("    '").append(installedApps.getString(i)).append("',\n");
        }
        
        // إضافة تطبيقات المشروع
        JSONArray apps = projectData.getJSONArray("apps");
        for (int i = 0; i < apps.length(); i++) {
            JSONObject app = apps.getJSONObject(i);
            content.append("    '").append(app.getString("name")).append("',\n");
        }
        content.append("]\n\n");
        
        // إعدادات قاعدة البيانات
        content.append("DATABASES = {\n")
               .append("    'default': {\n")
               .append("        'ENGINE': '").append(database.getString("engine")).append("',\n")
               .append("        'NAME': BASE_DIR / '").append(database.getString("name")).append("',\n")
               .append("    }\n")
               .append("}\n\n");
        
        content.append("STATIC_URL = 'static/'\n")
               .append("STATICFILES_DIRS = [os.path.join(BASE_DIR, 'static')]\n")
               .append("STATIC_ROOT = os.path.join(BASE_DIR, 'staticfiles')\n\n")
               .append("MEDIA_URL = '/media/'\n")
               .append("MEDIA_ROOT = os.path.join(BASE_DIR, 'media')\n\n")
               .append("TEMPLATES = [\n")
               .append("    {\n")
               .append("        'BACKEND': 'django.template.backends.django.DjangoTemplates',\n")
               .append("        'DIRS': [os.path.join(BASE_DIR, 'templates')],\n")
               .append("        'APP_DIRS': True,\n")
               .append("        'OPTIONS': {\n")
               .append("            'context_processors': [\n")
               .append("                'django.template.context_processors.debug',\n")
               .append("                'django.template.context_processors.request',\n")
               .append("                'django.contrib.auth.context_processors.auth',\n")
               .append("                'django.contrib.messages.context_processors.messages',\n")
               .append("            ],\n")
               .append("        },\n")
               .append("    },\n")
               .append("]\n");
        
        writeFile(settingsFile, content.toString());
    }
    
    private void createApps() throws Exception {
        JSONArray apps = projectData.getJSONArray("apps");
        
        for (int i = 0; i < apps.length(); i++) {
            JSONObject app = apps.getJSONObject(i);
            createApp(app);
        }
    }
    
    private void createApp(JSONObject app) throws Exception {
        String appName = app.getString("name");
        File appDir = new File(projectDirectory, appName);
        appDir.mkdir();
        
        // إنشاء __init__.py
        new File(appDir, "__init__.py").createNewFile();
        
        // إنشاء models.py
        createModelsFile(app);
        
        // إنشاء views.py
        createViewsFile(app);
        
        // إنشاء urls.py للتطبيق
        createAppUrlsFile(app);
        
        // إنشاء admin.py
        createAdminFile(app);
        
        // إنشاء apps.py
        createAppsConfigFile(app);
        
        // إنشاء القوالب
        createTemplates(app);
    }
    
    private void createModelsFile(JSONObject app) throws Exception {
        String appName = app.getString("name");
        File modelsFile = new File(projectDirectory, 
                                 appName + "/models.py");
        
        StringBuilder content = new StringBuilder();
        content.append("from django.db import models\n")
               .append("from django.contrib.auth.models import User\n\n");
        
        JSONArray models = app.getJSONArray("models");
        
        for (int i = 0; i < models.length(); i++) {
            JSONObject model = models.getJSONObject(i);
            String modelName = model.getString("name");
            
            content.append("class ").append(modelName).append("(models.Model):\n");
            
            JSONArray fields = model.getJSONArray("fields");
            for (int j = 0; j < fields.length(); j++) {
                JSONObject field = fields.getJSONObject(j);
                String fieldName = field.getString("name");
                String fieldType = field.getString("type");
                
                content.append("    ").append(fieldName).append(" = models.");
                
                switch (fieldType) {
                    case "CharField":
                        content.append("CharField(max_length=")
                               .append(field.getInt("max_length"));
                        break;
                    case "TextField":
                        content.append("TextField()");
                        break;
                    case "DateTimeField":
                        if (field.optBoolean("auto_now_add", false)) {
                            content.append("DateTimeField(auto_now_add=True)");
                        } else {
                            content.append("DateTimeField()");
                        }
                        break;
                    case "ForeignKey":
                        String to = field.getString("to");
                        String onDelete = field.optString("on_delete", "CASCADE");
                        content.append("ForeignKey('")
                               .append(to)
                               .append("', on_delete=models.")
                               .append(onDelete);
                        if (field.has("related_name")) {
                            content.append(", related_name='")
                                   .append(field.getString("related_name"))
                                   .append("'");
                        }
                        content.append(")");
                        break;
                    default:
                        content.append(fieldType).append("()");
                }
                
                // إضافة خيارات إضافية
                if (field.has("null")) {
                    content.append(", null=").append(field.getBoolean("null"));
                }
                if (field.has("blank")) {
                    content.append(", blank=").append(field.getBoolean("blank"));
                }
                
                content.append("\n");
            }
            
            content.append("\n    class Meta:\n")
                   .append("        verbose_name = '").append(modelName).append("'\n")
                   .append("        verbose_name_plural = '").append(modelName).append("s'\n\n")
                   .append("    def __str__(self):\n")
                   .append("        return self.").append(getFirstCharField(fields)).append("\n\n");
        }
        
        writeFile(modelsFile, content.toString());
    }
    
    private String getFirstCharField(JSONArray fields) throws JSONException {
        for (int i = 0; i < fields.length(); i++) {
            JSONObject field = fields.getJSONObject(i);
            if (field.getString("type").equals("CharField")) {
                return field.getString("name");
            }
        }
        return "id";
    }
    
    private void createViewsFile(JSONObject app) throws Exception {
        String appName = app.getString("name");
        File viewsFile = new File(projectDirectory, 
                                appName + "/views.py");
        
        StringBuilder content = new StringBuilder();
        content.append("from django.views.generic import ListView, DetailView\n")
               .append("from .models import *\n\n");
        
        if (app.has("views")) {
            JSONArray views = app.getJSONArray("views");
            
            for (int i = 0; i < views.length(); i++) {
                JSONObject view = views.getJSONObject(i);
                String viewName = view.getString("name");
                String viewType = view.getString("type");
                String modelName = view.getString("model");
                String templateName = view.optString("template_name", "");
                
                content.append("class ").append(viewName).append("(").append(viewType).append("):\n")
                       .append("    model = ").append(modelName).append("\n");
                
                if (!templateName.isEmpty()) {
                    content.append("    template_name = '").append(templateName).append("'\n");
                }
                
                if (viewType.equals("ListView")) {
                    content.append("    context_object_name = '").append(modelName.toLowerCase()).append("_list'\n")
                           .append("    paginate_by = 10\n");
                }
                
                content.append("\n");
            }
        }
        
        writeFile(viewsFile, content.toString());
    }
    
    private void createAppUrlsFile(JSONObject app) throws Exception {
        String appName = app.getString("name");
        File urlsFile = new File(projectDirectory, 
                               appName + "/urls.py");
        
        StringBuilder content = new StringBuilder();
        content.append("from django.urls import path\n")
               .append("from . import views\n\n")
               .append("app_name = '").append(appName).append("'\n\n")
               .append("urlpatterns = [\n");
        
        if (app.has("urls")) {
            JSONArray urls = app.getJSONArray("urls");
            
            for (int i = 0; i < urls.length(); i++) {
                JSONObject url = urls.getJSONObject(i);
                String path = url.getString("path");
                String view = url.getString("view");
                String name = url.optString("name", "");
                
                content.append("    path('").append(path)
                       .append("', views.").append(view)
                       .append(".as_view(), name='").append(name).append("'),\n");
            }
        }
        
        content.append("]\n");
        
        writeFile(urlsFile, content.toString());
    }
    
    private void createUrlsFile() throws Exception {
        File urlsFile = new File(projectDirectory, 
                               projectName + "/urls.py");
        
        StringBuilder content = new StringBuilder();
        content.append("from django.contrib import admin\n")
               .append("from django.urls import path, include\n")
               .append("from django.conf import settings\n")
               .append("from django.conf.urls.static import static\n\n")
               .append("urlpatterns = [\n")
               .append("    path('admin/', admin.site.urls),\n");
        
        JSONArray apps = projectData.getJSONArray("apps");
        for (int i = 0; i < apps.length(); i++) {
            JSONObject app = apps.getJSONObject(i);
            String appName = app.getString("name");
            content.append("    path('").append(appName).append("/', include('")
                   .append(appName).append(".urls')),\n");
        }
        
        content.append("]\n\n");
        /*
        if (settings.getBoolean("DEBUG")) {
            content.append("if settings.DEBUG:\n")
                   .append("    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)\n")
                   .append("    urlpatterns += static(settings.STATIC_URL, document_root=settings.STATIC_ROOT)\n");
        }
        */
        writeFile(urlsFile, content.toString());
    }
    
    private void createAdminFile(JSONObject app) throws Exception {
        String appName = app.getString("name");
        File adminFile = new File(projectDirectory, 
                                appName + "/admin.py");
        
        StringBuilder content = new StringBuilder();
        content.append("from django.contrib import admin\n")
               .append("from .models import *\n\n");
        
        JSONArray models = app.getJSONArray("models");
        for (int i = 0; i < models.length(); i++) {
            JSONObject model = models.getJSONObject(i);
            String modelName = model.getString("name");
            content.append("admin.site.register(").append(modelName).append(")\n");
        }
        
        writeFile(adminFile, content.toString());
    }
    
    private void createAppsConfigFile(JSONObject app) throws Exception {
        String appName = app.getString("name");
        File appsFile = new File(projectDirectory, 
                               appName + "/apps.py");
        
        String content = "from django.apps import AppConfig\n\n" +
                        "class " + capitalize(appName) + "Config(AppConfig):\n" +
                        "    default_auto_field = 'django.db.models.BigAutoField'\n" +
                        "    name = '" + appName + "'\n";
        
        writeFile(appsFile, content);
    }
    
    private void createTemplates(JSONObject app) throws Exception {
        if (!app.has("templates")) return;
        
        String appName = app.getString("name");
        File templateDir = new File(projectDirectory, "templates/" + appName);
        templateDir.mkdirs();
        
        JSONArray templates = app.getJSONArray("templates");
        for (int i = 0; i < templates.length(); i++) {
            JSONObject template = templates.getJSONObject(i);
            String templateName = template.getString("name");
            String templateContent = template.getString("content");
            
            File templateFile = new File(templateDir, templateName);
            writeFile(templateFile, templateContent);
        }
    }
    
    private void createRequirementsFile() throws Exception {
        File reqFile = new File(projectDirectory, "requirements.txt");
        String content = "Django>=4.0\n" +
                        "Pillow\n" +
                        "django-crispy-forms\n" +
                        "django-debug-toolbar\n";
        
        writeFile(reqFile, content);
    }
    
    private void createManagePy() throws Exception {
        File manageFile = new File(projectDirectory, "manage.py");
        
        String content = "#!/usr/bin/env python\n" +
                        "\"\"\"Django's command-line utility for administrative tasks.\"\"\"\n" +
                        "import os\n" +
                        "import sys\n" +
                        "\n" +
                        "def main():\n" +
                        "    \"\"\"Run administrative tasks.\"\"\"\n" +
                        "    os.environ.setdefault('DJANGO_SETTINGS_MODULE', '" + projectName + ".settings')\n" +
                        "    try:\n" +
                        "        from django.core.management import execute_from_command_line\n" +
                        "    except ImportError as exc:\n" +
                        "        raise ImportError(\n" +
                        "            \"Couldn't import Django. Are you sure it's installed and \"\n" +
                        "            \"available on your PYTHONPATH environment variable? Did you \"\n" +
                        "            \"forget to activate a virtual environment?\"\n" +
                        "        ) from exc\n" +
                        "    execute_from_command_line(sys.argv)\n" +
                        "\n" +
                        "if __name__ == '__main__':\n" +
                        "    main()\n";
        
        writeFile(manageFile, content);
    }
    
    private File createZipArchive() throws Exception {
        File zipFile = new File(context.getExternalFilesDir(null), 
                               projectName + ".zip");
        
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
             FileInputStream fis = new FileInputStream(projectDirectory)) {
            
            zipDirectory(projectDirectory, projectDirectory.getName(), zos);
        }
        
        return zipFile;
    }
    
    private void zipDirectory(File folder, String parentFolder, ZipOutputStream zos) throws Exception {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                zipDirectory(file, parentFolder + "/" + file.getName(), zos);
                continue;
            }
            
            ZipEntry ze = new ZipEntry(parentFolder + "/" + file.getName());
            zos.putNextEntry(ze);
            
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
            }
            
            zos.closeEntry();
        }
    }
    
    private void writeFile(File file, String content) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
    
    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}