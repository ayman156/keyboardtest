package ayman.djangogenerator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.List;
import android.content.Context;
import android.widget.*;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;
import com.google.android.material.appbar.AppBarLayout;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;
import android.os.Environment;
import android.widget.EditText;
import androidx.core.content.FileProvider;
import java.io.File;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    
    private ListView projectsListView;
    private Button addProjectButton, generateAllButton, but_impo, b_mor;
    
    private TextView textview2;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseHelper dbHelper;
    private List<Project> projectsList;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        dbHelper = new DatabaseHelper(this);
        
        initViews();
        setupListeners();
        loadProjects();
        requestStoragePermission();
        but_impo.setOnClickListener(v -> openFilePickerToImport());
        b_mor.setOnClickListener(v -> opt_db());
        PasswordGuard.checkAndShowLock(MainActivity.this, "000", new PasswordGuard.PasswordListener() {
	@Override
	public void onCorrectPassword() {
		// يتم تنفيذ الكود هنا فقط إذا كان الباسورد صحيحاً 
		// أو إذا كان المستخدم قد سجل دخوله بنجاح في وقت سابق.
	}
});
    }
    
    private void initViews() {
        projectsListView = findViewById(R.id.projects_list_view);
        addProjectButton = findViewById(R.id.add_project_button);
        generateAllButton = findViewById(R.id.generate_all_button);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        textview2 = findViewById(R.id.textview2);
        but_impo = findViewById(R.id.but_impo);
        b_mor = findViewById(R.id.b_mor);
    }
    
    private void setupListeners() {
        addProjectButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddProjectActivity.class);
            startActivityForResult(intent, 1);
        });
        
        generateAllButton.setOnClickListener(v -> {
            Intent mm = new Intent();  
            mm.setAction(Intent.ACTION_VIEW);
            mm.setData(Uri.parse("https://t.me/AYMAN_ZAFER"));
            startActivity(mm);
            
        });
        
        projectsListView.setOnItemClickListener((parent, view, position, id) -> {
            Project project = projectsList.get(position);
            Intent intent = new Intent(MainActivity.this, ProjectDetailActivity.class);
            intent.putExtra("PROJECT_ID", project.getId());
            intent.putExtra("PROJECT_NAME", project.getName());
            startActivity(intent);
        });
        
        projectsListView.setOnItemLongClickListener((parent, view, position, id) -> {
            Project project = projectsList.get(position);
            // عرض قائمة خيارات (تعديل، حذف، توليد)
            showProjectOptions(project);
            return true;
        });
        
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadProjects();
            swipeRefreshLayout.setRefreshing(false);
        });
    }
    
    private void loadProjects() {
        projectsList = dbHelper.getAllProjects();
        
        if (projectsList.isEmpty()) {
            /*
            String[] emptyMessage = {"لا توجد مشاريع، أضف مشروعاً جديداً"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, emptyMessage);
            projectsListView.setAdapter(adapter);
            */
        } else {
            String[] projectNames = new String[projectsList.size()];
            for (int i = 0; i < projectsList.size(); i++) {
                projectNames[i] = projectsList.get(i).getName() + "\n" 
                    + projectsList.get(i).getDescription();
            }
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, projectNames);
            projectsListView.setAdapter(adapter);
        }
    }
    
    private void showProjectOptions(Project project) {
       // String[] options = {"تعديل المشروع", "حذف المشروع", "توليد المشروع", "عرض التطبيقات"};
        //String[] options = {"تعديل المشروع", "حذف المشروع", "توليد المشروع", "عرض التطبيقات", "تصدير", "إنشاء تقارير"};
        String[] options = {
"تعديل المشروع"
, "حذف المشروع", 
"توليد المشروع",
 "عرض التطبيقات",
 "نسخ احتياطي", 
"إنشاء تقارير",
 "إنشاء تقارير 2",
 "إنشاء نماذج مضمنة"
 //"قائمة نماذج المضمنة"
};


        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("خيارات المشروع: " + project.getName())
               .setItems(options, (dialog, which) -> {
                   switch (which) {
                       case 0: // تعديل
                           editProject(project);
                           break;
                       case 1: // حذف
                           deleteProject(project);
                           break;
                       case 2: // توليد
                           generateProject(project);
                           break;
                       case 3: // عرض التطبيقات
                           viewApps(project);
                           break;
                           case 4:
                           export(project);
                           break;
                           case 5:
                           reportm(project);
                           break;
                           case 6:
                           reportmm(project);
                           break;
                           case 7:
                           creatfromset(project);
                           break;
                           
                   }
               })
               .show();
    }
    private void opt_db() {
       // String[] options = {"تعديل المشروع", "حذف المشروع", "توليد المشروع", "عرض التطبيقات"};
        String[] options = {"نسخ إحتياطي","إستعادة نسخة"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("النسخ والاستعة : " )
               .setItems(options, (dialog, which) -> {
                   switch (which) {
                       case 0: // تعديل
                           exdb();
                           break;
                       case 1: // حذف
                           openFilePicker();
                           break;
                       
                   }
               })
               .show();
    }
    
    private void exdb(){
        
        
       String exx = dbHelper.backupDatabase(this);
       Toast.makeText(this, exx , Toast.LENGTH_SHORT).show();
       
    }
    
    private void openFilePickerToImport() {
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    intent.setType("application/json"); // لتحديد ملفات JSON فقط
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    
    try {
        startActivityForResult(Intent.createChooser(intent, "اختر ملف المشروع (JSON)"), 101);
    } catch (android.content.ActivityNotFoundException ex) {
        Toast.makeText(this, "يرجى تثبيت مدير ملفات", Toast.LENGTH_SHORT).show();
    }
   }

    private void export(Project project) {
    // 1. جلب البيانات من قاعدة البيانات (يفضل خارج الـ Thread إذا كانت سريعة أو داخلها)
    String jsonExport = dbHelper.exportProjectToJson(project.getId());

    if (jsonExport == null) {
        Toast.makeText(this, "فشل في تجهيز بيانات المشروع", Toast.LENGTH_SHORT).show();
        return;
    }

    new Thread(() -> {
        // تحديد المسار الرئيسي للمجلد
        String folderPath = FileUtil.getExternalStorageDir().concat("/django_projects/backup/");
        // تحديد المسار الكامل للملف مع اسم المشروع
        String filePath = folderPath.concat(project.getName() + ".json");

        try {
            // 2. التأكد من وجود المجلد، إذا لم يوجد نقوم بإنشائه
            if (!FileUtil.isDirectory(folderPath)) {
                FileUtil.makeDir(folderPath);
            }

            // 3. كتابة الملف (تأكد أن FileUtil.writeFile تأخذ المسار الكامل للملف وليس المجلد فقط)
            FileUtil.writeFile(filePath, jsonExport);

            // 4. تحديث الواجهة عند النجاح
            runOnUiThread(() -> {
                Toast.makeText(this, "تم تصدير المشروع إلى: " + filePath, Toast.LENGTH_LONG).show();
            });

        } catch (Exception e) {
            // معالجة الأخطاء في حال فشل الكتابة (مثل تصاريح التخزين)
            runOnUiThread(() -> {
                Toast.makeText(this, "خطأ أثناء الحفظ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }).start();
   }

    
    private void editProject(Project project) {
        Intent intent = new Intent(this, AddProjectActivity.class);
        intent.putExtra("PROJECT_ID", project.getId());
        startActivityForResult(intent, 2);
    }
    private void creatfromset(Project project) {
        Intent intent = new Intent(this, FromsetActivity.class);
        intent.putExtra("PROJECT_ID", project.getId());
        intent.putExtra("PROJECT_NAME", project.getName());
        startActivityForResult(intent, 2);
    }
    private void listfromset(Project project) {
        Intent intent = new Intent(this, FormsetsListActivity.class);
        intent.putExtra("PROJECT_ID", project.getId());
        startActivityForResult(intent, 2);
    }
    
    private void deleteProject(Project project) {
        
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("تأكيد الحذف")
            .setMessage("هل أنت متأكد من حذف المشروع '" + project.getName() + "'؟")
            .setPositiveButton("نعم", (dialog, which) -> {
                dbHelper.deleteProject(project.getId());
                loadProjects();
                Toast.makeText(this, "تم حذف المشروع", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("لا", null)
            .show();
            
    }
    private void reportm(Project project) {
        Intent intent = new Intent(this, ReportDesignerrActivity.class);
        intent.putExtra("PROJECT_ID", project.getId());
        startActivityForResult(intent, 2);
        
    }
    private void reportmm(Project project) {
        Intent intent = new Intent(this, ReportDesignerActivity.class);
        intent.putExtra("PROJECT_ID", project.getId());
        startActivityForResult(intent, 2);
        
    }
    
    private void generateProject(Project project) {
    // 1. إظهار رسالة البدء للمستخدم
    Toast.makeText(this, "جاري تحضير البيانات...", Toast.LENGTH_SHORT).show();

    try {
        // 2. استخراج البيانات من قاعدة البيانات في الخيط الرئيسي (أو يفضل في الخلفية لاحقاً)
        JSONObject jsonData = dbHelper.generateProjectJSON(project.getId());

        if (jsonData == null) {
            Toast.makeText(this, "فشل في استخراج بيانات المشروع", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. بدء عملية التوليد في خيط منفصل (Thread) لتجنب تجميد الواجهة
        new Thread(() -> {
            try {
                // استخدام jsonData الذي تم تجهيزه
                AdvancedDjangoGenerator generator = new AdvancedDjangoGenerator(this, jsonData.toString());
                File zipFile = generator.generateProject();

                // 4. العودة لواجهة المستخدم (UI Thread) لإظهار النتيجة النهائية
                runOnUiThread(() -> {
                    if (zipFile != null && zipFile.exists()) {
                        Toast.makeText(this, "تم الإنشاء بنجاح: " + zipFile.getName(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "تمت العملية ولكن تعذر العثور على الملف", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "خطأ أثناء التوليد: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();

    } catch (Exception e) {
        Toast.makeText(this, "خطأ في قاعدة البيانات: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
}

    
    private void viewApps(Project project) {
        Intent intent = new Intent(this, ProjectDetailActivity.class);
        intent.putExtra("PROJECT_ID", project.getId());
        intent.putExtra("PROJECT_NAME", project.getName());
        startActivity(intent);
    }
    
      @Override
      protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadProjects();
        }
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
        Uri uri = data.getData();
        if (uri != null) {
            importProject(uri);
        }
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
          Uri urii = data.getData();
          if (dbHelper.restoreDatabase(this, urii)) {
            Toast.makeText(this, "تمت استعادة البيانات بنجاح! يرجى إعادة تشغيل التطبيق.", Toast.LENGTH_LONG).show();
          }
        }
       }
      }
    // لفتح مدير الملفات واختيار ملف النسخة الاحتياطية
    private void openFilePicker() {
      Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
      intent.setType("*/*"); // أو حدد النوع إذا كان ينتهي بـ .db
      startActivityForResult(intent, 100);
    }




private void importProject(Uri uri) {
    new Thread(() -> {
        try {
            // 1. قراءة النص من ملف الـ JSON باستخدام Uri
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            inputStream.close();
            
            String jsonContent = stringBuilder.toString();

            // 2. استدعاء دالة الاستيراد في DatabaseHelper
            boolean success = dbHelper.importProjectFromJson(jsonContent);

            // 3. تحديث الواجهة
            runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(this, "تم استعادة المشروع بنجاح!", Toast.LENGTH_LONG).show();
                    // هنا يمكنك إضافة كود لتحديث قائمة المشاريع في الشاشة
                } else {
                    Toast.makeText(this, "فشل استيراد الملف، تأكد من صحة التنسيق", Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                Toast.makeText(this, "خطأ في قراءة الملف: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }).start();
}






public void requestStoragePermission() {
    // 1. للأجهزة التي تعمل بنظام Android 11 (API 30) أو أحدث
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (Environment.isExternalStorageManager()) {
            // التصريح ممنوح بالفعل
            System.out.println("Permission Granted");
        } else {
            // نطلب من المستخدم الذهاب لصفحة الإعدادات لمنح التصريح
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }
    } 
    // 2. للأجهزة القديمة (Android 10 وما دون)
    else {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                == PackageManager.PERMISSION_GRANTED) {
            // التصريح ممنوح بالفعل
        } else {
            // نطلب التصريح العادي
            ActivityCompat.requestPermissions(this, 
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, 
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
    }
}


    @Override
    protected void onResume() {
        super.onResume();
        loadProjects();
    }
}