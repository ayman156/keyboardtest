package ayman.djangogenerator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.*;
//import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.net.Uri;
import java.nio.channels.FileChannel;
import java.io.FileInputStream;
import java.io.FileOutputStream;


public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "django_builder.db";
    private static final int DATABASE_VERSION = 2;
    
    // جداول المشاريع
    private static final String TABLE_PROJECTS = "projects";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PROJECT_NAME = "project_name";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_UPDATED_AT = "updated_at";
    private static final String COLUMN_SETTINGS = "settings";
    
    // جداول التطبيقات
    private static final String TABLE_APPS = "apps";
    private static final String COLUMN_PROJECT_ID = "project_id";
    private static final String COLUMN_APP_NAME = "app_name";
    private static final String COLUMN_VERBOSE_NAME = "verbose_name";
    private static final String COLUMN_APP_ORDER = "app_order";
    
    // جداول النماذج
    private static final String TABLE_MODELS = "models";
    private static final String COLUMN_APP_ID = "app_id";
    private static final String COLUMN_MODEL_NAME = "model_name";
    private static final String COLUMN_MODEL_OPTIONS = "model_options";
    
    // جداول الحقول
    private static final String TABLE_FIELDS = "fields";
    private static final String COLUMN_MODEL_ID = "model_id";
    private static final String COLUMN_FIELD_NAME = "field_name";
    private static final String COLUMN_FIELD_TYPE = "field_type";
    private static final String COLUMN_FIELD_OPTIONS = "field_options";
    private static final String COLUMN_FIELD_ORDER = "field_order";
    // في DatabaseHelper.java، أضف هذا الثابت في الأعلى مع الثوابت الأخرى:
    public static final String COLUMN_REPORT_NAME = "report_name";


    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // إنشاء جدول المشاريع
        String CREATE_PROJECTS_TABLE = "CREATE TABLE " + TABLE_PROJECTS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PROJECT_NAME + " TEXT UNIQUE NOT NULL,"
                + COLUMN_DESCRIPTION + " TEXT,"
                + COLUMN_SETTINGS + " TEXT,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_PROJECTS_TABLE);
        
        // إنشاء جدول التطبيقات
        String CREATE_APPS_TABLE = "CREATE TABLE " + TABLE_APPS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PROJECT_ID + " INTEGER NOT NULL,"
                + COLUMN_APP_NAME + " TEXT NOT NULL,"
                + COLUMN_VERBOSE_NAME + " TEXT,"
                + COLUMN_APP_ORDER + " INTEGER DEFAULT 0,"
                + "FOREIGN KEY(" + COLUMN_PROJECT_ID + ") REFERENCES " 
                + TABLE_PROJECTS + "(" + COLUMN_ID + ") ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_APPS_TABLE);
        
        // إنشاء جدول النماذج
        String CREATE_MODELS_TABLE = "CREATE TABLE " + TABLE_MODELS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_APP_ID + " INTEGER NOT NULL,"
                + COLUMN_MODEL_NAME + " TEXT NOT NULL,"
                + COLUMN_MODEL_OPTIONS + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_APP_ID + ") REFERENCES " 
                + TABLE_APPS + "(" + COLUMN_ID + ") ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_MODELS_TABLE);
        
        // إنشاء جدول الحقول
        String CREATE_FIELDS_TABLE = "CREATE TABLE " + TABLE_FIELDS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MODEL_ID + " INTEGER NOT NULL,"
                + COLUMN_FIELD_NAME + " TEXT NOT NULL,"
                + COLUMN_FIELD_TYPE + " TEXT NOT NULL,"
                + COLUMN_FIELD_OPTIONS + " TEXT,"
                + COLUMN_FIELD_ORDER + " INTEGER DEFAULT 0,"
                + "FOREIGN KEY(" + COLUMN_MODEL_ID + ") REFERENCES " 
                + TABLE_MODELS + "(" + COLUMN_ID + ") ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_FIELDS_TABLE);
        
        
        
        // في DatabaseHelper.java - في onCreate() بعد إنشاء جدول الحقول
String CREATE_FORMSETS_TABLE = "CREATE TABLE IF NOT EXISTS formsets (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "parent_model_id INTEGER NOT NULL," + // الجدول الرئيسي
        "child_model_id INTEGER NOT NULL," +  // الجدول الفرعي
        "relationship_name TEXT," +           // اسم العلاقة (اختياري)
        "extra_fields INTEGER DEFAULT 1," +   // عدد الحقول الإضافية
        "can_delete BOOLEAN DEFAULT 1," +     // هل يسمح بالحذف؟
        "max_num INTEGER DEFAULT 10," +       // أقصى عدد
        "prefix TEXT," +                      // البادئة
        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
        "FOREIGN KEY(parent_model_id) REFERENCES " + TABLE_MODELS + "(id) ON DELETE CASCADE," +
        "FOREIGN KEY(child_model_id) REFERENCES " + TABLE_MODELS + "(id) ON DELETE CASCADE" +
        ")";
db.execSQL(CREATE_FORMSETS_TABLE);

