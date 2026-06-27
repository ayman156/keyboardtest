package ayman.djangogenerator;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import android.util.Log;


public class AdvancedDjangoGenerator {
    
    private Context context;
    private String projectName;
    private JSONObject projectData;
    private File projectDirectory;
    private boolean useDrf;
    private boolean use_crispy;
    
    // أنواع الحقول المدعومة
    private static final Map<String, String> FIELD_TYPES = new HashMap<String, String>() {{
        put("CharField", "models.CharField");
        put("TextField", "models.TextField");
        put("IntegerField", "models.IntegerField");
        put("BooleanField", "models.BooleanField");
        put("DateField", "models.DateField");
        put("DateTimeField", "models.DateTimeField");
        put("EmailField", "models.EmailField");
        put("URLField", "models.URLField");
        put("ForeignKey", "models.ForeignKey");
        put("OneToOneField", "models.OneToOneField");
        put("ManyToManyField", "models.ManyToManyField");
        put("ImageField", "models.ImageField");
        put("FileField", "models.FileField");
        put("FloatField", "models.FloatField");
        put("DecimalField", "models.DecimalField");
    }};
    
    public AdvancedDjangoGenerator(Context context, String jsonString) throws JSONException {
        this.context = context;
        this.projectData = new JSONObject(jsonString);
        this.projectName = projectData.getString("project_name");
    }
    
    public File generateProject() throws Exception {
        projectDirectory = new File(FileUtil.getExternalStorageDir(), 
                                  "django_projects/" + projectName);
        if (!projectDirectory.exists()) {
            projectDirectory.mkdirs();
        }
        try {
    
    // الدخول إلى كائن settings
    JSONObject settings = projectData.getJSONObject("settings");
    
    
     use_crispy = settings.getBoolean("crispy");


} catch (JSONException e) {
    e.printStackTrace();
}
        createProjectStructure();
        createSettingsFile();
       // createUrlsFile();
       //نقلت تحت التحقق
        createWsgiFile();
        createAsgiFile();
        createBaseTemplates();
        createStaticFiles();
        createRequirementsFile();
        createGitignoreFile();
        createEnvFiles();
        createReadmeFile();
        createMakefile();
        createManagePy();
        createApps();
        createAdminConfig();
        createAuthenticationApp();
        createRESTAPI();
        createIndexTemplate();
        createDockerFiles();
        myfiel();
        copyapp("static", projectDirectory.toString());
        mybase();
        
        //  إضافة Formsets
    if (projectData.has("formsets")) {
        AdDjangoFormsetGenerator formsetGenerator = new AdDjangoFormsetGenerator(context, projectData.toString());
        formsetGenerator.generate();
    }
        
        try {
    
    // الدخول إلى كائن settings
    JSONObject settings = projectData.getJSONObject("settings");
    
    // استخراج القيم البولينية
     useDrf = settings.getBoolean("use_drf");
    boolean supportExcel = settings.getBoolean("support_excel");

    // التحقق من الشرط الذي طلبته
    if (useDrf) {
        RestFramework framework = new RestFramework(context, projectData.toString());
          framework.createRESTFramework();
        
        
    }
    
   if (supportExcel) {
       ImportExportGenerator impoex = new ImportExportGenerator(context, projectData.toString());
          impoex.generate();
        
    }

} catch (JSONException e) {
    e.printStackTrace();
}
// نقلت الى هنا
        createUrlsFile();
      //ملف about.html
     myabout();

        return createZipArchive();
    }
    
    private void createProjectStructure() {
        String[] folders = {
            projectName,
          //  "apps",
            "static",
            "static/css",
            "static/js",
            "static/images",
            "static/fonts",
            "static/webfonts",
           // "media",
            "templates",
            "templates/includes",
            "templates/registration",
            "templates/accounts",
            "templates/admin",
            //"locale",
           // "tests",
           // "fixtures",
           // "logs",
       //     "docs",
           // "scripts"
        };
        
        for (String folder : folders) {
            new File(projectDirectory, folder).mkdirs();
        }
    }
    
    
    
    
    private void myfiel() {
    // تحديد المسار الأساسي للمشروع
    String destDir = projectDirectory.getAbsolutePath();

    // تعريف مصفوفة بالملفات التي ترغب في نسخها من الـ Assets
    String[] assetsToCopy = {"qrun.sh", "st.py"};

    for (String assetPath : assetsToCopy) {
        Assets assetHelper = new Assets(context, assetPath, destDir);
        
        if (assetHelper.copyAssetToPath()) {
            // يمكنك طباعة سجل (Log) هنا للتأكد من نجاح كل ملف
            android.util.Log.d("AssetsCopy", "Successfully copied: " + assetPath);
        } else {
            android.util.Log.e("AssetsCopy", "Failed to copy: " + assetPath);
        }
    }
    }

    
     
    private void myabout(){
        
      Assets assetHelper = new Assets(context, "about.html", projectDirectory.getAbsolutePath() + "/templates");
     if (assetHelper.copyAssetToPath()) {
    // تم النسخ بنجاح
     }

        
        
    }
    private void mybase(){
        
      Assets assetHelper = new Assets(context, "base.html", projectDirectory.getAbsolutePath() + "/templates");
     if (assetHelper.copyAssetToPath()) {
    // تم النسخ بنجاح
     }

        
        
    }
    
    
    
    //انشاء docker 
     private void createDockerFiles() throws Exception {
        // Dockerfile
        File dockerfile = new File(projectDirectory, "Dockerfile");
        String dockerfileContent = "# Use Python 3.11 slim image\n" +
        "FROM python:3.11-slim\n\n" +
        "# Set environment variables\n" +
        "ENV PYTHONDONTWRITEBYTECODE=1\n" +
        "ENV PYTHONUNBUFFERED=1\n" +
        "ENV DJANGO_SETTINGS_MODULE=" + projectName + ".settings\n\n" +
        "# Set work directory\n" +
        "WORKDIR /app\n\n" +
        "# Install system dependencies\n" +
        "RUN apt-get update \\n" +
        "    && apt-get install -y gcc python3-dev musl-dev \\n" +
        "    && rm -rf /var/lib/apt/lists/*\n\n" +
        "# Install Python dependencies\n" +
        "COPY requirements.txt .\n" +
        "RUN pip install --no-cache-dir -r requirements.txt\n\n" +
        "# Copy project\n" +
        "COPY . .\n\n" +
        "# Collect static files\n" +
        "RUN python manage.py collectstatic --noinput\n\n" +
        "# Run as non-root user\n" +
        "RUN useradd -m django && chown -R django:django /app\n" +
        "USER django\n\n" +
        "# Run gunicorn\n" +
        "CMD [\"gunicorn\", \"--bind\", \"0.0.0.0:8000\", \"--workers\", \"3\", \"" + projectName + ".wsgi:application\"]";



    writeFile(dockerfile, dockerfileContent);
    // docker-compose.yml
    File dockerCompose = new File(projectDirectory, "docker-compose.yml");
    String dockerComposeContent = "version: '3.8'\n\n" +
            "services:\n" +
            "  web:\n" +
            "    build: .\n" +
            "    command: python manage.py runserver 0.0.0.0:8000\n" +
            "    volumes:\n" +
            "      - .:/app\n" +
            "    ports:\n" +
            "      - \"8000:8000\"\n" +
            "    environment:\n" +
            "      - DJANGO_SETTINGS_MODULE=" + projectName + ".settings\n" +
            "      - DEBUG=1";
    writeFile(dockerCompose, dockerComposeContent);

    // nginx.conf
    File nginxDir = new File(projectDirectory, "docker/nginx");
    if (!nginxDir.exists()) nginxDir.mkdirs();

    File nginxConf = new File(nginxDir, "nginx.conf");
    String nginxContent = "events {\n" +
            "    worker_connections 1024;\n" +
            "}\n\n" +
            "http {\n" +
            "    upstream django {\n" +
            "        server web:8000;\n" +
            "    }\n\n" +
            "    server {\n" +
            "        listen 80;\n\n" +
            "        location / {\n" +
            "            proxy_pass http://django;\n" +
            "            proxy_set_header Host $host;\n" +
            "            proxy_set_header X-Real-IP $remote_addr;\n" +
            "        }\n\n" +
            "        location /static/ {\n" +
            "            alias /app/staticfiles/;\n" +
            "        }\n\n" +
            "        location /media/ {\n" +
            "            alias /app/media/;\n" +
            "        }\n" +
            "    }\n" +
            "}";
    writeFile(nginxConf, nginxContent);

}
    
    
    
    
    
    
    
