package ayman.djangogenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import java.io.*;
import java.util.*;
import android.util.Log;
import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import android.util.Log;

public class AdDjangoFormsetGenerator {
    
    private Context context;
    private JSONObject projectData;
    private File projectDirectory;
    
    public AdDjangoFormsetGenerator(Context context, String jsonString) throws JSONException {
        this.context = context;
        this.projectData = new JSONObject(jsonString);
    }
    
    public void generate() throws Exception {
        if (!projectData.has("formsets")) {
            Log.d("FormsetGenerator", "No formsets found in project data");
            return;
        }
        
        JSONArray formsets = projectData.getJSONArray("formsets");
        if (formsets.length() == 0) {
            Log.d("FormsetGenerator", "Formsets array is empty");
            return;
        }
        
        // إنشاء مجلد المشروع (افترض أنه موجود بالفعل)
        String projectName = projectData.getString("project_name");
        projectDirectory = new File(FileUtil.getExternalStorageDir(), 
                                  "django_projects/" + projectName);
        
        if (!projectDirectory.exists()) {
            Log.e("FormsetGenerator", "Project directory not found: " + projectDirectory.getPath());
            throw new Exception("Project directory not found. Please generate project first.");
        }
        
        Log.i("FormsetGenerator", "Generating formsets for project: " + projectName);
        
        // 1. التحقق من تكوين formsets
        validateFormsetsConfiguration();
        
        // 2. تحديث الملفات الحالية
        updateExistingFiles();
        
        // 3. إنشاء قوالب formsets
        createFormsetTemplates();
        
        // 4. إنشاء ملف توثيق
        createFormsetDocumentation();
        
        // 5. إنشاء ملفات JavaScript إضافية
        createFormsetJavaScript();
        
        Log.i("FormsetGenerator", "Formsets generation completed successfully");
    }
    
    private void validateFormsetsConfiguration() throws JSONException {
        JSONArray formsets = projectData.getJSONArray("formsets");
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        
        for (int i = 0; i < formsets.length(); i++) {
            JSONObject formset = formsets.getJSONObject(i);
            String formsetId = "Formset#" + formset.optInt("id", i+1);
            
            // التحقق من الحقول المطلوبة
            String[] requiredFields = {
                "parent_model_name", "child_model_name",
                "parent_app_name", "child_app_name"
            };
            
            for (String field : requiredFields) {
                if (!formset.has(field) || formset.getString(field).isEmpty()) {
                    errors.add(formsetId + ": Missing required field '" + field + "'");
                }
            }
            
            // التحقق من وجود التطبيقات
            String parentApp = formset.getString("parent_app_name");
            String childApp = formset.getString("child_app_name");
            
            if (!isAppExists(parentApp)) {
                errors.add(formsetId + ": Parent app '" + parentApp + "' not found");
            }
            
            if (!isAppExists(childApp)) {
                errors.add(formsetId + ": Child app '" + childApp + "' not found");
            }
            
            // التحقق من وجود النماذج
            String parentModel = formset.getString("parent_model_name");
            String childModel = formset.getString("child_model_name");
            
            if (!isModelExists(parentApp, parentModel)) {
                errors.add(formsetId + ": Parent model '" + parentModel + "' not found in app '" + parentApp + "'");
            }
            
            if (!isModelExists(childApp, childModel)) {
                errors.add(formsetId + ": Child model '" + childModel + "' not found in app '" + childApp + "'");
            }
            
            // تحذيرات
            if (!formset.has("extra_fields") || formset.getInt("extra_fields") <= 0) {
                warnings.add(formsetId + ": extra_fields should be at least 1");
            }
            
            if (!formset.has("prefix") || formset.getString("prefix").isEmpty()) {
                warnings.add(formsetId + ": No prefix specified, using default");
            }
        }
        
        // تسجيل التحذيرات
        if (!warnings.isEmpty()) {
            Log.w("FormsetGenerator", "Formset warnings:");
            for (String warning : warnings) {
                Log.w("FormsetGenerator", "  ⚠ " + warning);
            }
        }
        
        // تسجيل الأخطاء ورمي استثناء إذا وجدت
        if (!errors.isEmpty()) {
            Log.e("FormsetGenerator", "Formset configuration errors:");
            for (String error : errors) {
                Log.e("FormsetGenerator", "  ✗ " + error);
            }
            throw new IllegalArgumentException("Invalid formset configuration. Please fix the errors above.");
        }
    }
    