db.execSQL("CREATE TABLE IF NOT EXISTS reports (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "project_id INTEGER," +
            "name TEXT," +
            "description TEXT," +
            "model_id INTEGER," +
            "model_name TEXT," +
            "config_json TEXT," +
            "django_query TEXT," +
            "created_at INTEGER" +
            ")");
        
        db.execSQL("CREATE TABLE IF NOT EXISTS templates (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "project_id INTEGER," +
            "name TEXT," +
            "description TEXT," +
            "html_content TEXT," +
            "css_content TEXT," +
            "js_content TEXT," +
            "is_system INTEGER DEFAULT 0" +
            ")");
        
        // إضافة فهارس لتحسين الأداء
        db.execSQL("CREATE INDEX idx_projects_name ON " + TABLE_PROJECTS + "(" + COLUMN_PROJECT_NAME + ")");
        db.execSQL("CREATE INDEX idx_apps_project ON " + TABLE_APPS + "(" + COLUMN_PROJECT_ID + ")");
        db.execSQL("CREATE INDEX idx_models_app ON " + TABLE_MODELS + "(" + COLUMN_APP_ID + ")");
        db.execSQL("CREATE INDEX idx_fields_model ON " + TABLE_FIELDS + "(" + COLUMN_MODEL_ID + ")");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FIELDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODELS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PROJECTS);
        db.execSQL("DROP TABLE IF EXISTS reports");
        db.execSQL("DROP TABLE IF EXISTS templates");
        onCreate(db);
    }
    
    
    public String backupDatabase(Context context) {
    try {
        // المسار الحالي لقاعدة البيانات
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        
        // تحديد مسار النسخة الاحتياطية (في مجلد Downloads)
        File exportDir = new File(FileUtil.getExternalStorageDir(), "django_projects/DjangoGen_Backups");
        if (!exportDir.exists()) exportDir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File backupFile = new File(exportDir, "backup_" + timeStamp + ".db");

        FileChannel src = new FileInputStream(dbFile).getChannel();
        FileChannel dst = new FileOutputStream(backupFile).getChannel();
        dst.transferFrom(src, 0, src.size());
        src.close();
        dst.close();

        return "تم الحفظ في: " + backupFile.getAbsolutePath();
    } catch (Exception e) {
        e.printStackTrace();
        return "فشل النسخ الاحتياطي: " + e.getMessage();
    }
}

// لجلب الموديلات ككائنات تحتوي ID و Name
public List<ModelObj> getModelsObjectsByProject(long projectId) {
    List<ModelObj> list = new ArrayList<>();
    String query = "SELECT m." + COLUMN_ID + ", m." + COLUMN_MODEL_NAME + 
                   " FROM " + TABLE_MODELS + " m" +
                   " JOIN " + TABLE_APPS + " a ON m." + COLUMN_APP_ID + " = a." + COLUMN_ID +
                   " WHERE a." + COLUMN_PROJECT_ID + " = ?";
    Cursor c = getReadableDatabase().rawQuery(query, new String[]{String.valueOf(projectId)});
    while (c.moveToNext()) {
        list.add(new ModelObj(c.getLong(0), c.getString(1)));
    }
    c.close();
    return list;
}



public List<FormsetConfig> getAllFormsetsForProject(long projectId) {
    List<FormsetConfig> list = new ArrayList<>();
    // استعلام يربط الـ Formsets بالموديلات ثم بالتطبيقات ثم بالمشروع
    String query = "SELECT f.* FROM formsets f " +
                   "JOIN models m ON f.parent_model_id = m.id " +
                   "JOIN apps a ON m.app_id = a.id " +
                   "WHERE a.project_id = ?";
                   
    Cursor cursor = getReadableDatabase().rawQuery(query, new String[]{String.valueOf(projectId)});
    
    if (cursor.moveToFirst()) {
        do {
            FormsetConfig formset = new FormsetConfig();
            formset.setId(cursor.getLong(cursor.getColumnIndex("id")));
            formset.setParentModelId(cursor.getLong(cursor.getColumnIndex("parent_model_id")));
            formset.setChildModelId(cursor.getLong(cursor.getColumnIndex("child_model_id")));
            formset.setRelationshipName(cursor.getString(cursor.getColumnIndex("relationship_name")));
            // ... أكمل باقي الحقول هنا ...
            list.add(formset);
        } while (cursor.moveToNext());
    }
    cursor.close();
    return list;
}


// لجلب العلاقات فقط
// في getRelationsByModelId
public List<Field> getRelationsByModelId(long modelId) {
    List<Field> fields = new ArrayList<>();
    String query = "SELECT * FROM " + TABLE_FIELDS + " WHERE " + COLUMN_MODEL_ID + 
                   " = ? AND " + COLUMN_FIELD_TYPE + " IN ('ForeignKey', 'ManyToManyField', 'OneToOneField')";
    Cursor c = getReadableDatabase().rawQuery(query, new String[]{String.valueOf(modelId)});
    while (c.moveToNext()) {
        Field f = new Field(); // هذا من Field.java المستقل
        f.setName(c.getString(c.getColumnIndex(COLUMN_FIELD_NAME)));
        f.setType(c.getString(c.getColumnIndex(COLUMN_FIELD_TYPE)));
        fields.add(f);
    }
    c.close();
    return fields;
}

// في getFieldsByModelId
public List<Field> getFieldsByModelId(long modelId) {
    List<Field> fields = new ArrayList<>();
    SQLiteDatabase db = this.getReadableDatabase();
    
    Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FIELDS + 
                               " WHERE " + COLUMN_MODEL_ID + " = ?", 
                               new String[]{String.valueOf(modelId)});
    
    while (cursor.moveToNext()) {
        Field field = new Field(); // هذا من Field.java المستقل
        field.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
        field.setName(cursor.getString(cursor.getColumnIndex(COLUMN_FIELD_NAME)));
        field.setType(cursor.getString(cursor.getColumnIndex(COLUMN_FIELD_TYPE)));
        fields.add(field);
    }
    
    cursor.close();
    db.close();
    return fields;
}

