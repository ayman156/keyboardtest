package ayman.djangogenerator;
import ayman.djangogenerator.Template;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
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
import android.content.Intent;
import android.content.Context;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import java.util.List;
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
import android.view.View;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;
import com.google.android.material.appbar.AppBarLayout;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
public class ReportDesignerrActivity extends AppCompatActivity {
    
    // UI Components
    private Spinner modelSpinner, templateSpinner, reportTypeSpinner;
    private LinearLayout annotateContainer, relatedContainer, filterContainer, 
                       templateFieldsContainer, advancedOptionsContainer;
    private Button btnSaveReport, btnAddAnnotate, btnAddFilter, btnGenerateFullCode;
    private Button btnExportExcel, btnExportPDF, btnPreviewTemplate, btnSaveTemplate;
    private Button btnLoadSavedReports, btnSaveToDB, btnAddTemplateField;
    private EditText etReportName, etReportDescription, etLimit, etOffset;
    private EditText etTemplateName, etTemplateDescription;
    private RecyclerView rvSelectedFields, rvTemplates;
    private CheckBox cbGroupBy, cbOrderBy, cbDistinct, cbIncludeSubtotals;
    private RadioGroup rgChartType;
    private TextView tvReportId, tvCreatedDate;
    
    // Data
    private DatabaseHelper dbHelper;
    private long projectId;
    private long currentReportId = -1;
    private List<ModelObj> availableModels;
    private List<Field> selectedFields;
   // private List<Template> availableTemplates;
   // private List<TemplateField> templateFields;
    private List<Template> availableTemplates;  // تغيير النوع
private List<Template> templateFields;      // إذا كنت تستخدمها

    private FieldsAdapter fieldsAdapter;
    private TemplatesAdapter templatesAdapter;
    private ReportConfig currentConfig;
    
    // Classes
    static class ReportConfig {
        long id;
        String name;
        String description;
        long modelId;
        String modelName;
        String reportType; // "table", "chart", "summary"
        String chartType; // "bar", "line", "pie", "column"
        boolean includeSubtotals;
        boolean includeCharts;
        boolean includeSummary;
        Map<String, Object> settings;
        List<FilterConfig> filters;
        List<AnnotateConfig> annotates;
        List<TemplateField> templateFields;
        String customTemplate;
        Date createdAt;
        Date updatedAt;
        
        ReportConfig() {
            settings = new HashMap<>();
            filters = new ArrayList<>();
            annotates = new ArrayList<>();
            templateFields = new ArrayList<>();
        }
    }
    
    static class FilterConfig {
        String field;
        String operator;
        String value;
        String value2; // للفلاتر المتقدمة مثل BETWEEN
        String type; // "basic", "date_range", "choice", "number_range", "custom"
        Map<String, String> options;
        
        FilterConfig() {
            options = new HashMap<>();
        }
    }
    
    static class AnnotateConfig {
        String function;
        String alias;
        String field;
        String expression; // للتجميعات المخصصة
    }
    
    
    static class TemplateField {
        String name;
        String displayName;
        String type; // "text", "number", "date", "datetime", "select", "checkbox", "range"
        Map<String, String> options;
        boolean required;
        String defaultValue;
        int order;
        
        TemplateField() {
            options = new HashMap<>();
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_designer);
        
        // Initialize
        dbHelper = new DatabaseHelper(this);
        projectId = getIntent().getLongExtra("PROJECT_ID", -1);
        currentReportId = getIntent().getLongExtra("REPORT_ID", -1);
        currentConfig = new ReportConfig();
        
        // Setup UI
        initViews();
        setupViews();
        loadModels();
        loadTemplates();
        
        // Load report if editing
        if (currentReportId != -1) {
            loadReport(currentReportId);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.report_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.menu_manage_templates) {
            manageTemplates();
            return true;
        } else if (id == R.id.menu_export_config) {
            exportReportConfig();
            return true;
        } else if (id == R.id.menu_import_config) {
            importReportConfig();
            return true;
        } else if (id == R.id.menu_clear_all) {
            clearAll();
            return true;
        } else if (id == R.id.menu_advanced_settings) {
            showAdvancedSettings();
            return true;
        } else if (id == R.id.menu_generate_api) {
            generateAPI();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void initViews() {
        // Report Info
        etReportName = findViewById(R.id.et_report_name);
        etReportDescription = findViewById(R.id.et_report_description);
        tvReportId = findViewById(R.id.tv_report_id);
        tvCreatedDate = findViewById(R.id.tv_created_date);
        
        // Model Selection
        modelSpinner = findViewById(R.id.model_spinner);
        reportTypeSpinner = findViewById(R.id.report_type_spinner);
        
        // Template Selection
        templateSpinner = findViewById(R.id.template_spinner);
        etTemplateName = findViewById(R.id.et_template_name);
        etTemplateDescription = findViewById(R.id.et_template_description);
        
        // Fields
        rvSelectedFields = findViewById(R.id.rv_selected_fields);
        
        // Containers
        annotateContainer = findViewById(R.id.annotate_container);
        relatedContainer = findViewById(R.id.related_container);
        filterContainer = findViewById(R.id.filter_container);
        templateFieldsContainer = findViewById(R.id.template_fields_container);
        advancedOptionsContainer = findViewById(R.id.advanced_options_container);
        
        // Checkboxes
        cbGroupBy = findViewById(R.id.cb_group_by);
        cbOrderBy = findViewById(R.id.cb_order_by);
        cbDistinct = findViewById(R.id.cb_distinct);
        cbIncludeSubtotals = findViewById(R.id.cb_include_subtotals);
        
        // Chart Type
        rgChartType = findViewById(R.id.rg_chart_type);
        
        // EditTexts
        etLimit = findViewById(R.id.et_limit);
        etOffset = findViewById(R.id.et_offset);
        
        // Buttons
        btnSaveReport = findViewById(R.id.btn_save_report);
        btnAddAnnotate = findViewById(R.id.btn_add_annotate);
        btnAddFilter = findViewById(R.id.btn_add_filter);
        btnGenerateFullCode = findViewById(R.id.btn_generate_full_code);
        btnExportExcel = findViewById(R.id.btn_export_excel);
        btnExportPDF = findViewById(R.id.btn_export_pdf);
        btnPreviewTemplate = findViewById(R.id.btn_preview_template);
        btnSaveTemplate = findViewById(R.id.btn_save_template);
        btnLoadSavedReports = findViewById(R.id.btn_load_saved_reports);
        btnSaveToDB = findViewById(R.id.btn_save_to_db);
        btnAddTemplateField = findViewById(R.id.btn_add_template_field);
        
        // Templates RecyclerView
        rvTemplates = findViewById(R.id.rv_templates);
    }
    
    private void setupViews() {
        // Setup Report Type Spinner
        ArrayAdapter<CharSequence> reportTypeAdapter = ArrayAdapter.createFromResource(
            this, R.array.report_types, android.R.layout.simple_spinner_item);
        reportTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reportTypeSpinner.setAdapter(reportTypeAdapter);
        
        // Setup Fields RecyclerView
        selectedFields = new ArrayList<>();
        fieldsAdapter = new FieldsAdapter(this, selectedFields);
        rvSelectedFields.setLayoutManager(new LinearLayoutManager(this));
        rvSelectedFields.setAdapter(fieldsAdapter);
        
        // Setup Templates RecyclerView
        availableTemplates = new ArrayList<>();
        templatesAdapter = new TemplatesAdapter(this, availableTemplates);
        rvTemplates.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvTemplates.setAdapter(templatesAdapter);
        templatesAdapter.setOnTemplateClickListener(template -> loadTemplate(template.getId()));
        
        // Setup listeners
        setupListeners();
        
        // Setup model spinner listener
        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (availableModels != null && position < availableModels.size()) {
                    long modelId = availableModels.get(position).id;
                    loadModelRelations(modelId);
                    updateFieldsList(modelId);
                    updateCurrentConfig();
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Setup report type listener
        reportTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateReportTypeUI();
                updateCurrentConfig();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Setup template spinner listener
        templateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && availableTemplates != null && position - 1 < availableTemplates.size()) {
                    //loadTemplate(availableTemplates.get(position - 1).id);
                }
                updateCurrentConfig();
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Setup field click listener
        fieldsAdapter.setOnFieldClickListener(position -> {
            Field field = selectedFields.get(position);
            showAdvancedFieldOptionsDialog(field);
        });
    }
    
    private void setupListeners() {
        btnAddAnnotate.setOnClickListener(v -> showAnnotateTypeDialog());
        btnAddFilter.setOnClickListener(v -> showAdvancedFilterDialog());
        btnSaveReport.setOnClickListener(v -> saveReport());
        btnGenerateFullCode.setOnClickListener(v -> generateFullDjangoCode());
        btnSaveToDB.setOnClickListener(v -> saveReportToDatabase());
        btnLoadSavedReports.setOnClickListener(v -> showSavedReportsDialog());
        
        btnExportExcel.setOnClickListener(v -> generateExcelCode());
        btnExportPDF.setOnClickListener(v -> generatePdfCode());
        btnPreviewTemplate.setOnClickListener(v -> previewHtmlTemplate());
        
        btnSaveTemplate.setOnClickListener(v -> saveTemplate());
        btnAddTemplateField.setOnClickListener(v -> addTemplateField());
        
        // Chart type listener
        rgChartType.setOnCheckedChangeListener((group, checkedId) -> {
            updateCurrentConfig();
        });
    }
    
private void loadTemplates() {
    availableTemplates = dbHelper.getTemplates(projectId);
    
    // إذا كانت الدالة getTemplates لا ترجع List<Template>، فعدل DatabaseHelper
    if (availableTemplates == null || availableTemplates.isEmpty()) {
        availableTemplates = new ArrayList<>();
        
        // إضافة قالب افتراضي
        Template defaultTemplate = new Template();
        defaultTemplate.setId(-1);
        defaultTemplate.setName("قالب جدول افتراضي");
        defaultTemplate.setDescription("قالب جدول أساسي");
        defaultTemplate.setSystem(true);
        availableTemplates.add(defaultTemplate);
    }
    
    // تحديث Spinner
    List<String> templateNames = new ArrayList<>();
    templateNames.add("-- اختر قالب --");
    for (Template template : availableTemplates) {
        templateNames.add(template.getName());
    }
    
    ArrayAdapter<String> adapter = new ArrayAdapter<>(
        this, android.R.layout.simple_spinner_item, templateNames
    );
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    templateSpinner.setAdapter(adapter);
    
    // تحديث Adapter
    if (templatesAdapter != null) {
        templatesAdapter.notifyDataSetChanged();
    }
}

// وفي دالة saveTemplate:
private void saveTemplate() {
    String name = etTemplateName.getText().toString().trim();
    String description = etTemplateDescription.getText().toString().trim();
    
    if (name.isEmpty()) {
        Toast.makeText(this, "يرجى إدخال اسم القالب", Toast.LENGTH_SHORT).show();
        return;
    }
    
    Template template = new Template();
    template.setName(name);
    template.setDescription(description);
    template.setHtmlContent(generateTemplateHtml());
    template.setCssContent(generateTemplateCss());
    template.setJsContent(generateTemplateJs());
    template.setSystem(false);
    template.setProjectId(projectId);
    
    long templateId = dbHelper.saveTemplate(template);
    if (templateId != -1) {
        Toast.makeText(this, "تم حفظ القالب بنجاح", Toast.LENGTH_SHORT).show();
        loadTemplates();
    } else {
        Toast.makeText(this, "فشل حفظ القالب", Toast.LENGTH_SHORT).show();
    }
}
    //$
    private void addSearchFilterRow() {
    // فلتر البحث
    LinearLayout row = createFormRow();
    
    addLabel(row, "حقل البحث:");
    EditText etField = new EditText(this);
    etField.setHint("اسم الحقل");
    row.addView(etField);
    
    addLabel(row, "نص البحث:");
    EditText etSearch = new EditText(this);
    etSearch.setHint("أدخل نص البحث");
    row.addView(etSearch);
    
    addLabel(row, "نوع البحث:");
    Spinner searchTypeSpinner = new Spinner(this);
    String[] searchTypes = {"يحتوي على", "يبدأ بـ", "ينتهي بـ", "يساوي تماماً"};
    ArrayAdapter<String> searchTypeAdapter = new ArrayAdapter<>(
        this, android.R.layout.simple_spinner_item, searchTypes
    );
    searchTypeSpinner.setAdapter(searchTypeAdapter);
    row.addView(searchTypeSpinner);
    
    addRemoveButton(row);
    filterContainer.addView(row);
}

private void addCustomFilterRow() {
    // فلتر مخصص
    LinearLayout row = createFormRow();
    
    addLabel(row, "التعبير المخصص (Q object):");
    EditText etExpression = new EditText(this);
    etExpression.setHint("مثال: Q(amount__gt=100) & Q(status='active')");
    etExpression.setLines(3);
    row.addView(etExpression);
    
    addRemoveButton(row);
    filterContainer.addView(row);
}

private void addRelatedFilterRow() {
    // فلتر مرتبط
    LinearLayout row = createFormRow();
    
    addLabel(row, "النموذج المرتبط:");
    EditText etRelatedModel = new EditText(this);
    etRelatedModel.setHint("اسم النموذج المرتبط");
    row.addView(etRelatedModel);
    
    addLabel(row, "حقل النموذج المرتبط:");
    EditText etRelatedField = new EditText(this);
    etRelatedField.setHint("اسم الحقل في النموذج المرتبط");
    row.addView(etRelatedField);
    
    addLabel(row, "المعامل:");
    Spinner operatorSpinner = new Spinner(this);
    String[] operators = {"=", "!=", ">", "<", "contains"};
    ArrayAdapter<String> operatorAdapter = new ArrayAdapter<>(
        this, android.R.layout.simple_spinner_item, operators
    );
    operatorSpinner.setAdapter(operatorAdapter);
    row.addView(operatorSpinner);
    
    addLabel(row, "القيمة:");
    EditText etValue = new EditText(this);
    etValue.setHint("القيمة");
    row.addView(etValue);
    
    addRemoveButton(row);
    filterContainer.addView(row);
}

private void addTimeRangeFilterRow() {
    // فلتر نطاق زمني
    LinearLayout row = createFormRow();
    
    addLabel(row, "حقل الوقت:");
    EditText etTimeField = new EditText(this);
    etTimeField.setHint("اسم حقل الوقت");
    row.addView(etTimeField);
    
    addLabel(row, "من وقت:");
    EditText etFromTime = new EditText(this);
    etFromTime.setHint("HH:MM");
    row.addView(etFromTime);
    
    addLabel(row, "إلى وقت:");
    EditText etToTime = new EditText(this);
    etToTime.setHint("HH:MM");
    row.addView(etToTime);
    
    addRemoveButton(row);
    filterContainer.addView(row);
}
    private void loadModels() {
        availableModels = dbHelper.getModelsObjectsByProject(projectId);
        
        if (availableModels == null || availableModels.isEmpty()) {
            Toast.makeText(this, "لا توجد نماذج في هذا المشروع", Toast.LENGTH_SHORT).show();
            return;
        }
        
        List<String> modelNames = new ArrayList<>();
        for (ModelObj model : availableModels) {
            modelNames.add(model.name);
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, modelNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelSpinner.setAdapter(adapter);
    }
    
    private void loadTemplate(long templateId) {
        Template template = dbHelper.getTemplateById(templateId);
        if (template != null) {
            etTemplateName.setText(template.getName());
            etTemplateDescription.setText(template.getDescription());
            currentConfig.customTemplate = template.getHtmlContent();
        }
    }
    
    private void updateFieldsList(long modelId) {
        selectedFields.clear();
        
        List<Field> fields = dbHelper.getFieldsByModelId(modelId);
        if (fields != null && !fields.isEmpty()) {
            selectedFields.addAll(fields);
        } else {
            createSampleFields();
        }
        
        fieldsAdapter.notifyDataSetChanged();
    }
    
    private void createSampleFields() {
        String[] fieldNames = {"id", "name", "created_at", "status", "amount", "category", "user_id"};
        String[] fieldTypes = {"IntegerField", "CharField", "DateTimeField", "CharField", "DecimalField", "ForeignKey", "ForeignKey"};
        
        for (int i = 0; i < fieldNames.length; i++) {
            Field field = new Field(i + 1, fieldNames[i], fieldTypes[i]);
            field.setDisplayName(getArabicFieldName(fieldNames[i]));
            field.setIncludeInReport(true);
            selectedFields.add(field);
        }
    }
    
    private void loadModelRelations(long modelId) {
        relatedContainer.removeAllViews();
        
        List<Field> relations = dbHelper.getRelationsByModelId(modelId);
        if (relations == null || relations.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("لا توجد علاقات مرتبطة");
            tv.setTextColor(Color.GRAY);
            relatedContainer.addView(tv);
            return;
        }
        
        for (Field field : relations) {
            CardView card = new CardView(this);
            card.setCardBackgroundColor(Color.parseColor("#f8f9fa"));
            card.setRadius(8);
            card.setCardElevation(2);
            
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(16, 16, 16, 16);
            
            CheckBox cb = new CheckBox(this);
            cb.setText(field.getName());
            cb.setTag(field);
            
            TextView tvType = new TextView(this);
            tvType.setText(" (" + field.getType() + ")");
            tvType.setTextColor(Color.GRAY);
            tvType.setTextSize(12);
            
            row.addView(cb);
            row.addView(tvType);
            card.addView(row);
            relatedContainer.addView(card);
            
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) card.getLayoutParams();
            params.setMargins(0, 0, 0, 8);
            card.setLayoutParams(params);
        }
    }
    
    private void updateReportTypeUI() {
        String reportType = reportTypeSpinner.getSelectedItem().toString();
        
        // Hide/show chart options
        if (reportType.equals("مخطط")) {
            findViewById(R.id.chart_options_container).setVisibility(View.VISIBLE);
            cbIncludeSubtotals.setVisibility(View.GONE);
        } else if (reportType.equals("جدول مع إجماليات")) {
            findViewById(R.id.chart_options_container).setVisibility(View.GONE);
            cbIncludeSubtotals.setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.chart_options_container).setVisibility(View.GONE);
            cbIncludeSubtotals.setVisibility(View.GONE);
        }
    }
    
