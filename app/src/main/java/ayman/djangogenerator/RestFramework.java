package ayman.djangogenerator;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.*;
import java.util.*;

public class RestFramework {
    private Context context;
    private String projectName;
    private JSONObject projectData;
    private File projectDirectory;

    public RestFramework(Context context, String jsonString) throws JSONException {
        this.context = context;
        this.projectData = new JSONObject(jsonString);
        this.projectName = projectData.getString("project_name");
    }

    public void createRESTFramework() throws Exception {
        projectDirectory = new File(FileUtil.getExternalStorageDir(), "django_projects/" + projectName);
        if (!projectDirectory.exists()) {
            projectDirectory.mkdirs();
        }

        // 1. تحديث الإعدادات (settings.py)
        updateSettingsForREST();

        // 2. إنشاء ملف api_urls.py الرئيسي للمشروع
        createMainAPIUrlsFile();

        // 3. إنشاء ملفات الـ API لكل تطبيق (App)
        JSONArray apps = projectData.getJSONArray("apps");
        for (int i = 0; i < apps.length(); i++) {
            JSONObject app = apps.getJSONObject(i);
            String appName = app.getString("name"); // استخدام name حسب الـ JSON الخاص بك

            // إنشاء مجلد api داخل كل تطبيق (مثل intelligence/api/)
            File apiDir = new File(projectDirectory, appName + "/api");
            if (!apiDir.exists()) apiDir.mkdirs();
            new File(apiDir, "__init__.py").createNewFile();

            createAppAPISerializers(app);
            createAppAPIViewsets(app);
            createAppAPIFilters(app);
            createAppAPIUrls(app);
        }
    }
/*
    private void updateSettingsForREST() throws Exception {
        File settingsFile = new File(projectDirectory, projectName + "/settings.py");
        if (!settingsFile.exists()) return;

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(settingsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
                if (line.contains("INSTALLED_APPS = [")) {
                    sb.append("    'rest_framework',\n")
                      .append("    'django_filters',\n")
                      .append("    'drf_yasg',\n")
                      .append("    'rest_framework_simplejwt',\n");
                }
            }
        }


   sb.append("\nREST_FRAMEWORK = {\n")
  .append("    'DEFAULT_PERMISSION_CLASSES': ['rest_framework.permissions.IsAuthenticated'],\n")
  .append("    'DEFAULT_AUTHENTICATION_CLASSES': [\n")
  .append("        'rest_framework_simplejwt.authentication.JWTAuthentication',\n")
  .append("    ],\n")
  .append("    'DEFAULT_FILTER_BACKENDS': (\n")
  .append("        'django_filters.rest_framework.DjangoFilterBackend',\n")
  .append("        'rest_framework.filters.SearchFilter',\n")
  .append("        'rest_framework.filters.OrderingFilter',\n")
  .append("    ),\n")
  .append("    'DEFAULT_SCHEMA_CLASS': 'rest_framework.schemas.openapi.AutoSchema',\n")
  .append("}\n")
  .append("\nSWAGGER_SETTINGS = {\n")
  .append("    'SECURITY_DEFINITIONS': {\n")
  .append("        'Bearer': {\n")
  .append("            'type': 'apiKey',\n")
  .append("            'name': 'Authorization',\n")
  .append("            'in': 'header'\n")
  .append("        }\n")
  .append("    },\n")
  .append("    'USE_SESSION_AUTH': False,\n")
  .append("    'JSON_EDITOR': True,\n")
  .append("}\n")
  .append("\nimport os\n")
  .append("logs_dir = BASE_DIR / 'logs'\n")
  .append("if not os.path.exists(logs_dir):\n")
  .append("    os.makedirs(logs_dir)\n")
  .append("\nLOGGING = {\n")
.append("    'version': 1,\n")
.append("    'disable_existing_loggers': False,\n")
.append("    'formatters': {\n")
.append("        'verbose': {\n")
.append("            'format': '{levelname} {asctime} {module} {process:d} {thread:d} {message}',\n")
.append("            'style': '{',\n")
.append("        },\n")
.append("    },\n")
.append("    'handlers': {\n")
.append("        'file': {\n")
.append("            'level': 'ERROR',\n")
.append("            'class': 'logging.FileHandler',\n")
.append("            'filename': logs_dir / 'error.log',\n")
.append("            'formatter': 'verbose',\n")
.append("        },\n")
.append("        'console': {\n")
.append("            'class': 'logging.StreamHandler',\n")
.append("        },\n")
.append("    },\n")
.append("    'loggers': {\n")
.append("        'django': {\n")
.append("            'handlers': ['file', 'console'],\n")
.append("            'level': 'ERROR',\n")
.append("            'propagate': True,\n")
.append("        },\n")
.append("    },\n")
.append("}\n");




        writeFile(settingsFile, sb.toString());
    }*/
    private void updateSettingsForREST() throws Exception {
    File settingsFile = new File(projectDirectory, projectName + "/settings.py");
    if (!settingsFile.exists()) return;

    StringBuilder sb = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new FileReader(settingsFile))) {
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
            if (line.contains("INSTALLED_APPS = [")) {
                sb.append("    'rest_framework',\n")
                  .append("    'django_filters',\n")
                  .append("    'drf_yasg',\n")
                  .append("    'rest_framework_simplejwt',\n");
            }
        }
    }

    sb.append("\n# ==========================================\n")
      .append("# Django REST Framework Settings\n")
      .append("# ==========================================\n")
      .append("REST_FRAMEWORK = {\n")
      .append("    'DEFAULT_PERMISSION_CLASSES': ['rest_framework.permissions.IsAuthenticated'],\n")
      .append("    'DEFAULT_AUTHENTICATION_CLASSES': [\n")
      .append("        'rest_framework_simplejwt.authentication.JWTAuthentication',\n")
      .append("    ],\n")
      .append("    'DEFAULT_FILTER_BACKENDS': (\n")
      .append("        'django_filters.rest_framework.DjangoFilterBackend',\n")
      .append("        'rest_framework.filters.SearchFilter',\n")
      .append("        'rest_framework.filters.OrderingFilter',\n")
      .append("    ),\n")
      .append("    'DEFAULT_SCHEMA_CLASS': 'rest_framework.schemas.openapi.AutoSchema',\n")
      .append("}\n")

      .append("\n# ==========================================\n")
      .append("# Swagger Settings\n")
      .append("# ==========================================\n")
      .append("SWAGGER_SETTINGS = {\n")
      .append("    'SECURITY_DEFINITIONS': {\n")
      .append("        'Bearer': {\n")
      .append("            'type': 'apiKey',\n")
      .append("            'name': 'Authorization',\n")
      .append("            'in': 'header'\n")
      .append("        }\n")
      .append("    },\n")
      .append("    'USE_SESSION_AUTH': False,\n")
      .append("    'JSON_EDITOR': True,\n")
      .append("}\n")

      .append("\n# ==========================================\n")
      .append("# Simple JWT Settings\n")
      .append("# ==========================================\n")
      .append("from datetime import timedelta\n\n")
      .append("SIMPLE_JWT = {\n")
      .append("    'ACCESS_TOKEN_LIFETIME': timedelta(minutes=60),\n")
      .append("    'REFRESH_TOKEN_LIFETIME': timedelta(days=1),\n")
      .append("    'ROTATE_REFRESH_TOKENS': False,\n")
      .append("    'BLACKLIST_AFTER_ROTATION': False,\n")
      .append("    'AUTH_HEADER_TYPES': ('Bearer',),\n")
      .append("    'AUTH_TOKEN_CLASSES': ('rest_framework_simplejwt.tokens.AccessToken',),\n")
      .append("}\n")

      .append("\n# ==========================================\n")
      .append("# Logging & OS Settings\n")
      .append("# ==========================================\n")
      .append("import os\n")
      .append("logs_dir = BASE_DIR / 'logs'\n")
      .append("if not os.path.exists(logs_dir):\n")
      .append("    os.makedirs(logs_dir)\n")

      .append("\nLOGGING = {\n")
      .append("    'version': 1,\n")
      .append("    'disable_existing_loggers': False,\n")
      .append("    'formatters': {\n")
      .append("        'verbose': {\n")
      .append("            'format': '{levelname} {asctime} {module} {process:d} {thread:d} {message}',\n")
      .append("            'style': '{',\n")
      .append("        },\n")
      .append("    },\n")
      .append("    'handlers': {\n")
      .append("        'file': {\n")
      .append("            'level': 'ERROR',\n")
      .append("            'class': 'logging.FileHandler',\n")
      .append("            'filename': logs_dir / 'error.log',\n")
      .append("            'formatter': 'verbose',\n")
      .append("        },\n")
      .append("        'console': {\n")
      .append("            'class': 'logging.StreamHandler',\n")
      .append("        },\n")
      .append("    },\n")
      .append("    'loggers': {\n")
      .append("        'django': {\n")
      .append("            'handlers': ['file', 'console'],\n")
      .append("            'level': 'ERROR',\n")
      .append("            'propagate': True,\n")
      .append("        },\n")
      .append("    },\n")
      .append("}\n");

    writeFile(settingsFile, sb.toString());
}


    private void createMainAPIUrlsFile() throws Exception {
        File file = new File(projectDirectory, projectName + "/api_urls.py");
        StringBuilder sb = new StringBuilder();
        sb.append("from django.urls import path, include\n")
          .append("from rest_framework import permissions\n")
          .append("from drf_yasg.views import get_schema_view\n")
          .append("from drf_yasg import openapi\n")
          .append("from rest_framework_simplejwt.views import TokenObtainPairView, TokenRefreshView\n\n");

        sb.append("schema_view = get_schema_view(\n")
          .append("   openapi.Info(title='").append(projectName).append(" API', default_version='v1'),\n")
          .append("   public=True,\n")
          .append("   permission_classes=(permissions.AllowAny,),\n")
          .append("   authentication_classes=[], ")
          .append(")\n\n");

        sb.append("urlpatterns = [\n")
          .append("    path('jwt/token/', TokenObtainPairView.as_view(), name='token_obtain_pair'),\n")
          .append("    path('jwt/token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),\n")
          .append("    path('', schema_view.with_ui('swagger', cache_timeout=0), name='schema-swagger-ui'),\n");

        JSONArray apps = projectData.getJSONArray("apps");
        for (int i = 0; i < apps.length(); i++) {
            String appName = apps.getJSONObject(i).getString("name");
            // هنا الحل: نستخدم include للمسارات مباشرة لتجنب خطأ ImportError
            sb.append("    path('").append(appName).append("/', include('").append(appName).append(".api.urls')),\n");
        }
        sb.append("]\n");
        writeFile(file, sb.toString());
    }

    private void createAppAPISerializers(JSONObject app) throws Exception {
        String appName = app.getString("name");
        File file = new File(projectDirectory, appName + "/api/serializers.py");
        StringBuilder sb = new StringBuilder("from rest_framework import serializers\nfrom ..models import *\n\n");

        JSONArray models = app.getJSONArray("models");
        for (int i = 0; i < models.length(); i++) {
            String modelName = models.getJSONObject(i).getString("name");
            sb.append("class ").append(modelName).append("Serializer(serializers.ModelSerializer):\n")
              .append("    class Meta:\n")
              .append("        model = ").append(modelName).append("\n")
              .append("        fields = '__all__'\n\n");
        }
        writeFile(file, sb.toString());
    }

    private void createAppAPIViewsets(JSONObject app) throws Exception {
        String appName = app.getString("name");
        File file = new File(projectDirectory, appName + "/api/viewsets.py");
        StringBuilder sb = new StringBuilder("from rest_framework import viewsets, permissions\nfrom ..models import *\nfrom .serializers import *\nfrom .filters import *\n\n");

        JSONArray models = app.getJSONArray("models");
        for (int i = 0; i < models.length(); i++) {
            String modelName = models.getJSONObject(i).getString("name");
            sb.append("class ").append(modelName).append("ViewSet(viewsets.ModelViewSet):\n")
              .append("    queryset = ").append(modelName).append(".objects.all()\n")
              .append("    serializer_class = ").append(modelName).append("Serializer\n")
              .append("    filterset_class = ").append(modelName).append("Filter\n")
              .append("    permission_classes = [permissions.IsAuthenticated]\n\n");
        }
        writeFile(file, sb.toString());
    }
    /*

    private void createAppAPIFilters(JSONObject app) throws Exception {
        String appName = app.getString("name");
        File file = new File(projectDirectory, appName + "/api/filters.py");
        StringBuilder sb = new StringBuilder("from django_filters import rest_framework as filters\nfrom ..models import *\n\n");

        JSONArray models = app.getJSONArray("models");
        for (int i = 0; i < models.length(); i++) {
            JSONObject model = models.getJSONObject(i);
            String modelName = model.getString("name");
            sb.append("class ").append(modelName).append("Filter(filters.FilterSet):\n")
              .append("    class Meta:\n")
              .append("        model = ").append(modelName).append("\n")
              .append("        exclude = [");

            // استبعاد حقول الصور والملفات لمنع AssertionError
            JSONArray fields = model.getJSONArray("fields");
            List<String> excludeList = new ArrayList<>();
            for (int j = 0; j < fields.length(); j++) {
                JSONObject f = fields.getJSONObject(j);
                String type = f.getString("type");
                if (type.equals("ImageField") || type.equals("FileField")) {
                    excludeList.add("'" + f.getString("name") + "'");
                }
            }
            sb.append(String.join(", ", excludeList)).append("]\n\n");
        }
        writeFile(file, sb.toString());
    }*/
    private void createAppAPIFilters(JSONObject app) throws Exception {
    String appName = app.getString("name");
    File file = new File(projectDirectory, appName + "/api/filters.py");
    
    StringBuilder sb = new StringBuilder();
    sb.append("from django.db import models\n");
    sb.append("from django_filters import rest_framework as filters\n");
    sb.append("from ..models import *\n\n");

    JSONArray models = app.getJSONArray("models");
    for (int i = 0; i < models.length(); i++) {
        JSONObject model = models.getJSONObject(i);
        String modelName = model.getString("name");
        JSONArray fields = model.getJSONArray("fields");

        sb.append("class ").append(modelName).append("Filter(filters.FilterSet):\n")
          .append("    class Meta:\n")
          .append("        model = ").append(modelName).append("\n");

        List<String> excludeList = new ArrayList<>();
        boolean hasJsonField = false;

        for (int j = 0; j < fields.length(); j++) {
            JSONObject f = fields.getJSONObject(j);
            String type = f.getString("type");
            String name = f.getString("name");

            // 1. تحديد الحقول المستبعدة (الصور والملفات)
            if (type.equals("ImageField") || type.equals("FileField")) {
                excludeList.add("'" + name + "'");
            }
            
            // 2. التحقق من وجود JSONField لتفعيل الـ Override
            if (type.equals("JSONField")) {
                hasJsonField = true;
            }
        }

        // كتابة الـ exclude بطريقة نظيفة
        sb.append("        exclude = [").append(String.join(", ", excludeList)).append("]\n");

        // 3. إضافة الـ overrides فقط عند الحاجة ليكون الكود نظيفاً (Clean Code)
        if (hasJsonField) {
            sb.append("        filter_overrides = {\n")
              .append("            models.JSONField: {\n")
              .append("                'filter_class': filters.CharFilter,\n")
              .append("                'extra': lambda f: {\n")
              .append("                    'lookup_expr': 'icontains',\n")
              .append("                },\n")
              .append("            },\n")
              .append("        }\n");
        }
        sb.append("\n");
    }
    writeFile(file, sb.toString());
}



    private void createAppAPIUrls(JSONObject app) throws Exception {
        String appName = app.getString("name");
        File file = new File(projectDirectory, appName + "/api/urls.py");
        StringBuilder sb = new StringBuilder("from django.urls import path, include\nfrom rest_framework.routers import DefaultRouter\nfrom .viewsets import *\n\nrouter = DefaultRouter()\n");

        JSONArray models = app.getJSONArray("models");
        for (int i = 0; i < models.length(); i++) {
            String modelName = models.getJSONObject(i).getString("name");
            sb.append("router.register(r'").append(modelName.toLowerCase()).append("', ").append(modelName).append("ViewSet)\n");
        }
        sb.append("\nurlpatterns = [path('', include(router.urls))]\n");
        writeFile(file, sb.toString());
    }

    private void writeFile(File file, String content) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
}