// إضافة هذه الطرق إلى DatabaseHelper.java

    
    // دالة حفظ التقرير
    public long saveReport(Report report) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put("project_id", report.getProjectId());
        values.put("name", report.getName());
        values.put("description", report.getDescription());
        values.put("model_id", report.getModelId());
        values.put("model_name", report.getModelName());
        values.put("config_json", report.getConfigJson());
        values.put("django_query", report.getDjangoQuery());
        values.put("created_at", report.getCreatedAt());
        
        long id = db.insert("reports", null, values);
        db.close();
        return id;
    }
    
    // دالة الحصول على التقارير حسب المشروع
    public List<Report> getReportsByProject(long projectId) {
        List<Report> reports = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query("reports",
            new String[]{"id", "name", "description", "model_name", "created_at", "config_json"},
            "project_id = ?",
            new String[]{String.valueOf(projectId)},
            null, null, "created_at DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Report report = new Report();
                report.setId(cursor.getLong(0));
                report.setName(cursor.getString(1));
                report.setDescription(cursor.getString(2));
                report.setModelName(cursor.getString(3));
                report.setCreatedAt(cursor.getLong(4));
                report.setConfigJson(cursor.getString(5));
                reports.add(report);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return reports;
    }
    
    // دالة الحصول على التقرير بواسطة ID
    public Report getReportById(long reportId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("reports",
            null, "id = ?", new String[]{String.valueOf(reportId)},
            null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            Report report = new Report();
            report.setId(cursor.getLong(0));
            report.setProjectId(cursor.getLong(1));
            report.setName(cursor.getString(2));
            report.setDescription(cursor.getString(3));
            report.setModelId(cursor.getLong(4));
            report.setModelName(cursor.getString(5));
            report.setConfigJson(cursor.getString(6));
            report.setDjangoQuery(cursor.getString(7));
            report.setCreatedAt(cursor.getLong(8));
            cursor.close();
            db.close();
            return report;
        }
        db.close();
        return null;
    }
    
    // دالة حذف التقرير
    public boolean deleteReport(long reportId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("reports", "id = ?", 
            new String[]{String.valueOf(reportId)});
        db.close();
        return result > 0;
    }
    
    // دالة الحصول على القوالب
    // في DatabaseHelper.java
public List<Template> getTemplates(long projectId) {
    List<Template> templates = new ArrayList<>();
    SQLiteDatabase db = this.getReadableDatabase();
    
    Cursor cursor = db.query("templates",
        new String[]{"id", "name", "description", "html_content", "css_content", "js_content", "is_system", "project_id"},
        "project_id = ?",
        new String[]{String.valueOf(projectId)},
        null, null, null);
    
    if (cursor != null && cursor.moveToFirst()) {
        do {
            Template template = new Template();
            template.setId(cursor.getLong(0));
            template.setName(cursor.getString(1));
            template.setDescription(cursor.getString(2));
            template.setHtmlContent(cursor.getString(3));
            template.setCssContent(cursor.getString(4));
            template.setJsContent(cursor.getString(5));
            template.setSystem(cursor.getInt(6) == 1);
            template.setProjectId(cursor.getLong(7));
            templates.add(template);
        } while (cursor.moveToNext());
        cursor.close();
    }
    db.close();
    return templates;
}

public Template getTemplateById(long templateId) {
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.query("templates",
        new String[]{"id", "name", "description", "html_content", "css_content", "js_content", "is_system", "project_id"},
        "id = ?", new String[]{String.valueOf(templateId)},
        null, null, null);
    
    if (cursor != null && cursor.moveToFirst()) {
        Template template = new Template();
        template.setId(cursor.getLong(0));
        template.setName(cursor.getString(1));
        template.setDescription(cursor.getString(2));
        template.setHtmlContent(cursor.getString(3));
        template.setCssContent(cursor.getString(4));
        template.setJsContent(cursor.getString(5));
        template.setSystem(cursor.getInt(6) == 1);
        template.setProjectId(cursor.getLong(7));
        cursor.close();
        db.close();
        return template;
    }
    db.close();
    return null;
}

public long saveTemplate(Template template) {
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues values = new ContentValues();
    
    values.put("name", template.getName());
    values.put("description", template.getDescription());
    values.put("html_content", template.getHtmlContent());
    values.put("css_content", template.getCssContent());
    values.put("js_content", template.getJsContent());
    values.put("is_system", template.isSystem() ? 1 : 0);
    values.put("project_id", template.getProjectId());
    
    long id = db.insert("templates", null, values);
    db.close();
    return id;
}






public boolean restoreDatabase(Context context, Uri backupUri) {
    try {
        // مسار قاعدة البيانات الحالية
        File dbFile = context.getDatabasePath(DATABASE_NAME);
        
        // إغلاق قاعدة البيانات الحالية قبل الاستبدال
        this.close();

        InputStream is = context.getContentResolver().openInputStream(backupUri);
        OutputStream os = new FileOutputStream(dbFile);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }

        os.flush();
        os.close();
        is.close();
        
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

    
    // ============ عمليات المشاريع ============
    
    public long addProject(Project project) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROJECT_NAME, project.getName());
        values.put(COLUMN_DESCRIPTION, project.getDescription());
        values.put(COLUMN_SETTINGS, project.getSettings().toString());
        
        long id = db.insert(TABLE_PROJECTS, null, values);
        db.close();
        return id;
    }
    
    public List<Project> getAllProjects() {
        List<Project> projectList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PROJECTS + " ORDER BY " + COLUMN_CREATED_AT + " DESC";
        
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                Project project = new Project();
                project.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                project.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_NAME)));
                project.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                try {
                    project.setSettings(new JSONObject(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETTINGS))));
                } catch (JSONException e) {
                    project.setSettings(new JSONObject());
                }
                project.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
                project.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)));
                projectList.add(project);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return projectList;
    }
    
    public Project getProject(long projectId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PROJECTS,
                new String[]{COLUMN_ID, COLUMN_PROJECT_NAME, COLUMN_DESCRIPTION, COLUMN_SETTINGS, COLUMN_CREATED_AT, COLUMN_UPDATED_AT},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(projectId)}, null, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            Project project = new Project();
            project.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            project.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_NAME)));
            project.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
            try {
                project.setSettings(new JSONObject(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETTINGS))));
            } catch (JSONException e) {
                project.setSettings(new JSONObject());
            }
            project.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
            project.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)));
            cursor.close();
            db.close();
            return project;
        }
        if (cursor != null) cursor.close();
        db.close();
        return null;
    }
    //نسخ احتياطي لمشروع او 
    public boolean importProjectFromJson(String jsonString) {
    SQLiteDatabase db = this.getWritableDatabase();
    db.beginTransaction();
    try {
        JSONObject backupJson = new JSONObject(jsonString);

        // إدراج المشروع
        ContentValues projectValues = new ContentValues();
        projectValues.put(COLUMN_PROJECT_NAME, backupJson.getString("project_name") + "_copy"); // إضافة كلمة copy لتجنب تكرار الاسم الفريد
        projectValues.put(COLUMN_DESCRIPTION, backupJson.getString("description"));
        projectValues.put(COLUMN_SETTINGS, backupJson.getString("settings"));
        long newProjectId = db.insert(TABLE_PROJECTS, null, projectValues);

        // إدراج التطبيقات
        JSONArray appsArray = backupJson.getJSONArray("apps");
        for (int i = 0; i < appsArray.length(); i++) {
            JSONObject appJson = appsArray.getJSONObject(i);
            ContentValues appValues = new ContentValues();
            appValues.put(COLUMN_PROJECT_ID, newProjectId);
            appValues.put(COLUMN_APP_NAME, appJson.getString("app_name"));
            appValues.put(COLUMN_VERBOSE_NAME, appJson.getString("verbose_name"));
            long newAppId = db.insert(TABLE_APPS, null, appValues);

            // إدراج النماذج
            JSONArray modelsArray = appJson.getJSONArray("models");
            for (int j = 0; j < modelsArray.length(); j++) {
                JSONObject modelJson = modelsArray.getJSONObject(j);
                ContentValues modelValues = new ContentValues();
                modelValues.put(COLUMN_APP_ID, newAppId);
                modelValues.put(COLUMN_MODEL_NAME, modelJson.getString("model_name"));
                modelValues.put(COLUMN_MODEL_OPTIONS, modelJson.getString("options"));
                long newModelId = db.insert(TABLE_MODELS, null, modelValues);

                // إدراج الحقول
                JSONArray fieldsArray = modelJson.getJSONArray("fields");
                for (int k = 0; k < fieldsArray.length(); k++) {
                    JSONObject fieldJson = fieldsArray.getJSONObject(k);
                    ContentValues fieldValues = new ContentValues();
                    fieldValues.put(COLUMN_MODEL_ID, newModelId);
                    fieldValues.put(COLUMN_FIELD_NAME, fieldJson.getString("name"));
                    fieldValues.put(COLUMN_FIELD_TYPE, fieldJson.getString("type"));
                    fieldValues.put(COLUMN_FIELD_OPTIONS, fieldJson.getString("options"));
                    db.insert(TABLE_FIELDS, null, fieldValues);
                }
            }
        }
        db.setTransactionSuccessful();
        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    } finally {
        db.endTransaction();
    }
}


