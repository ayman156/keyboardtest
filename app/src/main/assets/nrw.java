package com.djangobuilder.app;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.Intent;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.net.Uri;
import android.os.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    
    private static final int STORAGE_PERMISSION_CODE = 100;
    
    private EditText editProjectName, editAppName, editPreview;
    private CheckBox checkAdmin, checkRestApi, checkAuth;
    private Button btnAddModel, btnGenerate, btnSave;
    private ListView listModels;
    private Spinner spinnerFiles;
    private CardView cardModels;
    
    private ArrayList<DjangoModel> modelsList = new ArrayList<>();
    private ModelsAdapter modelsAdapter;
    
    private String projectName = "myproject";
    private String appName = "myapp";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupAdapters();
        setupListeners();
        requestStoragePermission();
        loadSavedData();
    }
    
    private void initViews() {
        editProjectName = findViewById(R.id.edit_project_name);
        editAppName = findViewById(R.id.edit_app_name);
        editPreview = findViewById(R.id.edit_preview);
        checkAdmin = findViewById(R.id.check_admin);
        checkRestApi = findViewById(R.id.check_rest_api);
        checkAuth = findViewById(R.id.check_auth);
        btnAddModel = findViewById(R.id.btn_add_model);
        btnGenerate = findViewById(R.id.btn_generate);
        btnSave = findViewById(R.id.btn_save);
        listModels = findViewById(R.id.list_models);
        spinnerFiles = findViewById(R.id.spinner_files);
        cardModels = findViewById(R.id.card_models);
    }
    
    private void setupAdapters() {
        modelsAdapter = new ModelsAdapter(this, modelsList);
        listModels.setAdapter(modelsAdapter);
        
        ArrayAdapter<String> filesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"models.py", "views.py", "urls.py", "admin.py", "settings.py", "serializers.py"});
        filesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiles.setAdapter(filesAdapter);
    }
    
    private void setupListeners() {
        btnAddModel.setOnClickListener(v -> openAddModelActivity());
        
        btnGenerate.setOnClickListener(v -> generateDjangoProject());
        
        btnSave.setOnClickListener(v -> saveProjectToFile());
        
        spinnerFiles.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                previewSelectedFile(spinnerFiles.getSelectedItem().toString());
            }
            
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        listModels.setOnItemClickListener((parent, view, position, id) -> {
            DjangoModel model = modelsList.get(position);
            editModel(model, position);
        });
        
        listModels.setOnItemLongClickListener((parent, view, position, id) -> {
            showDeleteDialog(position);
            return true;
        });
    }
    
    private void openAddModelActivity() {
        Intent intent = new Intent(MainActivity.this, ModelActivity.class);
        startActivityForResult(intent, 1);
    }
    
    private void editModel(DjangoModel model, int position) {
        Intent intent = new Intent(MainActivity.this, ModelActivity.class);
        intent.putExtra("model", new Gson().toJson(model));
        intent.putExtra("position", position);
        startActivityForResult(intent, 2);
    }
    
    private void showDeleteDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("حذف النموذج")
                .setMessage("هل أنت متأكد من حذف هذا النموذج؟")
                .setPositiveButton("نعم", (dialog, which) -> {
                    modelsList.remove(position);
                    modelsAdapter.notifyDataSetChanged();
                    saveModelsToPrefs();
                })
                .setNegativeButton("لا", null)
                .show();
    }
    
    private void previewSelectedFile(String fileName) {
        switch (fileName) {
            case "models.py":
                editPreview.setText(generateModelsPy());
                break;
            case "views.py":
                editPreview.setText(generateViewsPy());
                break;
            case "urls.py":
                editPreview.setText(generateUrlsPy());
                break;
            case "admin.py":
                editPreview.setText(generateAdminPy());
                break;
            case "settings.py":
                editPreview.setText(generateSettingsPy());
                break;
            case "serializers.py":
                editPreview.setText(generateSerializersPy());
                break;
        }
    }
    
    private String generateModelsPy() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Generated by Django Builder Pro\n");
        sb.append("from django.db import models\n");
        sb.append("from django.utils.translation import gettext_lazy as _\n\n");
        
        for (DjangoModel model : modelsList) {
            sb.append("\nclass ").append(model.getName()).append("(models.Model):\n");
            
            for (DjangoField field : model.getFields()) {
                sb.append("    ").append(field.getName()).append(" = models.");
                sb.append(field.getType()).append("(");
                
                List<String> options = new ArrayList<>();
                
                // Add field-specific options
                switch (field.getType()) {
                    case "CharField":
                        options.add("max_length=255");
                        break;
                    case "DecimalField":
                        options.add("max_digits=10");
                        options.add("decimal_places=2");
                        break;
                }
                
                // Add verbose name
                if (!field.getVerboseName().isEmpty()) {
                    options.add("verbose_name=_('" + field.getVerboseName() + "')");
                }
                
                // Add other options
                if (!field.isRequired()) {
                    options.add("null=True");
                    options.add("blank=True");
                }
                
                if (field.isUnique()) {
                    options.add("unique=True");
                }
                
                if (field.isIndexed()) {
                    options.add("db_index=True");
                }
                
                if (!field.getDefaultValue().isEmpty()) {
                    options.add("default='" + field.getDefaultValue() + "'");
                }
                
                if (!options.isEmpty()) {
                    sb.append(String.join(", ", options));
                }
                
                sb.append(")\n");
            }
            
            // Add Meta class
            sb.append("\n    class Meta:\n");
            sb.append("        verbose_name = _('").append(model.getVerboseName()).append("')\n");
            sb.append("        verbose_name_plural = _('").append(model.getVerboseName()).append("s')\n");
            sb.append("        ordering = ['-id']\n");
            
            // Add __str__ method
            sb.append("\n    def __str__(self):\n");
            if (!model.getFields().isEmpty()) {
                sb.append("        return str(self.").append(model.getFields().get(0).getName()).append(")\n");
            } else {
                sb.append("        return self.__class__.__name__\n");
            }
            
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    private String generateViewsPy() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Generated by Django Builder Pro\n");
        sb.append("from django.shortcuts import render, get_object_or_404, redirect\n");
        sb.append("from django.views.generic import ListView, DetailView, CreateView, UpdateView, DeleteView\n");
        sb.append("from django.urls import reverse_lazy\n");
        sb.append("from django.contrib.auth.mixins import LoginRequiredMixin\n");
        sb.append("from .models import *\n");
        sb.append("from .forms import *\n\n");
        
        for (DjangoModel model : modelsList) {
            String modelName = model.getName();
            String modelNameLower = modelName.toLowerCase();
            
            // List View
            sb.append("class ").append(modelName).append("ListView(LoginRequiredMixin, ListView):\n");
            sb.append("    model = ").append(modelName).append("\n");
            sb.append("    template_name = '").append(modelNameLower).append("_list.html'\n");
            sb.append("    context_object_name = '").append(modelNameLower).append("s'\n");
            sb.append("    paginate_by = 10\n\n");
            
            // Detail View
            sb.append("class ").append(modelName).append("DetailView(LoginRequiredMixin, DetailView):\n");
            sb.append("    model = ").append(modelName).append("\n");
            sb.append("    template_name = '").append(modelNameLower).append("_detail.html'\n\n");
            
            // Create View
            sb.append("class ").append(modelName).append("CreateView(LoginRequiredMixin, CreateView):\n");
            sb.append("    model = ").append(modelName).append("\n");
            sb.append("    form_class = ").append(modelName).append("Form\n");
            sb.append("    template_name = '").append(modelNameLower).append("_form.html'\n");
            sb.append("    success_url = reverse_lazy('").append(modelNameLower).append("-list')\n\n");
            
            // Update View
            sb.append("class ").append(modelName).append("UpdateView(LoginRequiredMixin, UpdateView):\n");
            sb.append("    model = ").append(modelName).append("\n");
            sb.append("    form_class = ").append(modelName).append("Form\n");
            sb.append("    template_name = '").append(modelNameLower).append("_form.html'\n");
            sb.append("    success_url = reverse_lazy('").append(modelNameLower).append("-list')\n\n");
            
            // Delete View
            sb.append("class ").append(modelName).append("DeleteView(LoginRequiredMixin, DeleteView):\n");
            sb.append("    model = ").append(modelName).append("\n");
            sb.append("    template_name = '").append(modelNameLower).append("_confirm_delete.html'\n");
            sb.append("    success_url = reverse_lazy('").append(modelNameLower).append("-list')\n\n");
        }
        
        return sb.toString();
    }
    
    private String generateUrlsPy() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Generated by Django Builder Pro\n");
        sb.append("from django.urls import path\n");
        sb.append("from . import views\n\n");
        sb.append("urlpatterns = [\n");
        
        for (DjangoModel model : modelsList) {
            String modelName = model.getName();
            String modelNameLower = modelName.toLowerCase();
            
            sb.append("    path('").append(modelNameLower).append("/', views.").append(modelName).append("ListView.as_view(), name='").append(modelNameLower).append("-list'),\n");
            sb.append("    path('").append(modelNameLower).append("/<int:pk>/', views.").append(modelName).append("DetailView.as_view(), name='").append(modelNameLower).append("-detail'),\n");
            sb.append("    path('").append(modelNameLower).append("/create/', views.").append(modelName).append("CreateView.as_view(), name='").append(modelNameLower).append("-create'),\n");
            sb.append("    path('").append(modelNameLower).append("/<int:pk>/update/', views.").append(modelName).append("UpdateView.as_view(), name='").append(modelNameLower).append("-update'),\n");
            sb.append("    path('").append(modelNameLower).append("/<int:pk>/delete/', views.").append(modelName).append("DeleteView.as_view(), name='").append(modelNameLower).append("-delete'),\n");
        }
        
        sb.append("]\n");
        return sb.toString();
    }
    
    private String generateAdminPy() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Generated by Django Builder Pro\n");
        sb.append("from django.contrib import admin\n");
        sb.append("from .models import *\n\n");
        
        for (DjangoModel model : modelsList) {
            String modelName = model.getName();
            
            sb.append("@admin.register(").append(modelName).append(")\n");
            sb.append("class ").append(modelName).append("Admin(admin.ModelAdmin):\n");
            sb.append("    list_display = [");
            
            List<String> displayFields = new ArrayList<>();
            for (DjangoField field : model.getFields()) {
                if (field.isIndexed() || field.isUnique()) {
                    displayFields.add("'" + field.getName() + "'");
                }
            }
            
            if (displayFields.isEmpty() && !model.getFields().isEmpty()) {
                displayFields.add("'" + model.getFields().get(0).getName() + "'");
            }
            
            displayFields.add("'id'");
            sb.append(String.join(", ", displayFields));
            sb.append("]\n");
            
            sb.append("    list_filter = ['created_at']\n");
            sb.append("    search_fields = [");
            
            List<String> searchFields = new ArrayList<>();
            for (DjangoField field : model.getFields()) {
                if (field.getType().equals("CharField") || field.getType().equals("TextField")) {
                    searchFields.add("'" + field.getName() + "'");
                }
            }
            sb.append(String.join(", ", searchFields));
            sb.append("]\n");
            sb.append("    list_per_page = 20\n\n");
        }
        
        return sb.toString();
    }
    
    private String generateSettingsPy() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Generated by Django Builder Pro\n");
        sb.append("import os\n");
        sb.append("from pathlib import Path\n\n");
        sb.append("BASE_DIR = Path(__file__).resolve().parent.parent\n\n");
        sb.append("SECRET_KEY = 'django-insecure-your-secret-key-here'\n\n");
        sb.append("DEBUG = True\n\n");
        sb.append("ALLOWED_HOSTS = []\n\n");
        sb.append("INSTALLED_APPS = [\n");
        sb.append("    'django.contrib.admin',\n");
        sb.append("    'django.contrib.auth',\n");
        sb.append("    'django.contrib.contenttypes',\n");
        sb.append("    'django.contrib.sessions',\n");
        sb.append("    'django.contrib.messages',\n");
        sb.append("    'django.contrib.staticfiles',\n");
        
        if (checkRestApi.isChecked()) {
            sb.append("    'rest_framework',\n");
            sb.append("    'corsheaders',\n");
        }
        
        sb.append("    '").append(appName).append("',\n");
        sb.append("]\n\n");
        
        sb.append("MIDDLEWARE = [\n");
        if (checkRestApi.isChecked()) {
            sb.append("    'corsheaders.middleware.CorsMiddleware',\n");
        }
        sb.append("    'django.middleware.security.SecurityMiddleware',\n");
        sb.append("    'django.contrib.sessions.middleware.SessionMiddleware',\n");
        sb.append("    'django.middleware.common.CommonMiddleware',\n");
        sb.append("    'django.middleware.csrf.CsrfViewMiddleware',\n");
        sb.append("    'django.contrib.auth.middleware.AuthenticationMiddleware',\n");
        sb.append("    'django.contrib.messages.middleware.MessageMiddleware',\n");
        sb.append("    'django.middleware.clickjacking.XFrameOptionsMiddleware',\n");
        sb.append("]\n\n");
        
        sb.append("ROOT_URLCONF = '").append(projectName).append(".urls'\n\n");
        
        sb.append("TEMPLATES = [\n");
        sb.append("    {\n");
        sb.append("        'BACKEND': 'django.template.backends.django.DjangoTemplates',\n");
        sb.append("        'DIRS': [os.path.join(BASE_DIR, 'templates')],\n");
        sb.append("        'APP_DIRS': True,\n");
        sb.append("        'OPTIONS': {\n");
        sb.append("            'context_processors': [\n");
        sb.append("                'django.template.context_processors.debug',\n");
        sb.append("                'django.template.context_processors.request',\n");
        sb.append("                'django.contrib.auth.context_processors.auth',\n");
        sb.append("                'django.contrib.messages.context_processors.messages',\n");
        sb.append("            ],\n");
        sb.append("        },\n");
        sb.append("    },\n");
        sb.append("]\n\n");
        
        sb.append("WSGI_APPLICATION = '").append(projectName).append(".wsgi.application'\n\n");
        
        sb.append("DATABASES = {\n");
        sb.append("    'default': {\n");
        sb.append("        'ENGINE': 'django.db.backends.sqlite3',\n");
        sb.append("        'NAME': BASE_DIR / 'db.sqlite3',\n");
        sb.append("    }\n");
        sb.append("}\n\n");
        
        sb.append("AUTH_PASSWORD_VALIDATORS = [\n");
        sb.append("    {\n");
        sb.append("        'NAME': 'django.contrib.auth.password_validation.UserAttributeSimilarityValidator',\n");
        sb.append("    },\n");
        sb.append("    {\n");
        sb.append("        'NAME': 'django.contrib.auth.password_validation.MinimumLengthValidator',\n");
        sb.append("    },\n");
        sb.append("    {\n");
        sb.append("        'NAME': 'django.contrib.auth.password_validation.CommonPasswordValidator',\n");
        sb.append("    },\n");
        sb.append("    {\n");
        sb.append("        'NAME': 'django.contrib.auth.password_validation.NumericPasswordValidator',\n");
        sb.append("    },\n");
        sb.append("]\n\n");
        
        sb.append("LANGUAGE_CODE = 'ar'\n\n");
        sb.append("TIME_ZONE = 'Asia/Riyadh'\n\n");
        sb.append("USE_I18N = True\n\n");
        sb.append("USE_TZ = True\n\n");
        
        sb.append("STATIC_URL = 'static/'\n");
        sb.append("STATICFILES_DIRS = [os.path.join(BASE_DIR, 'static')]\n\n");
        
        sb.append("MEDIA_URL = 'media/'\n");
        sb.append("MEDIA_ROOT = os.path.join(BASE_DIR, 'media')\n\n");
        
        sb.append("DEFAULT_AUTO_FIELD = 'django.db.models.BigAutoField'\n\n");
        
        if (checkRestApi.isChecked()) {
            sb.append("# REST Framework Settings\n");
            sb.append("REST_FRAMEWORK = {\n");
            sb.append("    'DEFAULT_PERMISSION_CLASSES': [\n");
            sb.append("        'rest_framework.permissions.IsAuthenticated',\n");
            sb.append("    ],\n");
            sb.append("    'DEFAULT_AUTHENTICATION_CLASSES': [\n");
            sb.append("        'rest_framework.authentication.SessionAuthentication',\n");
            sb.append("        'rest_framework.authentication.BasicAuthentication',\n");
            sb.append("    ]\n");
            sb.append("}\n\n");
            
            sb.append("CORS_ALLOW_ALL_ORIGINS = True\n");
        }
        
        return sb.toString();
    }
    
    private String generateSerializersPy() {
        if (!checkRestApi.isChecked()) {
            return "# REST API is not enabled";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("# Generated by Django Builder Pro\n");
        sb.append("from rest_framework import serializers\n");
        sb.append("from .models import *\n\n");
        
        for (DjangoModel model : modelsList) {
            sb.append("class ").append(model.getName()).append("Serializer(serializers.ModelSerializer):\n");
            sb.append("    class Meta:\n");
            sb.append("        model = ").append(model.getName()).append("\n");
            sb.append("        fields = '__all__'\n");
            sb.append("        read_only_fields = ('id', 'created_at', 'updated_at')\n\n");
        }
        
        return sb.toString();
    }
    
    private void generateDjangoProject() {
        projectName = editProjectName.getText().toString().trim();
        appName = editAppName.getText().toString().trim();
        
        if (projectName.isEmpty() || appName.isEmpty()) {
            Toast.makeText(this, "الرجاء إدخال اسم المشروع والتطبيق", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (modelsList.isEmpty()) {
            Toast.makeText(this, "الرجاء إضافة نماذج أولاً", Toast.LENGTH_SHORT).show();
            return;
        }
        
        createProjectStructure();
        Toast.makeText(this, "تم إنشاء المشروع بنجاح!", Toast.LENGTH_LONG).show();
    }
   // في دالة createProjectStructure()، استبدل جميع السطور التي تحتوي على new File بثلاثة معاملات:

private void createProjectStructure() {
    File baseDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS), projectName);
    
    // إنشاء المجلدات
    createDirectory(baseDir);
    
    // إنشاء مجلد app داخل المشروع
    File appDir = new File(baseDir, appName);
    createDirectory(appDir);
    
    // إنشاء مجلد project داخل المشروع (لإعدادات Django)
    File projectDir = new File(baseDir, projectName);
    createDirectory(projectDir);
    
    createDirectory(new File(baseDir, "templates"));
    createDirectory(new File(baseDir, "static"));
    createDirectory(new File(baseDir, "media"));
    
    // إنشاء ملف __init__.py للمشروع الرئيسي
    createFile(new File(baseDir, "__init__.py"), "");
    
    // إنشاء ملف __init__.py للتطبيق
    createFile(new File(appDir, "__init__.py"), "");
    
    // إنشاء ملفات المشروع الرئيسية
    createFile(new File(baseDir, "manage.py"), generateManagePy());
    createFile(new File(baseDir, "requirements.txt"), generateRequirements());
    
    // إنشاء ملفات إعدادات Django داخل مجلد المشروع
    createFile(new File(projectDir, "__init__.py"), "");
    createFile(new File(projectDir, "settings.py"), generateSettingsPy());
    createFile(new File(projectDir, "urls.py"), generateProjectUrlsPy());
    createFile(new File(projectDir, "wsgi.py"), generateWsgiPy());
    createFile(new File(projectDir, "asgi.py"), generateAsgiPy());
    
    // إنشاء ملفات التطبيق داخل مجلد التطبيق
    createFile(new File(appDir, "models.py"), generateModelsPy());
    createFile(new File(appDir, "views.py"), generateViewsPy());
    createFile(new File(appDir, "urls.py"), generateUrlsPy());
    createFile(new File(appDir, "admin.py"), generateAdminPy());
    createFile(new File(appDir, "apps.py"), generateAppsPy());
    
    if (checkRestApi.isChecked()) {
        createFile(new File(appDir, "serializers.py"), generateSerializersPy());
    }
    
    // إنشاء forms.py
    createFile(new File(appDir, "forms.py"), generateFormsPy());
    
    // إنشاء القوالب
    createTemplates(baseDir);
}
    
    private void createDirectory(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    private void createFile(File file, String content) {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private String generateManagePy() {
        return "#!/usr/bin/env python\n" +
                "\"\"\"Django's command-line utility for administrative tasks.\"\"\"\n" +
                "import os\n" +
                "import sys\n" +
                "\n" +
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
                "\n" +
                "if __name__ == '__main__':\n" +
                "    main()\n";
    }
    
    private String generateProjectUrlsPy() {
        return "from django.contrib import admin\n" +
                "from django.urls import path, include\n" +
                "from django.conf import settings\n" +
                "from django.conf.urls.static import static\n\n" +
                "urlpatterns = [\n" +
                "    path('admin/', admin.site.urls),\n" +
                "    path('', include('" + appName + ".urls')),\n" +
                "]\n\n" +
                "if settings.DEBUG:\n" +
                "    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)\n";
    }
    
    private String generateWsgiPy() {
        return "import os\n\n" +
                "from django.core.wsgi import get_wsgi_application\n\n" +
                "os.environ.setdefault('DJANGO_SETTINGS_MODULE', '" + projectName + ".settings')\n\n" +
                "application = get_wsgi_application()\n";
    }
    
    private String generateAsgiPy() {
        return "import os\n\n" +
                "from django.core.asgi import get_asgi_application\n\n" +
                "os.environ.setdefault('DJANGO_SETTINGS_MODULE', '" + projectName + ".settings')\n\n" +
                "application = get_asgi_application()\n";
    }
    
    private String generateAppsPy() {
        return "from django.apps import AppConfig\n\n\n" +
                "class " + capitalizeFirst(appName) + "Config(AppConfig):\n" +
                "    default_auto_field = 'django.db.models.BigAutoField'\n" +
                "    name = '" + appName + "'\n" +
                "    verbose_name = '" + appName + "'\n";
    }
    
    private String generateFormsPy() {
        StringBuilder sb = new StringBuilder();
        sb.append("from django import forms\n");
        sb.append("from .models import *\n\n");
        
        for (DjangoModel model : modelsList) {
            sb.append("class ").append(model.getName()).append("Form(forms.ModelForm):\n");
            sb.append("    class Meta:\n");
            sb.append("        model = ").append(model.getName()).append("\n");
            sb.append("        fields = '__all__'\n\n");
            
            sb.append("    def __init__(self, *args, **kwargs):\n");
            sb.append("        super().__init__(*args, **kwargs)\n");
            sb.append("        for field in self.fields:\n");
            sb.append("            self.fields[field].widget.attrs.update({\n");
            sb.append("                'class': 'form-control'\n");
            sb.append("            })\n\n");
        }
        
        return sb.toString();
    }
    
    private String generateRequirements() {
        StringBuilder sb = new StringBuilder();
        sb.append("Django==4.2.0\n");
        sb.append("pillow==9.5.0\n");
        
        if (checkRestApi.isChecked()) {
            sb.append("djangorestframework==3.14.0\n");
            sb.append("django-cors-headers==3.14.0\n");
        }
        
        return sb.toString();
    }
    
    private void createTemplates(File baseDir) {
    for (DjangoModel model : modelsList) {
        String modelName = model.getName();
        String modelNameLower = modelName.toLowerCase();
        
        // إنشاء مجلد القوالب للنموذج
        File modelTemplateDir = new File(new File(baseDir, "templates"), modelNameLower);
        createDirectory(modelTemplateDir);
        
        // إنشاء قوالب HTML
        createFile(new File(modelTemplateDir, modelNameLower + "_list.html"),
                generateListTemplate(model));
        createFile(new File(modelTemplateDir, modelNameLower + "_form.html"),
                generateFormTemplate(model));
        createFile(new File(modelTemplateDir, modelNameLower + "_detail.html"),
                generateDetailTemplate(model));
        createFile(new File(modelTemplateDir, modelNameLower + "_confirm_delete.html"),
                generateDeleteTemplate(model));
    }
}
    
    private String generateListTemplate(DjangoModel model) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"ar\" dir=\"rtl\">\n");
        sb.append("<head>\n");
        sb.append("    <meta charset=\"UTF-8\">\n");
        sb.append("    <title>").append(model.getVerboseName()).append("</title>\n");
        sb.append("    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("    <div class=\"container mt-4\">\n");
        sb.append("        <h1 class=\"mb-4\">").append(model.getVerboseName()).append("</h1>\n");
        sb.append("        <a href=\"{% url '").append(model.getName().toLowerCase()).append("-create' %}\" class=\"btn btn-primary mb-3\">إضافة جديد</a>\n");
        sb.append("        <table class=\"table table-striped\">\n");
        sb.append("            <thead>\n");
        sb.append("                <tr>\n");
        
        for (DjangoField field : model.getFields()) {
            if (!field.getVerboseName().isEmpty()) {
                sb.append("                    <th>").append(field.getVerboseName()).append("</th>\n");
            } else {
                sb.append("                    <th>").append(field.getName()).append("</th>\n");
            }
        }
        
        sb.append("                    <th>الإجراءات</th>\n");
        sb.append("                </tr>\n");
        sb.append("            </thead>\n");
        sb.append("            <tbody>\n");
        sb.append("                {% for item in object_list %}\n");
        sb.append("                <tr>\n");
        
        for (DjangoField field : model.getFields()) {
            sb.append("                    <td>{{ item.").append(field.getName()).append(" }}</td>\n");
        }
        
        sb.append("                    <td>\n");
        sb.append("                        <a href=\"{% url '").append(model.getName().toLowerCase()).append("-detail' item.pk %}\" class=\"btn btn-info btn-sm\">عرض</a>\n");
        sb.append("                        <a href=\"{% url '").append(model.getName().toLowerCase()).append("-update' item.pk %}\" class=\"btn btn-warning btn-sm\">تعديل</a>\n");
        sb.append("                        <a href=\"{% url '").append(model.getName().toLowerCase()).append("-delete' item.pk %}\" class=\"btn btn-danger btn-sm\">حذف</a>\n");
        sb.append("                    </td>\n");
        sb.append("                </tr>\n");
        sb.append("                {% endfor %}\n");
        sb.append("            </tbody>\n");
        sb.append("        </table>\n");
        sb.append("    </div>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");
        
        return sb.toString();
    }
    
    private String generateFormTemplate(DjangoModel model) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"ar\" dir=\"rtl\">\n");
        sb.append("<head>\n");
        sb.append("    <meta charset=\"UTF-8\">\n");
        sb.append("    <title>إضافة/تعديل ").append(model.getVerboseName()).append("</title>\n");
        sb.append("    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("    <div class=\"container mt-4\">\n");
        sb.append("        <h1 class=\"mb-4\">{% if object %}تعديل{% else %}إضافة{% endif %} ").append(model.getVerboseName()).append("</h1>\n");
        sb.append("        <form method=\"post\">\n");
        sb.append("            {% csrf_token %}\n");
        sb.append("            {{ form.as_p }}\n");
        sb.append("            <button type=\"submit\" class=\"btn btn-success\">حفظ</button>\n");
        sb.append("            <a href=\"{% url '").append(model.getName().toLowerCase()).append("-list' %}\" class=\"btn btn-secondary\">إلغاء</a>\n");
        sb.append("        </form>\n");
        sb.append("    </div>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");
        
        return sb.toString();
    }
    
    private String generateDetailTemplate(DjangoModel model) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"ar\" dir=\"rtl\">\n");
        sb.append("<head>\n");
        sb.append("    <meta charset=\"UTF-8\">\n");
        sb.append("    <title>تفاصيل ").append(model.getVerboseName()).append("</title>\n");
        sb.append("    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("    <div class=\"container mt-4\">\n");
        sb.append("        <h1 class=\"mb-4\">تفاصيل ").append(model.getVerboseName()).append("</h1>\n");
        sb.append("        <div class=\"card\">\n");
        sb.append("            <div class=\"card-body\">\n");
        
        for (DjangoField field : model.getFields()) {
            String label = field.getVerboseName().isEmpty() ? field.getName() : field.getVerboseName();
            sb.append("                <p><strong>").append(label).append(":</strong> {{ object.").append(field.getName()).append(" }}</p>\n");
        }
        
        sb.append("            </div>\n");
        sb.append("        </div>\n");
        sb.append("        <div class=\"mt-3\">\n");
        sb.append("            <a href=\"{% url '").append(model.getName().toLowerCase()).append("-list' %}\" class=\"btn btn-primary\">العودة للقائمة</a>\n");
        sb.append("            <a href=\"{% url '").append(model.getName().toLowerCase()).append("-update' object.pk %}\" class=\"btn btn-warning\">تعديل</a>\n");
        sb.append("        </div>\n");
        sb.append("    </div>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");
        
        return sb.toString();
    }
    
    private String generateDeleteTemplate(DjangoModel model) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"ar\" dir=\"rtl\">\n");
        sb.append("<head>\n");
        sb.append("    <meta charset=\"UTF-8\">\n");
        sb.append("    <title>حذف ").append(model.getVerboseName()).append("</title>\n");
        sb.append("    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("    <div class=\"container mt-4\">\n");
        sb.append("        <h1 class=\"mb-4\">حذف ").append(model.getVerboseName()).append("</h1>\n");
        sb.append("        <div class=\"alert alert-danger\">\n");
        sb.append("            <p>هل أنت متأكد من حذف هذا العنصر؟</p>\n");
        sb.append("            <p><strong>{{ object }}</strong></p>\n");
        sb.append("        </div>\n");
        sb.append("        <form method=\"post\">\n");
        sb.append("            {% csrf_token %}\n");
        sb.append("            <button type=\"submit\" class=\"btn btn-danger\">نعم، حذف</button>\n");
        sb.append("            <a href=\"{% url '").append(model.getName().toLowerCase()).append("-list' %}\" class=\"btn btn-secondary\">إلغاء</a>\n");
        sb.append("        </form>\n");
        sb.append("    </div>\n");
        sb.append("</body>\n");
        sb.append("</html>\n");
        
        return sb.toString();
    }
    
    private void saveProjectToFile() {
        projectName = editProjectName.getText().toString().trim();
        appName = editAppName.getText().toString().trim();
        
        if (projectName.isEmpty() || appName.isEmpty()) {
            Toast.makeText(this, "الرجاء إدخال اسم المشروع والتطبيق", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (modelsList.isEmpty()) {
            Toast.makeText(this, "الرجاء إضافة نماذج أولاً", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Save project configuration
        saveModelsToPrefs();
        
        // Create ZIP file
        createProjectZip();
        
        Toast.makeText(this, "تم حفظ المشروع بنجاح في مجلد التحميلات", Toast.LENGTH_LONG).show();
    }
    
    private void createProjectZip() {
        // Implementation for creating ZIP file
        // This requires additional libraries or manual ZIP creation
        // For simplicity, we'll just save the files normally
        generateDjangoProject();
    }
    
    private void saveModelsToPrefs() {
        String modelsJson = new Gson().toJson(modelsList);
        getSharedPreferences("DjangoBuilder", MODE_PRIVATE)
                .edit()
                .putString("models", modelsJson)
                .putString("project_name", projectName)
                .putString("app_name", appName)
                .putBoolean("admin", checkAdmin.isChecked())
                .putBoolean("api", checkRestApi.isChecked())
                .putBoolean("auth", checkAuth.isChecked())
                .apply();
    }
    
    private void loadSavedData() {
        String modelsJson = getSharedPreferences("DjangoBuilder", MODE_PRIVATE)
                .getString("models", "");
        
        if (!modelsJson.isEmpty()) {
            Type type = new TypeToken<ArrayList<DjangoModel>>(){}.getType();
            modelsList = new Gson().fromJson(modelsJson, type);
            modelsAdapter.notifyDataSetChanged();
        }
        
        projectName = getSharedPreferences("DjangoBuilder", MODE_PRIVATE)
                .getString("project_name", "myproject");
        appName = getSharedPreferences("DjangoBuilder", MODE_PRIVATE)
                .getString("app_name", "myapp");
        
        editProjectName.setText(projectName);
        editAppName.setText(appName);
        
        checkAdmin.setChecked(getSharedPreferences("DjangoBuilder", MODE_PRIVATE)
                .getBoolean("admin", true));
        checkRestApi.setChecked(getSharedPreferences("DjangoBuilder", MODE_PRIVATE)
                .getBoolean("api", false));
        checkAuth.setChecked(getSharedPreferences("DjangoBuilder", MODE_PRIVATE)
                .getBoolean("auth", true));
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == 1 || requestCode == 2) {
                String modelJson = data.getStringExtra("model");
                DjangoModel model = new Gson().fromJson(modelJson, DjangoModel.class);
                
                if (requestCode == 2) {
                    int position = data.getIntExtra("position", -1);
                    if (position != -1) {
                        modelsList.set(position, model);
                    }
                } else {
                    modelsList.add(model);
                }
                
                modelsAdapter.notifyDataSetChanged();
                saveModelsToPrefs();
                
                // Auto-preview models.py
                spinnerFiles.setSelection(0);
                previewSelectedFile("models.py");
            }
        }
    }
    
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "تم منح صلاحيات التخزين", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "الصلاحيات مطلوبة لحفظ المشروع", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}