    // انشاء ملف الاعداد
    private void createSettingsFile() throws Exception {
    File settingsFile = new File(projectDirectory, projectName + "/settings.py");
    
    JSONObject settings = projectData.getJSONObject("settings");
    JSONArray installedApps = settings.getJSONArray("installed_apps");
    JSONObject database = settings.getJSONObject("database");
    
    StringBuilder content = new StringBuilder();
    content.append("import os\n")
           .append("import sys\n")
           .append("from pathlib import Path\n")
           .append("from datetime import timedelta\n")
           .append("from django.utils.translation import gettext_lazy as _\n\n")
           .append("import environ\n\n")
           
           .append("env = environ.Env()\n\n")
           
           .append("# 1. Support for EXE (PyInstaller Frozen Environment)\n")
           .append("if getattr(sys, 'frozen', False):\n")
           .append("    BASE_DIR = Path(sys._MEIPASS).resolve()\n")
           .append("    EXE_LOCATION = Path(sys.executable).parent\n")
           .append("else:\n")
           .append("    BASE_DIR = Path(__file__).resolve().parent.parent\n")
           .append("    EXE_LOCATION = BASE_DIR\n\n")
           
           .append("# 2. Load .env file\n")
           .append("env_file = EXE_LOCATION / '.env'\n")
           .append("if env_file.exists():\n")
           .append("    environ.Env.read_env(str(env_file))\n\n")
           
           .append("SECRET_KEY = env('SECRET_KEY', default='django-insecure-key')\n")
           .append("DEBUG = env.bool('DEBUG', default=True)\n")
           .append("ALLOWED_HOSTS = env.list('ALLOWED_HOSTS', default=['*'])\n")
           .append("CSRF_TRUSTED_ORIGINS = env.list('CSRF_TRUSTED_ORIGINS', default=[])\n")
           .append("SITE_ID = 1\n\n")
           
           .append("# Localization\n")
           .append("LANGUAGE_CODE = 'ar'\n")
           .append("TIME_ZONE = 'Asia/Riyadh'\n")
           .append("USE_I18N = True\n")
           .append("USE_TZ = True\n")
           .append("LANGUAGES = [('ar', _('العربية')), ('en', _('English'))]\n")
           .append("LOCALE_PATHS = [BASE_DIR / 'locale']\n\n");

    // Installed Apps
    // 1. فحص ما إذا كان المشروع يحتوي على jazzmin
boolean hasJazzmin = false;
content.append("INSTALLED_APPS = [\n");
    for (int i = 0; i < installedApps.length(); i++) {
        if (installedApps.getString(i).contains("azzmin")) {
        hasJazzmin = true;
        content.append("    '").append("jazzmin").append("',\n");
        }else{
         content.append("    '").append(installedApps.getString(i)).append("',\n");
            }
    }
    content.append("    'whitenoise.runserver_nostatic',\n") // WhiteNoise Support
           .append("    'crispy_forms',\n")
           .append("    'crispy_bootstrap5',\n")
           .append("    'accounts',\n")
           .append("    'widget_tweaks',\n");
           if(use_crispy){
    content.append("    'django_select2',\n");             
                   }
    
    if (projectData.has("apps")) {
        JSONArray apps = projectData.getJSONArray("apps");
        for (int i = 0; i < apps.length(); i++) {
            content.append("    '").append(apps.getJSONObject(i).getString("name")).append("',\n");
        }
    }
    content.append("]\n\n");
    //استدعا jazzmin 

// 3. استدعاء دالة إعدادات jazzmin بعد إغلاق المصفوفة
if (hasJazzmin) {
    // نقوم بإلحاق النص الذي تعيده الدالة بـ content
    content.append("#okjazzmin \n");
    content.append(jazzmin()); 
    content.append("\n\n");
}

    content.append("CRISPY_ALLOWED_TEMPLATE_PACKS = 'bootstrap5'\n");
    content.append("CRISPY_TEMPLATE_PACK = 'bootstrap5'\n");
    content.append("WSGI_APPLICATION = '").append(projectName).append(".wsgi.application' \n");
    content.append("ASGI_APPLICATION = '").append(projectName).append(".asgi.application' \n");


    // Middleware مع إضافة WhiteNoise
    content.append("MIDDLEWARE = [\n")
           .append("    'django.middleware.security.SecurityMiddleware',\n")
           .append("    'whitenoise.middleware.WhiteNoiseMiddleware',  # WhiteNoise Middleware\n")
           .append("    'django.contrib.sessions.middleware.SessionMiddleware',\n")
           .append("    'django.middleware.locale.LocaleMiddleware',\n")
           .append("    'django.middleware.common.CommonMiddleware',\n")
           .append("    'django.middleware.csrf.CsrfViewMiddleware',\n")
           .append("    'django.contrib.auth.middleware.AuthenticationMiddleware',\n")
           .append("    'django.contrib.messages.middleware.MessageMiddleware',\n")
           .append("    'django.middleware.clickjacking.XFrameOptionsMiddleware',\n")
           .append("]\n\n");

    content.append("ROOT_URLCONF = '").append(projectName).append(".urls'\n\n")
           .append("TEMPLATES = [{\n")
           .append("    'BACKEND': 'django.template.backends.django.DjangoTemplates',\n")
           .append("    'DIRS': [BASE_DIR / 'templates'],\n")
           .append("    'APP_DIRS': True,\n")
           .append("    'OPTIONS': {'context_processors': [\n")
           .append("        'django.template.context_processors.debug', 'django.template.context_processors.request',\n")
           .append("        'django.contrib.auth.context_processors.auth', 'django.contrib.messages.context_processors.messages',\n")
           .append("        'django.template.context_processors.i18n', 'django.template.context_processors.static', 'django.template.context_processors.media',\n")
           .append("    ]},\n")
           .append("}]\n\n");

    // 3. Database مع دعم .env و Frozen
    content.append("# Database Configuration\n")
           .append("DATABASES = {\n")
           .append("    'default': {\n");
    
    String engine = database.getString("engine");
    content.append("        'ENGINE': '").append(engine).append("',\n");
    
    if (engine.contains("sqlite")) {
        content.append("        'NAME': (EXE_LOCATION / '").append(database.getString("name")).append("') if getattr(sys, 'frozen', False) else (BASE_DIR / '").append(database.getString("name")).append("'),\n");
    } else {
        content.append("        'NAME': env('DB_NAME', default='").append(database.getString("name")).append("'),\n")
               .append("        'USER': env('DB_USER', default='").append(database.getString("user")).append("'),\n")
               .append("        'PASSWORD': env('DB_PASSWORD', default='").append(database.getString("password")).append("'),\n")
               .append("        'HOST': env('DB_HOST', default='").append(database.getString("host")).append("'),\n")
               .append("        'PORT': env('DB_PORT', default='").append(database.getString("port")).append("'),\n");
    }
    content.append("    }\n")
           .append("}\n\n");

    // 4. Static & WhiteNoise
    content.append("# Static Files (WhiteNoise)\n")
           .append("STATIC_URL = '/static/'\n")
           .append("STATIC_ROOT = EXE_LOCATION / 'staticfiles'\n")
           .append("STATICFILES_DIRS = [BASE_DIR / 'static']\n")
           .append("STATICFILES_STORAGE = 'whitenoise.storage.CompressedManifestStaticFilesStorage'\n\n")
           
           .append("# Media Files\n")
           .append("MEDIA_URL = '/media/'\n")
           .append("MEDIA_ROOT = EXE_LOCATION / 'media'\n\n");

    content.append("DEFAULT_AUTO_FIELD = 'django.db.models.BigAutoField'\n")
           .append("LOGIN_REDIRECT_URL = '/'\n")
           .append("LOGOUT_REDIRECT_URL = '/accounts/login/'\n");

    writeFile(settingsFile, content.toString());
}

private String jazzmin() {
    StringBuilder co = new StringBuilder();

    co.append("JAZZMIN_SETTINGS = {\n")
      .append("    # عنوان اللوحة\n")
      .append("    'site_title': '").append(projectName).append("',\n")
      .append("    'site_header': '").append(projectName).append("',\n")
      .append("    'welcome_sign': 'مرحباً بك في لوحة التحكم',\n\n")
      .append("    # تحويل القائمة للأعلى\n")
      .append("    'topmenu_links': [\n")
      .append("        {'name': 'الرئيسية', 'url': 'admin:index', 'permissions': ['auth.view_user']},\n")
      .append("        {'model': 'auth.User'},\n");

    // إضافة التطبيقات من JSONArray
    if (projectData.has("apps")) {
        try {
            JSONArray apps = projectData.getJSONArray("apps");
            for (int i = 0; i < apps.length(); i++) {
                String appName = apps.getJSONObject(i).getString("name");
                co.append("        {'app': '").append(appName).append("'},\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    co.append("    ],\n\n")
      .append("    # الإعدادات الجمالية\n")
      .append("    'navigation_expanded': True,\n")
      .append("    'show_sidebar': False,\n")
      .append("    'changeform_format': 'horizontal_tabs',\n")
      .append("}");
    return co.toString();
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
    

// دالة مساعدة للتأكد من القيمة الافتراضية
private boolean isNumeric(String str) {
	return str.matches("-?\\d+(\\.\\d+)?");
}

private String getFirstCharField(JSONArray fields) throws JSONException {
	// قائمة بالأسماء المفضلة لتمثيل السجل
	String[] priorityNames = {"name", "title", "subject", "username", "label"};
	
	// المحاولة الأولى: البحث عن حقل CharField يحمل اسماً من القائمة أعلاه
	for (String priority : priorityNames) {
		for (int i = 0; i < fields.length(); i++) {
			JSONObject field = fields.getJSONObject(i);
			String name = field.getString("name").toLowerCase();
			String type = field.getString("type");
			
			if ("CharField".equals(type) && name.contains(priority)) {
				return field.getString("name");
			}
		}
	}
	
	// المحاولة الثانية: إذا لم نجد الأسماء المفضلة، نأخذ أول CharField يصادفنا
	for (int i = 0; i < fields.length(); i++) {
		JSONObject field = fields.getJSONObject(i);
		if ("CharField".equals(field.getString("type"))) {
			return field.getString("name");
		}
	}
	
	// المحاولة الأخيرة: إذا لم يوجد أي CharField، نستخدم "pk" (المفتاح الرئيسي)
	// استخدام "pk" في Django أكثر شمولية من "id" لأنه يعمل مع أي Primary Key
	return "pk";
}

private String getProxyParent(String modelName) {
	// في دجانغو، الـ Proxy Model يجب أن يرث من كلاس موديل آخر وليس models.Model
	// يمكنك تطويرها لتبحث في الـ JSON عن الأب، أو إرجاع اسم افتراضي
	return "Base" + modelName; // مثال: إذا كان الموديل UserProxy سيرث من BaseUser
}

private void createModelsFile(JSONObject app) throws Exception {
    String appName = app.getString("name");
    File modelsFile = new File(projectDirectory, appName + "/models.py");
    
    StringBuilder content = new StringBuilder();
    content.append("from django.db import models\n")
           .append("from django.conf import settings\n")
           .append("from django.utils.translation import gettext_lazy as _\n\n");

    // استيراد النماذج من التطبيقات الأخرى
    if (projectData != null && projectData.has("apps")) {
        JSONArray apps = projectData.getJSONArray("apps");
        for (int i = 0; i < apps.length(); i++) {
            JSONObject otherApp = apps.getJSONObject(i);
            String otherAppName = otherApp.getString("name");
            if (!otherAppName.equals(appName)) {
                content.append("from ").append(otherAppName).append(".models import *\n");
            }
        }
    }
    
    if (app.has("models")) {
        JSONArray models = app.getJSONArray("models");
        for (int i = 0; i < models.length(); i++) {
            JSONObject model = models.getJSONObject(i);
            String modelName = model.getString("name");
            JSONObject modelOptions = model.optJSONObject("model_options");
            
            String parentClass = (modelOptions != null && modelOptions.optBoolean("proxy", false)) 
                ? getProxyParent(modelName) : "models.Model";
            
            content.append("\nclass ").append(modelName).append("(").append(parentClass).append("):\n");
            
            if (model.has("fields")) {
                JSONArray fields = model.getJSONArray("fields");
                for (int j = 0; j < fields.length(); j++) {
                    JSONObject field = fields.getJSONObject(j);
                    String fieldName = field.getString("name");
                    String fieldType = field.getString("type");
                    JSONObject opts = field.optJSONObject("field_options");
                    if (opts == null) opts = new JSONObject();
                    
                    content.append("    ").append(fieldName).append(" = models.");
                    
                    StringBuilder fieldArgs = new StringBuilder();
                    
                    switch (fieldType) {
                        case "ForeignKey":
                        case "OneToOneField":
                            String to = opts.optString("to", "auth.User");
                            if (to.equalsIgnoreCase(modelName)) to = "self";
                            fieldArgs.append("'").append(to).append("', ")
                                     .append("on_delete=").append(opts.optString("on_delete", "models.CASCADE")).append(", ");
                            
                            String relName = opts.optString("related_name", appName + "_" + modelName.toLowerCase() + "_" + fieldName + "_set");
                            fieldArgs.append("related_name='").append(relName).append("', ");
                            break;
                            
                        case "ManyToManyField":
                            fieldArgs.append("'").append(opts.optString("to", "")).append("', ");
                            String m2mRelName = opts.optString("related_name", appName + "_" + modelName.toLowerCase() + "_" + fieldName + "_rel");
                            fieldArgs.append("related_name='").append(m2mRelName).append("', ");
                            break;
                            
                        case "DecimalField":
                            fieldArgs.append("max_digits=").append(opts.optString("max_digits", "19")).append(", ")
                                     .append("decimal_places=").append(opts.optString("decimal_places", "2")).append(", ");
                            break;
                            
                        case "CharField":
                            fieldArgs.append("max_length=").append(opts.optString("max_length", "255")).append(", ");
                            break;
                    }

                    // إضافة الخيارات المشتركة في نفس السطر
                    if (opts.has("verbose_name")) fieldArgs.append("verbose_name=_('").append(opts.getString("verbose_name")).append("'), ");
                    if (opts.has("null")) fieldArgs.append("null=").append(opts.getBoolean("null") ? "True" : "False").append(", ");
                    if (opts.has("blank")) fieldArgs.append("blank=").append(opts.getBoolean("blank") ? "True" : "False").append(", ");
                    if (opts.has("unique")) fieldArgs.append("unique=").append(opts.getBoolean("unique") ? "True" : "False").append(", ");
                    
                    if (opts.has("default")) {
                        String def = opts.get("default").toString();
                        if (def.equalsIgnoreCase("true")) fieldArgs.append("default=True, ");
                        else if (def.equalsIgnoreCase("false")) fieldArgs.append("default=False, ");
                        else if (isNumeric(def)) fieldArgs.append("default=").append(def).append(", ");
                        else fieldArgs.append("default='").append(def).append("', ");
                    }

                    if (opts.has("choices") && fieldType.equals("CharField")) {
                        fieldArgs.append(formatChoicesInline(opts.get("choices").toString()));
                    }

                    // تنظيف الفاصلة الأخيرة وإغلاق القوس
                    String finalArgs = fieldArgs.toString().trim();
                    if (finalArgs.endsWith(",")) finalArgs = finalArgs.substring(0, finalArgs.length() - 1);
                    
                    content.append(fieldType).append("(").append(finalArgs).append(")\n");
                }
            }
            
            // الحقول التلقائية (مختصرة في أسطر)
            content.append("\n    # Tracking\n")
                   .append("    created_at = models.DateTimeField(auto_now_add=True, verbose_name=_('Created At'))\n")
                   .append("    updated_at = models.DateTimeField(auto_now=True, verbose_name=_('Updated At'))\n")
                   .append("    created_by = models.ForeignKey(settings.AUTH_USER_MODEL, on_delete=models.SET_NULL, null=True, blank=True, related_name='").append(appName).append("_").append(modelName.toLowerCase()).append("_created_by')\n");
            
            // Meta Class
            content.append("\n    class Meta:\n");
            String vName = (modelOptions != null) ? modelOptions.optString("verbose_name", modelName) : modelName;
            String vNamePlural = (modelOptions != null) ? modelOptions.optString("verbose_name_plural", vName + "s") : vName + "s";
            content.append("        verbose_name = _('").append(vName).append("')\n")
                   .append("        verbose_name_plural = _('").append(vNamePlural).append("')\n");
            
            if (modelOptions != null) {
                if (modelOptions.optBoolean("abstract", false)) content.append("        abstract = True\n");
                if (modelOptions.optBoolean("proxy", false)) content.append("        proxy = True\n");
            }
            
            content.append("\n    def __str__(self):\n")
                   .append("        return str(self.").append(getFirstCharField(model.getJSONArray("fields"))).append(")\n");
        }
    }
    
    writeFile(modelsFile, content.toString());
}

// دالة محسنة لإنتاج الـ Choices في سطر واحد مضغوط
private String formatChoicesInline(String rawChoices) {
    if (rawChoices == null || rawChoices.isEmpty()) return "";
    
    StringBuilder sb = new StringBuilder("choices=[");
    try {
        if (rawChoices.trim().startsWith("[")) {
            JSONArray jsonChoices = new JSONArray(rawChoices);
            for (int i = 0; i < jsonChoices.length(); i++) {
                Object item = jsonChoices.get(i);
                if (item instanceof JSONArray) {
                    JSONArray pair = (JSONArray) item;
                    sb.append("('").append(pair.get(0)).append("', _('").append(pair.get(1)).append("')), ");
                } else {
                    sb.append("('").append(item).append("', _('").append(item).append("')), ");
                }
            }
        } else {
            for (String choice : rawChoices.split(",")) {
                String t = choice.trim();
                if (!t.isEmpty()) sb.append("('").append(t).append("', _('").append(t).append("')), ");
            }
        }
    } catch (Exception e) {
        return "";
    }
    
    String res = sb.toString().trim();
    if (res.endsWith(",")) res = res.substring(0, res.length() - 1);
    return res + "], ";
}

    
    private void createUrlsFile() throws Exception {
        File urlsFile = new File(projectDirectory, projectName + "/urls.py");
        
        StringBuilder content = new StringBuilder();
        content.append("from django.contrib import admin\n")
               .append("from django.urls import path, include\n")
               .append("from django.conf import settings\n")
               .append("from django.conf.urls.static import static\n")
               .append("from django.conf.urls.i18n import i18n_patterns\n\n")
               .append("from django.urls import path\n")
               .append("from django.views.generic import TemplateView\n")
               .append("from django.views.generic.base import RedirectView")
               .append("# نمط URLs مع دعم اللغات\n")
               .append("urlpatterns = i18n_patterns(\n")
               .append("    path('', TemplateView.as_view(template_name='index.html'), name='index'),\n")
               .append("    path('about/', TemplateView.as_view(template_name='about.html'), name='about'),\n");
               if(use_crispy){
        content.append("    path('select2/', include('django_select2.urls')),\n");    
                   }
               if (useDrf){
        content.append("    path('api/', include('").append(projectName).append(".api_urls')),\n");
               }
        content.append("    path('admin/', admin.site.urls),\n")
               .append("    path('accounts/', include('accounts.urls')),\n")
               .append("    path('favicon.ico', RedirectView.as_view(url=settings.STATIC_URL + 'images/favicon.ico')),\n");
        
        if (projectData.has("apps")) {
            JSONArray apps = projectData.getJSONArray("apps");
            for (int i = 0; i < apps.length(); i++) {
                JSONObject app = apps.getJSONObject(i);
                String appName = app.getString("name");
                content.append("    path('").append(appName).append("/', include('")
                       .append(appName).append(".urls')),\n");
            }
        }
        
        content.append("    prefix_default_language=False,\n")
               .append(")\n\n")
               .append("# URLs بدون لغة (للملفات الثابتة والوسائط)\n")
               .append("urlpatterns += [\n")
               .append("    path('i18n/', include('django.conf.urls.i18n')),\n")
               .append("]\n\n")
               .append("if settings.DEBUG:\n")
               .append("    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)\n")
               .append("    urlpatterns += static(settings.STATIC_URL, document_root=settings.STATIC_ROOT)\n")
               .append("admin.site.site_header = '").append(projectName).append("'\n")
               .append("admin.site.site_title = '").append(projectName).append("'\n")
               .append("admin.site.index_title = '").append("لوحة التحكم").append("'\n");
        
        writeFile(urlsFile, content.toString());
    }
    
    private void createWsgiFile() throws Exception {
        File wsgiFile = new File(projectDirectory, projectName + "/wsgi.py");
        String content = "import os\n" +
                "from django.core.wsgi import get_wsgi_application\n\n" +
                "os.environ.setdefault('DJANGO_SETTINGS_MODULE', '" + projectName + ".settings')\n\n" +
                "application = get_wsgi_application()\n";
        writeFile(wsgiFile, content);
    }
    
    private void createAsgiFile() throws Exception {
        File asgiFile = new File(projectDirectory, projectName + "/asgi.py");
        String content = "import os\n" +
                "from django.core.asgi import get_asgi_application\n\n" +
                "os.environ.setdefault('DJANGO_SETTINGS_MODULE', '" + projectName + ".settings')\n\n" +
                "application = get_asgi_application()\n";
        writeFile(asgiFile, content);
    }
    
    private void createBaseTemplates() throws Exception {
        File includesDir = new File(projectDirectory, "templates/includes");
        includesDir.mkdirs();
           /*
        // ملف base.html رئيسي بتصميم احترافي
        File baseTemplate = new File(projectDirectory, "templates/base.html");
        StringBuilder baseSb = new StringBuilder();
        baseSb.append("<!DOCTYPE html>\n")
               .append("{% load static %}")
              .append("<html lang=\"{{ request.LANGUAGE_CODE }}\" dir=\"{% if request.LANGUAGE_CODE == 'ar' %}rtl{% else %}ltr{% endif %}\">\n")
              .append("<head>\n")
              .append("    <meta charset=\"UTF-8\">\n")
              .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
              .append("    <meta name=\"description\" content=\"{{ project_name }} - تطبيق ويب احترافي\">\n")
              .append("    <meta name=\"keywords\" content=\"django, python, web application\">\n")
              .append("    <title>{% block title %}{{ project_name }}{% endblock %}</title>\n")
              .append("    <!-- Font Awesome -->\n")
              .append("    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css\">\n")
              .append("    <!-- Bootstrap 5 with RTL support -->\n")
              .append("    {% if request.LANGUAGE_CODE == 'ar' %}\n")
              .append("    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.rtl.min.css\" rel=\"stylesheet\">\n")
              .append("    {% else %}\n")
              .append("    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n")
              .append("    {% endif %}\n")
              .append("    <!-- Custom CSS -->\n")
              .append("    <link href=\"/static/css/style.css\" rel=\"stylesheet\">\n")
              .append("    {% block extra_css %}{% endblock %}\n")
              .append("</head>\n")
              .append("<body>\n")
              .append("    {% include 'includes/navbar.html' %}\n")
              .append("    <main>\n")
              .append("        {% if messages %}\n")
              .append("            {% for message in messages %}\n")
              .append("                <div class=\"container mt-3\">\n")
              .append("                    <div class=\"alert alert-{{ message.tags }} alert-dismissible fade show\" role=\"alert\">\n")
              .append("                        <i class=\"fas fa-info-circle me-2\"></i>\n")
              .append("                        {{ message }}\n")
              .append("                        <button type=\"button\" class=\"btn-close\" data-bs-dismiss=\"alert\" aria-label=\"Close\"></button>\n")
              .append("                    </div>\n")
              .append("                </div>\n")
              .append("            {% endfor %}\n")
              .append("        {% endif %}\n")
              .append("        {% block content %}{% endblock %}\n")
              .append("    </main>\n")
              .append("    {% include 'includes/footer.html' %}\n")
              .append("    <!-- Bootstrap Bundle with Popper -->\n")
              .append("    <script src=\"https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js\"></script>\n")
              .append("     <script src='{% static '/js/jquery-3.6.0.min.js' %}'></script>")
              .append("    <!-- Custom JS -->\n")
              .append("    <script src=\"/static/js/main.js\"></script>\n")
              .append("    {% block extra_js %}{% endblock %}\n")
              .append("</body>\n")
              .append("</html>");
        writeFile(baseTemplate, baseSb.toString());
          */
        // navbar.html محسن
        File navbarTemplate = new File(projectDirectory, "templates/includes/navbar.html");
        StringBuilder navSb = new StringBuilder();
        navSb.append("<nav class=\"navbar navbar-expand-lg navbar-dark bg-primary shadow-sm\">\n")
             .append("    <div class=\"container\">\n")
             .append("        <a class=\"navbar-brand d-flex align-items-center\" href=\"/\">\n")
             .append("            <i class=\"fas fa-rocket me-2\"></i>\n")
             .append("            <strong>").append(projectName).append("</strong>\n")
             .append("        </a>\n")
             .append("        <button class=\"navbar-toggler\" type=\"button\" data-bs-toggle=\"collapse\" data-bs-target=\"#navbarMain\">\n")
             .append("            <span class=\"navbar-toggler-icon\"></span>\n")
             .append("        </button>\n")
             .append("        <div class=\"collapse navbar-collapse\" id=\"navbarMain\">\n")
             .append("            <ul class=\"navbar-nav me-auto mb-2 mb-lg-0\">\n")
             .append("                <li class=\"nav-item\">\n")
             .append("                    <a class=\"nav-link active\" href=\"/\">\n")
             .append("                        <i class=\"fas fa-home me-1\"></i>\n")
             .append("                        الرئيسية\n")
             .append("                    </a>\n")
             .append("                </li>\n")
             .append("                <li class=\"nav-item\">\n")
             .append("                    <a class=\"nav-link\" href=\"/about/\">\n")
             .append("                        <i class=\"fas fa-info-circle me-1\"></i>\n")
             .append("                        عن المشروع\n")
             .append("                    </a>\n")
             .append("                </li>\n")
             .append("            </ul>\n")
             .append("            <ul class=\"navbar-nav ms-auto mb-2 mb-lg-0\">\n")
             .append("                <!-- Language Switcher -->\n")
             .append("                <li class=\"nav-item dropdown\">\n")
             .append("                    <a class=\"nav-link dropdown-toggle\" href=\"#\" role=\"button\" data-bs-toggle=\"dropdown\">\n")
             .append("                        <i class=\"fas fa-language me-1\"></i>\n")
             .append("                        اللغة\n")
             .append("                    </a>\n")
             .append("                    <ul class=\"dropdown-menu\">\n")
             .append("                        <li><a class=\"dropdown-item\" href=\"/ar/\">العربية</a></li>\n")
             .append("                        <li><a class=\"dropdown-item\" href=\"/en/\">English</a></li>\n")
             .append("                    </ul>\n")
             .append("                </li>\n")
             .append("                {% if user.is_authenticated %}\n")
             .append("                <li class=\"nav-item dropdown\">\n")
             .append("                    <a class=\"nav-link dropdown-toggle d-flex align-items-center\" href=\"#\" role=\"button\" data-bs-toggle=\"dropdown\">\n")
             .append("                        <i class=\"fas fa-user-circle me-2\"></i>\n")
             .append("                        {{ user.username }}\n")
             .append("                    </a>\n")
             .append("                    <ul class=\"dropdown-menu\">\n")
             .append("                        <li><a class=\"dropdown-item\" href=\"accounts/profile/\">\n")
             .append("                            <i class=\"fas fa-user me-2\"></i>الملف الشخصي\n")
             .append("                        </a></li>\n")
             .append("                        <li><a class=\"dropdown-item\" href=\"/accounts/settings/\">\n")
             .append("                            <i class=\"fas fa-cog me-2\"></i>الإعدادات\n")
             .append("                        </a></li>\n")
             .append("                        <li><hr class=\"dropdown-divider\"></li>\n")
             .append("                        <li>\n")
             .append("                            <form method=\"post\" action=\"{% url 'accounts:logout' %}\">\n")
             .append("                                {% csrf_token %}\n")
             .append("                                <button type=\"submit\" class=\"dropdown-item\">\n")
             .append("                                    <i class=\"fas fa-sign-out-alt me-2\"></i>تسجيل الخروج\n")
             .append("                                </button>\n")
             .append("                            </form>\n")
             .append("                        </li>\n")
             .append("                    </ul>\n")
             .append("                </li>\n")
             .append("                {% else %}\n")
             .append("                <li class=\"nav-item\">\n")
             .append("                    <a class=\"nav-link\" href=\"{% url 'accounts:login' %}\">\n")
             .append("                        <i class=\"fas fa-sign-in-alt me-1\"></i>\n")
             .append("                        تسجيل الدخول\n")
             .append("                    </a>\n")
             .append("                </li>\n")
             .append("                <li class=\"nav-item\">\n")
             .append("                    <a class=\"btn btn-outline-light ms-2\" href=\"{% url 'accounts:register' %}\">\n")
             .append("                        <i class=\"fas fa-user-plus me-1\"></i>\n")
             .append("                        إنشاء حساب\n")
             .append("                    </a>\n")
             .append("                </li>\n")
             .append("                {% endif %}\n")
             .append("            </ul>\n")
             .append("        </div>\n")
             .append("    </div>\n")
             .append("</nav>");
        writeFile(navbarTemplate, navSb.toString());

        // footer.html محسن
        File footerTemplate = new File(projectDirectory, "templates/includes/footer.html");
        StringBuilder footSb = new StringBuilder();
        footSb.append("<footer class=\"bg-dark text-white mt-5 py-4\">\n")
              .append("    <div class=\"container\">\n")
              .append("        <div class=\"row\">\n")
              .append("            <div class=\"col-md-4 mb-3\">\n")
              .append("                <h5 class=\"mb-3\">").append(projectName).append("</h5>\n")
              .append("                <p class=\"text-light\">تطبيق ويب احترافي مبني باستخدام Django</p>\n")
              .append("            </div>\n")
              .append("            <div class=\"col-md-4 mb-3\">\n")
              .append("                <h5 class=\"mb-3\">روابط سريعة</h5>\n")
              .append("                <ul class=\"list-unstyled\">\n")
              .append("                    <li><a href=\"/\" class=\"text-light text-decoration-none\">الرئيسية</a></li>\n")
              .append("                    <li><a href=\"/about/\" class=\"text-light text-decoration-none\">عن المشروع</a></li>\n")
              .append("                    <li><a href=\"/contact/\" class=\"text-light text-decoration-none\">اتصل بنا</a></li>\n")
              .append("                    <li><a href=\"/privacy/\" class=\"text-light text-decoration-none\">سياسة الخصوصية</a></li>\n")
              .append("                </ul>\n")
              .append("            </div>\n")
              .append("            <div class=\"col-md-4 mb-3\">\n")
              .append("                <h5 class=\"mb-3\">تابعنا</h5>\n")
              .append("                <div class=\"social-links\">\n")
              .append("                    <a href=\"#\" class=\"text-light me-3\"><i class=\"fab fa-twitter fa-lg\"></i></a>\n")
              .append("                    <a href=\"#\" class=\"text-light me-3\"><i class=\"fab fa-facebook fa-lg\"></i></a>\n")
              .append("                    <a href=\"#\" class=\"text-light me-3\"><i class=\"fab fa-linkedin fa-lg\"></i></a>\n")
              .append("                    <a href=\"#\" class=\"text-light\"><i class=\"fab fa-github fa-lg\"></i></a>\n")
              .append("                </div>\n")
              .append("            </div>\n")
              .append("        </div>\n")
              .append("        <hr class=\"bg-light\">\n")
              .append("        <div class=\"text-center pt-3\">\n")
              .append("            <p class=\"mb-0\">&copy; {% now \"Y\" %} ").append(projectName).append(".فريق سام برو جميع الحقوق محفوظة.</p>\n")
              .append("        </div>\n")
              .append("    </div>\n")
              .append("</footer>");
        writeFile(footerTemplate, footSb.toString());
    }
    
    private void createStaticFiles() throws Exception {
        // ملف CSS رئيسي بتصميم احترافي مع دعم RTL
        File styleCss = new File(projectDirectory, "static/css/style.css");
        String styleContent = "/* Main Styles with RTL Support */\n" +
                ":root {\n" +
                "    --primary-color: #0d6efd;\n" +
                "    --secondary-color: #6c757d;\n" +
                "    --success-color: #198754;\n" +
                "    --danger-color: #dc3545;\n" +
                "    --warning-color: #ffc107;\n" +
                "    --info-color: #0dcaf0;\n" +
                "    --light-color: #f8f9fa;\n" +
                "    --dark-color: #212529;\n" +
                "}\n\n" +
                "body {\n" +
                "    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
                "    background-color: #f8f9fa;\n" +
                "    min-height: 100vh;\n" +
                "    display: flex;\n" +
                "    flex-direction: column;\n" +
                "}\n\n" +
                "main {\n" +
                "    flex: 1;\n" +
                "}\n\n" +
                "/* Login Page Styling */\n" +
                ".login-container {\n" +
                "    max-width: 400px;\n" +
                "    margin: 100px auto;\n" +
                "    padding: 30px;\n" +
                "    background: white;\n" +
                "    border-radius: 10px;\n" +
                "    box-shadow: 0 10px 30px rgba(0,0,0,0.1);\n" +
                "}\n\n" +
                ".login-header {\n" +
                "    text-align: center;\n" +
    "    margin-bottom: 30px;\n" +
    "}\n\n" +
    ".login-header i {\n" +
    "    font-size: 3rem;\n" +
    "    color: var(--primary-color);\n" +
    "    margin-bottom: 15px;\n" +
    "}\n\n" +
    ".form-control {\n" +
    "    padding: 12px 15px;\n" +
    "    border-radius: 8px;\n" +
    "    border: 1px solid #ddd;\n" +
    "    transition: all 0.3s;\n" +
    "}\n\n" +
    ".form-control:focus {\n" +
    "    border-color: var(--primary-color);\n" +
    "    box-shadow: 0 0 0 0.25rem rgba(13, 110, 253, 0.25);\n" +
    "}\n\n" +
    ".btn-primary {\n" +
    "    padding: 12px 30px;\n" +
    "    border-radius: 8px;\n" +
    "    font-weight: 600;\n" +
    "    transition: all 0.3s;\n" +
    "}\n\n" +
    ".btn-primary:hover {\n" +
    "    transform: translateY(-2px);\n" +
    "    box-shadow: 0 5px 15px rgba(13, 110, 253, 0.3);\n" +
    "}\n\n" +
    "/* Card Styling */\n" +
    ".card {\n" +
    "    border: none;\n" +
    "    border-radius: 10px;\n" +
    "    box-shadow: 0 5px 15px rgba(0,0,0,0.08);\n" +
    "    transition: transform 0.3s;\n" +
    "}\n\n" +
    ".card:hover {\n" +
    "    transform: translateY(-5px);\n" +
    "}\n\n" +
    "/* Alert Styling */\n" +
    ".alert {\n" +
    "    border-radius: 8px;\n" +
    "    border: none;\n" +
    "}\n\n" +
    "/* Social Login Buttons */\n" +
    ".social-login {\n" +
    "    margin-top: 20px;\n" +
    "}\n\n" +
    ".social-btn {\n" +
    "    display: block;\n" +
    "    width: 100%;\n" +
    "    padding: 10px;\n" +
    "    margin-bottom: 10px;\n" +
    "    border-radius: 8px;\n" +
    "    text-align: center;\n" +
    "    text-decoration: none;\n" +
    "    color: white;\n" +
    "    transition: opacity 0.3s;\n" +
    "}\n\n" +
    ".social-btn.google {\n" +
    "    background: #db4437;\n" +
    "}\n\n" +
    ".social-btn.facebook {\n" +
    "    background: #4267B2;\n" +
    "}\n\n" +
    ".social-btn:hover {\n" +
    "    opacity: 0.9;\n" +
    "    color: white;\n" +
    "}\n\n" +
    "/* Responsive Design */\n" +
    "@media (max-width: 768px) {\n" +
    "    .login-container {\n" +
    "        margin: 50px auto;\n" +
    "        padding: 20px;\n" +
    "    }\n" +
    "}\n\n" +
    "/* Arabic Specific Styles */\n" +
    "[dir=\"rtl\"] .form-check-input {\n" +
    "    margin-left: 0.5em;\n" +
    "    margin-right: -1.5em;\n" +
    "}\n\n" +
    "[dir=\"rtl\"] .social-links a {\n" +
    "    margin-left: 0.75rem;\n" +
    "    margin-right: 0;\n" +
    "}\n";
        writeFile(styleCss, styleContent);
        
        // ملف JavaScript رئيسي
        File mainJs = new File(projectDirectory, "static/js/main.js");
        String mainJsContent = "// Main JavaScript File\n" +
                "console.log('" + projectName + " loaded successfully');\n\n" +
                "// Initialize tooltips\n" +
                "document.addEventListener('DOMContentLoaded', function() {\n" +
                "    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle=\"tooltip\"]'));\n" +
                "    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {\n" +
                "        return new bootstrap.Tooltip(tooltipTriggerEl);\n" +
                "    });\n\n" +
                "    // Auto dismiss alerts after 5 seconds\n" +
                "    setTimeout(function() {\n" +
                "        var alerts = document.querySelectorAll('.alert');\n" +
                "        alerts.forEach(function(alert) {\n" +
                "            var bsAlert = new bootstrap.Alert(alert);\n" +
                "            bsAlert.close();\n" +
                "        });\n" +
                "    }, 5000);\n\n" +
                "    // Password strength indicator\n" +
                "    const passwordInputs = document.querySelectorAll('input[type=\"password\"]');\n" +
                "    passwordInputs.forEach(input => {\n" +
                "        input.addEventListener('input', function() {\n" +
                "            const password = this.value;\n" +
                "            const strength = checkPasswordStrength(password);\n" +
                "            updatePasswordStrength(strength, this);\n" +
                "        });\n" +
                "    });\n\n" +
                "    // Form validation enhancement\n" +
                "    const forms = document.querySelectorAll('.needs-validation');\n" +
                "    forms.forEach(form => {\n" +
                "        form.addEventListener('submit', function(event) {\n" +
                "            if (!form.checkValidity()) {\n" +
                "                event.preventDefault();\n" +
                "                event.stopPropagation();\n" +
                "            }\n" +
                "            form.classList.add('was-validated');\n" +
                "        }, false);\n" +
                "    });\n" +
                "});\n\n" +
                "// Password strength checker\n" +
                "function checkPasswordStrength(password) {\n" +
                "    let strength = 0;\n" +
                "    \n" +
                "    if (password.length >= 8) strength++;\n" +
                "    if (/[a-z]/.test(password)) strength++;\n" +
                "    if (/[A-Z]/.test(password)) strength++;\n" +
                "    if (/[0-9]/.test(password)) strength++;\n" +
                "    if (/[^A-Za-z0-9]/.test(password)) strength++;\n" +
                "    \n" +
                "    return strength;\n" +
                "}\n\n" +
                "function updatePasswordStrength(strength, input) {\n" +
                "    const meter = input.nextElementSibling;\n" +
                "    if (meter && meter.classList.contains('password-strength-meter')) {\n" +
                "        meter.value = strength;\n" +
                "    }\n" +
                "}\n\n" +
                "// Language switcher\n" +
                "function switchLanguage(lang) {\n" +
                "    document.cookie = `django_language=${lang}; path=/`;\n" +
                "    window.location.reload();\n" +
                "}\n";
        writeFile(mainJs, mainJsContent);
    }
    
    private void createRequirementsFile() throws Exception {
        File reqFile = new File(projectDirectory, "requirements.txt");
        StringBuilder content = new StringBuilder();
        
        if (projectData.has("requirements")) {
            JSONArray requirements = projectData.getJSONArray("requirements");
            for (int i = 0; i < requirements.length(); i++) {
                content.append(requirements.getString(i)).append("\n");
            }
        } else {
            content.append("Django>=5.0.0\n") // الأساس
               .append("django-jazzmin>=3.0.0\n") // واجهة إدارة عصرية
               .append("whitenoise[brotli]>=6.6.0\n") // إدارة الملفات الساكنة للإنتاج
               .append("django-select2>=8.1.0\n") // حقول اختيار ذكية (Select2)
               .append("django-environ>=0.11.0\n") // إدارة متغيرات البيئة (أفضل من dotenv لـ Django)
               
               // مكتبات التنسيق والواجهة
               .append("django-crispy-forms>=2.1\n")
               .append("crispy-bootstrap5>=2024.2\n")
               .append("django-widget-tweaks>=1.5.0\n")
               
               // مكتبات الوظائف المتقدمة
               .append("django-allauth>=0.61.0\n") // التسجيل ووسائل التواصل الاجتماعي
               .append("Pillow>=10.2.0\n") // معالجة الصور
               
               // أدوات التطوير (يمكن فصلها لاحقاً)
               .append("django-debug-toolbar>=4.3.0\n")
               .append("django-extensions>=3.2.3\n")
               .append("python-dotenv>=1.0.0\n")
               .append("gunicorn>=21.2.0\n"); // خادم الإنتاج
        }
        
        writeFile(reqFile, content.toString());
    }
    

    
    private void createGitignoreFile() throws Exception {
        File gitignore = new File(projectDirectory, ".gitignore");
        String content = "# Django\n*.log\n*.pot\n*.pyc\n__pycache__/\nlocal_settings.py\ndb.sqlite3\ndb.sqlite3-journal\nmedia/\nstaticfiles/\n\n" +
                       "# Virtual environment\nvenv/\nenv/\n.env\n.venv\n.env.local\n\n" +
                       "# IDE\n.vscode/\n.idea/\n*.swp\n*.swo\n*~\n\n" +
                       "# OS\n.DS_Store\nThumbs.db\ndesktop.ini\n\n" +
                       "# Coverage reports\nhtmlcov/\n.coverage\n.coverage.*\n\n" +
                       "# Translations\n*.mo\n\n" +
                       "# PyCharm\n.idea/\n\n" +
                       "# Visual Studio Code\n.vscode/\n\n" +
                       "# Jupyter Notebook\n.ipynb_checkpoints/\n";
        writeFile(gitignore, content);
    }
    
    private void createEnvFiles() throws Exception {
        File envExample = new File(projectDirectory, ".env");
        String envExampleContent = "# Django Settings\n" +
                "DEBUG=True\n" +
                "SECRET_KEY=your-secret-key-change-this-in-production\n" +
                "ALLOWED_HOSTS=localhost,127.0.0.1,0.0.0.0\n\n" +
                "# Database Settings\n" +
                "DB_ENGINE=django.db.backends.sqlite3\n" +
                "DB_NAME=db.sqlite3\n" +
                "# For PostgreSQL:\n" +
                "# DB_ENGINE=django.db.backends.postgresql\n" +
                "# DB_NAME=dbname\n" +
                "# DB_USER=username\n" +
                "# DB_PASSWORD=password\n" +
                "# DB_HOST=localhost\n" +
                "# DB_PORT=5432\n\n" +
                "# Email Settings\n" +
                "EMAIL_BACKEND=django.core.mail.backends.console.EmailBackend\n" +
                "# For production email:\n" +
                "# EMAIL_HOST=smtp.gmail.com\n" +
                "# EMAIL_PORT=587\n" +
                "# EMAIL_USE_TLS=True\n" +
                "# EMAIL_HOST_USER=your-email@gmail.com\n" +
                "# EMAIL_HOST_PASSWORD=your-password\n\n" +
                "# Security Settings\n" +
                "SECURE_SSL_REDIRECT=False\n" +
                "SESSION_COOKIE_SECURE=False\n" +
                "CSRF_COOKIE_SECURE=False\n";
        writeFile(envExample, envExampleContent);
    }

    private void createReadmeFile() throws Exception {
        File readme = new File(projectDirectory, "README.md");
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(projectName).append("\n\n")
          .append("## الوصف\n").append(projectData.optString("description", "مشروع Django احترافي مع دعم اللغة العربية")).append("\n\n")
          .append("## المميزات\n")
          .append("- تصميم احترافي متجاوب مع جميع الشاشات\n")
          .append("- دعم كامل للغة العربية (RTL)\n")
          .append("- نظام تسجيل دخول وتسجيل مستخدمين متكامل\n")
          .append("- واجهة إدارة متقدمة\n")
          .append("- نماذج ذكية مع تحقق مدمج\n")
          .append("- ملفات ثابتة محسنة\n\n")
          .append("## التثبيت\n\n")
          .append("```bash\n")
          .append("# 1. إنشاء بيئة افتراضية\n")
          .append("python -m venv venv\n\n")
          .append("# 2. تفعيل البيئة الافتراضية\n")
          .append("# على Windows:\n")
          .append("venv\\Scripts\\activate\n")
          .append("# على Linux/Mac:\n")
          .append("source venv/bin/activate\n\n")
          .append("# 3. تثبيت المتطلبات\n")
          .append("pip install -r requirements.txt\n\n")
          .append("# 4. نسخ ملف البيئة\n")
          .append("cp .env .env\n\n")
          .append("# 5. إجراء الهجرة\n")
          .append("python manage.py migrate\n\n")
          .append("# 6. إنشاء مستخدم مدير\n")
          .append("python manage.py createsuperuser\n\n")
          .append("# 7. تشغيل الخادم\n")
          .append("python manage.py runserver\n")
          .append("```\n\n")
          .append("## الهيكل\n\n")
          .append("```\n")
          .append(projectName).append("/\n")
          .append("├── apps/\n")
          .append("├── media/\n")
          .append("├── static/\n")
          .append("├── templates/\n")
          .append("├── locale/\n")
          .append("└── ").append(projectName).append("/\n")
          .append("```\n\n")
          .append("## الترجمة\n\n")
          .append("لإنشاء ملفات الترجمة:\n")
          .append("```bash\n")
          .append("python manage.py makemessages -l ar\n")
          .append("python manage.py compilemessages\n")
          .append("```\n\n")
          .append("## الترخيص\n")
          .append("MIT License\n");
        writeFile(readme, sb.toString());
    }
    
    private void createMakefile() throws Exception {
        File makefile = new File(projectDirectory, "Makefile");
        String content = ".PHONY: help install migrate run collectstatic createsuperuser test shell\n\n" +
                "help:\n" +
                "\t@echo \"الأوامر المتاحة:\"\n" +
                "\t@echo \"  install     - تثبيت المتطلبات\"\n" +
                "\t@echo \"  migrate     - تطبيق الهجرات\"\n" +
                "\t@echo \"  run         - تشغيل الخادم\"\n" +
                "\t@echo \"  collectstatic - جمع الملفات الثابتة\"\n" +
                "\t@echo \"  createsuperuser - إنشاء مستخدم مدير\"\n" +
                "\t@echo \"  test        - تشغيل الاختبارات\"\n" +
                "\t@echo \"  shell       - فتح شل Django\"\n\n" +
                "install:\n" +
                "\tpip install -r requirements.txt\n\n" +
                "migrate:\n" +
                "\tpython manage.py migrate\n\n" +
                "run:\n" +
                "\tpython manage.py runserver\n\n" +
                "collectstatic:\n" +
                "\tpython manage.py collectstatic --noinput\n\n" +
                "createsuperuser:\n" +
                "\tpython manage.py createsuperuser\n\n" +
                "test:\n" +
                "\tpython manage.py test\n\n" +
                "shell:\n" +
                "\tpython manage.py shell\n";
        writeFile(makefile, content);
    }
    
    private void createApps() throws Exception {
        if (!projectData.has("apps")) return;
        JSONArray apps = projectData.getJSONArray("apps");
        for (int i = 0; i < apps.length(); i++) {
            createApp(apps.getJSONObject(i));
        }
    }
    
    private void createApp(JSONObject app) throws Exception {
        String appName = app.getString("name");
        File appDir = new File(projectDirectory, appName);
        appDir.mkdir();
        
        new File(appDir, "__init__.py").createNewFile();
        createAppConfigFile(app);
        createModelsFile(app);
        createAdminFile(app);
        createUrlsFileForApp(app);
        if(use_crispy){
            createFormsFile_c(app);
            createViewsFile_c(app);
            }else{
                createFormsFile(app);
                createViewsFile(app);
                }
        
        //createSerializersFile(app);
        
        new File(appDir, "migrations/__init__.py").getParentFile().mkdirs();
        new File(appDir, "migrations/__init__.py").createNewFile();
        
        // إنشاء مجلد templates للتطبيق
        File appTemplatesDir = new File(projectDirectory, "templates/" + appName);
        appTemplatesDir.mkdirs();
        
        // إنشاء قوالب CRUD كاملة للتطبيق
        createAppTemplates(app);
        createAppIndexTemplate(app);
    }
     
    private void createAppConfigFile(JSONObject app) throws Exception {
        String appName = app.getString("name");
        File appsFile = new File(projectDirectory, appName + "/apps.py");
        String content = "from django.apps import AppConfig\n\n" +
                "class " + capitalize(appName) + "Config(AppConfig):\n" +
                "    default_auto_field = 'django.db.models.BigAutoField'\n" +
                "    name = '" + appName + "'\n" +
                "    verbose_name = '" + app.optString("verbose_name", capitalize(appName)) + "'\n";
        writeFile(appsFile, content);
    }
    
    private void createAppTemplates(JSONObject app) throws Exception {
    String appName = app.getString("name");
    
    // التأكد من وجود مصفوفة الـ models قبل البدء
    if (!app.has("models") || app.isNull("models")) return;
    
    JSONArray models = app.getJSONArray("models");
    for (int i = 0; i < models.length(); i++) {
        JSONObject model = models.getJSONObject(i);
        String modelName = model.getString("name");
        String lowerModelName = modelName.toLowerCase();
        JSONArray fields = model.getJSONArray("fields");

        // --- التعديل الجديد للتحقق من الاختيارات ---
        
        // 1. تحقق من وجود كائن model_options
if (model.has("model_options") && !model.isNull("model_options")) {
    JSONObject options = model.getJSONObject("model_options");
    
    // التحقق: إذا كان المفتاح غير موجود، أو كان موجوداً ولكنه فارغ (Empty String) أو نال (Null)
    boolean templateMissingOrEmpty = !options.has("template") 
                                    || options.isNull("template") 
                                    || options.getString("template").trim().isEmpty();

    if (templateMissingOrEmpty) {
        // تنفيذ إنشاء القوالب لأنها غير موجودة في الإعدادات
        if(use_crispy){
        createCreateTemplate_c(appName, modelName, lowerModelName, fields);
        createUpdateTemplate_c(appName, modelName, lowerModelName, fields);
        createListTemplate_c(appName, modelName, lowerModelName, fields);
        }else{
            
        createCreateTemplate(appName, modelName, lowerModelName, fields);
        createUpdateTemplate(appName, modelName, lowerModelName, fields);
        createListTemplate(appName, modelName, lowerModelName, fields);
            }
        
        createDetailTemplate(appName, modelName, lowerModelName, fields);
        createDeleteTemplate(appName, modelName, lowerModelName);
        createFormTemplate(appName, modelName, lowerModelName);
            
            
        //System.out.println("Templates created successfully for: " + modelName);
    } else {
        // في حال كان الـ template موجوداً بالفعل
        //System.out.println("Template already exists, skipping creation.");
    }
}

        // إذا لم تتحقق الشروط أعلاه، سيتخطى الكود عملية الإنشاء تلقائياً دون أخطاء
    }
}

    
    private void createListTemplate(String appName, String modelName, String lowerModelName, JSONArray fields) throws Exception {
        File listFile = new File(projectDirectory,  "/templates/" + appName + "/" + lowerModelName + "_list.html");
        
        StringBuilder sb = new StringBuilder();
        sb.append("{% extends 'base.html' %}\n")
          .append("{% load i18n %}\n")
          .append("{% load static %}\n\n")
          .append("{% block title %}{% trans 'قائمة' %} {{ ").append(modelName).append(".model.verbose_name_plural }}{% endblock %}\n\n")
          .append("{% block content %}\n")
          .append("<div class=\"container\">\n")
          .append("    <div class=\"row mb-4\">\n")
          .append("        <div class=\"col-md-8\">\n")
          .append("            <h1><i class=\"fas fa-list me-2\"></i>{% trans 'قائمة' %} {{ ").append(modelName).append(".model.verbose_name_plural }}</h1>\n")
          .append("        </div>\n")
          .append("        <div class=\"col-md-4 text-end\">\n")
          .append("            <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_create' %}\" class=\"btn btn-primary\">\n")
          .append("                <i class=\"fas fa-plus me-1\"></i>{% trans 'إضافة جديد' %}\n")
          .append("            </a>\n")
          .append("        </div>\n")
          .append("    </div>\n\n")
          .append("    <!-- شريط البحث والتصفية -->\n")
          .append("    <div class=\"card mb-4\">\n")
          .append("        <div class=\"card-body\">\n")
          .append("            <form method=\"get\" class=\"row g-3\">\n")
          .append("                <div class=\"col-md-8\">\n")
          .append("                    <div class=\"input-group\">\n")
          .append("                        <input type=\"text\" name=\"q\" class=\"form-control\" placeholder=\"{% trans 'بحث...' %}\" value=\"{{ request.GET.q }}\">\n")
          .append("                        <button class=\"btn btn-outline-secondary\" type=\"submit\">\n")
          .append("                            <i class=\"fas fa-search\"></i>\n")
          .append("                        </button>\n")
          .append("                    </div>\n")
          .append("                </div>\n")
          .append("                <div class=\"col-md-4\">\n")
          .append("                    <select name=\"sort\" class=\"form-select\" onchange=\"this.form.submit()\">\n")
          .append("                        <option value=\"\">{% trans 'ترتيب حسب' %}</option>\n")
          .append("                        <option value=\"newest\" {% if request.GET.sort == 'newest' %}selected{% endif %}>{% trans 'الأحدث' %}</option>\n")
          .append("                        <option value=\"oldest\" {% if request.GET.sort == 'oldest' %}selected{% endif %}>{% trans 'الأقدم' %}</option>\n");
        
        // إضافة خيارات التصفية حسب الحقول
        for (int i = 0; i < Math.min(fields.length(), 5); i++) {
            String fieldName = fields.getJSONObject(i).getString("name");
            sb.append("                        <option value=\"").append(fieldName)
              .append("\" {% if request.GET.sort == '").append(fieldName)
              .append("' %}selected{% endif %}>").append(fieldName).append("</option>\n");
        }
        
        sb.append("                    </select>\n")
          .append("                </div>\n")
          .append("            </form>\n")
          .append("        </div>\n")
          .append("    </div>\n\n")
          .append("    <!-- جدول البيانات -->\n")
          .append("    <div class=\"card\">\n")
          .append("        <div class=\"card-body\">\n")
          .append("            {% if items %}\n")
          .append("            <div class=\"table-responsive\">\n")
          .append("                <table class=\"table table-hover table-striped\">\n")
          .append("                    <thead class=\"table-dark\">\n")
          .append("                        <tr>\n");
        
        // عناوين الأعمدة
        for (int i = 0; i < Math.min(fields.length(), 6); i++) {
            String fieldName = fields.getJSONObject(i).getString("name");
            sb.append("                            <th>").append(fieldName).append("</th>\n");
        }
        sb.append("                            <th>{% trans 'الإجراءات' %}</th>\n")
          .append("                        </tr>\n")
          .append("                    </thead>\n")
          .append("                    <tbody>\n")
          .append("                        {% for item in items %}\n")
          .append("                        <tr>\n");
        
        // بيانات الصفوف
        for (int i = 0; i < Math.min(fields.length(), 6); i++) {
            String fieldName = fields.getJSONObject(i).getString("name");
            sb.append("                            <td>{{ item.").append(fieldName).append(" }}</td>\n");
        }
        
        sb.append("                            <td>\n")
          .append("                                <div class=\"btn-group btn-group-sm\">\n")
          .append("                                    <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_detail' item.id %}\" class=\"btn btn-info\" title=\"{% trans 'عرض' %}\">\n")
          .append("                                        <i class=\"fas fa-eye\"></i>\n")
          .append("                                    </a>\n")
          .append("                                    <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_update' item.id %}\" class=\"btn btn-warning\" title=\"{% trans 'تعديل' %}\">\n")
          .append("                                        <i class=\"fas fa-edit\"></i>\n")
          .append("                                    </a>\n")
  // تم تغيير الزر هنا من Button يفتح Modal إلى رابط (Anchor) يفتح صفحة الحذف
          .append("                                    <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_delete' item.id %}\" class=\"btn btn-danger\" title=\"{% trans 'حذف' %}\">\n")
          .append("                                        <i class=\"fas fa-trash\"></i>\n")
          .append("                                    </a>\n")
          .append("                                </div>\n")
          .append("                            </td>\n")
          .append("                        </tr>\n")
          .append("                        {% endfor %}\n")
          .append("                    </tbody>\n")
          .append("                </table>\n")
          .append("            </div>\n\n")

          .append("            <!-- الترقيم -->\n")
          .append("            {% if items.has_other_pages %}\n")
          .append("            <nav aria-label=\"Page navigation\" class=\"mt-4\">\n")
          .append("                <ul class=\"pagination justify-content-center\">\n")
          .append("                    {% if items.has_previous %}\n")
          .append("                    <li class=\"page-item\">\n")
          .append("                        <a class=\"page-link\" href=\"?page={{ items.previous_page_number }}{% if request.GET.q %}&q={{ request.GET.q }}{% endif %}{% if request.GET.sort %}&sort={{ request.GET.sort }}{% endif %}\">\n")
          .append("                            &laquo; {% trans 'السابق' %}\n")
          .append("                        </a>\n")
          .append("                    </li>\n")
          .append("                    {% else %}\n")
          .append("                    <li class=\"page-item disabled\">\n")
          .append("                        <span class=\"page-link\">&laquo; {% trans 'السابق' %}</span>\n")
          .append("                    </li>\n")
          .append("                    {% endif %}\n\n")
          .append("                    {% for i in items.paginator.page_range %}\n")
          .append("                        {% if items.number == i %}\n")
          .append("                        <li class=\"page-item active\">\n")
          .append("                            <span class=\"page-link\">{{ i }}</span>\n")
          .append("                        </li>\n")
          .append("                        {% else %}\n")
          .append("                        <li class=\"page-item\">\n")
          .append("                            <a class=\"page-link\" href=\"?page={{ i }}{% if request.GET.q %}&q={{ request.GET.q }}{% endif %}{% if request.GET.sort %}&sort={{ request.GET.sort }}{% endif %}\">{{ i }}</a>\n")
          .append("                        </li>\n")
          .append("                        {% endif %}\n")
          .append("                    {% endfor %}\n\n")
          .append("                    {% if items.has_next %}\n")
          .append("                    <li class=\"page-item\">\n")
          .append("                        <a class=\"page-link\" href=\"?page={{ items.next_page_number }}{% if request.GET.q %}&q={{ request.GET.q }}{% endif %}{% if request.GET.sort %}&sort={{ request.GET.sort }}{% endif %}\">\n")
          .append("                            {% trans 'التالي' %} &raquo;\n")
          .append("                        </a>\n")
          .append("                    </li>\n")
          .append("                    {% else %}\n")
          .append("                    <li class=\"page-item disabled\">\n")
          .append("                        <span class=\"page-link\">{% trans 'التالي' %} &raquo;</span>\n")
          .append("                    </li>\n")
          .append("                    {% endif %}\n")
          .append("                </ul>\n")
          .append("            </nav>\n")
          .append("            {% endif %}\n")
          .append("            {% else %}\n")
          .append("            <div class=\"alert alert-info text-center\">\n")
          .append("                <i class=\"fas fa-info-circle fa-2x mb-3\"></i>\n")
          .append("                <h4>{% trans 'لا توجد بيانات' %}</h4>\n")
          .append("                <p>{% trans 'لم يتم إضافة أي عناصر بعد' %}</p>\n")
          .append("                <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_create' %}\" class=\"btn btn-primary mt-2\">\n")
          .append("                    <i class=\"fas fa-plus me-1\"></i>{% trans 'إضافة أول عنصر' %}\n")
          .append("                </a>\n")
          .append("            </div>\n")
          .append("            {% endif %}\n")
          .append("        </div>\n")
          .append("        <div class=\"card-footer text-muted\">\n")
          .append("            <small>{% trans 'إجمالي العناصر:' %} {{ items.count }}</small>\n")
          .append("        </div>\n")
          .append("    </div>\n")
          .append("</div>\n")
          .append("{% endblock %}\n\n")
          .append("{% block extra_js %}\n")
          .append("<script>\n")
          .append("    // البحث التلقائي مع تأخير\n")
          .append("    let searchTimeout;\n")
          .append("    document.querySelector('input[name=\"q\"]').addEventListener('input', function(e) {\n")
          .append("        clearTimeout(searchTimeout);\n")
          .append("        searchTimeout = setTimeout(() => {\n")
          .append("            this.form.submit();\n")
          .append("        }, 500);\n")
          .append("    });\n")
          .append("</script>\n")
          .append("{% endblock %}\n");
        
        writeFile(listFile, sb.toString());
    }
    private void createListTemplate_c(String appName, String modelName, String lowerModelName, JSONArray fields) throws Exception {
    File listFile = new File(projectDirectory, "/templates/" + appName + "/" + lowerModelName + "_list.html");
    
    StringBuilder sb = new StringBuilder();
    sb.append("{% extends 'base.html' %}\n")
      .append("{% load i18n %}\n")
      .append("{% load static %}\n\n")
      // استخدام verbose_name_plural لجعل العنوان ديناميكي ومترجم
      .append("{% block title %}{% trans 'قائمة' %} {{ ").append(modelName).append(".model.verbose_name_plural }}{% endblock %}\n\n")
      
      .append("{% block content %}\n")
      .append("<div class=\"container-fluid py-4\">\n") // fluid ليعطي مساحة أكبر للجداول الضخمة
      
      .append("    <div class=\"d-flex justify-content-between align-items-center mb-4\">\n")
      .append("        <h1 class=\"h3 mb-0 text-gray-800\"><i class=\"fas fa-list me-2 text-primary\"></i>{{ ").append(modelName).append(".model.verbose_name_plural }}</h1>\n")
      .append("        <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_create' %}\" class=\"btn btn-primary shadow-sm\">\n")
      .append("            <i class=\"fas fa-plus me-1\"></i>{% trans 'إضافة جديد' %}\n")
      .append("        </a>\n")
      .append("    </div>\n\n")

      // --- شريط البحث والتصفية المحسن ---
      .append("    <div class=\"card shadow mb-4 border-0\">\n")
      .append("        <div class=\"card-body\">\n")
      .append("            <form method=\"get\" class=\"row g-3 align-items-center\">\n")
      .append("                <div class=\"col-md-6\">\n")
      .append("                    <div class=\"input-group shadow-sm\">\n")
      .append("                        <span class=\"input-group-text bg-white border-end-0\"><i class=\"fas fa-search text-muted\"></i></span>\n")
      .append("                        <input type=\"text\" name=\"q\" class=\"form-control border-start-0\" placeholder=\"{% trans 'بحث في البيانات...' %}\" value=\"{{ request.GET.q }}\">\n")
      .append("                        {% if request.GET.q %}\n") // زر لمسح البحث إذا وجد
      .append("                           <a href=\"?\" class=\"btn btn-outline-light text-muted border border-start-0\"><i class=\"fas fa-times\"></i></a>\n")
      .append("                        {% endif %}\n")
      .append("                    </div>\n")
      .append("                </div>\n")
      .append("                <div class=\"col-md-3\">\n")
      .append("                    <select name=\"sort\" class=\"form-select shadow-sm\" onchange=\"this.form.submit()\">\n")
      .append("                        <option value=\"\">{% trans 'ترتيب حسب' %}</option>\n")
      .append("                        <option value=\"-id\" {% if request.GET.sort == '-id' %}selected{% endif %}>{% trans 'الأحدث أولاً' %}</option>\n")
      .append("                        <option value=\"id\" {% if request.GET.sort == 'id' %}selected{% endif %}>{% trans 'الأقدم أولاً' %}</option>\n");
        
    // الترتيب حسب أول 3 حقول (نصية عادة)
    for (int i = 0; i < Math.min(fields.length(), 3); i++) {
        String fieldName = fields.getJSONObject(i).getString("name");
        sb.append("                        <option value=\"").append(fieldName)
          .append("\" {% if request.GET.sort == '").append(fieldName)
          .append("' %}selected{% endif %}>").append(fieldName).append("</option>\n");
    }
    
    sb.append("                    </select>\n")
      .append("                </div>\n")
      .append("            </form>\n")
      .append("        </div>\n")
      .append("    </div>\n\n")

      // --- جدول البيانات ---
      .append("    <div class=\"card shadow border-0\">\n")
      .append("        <div class=\"card-body p-0\">\n") // p-0 لجعل الجدول يملأ البطاقة
      .append("            {% if items %}\n")
      .append("            <div class=\"table-responsive\">\n")
      .append("                <table class=\"table table-hover align-middle mb-0\">\n")
      .append("                    <thead class=\"table-light border-bottom\">\n")
      .append("                        <tr>\n");
    
    // عناوين الأعمدة باستخدام verbose_name
   /*
    for (int i = 0; i < Math.min(fields.length(), 6); i++) {
        String fieldName = fields.getJSONObject(i).getString("name");
        // هنا نستخدم حيلة في جانجو للوصول للـ label الخاص بالحقل
        sb.append("                            <th class=\"px-4 text-secondary\">").append(fieldName).append("</th>\n");
    }*/
    for (int i = 0; i < Math.min(fields.length(), 6); i++) {
    JSONObject field = fields.getJSONObject(i);
    String fieldName = field.getString("name");
    String label = fieldName; // القيمة الافتراضية

    // التحقق من وجود field_options و verbose_name
    if (field.has("field_options")) {
        JSONObject options = field.getJSONObject("field_options");
        if (options.has("verbose_name") && !options.isNull("verbose_name")) {
            label = options.getString("verbose_name");
        }
    }

    sb.append("                            <th class=\"px-4 text-secondary\">").append(label).append("</th>\n");
    }

    sb.append("                            <th class=\"text-center px-4 text-secondary\">{% trans 'الإجراءات' %}</th>\n")
      .append("                        </tr>\n")
      .append("                    </thead>\n")
      .append("                    <tbody>\n")
      .append("                        {% for item in items %}\n")
      .append("                        <tr>\n");
    
    for (int i = 0; i < Math.min(fields.length(), 6); i++) {
        String fieldName = fields.getJSONObject(i).getString("name");
        sb.append("                            <td class=\"px-4\">{{ item.").append(fieldName).append(" }}</td>\n");
    }
    
    sb.append("                            <td class=\"text-center px-4\">\n")
      .append("                                <div class=\"btn-group shadow-sm\">\n")
      .append("                                    <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_detail' item.id %}\" class=\"btn btn-sm btn-outline-info\" title=\"{% trans 'عرض' %}\">\n")
      .append("                                        <i class=\"fas fa-eye\"></i>\n")
      .append("                                    </a>\n")
      .append("                                    <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_update' item.id %}\" class=\"btn btn-sm btn-outline-warning\" title=\"{% trans 'تعديل' %}\">\n")
      .append("                                        <i class=\"fas fa-edit\"></i>\n")
      .append("                                    </a>\n")
      .append("                                    <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_delete' item.id %}\" class=\"btn btn-sm btn-outline-danger\" title=\"{% trans 'حذف' %}\">\n")
      .append("                                        <i class=\"fas fa-trash\"></i>\n")
      .append("                                    </a>\n")
      .append("                                </div>\n")
      .append("                            </td>\n")
      .append("                        </tr>\n")
      .append("                        {% endfor %}\n")
      .append("                    </tbody>\n")
      .append("                </table>\n")
      .append("            </div>\n\n")

      // --- الترقيم المحسن (حفظ المعاملات) ---
      .append("            {% if items.has_other_pages %}\n")
      .append("            <div class=\"card-footer bg-white border-top-0 py-3\">\n")
      .append("                <nav aria-label=\"Page navigation\">\n")
      .append("                    <ul class=\"pagination pagination-sm justify-content-center mb-0\">\n")
      .append("                        {% if items.has_previous %}\n")
      .append("                            <li class=\"page-item\"><a class=\"page-link shadow-none\" href=\"?page=1{% if request.GET.q %}&q={{ request.GET.q }}{% endif %}{% if request.GET.sort %}&sort={{ request.GET.sort }}{% endif %}\">&laquo; {% trans 'الأول' %}</a></li>\n")
      .append("                        {% endif %}\n")
      .append("                        {% for num in items.paginator.page_range %}\n")
      .append("                            {% if items.number == num %}\n")
      .append("                                <li class=\"page-item active\"><span class=\"page-link\">{{ num }}</span></li>\n")
      .append("                            {% elif num > items.number|add:'-3' and num < items.number|add:'3' %}\n")
      .append("                                <li class=\"page-item\"><a class=\"page-link shadow-none\" href=\"?page={{ num }}{% if request.GET.q %}&q={{ request.GET.q }}{% endif %}{% if request.GET.sort %}&sort={{ request.GET.sort }}{% endif %}\">{{ num }}</a></li>\n")
      .append("                            {% endif %}\n")
      .append("                        {% endfor %}\n")
      .append("                        {% if items.has_next %}\n")
      .append("                            <li class=\"page-item\"><a class=\"page-link shadow-none\" href=\"?page={{ items.paginator.num_pages }}{% if request.GET.q %}&q={{ request.GET.q }}{% endif %}{% if request.GET.sort %}&sort={{ request.GET.sort }}{% endif %}\">{% trans 'الأخير' %} &raquo;</a></li>\n")
      .append("                        {% endif %}\n")
      .append("                    </ul>\n")
      .append("                </nav>\n")
      .append("            </div>\n")
      .append("            {% endif %}\n")
      
      .append("            {% else %}\n")
      // حالة "لا توجد بيانات" بشكل جمالي
      .append("            <div class=\"text-center py-5\">\n")
      .append("                <img src=\"{% static 'images/empty.svg' %}\" style=\"width:150px; opacity:0.5\" class=\"mb-4\">\n")
      .append("                <h4 class=\"text-muted\">{% trans 'القائمة فارغة حالياً' %}</h4>\n")
      .append("                <p class=\"text-secondary\">{% trans 'ابدأ بإضافة أول سجل من خلال الزر في الأعلى' %}</p>\n")
      .append("            </div>\n")
      .append("            {% endif %}\n")
      .append("        </div>\n")
      .append("    </div>\n")
      .append("</div>\n")
      .append("{% endblock %}\n");
    
    writeFile(listFile, sb.toString());
}

    
    private void createDetailTemplate(String appName, String modelName, String lowerModelName, JSONArray fields) throws Exception {
        File detailFile = new File(projectDirectory,  "/templates/" + appName + "/" + lowerModelName + "_detail.html");
        
        StringBuilder sb = new StringBuilder();
        sb.append("{% extends 'base.html' %}\n")
          .append("{% load i18n %}\n\n")
          .append("{% block title %}{% trans 'تفاصيل' %} {{ ").append(modelName).append(".model.verbose_name }}{% endblock %}\n\n")
          .append("{% block content %}\n")
          .append("<div class=\"container\">\n")
          .append("    <div class=\"row mb-4\">\n")
          .append("        <div class=\"col-md-8\">\n")
          .append("            <nav aria-label=\"breadcrumb\">\n")
          .append("                <ol class=\"breadcrumb\">\n")
          .append("                    <li class=\"breadcrumb-item\">\n")
          .append("                        <a href=\"{% url '").append(appName).append(":index' %}\">{% trans 'الرئيسية' %}</a>\n")
          .append("                    </li>\n")
          .append("                    <li class=\"breadcrumb-item\">\n")
          .append("                        <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_list' %}\">{{ ").append(modelName).append(".model.verbose_name_plural }}</a>\n")
          .append("                    </li>\n")
          .append("                    <li class=\"breadcrumb-item active\" aria-current=\"page\">{{ item }}</li>\n")
          .append("                </ol>\n")
          .append("            </nav>\n")
          .append("            <h1><i class=\"fas fa-info-circle me-2\"></i>{{ item }}</h1>\n")
          .append("        </div>\n")
          .append("        <div class=\"col-md-4 text-end\">\n")
          .append("            <div class=\"btn-group\">\n")
          .append("                <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_update' item.id %}\" class=\"btn btn-warning\">\n")
          .append("                    <i class=\"fas fa-edit me-1\"></i>{% trans 'تعديل' %}\n")
          .append("                </a>\n")
          .append("                <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_list' %}\" class=\"btn btn-secondary\">\n")
          .append("                    <i class=\"fas fa-arrow-left me-1\"></i>{% trans 'رجوع' %}\n")
          .append("                </a>\n")
          .append("            </div>\n")
          .append("        </div>\n")
          .append("    </div>\n\n")
          .append("    <div class=\"card\">\n")
          .append("        <div class=\"card-header\">\n")
          .append("            <h5 class=\"mb-0\"><i class=\"fas fa-table me-2\"></i>{% trans 'البيانات' %}</h5>\n")
          .append("        </div>\n")
          .append("        <div class=\"card-body\">\n")
          .append("            <div class=\"row\">\n");
        
        // عرض جميع الحقول
        for (int i = 0; i < fields.length(); i++) {
            JSONObject field = fields.getJSONObject(i);
            String fieldName = field.getString("name");
            String fieldType = field.getString("type");
            
            sb.append("                <div class=\"col-md-6 mb-3\">\n")
              .append("                    <div class=\"form-group\">\n")
              .append("                        <label class=\"form-label fw-bold\">").append(fieldName).append(":</label>\n")
              .append("                        <div class=\"form-control-plaintext\">\n");
            
            if (fieldType.equals("BooleanField")) {
                sb.append("                            {% if item.").append(fieldName).append(" %}\n")
                  .append("                            <span class=\"badge bg-success\"><i class=\"fas fa-check\"></i> {% trans 'نعم' %}</span>\n")
                  .append("                            {% else %}\n")
                  .append("                            <span class=\"badge bg-danger\"><i class=\"fas fa-times\"></i> {% trans 'لا' %}</span>\n")
                  .append("                            {% endif %}\n");
            } else if (fieldType.equals("ImageField") || fieldType.equals("FileField")) {
                sb.append("                            {% if item.").append(fieldName).append(" %}\n")
                  .append("                            <a href=\"{{ item.").append(fieldName).append(".url }}\" target=\"_blank\">\n")
                  .append("                                <i class=\"fas fa-file me-1\"></i>{% trans 'عرض الملف' %}\n")
                  .append("                            </a>\n")
                  .append("                            {% else %}\n")
                  .append("                            <span class=\"text-muted\">{% trans 'لا يوجد ملف' %}</span>\n")
                  .append("                            {% endif %}\n");
            } else if (fieldType.equals("ForeignKey") || fieldType.equals("OneToOneField")) {
                sb.append("                            {% if item.").append(fieldName).append(" %}\n")
                  .append("                            <a href=\"{{ item.").append(fieldName).append(".get_absolute_url }}\">{{ item.").append(fieldName).append(" }}</a>\n")
                  .append("                            {% else %}\n")
                  .append("                            <span class=\"text-muted\">---</span>\n")
                  .append("                            {% endif %}\n");
            } else if (fieldType.equals("ManyToManyField")) {
                sb.append("                            {% for obj in item.").append(fieldName).append(".all %}\n")
                  .append("                            <span class=\"badge bg-primary me-1\">{{ obj }}</span>\n")
                  .append("                            {% empty %}\n")
                  .append("                            <span class=\"text-muted\">---</span>\n")
                  .append("                            {% endfor %}\n");
            } else {
                sb.append("                            {{ item.").append(fieldName).append("|default:'---' }}\n");
            }
            
            sb.append("                        </div>\n")
              .append("                    </div>\n")
              .append("                </div>\n");
        }
        
        sb.append("            </div>\n")
          .append("        </div>\n")
          .append("        <div class=\"card-footer\">\n")
          .append("            <small class=\"text-muted\">\n")
          .append("                <i class=\"fas fa-calendar me-1\"></i>\n")
          .append("                {% trans 'تم الإنشاء في:' %} {{ item.created_at|date:'Y-m-d H:i' }}\n")
          .append("                {% if item.updated_at %}\n")
          .append("                | <i class=\"fas fa-edit me-1\"></i>\n")
          .append("                {% trans 'تم التحديث في:' %} {{ item.updated_at|date:'Y-m-d H:i' }}\n")
          .append("                {% endif %}\n")
          .append("            </small>\n")
          .append("        </div>\n")
          .append("    </div>\n")
          .append("</div>\n")
          .append("{% endblock %}\n");
        
        writeFile(detailFile, sb.toString());
    }

    
    private void createCreateTemplate(String appName, String modelName, String lowerModelName, JSONArray fields) throws Exception {
    File createFile = new File(projectDirectory, "/templates/" + appName + "/" + lowerModelName + "_create.html");
    
    StringBuilder sb = new StringBuilder();
    sb.append("{% extends 'base.html' %}\n")
      .append("{% load crispy_forms_tags %}\n")
      .append("{% load i18n %}\n\n")
      .append("{% block title %}{% trans 'إضافة جديد' %} | {{ ").append(modelName).append(".model.verbose_name }}{% endblock %}\n\n")
      .append("{% block content %}\n")
      .append("<div class=\"container-fluid py-4\">\n") // container-fluid يعطي مساحة أكبر للبيانات الكثيرة
      .append("    \n")
      .append("    <div class=\"row mb-4\">\n")
      .append("        <div class=\"col-12\">\n")
      .append("            <nav aria-label=\"breadcrumb\">\n")
      .append("                <ol class=\"breadcrumb bg-light p-3 rounded shadow-sm\">\n")
      .append("                    <li class=\"breadcrumb-item\"><a href=\"{% url '").append(appName).append(":index' %}\" class=\"text-decoration-none\">{% trans 'الرئيسية' %}</a></li>\n")
      .append("                    <li class=\"breadcrumb-item\"><a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_list' %}\" class=\"text-decoration-none\">{{ ").append(modelName).append(".model.verbose_name_plural }}</a></li>\n")
      .append("                    <li class=\"breadcrumb-item active text-dark fw-bold\">{% trans 'إضافة جديد' %}</li>\n")
      .append("                </ol>\n")
      .append("            </nav>\n")
      .append("            <h2 class=\"fw-bold text-primary\"><i class=\"fas fa-plus-circle me-2\"></i>{% trans 'إضافة' %} {{ ").append(modelName).append(".model.verbose_name }}</h2>\n")
      .append("        </div>\n")
      .append("    </div>\n\n")

      .append("    \n")
      .append("    <div class=\"card border-0 shadow-lg\">\n")
      .append("        <div class=\"card-header bg-white py-3\">\n")
      .append("            <h5 class=\"card-title mb-0 text-muted italic\">{% trans 'يرجى ملء كافة الحقول المطلوبة' %}</h5>\n")
      .append("        </div>\n")
      .append("        <div class=\"card-body p-4\">\n")
      .append("            <form method=\"post\" enctype=\"multipart/form-data\" class=\"needs-validation\" novalidate>\n")
      .append("                {% csrf_token %}\n")
      
      .append("                <div class=\"row\">\n");

    // الجزء المسؤول عن تقسيم الحقول: كل 4 في صف واحد
    for (int i = 0; i < fields.length(); i++) {
        JSONObject field = fields.getJSONObject(i);
        String fieldName = field.getString("name");
        
        sb.append("                    <div class=\"col-md-3 mb-3\">\n") // col-md-3 تعني 4 أعمدة في الصف
          .append("                        {{ form.").append(fieldName).append("|as_crispy_field }}\n")
          .append("                    </div>\n");
    }

    sb.append("                </div>\n\n") // إغلاق الـ row

      .append("                \n")
      .append("                <div class=\"row mt-4 border-top pt-4\">\n")
      .append("                    <div class=\"col-12 d-flex justify-content-end gap-2\">\n")
      .append("                        <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_list' %}\" class=\"btn btn-outline-secondary px-4\">\n")
      .append("                            <i class=\"fas fa-times me-1\"></i>{% trans 'إلغاء' %}\n")
      .append("                        </a>\n")
      .append("                        <button type=\"submit\" class=\"btn btn-primary px-5 shadow-sm\">\n")
      .append("                            <i class=\"fas fa-save me-1\"></i>{% trans 'حفظ البيانات' %}\n")
      .append("                        </button>\n")
      .append("                    </div>\n")
      .append("                </div>\n")
      .append("            </form>\n")
      .append("        </div>\n")
      .append("    </div>\n")
      .append("</div>\n")
      .append("{% endblock %}\n\n")
      
      .append("{% block extra_js %}\n")
      .append("<script>\n")
      .append("    // Form validation\n")
      .append("    (function() {\n")
      .append("        'use strict';\n")
      .append("        window.addEventListener('load', function() {\n")
      .append("            var forms = document.getElementsByClassName('needs-validation');\n")
      .append("            var validation = Array.prototype.filter.call(forms, function(form) {\n")
      .append("                form.addEventListener('submit', function(event) {\n")
      .append("                    if (form.checkValidity() === false) {\n")
      .append("                        event.preventDefault();\n")
      .append("                        event.stopPropagation();\n")
      .append("                    }\n")
      .append("                    form.classList.add('was-validated');\n")
      .append("                }, false);\n")
      .append("            });\n")
      .append("        }, false);\n")
      .append("    })();\n")
      .append("</script>\n")
      .append("{% endblock %}\n");
    
    writeFile(createFile, sb.toString());
}

private void createCreateTemplate_c(String appName, String modelName, String lowerModelName, JSONArray fields) throws Exception {
    File createFile = new File(projectDirectory, "/templates/" + appName + "/" + lowerModelName + "_create.html");
    
    StringBuilder sb = new StringBuilder();
    sb.append("{% extends 'base.html' %}\n")
      .append("{% load crispy_forms_tags %}\n") // ضروري لاستخدام {% crispy form %}
      .append("{% load i18n %}\n\n")
      
      .append("{% block title %}{% trans 'إضافة جديد' %} | {{ ").append(modelName).append(".model.verbose_name }}{% endblock %}\n\n")
      
      .append("{% block content %}\n")
      .append("<div class=\"container-fluid py-4\">\n")
      
      // --- Breadcrumb & Header ---
      .append("    <div class=\"row mb-4\">\n")
      .append("        <div class=\"col-12\">\n")
      .append("            <nav aria-label=\"breadcrumb\">\n")
      .append("                <ol class=\"breadcrumb bg-light p-3 rounded shadow-sm\">\n")
      .append("                    <li class=\"breadcrumb-item\"><a href=\"{% url '").append(appName).append(":index' %}\">{% trans 'الرئيسية' %}</a></li>\n")
      .append("                    <li class=\"breadcrumb-item\"><a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_list' %}\">{{ ").append(modelName).append(".model.verbose_name_plural }}</a></li>\n")
      .append("                    <li class=\"breadcrumb-item active text-dark fw-bold\">{% trans 'إضافة جديد' %}</li>\n")
      .append("                </ol>\n")
      .append("            </nav>\n")
      .append("            <h2 class=\"fw-bold text-primary\"><i class=\"fas fa-plus-circle me-2\"></i>{% trans 'إضافة' %} {{ ").append(modelName).append(".model.verbose_name }}</h2>\n")
      .append("        </div>\n")
      .append("    </div>\n\n")

      // --- Form Card ---
      .append("    <div class=\"card border-0 shadow-lg\">\n")
      .append("        <div class=\"card-header bg-white py-3\">\n")
      .append("            <h5 class=\"card-title mb-0 text-muted italic\">{% trans 'يرجى ملء كافة الحقول المطلوبة' %}</h5>\n")
      .append("        </div>\n")
      .append("        <div class=\"card-body p-4\">\n")
      
      // نستخدم التاج المباشر هنا لأننا صممنا الـ Layout في الـ FormHelper
      // الـ enctype ضروري في حال وجود حقول ملفات أو صور
      .append("            <form method=\"post\" enctype=\"multipart/form-data\">\n")
      .append("                {% csrf_token %}\n")
      
      // السحر هنا: هذا السطر يستدعي التوزيع (Rows/Columns) الذي برمجناه في الـ Java السابق لملف forms.py
      .append("                {% crispy form %}\n\n") 
      
      // --- أزرار التحكم (خارج الـ crispy لضمان ثبات مكانها) ---
      .append("                <div class=\"row mt-4 border-top pt-4\">\n")
      .append("                    <div class=\"col-12 d-flex justify-content-end gap-2\">\n")
      .append("                        <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_list' %}\" class=\"btn btn-outline-secondary px-4\">\n")
      .append("                            <i class=\"fas fa-times me-1\"></i>{% trans 'إلغاء' %}\n")
      .append("                        </a>\n")
      .append("                        <button type=\"submit\" class=\"btn btn-primary px-5 shadow-sm\">\n")
      .append("                            <i class=\"fas fa-save me-1\"></i>{% trans 'حفظ البيانات' %}\n")
      .append("                        </button>\n")
      .append("                    </div>\n")
      .append("                </div>\n")
      .append("            </form>\n")
      .append("        </div>\n")
      .append("    </div>\n")
      .append("</div>\n")
      .append("{% endblock %}\n\n")

      // --- إضافة ميديا Select2 و Crispy Media ---
      .append("{% block extra_js %}\n")
      .append("{{ form.media.js }}\n") // ضروري جداً لتفعيل الـ Select2 والتقويمات
      .append("{{ form.media.css }}\n")
      .append("{% endblock %}\n");
    
    writeFile(createFile, sb.toString());
}


    
    private void createUpdateTemplate(String appName, String modelName, String lowerModelName, JSONArray fields) throws Exception {
    File updateFile = new File(projectDirectory, "/templates/" + appName + "/" + lowerModelName + "_update.html");
    
    StringBuilder sb = new StringBuilder();
    sb.append("{% extends 'base.html' %}\n")
      .append("{% load crispy_forms_tags %}\n")
      .append("{% load i18n %}\n\n")
      .append("{% block title %}{% trans 'تعديل' %} | {{ ").append(modelName).append(".model.verbose_name }}{% endblock %}\n\n")
      .append("{% block content %}\n")
      .append("<div class=\"container-fluid py-4\">\n")
      
      .append("    \n")
      .append("    <div class=\"row mb-4\">\n")
      .append("        <div class=\"col-12\">\n")
      .append("            <nav aria-label=\"breadcrumb\">\n")
      .append("                <ol class=\"breadcrumb bg-light p-3 rounded shadow-sm\">\n")
      .append("                    <li class=\"breadcrumb-item\"><a href=\"{% url '").append(appName).append(":index' %}\" class=\"text-decoration-none\">{% trans 'الرئيسية' %}</a></li>\n")
      .append("                    <li class=\"breadcrumb-item\"><a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_list' %}\" class=\"text-decoration-none\">{{ ").append(modelName).append(".model.verbose_name_plural }}</a></li>\n")
      .append("                    <li class=\"breadcrumb-item active text-dark fw-bold\">{% trans 'تعديل' %} {{ item }}</li>\n")
      .append("                </ol>\n")
      .append("            </nav>\n")
      .append("            <h2 class=\"fw-bold text-primary\"><i class=\"fas fa-edit me-2\"></i>{% trans 'تعديل' %} {{ item }}</h2>\n")
      .append("        </div>\n")
      .append("    </div>\n\n")

      .append("    \n")
      .append("    <div class=\"card border-0 shadow-lg rounded-4\">\n")
      .append("        <div class=\"card-header bg-white py-3 border-bottom-0\">\n")
      .append("            <h5 class=\"card-title mb-0 text-muted\">{% trans 'تحديث بيانات السجل الحالي' %}</h5>\n")
      .append("        </div>\n")
      .append("        <div class=\"card-body p-4\">\n")
      .append("            <form method=\"post\" enctype=\"multipart/form-data\" class=\"needs-validation\" novalidate>\n")
      .append("                {% csrf_token %}\n")
      
      .append("                \n")
      .append("                <div class=\"row\">\n");

    // حلقة التكرار لتوليد الحقول بشكل منفصل داخل أعمدة
    for (int i = 0; i < fields.length(); i++) {
        JSONObject field = fields.getJSONObject(i);
        String fieldName = field.getString("name");
        
        sb.append("                    <div class=\"col-md-3 mb-3\">\n")
          .append("                        {{ form.").append(fieldName).append("|as_crispy_field }}\n")
          .append("                    </div>\n");
    }

    sb.append("                </div>\n\n")

      .append("                \n")
      .append("                <div class=\"d-flex justify-content-end gap-2 mt-4 border-top pt-4\">\n")
      .append("                    <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_list' %}\" class=\"btn btn-outline-secondary px-4 shadow-sm\">\n")
      .append("                        <i class=\"fas fa-times me-1\"></i>{% trans 'إلغاء' %}\n")
      .append("                    </a>\n")
      .append("                    <button type=\"submit\" class=\"btn btn-primary px-5 shadow-sm\">\n")
      .append("                        <i class=\"fas fa-save me-1\"></i>{% trans 'حفظ التغييرات' %}\n")
      .append("                    </button>\n")
      .append("                </div>\n")
      .append("            </form>\n")
      .append("        </div>\n")
      .append("    </div>\n")
      .append("</div>\n")
      .append("{% endblock %}\n\n")

      .append("{% block extra_js %}\n")
      .append("<script>\n")
      .append("    (function() {\n")
      .append("        'use strict';\n")
      .append("        window.addEventListener('load', function() {\n")
      .append("            const forms = document.getElementsByClassName('needs-validation');\n")
      .append("            Array.prototype.filter.call(forms, function(form) {\n")
      .append("                form.addEventListener('submit', function(event) {\n")
      .append("                    if (form.checkValidity() === false) {\n")
      .append("                        event.preventDefault();\n")
      .append("                        event.stopPropagation();\n")
      .append("                    }\n")
      .append("                    form.classList.add('was-validated');\n")
      .append("                }, false);\n")
      .append("            });\n")
      .append("        }, false);\n")
      .append("    })();\n")
      .append("</script>\n")
      .append("{% endblock %}\n");
    
    writeFile(updateFile, sb.toString());
}

    private void createUpdateTemplate_c(String appName, String modelName, String lowerModelName, JSONArray fields) throws Exception {
    File updateFile = new File(projectDirectory, "/templates/" + appName + "/" + lowerModelName + "_update.html");
    
    StringBuilder sb = new StringBuilder();
    sb.append("{% extends 'base.html' %}\n")
      .append("{% load crispy_forms_tags %}\n")
      .append("{% load i18n %}\n\n")
      
      .append("{% block title %}{% trans 'تعديل' %} | {{ ").append(modelName).append(".model.verbose_name }}{% endblock %}\n\n")
      
      .append("{% block content %}\n")
      .append("<div class=\"container-fluid py-4\">\n")
      
      // --- Breadcrumb ---
      .append("    <div class=\"row mb-4\">\n")
      .append("        <div class=\"col-12\">\n")
      .append("            <nav aria-label=\"breadcrumb\">\n")
      .append("                <ol class=\"breadcrumb bg-light p-3 rounded shadow-sm\">\n")
      .append("                    <li class=\"breadcrumb-item\"><a href=\"{% url '").append(appName).append(":index' %}\" class=\"text-decoration-none\">{% trans 'الرئيسية' %}</a></li>\n")
      .append("                    <li class=\"breadcrumb-item\"><a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_list' %}\" class=\"text-decoration-none\">{{ ").append(modelName).append(".model.verbose_name_plural }}</a></li>\n")
      .append("                    <li class=\"breadcrumb-item active text-dark fw-bold\">{% trans 'تعديل' %} {{ object }}</li>\n")
      .append("                </ol>\n")
      .append("            </nav>\n")
      .append("            <h2 class=\"fw-bold text-primary\"><i class=\"fas fa-edit me-2\"></i>{% trans 'تعديل السجل' %}</h2>\n")
      .append("        </div>\n")
      .append("    </div>\n\n")

      // --- Card Container ---
      .append("    <div class=\"card border-0 shadow-lg rounded-4\">\n")
      .append("        <div class=\"card-header bg-white py-3 border-bottom-0\">\n")
      .append("            <h5 class=\"card-title mb-0 text-muted\">{% trans 'تحديث بيانات السجل الحالي' %}: <span class=\"text-dark\">{{ object }}</span></h5>\n")
      .append("        </div>\n")
      .append("        <div class=\"card-body p-4\">\n")
      
      // استخدام التاج المباشر والاعتماد على الـ Layout المرسل من البايثون
      .append("            <form method=\"post\" enctype=\"multipart/form-data\">\n")
      .append("                {% csrf_token %}\n")
      
      // هنا السحر: يتم استدعاء التصميم الديناميكي (Rows & Columns) تلقائياً
      .append("                {% crispy form %}\n\n")

      // --- أزرار التحكم ---
      .append("                <div class=\"d-flex justify-content-end gap-2 mt-4 border-top pt-4\">\n")
      .append("                    <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_list' %}\" class=\"btn btn-outline-secondary px-4 shadow-sm\">\n")
      .append("                        <i class=\"fas fa-times me-1\"></i>{% trans 'إلغاء' %}\n")
      .append("                    </a>\n")
      .append("                    <button type=\"submit\" class=\"btn btn-primary px-5 shadow-sm\">\n")
      .append("                        <i class=\"fas fa-save me-1\"></i>{% trans 'حفظ التغييرات' %}\n")
      .append("                    </button>\n")
      .append("                </div>\n")
      .append("            </form>\n")
      .append("        </div>\n")
      .append("    </div>\n")
      .append("</div>\n")
      .append("{% endblock %}\n\n")

      // --- Media Blocks (Select2, DatePickers, etc.) ---
      .append("{% block extra_js %}\n")
      .append("{{ form.media.js }}\n")
      .append("{{ form.media.css }}\n")
      .append("{% endblock %}\n");
    
    writeFile(updateFile, sb.toString());
}


    private void createDeleteTemplate(String appName, String modelName, String lowerModelName) throws Exception {
        File deleteFile = new File(projectDirectory,  "/templates/" + appName + "/" + lowerModelName + "_confirm_delete.html");
        
        StringBuilder sb = new StringBuilder();
        sb.append("{% extends 'base.html' %}\n")
          .append("{% load i18n %}\n\n")
          .append("{% block title %}{% trans 'حذف' %} {{ ").append(modelName).append(".model.verbose_name }}{% endblock %}\n\n")
          .append("{% block content %}\n")
          .append("<div class=\"container\">\n")
          .append("    <div class=\"row justify-content-center\">\n")
          .append("        <div class=\"col-md-6\">\n")
          .append("            <div class=\"card border-danger\">\n")
          .append("                <div class=\"card-header bg-danger text-white\">\n")
          .append("                    <h5 class=\"mb-0\"><i class=\"fas fa-exclamation-triangle me-2\"></i>{% trans 'تأكيد الحذف' %}</h5>\n")
          .append("                </div>\n")
          .append("                <div class=\"card-body\">\n")
          .append("                    <div class=\"text-center mb-4\">\n")
          .append("                        <i class=\"fas fa-trash-alt fa-4x text-danger mb-3\"></i>\n")
          .append("                        <h4>{% trans 'هل أنت متأكد من الحذف؟' %}</h4>\n")
          .append("                        <p class=\"text-muted\">{% trans 'سيتم حذف العنصر التالي بشكل نهائي:' %}</p>\n")
          .append("                        <div class=\"alert alert-warning\">\n")
          .append("                            <h5>{{ object }}</h5>\n")
          .append("                        </div>\n")
          .append("                        <p class=\"text-danger\">\n")
          .append("                            <i class=\"fas fa-exclamation-circle me-1\"></i>\n")
          .append("                            {% trans 'لا يمكن التراجع عن هذا الإجراء!' %}\n")
          .append("                        </p>\n")
          .append("                    </div>\n")
          .append("                    \n")
          .append("                    <form method=\"post\">\n")
          .append("                        {% csrf_token %}\n")
          .append("                        <div class=\"d-flex justify-content-between\">\n")
          .append("                            <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_list' %}\" class=\"btn btn-secondary\">\n")
          .append("                                <i class=\"fas fa-arrow-left me-1\"></i>{% trans 'رجوع' %}\n")
          .append("                            </a>\n")
          .append("                            <button type=\"submit\" class=\"btn btn-danger\">\n")
          .append("                                <i class=\"fas fa-trash me-1\"></i>{% trans 'نعم، احذف' %}\n")
          .append("                            </button>\n")
          .append("                        </div>\n")
          .append("                    </form>\n")
          .append("                </div>\n")
          .append("            </div>\n")
          .append("        </div>\n")
          .append("    </div>\n")
          .append("</div>\n")
          .append("{% endblock %}\n");
        
        writeFile(deleteFile, sb.toString());
    }
    
    
    private void createFormTemplate(String appName, String modelName, String lowerModelName) throws Exception {
        File formFile = new File(projectDirectory,  "/templates/" + appName + "/" + lowerModelName + "_form.html");
        
        StringBuilder sb = new StringBuilder();
        sb.append("{% load crispy_forms_tags %}\n")
          .append("{% load i18n %}\n\n")
          .append("<form method=\"post\" enctype=\"multipart/form-data\" class=\"needs-validation\" novalidate>\n")
          .append("    {% csrf_token %}\n")
          .append("    {{ form|crispy }}\n")
          .append("    \n")
          .append("    <div class=\"form-group mt-4\">\n")
          .append("        <div class=\"d-flex justify-content-between\">\n")
          .append("            <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_list' %}\" class=\"btn btn-secondary\">\n")
          .append("                <i class=\"fas fa-times me-1\"></i>{% trans 'إلغاء' %}\n")
          .append("            </a>\n")
          .append("            <button type=\"submit\" class=\"btn btn-primary\">\n")
          .append("                <i class=\"fas fa-save me-1\"></i>\n")
          .append("                {% if object %}{% trans 'تحديث' %}{% else %}{% trans 'حفظ' %}{% endif %}\n")
          .append("            </button>\n")
          .append("        </div>\n")
          .append("    </div>\n")
          .append("</form>\n");
        
        writeFile(formFile, sb.toString());
    }

    
    private void createFormsFile(JSONObject app) throws Exception {
    String appName = app.getString("name");
    File formsFile = new File(projectDirectory, appName + "/forms.py");
    
    StringBuilder sb = new StringBuilder();
    sb.append("from django import forms\n")
      .append("from django.utils.translation import gettext_lazy as _\n")
      //.append("from django.forms import inlineformset_factory\n")
      .append("from .models import *\n\n");
      if (projectData != null && projectData.has("apps")) {
        JSONArray apps = projectData.getJSONArray("apps");
        for (int i = 0; i < apps.length(); i++) {
            JSONObject otherApp = apps.getJSONObject(i);
            String otherAppName = otherApp.getString("name");
            if (!otherAppName.equals(appName)) {
                sb.append("from ").append(otherAppName).append(".forms import *\n");
            }
        }
    }
    
    if (app.has("models")) {
        JSONArray models = app.getJSONArray("models");
        for (int i = 0; i < models.length(); i++) {
            JSONObject model = models.getJSONObject(i);
            String modelName = model.getString("name");
            JSONArray fields = model.getJSONArray("fields"); // افترضنا وجود قائمة الحقول هنا
            
            sb.append("class ").append(modelName).append("Form(forms.ModelForm):\n")
              .append("    class Meta:\n")
              .append("        model = ").append(modelName).append("\n")
              .append("        fields = '__all__'\n")
              .append("        widgets = {\n");

            // توليد الـ Widgets بناءً على نوع الحقل أو اسمه
            for (int j = 0; j < fields.length(); j++) {
                JSONObject field = fields.getJSONObject(j);
                String fieldName = field.getString("name");
                String fieldType = field.optString("type", "").toLowerCase();

                if (fieldType.contains("date")) {
                    sb.append("            '").append(fieldName).append("': forms.DateInput(attrs={'type': 'date', 'class': 'form-control'}),\n");
                } else if (fieldType.contains("datetime")) {
                    sb.append("            '").append(fieldName).append("': forms.DateTimeInput(attrs={'type': 'datetime-local', 'class': 'form-control'}),\n");
                } else if (fieldName.equals("description")) {
                    sb.append("            '").append(fieldName).append("': forms.Textarea(attrs={'rows': 4, 'class': 'form-control'}),\n");
                }
            }

            sb.append("        }\n")
              .append("        labels = {\n")
              .append("            'name': _('الاسم'),\n")
              .append("            'description': _('الوصف'),\n")
              .append("        }\n\n");
        }
    }
    
    writeFile(formsFile, sb.toString());
   }
   //هذه الدالة تقسم حسب كرسبي فورم
   private void createFormsFile_cc(JSONObject app) throws Exception {
    String appName = app.getString("name");
    File formsFile = new File(projectDirectory, appName + "/forms.py");
    
    // عدد الحقول في الصف الواحد (يمكنك جعلها متغيرة حسب إعداداتك)
    int fieldsPerRow = 3; 

    StringBuilder sb = new StringBuilder();
    sb.append("from django import forms\n")
      .append("from django.utils.translation import gettext_lazy as _\n")
      .append("from crispy_forms.helper import FormHelper\n") // إضافة المكتبة
      .append("from crispy_forms.layout import Layout, Row, Column\n") // إضافة الـ Layout
      .append("from .models import *\n\n");

    // استيراد الفورمز من التطبيقات الأخرى
    if (projectData != null && projectData.has("apps")) {
        JSONArray apps = projectData.getJSONArray("apps");
        for (int i = 0; i < apps.length(); i++) {
            JSONObject otherApp = apps.getJSONObject(i);
            String otherAppName = otherApp.getString("name");
            if (!otherAppName.equals(appName)) {
                sb.append("from ").append(otherAppName).append(".forms import *\n");
            }
        }
    }
    
    if (app.has("models")) {
        JSONArray models = app.getJSONArray("models");
        for (int i = 0; i < models.length(); i++) {
            JSONObject model = models.getJSONObject(i);
            String modelName = model.getString("name");
            JSONArray fields = model.getJSONArray("fields");
            
            sb.append("class ").append(modelName).append("Form(forms.ModelForm):\n")
              .append("    class Meta:\n")
              .append("        model = ").append(modelName).append("\n")
              .append("        fields = '__all__'\n")
              .append("        widgets = {\n");

            // إنشاء الـ Widgets
            for (int j = 0; j < fields.length(); j++) {
                JSONObject field = fields.getJSONObject(j);
                String fieldName = field.getString("name");
                String fieldType = field.optString("type", "").toLowerCase();
                if (fieldType.contains("date")) {
                    sb.append("            '").append(fieldName).append("': forms.DateInput(attrs={'type': 'date', 'class': 'form-control'}),\n");
                } else if (fieldType.contains("datetime")) {
                    sb.append("            '").append(fieldName).append("': forms.DateTimeInput(attrs={'type': 'datetime-local', 'class': 'form-control'}),\n");
                }
            }
            sb.append("        }\n\n");

            // --- إضافة الـ FormHelper وتوزيع الحقول تلقائياً ---
            sb.append("    def __init__(self, *args, **kwargs):\n")
              .append("        super().__init__(*args, **kwargs)\n")
              .append("        self.helper = FormHelper()\n")
              .append("        self.helper.layout = Layout(\n");

            // منطق تقسيم الحقول إلى صفوف
            for (int j = 0; j < fields.length(); j += fieldsPerRow) {
                sb.append("            Row(\n");
                for (int k = 0; k < fieldsPerRow && (j + k) < fields.length(); k++) {
                    String fieldName = fields.getJSONObject(j + k).getString("name");
                    // تقسيم العرض بناءً على عدد الحقول (12 / 3 = 4)
                    int colSize = 12 / fieldsPerRow; 
                    sb.append("                Column('").append(fieldName).append("', css_class='form-group col-md-").append(colSize).append(" mb-0'),\n");
                }
                sb.append("                css_class='form-row'\n")
                  .append("            ),\n");
            }

            sb.append("        )\n\n");
        }
    }
    
    writeFile(formsFile, sb.toString());
}
private void createFormsFile_c(JSONObject app) throws Exception {
    String appName = app.getString("name");
    File formsFile = new File(projectDirectory, appName + "/forms.py");
    
    int fieldsPerRow = 4; 

    StringBuilder sb = new StringBuilder();
    sb.append("from django import forms\n")
      .append("from django.utils.translation import gettext_lazy as _\n")
      .append("from crispy_forms.helper import FormHelper\n")
      .append("from crispy_forms.layout import Layout, Row, Column\n")
      .append("from django_select2 import forms as s2forms\n")
      .append("from .models import *\n\n");

    // استيراد الموديلات من التطبيقات الأخرى لضمان عمل الـ ForeignKey
    if (projectData != null && projectData.has("apps")) {
        JSONArray apps = projectData.getJSONArray("apps");
        for (int i = 0; i < apps.length(); i++) {
            String otherAppName = apps.getJSONObject(i).getString("name");
            if (!otherAppName.equals(appName)) {
                sb.append("from ").append(otherAppName).append(".models import *\n");
            }
        }
    }
    
    if (app.has("models")) {
        JSONArray models = app.getJSONArray("models");
        for (int i = 0; i < models.length(); i++) {
            JSONObject model = models.getJSONObject(i);
            String modelName = model.getString("name");
            JSONArray fields = model.getJSONArray("fields");
            
            sb.append("class ").append(modelName).append("Form(forms.ModelForm):\n")
              .append("    class Meta:\n")
              .append("        model = ").append(modelName).append("\n")
              .append("        fields = '__all__'\n")
              .append("        widgets = {\n");

            for (int j = 0; j < fields.length(); j++) {
                JSONObject field = fields.getJSONObject(j);
                String fieldName = field.getString("name");
                String fieldType = field.optString("type", "");
                JSONObject fieldOptions = field.optJSONObject("field_options");

                // 1. معالجة حقول التاريخ
                if (fieldType.equals("DateField") || fieldType.equals("DateTimeField")) {
                    String inputType = fieldType.equals("DateField") ? "date" : "datetime-local";
                    sb.append("            '").append(fieldName).append("': forms.DateInput(attrs={'type': '").append(inputType).append("', 'class': 'form-control'}),\n");
                } 
                
                // 2. معالجة الـ ForeignKey باستخدام Django Select2
                else if (fieldType.equals("ForeignKey")) {
                    String toModelFull = fieldOptions.optString("to", ""); // مثال: "Mainapp.mymodel"
                    String targetModel = toModelFull;
                    
                    // إذا كان الاسم يحتوي على نقطة، نأخذ ما بعد النقطة (اسم الموديل)
                    if (toModelFull.contains(".")) {
                        targetModel = toModelFull.substring(toModelFull.lastIndexOf(".") + 1);
                    }

                    sb.append("            '").append(fieldName).append("': s2forms.ModelSelect2Widget(\n")
                      .append("                model=").append(targetModel).append(",\n")
                      .append("                search_fields=['name__icontains'],\n")
                      .append("                attrs={'data-placeholder': _('اختر ') + _('").append(fieldName).append("'), 'data-width': '100%'},\n")
                      .append("            ),\n");
                }
            }
            sb.append("        }\n\n");

            // --- إعداد الـ FormHelper (توزيع الحقول) ---
            sb.append("    def __init__(self, *args, **kwargs):\n")
              .append("        super().__init__(*args, **kwargs)\n")
              .append("        self.helper = FormHelper()\n")
              .append("        self.helper.form_tag = False\n")
              .append("        self.helper.layout = Layout(\n");

            for (int j = 0; j < fields.length(); j += fieldsPerRow) {
                sb.append("            Row(\n");
                for (int k = 0; k < fieldsPerRow && (j + k) < fields.length(); k++) {
                    String fName = fields.getJSONObject(j + k).getString("name");
                    int colSize = 12 / fieldsPerRow; 
                    sb.append("                Column('").append(fName).append("', css_class='form-group col-md-").append(colSize).append(" mb-0'),\n");
                }
                sb.append("                css_class='form-row'\n")
                  .append("            ),\n");
            }
            sb.append("        )\n\n");
        }
    }
    writeFile(formsFile, sb.toString());
}


    private void createAdminFile(JSONObject app) throws Exception {
        String appName = app.getString("name");
        File adminFile = new File(projectDirectory, appName + "/admin.py");
        
        StringBuilder sb = new StringBuilder();
        sb.append("from django.contrib import admin\n")
          .append("from django.utils.translation import gettext_lazy as _\n")
          .append("from .models import *\n\n");
        
        if (app.has("models")) {
            JSONArray models = app.getJSONArray("models");
            for (int i = 0; i < models.length(); i++) {
                JSONObject model = models.getJSONObject(i);
                String modelName = model.getString("name");
                
                sb.append("@admin.register(").append(modelName).append(")\n")
                  .append("class ").append(modelName).append("Admin(admin.ModelAdmin):\n")
                  .append("    list_display = [");
                
                JSONArray fields = model.getJSONArray("fields");
                for (int j = 0; j < Math.min(fields.length(), 5); j++) {
                    sb.append("'").append(fields.getJSONObject(j).getString("name")).append("'");
                    if (j < Math.min(fields.length(), 5) - 1) sb.append(", ");
                }
                sb.append("]\n")
                  .append("    list_filter = ['created_at']\n")
                  .append("    search_fields = [");
                
                boolean first = true;
                for (int j = 0; j < fields.length(); j++) {
                    String type = fields.getJSONObject(j).getString("type");
                    if (type.equals("CharField") || type.equals("TextField") || type.equals("EmailField")) {
                        if (!first) sb.append(", ");
                        sb.append("'").append(fields.getJSONObject(j).getString("name")).append("'");
                        first = false;
                    }
                }
                sb.append("]\n")
                  .append("    ordering = ['-created_at']\n")
                  .append("    date_hierarchy = 'created_at'\n\n");
            }
        }
        
        writeFile(adminFile, sb.toString());
    }
    // تحديث دالة createViewsFile لدعم CRUD الكامل
  private void createViewsFile(JSONObject app) throws Exception {
    String appName = app.getString("name");
    File viewsFile = new File(projectDirectory, appName + "/views.py");
    
    StringBuilder sb = new StringBuilder();
    sb.append("from django.shortcuts import render, get_object_or_404, redirect\n")
      .append("from django.contrib.auth.decorators import login_required, permission_required\n")
      .append("from django.utils.translation import gettext as _\n")
      .append("from django.contrib import messages\n")
      .append("from django.core.paginator import Paginator\n")
      .append("from django.db.models import Q\n")
      .append("from .models import *\n")
      .append("from .forms import *\n\n")
      .append("@login_required \n")
      .append("def index(request):\n")
      .append("    context = {\n")
      .append("        'title': _('الرئيسية'),\n")
      .append("        'project_name': '").append(projectName).append("',\n")
    //projectName يجب أن يكون متاحاً في نطاق الدالة
      .append("    }\n")
      .append("    return render(request, '").append(appName).append("/index.html', context)\n\n");
    
    if (app.has("models") && !app.isNull("models")) {
        JSONArray models = app.getJSONArray("models");
        for (int i = 0; i < models.length(); i++) {
            JSONObject modelObj = models.getJSONObject(i);
            
            // --- التحقق من خيارات القالب ---
            boolean hasTemplate = true;
            if (modelObj.has("model_options") && !modelObj.isNull("model_options")) {
                JSONObject options = modelObj.getJSONObject("model_options");
                if (options.has("template") && !options.isNull("template")) {
                    if (!options.getString("template").trim().isEmpty()) {
                        hasTemplate = true;
                    }else{
                        hasTemplate = true;
                        }
                }
            }else { hasTemplate = true;}

            // إذا لم يكن هناك قالب، ننتقل للموديل التالي فوراً
            if (!hasTemplate) continue;

            String modelName = modelObj.getString("name");
            String lowerName = modelName.toLowerCase();
            
            // List View
            sb.append("@login_required\n")
              .append("def ").append(lowerName).append("_list(request):\n")
              .append("    items = ").append(modelName).append(".objects.all()\n")
              .append("    query = request.GET.get('q', '')\n")
              .append("    if query:\n")
              .append("        items = items.filter(\n");
            
            JSONArray fields = modelObj.getJSONArray("fields");
            boolean firstField = true;
            for (int j = 0; j < fields.length(); j++) {
                JSONObject field = fields.getJSONObject(j);
                String fieldName = field.getString("name");
                String fieldType = field.getString("type");
                
                if (fieldType.equals("CharField") || fieldType.equals("TextField") || 
                    fieldType.equals("EmailField") || fieldType.equals("URLField")) {
                    
                    if (!firstField) {
                        sb.append("            | ");
                    } else {
                        sb.append("            ");
                        firstField = false;
                    }
                    sb.append("Q(").append(fieldName).append("__icontains=query)\n");
                }
            }
            
            if (firstField) {
                sb.append("            pk__isnull=False\n");
            }

            sb.append("        )\n")
              .append("    sort = request.GET.get('sort', '-created_at')\n")
              .append("    if sort:\n")
              .append("        try:\n")
              .append("            items = items.order_by(sort)\n")
              .append("        except:\n")
              .append("            items = items.order_by('-id')\n")
              .append("    paginator = Paginator(items, 10)\n")
              .append("    page_number = request.GET.get('page')\n")
              .append("    page_obj = paginator.get_page(page_number)\n")
              .append("    context = {\n")
              .append("        'title': _('قائمة ') + _('").append(modelName).append("'),\n")
              .append("        'items': page_obj,\n")
              .append("        '").append(modelName).append("': ").append(modelName).append(",\n")
              .append("    }\n")
              .append("    return render(request, '").append(appName).append("/").append(lowerName).append("_list.html', context)\n\n")
              
              .append("@login_required\n")
              .append("def ").append(lowerName).append("_detail(request, pk):\n")
              .append("    item = get_object_or_404(").append(modelName).append(", pk=pk)\n")
              .append("    context = {'item': item, 'title': _('تفاصيل ') + _('").append(modelName).append("')}\n")
              .append("    return render(request, '").append(appName).append("/").append(lowerName).append("_detail.html', context)\n\n")
              
              .append("@login_required\n")
              .append("@permission_required('").append(appName).append(".add_").append(lowerName).append("', raise_exception=True)\n")
              .append("def ").append(lowerName).append("_create(request):\n")
              .append("    if request.method == 'POST':\n")
              .append("        form = ").append(modelName).append("Form(request.POST, request.FILES)\n")
              .append("        if form.is_valid():\n")
              .append("            item = form.save()\n")
              .append("            messages.success(request, _('تم إضافة البيانات بنجاح'))\n")
              .append("            return redirect('").append(appName).append(":").append(lowerName).append("_list')\n")
              .append("    else: form = ").append(modelName).append("Form()\n")
              .append("    return render(request, '").append(appName).append("/").append(lowerName).append("_create.html', {'form': form, 'title': _('إضافة')})\n\n")
              
              .append("@login_required\n")
              .append("@permission_required('").append(appName).append(".change_").append(lowerName).append("', raise_exception=True)\n")
              .append("def ").append(lowerName).append("_update(request, pk):\n")
              .append("    item = get_object_or_404(").append(modelName).append(", pk=pk)\n")
              .append("    if request.method == 'POST':\n")
              .append("        form = ").append(modelName).append("Form(request.POST, request.FILES, instance=item)\n")
              .append("        if form.is_valid():\n")
              .append("            form.save()\n")
              .append("            messages.success(request, _('تم التحديث بنجاح'))\n")
              .append("            return redirect('").append(appName).append(":").append(lowerName).append("_list')\n")
              .append("    else: form = ").append(modelName).append("Form(instance=item)\n")
              .append("    return render(request, '").append(appName).append("/").append(lowerName).append("_update.html', {'form': form, 'item': item, 'title': _('تعديل')})\n\n")
              
              .append("@login_required\n")
              .append("@permission_required('").append(appName).append(".delete_").append(lowerName).append("', raise_exception=True)\n")
              .append("def ").append(lowerName).append("_delete(request, pk):\n")
              .append("    item = get_object_or_404(").append(modelName).append(", pk=pk)\n")
              .append("    if request.method == 'POST':\n")
              .append("        item.delete()\n")
              .append("        messages.success(request, _('تم الحذف بنجاح'))\n")
              .append("        return redirect('").append(appName).append(":").append(lowerName).append("_list')\n")
              .append("    return render(request, '").append(appName).append("/").append(lowerName).append("_confirm_delete.html', {'item': item})\n\n");
        }
    }
    
    writeFile(viewsFile, sb.toString());
}

private void createViewsFile_c(JSONObject app) throws Exception {
    String appName = app.getString("name");
    File viewsFile = new File(projectDirectory, appName + "/views.py");
    
    StringBuilder sb = new StringBuilder();
    sb.append("from django.shortcuts import render, get_object_or_404, redirect\n")
      .append("from django.contrib.auth.decorators import login_required, permission_required\n")
      .append("from django.utils.translation import gettext as _\n")
      .append("from django.contrib import messages\n")
      .append("from django.core.paginator import Paginator\n")
      .append("from django.db.models import Q\n")
      .append("from .models import *\n")
      .append("from .forms import *\n\n")
      
      // الصفحة الرئيسية للتطبيق
      .append("def index(request):\n")
      .append("    context = {\n")
      .append("        'title': _('الرئيسية'),\n")
      .append("        'project_name': '").append(projectName).append("',\n")
      .append("    }\n")
      .append("    return render(request, '").append(appName).append("/index.html', context)\n\n");
    
    if (app.has("models") && !app.isNull("models")) {
        JSONArray models = app.getJSONArray("models");
        for (int i = 0; i < models.length(); i++) {
            JSONObject modelObj = models.getJSONObject(i);
            String modelName = modelObj.getString("name");
            String lowerName = modelName.toLowerCase();
            JSONArray fields = modelObj.getJSONArray("fields");

            // 1. List View (قائمة البيانات مع البحث والترتيب)
            sb.append("@login_required\n")
              .append("def ").append(lowerName).append("_list(request):\n")
              .append("    items = ").append(modelName).append(".objects.all()\n")
              .append("    query = request.GET.get('q', '')\n")
              .append("    if query:\n")
              .append("        items = items.filter(\n");
            
            boolean firstField = true;
            for (int j = 0; j < fields.length(); j++) {
                JSONObject field = fields.getJSONObject(j);
                String fieldName = field.getString("name");
                String fieldType = field.optString("type", "");
                
                // تحسين البحث ليشمل الحقول النصية فقط
                if (fieldType.contains("Char") || fieldType.contains("Text") || fieldType.contains("Email")) {
                    if (!firstField) sb.append("            | ");
                    else { sb.append("            "); firstField = false; }
                    sb.append("Q(").append(fieldName).append("__icontains=query)\n");
                }
            }
            if (firstField) sb.append("            pk__isnull=False\n"); // حالة احتياطية إذا لم توجد حقول نصية

            sb.append("        )\n")
              .append("    sort = request.GET.get('sort', '-id')\n")
              .append("    try:\n")
              .append("        items = items.order_by(sort)\n")
              .append("    except:\n")
              .append("        items = items.order_by('-id')\n\n")
              .append("    paginator = Paginator(items, 10)\n")
              .append("    page_obj = paginator.get_page(request.GET.get('page'))\n")
              .append("    context = {\n")
              .append("        'title': _('قائمة ') + _('").append(modelName).append("'),\n")
              .append("        'items': page_obj,\n")
              .append("        '").append(modelName).append("': ").append(modelName).append(",\n") // لتسهيل الوصول للميتا داتا
              .append("    }\n")
              .append("    return render(request, '").append(appName).append("/").append(lowerName).append("_list.html', context)\n\n")
              
              // 2. Detail View (تفاصيل السجل)
              .append("@login_required\n")
              .append("def ").append(lowerName).append("_detail(request, pk):\n")
              .append("    item = get_object_or_404(").append(modelName).append(", pk=pk)\n")
              .append("    context = {'item': item, 'object': item, 'title': _('تفاصيل ') + _('").append(modelName).append("')}\n")
              .append("    return render(request, '").append(appName).append("/").append(lowerName).append("_detail.html', context)\n\n")
              
              // 3. Create View (إضافة جديد)
              .append("@login_required\n")
              .append("@permission_required('").append(appName).append(".add_").append(lowerName).append("', raise_exception=True)\n")
              .append("def ").append(lowerName).append("_create(request):\n")
              .append("    if request.method == 'POST':\n")
              .append("        form = ").append(modelName).append("Form(request.POST, request.FILES)\n")
              .append("        if form.is_valid():\n")
              .append("            form.save()\n")
              .append("            messages.success(request, _('تم إضافة البيانات بنجاح'))\n")
              .append("            return redirect('").append(appName).append(":").append(lowerName).append("_list')\n")
              .append("    else:\n")
              .append("        form = ").append(modelName).append("Form()\n")
              .append("    context = {'form': form, '").append(modelName).append("': ").append(modelName).append(", 'title': _('إضافة جديدة')}\n")
              .append("    return render(request, '").append(appName).append("/").append(lowerName).append("_create.html', context)\n\n")
              
              // 4. Update View (تعديل سجل)
              .append("@login_required\n")
              .append("@permission_required('").append(appName).append(".change_").append(lowerName).append("', raise_exception=True)\n")
              .append("def ").append(lowerName).append("_update(request, pk):\n")
              .append("    item = get_object_or_404(").append(modelName).append(", pk=pk)\n")
              .append("    if request.method == 'POST':\n")
              .append("        form = ").append(modelName).append("Form(request.POST, request.FILES, instance=item)\n")
              .append("        if form.is_valid():\n")
              .append("            form.save()\n")
              .append("            messages.success(request, _('تم التحديث بنجاح'))\n")
              .append("            return redirect('").append(appName).append(":").append(lowerName).append("_list')\n")
              .append("    else:\n")
              .append("        form = ").append(modelName).append("Form(instance=item)\n")
              .append("    context = {\n")
              .append("        'form': form, \n")
              .append("        'item': item, \n")
              .append("        'object': item, \n") // مهم جداً للقوالب الموحدة
              .append("        '").append(modelName).append("': ").append(modelName).append(", \n")
              .append("        'title': _('تعديل')\n")
              .append("    }\n")
              .append("    return render(request, '").append(appName).append("/").append(lowerName).append("_update.html', context)\n\n")
              
              // 5. Delete View (حذف سجل)
              .append("@login_required\n")
              .append("@permission_required('").append(appName).append(".delete_").append(lowerName).append("', raise_exception=True)\n")
              .append("def ").append(lowerName).append("_delete(request, pk):\n")
              .append("    item = get_object_or_404(").append(modelName).append(", pk=pk)\n")
              .append("    if request.method == 'POST':\n")
              .append("        item.delete()\n")
              .append("        messages.success(request, _('تم الحذف بنجاح'))\n")
              .append("        return redirect('").append(appName).append(":").append(lowerName).append("_list')\n")
              .append("    return render(request, '").append(appName).append("/").append(lowerName).append("_confirm_delete.html', {'item': item})\n\n");
        }
    }
    
    writeFile(viewsFile, sb.toString());
}


    
    // تحديث دالة createUrlsFileForApp لدعم جميع عمليات CRUD
    private void createUrlsFileForApp(JSONObject app) throws Exception {
    String appName = app.getString("name");
    File urlsFile = new File(projectDirectory, appName + "/urls.py");
    
    StringBuilder sb = new StringBuilder();
    sb.append("from django.urls import path\n")
      .append("from . import views\n\n")
      .append("app_name = '").append(appName).append("'\n\n")
      .append("urlpatterns = [\n")
      .append("    path('', views.index, name='index'),\n");
    
    if (app.has("models") && !app.isNull("models")) {
        JSONArray models = app.getJSONArray("models");
        for (int i = 0; i < models.length(); i++) {
            JSONObject model = models.getJSONObject(i);
            
            // التحقق من وجود model_options و template قبل إضافة الروابط
            boolean shouldCreateUrls = true;
            /*
            if (model.has("model_options") && !model.isNull("model_options")) {
                JSONObject options = model.getJSONObject("model_options");
                if (options.has("template") && !options.isNull("template")) {
                    if (!options.getString("template").trim().isEmpty()) {
                        shouldCreateUrls =  false;
                    }else {
                        shouldCreateUrls = true;
                        }
                }
            }else { shouldCreateUrls = true;}*/

            // إذا تحقق الشرط، يتم إنشاء الروابط لهذا الموديل
            if (shouldCreateUrls) {
                String mName = model.getString("name").toLowerCase();
                sb.append("    path('").append(mName).append("/', views.").append(mName).append("_list, name='").append(mName).append("_list'),\n")
                  .append("    path('").append(mName).append("/create/', views.").append(mName).append("_create, name='").append(mName).append("_create'),\n")
                  .append("    path('").append(mName).append("/<int:pk>/', views.").append(mName).append("_detail, name='").append(mName).append("_detail'),\n")
                  .append("    path('").append(mName).append("/<int:pk>/update/', views.").append(mName).append("_update, name='").append(mName).append("_update'),\n")
                  .append("    path('").append(mName).append("/<int:pk>/delete/', views.").append(mName).append("_delete, name='").append(mName).append("_delete'),\n");
            }
        }
    }
    
    sb.append("]\n");
    writeFile(urlsFile, sb.toString());
}

    
    
    
    private void createSerializersFile(JSONObject app) throws Exception {
        String appName = app.getString("name");
        File serializersFile = new File(projectDirectory, appName + "/serializers.py");
        
        StringBuilder sb = new StringBuilder();
        sb.append("from rest_framework import serializers\n")
          .append("from .models import *\n\n");
        
        if (app.has("models")) {
            JSONArray models = app.getJSONArray("models");
            for (int i = 0; i < models.length(); i++) {
                String modelName = models.getJSONObject(i).getString("name");
                
                sb.append("class ").append(modelName).append("Serializer(serializers.ModelSerializer):\n")
                  .append("    class Meta:\n")
                  .append("        model = ").append(modelName).append("\n")
                  .append("        fields = '__all__'\n")
                  .append("        read_only_fields = ('created_at', 'updated_at')\n\n");
            }
        }
        
        writeFile(serializersFile, sb.toString());
    }
     
    private void createAuthenticationApp() throws Exception {
        // إنشاء مجلد تطبيق المصادقة
        File authAppDir = new File(projectDirectory, "accounts");
        authAppDir.mkdir();
        copyapp("accounts", projectDirectory.toString());
     }
    
    
    private void createAdminConfig() throws Exception {
        File adminConfig = new File(projectDirectory, projectName + "/admin_config.py");
        writeFile(adminConfig, "from django.contrib import admin\n");
    }
    
    private void createRESTAPI() throws Exception {
       // File apiUrls = new File(projectDirectory, projectName + "/api_urls.py");
      //  writeFile(apiUrls, "from django.urls import path\nurlpatterns = []\n");
    }
    
    
    private void writeFile(File file, String content) throws Exception {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
    
    private File createZipArchive() throws Exception {
        /*
        File zipFile = new File(projectDirectory, projectName + ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            addToZip(projectDirectory, "", zos);
        }
        return zipFile;
        */
        return new File(projectDirectory, projectName);

    }
    
    private void addToZip(File file, String parentPath, ZipOutputStream zos) throws Exception {
        String zipPath = parentPath + file.getName();
        if (file.isDirectory()) {
            zipPath += "/";
            if (!zipPath.equals(projectName + "/")) {
                zos.putNextEntry(new ZipEntry(zipPath));
                zos.closeEntry();
            }
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) addToZip(child, zipPath, zos);
            }
        } else {
            zos.putNextEntry(new ZipEntry(zipPath));
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) zos.write(buffer, 0, length);
            }
            zos.closeEntry();
        }
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    
    private void createIndexTemplate() throws Exception {
    File indexFile = new File(projectDirectory, "templates/index.html");
    
    StringBuilder sb = new StringBuilder();
    sb.append("{% extends 'base.html' %}\n")
    .append("{% load i18n %}\n")
    .append("{% load static %}\n\n")
    .append("{% block title %}{{ project_name }}{% endblock %}\n\n")
    .append("{% block content %}\n")
    .append("<div class=\"container py-5 text-center\">\n")
    .append("    \n")
    .append("    <div class=\"jumbotron p-5 mb-4 bg-light rounded-3 shadow-sm\">\n")
    .append("        <h1 class=\"display-4 fw-bold\">{% trans 'مرحباً بك في' %} {{ project_name }}</h1>\n")
    .append("        <p class=\"lead text-muted\">{% trans 'اختر أحد التطبيقات للبدء بالعمل' %}</p>\n")
    .append("    </div>\n\n")
    .append("    \n")
    .append("    <div class=\"row justify-content-center\">\n");

    // إضافة تطبيق الحسابات (Accounts) إذا كان مفعلاً
    if (projectData.has("authentication") && projectData.getBoolean("authentication")) {
        sb.append("        <div class=\"col-md-4 mb-4\">\n")
        .append("            <div class=\"card h-100 shadow-sm border-0\">\n")
        .append("                <div class=\"card-body\">\n")
        .append("                    <h5 class=\"card-title text-primary fw-bold\">{% trans 'إدارة الحسابات' %}</h5>\n")
        .append("                    <p class=\"card-text\">{% trans 'تسجيل الدخول وإدارة الملف الشخصي' %}</p>\n")
        .append("                    <a href=\"{% url 'accounts:profile' %}\" class=\"btn btn-outline-primary\">{% trans 'دخول' %}</a>\n")
        .append("                </div>\n")
        .append("            </div>\n")
        .append("        </div>\n");
    }

    // إضافة بقية التطبيقات من المصفوفة
    if (projectData.has("apps")) {
        JSONArray apps = projectData.getJSONArray("apps");
        for (int i = 0; i < apps.length(); i++) {
            JSONObject app = apps.getJSONObject(i);
            String appName = app.getString("name");
            
            sb.append("        <div class=\"col-md-4 mb-4\">\n")
            .append("            <div class=\"card h-100 shadow-sm border-0\">\n")
            .append("                <div class=\"card-body\">\n")
            .append("                    <h5 class=\"card-title fw-bold\">").append(appName).append("</h5>\n")
            .append("                    <p class=\"card-text text-muted\">{% trans 'تطبيق' %} ").append(appName).append("</p>\n")
            .append("                    <a href=\"{% url '").append(appName).append(":index' %}\" class=\"btn btn-primary\">")
            .append("{% trans 'فتح التطبيق' %}</a>\n")
            .append("                </div>\n")
            .append("            </div>\n")
            .append("        </div>\n");
        }
    }

    sb.append("    </div>\n")
    .append("</div>\n")
    .append("{% endblock %}\n\n")
    .append("{% block extra_css %}\n")
    .append("<style>\n")
    .append("    .card { transition: transform 0.2s; }\n")
    .append("    .card:hover { transform: translateY(-5px); }\n")
    .append("</style>\n")
    .append("{% endblock %}\n");
    
    writeFile(indexFile, sb.toString());
}

    // دالة مساعدة لإرجاع أيقونة مناسبة للتطبيق
    private String getAppIcon(String appName) {
        Map<String, String> iconMap = new HashMap<String, String>() {{
            put("blog", "blog");
            put("news", "newspaper");
            put("shop", "shopping-cart");
            put("products", "box");
            put("orders", "receipt");
            put("users", "users");
            put("customers", "user-friends");
            put("employees", "user-tie");
            put("inventory", "warehouse");
            put("sales", "chart-line");
            put("reports", "chart-bar");
            put("settings", "cog");
            put("content", "file-alt");
            put("media", "images");
            put("pages", "file");
            put("categories", "tags");
            put("tags", "tag");
            put("comments", "comments");
            put("messages", "envelope");
            put("notifications", "bell");
            put("tasks", "tasks");
            put("projects", "project-diagram");
            put("invoices", "file-invoice");
            put("payments", "credit-card");
        }};
        
        for (Map.Entry<String, String> entry : iconMap.entrySet()) {
            if (appName.toLowerCase().contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return "cube"; // أيقونة افتراضية
}

private void createAppIndexTemplate(JSONObject app) throws Exception {
    String appName = app.getString("name");
    File appIndexFile = new File(projectDirectory, "/templates/" + appName + "/index.html");
    
    StringBuilder sb = new StringBuilder();
    sb.append("{% extends 'base.html' %}\n")
      .append("{% load i18n %}\n")
      .append("{% load static %}\n\n")
      .append("{% block title %}{{ app_name }} - {{ project_name }}{% endblock %}\n\n")
      .append("{% block content %}\n")
      .append("<div class=\"container\">\n")
      .append("    <!-- App Header -->\n")
      .append("    <div class=\"app-header py-4 mb-4 border-bottom\">\n")
      .append("        <div class=\"row align-items-center\">\n")
      .append("            <div class=\"col-md-8\">\n")
      .append("                <nav aria-label=\"breadcrumb\">\n")
      .append("                    <ol class=\"breadcrumb\">\n")
      .append("                        <li class=\"breadcrumb-item\">\n")
      .append("                            <a href=\"/\"><i class=\"fas fa-home me-1\"></i>{% trans 'الرئيسية' %}</a>\n")
      .append("                        </li>\n")
      .append("                        <li class=\"breadcrumb-item active\" aria-current=\"page\">\n")
      .append("                            <i class=\"fas fa-").append(getAppIcon(appName)).append(" me-1\"></i>{{ app_name }}\n")
      .append("                        </li>\n")
      .append("                    </ol>\n")
      .append("                </nav>\n")
      .append("                <h1 class=\"mb-0\"><i class=\"fas fa-").append(getAppIcon(appName)).append(" me-2\"></i>{{ app_name }}</h1>\n")
      .append("                <p class=\"text-muted mb-0\">").append(app.optString("description", "تطبيق " + appName)).append("</p>\n")
      .append("            </div>\n")
      .append("            <div class=\"col-md-4 text-end\">\n")
      .append("                <div class=\"app-actions\">\n")
      .append("                    {% if user.is_authenticated %}\n")
      .append("                    <button class=\"btn btn-outline-primary\" id=\"toggleSidebar\">\n")
      .append("                        <i class=\"fas fa-bars\"></i>\n")
      .append("                    </button>\n")
      .append("                    {% endif %}\n")
      .append("                </div>\n")
      .append("            </div>\n")
      .append("        </div>\n")
      .append("    </div>\n\n")
      .append("    <div class=\"row\">\n")
      .append("        <!-- Sidebar -->\n")
      .append("        <div class=\"col-md-3 mb-4\" id=\"appSidebar\">\n")
      .append("            <div class=\"card\">\n")
      .append("                <div class=\"card-header bg-light\">\n")
      .append("                    <h5 class=\"mb-0\"><i class=\"fas fa-th me-2\"></i>{% trans 'القائمة' %}</h5>\n")
      .append("                </div>\n")
      .append("                <div class=\"card-body p-0\">\n")
      .append("                    <div class=\"list-group list-group-flush\">\n")
      .append("                        <a href=\"{% url '").append(appName).append(":index' %}\" class=\"list-group-item list-group-item-action active\">\n")
      .append("                            <i class=\"fas fa-home me-2\"></i>{% trans 'الصفحة الرئيسية' %}\n")
      .append("                        </a>\n");
    
    // روابط النماذج في القائمة الجانبية
    if (app.has("models")) {
        JSONArray models = app.getJSONArray("models");
        for (int i = 0; i < models.length(); i++) {
            JSONObject model = models.getJSONObject(i);
            String modelName = model.getString("name");
            String lowerModelName = modelName.toLowerCase();
            
            sb.append("                        <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_list' %}\" class=\"list-group-item list-group-item-action\">\n")
              .append("                            <i class=\"fas fa-table me-2\"></i>").append(modelName).append("\n")
              .append("                            <span class=\"badge bg-primary float-end\">").append(model.getJSONArray("fields").length()).append("</span>\n")
              .append("                        </a>\n");
        }
    }
    
    sb.append("                        <a href=\"/admin/").append(appName).append("/\" class=\"list-group-item list-group-item-action\" target=\"_blank\">\n")
      .append("                            <i class=\"fas fa-cog me-2\"></i>{% trans 'إدارة التطبيق' %}\n")
      .append("                            <span class=\"badge bg-warning float-end\"><i class=\"fas fa-external-link-alt\"></i></span>\n")
      .append("                        </a>\n")
      .append("                    </div>\n")
      .append("                </div>\n")
      .append("                <div class=\"card-footer\">\n")
      .append("                    <small class=\"text-muted\">\n")
      .append("                        <i class=\"fas fa-info-circle me-1\"></i>\n")
      .append("                        {% trans 'آخر تحديث:' %} {% now 'Y-m-d' %}\n")
      .append("                    </small>\n")
      .append("                </div>\n")
      .append("            </div>\n\n")
      .append("            <!-- Quick Stats -->\n")
      .append("            <div class=\"card mt-3\">\n")
      .append("                <div class=\"card-header bg-light\">\n")
      .append("                    <h6 class=\"mb-0\"><i class=\"fas fa-chart-bar me-2\"></i>{% trans 'إحصائيات سريعة' %}</h6>\n")
      .append("                </div>\n")
      .append("                <div class=\"card-body\">\n");
    
    if (app.has("models")) {
        JSONArray models = app.getJSONArray("models");
        for (int i = 0; i < Math.min(models.length(), 3); i++) { // عرض 3 نماذج فقط
            JSONObject model = models.getJSONObject(i);
            String modelName = model.getString("name");
            String lowerModelName = modelName.toLowerCase();
            
            sb.append("                    <div class=\"d-flex justify-content-between align-items-center mb-2\">\n")
              .append("                        <span class=\"small\">").append(modelName).append("</span>\n")
              .append("                        <span class=\"badge bg-info\">{{ ").append(lowerModelName).append("_count|default:'0' }}</span>\n")
              .append("                    </div>\n");
        }
    }
    
    sb.append("                </div>\n")
      .append("            </div>\n")
      .append("        </div>\n\n")
      .append("        <!-- Main Content -->\n")
      .append("        <div class=\"col-md-9\">\n")
      .append("            <!-- Welcome Card -->\n")
      .append("            <div class=\"card mb-4\">\n")
      .append("                <div class=\"card-body text-center\">\n")
      .append("                    <i class=\"fas fa-").append(getAppIcon(appName)).append(" fa-4x text-primary mb-3\"></i>\n")
      .append("                    <h3 class=\"card-title\">{% trans 'مرحبًا بك في ' %}{{ app_name }}</h3>\n")
      .append("                    <p class=\"card-text\">").append(app.optString("description", "تطبيق متكامل لإدارة المحتوى")).append("</p>\n")
      .append("                    <div class=\"mt-4\">\n")
      .append("                        <a href=\"#models\" class=\"btn btn-primary me-2\">\n")
      .append("                            <i class=\"fas fa-database me-1\"></i>{% trans 'عرض النماذج' %}\n")
      .append("                        </a>\n")
      .append("                        <a href=\"#actions\" class=\"btn btn-outline-secondary\">\n")
      .append("                            <i class=\"fas fa-bolt me-1\"></i>{% trans 'الإجراءات السريعة' %}\n")
      .append("                        </a>\n")
      .append("                    </div>\n")
      .append("                </div>\n")
      .append("            </div>\n\n")
      .append("            <!-- Models Overview -->\n")
      .append("            <div class=\"card mb-4\" id=\"models\">\n")
      .append("                <div class=\"card-header bg-light\">\n")
      .append("                    <h4 class=\"mb-0\"><i class=\"fas fa-database me-2\"></i>{% trans 'النماذج المتاحة' %}</h4>\n")
      .append("                </div>\n")
      .append("                <div class=\"card-body\">\n");
    
    if (app.has("models") && app.getJSONArray("models").length() > 0) {
        JSONArray models = app.getJSONArray("models");
        sb.append("                    <div class=\"row\">\n");
        
        for (int i = 0; i < models.length(); i++) {
            JSONObject model = models.getJSONObject(i);
            String modelName = model.getString("name");
            String lowerModelName = modelName.toLowerCase();
            String modelDescription = model.optString("description", "نموذج " + modelName);
            JSONArray fields = model.getJSONArray("fields");
            boolean showDefaultCard = true;
            if (model.has("model_options") && !model.isNull("model_options")) {
                JSONObject options = model.getJSONObject("model_options");
                if (options.has("template") && !options.isNull("template") && !options.getString("template").trim().isEmpty()) {
                    showDefaultCard = false;
                }
            }

            if (showDefaultCard) {
                sb.append("                        <div class=\"col-md-").append(getColumnSize(models.length())).append(" mb-3\">\n")
                  .append("                            <div class=\"card h-100\">\n")
                  .append("                                <div class=\"card-body\">\n")
                  .append("                                    <div class=\"d-flex align-items-center mb-3\">\n")
                  .append("                                        <div class=\"bg-primary bg-opacity-10 p-2 rounded me-3\">\n")
                  .append("                                            <i class=\"fas fa-table text-primary fa-lg\"></i>\n")
                  .append("                                        </div>\n")
                  .append("                                        <div>\n")
                  .append("                                            <h5 class=\"card-title mb-0\">").append(modelName).append("</h5>\n")
                  .append("                                            <small class=\"text-muted\">").append(fields.length()).append(" {% trans 'حقول' %}</small>\n")
                  .append("                                        </div>\n")
                  .append("                                    </div>\n")
                  .append("                                    <p class=\"card-text small\">").append(modelDescription).append("</p>\n")
                  .append("                                    <div class=\"row g-1\">\n")
                  .append("                                        <div class=\"col-6\">\n")
                  .append("                                            <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_list' %}\" class=\"btn btn-outline-primary btn-sm w-100\">\n")
                  .append("                                                <i class=\"fas fa-list me-1\"></i>{% trans 'القائمة' %}\n")
                  .append("                                            </a>\n")
                  .append("                                        </div>\n")
                  .append("                                        <div class=\"col-6\">\n")
                  .append("                                            <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_create' %}\" class=\"btn btn-outline-success btn-sm w-100\">\n")
                  .append("                                                <i class=\"fas fa-plus me-1\"></i>{% trans 'إضافة' %}\n")
                  .append("                                            </a>\n")
                  .append("                                        </div>\n")
                  .append("                                    </div>\n")
                  .append("                                </div>\n")
                  .append("                            </div>\n")
                  .append("                        </div>\n");
            }
        }
        sb.append("                    </div>\n");
    } else {
        sb.append("                    <div class=\"alert alert-info text-center\">\n")
          .append("                        <i class=\"fas fa-info-circle fa-2x mb-3\"></i>\n")
          .append("                        <h5>{% trans 'لا توجد نماذج' %}</h5>\n")
          .append("                        <p>{% trans 'لم يتم إضافة أي نماذج لهذا التطبيق بعد' %}</p>\n")
          .append("                    </div>\n");
    }
    
    sb.append("                </div>\n")
      .append("            </div>\n\n")
      .append("            <!-- Quick Actions -->\n")
      .append("            <div class=\"card mb-4\" id=\"actions\">\n")
      .append("                <div class=\"card-header bg-light\">\n")
      .append("                    <h4 class=\"mb-0\"><i class=\"fas fa-bolt me-2\"></i>{% trans 'الإجراءات السريعة' %}</h4>\n")
      .append("                </div>\n")
      .append("                <div class=\"card-body\">\n")
      .append("                    <div class=\"row\">\n");
    
    if (app.has("models")) {
        JSONArray models = app.getJSONArray("models");
        for (int i = 0; i < Math.min(models.length(), 6); i++) { // عرض 6 نماذج كحد أقصى
            JSONObject model = models.getJSONObject(i);
            String modelName = model.getString("name");
            String lowerModelName = modelName.toLowerCase();
            
            sb.append("                        <div class=\"col-md-4 mb-3\">\n")
              .append("                            <div class=\"card border-0 shadow-sm\">\n")
              .append("                                <div class=\"card-body text-center\">\n")
              .append("                                    <i class=\"fas fa-plus-circle fa-2x text-success mb-2\"></i>\n")
              .append("                                    <h6 class=\"card-title\">{% trans 'إضافة ' %}").append(modelName).append("</h6>\n")
              .append("                                    <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_create' %}\" class=\"stretched-link\"></a>\n")
              .append("                                </div>\n")
              .append("                            </div>\n")
              .append("                        </div>\n");
        }
    }
    
    sb.append("                    </div>\n")
      .append("                </div>\n")
      .append("            </div>\n\n")
      .append("            <!-- Recent Items -->\n")
      .append("            <div class=\"card\">\n")
      .append("                <div class=\"card-header bg-light\">\n")
      .append("                    <h4 class=\"mb-0\"><i class=\"fas fa-history me-2\"></i>{% trans 'آخر الإضافات' %}</h4>\n")
      .append("                </div>\n")
      .append("                <div class=\"card-body\">\n")
      .append("                    <div class=\"table-responsive\">\n")
      .append("                        <table class=\"table table-hover\">\n")
      .append("                            <thead>\n")
      .append("                                <tr>\n")
      .append("                                    <th>{% trans 'النموذج' %}</th>\n")
      .append("                                    <th>{% trans 'آخر إضافة' %}</th>\n")
      .append("                                    <th>{% trans 'عدد العناصر' %}</th>\n")
      .append("                                    <th>{% trans 'الإجراءات' %}</th>\n")
      .append("                                </tr>\n")
      .append("                            </thead>\n")
      .append("                            <tbody>\n");
    
    if (app.has("models")) {
        JSONArray models = app.getJSONArray("models");
        for (int i = 0; i < Math.min(models.length(), 5); i++) { // عرض 5 نماذج فقط
            JSONObject model = models.getJSONObject(i);
            String modelName = model.getString("name");
            String lowerModelName = modelName.toLowerCase();
            
            sb.append("                                <tr>\n")
              .append("                                    <td>\n")
              .append("                                        <i class=\"fas fa-table me-2\"></i>\n")
              .append("                                        <strong>").append(modelName).append("</strong>\n")
              .append("                                    </td>\n")
              .append("                                    <td>\n")
              .append("                                        <span class=\"text-muted\">---</span>\n")
              .append("                                    </td>\n")
              .append("                                    <td>\n")
              .append("                                        <span class=\"badge bg-secondary\">{{ ").append(lowerModelName).append("_count|default:'0' }}</span>\n")
              .append("                                    </td>\n")
              .append("                                    <td>\n")
              .append("                                        <div class=\"btn-group btn-group-sm\">\n")
              .append("                                            <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_list' %}\" class=\"btn btn-outline-primary\">\n")
              .append("                                                <i class=\"fas fa-eye\"></i>\n")
              .append("                                            </a>\n")
              .append("                                            <a href=\"{% url '").append(appName).append(":").append(lowerModelName).append("_create' %}\" class=\"btn btn-outline-success\">\n")
              .append("                                                <i class=\"fas fa-plus\"></i>\n")
              .append("                                            </a>\n")
              .append("                                        </div>\n")
              .append("                                    </td>\n")
              .append("                                </tr>\n");
        }
    } else {
        sb.append("                                <tr>\n")
          .append("                                    <td colspan=\"4\" class=\"text-center text-muted py-4\">\n")
          .append("                                        <i class=\"fas fa-inbox fa-2x mb-2\"></i>\n")
          .append("                                        <p>{% trans 'لا توجد بيانات بعد' %}</p>\n")
          .append("                                    </td>\n")
          .append("                                </tr>\n");
    }
    
    sb.append("                            </tbody>\n")
      .append("                        </table>\n")
      .append("                    </div>\n")
      .append("                </div>\n")
      .append("            </div>\n")
      .append("        </div>\n")
      .append("    </div>\n")
      .append("</div>\n")
      .append("{% endblock %}\n\n")
      .append("{% block extra_css %}\n")
      .append("<style>\n")
      .append("    .app-header {\n")
      .append("        background-color: #f8f9fa;\n")
      .append("    }\n")
      .append("    .list-group-item.active {\n")
      .append("        background-color: #0d6efd;\n")
      .append("        border-color: #0d6efd;\n")
      .append("    }\n")
      .append("    .card {\n")
      .append("        transition: all 0.3s ease;\n")
      .append("    }\n")
      .append("    .card:hover {\n")
      .append("        box-shadow: 0 5px 15px rgba(0,0,0,0.1);\n")
      .append("    }\n")
      .append("    .stretched-link::after {\n")
      .append("        position: absolute;\n")
      .append("        top: 0;\n")
      .append("        right: 0;\n")
      .append("        bottom: 0;\n")
      .append("        left: 0;\n")
      .append("        z-index: 1;\n")
      .append("        content: \"\";\n")
      .append("    }\n")
      .append("</style>\n")
      .append("{% endblock %}\n\n")
      .append("{% block extra_js %}\n")
      .append("<script>\n")
      .append("    // تبديل القائمة الجانبية في الشاشات الصغيرة\n")
      .append("    document.addEventListener('DOMContentLoaded', function() {\n")
      .append("        const toggleBtn = document.getElementById('toggleSidebar');\n")
      .append("        const sidebar = document.getElementById('appSidebar');\n")
      .append("        \n")
      .append("        if (toggleBtn && sidebar) {\n")
      .append("            toggleBtn.addEventListener('click', function() {\n")
      .append("                sidebar.classList.toggle('d-none');\n")
      .append("                toggleBtn.innerHTML = sidebar.classList.contains('d-none') ? \n")
      .append("                    '<i class=\"fas fa-bars\"></i>' : \n")
      .append("                    '<i class=\"fas fa-times\"></i>';\n")
      .append("            });\n")
      .append("            \n")
      .append("            // إخفاء القائمة الجانبية في الشاشات الصغيرة\n")
      .append("            if (window.innerWidth < 768) {\n")
      .append("                sidebar.classList.add('d-none');\n")
      .append("            }\n")
      .append("        }\n")
      .append("        \n")
      .append("        // تحديث الإحصائيات\n")
      .append("        updateStats();\n")
      .append("        \n")
      .append("        // تأثيرات الرسوم المتحركة\n")
      .append("        const cards = document.querySelectorAll('.card');\n")
      .append("        cards.forEach((card, index) => {\n")
      .append("            card.style.animationDelay = (index * 0.1) + 's';\n")
      .append("            card.classList.add('animate__animated', 'animate__fadeIn');\n")
      .append("        });\n")
      .append("    });\n")
      .append("    \n")
      .append("    // دالة لتحديث الإحصائيات\n")
      .append("    function updateStats() {\n")
      .append("        // يمكن إضافة AJAX هنا لجلب الإحصائيات الحقيقية\n")
      .append("        console.log('Updating app statistics...');\n")
      .append("    }\n")
      .append("    \n")
      .append("    // البحث في الجدول\n")
      .append("    function searchTable() {\n")
      .append("        const input = document.getElementById('searchTable');\n")
      .append("        const filter = input.value.toUpperCase();\n")
      .append("        const table = document.getElementById('recentTable');\n")
      .append("        const rows = table.getElementsByTagName('tr');\n")
      .append("        \n")
      .append("        for (let i = 0; i < rows.length; i++) {\n")
      .append("            const cells = rows[i].getElementsByTagName('td');\n")
      .append("            let match = false;\n")
      .append("            \n")
      .append("            for (let j = 0; j < cells.length; j++) {\n")
      .append("                const cell = cells[j];\n")
      .append("                if (cell) {\n")
      .append("                    if (cell.textContent.toUpperCase().indexOf(filter) > -1) {\n")
      .append("                        match = true;\n")
      .append("                        break;\n")
      .append("                    }\n")
      .append("                }\n")
      .append("            }\n")
      .append("            \n")
      .append("            rows[i].style.display = match ? '' : 'none';\n")
      .append("        }\n")
      .append("    }\n")
      .append("</script>\n")
      .append("{% endblock %}\n");
    
    writeFile(appIndexFile, sb.toString());
}

// دالة مساعدة لتحديد حجم الأعمدة بناءً على عدد النماذج
private String getColumnSize(int modelCount) {
    if (modelCount >= 4) return "3";
    else if (modelCount == 3) return "4";
    else if (modelCount == 2) return "6";
    else return "12";
}

// دالة مساعدة لإرجاع أيقونة مناسبة للتطبيق

  private void copyapp(String app_name, String project){
    new Thread(() -> {
    // اسم المجلد في assets
        String folderToCopy = app_name; 
        String targetPath = project + "/" + folderToCopy;
        Assets.copyAssetsFolder(context, folderToCopy, targetPath);
    
    }).start();

    
    
    
  }
  private void copyfile(String a_p, String t_p){
      
      
    new Thread(() -> {
    // اسم المجلد في assets
        String folderToCopy = a_p; 
        String targetPath = projectDirectory.toString() + "/" + t_p;
        Assets.copyFile(context, folderToCopy, targetPath);
    
    }).start();

    
    
    
  
      
      }

}