public String exportProjectToJson(long projectId) {
    try {
        JSONObject backupJson = new JSONObject();
        SQLiteDatabase db = this.getReadableDatabase();

        // 1. جلب بيانات المشروع
        Cursor projectCursor = db.query(TABLE_PROJECTS, null, COLUMN_ID + "=?", new String[]{String.valueOf(projectId)}, null, null, null);
        if (projectCursor.moveToFirst()) {
            backupJson.put("project_name", projectCursor.getString(projectCursor.getColumnIndex(COLUMN_PROJECT_NAME)));
            backupJson.put("description", projectCursor.getString(projectCursor.getColumnIndex(COLUMN_DESCRIPTION)));
            backupJson.put("settings", projectCursor.getString(projectCursor.getColumnIndex(COLUMN_SETTINGS)));
        }
        projectCursor.close();

        // 2. جلب التطبيقات
        JSONArray appsArray = new JSONArray();
        Cursor appsCursor = db.query(TABLE_APPS, null, COLUMN_PROJECT_ID + "=?", new String[]{String.valueOf(projectId)}, null, null, null);
        while (appsCursor.moveToNext()) {
            JSONObject appJson = new JSONObject();
            long appId = appsCursor.getLong(appsCursor.getColumnIndex(COLUMN_ID));
            appJson.put("app_name", appsCursor.getString(appsCursor.getColumnIndex(COLUMN_APP_NAME)));
            appJson.put("verbose_name", appsCursor.getString(appsCursor.getColumnIndex(COLUMN_VERBOSE_NAME)));

            // 3. جلب النماذج لكل تطبيق
            JSONArray modelsArray = new JSONArray();
            Cursor modelsCursor = db.query(TABLE_MODELS, null, COLUMN_APP_ID + "=?", new String[]{String.valueOf(appId)}, null, null, null);
            while (modelsCursor.moveToNext()) {
                JSONObject modelJson = new JSONObject();
                long modelId = modelsCursor.getLong(modelsCursor.getColumnIndex(COLUMN_ID));
                modelJson.put("model_name", modelsCursor.getString(modelsCursor.getColumnIndex(COLUMN_MODEL_NAME)));
                modelJson.put("options", modelsCursor.getString(modelsCursor.getColumnIndex(COLUMN_MODEL_OPTIONS)));

                // 4. جلب الحقول لكل نموذج
                JSONArray fieldsArray = new JSONArray();
                Cursor fieldsCursor = db.query(TABLE_FIELDS, null, COLUMN_MODEL_ID + "=?", new String[]{String.valueOf(modelId)}, null, null, null);
                while (fieldsCursor.moveToNext()) {
                    JSONObject fieldJson = new JSONObject();
                    fieldJson.put("name", fieldsCursor.getString(fieldsCursor.getColumnIndex(COLUMN_FIELD_NAME)));
                    fieldJson.put("type", fieldsCursor.getString(fieldsCursor.getColumnIndex(COLUMN_FIELD_TYPE)));
                    fieldJson.put("options", fieldsCursor.getString(fieldsCursor.getColumnIndex(COLUMN_FIELD_OPTIONS)));
                    fieldsArray.put(fieldJson);
                }
                fieldsCursor.close();
                modelJson.put("fields", fieldsArray);
                modelsArray.put(modelJson);
            }
            modelsCursor.close();
            appJson.put("models", modelsArray);
            appsArray.put(appJson);
        }
        appsCursor.close();
        backupJson.put("apps", appsArray);

        return backupJson.toString(4); // 4 مسافات للتنسيق الجميل
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}

    
    public int updateProject(Project project) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROJECT_NAME, project.getName());
        values.put(COLUMN_DESCRIPTION, project.getDescription());
        values.put(COLUMN_SETTINGS, project.getSettings().toString());
        values.put(COLUMN_UPDATED_AT, "CURRENT_TIMESTAMP");
        
        int rows = db.update(TABLE_PROJECTS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(project.getId())});
        db.close();
        return rows;
    }
    
    public void deleteProject(long projectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PROJECTS, COLUMN_ID + " = ?", new String[]{String.valueOf(projectId)});
        db.close();
    }
    
    // ============ عمليات التطبيقات ============
    
    public long addApp(App app) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROJECT_ID, app.getProjectId());
        values.put(COLUMN_APP_NAME, app.getName());
        values.put(COLUMN_VERBOSE_NAME, app.getVerboseName());
        values.put(COLUMN_APP_ORDER, app.getOrder());
        
        long id = db.insert(TABLE_APPS, null, values);
        db.close();
        return id;
    }
    
    public List<App> getAppsByProject(long projectId) {
        List<App> appList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_APPS + " WHERE " + COLUMN_PROJECT_ID + " = " + projectId 
                + " ORDER BY " + COLUMN_APP_ORDER;
        
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                App app = new App();
                app.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                app.setProjectId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PROJECT_ID)));
                app.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APP_NAME)));
                app.setVerboseName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VERBOSE_NAME)));
                app.setOrder(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_APP_ORDER)));
                appList.add(app);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return appList;
    }
    
    public int updateApp(App app) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_APP_NAME, app.getName());
        values.put(COLUMN_VERBOSE_NAME, app.getVerboseName());
        values.put(COLUMN_APP_ORDER, app.getOrder());
        
        int rows = db.update(TABLE_APPS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(app.getId())});
        db.close();
        return rows;
    }
    
    public void deleteApp(long appId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APPS, COLUMN_ID + " = ?", new String[]{String.valueOf(appId)});
        db.close();
    }
    
    // ============ عمليات النماذج ============
    
    public long addModel(DjangoModel model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_APP_ID, model.getAppId());
        values.put(COLUMN_MODEL_NAME, model.getName());
        values.put(COLUMN_MODEL_OPTIONS, model.getOptions().toString());
        
        long id = db.insert(TABLE_MODELS, null, values);
        db.close();
        return id;
    }
    
    public List<DjangoModel> getModelsByApp(long appId) {
        List<DjangoModel> modelList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MODELS + " WHERE " + COLUMN_APP_ID + " = " + appId;
        
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                DjangoModel model = new DjangoModel();
                model.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                model.setAppId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_APP_ID)));
                model.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MODEL_NAME)));
                
                try {
                    model.setOptions(new JSONObject(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MODEL_OPTIONS))));
                } catch (JSONException e) {
                    model.setOptions(new JSONObject());
                }
                modelList.add(model);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return modelList;
    }
    
    public int updateModel(DjangoModel model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MODEL_NAME, model.getName());
        values.put(COLUMN_MODEL_OPTIONS, model.getOptions().toString());
        
        int rows = db.update(TABLE_MODELS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(model.getId())});
        db.close();
        return rows;
    }
    
    public void deleteModel(long modelId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MODELS, COLUMN_ID + " = ?", new String[]{String.valueOf(modelId)});
        db.close();
    }
    
    // ============ عمليات الحقول ============
    
    public long addField(Field field) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MODEL_ID, field.getModelId());
        values.put(COLUMN_FIELD_NAME, field.getName());
        values.put(COLUMN_FIELD_TYPE, field.getType());
        values.put(COLUMN_FIELD_OPTIONS, field.getOptions().toString());
        values.put(COLUMN_FIELD_ORDER, field.getOrder());
        
        long id = db.insert(TABLE_FIELDS, null, values);
        db.close();
        return id;
    }
    public List<String> getModelsByProjectId(long projectId) {
    List<String> modelList = new ArrayList<>();
    // استعلام يربط الجداول الثلاثة للوصول من المشروع إلى النماذج
    String query = "SELECT a." + COLUMN_APP_NAME + ", m." + COLUMN_MODEL_NAME +
                   " FROM " + TABLE_MODELS + " m" +
                   " JOIN " + TABLE_APPS + " a ON m." + COLUMN_APP_ID + " = a." + COLUMN_ID +
                   " WHERE a." + COLUMN_PROJECT_ID + " = ?";
                   
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(projectId)});

    if (cursor.moveToFirst()) {
        do {
            String appName = cursor.getString(0);
            String modelName = cursor.getString(1);
            // Django format: app_label.ModelName
            modelList.add(appName + "." + modelName);
        } while (cursor.moveToNext());
    }
    cursor.close();
    return modelList;
}