    private void showAdvancedFieldOptionsDialog(Field field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("خيارات متقدمة للحقل: " + field.getDisplayName());
        
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        
        // Basic Info
        addSectionTitle(layout, "المعلومات الأساسية");
        
        addLabelAndEditText(layout, "اسم العرض:", field.getDisplayName(), text -> {
            field.setDisplayName(text);
        });
        
        // Report Options
        addSectionTitle(layout, "خيارات التقرير");
        
        CheckBox cbInclude = addCheckBox(layout, "تضمين في التقرير", 
            field.isIncludeInReport(), checked -> {
                field.setIncludeInReport(checked);
            });
        
        CheckBox cbGroupBy = addCheckBox(layout, "استخدام في Group By", 
            field.isGroupBy(), checked -> {
                field.setGroupBy(checked);
            });
        
        CheckBox cbOrderBy = addCheckBox(layout, "استخدام في Order By", 
            field.isOrderBy(), checked -> {
                field.setOrderBy(checked);
            });
        
        // Order Direction
        addLabel(layout, "اتجاه الترتيب:");
        Spinner spOrderDir = new Spinner(this);
        String[] directions = {"تصاعدي (ASC)", "تنازلي (DESC)"};
        ArrayAdapter<String> dirAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, directions
        );
        spOrderDir.setAdapter(dirAdapter);
        spOrderDir.setSelection(field.getOrderDirection().equals("DESC") ? 1 : 0);
        spOrderDir.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                field.setOrderDirection(position == 1 ? "DESC" : "ASC");
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        layout.addView(spOrderDir);
        
        // Advanced Options
        addSectionTitle(layout, "خيارات متقدمة");
        
        addLabelAndEditText(layout, "تنسيق العرض (مثال: %.2f):", 
            field.getFormat() != null ? field.getFormat() : "", text -> {
                field.setFormat(text.isEmpty() ? null : text);
            });
        
        addLabelAndEditText(layout, "شرط عرض (Python expression):", 
            field.getCondition() != null ? field.getCondition() : "", text -> {
                field.setCondition(text.isEmpty() ? null : text);
            });
        
        // Aggregate Functions
        addSectionTitle(layout, "دوال التجميع");
        
        String[] aggregates = {"", "sum", "avg", "count", "min", "max"};
        Spinner spAggregate = new Spinner(this);
        ArrayAdapter<String> aggAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, aggregates
        );
        spAggregate.setAdapter(aggAdapter);
        layout.addView(spAggregate);
        
        scrollView.addView(layout);
        builder.setView(scrollView);
        
        builder.setPositiveButton("حفظ", (dialog, which) -> {
            // Save all changes
            fieldsAdapter.notifyDataSetChanged();
            updateCurrentConfig();
        });
        
        builder.setNegativeButton("إلغاء", null);
        
        builder.setNeutralButton("إعادة تعيين", (dialog, which) -> {
            field.reset();
            fieldsAdapter.notifyDataSetChanged();
        });
        