    private boolean isAppExists(String appName) throws JSONException {
        if (!projectData.has("apps")) return false;
        
        JSONArray apps = projectData.getJSONArray("apps");
        for (int i = 0; i < apps.length(); i++) {
            JSONObject app = apps.getJSONObject(i);
            if (app.getString("name").equals(appName)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isModelExists(String appName, String modelName) throws JSONException {
        if (!projectData.has("apps")) return false;
        
        JSONArray apps = projectData.getJSONArray("apps");
        for (int i = 0; i < apps.length(); i++) {
            JSONObject app = apps.getJSONObject(i);
            if (app.getString("name").equals(appName) && app.has("models")) {
                JSONArray models = app.getJSONArray("models");
                for (int j = 0; j < models.length(); j++) {
                    JSONObject model = models.getJSONObject(j);
                    if (model.getString("name").equals(modelName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private void updateExistingFiles() throws Exception {
        JSONArray formsets = projectData.getJSONArray("formsets");
        
        // تجميع formsets حسب التطبيق (للتطبيقات التي تحتوي على النماذج الأب)
        Map<String, List<JSONObject>> formsetsByParentApp = new HashMap<>();
        
        for (int i = 0; i < formsets.length(); i++) {
            JSONObject formset = formsets.getJSONObject(i);
            String parentApp = formset.getString("parent_app_name");
            
            if (!formsetsByParentApp.containsKey(parentApp)) {
                formsetsByParentApp.put(parentApp, new ArrayList<JSONObject>());
            }
            formsetsByParentApp.get(parentApp).add(formset);
        }
        
        // تحديث كل تطبيق
        for (Map.Entry<String, List<JSONObject>> entry : formsetsByParentApp.entrySet()) {
            String appName = entry.getKey();
            List<JSONObject> appFormsets = entry.getValue();
            
            Log.d("FormsetGenerator", "Updating app '" + appName + "' with " + appFormsets.size() + " formsets");
            
            // 1. تحديث views.py
            updateViewsForApp(appName, appFormsets);
            
            // 2. تحديث forms.py
            updateFormsForApp(appName, appFormsets);
            
            // 3. تحديث urls.py
            updateUrlsForApp(appName, appFormsets);
            
            // 4. تحديث admin.py
            updateAdminForApp(appName, appFormsets);
        }
    }
    
    private void updateViewsForApp(String appName, List<JSONObject> formsets) throws Exception {
        File viewsFile = new File(projectDirectory, appName + "/views.py");
        
        if (!viewsFile.exists()) {
            Log.e("FormsetGenerator", "views.py not found for app: " + appName);
            return;
        }
        
        String currentContent = readFile(viewsFile);
        
        // تحقق إذا كان يحتوي بالفعل على formsets
        if (currentContent.contains("inlineformset_factory") || 
            currentContent.contains("FormSet") || 
            currentContent.contains("_formset")) {
            Log.w("FormsetGenerator", "App '" + appName + "' already has formsets in views.py");
            return;
        }
        
        StringBuilder newContent = new StringBuilder();
        
        // توليد import statements
        newContent.append("\n# ============ INLINE FORMSETS GENERATED ============\n");
        newContent.append("from django.forms import inlineformset_factory\n");
        newContent.append("from django.http import JsonResponse\n");
        newContent.append("from django.views.decorators.http import require_POST\n");
        newContent.append("\n");
        
        // توليد دوال create مع formsets لكل نموذج أب
        Map<String, List<JSONObject>> formsetsByParentModel = groupFormsetsByParentModel(formsets);
        
        for (Map.Entry<String, List<JSONObject>> entry : formsetsByParentModel.entrySet()) {
            String parentModel = entry.getKey();
            List<JSONObject> modelFormsets = entry.getValue();
            
            // توليد دالة create
            generateCreateViewWithFormsets(newContent, appName, parentModel, modelFormsets);
            
            // توليد دالة update
            generateUpdateViewWithFormsets(newContent, appName, parentModel, modelFormsets);
            
            // توليد دوال AJAX مساعدة
            generateAjaxHelpers(newContent, appName, parentModel, modelFormsets);
        }
        
        // إضافة المحتوى الجديد إلى الملف
        String updatedContent = currentContent + newContent.toString();
        writeFile(viewsFile, updatedContent);
        
        Log.i("FormsetGenerator", "Updated views.py for app: " + appName);
    }
    
    private void generateCreateViewWithFormsets(StringBuilder sb, String appName, String parentModel, List<JSONObject> formsets) throws JSONException {
    String lowerModel = parentModel.toLowerCase();
    
    sb.append("\ndef create_").append(lowerModel).append("_with_formsets(request):\n");
    sb.append("    \"\"\"\n");
    sb.append("    Create ").append(parentModel).append(" with inline formsets\n");
    sb.append("    Includes: ");
    for (int i = 0; i < formsets.size(); i++) {
        sb.append(formsets.get(i).getString("child_model_name"));
        if (i < formsets.size() - 1) sb.append(", ");
    }
    sb.append("\n    \"\"\"\n\n");
    
    // 1. تعريف الـ FormSet Factories (داخل الدالة لضمان توفر الموديلات والفورمز)
    sb.append("    # تعريف مصانع الفورم سيت محلياً\n");
    for (JSONObject formset : formsets) {
        String childModel = formset.getString("child_model_name");
        int extra = formset.optInt("extra_fields", 1);
        boolean canDelete = formset.optBoolean("can_delete", true);
        String pyCanDelete = canDelete ? "True" : "False"; // تصحيح بايثون
        
        sb.append("    ").append(childModel).append("FS = inlineformset_factory(\n");
        sb.append("        ").append(parentModel).append(", ").append(childModel).append(",\n");
        sb.append("        form=").append(childModel).append("Form, fields='__all__',\n");
        sb.append("        extra=").append(extra).append(", can_delete=").append(pyCanDelete).append(",\n");
        sb.append("        max_num=").append(formset.optInt("max_num", 10)).append(", validate_max=True\n");
        sb.append("    )\n");
    }
    sb.append("\n");
    
    // 2. منطق POST
    sb.append("    if request.method == 'POST':\n");
    sb.append("        form = ").append(parentModel).append("Form(request.POST, request.FILES)\n");
    
    for (JSONObject formset : formsets) {
        String childModel = formset.getString("child_model_name");
        String prefix = formset.optString("prefix", childModel.toLowerCase());
        sb.append("        ").append(childModel.toLowerCase()).append("_fs = ");
        sb.append(childModel).append("FS(request.POST, request.FILES, prefix='").append(prefix).append("')\n");
    }
    
    sb.append("\n        # التحقق من صحة جميع النماذج\n");
    sb.append("        if form.is_valid()");
    for (JSONObject formset : formsets) {
        String childModel = formset.getString("child_model_name");
        sb.append(" and ").append(childModel.toLowerCase()).append("_fs.is_valid()");
    }
    sb.append(":\n");
    
    sb.append("            # حفظ النموذج الرئيسي\n");
    sb.append("            parent_obj = form.save(commit=False)\n");
    sb.append("            if hasattr(parent_obj, 'created_by'):\n");
    sb.append("                parent_obj.created_by = request.user\n");
    sb.append("            parent_obj.save()\n\n");
    
    for (JSONObject formset : formsets) {
        String childModel = formset.getString("child_model_name");
        sb.append("            # حفظ ").append(childModel).append("\n");
        sb.append("            ").append(childModel.toLowerCase()).append("_fs.instance = parent_obj\n");
        sb.append("            ").append(childModel.toLowerCase()).append("_fs.save()\n");
    }
    
    sb.append("\n            messages.success(request, _('تم إنشاء البيانات بنجاح'))\n");
    sb.append("            return redirect('").append(appName).append(":").append(lowerModel).append("_list')\n");
    sb.append("        else:\n");
    sb.append("            messages.error(request, _('يوجد أخطاء في النموذج'))\n");
    
    // 3. منطق GET
    sb.append("    else:\n");
    sb.append("        form = ").append(parentModel).append("Form()\n");
    for (JSONObject formset : formsets) {
        String childModel = formset.getString("child_model_name");
        String prefix = formset.optString("prefix", childModel.toLowerCase());
        sb.append("        ").append(childModel.toLowerCase()).append("_fs = ");
        sb.append(childModel).append("FS(prefix='").append(prefix).append("')\n");
    }
    
    // 4. إعداد السياق (Context)
    sb.append("\n    context = {\n");
    sb.append("        'form': form,\n");
    sb.append("        'title': _('إضافة %s جديد') % _('").append(parentModel).append("'),\n");
    for (JSONObject formset : formsets) {
        String childModel = formset.getString("child_model_name");
        sb.append("        '").append(childModel.toLowerCase()).append("_formset': ");
        sb.append(childModel.toLowerCase()).append("_fs,\n");
    }
    sb.append("        'formsets_count': ").append(formsets.size()).append(",\n");
    sb.append("        'parent_model': '").append(parentModel).append("',\n");
    sb.append("    }\n");
    
    sb.append("    return render(request, '").append(appName).append("/").append(lowerModel).append("_formset_form.html', context)\n");
}

    
    private void generateUpdateViewWithFormsets(StringBuilder sb, String appName, String parentModel, List<JSONObject> formsets) throws JSONException{
        String lowerModel = parentModel.toLowerCase();
        
        sb.append("\ndef update_").append(lowerModel).append("_with_formsets(request, pk):\n");
        sb.append("    \"\"\"\n");
        sb.append("    Update ").append(parentModel).append(" with inline formsets\n");
        sb.append("    \"\"\"\n");
        sb.append("    parent_obj = get_object_or_404(").append(parentModel).append(", pk=pk)\n");
        sb.append("    \n");
        
        // توليد FormSet factories
        for (JSONObject formset : formsets) {
            String childModel = formset.getString("child_model_name");
            String prefix = formset.optString("prefix", childModel.toLowerCase());
            boolean pcanDelete = formset.optBoolean("can_delete", true);
            String canDelete = pcanDelete ? "True" : "False";
            
            sb.append("    ").append(childModel).append("FormSet = inlineformset_factory(\n");
            sb.append("        ").append(parentModel).append(",\n");
            sb.append("        ").append(childModel).append(",\n");
            sb.append("        form=").append(childModel).append("Form,\n");
            sb.append("        fields='__all__',\n");
            sb.append("        extra=1,\n");
            sb.append("        can_delete=").append(canDelete).append(",\n");
            sb.append("        max_num=").append(formset.optInt("max_num", 10)).append(",\n");
            sb.append("        validate_max=True\n");
            sb.append("    )\n");
        }
        sb.append("    \n");
        
        // منطق POST
        sb.append("    if request.method == 'POST':\n");
        sb.append("        form = ").append(parentModel).append("Form(request.POST, request.FILES, instance=parent_obj)\n");
        
        for (JSONObject formset : formsets) {
            String childModel = formset.getString("child_model_name");
            String prefix = formset.optString("prefix", childModel.toLowerCase());
            sb.append("        ").append(childModel.toLowerCase()).append("_formset = ");
            sb.append(childModel).append("FormSet(request.POST, request.FILES, instance=parent_obj, prefix='").append(prefix).append("')\n");
        }
        sb.append("        \n");
        sb.append("        # التحقق من صحة جميع النماذج\n");
        sb.append("        is_valid = form.is_valid()\n");
        for (JSONObject formset : formsets) {
            String childModel = formset.getString("child_model_name");
            sb.append("        is_valid = is_valid and ").append(childModel.toLowerCase()).append("_formset.is_valid()\n");
        }
        sb.append("        \n");
        sb.append("        if is_valid:\n");
        sb.append("            form.save()\n");
        for (JSONObject formset : formsets) {
            String childModel = formset.getString("child_model_name");
            sb.append("            ").append(childModel.toLowerCase()).append("_formset.save()\n");
        }
        sb.append("            \n");
        sb.append("            messages.success(request, _('تم تحديث %s بنجاح') % parent_obj)\n");
        sb.append("            return redirect('").append(appName).append(":").append(lowerModel).append("_list')\n");
        sb.append("        else:\n");
        sb.append("            messages.error(request, _('يوجد أخطاء في النموذج'))\n");
        sb.append("    else:\n");
        sb.append("        form = ").append(parentModel).append("Form(instance=parent_obj)\n");
        
        for (JSONObject formset : formsets) {
            String childModel = formset.getString("child_model_name");
            String prefix = formset.optString("prefix", childModel.toLowerCase());
            sb.append("        ").append(childModel.toLowerCase()).append("_formset = ");
            sb.append(childModel).append("FormSet(instance=parent_obj, prefix='").append(prefix).append("')\n");
        }
        sb.append("    \n");
        
        // السياق
        sb.append("    context = {\n");
        sb.append("        'form': form,\n");
        sb.append("        'title': _('تعديل %s') % parent_obj,\n");
        for (JSONObject formset : formsets) {
            String childModel = formset.getString("child_model_name");
            sb.append("        '").append(childModel.toLowerCase()).append("_formset': ");
            sb.append(childModel.toLowerCase()).append("_formset,\n");
        }
        sb.append("        'parent_obj': parent_obj,\n");
        sb.append("        'formsets_count': ").append(formsets.size()).append(",\n");
        sb.append("        'parent_model': '").append(parentModel).append("',\n");
        sb.append("    }\n");
        sb.append("    \n");
        sb.append("    return render(request, '").append(appName).append("/").append(lowerModel).append("_formset_form.html', context)\n");
    }
    
    private void generateAjaxHelpers(StringBuilder sb, String appName, String parentModel, List<JSONObject> formsets) throws JSONException {
    String lowerModel = parentModel.toLowerCase();
    
    sb.append("\n@require_POST\n");
    sb.append("def add_").append(lowerModel).append("_formset_row(request, formset_name):\n");
    sb.append("    \"\"\"\n");
    sb.append("    إضافة صف جديد إلى formset عبر AJAX\n");
    sb.append("    \"\"\"\n");
    sb.append("    from django.template.loader import render_to_string\n");
    sb.append("    html = \"\"  # تعريف أولي لتجنب UnboundLocalError\n\n");
    
    // الحالة الأولى: النموذج الأب
    sb.append("    if formset_name == '").append(parentModel).append("':\n");
    sb.append("        form = ").append(parentModel).append("Form()\n");
    sb.append("        html = render_to_string('").append(appName).append("/includes/formset_row.html', {\n");
    sb.append("            'form': form,\n");
    sb.append("            'prefix': 'form',\n");
    sb.append("            'index': '{{ index }}',\n");
    sb.append("        })\n");
    
    // الحالات الأخرى: النماذج التابعة
    for (JSONObject formset : formsets) {
        String childModel = formset.getString("child_model_name");
        String prefix = formset.optString("prefix", childModel.toLowerCase());
        
        // لاحظ البدء بـ \n ثم مسافات لضبط محاذاة elif لتكون تحت if مباشرة
        sb.append("\n    elif formset_name == '").append(childModel).append("':\n");
        sb.append("        # توليد المصنع محلياً بدون prefix\n");
        sb.append("        ").append(childModel).append("_FS = inlineformset_factory(\n");
        sb.append("            ").append(parentModel).append(",\n");
        sb.append("            ").append(childModel).append(",\n");
        sb.append("            form=").append(childModel).append("Form,\n");
        sb.append("            fields='__all__',\n");
        sb.append("            extra=1,\n");
        sb.append("            can_delete=True\n"); // التأكد من T الكبيرة
        sb.append("        )\n");
        sb.append("        # إنشاء النسخة مع الـ prefix هنا\n");
        sb.append("        formset_inst = ").append(childModel).append("_FS(prefix='").append(prefix).append("')\n");
        sb.append("        form = formset_inst.forms[0]\n");
        sb.append("        html = render_to_string('").append(appName).append("/includes/formset_row.html', {\n");
        sb.append("            'form': form,\n");
        sb.append("            'prefix': '").append(prefix).append("',\n");
        sb.append("            'index': '{{ index }}',\n");
        sb.append("        })\n");
    }
    
    // محاذاة else لتكون على نفس مستوى if/elif
    sb.append("\n    else:\n");
    sb.append("        return JsonResponse({'error': 'Formset not found'}, status=400)\n");
    
    // العودة للـ JsonResponse النهائي بمحاذاة الدالة
    sb.append("\n    return JsonResponse({\n");
    sb.append("        'html': html,\n");
    sb.append("        'formset_name': formset_name,\n");
    sb.append("    })\n");
}

    
    private Map<String, List<JSONObject>> groupFormsetsByParentModel(List<JSONObject> formsets) throws JSONException {
        Map<String, List<JSONObject>> map = new HashMap<>();
        
        for (JSONObject formset : formsets) {
            String parentModel = formset.getString("parent_model_name");
            
            if (!map.containsKey(parentModel)) {
                map.put(parentModel, new ArrayList<JSONObject>());
            }
            map.get(parentModel).add(formset);
        }
        
        return map;
    }
    
    private void updateFormsForApp(String appName, List<JSONObject> formsets) throws Exception {
    File formsFile = new File(projectDirectory, appName + "/forms.py");
    
    if (!formsFile.exists()) {
        Log.w("FormsetGenerator", "forms.py not found for app: " + appName + ", creating it");
        createBasicFormsFile(appName);
    }
    
    String currentContent = readFile(formsFile);
    
    // تحقق مما إذا كان يحتوي بالفعل على FormSet لتجنب التكرار
    if (currentContent.contains("inlineformset_factory")) {
        Log.w("FormsetGenerator", "App '" + appName + "' already has FormSet factories in forms.py");
        return;
    }
    
    StringBuilder newContent = new StringBuilder();
    newContent.append("\n\n# ============ INLINE FORMSETS FACTORIES ============\n");
    newContent.append("from django.forms import inlineformset_factory\n");
    
    // توليد FormSet factories
    for (JSONObject formset : formsets) {
        String parentModel = formset.getString("parent_model_name");
        String childModel = formset.getString("child_model_name");
        int extra = formset.optInt("extra_fields", 1);
        boolean canDelete = formset.optBoolean("can_delete", true);
        int maxNum = formset.optInt("max_num", 10);
        
        // تحويل القيمة المنطقية من java (true/false) إلى python (True/False)
        String pyCanDelete = canDelete ? "True" : "False";
        
        newContent.append("\n");
        newContent.append(childModel).append("FormSet = inlineformset_factory(\n");
        newContent.append("    ").append(parentModel).append(",          # Parent model\n");
        newContent.append("    ").append(childModel).append(",          # Child model\n");
        newContent.append("    form=").append(childModel).append("Form,  # Form class\n");
        newContent.append("    fields='__all__',        # Use all fields\n");
        newContent.append("    extra=").append(extra).append(",            # Empty forms to show\n");
        newContent.append("    can_delete=").append(pyCanDelete).append(",       # Allow deletion\n");
        newContent.append("    max_num=").append(maxNum).append(",          # Maximum forms allowed\n");
        newContent.append("    validate_max=True        # Validate maximum number\n");
        // تم حذف سطر prefix نهائياً من هنا لتجنب TypeError في بايثون
        newContent.append(")\n");
    }
    
    // إضافة تعليق توثيق إرشادي للمستخدم
    newContent.append("\n");
    newContent.append("# ملاحظة هامة للاستخدام في views.py:\n");
    newContent.append("# عند إنشاء نسخة من الفورم سيت، أضف الـ prefix هناك:\n");
   // newContent.append("# formset = ").append(formsets.length() > 0 ? formsets.getJSONObject(0).getString("child_model_name") : "Model").append("FormSet(prefix='your_prefix')\n");
    
    String updatedContent = currentContent + newContent.toString();
    writeFile(formsFile, updatedContent);
    
    Log.i("FormsetGenerator", "Updated forms.py for app: " + appName);
}

    
    private void createBasicFormsFile(String appName) throws Exception {
        File formsFile = new File(projectDirectory, appName + "/forms.py");
        
        StringBuilder content = new StringBuilder();
        content.append("from django import forms\n");
        content.append("from django.forms import inlineformset_factory\n");
        content.append("from django.utils.translation import gettext_lazy as _\n");
        content.append("from .models import *\n\n");
        content.append("# Basic forms will be added by Formset Generator\n");
        
        writeFile(formsFile, content.toString());
    }
    
    private void updateUrlsForApp(String appName, List<JSONObject> formsets) throws Exception {
        File urlsFile = new File(projectDirectory, appName + "/urls.py");
        
        if (!urlsFile.exists()) {
            Log.e("FormsetGenerator", "urls.py not found for app: " + appName);
            return;
        }
        
        String currentContent = readFile(urlsFile);
        
        // استخراج النماذج الأب الفريدة من formsets
        Set<String> parentModels = new HashSet<>();
        for (JSONObject formset : formsets) {
            parentModels.add(formset.getString("parent_model_name").toLowerCase());
        }
        
        StringBuilder newUrls = new StringBuilder();
        
        for (String model : parentModels) {
            newUrls.append("    # Formsets URLs for ").append(model).append("\n");
            newUrls.append("    path('").append(model).append("/create-with-formsets/', views.create_").append(model).append("_with_formsets, name='").append(model).append("_create_with_formsets'),\n");
            newUrls.append("    path('").append(model).append("/<int:pk>/update-with-formsets/', views.update_").append(model).append("_with_formsets, name='").append(model).append("_update_with_formsets'),\n");
            newUrls.append("    path('").append(model).append("/add-formset-row/<str:formset_name>/', views.add_").append(model).append("_formset_row, name='add_").append(model).append("_formset_row'),\n");
            newUrls.append("\n");
        }
        
        // إضافة المسارات قبل السطر الأخير (])
        int lastBracketIndex = currentContent.lastIndexOf("]");
        if (lastBracketIndex > 0) {
            String before = currentContent.substring(0, lastBracketIndex);
            String after = currentContent.substring(lastBracketIndex);
            
            String updatedContent = before + "\n" + newUrls.toString() + after;
            writeFile(urlsFile, updatedContent);
        } else {
            // إذا لم نجد ]، نضيف في النهاية
            String updatedContent = currentContent + "\n" + newUrls.toString();
            writeFile(urlsFile, updatedContent);
        }
        
        Log.i("FormsetGenerator", "Updated urls.py for app: " + appName);
    }
    
    private void updateAdminForApp(String appName, List<JSONObject> formsets) throws Exception {
        File adminFile = new File(projectDirectory, appName + "/admin.py");
        
        if (!adminFile.exists()) {
            Log.w("FormsetGenerator", "admin.py not found for app: " + appName);
            return;
        }
        
        String currentContent = readFile(adminFile);
        
        // تحقق مما إذا كان يحتوي بالفعل على Inline
        if (currentContent.contains("class") && currentContent.contains("Inline")) {
            Log.w("FormsetGenerator", "App '" + appName + "' already has Inline classes in admin.py");
            return;
        }
        
        StringBuilder newContent = new StringBuilder();
        newContent.append("\n\n# ============ ADMIN INLINES FOR FORMSETS ============\n");
        newContent.append("# Auto-generated by Django Formset Generator\n");
        
        // تجميع formsets حسب النموذج الأب
        Map<String, List<JSONObject>> formsetsByParent = groupFormsetsByParentModel(formsets);
        
        for (Map.Entry<String, List<JSONObject>> entry : formsetsByParent.entrySet()) {
            String parentModel = entry.getKey();
            List<JSONObject> modelFormsets = entry.getValue();
            
            newContent.append("\n# Inlines for ").append(parentModel).append("\n");
            
            for (JSONObject formset : modelFormsets) {
                String childModel = formset.getString("child_model_name");
                int extra = formset.optInt("extra_fields", 1);
                boolean pcanDelete = formset.optBoolean("can_delete", true);
                String canDelete = pcanDelete ? "True" : "False";
                int maxNum = formset.optInt("max_num", 10);
                
                newContent.append("class ").append(childModel).append("Inline(admin.TabularInline):\n");
                newContent.append("    model = ").append(childModel).append("\n");
                newContent.append("    extra = ").append(extra).append("  # Number of empty forms\n");
                newContent.append("    can_delete = ").append(canDelete).append("  # Allow deletion\n");
                newContent.append("    max_num = ").append(maxNum).append("  # Maximum forms\n");
                newContent.append("    \n");
                newContent.append("    # Customize fields as needed\n");
                newContent.append("    # fields = ('field1', 'field2')\n");
                newContent.append("    # exclude = ('field3',)\n");
                newContent.append("    \n");
                newContent.append("    # Custom form if needed\n");
                newContent.append("    # form = ").append(childModel).append("Form\n");
                newContent.append("    \n");
                newContent.append("    def get_formset(self, request, obj=None, **kwargs):\n");
                newContent.append("        formset = super().get_formset(request, obj, **kwargs)\n");
                newContent.append("        # Customize formset if needed\n");
                newContent.append("        return formset\n");
                newContent.append("\n");
            }
            
            // تحديث ModelAdmin إذا كان موجوداً
            if (currentContent.contains("class " + parentModel + "Admin")) {
                newContent.append("# To use these inlines, update ").append(parentModel).append("Admin:\n");
                newContent.append("# class ").append(parentModel).append("Admin(admin.ModelAdmin):\n");
                newContent.append("#     inlines = [\n");
                for (JSONObject formset : modelFormsets) {
                    String childModel = formset.getString("child_model_name");
                    newContent.append("#         ").append(childModel).append("Inline,\n");
                }
                newContent.append("#     ]\n");
                newContent.append("\n");
            }
        }
        
        String updatedContent = currentContent + newContent.toString();
        writeFile(adminFile, updatedContent);
        
        Log.i("FormsetGenerator", "Updated admin.py for app: " + appName);
    }
    
    private void createFormsetTemplates() throws Exception {
        JSONArray formsets = projectData.getJSONArray("formsets");
        
        // تجميع حسب التطبيق والنموذج الأب
        Map<String, Map<String, List<JSONObject>>> templatesByApp = new HashMap<>();
        
        for (int i = 0; i < formsets.length(); i++) {
            JSONObject formset = formsets.getJSONObject(i);
            String appName = formset.getString("parent_app_name");
            String parentModel = formset.getString("parent_model_name");
            
            if (!templatesByApp.containsKey(appName)) {
                templatesByApp.put(appName, new HashMap<String, List<JSONObject>>());
            }
            
            Map<String, List<JSONObject>> appMap = templatesByApp.get(appName);
            if (!appMap.containsKey(parentModel)) {
                appMap.put(parentModel, new ArrayList<JSONObject>());
            }
            
            appMap.get(parentModel).add(formset);
        }
        
        // إنشاء قوالب لكل تطبيق ونموذج
        for (Map.Entry<String, Map<String, List<JSONObject>>> appEntry : templatesByApp.entrySet()) {
            String appName = appEntry.getKey();
            Map<String, List<JSONObject>> modelsMap = appEntry.getValue();
            
            // إنشاء مجلد templates للتطبيق إذا لم يكن موجوداً
            File templatesDir = new File(projectDirectory, "templates/" + appName);
            if (!templatesDir.exists()) {
                templatesDir.mkdirs();
            }
            
            // إنشاء مجلد includes
            File includesDir = new File(templatesDir, "includes");
            if (!includesDir.exists()) {
                includesDir.mkdirs();
            }
            
            // قالب صف formset
            createFormsetRowTemplate(includesDir);
            
            // قالب JavaScript
            createFormsetJsTemplate(includesDir);
            
            // قوالب formsets لكل نموذج
            for (Map.Entry<String, List<JSONObject>> modelEntry : modelsMap.entrySet()) {
                String parentModel = modelEntry.getKey();
                List<JSONObject> modelFormsets = modelEntry.getValue();
                
                createFormsetFormTemplate(appName, parentModel, modelFormsets);
                createFormsetManagementTemplate(appName, parentModel, modelFormsets);
            }
        }
        
        // قالب عام لل formsets
        createGlobalFormsetTemplate();
    }
    
    private void createFormsetRowTemplate(File includesDir) throws Exception {
        File rowTemplate = new File(includesDir, "formset_row.html");
        
        String content = "<!-- formset_row.html -->\n" +
                        "<!-- قالب صف formset واحد -->\n" +
                        "{% load crispy_forms_tags %}\n" +
                        "{% load static %}\n" +
                        "\n" +
                        "<div class=\"formset-row card mb-3\" id=\"formset-row-{{ prefix }}-{{ index }}\">\n" +
                        "    <div class=\"card-header d-flex justify-content-between align-items-center\">\n" +
                        "        <h6 class=\"mb-0\">#<span class=\"formset-counter\">{{ forloop.counter }}</span></h6>\n" +
                        "        <button type=\"button\" class=\"btn btn-sm btn-outline-danger remove-formset-row\" \n" +
                        "                data-target=\"#formset-row-{{ prefix }}-{{ index }}\">\n" +
                        "            <i class=\"fas fa-times\"></i>\n" +
                        "        </button>\n" +
                        "    </div>\n" +
                        "    <div class=\"card-body\">\n" +
                        "        {{ form|crispy }}\n" +
                        "        {{ form.DELETE|as_crispy_field }}\n" +
                        "    </div>\n" +
                        "</div>\n";
        
        writeFile(rowTemplate, content);
        Log.d("FormsetGenerator", "Created formset row template");
    }
    
    private void createFormsetJsTemplate(File includesDir) throws Exception {
      //  File jsTemplate = new File(includesDir, "formset_js.html");
        
        Assets assetHelper = new Assets(context, "formset_js.html", includesDir.getAbsolutePath());
      if (assetHelper.copyAssetToPath()) {
    // تم النسخ بنجاح
       }
        Log.d("FormsetGenerator", "Created formset JavaScript template");
    }
    
    private void createFormsetFormTemplate(String appName, String parentModel, List<JSONObject> formsets) throws Exception {
    String lowerModel = parentModel.toLowerCase();
    File formTemplate = new File(projectDirectory, "templates/" + appName + "/" + lowerModel + "_formset_form.html");
    
    StringBuilder content = new StringBuilder();
    content.append("{% extends 'base.html' %}\n");
    content.append("{% load crispy_forms_tags %}\n");
    content.append("{% load static %}\n");
    content.append("{% load i18n %}\n");
    content.append("\n");
    content.append("{% block title %}{{ title }}{% endblock %}\n");
    content.append("\n");
    content.append("{% block content %}\n");
    content.append("<div class=\"container-fluid py-4\">\n");
    
    // Breadcrumb section
    content.append("    <div class=\"row\">\n");
    content.append("        <div class=\"col-12\">\n");
    content.append("            <nav aria-label=\"breadcrumb\">\n");
    content.append("                <ol class=\"breadcrumb bg-light p-3 rounded shadow-sm\">\n");
    content.append("                    <li class=\"breadcrumb-item\"><a href=\"{% url '").append(appName).append(":index' %}\"><i class=\"fas fa-home me-1\"></i>{% trans 'الرئيسية' %}</a></li>\n");
    content.append("                    <li class=\"breadcrumb-item\"><a href=\"{% url '").append(appName).append(":").append(lowerModel).append("_list' %}\">{{ parent_model }}</a></li>\n");
    content.append("                    <li class=\"breadcrumb-item active text-dark fw-bold\">{{ title }}</li>\n");
    content.append("                </ol>\n");
    content.append("            </nav>\n");
    content.append("            <h2 class=\"fw-bold text-primary mb-4\"><i class=\"fas fa-layer-group me-2\"></i>{{ title }}</h2>\n");
    content.append("        </div>\n");
    content.append("    </div>\n");

    content.append("    <form method=\"post\" id=\"multi-formset-form\" enctype=\"multipart/form-data\" class=\"needs-validation\" novalidate>\n");
    content.append("        {% csrf_token %}\n");
    
    // النموذج الرئيسي - 4 حقول في الصف
    content.append("        <div class=\"card border-primary mb-4 shadow-sm\">\n");
    content.append("            <div class=\"card-header bg-primary text-white\">\n");
    content.append("                <h4 class=\"mb-0\"><i class=\"fas fa-info-circle me-2\"></i>{% trans 'البيانات الرئيسية' %}</h4>\n");
    content.append("            </div>\n");
    content.append("            <div class=\"card-body\">\n");
    content.append("                <div class=\"row\">\n");
    content.append("                    {% for field in form %}\n");
    content.append("                        <div class=\"col-md-3 mb-3\">{{ field|as_crispy_field }}</div>\n");
    content.append("                    {% endfor %}\n");
    content.append("                </div>\n");
    content.append("            </div>\n");
    content.append("        </div>\n");
    
    // Formsets - الجداول التابعة
    for (int i = 0; i < formsets.size(); i++) {
        JSONObject formset = formsets.get(i);
        String childModel = formset.getString("child_model_name");
        String prefix = formset.optString("prefix", childModel.toLowerCase());
        String formsetVar = childModel.toLowerCase() + "_formset";
        
        content.append("        <div class=\"card border-secondary mb-4 shadow-sm\">\n");
        content.append("            <div class=\"card-header bg-secondary text-white d-flex justify-content-between align-items-center\">\n");
        content.append("                <h5 class=\"mb-0\"><i class=\"fas fa-list me-2\"></i>").append(childModel).append("</h5>\n");
        content.append("                <button type=\"button\" class=\"btn btn-sm btn-light add-formset-row\" data-formset-prefix=\"").append(prefix).append("\">\n");
        content.append("                    <i class=\"fas fa-plus me-1\"></i>{% trans 'إضافة سطر' %}\n");
        content.append("                </button>\n");
        content.append("            </div>\n");
        content.append("            <div class=\"card-body p-0\">\n");
        content.append("                {{ ").append(formsetVar).append(".management_form }}\n");
        content.append("                <div class=\"table-responsive\">\n");
        content.append("                    <table class=\"table table-hover align-middle mb-0\">\n");
        content.append("                        <thead class=\"table-light\">\n");
        content.append("                            <tr>\n");
        content.append("                                {% for field in ").append(formsetVar).append(".empty_form %}\n");
        content.append("                                    {% if not field.is_hidden %}<th>{{ field.label }}</th>{% endif %}\n");
        content.append("                                {% endfor %}\n");
        content.append("                                <th class=\"text-center\">{% trans 'إجراءات' %}</th>\n");
        content.append("                            </tr>\n");
        content.append("                        </thead>\n");
        content.append("                        <tbody id=\"id_").append(prefix).append("-forms\">\n");
        content.append("                            {% for subform in ").append(formsetVar).append(" %}\n");
        content.append("                            <tr class=\"formset-row\">\n");
        content.append("                                {% for field in subform %}\n");
        content.append("                                    {% if field.is_hidden %}{{ field }}{% else %}\n");
        content.append("                                        <td>{{ field|as_crispy_field }}</td>\n");
        content.append("                                    {% endif %}\n");
        content.append("                                {% endfor %}\n");
        content.append("                                <td class=\"text-center\">\n");
        content.append("                                    {% if subform.instance.pk %}{{ subform.DELETE|as_crispy_field }}\n");
        content.append("                                    {% else %}<button type=\"button\" class=\"btn btn-outline-danger btn-sm remove-formset-row\"><i class=\"fas fa-trash\"></i></button>{% endif %}\n");
        content.append("                                </td>\n");
        content.append("                            </tr>\n");
        content.append("                            {% endfor %}\n");
        content.append("                        </tbody>\n");
        content.append("                    </table>\n");
        content.append("                </div>\n");
        content.append("            </div>\n");
        content.append("        </div>\n");

        // إضافة الـ Empty Form (مخفي) ليستخدمه الـ JS
        content.append("        <script type=\"text/html\" id=\"").append(prefix).append("-empty-form\">\n");
        content.append("            <tr class=\"formset-row\">\n");
        content.append("                {% for field in ").append(formsetVar).append(".empty_form %}\n");
        content.append("                    {% if field.is_hidden %}{{ field }}{% else %}<td>{{ field|as_crispy_field }}</td>{% endif %}\n");
        content.append("                {% endfor %}\n");
        content.append("                <td class=\"text-center\"><button type=\"button\" class=\"btn btn-outline-danger btn-sm remove-formset-row\"><i class=\"fas fa-trash\"></i></button></td>\n");
        content.append("            </tr>\n");
        content.append("        </script>\n");
    }

    content.append("        <div class=\"row mt-4\">\n");
    content.append("            <div class=\"col-12 d-flex justify-content-end gap-2\">\n");
    content.append("                <a href=\"{% url '").append(appName).append(":").append(lowerModel).append("_list' %}\" class=\"btn btn-outline-secondary px-4\">{% trans 'إلغاء' %}</a>\n");
    content.append("                <button type=\"submit\" class=\"btn btn-success px-5\"><i class=\"fas fa-save me-1\"></i>{% trans 'حفظ الكل' %}</button>\n");
    content.append("            </div>\n");
    content.append("        </div>\n");
    content.append("    </form>\n");
    content.append("</div>\n");
    content.append("{% endblock %}\n");

    content.append("{% block extra_css %}\n");
    content.append("<style>\n");
    content.append("    .table .mb-3 { margin-bottom: 0 !important; }\n");
    content.append("    .table .form-group { margin-bottom: 0 !important; }\n");
    content.append("    .asteriskField { display: none; }\n");
    content.append("    .deleted-row { background-color: #f8d7da !important; opacity: 0.6; }\n");
    content.append("</style>\n");
    content.append("{% endblock %}\n");

    content.append("{% block extra_js %}\n");
    content.append("{% include '").append(appName).append("/includes/formset_js.html' %}\n");
    content.append("{% endblock %}\n");

    writeFile(formTemplate, content.toString());
}


    
    private void createFormsetManagementTemplate(String appName, String parentModel, List<JSONObject> formsets) throws Exception {
        File managementTemplate = new File(projectDirectory, "templates/" + appName + "/includes/" + parentModel.toLowerCase() + "_formset_management.html");
        
        StringBuilder content = new StringBuilder();
        content.append("<!-- ").append(parentModel.toLowerCase()).append("_formset_management.html -->\n");
        content.append("<!-- إدارة Formsets لـ ").append(parentModel).append(" -->\n");
        content.append("{% load i18n %}\n");
        content.append("\n");
        content.append("<div class=\"formset-management card border-info mb-3\">\n");
        content.append("    <div class=\"card-header bg-info text-white\">\n");
        content.append("        <h5 class=\"mb-0\"><i class=\"fas fa-cogs me-2\"></i>{% trans 'إدارة البيانات المرتبطة' %}</h5>\n");
        content.append("    </div>\n");
        content.append("    <div class=\"card-body\">\n");
        content.append("        <div class=\"row\">\n");
        
        for (JSONObject formset : formsets) {
            String childModel = formset.getString("child_model_name");
            String formsetVar = childModel.toLowerCase() + "_formset";
            
            content.append("            <div class=\"col-md-6 mb-3\">\n");
            content.append("                <div class=\"card\">\n");
            content.append("                    <div class=\"card-body\">\n");
            content.append("                        <h6 class=\"card-title\">").append(childModel).append("</h6>\n");
            content.append("                        <p class=\"card-text small text-muted\">\n");
            content.append("                            {% trans 'عدد السجلات:' %} <span id=\"").append(childModel.toLowerCase()).append("-count\">{{ ").append(formsetVar).append(".total_form_count }}</span>\n");
            content.append("                        </p>\n");
            content.append("                        <button type=\"button\" class=\"btn btn-sm btn-outline-primary add-formset-row\" \n");
            content.append("                                data-formset-prefix=\"").append(formset.optString("prefix", childModel.toLowerCase())).append("\"\n");
            content.append("                                data-formset-name=\"").append(childModel).append("\">\n");
            content.append("                            <i class=\"fas fa-plus\"></i> {% trans 'إضافة' %}\n");
            content.append("                        </button>\n");
            content.append("                    </div>\n");
            content.append("                </div>\n");
            content.append("            </div>\n");
        }
        
        content.append("        </div>\n");
        content.append("    </div>\n");
        content.append("</div>\n");
        
        writeFile(managementTemplate, content.toString());
    }
    
    private void createGlobalFormsetTemplate() throws Exception {
        File globalTemplate = new File(projectDirectory, "templates/includes/formset_global.html");
        
        String content = "<!-- formset_global.html -->\n" +
                        "<!-- قالب عالمي لل Formsets -->\n" +
                        "{% load static %}\n" +
                        "\n" +
                        "<div class=\"formset-global-help alert alert-info mt-3\">\n" +
                        "    <h5><i class=\"fas fa-question-circle me-2\"></i>كيفية استخدام Formsets</h5>\n" +
                        "    <ul class=\"mb-0\">\n" +
                        "        <li>استخدم زر <i class=\"fas fa-plus\"></i> لإضافة سجلات جديدة</li>\n" +
                        "        <li>استخدم زر <i class=\"fas fa-times\"></i> لحذف السجلات</li>\n" +
                        "        " +
                        "        <li>يمكنك إضافة عدة سجلات دفعة واحدة</li>\n" +
                        "        <li>سيتم حفظ جميع البيانات معاً عند النقر على 'حفظ الكل'</li>\n" +
                        "    </ul>\n" +
                        "</div>\n";
        
        writeFile(globalTemplate, content);
    }
    
    private void createFormsetDocumentation() throws Exception {
        File docsDir = new File(projectDirectory, "docs/formsets");
        if (!docsDir.exists()) {
            docsDir.mkdirs();
        }
        
        File readmeFile = new File(docsDir, "README.md");
        
        StringBuilder content = new StringBuilder();
        content.append("# Django Formsets Documentation\n");
        content.append("\n");
        content.append("تم إنشاء هذا التوثيق تلقائياً بواسطة Django Formset Generator.\n");
        content.append("\n");
        
        if (projectData.has("formsets")) {
            JSONArray formsets = projectData.getJSONArray("formsets");
            
            content.append("## Formsets المُنشأة\n");
            content.append("\n");
            content.append("| # | النموذج الرئيسي | النموذج الفرعي | التطبيق | Prefix | حقول إضافية |\n");
            content.append("|---|-----------------|----------------|---------|--------|-------------|\n");
            
            for (int i = 0; i < formsets.length(); i++) {
                JSONObject formset = formsets.getJSONObject(i);
                
                content.append("| ").append(i+1).append(" | ");
                content.append(formset.getString("parent_model_name")).append(" | ");
                content.append(formset.getString("child_model_name")).append(" | ");
                content.append(formset.getString("parent_app_name")).append(" | ");
                content.append("`").append(formset.optString("prefix", "default")).append("` | ");
                content.append(formset.optInt("extra_fields", 1)).append(" |\n");
            }
            
            content.append("\n");
            content.append("## كيفية الاستخدام\n");
            content.append("\n");
            content.append("### 1. إنشاء سجل جديد مع Formsets\n");
            content.append("```python\n");
            content.append("from django.shortcuts import render, redirect\n");
            content.append("from django.forms import inlineformset_factory\n");
            content.append("from .models import ParentModel, ChildModel\n");
            content.append("from .forms import ParentModelForm, ChildModelForm\n");
            content.append("\n");
            content.append("def create_with_formsets(request):\n");
            content.append("    if request.method == 'POST':\n");
            content.append("        # ...\n");
            content.append("    # ...\n");
            content.append("```\n");
            content.append("\n");
            content.append("### 2. تحديث سجل مع Formsets\n");
            content.append("```python\n");
            content.append("def update_with_formsets(request, pk):\n");
            content.append("    parent_obj = ParentModel.objects.get(pk=pk)\n");
            content.append("    ChildFormSet = inlineformset_factory(ParentModel, ChildModel, form=ChildModelForm, extra=1)\n");
            content.append("    \n");
            content.append("    if request.method == 'POST':\n");
            content.append("        form = ParentModelForm(request.POST, instance=parent_obj)\n");
            content.append("        formset = ChildFormSet(request.POST, instance=parent_obj)\n");
            content.append("        # ...\n");
            content.append("```\n");
            content.append("\n");
            content.append("### 3. الروابط المتاحة\n");
            content.append("\n");
            
            // جمع الروابط الفريدة
            Set<String> urls = new HashSet<>();
            for (int i = 0; i < formsets.length(); i++) {
                JSONObject formset = formsets.getJSONObject(i);
                String parentModel = formset.getString("parent_model_name").toLowerCase();
                String appName = formset.getString("parent_app_name");
                
                urls.add("- `/" + appName + "/" + parentModel + "/create-with-formsets/` - إنشاء جديد");
                urls.add("- `/" + appName + "/" + parentModel + "/<id>/update-with-formsets/` - تحديث");
                urls.add("- `/" + appName + "/" + parentModel + "/add-formset-row/<formset_name>/` - إضافة صف عبر AJAX");
            }
            
            for (String url : urls) {
                content.append(url).append("\n");
            }
        }
        
        writeFile(readmeFile, content.toString());
        Log.i("FormsetGenerator", "Created formset documentation");
    }
    
    private void createFormsetJavaScript() throws Exception {
        File jsDir = new File(projectDirectory, "static/js/formsets");
        if (!jsDir.exists()) {
            jsDir.mkdirs();
        }
       Assets assetHelper = new Assets(context, "formset_manager.js", jsDir.getAbsolutePath());
      if (assetHelper.copyAssetToPath()) {
    // تم النسخ بنجاح
       }
        Log.i("FormsetGenerator", "Created formset JavaScript file");
    }
    
    private String readFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    
    private void writeFile(File file, String content) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
}