public long getProjectIdByModelId(long modelId) {
    String query = "SELECT a." + COLUMN_PROJECT_ID + 
                   " FROM " + TABLE_APPS + " a" +
                   " JOIN " + TABLE_MODELS + " m ON m." + COLUMN_APP_ID + " = a." + COLUMN_ID +
                   " WHERE m." + COLUMN_ID + " = ?";
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(modelId)});
    long projectId = -1;
    if (cursor.moveToFirst()) {
        projectId = cursor.getLong(0);
    }
    cursor.close();
    return projectId;
}

    
    public List<Field> getFieldsByModel(long modelId) {
        List<Field> fieldList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_FIELDS + " WHERE " + COLUMN_MODEL_ID + " = " + modelId 
                + " ORDER BY " + COLUMN_FIELD_ORDER;
        
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                Field field = new Field();
                field.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                field.setModelId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_MODEL_ID)));
                field.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIELD_NAME)));
                field.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIELD_TYPE)));
                try {
                    field.setOptions(new JSONObject(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIELD_OPTIONS))));
                } catch (JSONException e) {
                    field.setOptions(new JSONObject());
                }
                field.setOrder(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FIELD_ORDER)));
                fieldList.add(field);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return fieldList;
    }
    
    //==== from set ===
    
    // في DatabaseHelper.java