        builder.show();
    }
    
    private void showAnnotateTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("اختر نوع التجميع");
        
        String[] types = {
            "تجميع بسيط (Count, Sum, Avg, etc.)",
            "تجميع مخصص (Expression)",
            "حساب نسبة مئوية",
            "تجميع شرطي (Case When)"
        };
        
        builder.setItems(types, (dialog, which) -> {
            switch (which) {
                case 0: addAnnotateRow(); break;
                case 1: addCustomAnnotateRow(); break;
                case 2: addPercentageAnnotateRow(); break;
                case 3: addConditionalAnnotateRow(); break;
            }
        });
        
        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }
    
    private void addAnnotateRow() {
        LinearLayout row = createFormRow();
        
        // Function
        addLabel(row, "الدالة:");
        Spinner funcSpinner = new Spinner(this);
        String[] functions = {"Count", "Sum", "Avg", "Max", "Min", "StdDev", "Variance"};
        ArrayAdapter<String> funcAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, functions
        );
        funcSpinner.setAdapter(funcAdapter);
        row.addView(funcSpinner);
        
        // Alias
        addLabel(row, "اسم النتيجة:");
        EditText etAlias = new EditText(this);
        etAlias.setHint("مثال: total_count");
        row.addView(etAlias);
        
        // Field
        addLabel(row, "اسم الحقل:");
        EditText etField = new EditText(this);
        etField.setHint("مثال: id");
        row.addView(etField);
        
        // Distinct
        CheckBox cbDistinct = new CheckBox(this);
        cbDistinct.setText("مميز (Distinct)");
        row.addView(cbDistinct);
        
        addRemoveButton(row);
        annotateContainer.addView(row);
    }
    
    private void addCustomAnnotateRow() {
        LinearLayout row = createFormRow();
        
        // Expression
        addLabel(row, "التعبير (Python):");
        EditText etExpression = new EditText(this);
        etExpression.setHint("مثال: F('amount') * F('quantity')");
        etExpression.setLines(2);
        row.addView(etExpression);
        
        // Alias
        addLabel(row, "اسم النتيجة:");
        EditText etAlias = new EditText(this);
        etAlias.setHint("مثال: total_value");
        row.addView(etAlias);
        
        // Output Type
        addLabel(row, "نوع الناتج:");
        Spinner typeSpinner = new Spinner(this);
        String[] types = {"DecimalField", "IntegerField", "FloatField", "CharField"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, types
        );
        typeSpinner.setAdapter(typeAdapter);
        row.addView(typeSpinner);
        
        addRemoveButton(row);
        annotateContainer.addView(row);
    }
    
    private void addPercentageAnnotateRow() {
        LinearLayout row = createFormRow();
        
        // Numerator
        addLabel(row, "البسط (الحقل أو التعبير):");
        EditText etNumerator = new EditText(this);
        etNumerator.setHint("مثال: F('approved_count')");
        row.addView(etNumerator);
        
        // Denominator
        addLabel(row, "المقام (الحقل أو التعبير):");
        EditText etDenominator = new EditText(this);
        etDenominator.setHint("مثال: F('total_count')");
        row.addView(etDenominator);
        
        // Alias
        addLabel(row, "اسم النتيجة:");
        EditText etAlias = new EditText(this);
        etAlias.setHint("مثال: approval_rate");
        row.addView(etAlias);
        
        // Format
        addLabel(row, "التنسيق:");
        Spinner formatSpinner = new Spinner(this);
        String[] formats = {"نسبة مئوية (٪)", "كسر عشري", "قيمة من 100"};
        ArrayAdapter<String> formatAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, formats
        );
        formatSpinner.setAdapter(formatAdapter);
        row.addView(formatSpinner);
        
        addRemoveButton(row);
        annotateContainer.addView(row);
    }
    
    private void addConditionalAnnotateRow() {
        LinearLayout row = createFormRow();
        
        // Condition
        addLabel(row, "الشرط (Python expression):");
        EditText etCondition = new EditText(this);
        etCondition.setHint("مثال: Q(status='active')");
        etCondition.setLines(2);
        row.addView(etCondition);
        
        // True Value
        addLabel(row, "القيمة إذا تحقق الشرط:");
        EditText etTrueValue = new EditText(this);
        etTrueValue.setHint("مثال: 1");
        row.addView(etTrueValue);
        
        // False Value
        addLabel(row, "القيمة إذا لم يتحقق الشرط:");
        EditText etFalseValue = new EditText(this);
        etFalseValue.setHint("مثال: 0");
        row.addView(etFalseValue);
        
        // Alias
        addLabel(row, "اسم النتيجة:");
        EditText etAlias = new EditText(this);
        etAlias.setHint("مثال: is_active_count");
        row.addView(etAlias);
        
        addRemoveButton(row);
        annotateContainer.addView(row);
    }
    
    private void showAdvancedFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("اختر نوع الفلتر المتقدم");
        
        String[] filterTypes = {
            "فلتر عادي",
            "بين تاريخين",
            "اختيار من متعدد",
            "نطاق أرقام",
            "بحث نصي",
            "فلتر مخصص (Expression)",
            "فلتر مرتبط (Related Field)",
            "فلتر زمني (Time Range)"
        };
        
        builder.setItems(filterTypes, (dialog, which) -> {
            switch (which) {
                case 0: addBasicFilterRow(); break;
                case 1: addDateRangeFilterRow(); break;
                case 2: addChoiceFilterRow(); break;
                case 3: addNumberRangeFilterRow(); break;
                case 4: addSearchFilterRow(); break;
                case 5: addCustomFilterRow(); break;
                case 6: addRelatedFilterRow(); break;
                case 7: addTimeRangeFilterRow(); break;
            }
        });
        
        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }
    
    private void addBasicFilterRow() {
        LinearLayout row = createFormRow();
        
        // Field Selection
        addLabel(row, "اختر الحقل:");
        Spinner fieldSpinner = new Spinner(this);
        List<String> fieldNames = new ArrayList<>();
        for (Field field : selectedFields) {
            fieldNames.add(field.getName() + " (" + field.getDisplayName() + ")");
        }
        ArrayAdapter<String> fieldAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, fieldNames
        );
        fieldSpinner.setAdapter(fieldAdapter);
        row.addView(fieldSpinner);
        
        // Operator
        addLabel(row, "المعامل:");
        Spinner operatorSpinner = new Spinner(this);
        String[] operators = {"يساوي (=)", "لا يساوي (!=)", "أكبر من (>)", "أقل من (<)", 
                            "أكبر أو يساوي (>=)", "أقل أو يساوي (<=)", "يحتوي على", "يبدأ بـ"};
        ArrayAdapter<String> operatorAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, operators
        );
        operatorSpinner.setAdapter(operatorAdapter);
        row.addView(operatorSpinner);
        
        // Value
        addLabel(row, "القيمة:");
        EditText etValue = new EditText(this);
        etValue.setHint("أدخل القيمة");
        row.addView(etValue);
        
        // Case Sensitive
        CheckBox cbCaseSensitive = new CheckBox(this);
        cbCaseSensitive.setText("مراعاة حالة الأحرف");
        row.addView(cbCaseSensitive);
        
        addRemoveButton(row);
        filterContainer.addView(row);
    }
    
    private void addDateRangeFilterRow() {
        LinearLayout row = createFormRow();
        
        // Date Field
        addLabel(row, "حقل التاريخ:");
        Spinner dateFieldSpinner = new Spinner(this);
        List<String> dateFields = new ArrayList<>();
        for (Field field : selectedFields) {
            if (field.getType().contains("Date") || field.getType().contains("DateTime")) {
                dateFields.add(field.getName());
            }
        }
        if (dateFields.isEmpty()) dateFields.add("created_at");
        ArrayAdapter<String> fieldAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, dateFields
        );
        dateFieldSpinner.setAdapter(fieldAdapter);
        row.addView(dateFieldSpinner);
        
        // Range Type
        addLabel(row, "نطاق التاريخ:");
        Spinner rangeSpinner = new Spinner(this);
        String[] ranges = {"بين تاريخين محددين", "اليوم", "أمس", "هذا الأسبوع", 
                         "الشهر الحالي", "الربع الحالي", "السنة الحالية"};
        ArrayAdapter<String> rangeAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, ranges
        );
        rangeSpinner.setAdapter(rangeAdapter);
        row.addView(rangeSpinner);
        
        // From Date (visible for custom range)
        addLabel(row, "من تاريخ:");
        EditText etFromDate = new EditText(this);
        etFromDate.setHint("YYYY-MM-DD");
        etFromDate.setInputType(InputType.TYPE_CLASS_DATETIME);
        row.addView(etFromDate);
        
        // To Date
        addLabel(row, "إلى تاريخ:");
        EditText etToDate = new EditText(this);
        etToDate.setHint("YYYY-MM-DD");
        etToDate.setInputType(InputType.TYPE_CLASS_DATETIME);
        row.addView(etToDate);
        
        // Quick Buttons
        LinearLayout quickButtons = new LinearLayout(this);
        quickButtons.setOrientation(LinearLayout.HORIZONTAL);
        
        Button btnToday = new Button(this);
        btnToday.setText("اليوم");
        btnToday.setOnClickListener(v -> {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
            etFromDate.setText(today);
            etToDate.setText(today);
        });
        
        Button btnThisMonth = new Button(this);
        btnThisMonth.setText("هذا الشهر");
        btnThisMonth.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            String monthStart = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(cal.getTime());
            
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            String monthEnd = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(cal.getTime());
            
            etFromDate.setText(monthStart);
            etToDate.setText(monthEnd);
        });
        
        quickButtons.addView(btnToday);
        quickButtons.addView(btnThisMonth);
        row.addView(quickButtons);
        
        addRemoveButton(row);
        filterContainer.addView(row);
    }
    
    private void addChoiceFilterRow() {
        LinearLayout row = createFormRow();
        
        // Field
        addLabel(row, "الحقل:");
        Spinner fieldSpinner = new Spinner(this);
        ArrayAdapter<String> fieldAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, 
            new String[]{"status", "type", "category", "user"}
        );
        fieldSpinner.setAdapter(fieldAdapter);
        row.addView(fieldSpinner);
        
        // Selection Type
        addLabel(row, "نوع الاختيار:");
        Spinner typeSpinner = new Spinner(this);
        String[] types = {"اختيار واحد", "اختيار متعدد", "جميع القيم ما عدا"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, types
        );
        typeSpinner.setAdapter(typeAdapter);
        row.addView(typeSpinner);
        
        // Values
        addLabel(row, "القيم (مفصولة بفواصل):");
        EditText etValues = new EditText(this);
        etValues.setHint("active,inactive,pending");
        etValues.setLines(2);
        row.addView(etValues);
        
        // Or load from model
        Button btnLoadValues = new Button(this);
        btnLoadValues.setText("تحميل القيم من النموذج");
        btnLoadValues.setOnClickListener(v -> {
            // This would load distinct values from the model
            etValues.setText("active,inactive,pending,draft");
        });
        row.addView(btnLoadValues);
        
        addRemoveButton(row);
        filterContainer.addView(row);
    }
    
    private void addNumberRangeFilterRow() {
        LinearLayout row = createFormRow();
        
        // Number Field
        addLabel(row, "حقل الرقم:");
        Spinner fieldSpinner = new Spinner(this);
        List<String> numberFields = new ArrayList<>();
        for (Field field : selectedFields) {
            if (field.getType().contains("Integer") || field.getType().contains("Decimal") 
                || field.getType().contains("Float")) {
                numberFields.add(field.getName());
            }
        }
        if (numberFields.isEmpty()) numberFields.add("amount");
        ArrayAdapter<String> fieldAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, numberFields
        );
        fieldSpinner.setAdapter(fieldAdapter);
        row.addView(fieldSpinner);
        
        // Range
        LinearLayout rangeLayout = new LinearLayout(this);
        rangeLayout.setOrientation(LinearLayout.HORIZONTAL);
        
        EditText etMin = new EditText(this);
        etMin.setHint("الحد الأدنى");
        etMin.setInputType(InputType.TYPE_CLASS_NUMBER);
        etMin.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        
        TextView tvTo = new TextView(this);
        tvTo.setText(" إلى ");
        tvTo.setPadding(16, 0, 16, 0);
        
        EditText etMax = new EditText(this);
        etMax.setHint("الحد الأقصى");
        etMax.setInputType(InputType.TYPE_CLASS_NUMBER);
        etMax.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        
        rangeLayout.addView(etMin);
        rangeLayout.addView(tvTo);
        rangeLayout.addView(etMax);
        
        addLabel(row, "النطاق:");
        row.addView(rangeLayout);
        
        // Quick Ranges
        addLabel(row, "نطاقات سريعة:");
        GridLayout quickGrid = new GridLayout(this);
        quickGrid.setColumnCount(3);
        
        String[] ranges = {"0-100", "100-1000", "1000-5000", "5000-10000", ">10000", "<0"};
        for (String range : ranges) {
            Button btn = new Button(this);
            btn.setText(range);
            btn.setOnClickListener(v -> {
                String[] parts = range.split("-|>|<");
                if (range.startsWith(">")) {
                    etMin.setText(parts[1]);
                    etMax.setText("");
                } else if (range.startsWith("<")) {
                    etMin.setText("");
                    etMax.setText(parts[1]);
                } else if (parts.length == 2) {
                    etMin.setText(parts[0]);
                    etMax.setText(parts[1]);
                }
            });
            quickGrid.addView(btn);
        }
        row.addView(quickGrid);
        
        addRemoveButton(row);
        filterContainer.addView(row);
    }
    
    private void addTemplateField() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("إضافة حقل قالب مخصص");
        
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        
        // Field Name
        addLabelAndEditText(layout, "اسم الحقل (في الكود):", "", text -> {
            // Will be saved later
        });
        
        // Display Name
        addLabelAndEditText(layout, "اسم العرض:", "", text -> {
            // Will be saved later
        });
        
        // Field Type
        addLabel(layout, "نوع الحقل:");
        Spinner typeSpinner = new Spinner(this);
        String[] types = {"نص", "رقم", "تاريخ", "تاريخ ووقت", "قائمة منسدلة", 
                         "مربع اختيار", "نطاق", "لون", "ملف"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, types
        );
        typeSpinner.setAdapter(typeAdapter);
        layout.addView(typeSpinner);
        
        // Options (for select)
        addLabelAndEditText(layout, "الخيارات (مفصولة بفواصل):", "", text -> {
            // For select type
        });
        
        // Default Value
        addLabelAndEditText(layout, "القيمة الافتراضية:", "", text -> {
            // Will be saved later
        });
        
        // Required
        CheckBox cbRequired = addCheckBox(layout, "مطلوب", false, checked -> {});
        
        scrollView.addView(layout);
        builder.setView(scrollView);
        
        builder.setPositiveButton("إضافة", (dialog, which) -> {
            // Create and add template field
            createTemplateFieldUI();
        });
        
        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }
    
    private void createTemplateFieldUI() {
        LinearLayout row = createFormRow();
        
        // Field Name
        EditText etFieldName = new EditText(this);
        etFieldName.setHint("اسم الحقل في الكود");
        row.addView(etFieldName);
        
        // Display Name
        EditText etDisplayName = new EditText(this);
        etDisplayName.setHint("اسم العرض للمستخدم");
        row.addView(etDisplayName);
        
        // Type
        Spinner typeSpinner = new Spinner(this);
        String[] types = {"text", "number", "date", "datetime", "select", "checkbox", "range"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, types
        );
        typeSpinner.setAdapter(typeAdapter);
        row.addView(typeSpinner);
        
        addRemoveButton(row);
        templateFieldsContainer.addView(row);
    }
    /*
    private void saveTemplate() {
        String name = etTemplateName.getText().toString().trim();
        String description = etTemplateDescription.getText().toString().trim();
        
        if (name.isEmpty()) {
            Toast.makeText(this, "يرجى إدخال اسم القالب", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Template template = new Template();
        template.name = name;
        template.description = description;
        template.htmlContent = generateTemplateHtml();
        template.cssContent = generateTemplateCss();
        template.jsContent = generateTemplateJs();
        template.projectId = projectId;
        template.isSystem = false;
        long templateId = dbHelper.saveTemplate(template, projectId);
        
       // long templateId = dbHelper.saveTemplate(template);
        if (templateId != -1) {
            Toast.makeText(this, "تم حفظ القالب بنجاح", Toast.LENGTH_SHORT).show();
            loadTemplates();
        } else {
            Toast.makeText(this, "فشل حفظ القالب", Toast.LENGTH_SHORT).show();
        }
    }
    */
    private String generateTemplateHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"ar\" dir=\"rtl\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>{{ report_title }}</title>\n");
        html.append("    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n");
        html.append("    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css\">\n");
        html.append("    <style>\n");
        html.append("        {{ custom_css }}\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container-fluid mt-4\">\n");
        html.append("        <!-- Header -->\n");
        html.append("        <div class=\"report-header\">\n");
        html.append("            <h2>{{ report_title }}</h2>\n");
        html.append("            <p>{{ report_description }}</p>\n");
        html.append("            <div class=\"report-meta\">\n");
        html.append("                <span><i class=\"fas fa-calendar\"></i> {{ generation_date }}</span>\n");
        html.append("                <span><i class=\"fas fa-user\"></i> {{ user }}</span>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("        \n");
        html.append("        <!-- Filters Summary -->\n");
        html.append("        {% if filters %}\n");
        html.append("        <div class=\"filters-summary\">\n");
        html.append("            <h5><i class=\"fas fa-filter\"></i> الفلاتر المطبقة:</h5>\n");
        html.append("            <ul>\n");
        html.append("            {% for filter in filters %}\n");
        html.append("                <li>{{ filter.field }} {{ filter.operator }} {{ filter.value }}</li>\n");
        html.append("            {% endfor %}\n");
        html.append("            </ul>\n");
        html.append("        </div>\n");
        html.append("        {% endif %}\n");
        html.append("        \n");
        html.append("        <!-- Summary Cards -->\n");
        html.append("        {% if summary_data %}\n");
        html.append("        <div class=\"row summary-cards\">\n");
        html.append("            {% for card in summary_data %}\n");
        html.append("            <div class=\"col-md-3 col-sm-6\">\n");
        html.append("                <div class=\"card summary-card\">\n");
        html.append("                    <div class=\"card-body\">\n");
        html.append("                        <h6>{{ card.title }}</h6>\n");
        html.append("                        <h3>{{ card.value }}</h3>\n");
        html.append("                        <p>{{ card.subtitle }}</p>\n");
        html.append("                    </div>\n");
        html.append("                </div>\n");
        html.append("            </div>\n");
        html.append("            {% endfor %}\n");
        html.append("        </div>\n");
        html.append("        {% endif %}\n");
        html.append("        \n");
        html.append("        <!-- Charts -->\n");
        html.append("        {% if charts %}\n");
        html.append("        <div class=\"row charts-section\">\n");
        html.append("            {% for chart in charts %}\n");
        html.append("            <div class=\"col-md-6\">\n");
        html.append("                <div class=\"chart-container\">\n");
        html.append("                    <h5>{{ chart.title }}</h5>\n");
        html.append("                    <canvas id=\"chart-{{ forloop.counter }}\"></canvas>\n");
        html.append("                </div>\n");
        html.append("            </div>\n");
        html.append("            {% endfor %}\n");
        html.append("        </div>\n");
        html.append("        {% endif %}\n");
        html.append("        \n");
        html.append("        <!-- Data Table -->\n");
        html.append("        <div class=\"card data-table-section\">\n");
        html.append("            <div class=\"card-header\">\n");
        html.append("                <h5 class=\"mb-0\">\n");
        html.append("                    <i class=\"fas fa-table\"></i> البيانات\n");
        html.append("                    <span class=\"badge bg-primary\">{{ total_records }} سجل</span>\n");
        html.append("                </h5>\n");
        html.append("                <div class=\"export-buttons\">\n");
        html.append("                    <button class=\"btn btn-sm btn-success\" onclick=\"exportToExcel()\">\n");
        html.append("                        <i class=\"fas fa-file-excel\"></i> Excel\n");
        html.append("                    </button>\n");
        html.append("                    <button class=\"btn btn-sm btn-danger\" onclick=\"exportToPDF()\">\n");
        html.append("                        <i class=\"fas fa-file-pdf\"></i> PDF\n");
        html.append("                    </button>\n");
        html.append("                    <button class=\"btn btn-sm btn-info\" onclick=\"printReport()\">\n");
        html.append("                        <i class=\"fas fa-print\"></i> طباعة\n");
        html.append("                    </button>\n");
        html.append("                </div>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"card-body\">\n");
        html.append("                <div class=\"table-responsive\">\n");
        html.append("                    <table class=\"table table-striped table-hover\">\n");
        html.append("                        <thead>\n");
        html.append("                            <tr>\n");
        
        // Table headers
        for (Field field : selectedFields) {
            if (field.isIncludeInReport()) {
                html.append("                                <th>{{ fields.").append(field.getName()).append(".display_name }}</th>\n");
            }
        }
        
        html.append("                            </tr>\n");
        html.append("                        </thead>\n");
        html.append("                        <tbody>\n");
        html.append("                            {% for record in data %}\n");
        html.append("                            <tr>\n");
        
        for (Field field : selectedFields) {
            if (field.isIncludeInReport()) {
                html.append("                                <td>{{ record.").append(field.getName()).append(" }}</td>\n");
            }
        }
        
        html.append("                            </tr>\n");
        html.append("                            {% endfor %}\n");
        html.append("                        </tbody>\n");
        html.append("                        {% if subtotals %}\n");
        html.append("                        <tfoot>\n");
        html.append("                            <tr class=\"table-info\">\n");
        for (Field field : selectedFields) {
            if (field.isIncludeInReport()) {
                html.append("                                <td><strong>{{ subtotals.").append(field.getName()).append(" }}</strong></td>\n");
            }
        }
        html.append("                            </tr>\n");
        html.append("                        </tfoot>\n");
        html.append("                        {% endif %}\n");
        html.append("                    </table>\n");
        html.append("                </div>\n");
        html.append("                \n");
        html.append("                <!-- Pagination -->\n");
        html.append("                {% if pagination %}\n");
        html.append("                <nav aria-label=\"Page navigation\">\n");
        html.append("                    <ul class=\"pagination justify-content-center\">\n");
        html.append("                        {% if pagination.has_previous %}\n");
        html.append("                        <li class=\"page-item\">\n");
        html.append("                            <a class=\"page-link\" href=\"?page={{ pagination.previous_page }}\">السابق</a>\n");
        html.append("                        </li>\n");
        html.append("                        {% endif %}\n");
        html.append("                        \n");
        html.append("                        {% for page in pagination.page_range %}\n");
        html.append("                        <li class=\"page-item {% if page == pagination.current_page %}active{% endif %}\">\n");
        html.append("                            <a class=\"page-link\" href=\"?page={{ page }}\">{{ page }}</a>\n");
        html.append("                        </li>\n");
        html.append("                        {% endfor %}\n");
        html.append("                        \n");
        html.append("                        {% if pagination.has_next %}\n");
        html.append("                        <li class=\"page-item\">\n");
        html.append("                            <a class=\"page-link\" href=\"?page={{ pagination.next_page }}\">التالي</a>\n");
        html.append("                        </li>\n");
        html.append("                        {% endif %}\n");
        html.append("                    </ul>\n");
        html.append("                </nav>\n");
        html.append("                {% endif %}\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("        \n");
        html.append("        <!-- Footer -->\n");
        html.append("        <div class=\"report-footer\">\n");
        html.append("            <p>تم إنشاء التقرير تلقائياً بواسطة نظام Django Reports</p>\n");
        html.append("            <p>تاريخ الإنشاء: {{ generation_date }} | وقت الإنشاء: {{ generation_time }}</p>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("    \n");
        html.append("    <script src=\"https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js\"></script>\n");
        html.append("    <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n");
        html.append("    <script>\n");
        html.append("        {{ custom_js }}\n");
        html.append("        \n");
        html.append("        // Initialize charts\n");
        html.append("        {% if charts %}\n");
        html.append("        document.addEventListener('DOMContentLoaded', function() {\n");
        html.append("            {% for chart in charts %}\n");
        html.append("            new Chart(document.getElementById('chart-{{ forloop.counter }}'), {\n");
        html.append("                type: '{{ chart.type }}',\n");
        html.append("                data: {{ chart.data|safe }},\n");
        html.append("                options: {{ chart.options|safe }}\n");
        html.append("            });\n");
        html.append("            {% endfor %}\n");
        html.append("        });\n");
        html.append("        {% endif %}\n");
        html.append("        \n");
        html.append("        function exportToExcel() {\n");
        html.append("            // Export to Excel functionality\n");
        html.append("            alert('سيتم تنزيل ملف Excel');\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        function exportToPDF() {\n");
        html.append("            // Export to PDF functionality\n");
        html.append("            alert('سيتم تنزيل ملف PDF');\n");
        html.append("        }\n");
        html.append("        \n");
        html.append("        function printReport() {\n");
        html.append("            window.print();\n");
        html.append("        }\n");
        html.append("    </script>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }
    private String generateTemplateJs() {
    StringBuilder stringBuilder = new StringBuilder();
    BufferedReader reader = null;
    try {
        // الوصول إلى ملفات الـ assets
        reader = new BufferedReader(
            new InputStreamReader(getAssets().open("temp.js"), "UTF-8")); 

        // قراءة الملف سطرًا بسطر
        String mLine;
        while ((mLine = reader.readLine()) != null) {
            stringBuilder.append(mLine).append("\n");
        }
    } catch (IOException e) {
        // معالجة الخطأ في حال لم يجد الملف أو حدثت مشكلة في القراءة
        e.printStackTrace();
    } finally {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    return stringBuilder.toString();
}

        private String generateTemplateCss() {
    StringBuilder stringBuilder = new StringBuilder();
    BufferedReader reader = null;
    try {
        // الوصول إلى ملفات الـ assets
        reader = new BufferedReader(
            new InputStreamReader(getAssets().open("temp.css"), "UTF-8")); 

        // قراءة الملف سطرًا بسطر
        String mLine;
        while ((mLine = reader.readLine()) != null) {
            stringBuilder.append(mLine).append("\n");
        }
    } catch (IOException e) {
        // معالجة الخطأ في حال لم يجد الملف أو حدثت مشكلة في القراءة
        e.printStackTrace();
    } finally {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    return stringBuilder.toString();
}

    
    private void saveReport() {
        try {
            updateCurrentConfig();
            
            // Validate
            if (currentConfig.name == null || currentConfig.name.isEmpty()) {
                Toast.makeText(this, "يرجى إدخال اسم التقرير", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Generate full code
            String fullCode = generateFullDjangoCodeString();
            currentConfig.settings.put("full_code", fullCode);
            
            // Convert to JSON
            Gson gson = new Gson();
            String configJson = gson.toJson(currentConfig);
            
            // Save to database
            Report report = new Report();
            report.setProjectId(projectId);
            report.setName(currentConfig.name);
            report.setDescription(currentConfig.description);
            report.setModelId(currentConfig.modelId);
            report.setModelName(currentConfig.modelName);
            report.setConfigJson(configJson);
            report.setDjangoQuery(fullCode);
            report.setCreatedAt(System.currentTimeMillis());
            
            long reportId = dbHelper.saveReport(report);
            if (reportId != -1) {
                currentReportId = reportId;
                tvReportId.setText("رقم التقرير: " + reportId);
                tvCreatedDate.setText("تاريخ الإنشاء: " + 
                    new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        .format(new Date()));
                
                Toast.makeText(this, "تم حفظ التقرير بنجاح", Toast.LENGTH_SHORT).show();
                
                // Show options
                showSaveSuccessDialog();
            }
            
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في حفظ التقرير: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void saveReportToDatabase() {
        saveReport();
    }
    
    private void showSaveSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("تم الحفظ بنجاح");
        builder.setMessage("ماذا تريد أن تفعل الآن؟");
        
        builder.setPositiveButton("عرض الكود", (dialog, which) -> {
            generateFullDjangoCode();
        });
        
        builder.setNegativeButton("معاينة التقرير", (dialog, which) -> {
            previewHtmlTemplate();
        });
        
        builder.setNeutralButton("تصدير", (dialog, which) -> {
            showExportOptionsDialog();
        });
        
        builder.show();
    }
    
    private void showExportOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("خيارات التصدير");
        
        String[] options = {
            "تصدير كملف Python (.py)",
            "تصدير كملف JSON",
            "تصدير كملف HTML",
            "تصدير كقالب Django",
            "نسخ إلى الحافظة",
            "مشاركة عبر البريد"
        };
        
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: exportAsPythonFile(); break;
                case 1: exportAsJsonFile(); break;
                case 2: exportAsHtmlFile(); break;
                case 3: exportAsDjangoTemplate(); break;
                case 4: copyToClipboard(generateFullDjangoCodeString()); break;
                case 5: shareReport(); break;
            }
        });
        
        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }
    
    private void exportAsPythonFile() {
        String code = generateFullDjangoCodeString();
        showCodeDialog("كود Python", code);
    }
    
    private void exportAsJsonFile() {
        Gson gson = new Gson();
        String json = gson.toJson(currentConfig);
        showCodeDialog("إعدادات JSON", json);
    }
    
    private void exportAsHtmlFile() {
        String html = generateFullHtmlReport();
        showCodeDialog("تقرير HTML", html);
    }
    
    private void exportAsDjangoTemplate() {
        String template = generateDjangoTemplate();
        showCodeDialog("قالب Django", template);
    }
    
    private void shareReport() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "تقرير Django: " + currentConfig.name);
        intent.putExtra(Intent.EXTRA_TEXT, 
            "تقرير: " + currentConfig.name + "\n\n" +
            "وصف: " + currentConfig.description + "\n\n" +
            "كود Django:\n\n" + generateFullDjangoCodeString());
        
        startActivity(Intent.createChooser(intent, "مشاركة التقرير"));
    }
    
    private void showSavedReportsDialog() {
        List<Report> reports = dbHelper.getReportsByProject(projectId);
        
        if (reports.isEmpty()) {
            Toast.makeText(this, "لا توجد تقارير محفوظة", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("التقارير المحفوظة");
        
        String[] reportNames = new String[reports.size()];
        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(report.getCreatedAt()));
            reportNames[i] = report.getName() + " (" + date + ")";
        }
        
        builder.setItems(reportNames, (dialog, which) -> {
            loadReport(reports.get(which).getId());
        });
        
        builder.setNegativeButton("إلغاء", null);
        
        builder.setNeutralButton("إدارة التقارير", (dialog, which) -> {
            manageReports();
        });
        
        builder.show();
    }
    
    private void loadReport(long reportId) {
        Report report = dbHelper.getReportById(reportId);
        if (report == null) {
            Toast.makeText(this, "التقرير غير موجود", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            currentReportId = reportId;
            currentConfig = new Gson().fromJson(report.getConfigJson(), ReportConfig.class);
            
            // Update UI
            etReportName.setText(currentConfig.name);
            etReportDescription.setText(currentConfig.description);
            tvReportId.setText("رقم التقرير: " + reportId);
            tvCreatedDate.setText("تاريخ الإنشاء: " + 
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(report.getCreatedAt()));
            
            // Select model
            int modelIndex = -1;
            for (int i = 0; i < availableModels.size(); i++) {
                if (availableModels.get(i).id == currentConfig.modelId) {
                    modelIndex = i;
                    break;
                }
            }
            if (modelIndex != -1) {
                modelSpinner.setSelection(modelIndex);
            }
            
            // Select report type
            String[] reportTypes = getResources().getStringArray(R.array.report_types);
            for (int i = 0; i < reportTypes.length; i++) {
                if (reportTypes[i].equals(currentConfig.reportType)) {
                    reportTypeSpinner.setSelection(i);
                    break;
                }
            }
            
            Toast.makeText(this, "تم تحميل التقرير", Toast.LENGTH_SHORT).show();
            
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في تحميل التقرير", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void manageReports() {
        /*
        Intent intent = new Intent(this, ReportsListActivity.class);
        intent.putExtra("PROJECT_ID", projectId);
        startActivity(intent);
        */
    }
    
    private void manageTemplates() {
        /*
        Intent intent = new Intent(this, TemplatesActivity.class);
        intent.putExtra("PROJECT_ID", projectId);
        startActivity(intent);
        */
    }
    
    private void updateCurrentConfig() {
        currentConfig.name = etReportName.getText().toString();
        currentConfig.description = etReportDescription.getText().toString();
        
        int modelPosition = modelSpinner.getSelectedItemPosition();
        if (modelPosition >= 0 && modelPosition < availableModels.size()) {
            currentConfig.modelId = availableModels.get(modelPosition).id;
            currentConfig.modelName = availableModels.get(modelPosition).name;
        }
        
        currentConfig.reportType = reportTypeSpinner.getSelectedItem().toString();
        currentConfig.includeSubtotals = cbIncludeSubtotals.isChecked();
        
        // Chart type
        int checkedId = rgChartType.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_bar_chart) {
            currentConfig.chartType = "bar";
        } else if (checkedId == R.id.rb_line_chart) {
            currentConfig.chartType = "line";
        } else if (checkedId == R.id.rb_pie_chart) {
            currentConfig.chartType = "pie";
        } else if (checkedId == R.id.rb_column_chart) {
            currentConfig.chartType = "column";
        }
        
        // Settings
        currentConfig.settings.put("distinct", cbDistinct.isChecked());
        currentConfig.settings.put("group_by", cbGroupBy.isChecked());
        currentConfig.settings.put("order_by", cbOrderBy.isChecked());
        currentConfig.settings.put("limit", etLimit.getText().toString());
        currentConfig.settings.put("offset", etOffset.getText().toString());
        
        // Fields
        currentConfig.settings.put("fields_count", selectedFields.size());
    }
    
    private void generateFullDjangoCode() {
        String code = generateFullDjangoCodeString();
        showCodeDialog("كود Django كامل", code);
    }
    
    private String generateFullDjangoCodeString() {
        StringBuilder code = new StringBuilder();
        
        // Header
        code.append("# ============================================\n");
        code.append("# تقرير: ").append(etReportName.getText().toString()).append("\n");
        code.append("# ============================================\n\n");
        
        // Imports
        code.append("# Import statements\n");
        code.append("from django.db.models import (\n");
        code.append("    Count, Sum, Avg, Max, Min, StdDev, Variance,\n");
        code.append("    F, Q, Case, When, Value,\n");
        code.append("    CharField, IntegerField, DecimalField, FloatField\n");
        code.append(")\n");
        code.append("from django.db.models.functions import TruncDate, TruncMonth, TruncYear\n");
        code.append("from django.http import HttpResponse, JsonResponse\n");
        code.append("from django.shortcuts import render\n");
        code.append("from django.views.generic import View\n");
        code.append("from django.utils import timezone\n");
        code.append("import json\n");
        code.append("from datetime import datetime, timedelta\n");
        code.append("import pandas as pd\n");
        code.append("from reportlab.pdfgen import canvas\n");
        code.append("from reportlab.lib.pagesizes import A4\n");
        code.append("from reportlab.lib import colors\n");
        code.append("from reportlab.platypus import Table, TableStyle\n\n");
        
        // Model reference
        code.append("# Model reference\n");
        code.append("# from your_app.models import ").append(currentConfig.modelName).append("\n\n");
        
        // Main view class
        code.append("class ").append(toCamelCase(currentConfig.name)).append("ReportView(View):\n");
        code.append("    \"\"\"\n");
        code.append("    تقرير: ").append(currentConfig.name).append("\n");
        code.append("    ").append(currentConfig.description).append("\n");
        code.append("    \"\"\"\n\n");
        
        // Get method
        code.append("    def get(self, request, *args, **kwargs):\n");
        code.append("        # Parse request parameters\n");
        code.append("        params = self.get_report_params(request)\n");
        code.append("        \n");
        code.append("        # Get data\n");
        code.append("        queryset = self.get_queryset(params)\n");
        code.append("        data = self.process_data(queryset, params)\n");
        code.append("        \n");
        code.append("        # Return based on format\n");
        code.append("        format = params.get('format', 'html')\n");
        code.append("        if format == 'json':\n");
        code.append("            return self.render_json(data, params)\n");
        code.append("        elif format == 'excel':\n");
        code.append("            return self.render_excel(data, params)\n");
        code.append("        elif format == 'pdf':\n");
        code.append("            return self.render_pdf(data, params)\n");
        code.append("        else:\n");
        code.append("            return self.render_html(data, params)\n\n");
        
        // Get report params method
        code.append("    def get_report_params(self, request):\n");
        code.append("        \"\"\"استخراج معلمات التقرير من الطلب\"\"\"\n");
        code.append("        params = {\n");
        code.append("            'start_date': request.GET.get('start_date'),\n");
        code.append("            'end_date': request.GET.get('end_date'),\n");
        code.append("            'format': request.GET.get('format', 'html'),\n");
        code.append("            'page': int(request.GET.get('page', 1)),\n");
        code.append("            'page_size': int(request.GET.get('page_size', 50)),\n");
        code.append("        }\n");
        code.append("        \n");
        code.append("        # Add custom filters\n");
        for (int i = 0; i < filterContainer.getChildCount(); i++) {
            View row = filterContainer.getChildAt(i);
            if (row instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) row;
                for (int j = 0; j < layout.getChildCount(); j++) {
                    View child = layout.getChildAt(j);
                    if (child instanceof EditText) {
                        EditText et = (EditText) child;
                        if (et.getHint() != null && et.getHint().toString().contains("الحقل")) {
                            String fieldName = et.getText().toString();
                            if (!fieldName.isEmpty()) {
                                code.append("        params['").append(fieldName).append("'] = request.GET.get('").append(fieldName).append("')\n");
                            }
                        }
                    }
                }
            }
        }
        code.append("        \n");
        code.append("        return params\n\n");
        
        // Get queryset method
        code.append("    def get_queryset(self, params):\n");
        code.append("        \"\"\"بناء QuerySet الرئيسي\"\"\"\n");
        code.append("        queryset = ").append(currentConfig.modelName).append(".objects.all()\n");
        code.append("        \n");
        
        // Select related
        code.append("        # Select related fields\n");
        List<String> selectRelated = new ArrayList<>();
        for (int i = 0; i < relatedContainer.getChildCount(); i++) {
            View view = relatedContainer.getChildAt(i);
            if (view instanceof CardView) {
                CardView card = (CardView) view;
                LinearLayout row = (LinearLayout) card.getChildAt(0);
                for (int j = 0; j < row.getChildCount(); j++) {
                    View child = row.getChildAt(j);
                    if (child instanceof CheckBox) {
                        CheckBox cb = (CheckBox) child;
                        if (cb.isChecked() && cb.getTag() instanceof Field) {
                            Field field = (Field) cb.getTag();
                            if (!field.getType().equals("ManyToManyField")) {
                                selectRelated.add("'" + field.getName() + "'");
                            }
                        }
                    }
                }
            }
        }
        if (!selectRelated.isEmpty()) {
            code.append("        queryset = queryset.select_related(").append(String.join(", ", selectRelated)).append(")\n");
        }
        
        // Prefetch related
        code.append("        \n");
        code.append("        # Prefetch related fields\n");
        List<String> prefetchRelated = new ArrayList<>();
        for (int i = 0; i < relatedContainer.getChildCount(); i++) {
            View view = relatedContainer.getChildAt(i);
            if (view instanceof CardView) {
                CardView card = (CardView) view;
                LinearLayout row = (LinearLayout) card.getChildAt(0);
                for (int j = 0; j < row.getChildCount(); j++) {
                    View child = row.getChildAt(j);
                    if (child instanceof CheckBox) {
                        CheckBox cb = (CheckBox) child;
                        if (cb.isChecked() && cb.getTag() instanceof Field) {
                            Field field = (Field) cb.getTag();
                            if (field.getType().equals("ManyToManyField")) {
                                prefetchRelated.add("'" + field.getName() + "'");
                            }
                        }
                    }
                }
            }
        }
        if (!prefetchRelated.isEmpty()) {
            code.append("        queryset = queryset.prefetch_related(").append(String.join(", ", prefetchRelated)).append(")\n");
        }
        
        // Filters
        code.append("        \n");
        code.append("        # Apply filters\n");
        code.append("        filters = Q()\n");
        
        for (int i = 0; i < filterContainer.getChildCount(); i++) {
            View row = filterContainer.getChildAt(i);
            if (row instanceof LinearLayout) {
                String filterCode = generateFilterCode((LinearLayout) row);
                if (!filterCode.isEmpty()) {
                    code.append("        ").append(filterCode).append("\n");
                }
            }
        }
        
        code.append("        if filters:\n");
        code.append("            queryset = queryset.filter(filters)\n");
        
        // Annotations
        if (annotateContainer.getChildCount() > 0) {
            code.append("        \n");
            code.append("        # Apply annotations\n");
            code.append("        annotates = {}\n");
            
            for (int i = 0; i < annotateContainer.getChildCount(); i++) {
                View row = annotateContainer.getChildAt(i);
                if (row instanceof LinearLayout) {
                    String annotateCode = generateAnnotateCode((LinearLayout) row);
                    if (!annotateCode.isEmpty()) {
                        code.append("        ").append(annotateCode).append("\n");
                    }
                }
            }
            
            code.append("        if annotates:\n");
            code.append("            queryset = queryset.annotate(**annotates)\n");
        }
        
        // Group by
        if (cbGroupBy.isChecked()) {
            code.append("        \n");
            code.append("        # Group by\n");
            List<String> groupFields = new ArrayList<>();
            for (Field field : selectedFields) {
                if (field.isGroupBy()) {
                    groupFields.add(field.getName());
                }
            }
            if (!groupFields.isEmpty()) {
                code.append("        queryset = queryset.values(");
                for (int i = 0; i < groupFields.size(); i++) {
                    if (i > 0) code.append(", ");
                    code.append("'").append(groupFields.get(i)).append("'");
                }
                code.append(")\n");
            }
        }
        
        // Order by
        if (cbOrderBy.isChecked()) {
            code.append("        \n");
            code.append("        # Order by\n");
            List<String> orderFields = new ArrayList<>();
            for (Field field : selectedFields) {
                if (field.isOrderBy()) {
                    String prefix = field.getOrderDirection().equals("DESC") ? "-" : "";
                    orderFields.add("'" + prefix + field.getName() + "'");
                }
            }
            if (!orderFields.isEmpty()) {
                code.append("        queryset = queryset.order_by(").append(String.join(", ", orderFields)).append(")\n");
            }
        }
        
        // Distinct
        if (cbDistinct.isChecked()) {
            code.append("        \n");
            code.append("        # Distinct\n");
            code.append("        queryset = queryset.distinct()\n");
        }
        
        // Limit/Offset
        String limit = etLimit.getText().toString().trim();
        String offset = etOffset.getText().toString().trim();
        if (!limit.isEmpty() || !offset.isEmpty()) {
            code.append("        \n");
            code.append("        # Pagination\n");
            if (!limit.isEmpty()) {
                code.append("        limit = ").append(limit).append("\n");
            }
            if (!offset.isEmpty()) {
                code.append("        offset = ").append(offset).append("\n");
            }
            code.append("        queryset = queryset[offset:offset + limit] if 'offset' in locals() and 'limit' in locals() else queryset\n");
        }
        
        code.append("        \n");
        code.append("        return queryset\n\n");
        
        // Process data method
        code.append("    def process_data(self, queryset, params):\n");
        code.append("        \"\"\"معالجة البيانات\"\"\"\n");
        code.append("        data = {\n");
        code.append("            'records': list(queryset),\n");
        code.append("            'total_count': queryset.count(),\n");
        code.append("            'summary': {},\n");
        code.append("            'charts': [],\n");
        code.append("        }\n");
        code.append("        \n");
        code.append("        # Calculate summary\n");
        if (annotateContainer.getChildCount() > 0 || cbIncludeSubtotals.isChecked()) {
            code.append("        if data['records']:\n");
            if (cbIncludeSubtotals.isChecked()) {
                code.append("            # Calculate subtotals\n");
                for (Field field : selectedFields) {
                    if (field.isIncludeInReport() && 
                        (field.getType().contains("Integer") || 
                         field.getType().contains("Decimal") || 
                         field.getType().contains("Float"))) {
                        code.append("            data['summary']['total_").append(field.getName())
                            .append("'] = sum(r.").append(field.getName())
                            .append(" for r in data['records'] if r.").append(field.getName()).append(")\n");
                    }
                }
            }
        }
        code.append("        \n");
        code.append("        # Prepare chart data\n");
        code.append("        if params.get('include_charts', True):\n");
        code.append("            data['charts'] = self.prepare_chart_data(data['records'])\n");
        code.append("        \n");
        code.append("        return data\n\n");
        
        // Prepare chart data method
        code.append("    def prepare_chart_data(self, records):\n");
        code.append("        \"\"\"إعداد بيانات المخططات\"\"\"\n");
        code.append("        charts = []\n");
        code.append("        \n");
        code.append("        # Bar chart example\n");
        code.append("        if records:\n");
        code.append("            bar_chart = {\n");
        code.append("                'type': 'bar',\n");
        code.append("                'title': 'توزيع البيانات',\n");
        code.append("                'data': {\n");
        code.append("                    'labels': [],\n");
        code.append("                    'datasets': [{\n");
        code.append("                        'label': 'العدد',\n");
        code.append("                        'data': [],\n");
        code.append("                        'backgroundColor': 'rgba(54, 162, 235, 0.5)',\n");
        code.append("                    }]\n");
        code.append("                }\n");
        code.append("            }\n");
        code.append("            charts.append(bar_chart)\n");
        code.append("        \n");
        code.append("        return charts\n\n");
        
        // Render methods
        code.append("    def render_html(self, data, params):\n");
        code.append("        \"\"\"عرض HTML\"\"\"\n");
        code.append("        context = {\n");
        code.append("            'report_title': '").append(currentConfig.name).append("',\n");
        code.append("            'report_description': '").append(currentConfig.description).append("',\n");
        code.append("            'data': data['records'],\n");
        code.append("            'total_records': data['total_count'],\n");
        code.append("            'summary': data['summary'],\n");
        code.append("            'charts': data['charts'],\n");
        code.append("            'generation_date': timezone.now().strftime('%Y-%m-%d'),\n");
        code.append("            'generation_time': timezone.now().strftime('%H:%M'),\n");
        code.append("            'params': params,\n");
        code.append("        }\n");
        code.append("        return render(self.request, 'reports/").append(toSnakeCase(currentConfig.name))
            .append(".html', context)\n\n");
        
        code.append("    def render_json(self, data, params):\n");
        code.append("        \"\"\"عرض JSON\"\"\"\n");
        code.append("        return JsonResponse({\n");
        code.append("            'success': True,\n");
        code.append("            'data': data['records'],\n");
        code.append("            'total': data['total_count'],\n");
        code.append("            'summary': data['summary'],\n");
        code.append("        })\n\n");
        
        code.append("    def render_excel(self, data, params):\n");
        code.append("        \"\"\"تصدير إلى Excel\"\"\"\n");
        code.append("        filename = 'report_' + timezone.now().strftime('%Y%m%d_%H%M%S') + '.xlsx'\n");
        code.append("        response = HttpResponse(content_type='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')\n");
        code.append("        response['Content-Disposition'] = f'attachment; filename=\"{filename}\"'\n");
        code.append("        \n");
        code.append("        # Create DataFrame\n");
        code.append("        df_data = []\n");
        code.append("        for record in data['records']:\n");
        code.append("            row = {}\n");
        for (Field field : selectedFields) {
            if (field.isIncludeInReport()) {
                code.append("            row['").append(field.getDisplayName()).append("'] = record.")
                    .append(field.getName()).append("\n");
            }
        }
        code.append("            df_data.append(row)\n");
        code.append("        \n");
        code.append("        df = pd.DataFrame(df_data)\n");
        code.append("        df.to_excel(response, index=False)\n");
        code.append("        return response\n\n");
        
        code.append("    def render_pdf(self, data, params):\n");
        code.append("        \"\"\"تصدير إلى PDF\"\"\"\n");
        code.append("        filename = 'report_' + timezone.now().strftime('%Y%m%d_%H%M%S') + '.pdf'\n");
        code.append("        response = HttpResponse(content_type='application/pdf')\n");
        code.append("        response['Content-Disposition'] = f'attachment; filename=\"{filename}\"'\n");
        code.append("        \n");
        code.append("        p = canvas.Canvas(response, pagesize=A4)\n");
        code.append("        width, height = A4\n");
        code.append("        \n");
        code.append("        # Title\n");
        code.append("        p.setFont(\"Helvetica-Bold\", 16)\n");
        code.append("        p.drawString(50, height - 50, '").append(currentConfig.name).append("')\n");
        code.append("        \n");
        code.append("        # Table\n");
        code.append("        table_data = [['").append(String.join("', '", 
            selectedFields.stream()
                .filter(Field::isIncludeInReport)
                .map(Field::getDisplayName)
                .toArray(String[]::new)))
            .append("']]\n");
        code.append("        \n");
        code.append("        for record in data['records'][:50]:  # Limit to 50 rows in PDF\n");
        code.append("            row = []\n");
        for (Field field : selectedFields) {
            if (field.isIncludeInReport()) {
                code.append("            row.append(str(record.").append(field.getName()).append("))\n");
            }
        }
        code.append("            table_data.append(row)\n");
        code.append("        \n");
        code.append("        table = Table(table_data)\n");
        code.append("        table.setStyle(TableStyle([\n");
        code.append("            ('BACKGROUND', (0, 0), (-1, 0), colors.grey),\n");
        code.append("            ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),\n");
        code.append("            ('ALIGN', (0, 0), (-1, -1), 'CENTER'),\n");
        code.append("            ('GRID', (0, 0), (-1, -1), 1, colors.black),\n");
        code.append("        ]))\n");
        code.append("        \n");
        code.append("        table.wrapOn(p, width, height)\n");
        code.append("        table.drawOn(p, 50, height - 200)\n");
        code.append("        \n");
        code.append("        p.showPage()\n");
        code.append("        p.save()\n");
        code.append("        return response\n\n");
        
        // Template
        code.append("# ============================================\n");
        code.append("# Template: ").append(toSnakeCase(currentConfig.name)).append(".html\n");
        code.append("# ============================================\n\n");
        code.append(generateFullHtmlReport());
        
        return code.toString();
    }
    
    private String generateFilterCode(LinearLayout row) {
        // This is a simplified version - you would need to implement
        // based on the actual filter type and components
        return "filters &= Q(field__icontains='value')";
    }
    
    private String generateAnnotateCode(LinearLayout row) {
        // This is a simplified version
        return "annotates['total_count'] = Count('id')";
    }
    
    private String generateFullHtmlReport() {
        return generateTemplateHtml();
    }
    
    private String generateDjangoTemplate() {
        StringBuilder template = new StringBuilder();
        template.append("{% extends 'base.html' %}\n\n");
        template.append("{% block title %}").append(currentConfig.name).append("{% endblock %}\n\n");
        template.append("{% block content %}\n");
        template.append("<div class=\"container-fluid\">\n");
        template.append("    <h2>").append(currentConfig.name).append("</h2>\n");
        template.append("    <p>").append(currentConfig.description).append("</p>\n");
        template.append("    \n");
        template.append("    {% if data %}\n");
        template.append("    <table class=\"table\">\n");
        template.append("        <thead>\n");
        template.append("            <tr>\n");
        for (Field field : selectedFields) {
            if (field.isIncludeInReport()) {
                template.append("                <th>").append(field.getDisplayName()).append("</th>\n");
            }
        }
        template.append("            </tr>\n");
        template.append("        </thead>\n");
        template.append("        <tbody>\n");
        template.append("            {% for item in data %}\n");
        template.append("            <tr>\n");
        for (Field field : selectedFields) {
            if (field.isIncludeInReport()) {
                template.append("                <td>{{ item.").append(field.getName()).append(" }}</td>\n");
            }
        }
        template.append("            </tr>\n");
        template.append("            {% endfor %}\n");
        template.append("        </tbody>\n");
        template.append("    </table>\n");
        template.append("    {% else %}\n");
        template.append("    <p>لا توجد بيانات</p>\n");
        template.append("    {% endif %}\n");
        template.append("</div>\n");
        template.append("{% endblock %}\n");
        
        return template.toString();
    }
    
    private void generateExcelCode() {
        StringBuilder code = new StringBuilder();
        code.append("# تصدير إلى Excel - إصدار متقدم\n");
        code.append("import pandas as pd\n");
        code.append("from django.http import HttpResponse\n");
        code.append("from django.utils import timezone\n");
        code.append("from openpyxl import Workbook\n");
        code.append("from openpyxl.styles import Font, Alignment, PatternFill\n");
        code.append("from openpyxl.utils import get_column_letter\n\n");
        
        code.append("def export_to_excel_advanced(request, queryset, title=None):\n");
        code.append("    \"\"\"تصدير متقدم إلى Excel مع تنسيق\"\"\"\n");
        code.append("    if title is None:\n");
        code.append("        title = 'تقرير'\n");
        code.append("    \n");
        code.append("    filename = f'report_{timezone.now().strftime(\"%Y%m%d_%H%M%S\")}.xlsx'\n");
        code.append("    \n");
        code.append("    # إنشاء Workbook\n");
        code.append("    wb = Workbook()\n");
        code.append("    ws = wb.active\n");
        code.append("    ws.title = 'البيانات'\n");
        code.append("    \n");
        code.append("    # تنسيق العنوان\n");
        code.append("    ws.merge_cells('A1:H1')\n");
        code.append("    title_cell = ws['A1']\n");
        code.append("    title_cell.value = title\n");
        code.append("    title_cell.font = Font(size=16, bold=True)\n");
        code.append("    title_cell.alignment = Alignment(horizontal='center')\n");
        code.append("    \n");
        code.append("    # رأس الجدول\n");
        code.append("    headers = [");
        for (Field field : selectedFields) {
            if (field.isIncludeInReport()) {
                code.append("'").append(field.getDisplayName()).append("', ");
            }
        }
        code.append("]\n");
        code.append("    \n");
        code.append("    for col, header in enumerate(headers, 1):\n");
        code.append("        cell = ws.cell(row=3, column=col)\n");
        code.append("        cell.value = header\n");
        code.append("        cell.font = Font(bold=True)\n");
        code.append("        cell.fill = PatternFill(start_color='366092', end_color='366092', fill_type='solid')\n");
        code.append("        cell.alignment = Alignment(horizontal='center')\n");
        code.append("    \n");
        code.append("    # البيانات\n");
        code.append("    for row_idx, item in enumerate(queryset, 4):\n");
        code.append("        col_idx = 1\n");
        for (Field field : selectedFields) {
            if (field.isIncludeInReport()) {
                code.append("        ws.cell(row=row_idx, column=col_idx).value = item.").append(field.getName()).append("\n");
                code.append("        col_idx += 1\n");
            }
        }
        code.append("    \n");
        code.append("    # ضبط عرض الأعمدة\n");
        code.append("    for column in ws.columns:\n");
        code.append("        max_length = 0\n");
        code.append("        column_letter = get_column_letter(column[0].column)\n");
        code.append("        for cell in column:\n");
        code.append("            try:\n");
        code.append("                if len(str(cell.value)) > max_length:\n");
        code.append("                    max_length = len(str(cell.value))\n");
        code.append("            except:\n");
        code.append("                pass\n");
        code.append("        adjusted_width = min(max_length + 2, 50)\n");
        code.append("        ws.column_dimensions[column_letter].width = adjusted_width\n");
        code.append("    \n");
        code.append("    # إضافة المخططات (اختياري)\n");
        code.append("    if queryset.count() > 0:\n");
        code.append("        chart_sheet = wb.create_sheet(title='مخططات')\n");
        code.append("        # إضافة المخططات هنا\n");
        code.append("    \n");
        code.append("    # حفظ إلى response\n");
        code.append("    from io import BytesIO\n");
        code.append("    output = BytesIO()\n");
        code.append("    wb.save(output)\n");
        code.append("    \n");
        code.append("    response = HttpResponse(\n");
        code.append("        output.getvalue(),\n");
        code.append("        content_type='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'\n");
        code.append("    )\n");
        code.append("    response['Content-Disposition'] = f'attachment; filename=\"{filename}\"'\n");
        code.append("    \n");
        code.append("    return response\n");
        
        showCodeDialog("كود Excel متقدم", code.toString());
    }
    
    private void generatePdfCode() {
        // Similar to previous PDF code but enhanced
        showCodeDialog("كود PDF متقدم", generateFullDjangoCodeString());
    }
    
    private void previewHtmlTemplate() {
        String html = generateTemplateHtml();
        showCodeDialog("معاينة قالب HTML", html);
    }
    
    private void showCodeDialog(String title, String code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        
        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setText(code);
        textView.setTextSize(10);
        textView.setTypeface(Typeface.MONOSPACE);
        textView.setPadding(20, 20, 20, 20);
        textView.setTextIsSelectable(true);
        
        scrollView.addView(textView);
        builder.setView(scrollView);
        
        builder.setPositiveButton("نسخ الكود", (dialog, which) -> {
            copyToClipboard(code);
            Toast.makeText(this, "تم نسخ الكود", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("إغلاق", null);
        
        builder.setNeutralButton("حفظ كملف", (dialog, which) -> {
            saveCodeToFile(title, code);
        });
        
        builder.show();
    }
    
    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Django Code", text);
        clipboard.setPrimaryClip(clip);
    }
    
    private void saveCodeToFile(String title, String code) {
        // Implementation depends on your storage requirements
        Toast.makeText(this, "سيتم تنفيذ حفظ الملف في الإصدار القادم", Toast.LENGTH_SHORT).show();
    }
    
    private void exportReportConfig() {
        Gson gson = new Gson();
        String json = gson.toJson(currentConfig);
        showCodeDialog("تصدير إعدادات التقرير", json);
    }
    
    private void importReportConfig() {
        // Implement import from JSON
        Toast.makeText(this, "سيتم تنفيذ استيراد الإعدادات في الإصدار القادم", Toast.LENGTH_SHORT).show();
    }
    
    private void clearAll() {
        new AlertDialog.Builder(this)
            .setTitle("مسح الكل")
            .setMessage("هل أنت متأكد من مسح جميع الإعدادات؟")
            .setPositiveButton("نعم", (dialog, which) -> {
                etReportName.setText("");
                etReportDescription.setText("");
                etLimit.setText("");
                etOffset.setText("");
                etTemplateName.setText("");
                etTemplateDescription.setText("");
                
                cbDistinct.setChecked(false);
                cbGroupBy.setChecked(false);
                cbOrderBy.setChecked(false);
                cbIncludeSubtotals.setChecked(false);
                
                modelSpinner.setSelection(0);
                reportTypeSpinner.setSelection(0);
                templateSpinner.setSelection(0);
                
                annotateContainer.removeAllViews();
                filterContainer.removeAllViews();
                templateFieldsContainer.removeAllViews();
                
                currentReportId = -1;
                tvReportId.setText("");
                tvCreatedDate.setText("");
                
                Toast.makeText(this, "تم مسح جميع الإعدادات", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("لا", null)
            .show();
    }
    
    private void showAdvancedSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("إعدادات متقدمة");
        
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);
        
        addSectionTitle(layout, "إعدادات الأداء");
        
        EditText etCacheTimeout = new EditText(this);
        etCacheTimeout.setHint("مهلة الكاش (بالثواني)");
        etCacheTimeout.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(etCacheTimeout);
        
        CheckBox cbEnableCaching = addCheckBox(layout, "تفعيل الكاش", false, checked -> {});
        CheckBox cbEnablePagination = addCheckBox(layout, "تفعيل التقسيم", true, checked -> {});
        
        addSectionTitle(layout, "إعدادات الأمان");
        
        CheckBox cbRequireAuth = addCheckBox(layout, "يتطلب مصادقة", true, checked -> {});
        CheckBox cbCheckPermissions = addCheckBox(layout, "فحص الصلاحيات", true, checked -> {});
        
        addSectionTitle(layout, "إعدادات التصدير");
        
        EditText etExportLimit = new EditText(this);
        etExportLimit.setHint("حد التصدير (الحد الأقصى للسجلات)");
        etExportLimit.setInputType(InputType.TYPE_CLASS_NUMBER);
        etExportLimit.setText("10000");
        layout.addView(etExportLimit);
        
        scrollView.addView(layout);
        builder.setView(scrollView);
        
        builder.setPositiveButton("حفظ", (dialog, which) -> {
            Toast.makeText(this, "تم حفظ الإعدادات المتقدمة", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("إلغاء", null);
        
        builder.show();
    }
    
    private void generateAPI() {
        StringBuilder apiCode = new StringBuilder();
        
        apiCode.append("# ============================================\n");
        apiCode.append("# REST API للتقرير\n");
        apiCode.append("# ============================================\n\n");
        
        apiCode.append("from rest_framework import viewsets, permissions\n");
        apiCode.append("from rest_framework.decorators import action\n");
        apiCode.append("from rest_framework.response import Response\n");
        apiCode.append("from django_filters import rest_framework as filters\n");
        apiCode.append("import pandas as pd\n");
        apiCode.append("from io import BytesIO\n\n");
        
        apiCode.append("class ").append(toCamelCase(currentConfig.name)).append("Filter(filters.FilterSet):\n");
        apiCode.append("    \"\"\"فلاتر API للتقرير\"\"\"\n");
        apiCode.append("    start_date = filters.DateFilter(field_name='created_at', lookup_expr='gte')\n");
        apiCode.append("    end_date = filters.DateFilter(field_name='created_at', lookup_expr='lte')\n");
        
        for (Field field : selectedFields) {
            if (field.isIncludeInReport()) {
                apiCode.append("    ").append(field.getName()).append(" = filters.CharFilter(lookup_expr='icontains')\n");
            }
        }
        
        apiCode.append("    \n");
        apiCode.append("    class Meta:\n");
        apiCode.append("        model = ").append(currentConfig.modelName).append("\n");
        apiCode.append("        fields = [");
        for (Field field : selectedFields) {
            if (field.isIncludeInReport()) {
                apiCode.append("'").append(field.getName()).append("', ");
            }
        }
        apiCode.append("]\n\n");
        
        apiCode.append("class ").append(toCamelCase(currentConfig.name)).append("APIView(viewsets.ReadOnlyModelViewSet):\n");
        apiCode.append("    \"\"\"API للتقرير\"\"\"\n");
        apiCode.append("    queryset = ").append(currentConfig.modelName).append(".objects.all()\n");
        apiCode.append("    serializer_class = ").append(currentConfig.modelName).append("Serializer\n");
        apiCode.append("    filterset_class = ").append(toCamelCase(currentConfig.name)).append("Filter\n");
        apiCode.append("    permission_classes = [permissions.IsAuthenticated]\n");
        apiCode.append("    \n");
        apiCode.append("    @action(detail=False, methods=['get'])\n");
        apiCode.append("    def summary(self, request):\n");
        apiCode.append("        \"\"\"ملخص التقرير\"\"\"\n");
        apiCode.append("        queryset = self.filter_queryset(self.get_queryset())\n");
        apiCode.append("        \n");
        apiCode.append("        summary = {\n");
        apiCode.append("            'total': queryset.count(),\n");
        if (annotateContainer.getChildCount() > 0) {
            for (int i = 0; i < annotateContainer.getChildCount(); i++) {
                apiCode.append("            'annotation_").append(i).append("': queryset.aggregate(...),\n");
            }
        }
        apiCode.append("        }\n");
        apiCode.append("        \n");
        apiCode.append("        return Response(summary)\n");
        apiCode.append("    \n");
        apiCode.append("    @action(detail=False, methods=['get'])\n");
        apiCode.append("    def export(self, request):\n");
        apiCode.append("        \"\"\"تصدير البيانات\"\"\"\n");
        apiCode.append("        format = request.query_params.get('format', 'json')\n");
        apiCode.append("        queryset = self.filter_queryset(self.get_queryset())\n");
        apiCode.append("        \n");
        apiCode.append("        if format == 'excel':\n");
        apiCode.append("            return self.export_excel(queryset)\n");
        apiCode.append("        elif format == 'csv':\n");
        apiCode.append("            return self.export_csv(queryset)\n");
        apiCode.append("        else:\n");
        apiCode.append("            serializer = self.get_serializer(queryset, many=True)\n");
        apiCode.append("            return Response(serializer.data)\n");
        apiCode.append("    \n");
        apiCode.append("    def export_excel(self, queryset):\n");
        apiCode.append("        \"\"\"تصدير إلى Excel\"\"\"\n");
        apiCode.append("        df = pd.DataFrame(list(queryset.values()))\n");
        apiCode.append("        output = BytesIO()\n");
        apiCode.append("        df.to_excel(output, index=False)\n");
        apiCode.append("        output.seek(0)\n");
        apiCode.append("        \n");
        apiCode.append("        response = HttpResponse(\n");
        apiCode.append("            output.getvalue(),\n");
        apiCode.append("            content_type='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'\n");
        apiCode.append("        )\n");
        apiCode.append("        response['Content-Disposition'] = 'attachment; filename=\"report.xlsx\"'\n");
        apiCode.append("        return response\n");
        apiCode.append("    \n");
        apiCode.append("    def export_csv(self, queryset):\n");
        apiCode.append("        \"\"\"تصدير إلى CSV\"\"\"\n");
        apiCode.append("        df = pd.DataFrame(list(queryset.values()))\n");
        apiCode.append("        response = HttpResponse(content_type='text/csv')\n");
        apiCode.append("        response['Content-Disposition'] = 'attachment; filename=\"report.csv\"'\n");
        apiCode.append("        df.to_csv(response, index=False)\n");
        apiCode.append("        return response\n\n");
        
        apiCode.append("# Endpoints المتاحة:\n");
        apiCode.append("# GET /api/report/              - قائمة البيانات\n");
        apiCode.append("# GET /api/report/summary/      - ملخص التقرير\n");
        apiCode.append("# GET /api/report/export/?format=excel - تصدير Excel\n");
        apiCode.append("# GET /api/report/export/?format=csv   - تصدير CSV\n");
        
        showCodeDialog("كود REST API", apiCode.toString());
    }
    
    // Helper methods
    private LinearLayout createFormRow() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(16, 16, 16, 16);
        row.setBackgroundResource(R.drawable.border_rounded);
        row.setTag(System.currentTimeMillis());
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 8);
        row.setLayoutParams(params);
        
        return row;
    }
    
    private void addSectionTitle(LinearLayout layout, String title) {
        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextSize(16);
        tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        tv.setTextColor(Color.parseColor("#1976D2"));
        tv.setPadding(0, 16, 0, 8);
        layout.addView(tv);
        
        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#E0E0E0"));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1
        );
        params.setMargins(0, 0, 0, 16);
        divider.setLayoutParams(params);
        layout.addView(divider);
    }
    
    private void addLabel(LinearLayout layout, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14);
        tv.setPadding(0, 8, 0, 4);
        layout.addView(tv);
    }
    
    private void addLabelAndEditText(LinearLayout layout, String label, String defaultValue, 
                                     TextChangedListener listener) {
        addLabel(layout, label);
        EditText et = new EditText(this);
        et.setText(defaultValue);
        et.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                listener.onTextChanged(s.toString());
            }
        });
        layout.addView(et);
    }
    
    private CheckBox addCheckBox(LinearLayout layout, String text, boolean checked, 
                                 CheckChangedListener listener) {
        CheckBox cb = new CheckBox(this);
        cb.setText(text);
        cb.setChecked(checked);
        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listener.onCheckedChanged(isChecked);
        });
        layout.addView(cb);
        return cb;
    }
    
    private void addRemoveButton(LinearLayout row) {
        Button btnRemove = new Button(this);
        btnRemove.setText("حذف");
        btnRemove.setBackgroundColor(Color.parseColor("#F44336"));
        btnRemove.setTextColor(Color.WHITE);
        btnRemove.setOnClickListener(v -> {
            ViewGroup parent = (ViewGroup) row.getParent();
            if (parent != null) {
                parent.removeView(row);
            }
        });
        row.addView(btnRemove);
    }
    
    private String getArabicFieldName(String fieldName) {
        Map<String, String> arabicNames = new HashMap<>();
        arabicNames.put("id", "المعرف");
        arabicNames.put("name", "الاسم");
        arabicNames.put("created_at", "تاريخ الإنشاء");
        arabicNames.put("updated_at", "تاريخ التحديث");
        arabicNames.put("status", "الحالة");
        arabicNames.put("amount", "المبلغ");
        arabicNames.put("category", "الفئة");
        arabicNames.put("user_id", "المستخدم");
        arabicNames.put("description", "الوصف");
        arabicNames.put("price", "السعر");
        arabicNames.put("quantity", "الكمية");
        arabicNames.put("total", "الإجمالي");
        
        return arabicNames.getOrDefault(fieldName, fieldName);
    }
    
    private String toCamelCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String[] words = text.split("[\\s_]+");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase());
            }
        }
        
        return result.toString().replaceAll("[^a-zA-Z0-9]", "");
    }
    
    private String toSnakeCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String result = text.replaceAll("([a-z])([A-Z])", "$1_$2")
                           .replaceAll("[\\s-]+", "_")
                           .toLowerCase();
        
        return result.replaceAll("[^a-zA-Z0-9_]", "");
    }
    
    // Interface for listeners
    interface TextChangedListener {
        void onTextChanged(String text);
    }
    
    interface CheckChangedListener {
        void onCheckedChanged(boolean checked);
    }
}