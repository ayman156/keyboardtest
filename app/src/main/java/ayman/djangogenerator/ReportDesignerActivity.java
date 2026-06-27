package ayman.djangogenerator;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ReportDesignerActivity extends AppCompatActivity {
    
    // UI Components
    private Spinner modelSpinner;
    private LinearLayout annotateContainer, relatedContainer, filterContainer;
    private Button btnSaveReport, btnAddAnnotate, btnAddFilter, btnGenerateFullCode;
    private Button btnExportExcel, btnExportPDF, btnPreviewTemplate;
    private EditText etReportName, etReportDescription, etLimit, etOffset;
    private RecyclerView rvSelectedFields;
    private CheckBox cbGroupBy, cbOrderBy, cbDistinct;
    
    // Data
    private DatabaseHelper dbHelper;
    private long projectId;
    private List<ModelObj> availableModels;
    private List<Field> selectedFields;
    private FieldsAdapter fieldsAdapter;
    // في onCreate
     
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myrep);
        
        // Initialize
        dbHelper = new DatabaseHelper(this);
        projectId = getIntent().getLongExtra("PROJECT_ID", -1);
        
        // Setup UI
        initViews();
        setupViews();
        loadModels();
    }

private void initViews() {
    // البحث عن جميع العناصر
    modelSpinner = findViewById(R.id.model_spinner);
    annotateContainer = findViewById(R.id.annotate_container);
    relatedContainer = findViewById(R.id.related_container);
    filterContainer = findViewById(R.id.filter_container);
    btnSaveReport = findViewById(R.id.btn_save_report);
    btnAddAnnotate = findViewById(R.id.btn_add_annotate);
    btnAddFilter = findViewById(R.id.btn_add_filter);
    etReportName = findViewById(R.id.et_report_name);
    etReportDescription = findViewById(R.id.et_report_description);
    rvSelectedFields = findViewById(R.id.rv_selected_fields); // هذا مهم!
    cbGroupBy = findViewById(R.id.cb_group_by);
    cbOrderBy = findViewById(R.id.cb_order_by);
    cbDistinct = findViewById(R.id.cb_distinct);
    etLimit = findViewById(R.id.et_limit);
    etOffset = findViewById(R.id.et_offset);
    btnExportExcel = findViewById(R.id.btnExportExcel);
    btnExportPDF = findViewById(R.id.btnExportPDF);
    btnPreviewTemplate = findViewById(R.id.btnPreviewTemplate);
    btnGenerateFullCode = findViewById(R.id.btnGenerateFullCode);
}

    private void setupViews() {
        // Setup RecyclerView
        selectedFields = new ArrayList<>();
        fieldsAdapter = new FieldsAdapter(this, selectedFields);
        rvSelectedFields.setLayoutManager(new LinearLayoutManager(this));
        rvSelectedFields.setAdapter(fieldsAdapter);
        
        // Setup listeners
        setupListeners();
        
        // Setup spinner listener
        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (availableModels != null && position < availableModels.size()) {
                    long modelId = availableModels.get(position).id;
                    loadModelRelations(modelId);
                    updateFieldsList(modelId);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // Setup field click listener
        fieldsAdapter.setOnFieldClickListener(position -> {
            Field field = selectedFields.get(position);
            showFieldOptionsDialog(field);
        });
    }
    
    private void setupListeners() {
        btnAddAnnotate.setOnClickListener(v -> addAnnotateRow());
        btnAddFilter.setOnClickListener(v -> addFilterRow());
        btnSaveReport.setOnClickListener(v -> generateDjangoQuery());
        btnGenerateFullCode.setOnClickListener(v -> generateFullDjangoCode());
        
        if (btnExportExcel != null) {
            btnExportExcel.setOnClickListener(v -> generateExcelCode());
        }
        
        if (btnExportPDF != null) {
            btnExportPDF.setOnClickListener(v -> generatePdfCode());
        }
        
        if (btnPreviewTemplate != null) {
            btnPreviewTemplate.setOnClickListener(v -> previewHtmlTemplate());
        }
    }
    
    private void loadModels() {
        // Get models from database
        availableModels = dbHelper.getModelsObjectsByProject(projectId);
        
        if (availableModels == null || availableModels.isEmpty()) {
            Toast.makeText(this, "لا توجد نماذج في هذا المشروع", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Populate spinner
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
    
    private void updateFieldsList(long modelId) {
        selectedFields.clear();
        
        // Get fields from database or create sample
        try {
            List<Field> fields = dbHelper.getFieldsByModelId(modelId);
            if (fields != null && !fields.isEmpty()) {
                selectedFields.addAll(fields);
            } else {
                createSampleFields();
            }
        } catch (Exception e) {
            createSampleFields();
        }
        
        fieldsAdapter.notifyDataSetChanged();
    }
    private void generateFullDjangoCode(){
        //كود كامل
    }
    private void createSampleFields() {
        // Create sample fields for testing
        String[] fieldNames = {"id", "name", "created_at", "status", "amount"};
        String[] fieldTypes = {"IntegerField", "CharField", "DateTimeField", "CharField", "DecimalField"};
        
        for (int i = 0; i < fieldNames.length; i++) {
            Field field = new Field(i + 1, fieldNames[i], fieldTypes[i]);
            field.setDisplayName(getArabicFieldName(fieldNames[i]));
            selectedFields.add(field);
        }
    }
    
    private String getArabicFieldName(String fieldName) {
        switch (fieldName) {
            case "id": return "المعرف";
            case "name": return "الاسم";
            case "created_at": return "تاريخ الإنشاء";
            case "status": return "الحالة";
            case "amount": return "المبلغ";
            default: return fieldName;
        }
    }
    
    private void loadModelRelations(long modelId) {
        relatedContainer.removeAllViews();
        
        // Get relations from database
        List<Field> relations = dbHelper.getRelationsByModelId(modelId);
        
        if (relations == null || relations.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("لا توجد علاقات مرتبطة");
            relatedContainer.addView(tv);
            return;
        }
        
        for (Field field : relations) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 8, 0, 8);
            
            CheckBox cb = new CheckBox(this);
            cb.setText(field.getName() + " (" + field.getType() + ")");
            cb.setTag(field);
            
            row.addView(cb);
            relatedContainer.addView(row);
        }
    }
    
    private void addAnnotateRow() {
        // Create row layout
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(16, 16, 16, 16);
        row.setBackgroundResource(android.R.drawable.editbox_background);
        
        // Function spinner
        Spinner funcSpinner = new Spinner(this);
        String[] functions = {"Count", "Sum", "Avg", "Max", "Min"};
        ArrayAdapter<String> funcAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, functions
        );
        funcSpinner.setAdapter(funcAdapter);
        row.addView(funcSpinner);
        
        // Alias input
        EditText etAlias = new EditText(this);
        etAlias.setHint("اسم النتيجة (مثال: total_count)");
        row.addView(etAlias);
        
        // Field input
        EditText etField = new EditText(this);
        etField.setHint("اسم الحقل (مثال: id)");
        row.addView(etField);
        
        // Remove button
        Button btnRemove = new Button(this);
        btnRemove.setText("حذف");
        btnRemove.setOnClickListener(v -> annotateContainer.removeView(row));
        row.addView(btnRemove);
        
        annotateContainer.addView(row);
    }
    
    private void addFilterRow() {
        // Create row layout
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(16, 16, 16, 16);
        row.setBackgroundResource(android.R.drawable.editbox_background);
        
        // Field input
        EditText etField = new EditText(this);
        etField.setHint("اسم الحقل");
        row.addView(etField);
        
        // Operator spinner
        Spinner operatorSpinner = new Spinner(this);
        String[] operators = {"=", "!=", ">", "<", ">=", "<=", "contains", "startswith", "isnull"};
        ArrayAdapter<String> operatorAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, operators
        );
        operatorSpinner.setAdapter(operatorAdapter);
        row.addView(operatorSpinner);
        
        // Value input
        EditText etValue = new EditText(this);
        etValue.setHint("القيمة");
        row.addView(etValue);
        
        // Remove button
        Button btnRemove = new Button(this);
        btnRemove.setText("حذف");
        btnRemove.setOnClickListener(v -> filterContainer.removeView(row));
        row.addView(btnRemove);
        
        filterContainer.addView(row);
    }
    
    private void showFieldOptionsDialog(Field field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("خيارات الحقل: " + field.getDisplayName());
        
        // Create dialog content
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);
        
        // Display name
        TextView tvLabel = new TextView(this);
        tvLabel.setText("اسم العرض:");
        layout.addView(tvLabel);
        
        EditText etDisplayName = new EditText(this);
        etDisplayName.setText(field.getDisplayName());
        layout.addView(etDisplayName);
        
        // Include in report
        CheckBox cbInclude = new CheckBox(this);
        cbInclude.setText("تضمين في التقرير");
        cbInclude.setChecked(field.isIncludeInReport());
        layout.addView(cbInclude);
        
        // Group By
        CheckBox cbGroupBy = new CheckBox(this);
        cbGroupBy.setText("استخدام في Group By");
        cbGroupBy.setChecked(field.isGroupBy());
        layout.addView(cbGroupBy);
        
        // Order By
        CheckBox cbOrderBy = new CheckBox(this);
        cbOrderBy.setText("استخدام في Order By");
        cbOrderBy.setChecked(field.isOrderBy());
        layout.addView(cbOrderBy);
        
        // Order direction
        TextView tvOrderDir = new TextView(this);
        tvOrderDir.setText("اتجاه الترتيب:");
        layout.addView(tvOrderDir);
        
        Spinner spOrderDir = new Spinner(this);
        String[] directions = {"تصاعدي (ASC)", "تنازلي (DESC)"};
        ArrayAdapter<String> dirAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, directions
        );
        spOrderDir.setAdapter(dirAdapter);
        spOrderDir.setSelection(field.getOrderDirection().equals("DESC") ? 1 : 0);
        layout.addView(spOrderDir);
        
        builder.setView(layout);
        
        builder.setPositiveButton("حفظ", (dialog, which) -> {
            field.setDisplayName(etDisplayName.getText().toString());
            field.setIncludeInReport(cbInclude.isChecked());
            field.setGroupBy(cbGroupBy.isChecked());
            field.setOrderBy(cbOrderBy.isChecked());
            field.setOrderDirection(spOrderDir.getSelectedItemPosition() == 1 ? "DESC" : "ASC");
            fieldsAdapter.notifyDataSetChanged();
        });
        
        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }
    
    private void generateDjangoQuery() {
        try {
            // Validate inputs
            String reportName = etReportName.getText().toString().trim();
            if (reportName.isEmpty()) {
                Toast.makeText(this, "يرجى إدخال اسم التقرير", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (modelSpinner.getSelectedItem() == null) {
                Toast.makeText(this, "يرجى اختيار نموذج", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Build Django query
            StringBuilder query = new StringBuilder();
            String modelName = modelSpinner.getSelectedItem().toString();
            
            // Header
            query.append("# تقرير: ").append(reportName).append("\n");
            query.append("# ").append(etReportDescription.getText().toString()).append("\n\n");
            query.append("from django.db.models import Count, Sum, Avg, Max, Min, Q, F\n\n");
            
            // Start query
            query.append("data = ").append(modelName).append(".objects");
            
            // Add select_related and prefetch_related
            addRelatedJoins(query);
            
            // Add filters
            addFilters(query);
            
            // Add annotate
            addAnnotates(query);
            
            // Add group by
            addGroupBy(query);
            
            // Add order by
            addOrderBy(query);
            
            // Add distinct
            if (cbDistinct.isChecked()) {
                query.append("\n    .distinct()");
            }
            
            // Add limit/offset
            addLimitOffset(query);
            
            query.append("\n\n# الاستعلام جاهز للاستخدام");
            
            // Show result
            showCodeDialog("استعلام Django", query.toString());
            
        } catch (Exception e) {
            Toast.makeText(this, "خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void addRelatedJoins(StringBuilder query) {
        List<String> selectRelated = new ArrayList<>();
        List<String> prefetchRelated = new ArrayList<>();
        
        for (int i = 0; i < relatedContainer.getChildCount(); i++) {
            View view = relatedContainer.getChildAt(i);
            if (view instanceof LinearLayout) {
                LinearLayout row = (LinearLayout) view;
                for (int j = 0; j < row.getChildCount(); j++) {
                    View child = row.getChildAt(j);
                    if (child instanceof CheckBox) {
                        CheckBox cb = (CheckBox) child;
                        if (cb.isChecked() && cb.getTag() instanceof Field) {
                            Field field = (Field) cb.getTag();
                            if (field.getType().equals("ManyToManyField")) {
                                prefetchRelated.add("'" + field.getName() + "'");
                            } else {
                                selectRelated.add("'" + field.getName() + "'");
                            }
                        }
                    }
                }
            }
        }
        
        if (!selectRelated.isEmpty()) {
            query.append("\n    .select_related(").append(String.join(", ", selectRelated)).append(")");
        }
        
        if (!prefetchRelated.isEmpty()) {
            query.append("\n    .prefetch_related(").append(String.join(", ", prefetchRelated)).append(")");
        }
    }
    
    private void addFilters(StringBuilder query) {
        List<String> filters = new ArrayList<>();
        
        for (int i = 0; i < filterContainer.getChildCount(); i++) {
            View row = filterContainer.getChildAt(i);
            if (row instanceof LinearLayout) {
                String filter = extractFilterFromRow((LinearLayout) row);
                if (!filter.isEmpty()) {
                    filters.add(filter);
                }
            }
        }
        
        if (!filters.isEmpty()) {
            query.append("\n    .filter(");
            for (int i = 0; i < filters.size(); i++) {
                if (i > 0) query.append(" & ");
                query.append(filters.get(i));
            }
            query.append(")");
        }
    }
    
    private String extractFilterFromRow(LinearLayout row) {
        try {
            String field = "";
            String operator = "=";
            String value = "";
            
            // Extract values from row
            for (int i = 0; i < row.getChildCount(); i++) {
                View child = row.getChildAt(i);
                if (child instanceof EditText) {
                    EditText et = (EditText) child;
                    String hint = et.getHint() != null ? et.getHint().toString() : "";
                    if (hint.contains("الحقل")) {
                        field = et.getText().toString();
                    } else if (hint.contains("القيمة")) {
                        value = et.getText().toString();
                    }
                } else if (child instanceof Spinner) {
                    Spinner spinner = (Spinner) child;
                    operator = spinner.getSelectedItem().toString();
                }
            }
            
            if (field.isEmpty()) return "";
            
            // Build filter string
            switch (operator) {
                case "=":
                    return field + "='" + value + "'";
                case "!=":
                    return "~Q(" + field + "='" + value + "')";
                case "contains":
                    return field + "__icontains='" + value + "'";
                case "startswith":
                    return field + "__istartswith='" + value + "'";
                case "isnull":
                    return field + "__isnull=" + (value.isEmpty() ? "True" : value);
                default:
                    return field + "__" + operator + "=" + value;
            }
        } catch (Exception e) {
            return "";
        }
    }
    
    private void addAnnotates(StringBuilder query) {
        List<String> annotates = new ArrayList<>();
        
        for (int i = 0; i < annotateContainer.getChildCount(); i++) {
            View row = annotateContainer.getChildAt(i);
            if (row instanceof LinearLayout) {
                String annotate = extractAnnotateFromRow((LinearLayout) row);
                if (!annotate.isEmpty()) {
                    annotates.add(annotate);
                }
            }
        }
        
        if (!annotates.isEmpty()) {
            query.append("\n    .annotate(");
            for (int i = 0; i < annotates.size(); i++) {
                query.append("\n        ").append(annotates.get(i));
                if (i < annotates.size() - 1) query.append(",");
            }
            query.append("\n    )");
        }
    }
    
    private String extractAnnotateFromRow(LinearLayout row) {
        try {
            String function = "Count";
            String alias = "";
            String field = "";
            
            // Extract values from row
            for (int i = 0; i < row.getChildCount(); i++) {
                View child = row.getChildAt(i);
                if (child instanceof Spinner) {
                    Spinner spinner = (Spinner) child;
                    function = spinner.getSelectedItem().toString();
                } else if (child instanceof EditText) {
                    EditText et = (EditText) child;
                    String hint = et.getHint() != null ? et.getHint().toString() : "";
                    if (hint.contains("النتيجة")) {
                        alias = et.getText().toString();
                    } else if (hint.contains("الحقل")) {
                        field = et.getText().toString();
                    }
                }
            }
            
            if (alias.isEmpty() || field.isEmpty()) return "";
            return alias + "=" + function + "('" + field + "')";
        } catch (Exception e) {
            return "";
        }
    }
    
    private void addGroupBy(StringBuilder query) {
        if (cbGroupBy.isChecked()) {
            List<String> groupFields = new ArrayList<>();
            for (Field field : selectedFields) {
                if (field.isGroupBy()) {
                    groupFields.add("'" + field.getName() + "'");
                }
            }
            
            if (!groupFields.isEmpty()) {
                query.append("\n    .values(").append(String.join(", ", groupFields)).append(")");
            }
        }
    }
    
    private void addOrderBy(StringBuilder query) {
        if (cbOrderBy.isChecked()) {
            List<String> orderFields = new ArrayList<>();
            for (Field field : selectedFields) {
                if (field.isOrderBy()) {
                    String prefix = field.getOrderDirection().equals("DESC") ? "-" : "";
                    orderFields.add("'" + prefix + field.getName() + "'");
                }
            }
            
            if (!orderFields.isEmpty()) {
                query.append("\n    .order_by(").append(String.join(", ", orderFields)).append(")");
            }
        }
    }
    
    private void addLimitOffset(StringBuilder query) {
        String limit = etLimit != null ? etLimit.getText().toString().trim() : "";
        String offset = etOffset != null ? etOffset.getText().toString().trim() : "";
        
        if (!limit.isEmpty()) {
            try {
                int limitValue = Integer.parseInt(limit);
                query.append("\n    [:").append(limitValue).append("]");
            } catch (NumberFormatException e) {
                // Ignore invalid number
            }
        } else if (!offset.isEmpty()) {
            try {
                int offsetValue = Integer.parseInt(offset);
                query.append("\n    [").append(offsetValue).append(":]");
            } catch (NumberFormatException e) {
                // Ignore invalid number
            }
        }
    }
    
    private void generateExcelCode() {
        StringBuilder code = new StringBuilder();
        code.append("# تصدير إلى Excel\n");
        code.append("import pandas as pd\n");
        code.append("from django.http import HttpResponse\n");
        code.append("from django.utils import timezone\n\n");
        
        code.append("def export_to_excel(request, queryset):\n");
        code.append("    \"\"\"تصدير QuerySet إلى ملف Excel\"\"\"\n");
        code.append("    # إنشاء اسم الملف\n");
        code.append("    filename = 'report_' + timezone.now().strftime('%Y%m%d_%H%M%S') + '.xlsx'\n");
        code.append("    \n");
        code.append("    # إعداد الاستجابة\n");
        code.append("    response = HttpResponse(content_type='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')\n");
        code.append("    response['Content-Disposition'] = f'attachment; filename=\"{filename}\"'\n");
        code.append("    \n");
        code.append("    # تحويل QuerySet إلى DataFrame\n");
        code.append("    data = list(queryset.values())\n");
        code.append("    df = pd.DataFrame(data)\n");
        code.append("    \n");
        code.append("    # تصدير إلى Excel\n");
        code.append("    df.to_excel(response, index=False)\n");
        code.append("    \n");
        code.append("    return response\n");
        
        showCodeDialog("كود تصدير Excel", code.toString());
    }
    
    private void generatePdfCode() {
        StringBuilder code = new StringBuilder();
        code.append("# تصدير إلى PDF\n");
        code.append("from reportlab.pdfgen import canvas\n");
        code.append("from reportlab.lib.pagesizes import A4\n");
        code.append("from reportlab.lib import colors\n");
        code.append("from reportlab.platypus import Table, TableStyle\n");
        code.append("from django.http import HttpResponse\n");
        code.append("from django.utils import timezone\n\n");
        
        code.append("def export_to_pdf(request, queryset, title=None):\n");
        code.append("    \"\"\"تصدير QuerySet إلى ملف PDF\"\"\"\n");
        code.append("    # إنشاء اسم الملف\n");
        code.append("    filename = 'report_' + timezone.now().strftime('%Y%m%d_%H%M%S') + '.pdf'\n");
        code.append("    \n");
        code.append("    # إعداد الاستجابة\n");
        code.append("    response = HttpResponse(content_type='application/pdf')\n");
        code.append("    response['Content-Disposition'] = f'attachment; filename=\"{filename}\"'\n");
        code.append("    \n");
        code.append("    # إنشاء PDF\n");
        code.append("    p = canvas.Canvas(response, pagesize=A4)\n");
        code.append("    width, height = A4\n");
        code.append("    \n");
        code.append("    # العنوان\n");
        code.append("    if title is None:\n");
        code.append("        title = 'تقرير'\n");
        code.append("    \n");
        code.append("    p.setFont(\"Helvetica-Bold\", 16)\n");
        code.append("    p.drawString(50, height - 50, title)\n");
        code.append("    \n");
        code.append("    # التاريخ\n");
        code.append("    p.setFont(\"Helvetica\", 10)\n");
        code.append("    p.drawString(50, height - 70, f\"تاريخ التصدير: {timezone.now().strftime('%Y-%m-%d %H:%M')}\")\n");
        code.append("    \n");
        code.append("    # إعداد جدول البيانات\n");
        code.append("    data = []\n");
        code.append("    \n");
        code.append("    # رأس الجدول\n");
        code.append("    headers = [");
        
        // Add headers from selected fields
        boolean first = true;
        for (Field field : selectedFields) {
            if (field.isIncludeInReport()) {
                if (!first) code.append(", ");
                code.append("'").append(field.getDisplayName()).append("'");
                first = false;
            }
        }
        
        code.append("]\n");
        code.append("    data.append(headers)\n");
        code.append("    \n");
        code.append("    # بيانات الجدول\n");
        code.append("    for item in queryset:\n");
        code.append("        row = []\n");
        
        for (Field field : selectedFields) {
            if (field.isIncludeInReport()) {
                code.append("        row.append(str(item.").append(field.getName()).append("))\n");
            }
        }
        
        code.append("        data.append(row)\n");
        code.append("    \n");
        code.append("    # إنشاء الجدول\n");
        code.append("    table = Table(data)\n");
        code.append("    table.setStyle(TableStyle([\n");
        code.append("        ('BACKGROUND', (0, 0), (-1, 0), colors.grey),\n");
        code.append("        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),\n");
        code.append("        ('ALIGN', (0, 0), (-1, -1), 'CENTER'),\n");
        code.append("        ('GRID', (0, 0), (-1, -1), 1, colors.black),\n");
        code.append("    ]))\n");
        code.append("    \n");
        code.append("    # رسم الجدول\n");
        code.append("    table.wrapOn(p, width, height)\n");
        code.append("    table.drawOn(p, 50, height - 200)\n");
        code.append("    \n");
        code.append("    # حفظ PDF\n");
        code.append("    p.showPage()\n");
        code.append("    p.save()\n");
        code.append("    \n");
        code.append("    return response\n");
        
        showCodeDialog("كود تصدير PDF", code.toString());
    }
    
    private void previewHtmlTemplate() {
        StringBuilder html = new StringBuilder();
        String reportName = etReportName.getText().toString();
        if (reportName.isEmpty()) reportName = "التقرير";
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"ar\" dir=\"rtl\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>").append(reportName).append("</title>\n");
        html.append("    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css\" rel=\"stylesheet\">\n");
        html.append("    <style>\n");
        html.append("        body { font-family: 'Arial', sans-serif; background-color: #f8f9fa; }\n");
        html.append("        .report-header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }\n");
        html.append("        .table-hover tbody tr:hover { background-color: rgba(0, 0, 0, 0.075); }\n");
        html.append("        .export-buttons { margin-top: 20px; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container mt-4\">\n");
        html.append("        <div class=\"report-header p-4 rounded shadow\">\n");
        html.append("            <h2>").append(reportName).append("</h2>\n");
        html.append("            <p class=\"mb-0\">").append(etReportDescription.getText().toString()).append("</p>\n");
        html.append("        </div>\n");
        html.append("        \n");
        html.append("        <div class=\"card mt-4 shadow\">\n");
        html.append("            <div class=\"card-header bg-white d-flex justify-content-between align-items-center\">\n");
        html.append("                <h5 class=\"mb-0\">البيانات</h5>\n");
        html.append("                <div class=\"export-buttons\">\n");
        html.append("                    <a href=\"#\" class=\"btn btn-success btn-sm\">\n");
        html.append("                        <i class=\"fas fa-file-excel\"></i> Excel\n");
        html.append("                    </a>\n");
        html.append("                    <a href=\"#\" class=\"btn btn-danger btn-sm\">\n");
        html.append("                        <i class=\"fas fa-file-pdf\"></i> PDF\n");
        html.append("                    </a>\n");
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
                html.append("                                <th>").append(field.getDisplayName()).append("</th>\n");
            }
        }
        
        html.append("                            </tr>\n");
        html.append("                        </thead>\n");
        html.append("                        <tbody>\n");
        html.append("                            <!-- البيانات ستظهر هنا -->\n");
        html.append("                            <tr>\n");
        
        // Sample data row
        for (Field field : selectedFields) {
            if (field.isIncludeInReport()) {
                html.append("                                <td>").append(field.getName()).append("</td>\n");
            }
        }
        
        html.append("                            </tr>\n");
        html.append("                        </tbody>\n");
        html.append("                    </table>\n");
        html.append("                </div>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("    \n");
        html.append("    <script src=\"https://kit.fontawesome.com/your-fontawesome-kit.js\"></script>\n");
        html.append("    <script src=\"https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js\"></script>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        showCodeDialog("معاينة قالب HTML", html.toString());
    }
    
    private void showCodeDialog(String title, String code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        
        // Create scrollable view
        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setText(code);
        textView.setTextSize(12);
        textView.setPadding(20, 20, 20, 20);
        textView.setTextIsSelectable(true);
        
        scrollView.addView(textView);
        builder.setView(scrollView);
        
        builder.setPositiveButton("نسخ الكود", (dialog, which) -> {
            copyToClipboard(code);
            Toast.makeText(this, "تم نسخ الكود", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("إغلاق", null);
        
        builder.show();
    }
    
    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Django Code", text);
        clipboard.setPrimaryClip(clip);
    }
}