// ============ عمليات Formsets ============

public long addFormset(FormsetConfig formset) {
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues values = new ContentValues();
    
    values.put("parent_model_id", formset.getParentModelId());
    values.put("child_model_id", formset.getChildModelId());
    values.put("relationship_name", formset.getRelationshipName());
    values.put("extra_fields", formset.getExtraFields());
    values.put("can_delete", formset.isCanDelete() ? 1 : 0);
    values.put("max_num", formset.getMaxNum());
    values.put("prefix", formset.getPrefix());
    
    long id = db.insert("formsets", null, values);
    db.close();
    return id;
}

public List<FormsetConfig> getFormsetsByParentModel(long parentModelId) {
    List<FormsetConfig> formsets = new ArrayList<>();
    
    String query = "SELECT f.*, " +
                   "pm." + COLUMN_MODEL_NAME + " as parent_name, " +
                   "cm." + COLUMN_MODEL_NAME + " as child_name, " +
                   "pa." + COLUMN_APP_NAME + " as parent_app, " +
                   "ca." + COLUMN_APP_NAME + " as child_app " +
                   "FROM formsets f " +
                   "JOIN " + TABLE_MODELS + " pm ON f.parent_model_id = pm.id " +
                   "JOIN " + TABLE_MODELS + " cm ON f.child_model_id = cm.id " +
                   "JOIN " + TABLE_APPS + " pa ON pm." + COLUMN_APP_ID + " = pa.id " +
                   "JOIN " + TABLE_APPS + " ca ON cm." + COLUMN_APP_ID + " = ca.id " +
                   "WHERE f.parent_model_id = ? " +
                   "ORDER BY f.created_at DESC";
    
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(parentModelId)});
    
    // في getFormsetsByParentModel() - نعدل الجزء الذي فيه set()
while (cursor.moveToNext()) {
    FormsetConfig formset = new FormsetConfig();
    formset.setId(cursor.getLong(cursor.getColumnIndex("id")));
    formset.setParentModelId(cursor.getLong(cursor.getColumnIndex("parent_model_id")));
    formset.setChildModelId(cursor.getLong(cursor.getColumnIndex("child_model_id")));
    formset.setRelationshipName(cursor.getString(cursor.getColumnIndex("relationship_name")));
    formset.setExtraFields(cursor.getInt(cursor.getColumnIndex("extra_fields")));
    formset.setCanDelete(cursor.getInt(cursor.getColumnIndex("can_delete")) == 1);
    formset.setMaxNum(cursor.getInt(cursor.getColumnIndex("max_num")));
    formset.setPrefix(cursor.getString(cursor.getColumnIndex("prefix")));
    formset.setCreatedAt(cursor.getString(cursor.getColumnIndex("created_at")));
    
    // استخدام setAdditionalInfo بدلاً من set
    formset.setAdditionalInfo("parent_model_name", 
        cursor.getString(cursor.getColumnIndex("parent_name")));
    formset.setAdditionalInfo("child_model_name", 
        cursor.getString(cursor.getColumnIndex("child_name")));
    formset.setAdditionalInfo("parent_app_name", 
        cursor.getString(cursor.getColumnIndex("parent_app")));
    formset.setAdditionalInfo("child_app_name", 
        cursor.getString(cursor.getColumnIndex("child_app")));
    
    formsets.add(formset);
}
    
    cursor.close();
    db.close();
    return formsets;
}

public FormsetConfig getFormsetById(long formsetId) {
    SQLiteDatabase db = this.getReadableDatabase();
    
    String query = "SELECT * FROM formsets WHERE id = ?";
    Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(formsetId)});
    
    if (cursor.moveToFirst()) {
        FormsetConfig formset = new FormsetConfig();
        formset.setId(cursor.getLong(cursor.getColumnIndex("id")));
        formset.setParentModelId(cursor.getLong(cursor.getColumnIndex("parent_model_id")));
        formset.setChildModelId(cursor.getLong(cursor.getColumnIndex("child_model_id")));
        formset.setRelationshipName(cursor.getString(cursor.getColumnIndex("relationship_name")));
        formset.setExtraFields(cursor.getInt(cursor.getColumnIndex("extra_fields")));
        formset.setCanDelete(cursor.getInt(cursor.getColumnIndex("can_delete")) == 1);
        formset.setMaxNum(cursor.getInt(cursor.getColumnIndex("max_num")));
        formset.setPrefix(cursor.getString(cursor.getColumnIndex("prefix")));
        formset.setCreatedAt(cursor.getString(cursor.getColumnIndex("created_at")));
        
        cursor.close();
        db.close();
        return formset;
    }
    
    if (cursor != null) cursor.close();
    db.close();
    return null;
}

public int updateFormset(FormsetConfig formset) {
    SQLiteDatabase db = this.getWritableDatabase();
    ContentValues values = new ContentValues();
    
    values.put("relationship_name", formset.getRelationshipName());
    values.put("extra_fields", formset.getExtraFields());
    values.put("can_delete", formset.isCanDelete() ? 1 : 0);
    values.put("max_num", formset.getMaxNum());
    values.put("prefix", formset.getPrefix());
    
    int result = db.update("formsets", values, "id = ?", 
            new String[]{String.valueOf(formset.getId())});
    db.close();
    return result;
}

public boolean deleteFormset(long formsetId) {
    SQLiteDatabase db = this.getWritableDatabase();
    int result = db.delete("formsets", "id = ?", 
            new String[]{String.valueOf(formsetId)});
    db.close();
    return result > 0;
}
    
    
    
    
    // في DatabaseHelper.java
public String getModelNameById(long modelId) {
    SQLiteDatabase db = this.getReadableDatabase();
    
    Cursor cursor = db.query(TABLE_MODELS,
            new String[]{COLUMN_MODEL_NAME},
            COLUMN_ID + "=?",
            new String[]{String.valueOf(modelId)}, null, null, null);
    
    if (cursor != null && cursor.moveToFirst()) {
        String name = cursor.getString(0);
        cursor.close();
        db.close();
        return name;
    }
    
    if (cursor != null) cursor.close();
    db.close();
    return null;
}
    
    
    
    public String getAppNameForModel(long modelId) {
    SQLiteDatabase db = this.getReadableDatabase();
    
    String query = "SELECT a." + COLUMN_APP_NAME + 
                   " FROM " + TABLE_APPS + " a " +
                   "JOIN " + TABLE_MODELS + " m ON m." + COLUMN_APP_ID + " = a.id " +
                   "WHERE m." + COLUMN_ID + " = ?";
    
    Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(modelId)});
    
    if (cursor != null && cursor.moveToFirst()) {
        String appName = cursor.getString(0);
        cursor.close();
        db.close();
        return appName;
    }
    
    if (cursor != null) cursor.close();
    db.close();
    return null;
}
    
    
    // في DatabaseHelper.java
// في DatabaseHelper.java - نعدل الدالة الحالية قليلاً
public List<ModelObj> getAvailableChildModels(long parentModelId, long projectId) {
    List<ModelObj> availableModels = new ArrayList<>();
    
    String query = "SELECT m.id, m." + COLUMN_MODEL_NAME + 
                   " FROM " + TABLE_MODELS + " m " +
                   "JOIN " + TABLE_APPS + " a ON m." + COLUMN_APP_ID + " = a.id " +
                   "WHERE a." + COLUMN_PROJECT_ID + " = ? AND m.id != ?";
    
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.rawQuery(query, 
            new String[]{String.valueOf(projectId), String.valueOf(parentModelId)});
    
    while (cursor.moveToNext()) {
        long modelId = cursor.getLong(0);
        String modelName = cursor.getString(1);
        
        // استخدام ModelObj الحالي
        ModelObj model = new ModelObj(modelId, modelName);
        availableModels.add(model);
    }
    
    cursor.close();
    db.close();
    return availableModels;
}
    
    //===end frmo set
    
    public int updateField(Field field) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIELD_NAME, field.getName());
        values.put(COLUMN_FIELD_TYPE, field.getType());
        values.put(COLUMN_FIELD_OPTIONS, field.getOptions().toString());
        values.put(COLUMN_FIELD_ORDER, field.getOrder());
        
        int rows = db.update(TABLE_FIELDS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(field.getId())});
        db.close();
        return rows;
    }
    
    public void deleteField(long fieldId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FIELDS, COLUMN_ID + " = ?", new String[]{String.valueOf(fieldId)});
        db.close();
    }
    public int get_appid(String appname) {
        int appId = -1; // قيمة افتراضية في حال لم يتم العثور على التطبيق
    
    // 1. استخدام Selection Arguments لمنع SQL Injection
          String selectQuery = "SELECT " + COLUMN_ID + " FROM " + TABLE_APPS + " WHERE " + COLUMN_APP_NAME + " = ?";
    
          SQLiteDatabase db = this.getReadableDatabase(); // نستخدم Readable للقراءة فقط
           Cursor cursor = db.rawQuery(selectQuery, new String[]{appname});

           try {
              if (cursor != null && cursor.moveToFirst()) {
            // 2. الحصول على قيمة المعرف من العمود الأول
              appId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            }
           } catch (Exception e) {
                 e.printStackTrace();
           } finally {
        // 3. إغلاق الكرسر والقاعدة دائماً
            if (cursor != null) {
            cursor.close();
             }
        // لا تغلق قاعدة البيانات هنا إذا كنت تستخدمها في أماكن أخرى بكثرة، 
        // لكن من الأفضل إغلاق الكرسر فوراً.
             }

         return appId;
    }

    
    // ============ توليد JSON ============


public JSONObject generateProjectJSON(long projectId) throws JSONException {
    Project project = getProject(projectId);
    if (project == null) return null;
    
    JSONObject json = new JSONObject();
    json.put("project_name", project.getName());
    json.put("description", project.getDescription());
    
    // إعدادات المشروع
    JSONObject settings = project.getSettings();
    json.put("settings", settings);
    
    // التطبيقات
    JSONArray appsArray = new JSONArray();
    List<App> apps = getAppsByProject(projectId);
    
    for (App app : apps) {
        JSONObject appJson = new JSONObject();
        appJson.put("name", app.getName());
        appJson.put("verbose_name", app.getVerboseName());
        
        // النماذج
        JSONArray modelsArray = new JSONArray();
        List<DjangoModel> models = getModelsByApp(app.getId());
        
        for (DjangoModel model : models) {
            JSONObject modelJson = new JSONObject();
            modelJson.put("name", model.getName());
            modelJson.put("model_options", model.getOptions());
            
            // الحقول
            JSONArray fieldsArray = new JSONArray();
            List<Field> fields = getFieldsByModel(model.getId());
            
            for (Field field : fields) {
                JSONObject fieldJson = new JSONObject();
                fieldJson.put("name", field.getName());
                fieldJson.put("type", field.getType());
                fieldJson.put("field_options", field.getOptions());
                fieldsArray.put(fieldJson);
            }
            
            modelJson.put("fields", fieldsArray);
            modelsArray.put(modelJson);
        }
        
        appJson.put("models", modelsArray);
        appsArray.put(appJson);
    }
    
    json.put("apps", appsArray);
    
    // ============ إضافة FORMSETS إلى JSON ============
    JSONArray formsetsArray = new JSONArray();
    
    // جلب كل النماذج في المشروع
    for (App app : apps) {
        List<DjangoModel> models = getModelsByApp(app.getId());
        
        for (DjangoModel model : models) {
            // جلب Formsets لهذا النموذج كأب
            List<FormsetConfig> formsets = getFormsetsByParentModel(model.getId());
            
            for (FormsetConfig formset : formsets) {
                JSONObject formsetJson = formset.toJson();
                
                // إضافة معلومات إضافية من additionalInfo
                String parentModelName = formset.getAdditionalInfo("parent_model_name");
                String childModelName = formset.getAdditionalInfo("child_model_name");
                String parentAppName = formset.getAdditionalInfo("parent_app_name");
                String childAppName = formset.getAdditionalInfo("child_app_name");
                
                if (parentModelName != null) {
                    formsetJson.put("parent_model_name", parentModelName);
                } else {
                    // إذا لم تكن موجودة، نبحث عنها
                    formsetJson.put("parent_model_name", getModelNameById(formset.getParentModelId()));
                }
                
                if (childModelName != null) {
                    formsetJson.put("child_model_name", childModelName);
                } else {
                    formsetJson.put("child_model_name", getModelNameById(formset.getChildModelId()));
                }
                
                if (parentAppName != null) {
                    formsetJson.put("parent_app_name", parentAppName);
                } else {
                    formsetJson.put("parent_app_name", getAppNameForModel(formset.getParentModelId()));
                }
                
                if (childAppName != null) {
                    formsetJson.put("child_app_name", childAppName);
                } else {
                    formsetJson.put("child_app_name", getAppNameForModel(formset.getChildModelId()));
                }
                
                // إضافة حقول النموذج الابن
                List<Field> childFields = getFieldsByModel(formset.getChildModelId());
                JSONArray childFieldsArray = new JSONArray();
                
                for (Field field : childFields) {
                    JSONObject fieldJson = new JSONObject();
                    fieldJson.put("name", field.getName());
                    fieldJson.put("type", field.getType());
                    fieldJson.put("field_options", field.getOptions());
                    childFieldsArray.put(fieldJson);
                }
                
                formsetJson.put("child_fields", childFieldsArray);
                formsetsArray.put(formsetJson);
            }
        }
    }
    
    json.put("formsets", formsetsArray);
    // ============ نهاية إضافة FORMSETS ============
    
    return json;
}
// دالة خاصة لتوليد JSON مع Formsets فقط
public JSONObject generateProjectJSONWithFormsets(long projectId) throws JSONException {
    JSONObject json = generateProjectJSON(projectId); // JSON الأساسي بدون formsets
    
    JSONArray formsetsArray = new JSONArray();
    List<App> apps = getAppsByProject(projectId);
    
    for (App app : apps) {
        List<DjangoModel> models = getModelsByApp(app.getId());
        
        for (DjangoModel model : models) {
            List<FormsetConfig> formsets = getFormsetsByParentModel(model.getId());
            
            for (FormsetConfig formset : formsets) {
                JSONObject formsetJson = new JSONObject();
                
                // المعلومات الأساسية
                formsetJson.put("id", formset.getId());
                formsetJson.put("parent_model_id", formset.getParentModelId());
                formsetJson.put("child_model_id", formset.getChildModelId());
                formsetJson.put("relationship_name", formset.getRelationshipName());
                formsetJson.put("extra_fields", formset.getExtraFields());
                formsetJson.put("can_delete", formset.isCanDelete());
                formsetJson.put("max_num", formset.getMaxNum());
                formsetJson.put("prefix", formset.getPrefix());
                
                // أسماء النماذج
                String parentName = getModelNameById(formset.getParentModelId());
                String childName = getModelNameById(formset.getChildModelId());
                String parentApp = getAppNameForModel(formset.getParentModelId());
                String childApp = getAppNameForModel(formset.getChildModelId());
                
                formsetJson.put("parent_model_name", parentName != null ? parentName : "");
                formsetJson.put("child_model_name", childName != null ? childName : "");
                formsetJson.put("parent_app_name", parentApp != null ? parentApp : "");
                formsetJson.put("child_app_name", childApp != null ? childApp : "");
                
                // معلومات إضافية للقالب
                formsetJson.put("formset_title", childName + " for " + parentName);
                formsetJson.put("is_multiple", true); // يمكن أن يكون multiple
                formsetJson.put("can_add_more", true); // يمكن إضافة المزيد
                
                formsetsArray.put(formsetJson);
            }
        }
    }
    
    json.put("formsets", formsetsArray);
    json.put("has_formsets", formsetsArray.length() > 0);
    json.put("total_formsets", formsetsArray.length());
    
    return json;
}

}
// ============ فئات البيانات ============

class Project {
    private long id;
    private String name;
    private String description;
    private JSONObject settings;
    private String createdAt;
    private String updatedAt;
    
    public Project() {
        settings = new JSONObject();
        try {
            settings.put("installed_apps", new JSONArray()
                .put("django.contrib.admin")
                .put("django.contrib.auth")
                .put("django.contrib.contenttypes")
                .put("django.contrib.sessions")
                .put("django.contrib.messages")
                .put("django.contrib.staticfiles"));
            settings.put("database", new JSONObject()
                .put("engine", "django.db.backends.sqlite3")
                .put("name", "db.sqlite3"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public JSONObject getSettings() { return settings; }
    public void setSettings(JSONObject settings) { this.settings = settings; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}

class App {
    private long id;
    private long projectId;
    private String name;
    private String verboseName;
    private int order;
    
    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getProjectId() { return projectId; }
    public void setProjectId(long projectId) { this.projectId = projectId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getVerboseName() { return verboseName; }
    public void setVerboseName(String verboseName) { this.verboseName = verboseName; }
    
    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}

class DjangoModel {
    private long id;
    private long appId;
    private String name;
    private JSONObject options;
    
    public DjangoModel() {
        options = new JSONObject();
    }
    
    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getAppId() { return appId; }
    public void setAppId(long appId) { this.appId = appId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public JSONObject getOptions() { return options; }
    public void setOptions(JSONObject options) { this.options = options; }
}


