package com.example.djangogenerator;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
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
public class ReportDesignerActivity extends AppCompatActivity {

    private Spinner modelSpinner, exportFormatSpinner;
    private LinearLayout annotateContainer, relatedContainer, filterContainer, aggregateContainer;
    private Button btnSaveReport, btnAddAnnotate, btnAddFilter, btnAddAggregate, btnGenerateFullCode;
    private Button btnExportExcel, btnExportPDF, btnPreviewTemplate;
    private EditText etReportName, etReportDescription;
    private RecyclerView rvSelectedFields;
    private CheckBox cbGroupBy, cbOrderBy, cbDistinct;
    private DatabaseHelper dbHelper;
    private long projectId;
    private List<ModelObj> availableModels;
    private List<Field> allFields;
    private List<Field> selectedFields;
    private FieldsAdapter fieldsAdapter;
    
    // للتعامل مع التصدير
    private Map<String, String> exportTemplates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_designer_enhanced);

        dbHelper = new DatabaseHelper(this);
        projectId = getIntent().getLongExtra("PROJECT_ID", -1);
        
        // تهيئة قوالب التصدير
        initExportTemplates();

        initViews();
        loadModels();
        loadAllFields();
    }

    private void initExportTemplates() {
        exportTemplates = new HashMap<>();
        // قوالب Excel
        exportTemplates.put("excel_simple", 
            "import pandas as pd\nfrom django.http import HttpResponse\n\n" +
            "def export_to_excel(request, queryset):\n" +
            "    response = HttpResponse(content_type='application/vnd.ms-excel')\n" +
    "    response['Content-Disposition'] = 'attachment; filename=\"report.xlsx\"'\n" +
            "    df = pd.DataFrame(list(queryset.values()))\n" +
            "    df.to_excel(response, index=False)\n" +
            "    return response");
        
        // قوالب PDF
        exportTemplates.put("pdf_simple",
            "from reportlab.pdfgen import canvas\n" +
            "from reportlab.lib.pagesizes import A4\n" +
            "from reportlab.lib import colors\n" +
            "from reportlab.platypus import Table, TableStyle\n" +
            "from django.http import HttpResponse\n\n" +
            "def export_to_pdf(request, queryset, headers, data_fields):\n" +
            "    response = HttpResponse(content_type='application/pdf')\n" +
    "    response['Content-Disposition'] = 'attachment; filename=\"report.pdf\"'\n" +
            "    p = canvas.Canvas(response, pagesize=A4)\n" +
            "    width, height = A4\n" +
            "    # رسم الجدول...\n" +
            "    return response");
    }

    private void initViews() {
        modelSpinner = findViewById(R.id.model_spinner);
        exportFormatSpinner = findViewById(R.id.export_format_spinner);
        annotateContainer = findViewById(R.id.annotate_container);
        relatedContainer = findViewById(R.id.related_container);
        filterContainer = findViewById(R.id.filter_container);
        aggregateContainer = findViewById(R.id.aggregate_container);
        btnSaveReport = findViewById(R.id.btn_save_report);
        btnAddAnnotate = findViewById(R.id.btn_add_annotate);
        btnAddFilter = findViewById(R.id.btn_add_filter);
        btnAddAggregate = findViewById(R.id.btn_add_aggregate);
        btnGenerateFullCode = findViewById(R.id.btn_generate_full_code);
        btnExportExcel = findViewById(R.id.btn_export_excel);
        btnExportPDF = findViewById(R.id.btn_export_pdf);
        btnPreviewTemplate = findViewById(R.id.btn_preview_template);
        etReportName = findViewById(R.id.et_report_name);
        etReportDescription = findViewById(R.id.et_report_description);
        rvSelectedFields = findViewById(R.id.rv_selected_fields);
        cbGroupBy = findViewById(R.id.cb_group_by);
        cbOrderBy = findViewById(R.id.cb_order_by);
        cbDistinct = findViewById(R.id.cb_distinct);
        
        // إعداد RecyclerView للحقول المحددة
        selectedFields = new ArrayList<>();
        fieldsAdapter = new FieldsAdapter(this, selectedFields);
        rvSelectedFields.setLayoutManager(new LinearLayoutManager(this));
        rvSelectedFields.setAdapter(fieldsAdapter);
        
        // إعداد Spinner لتنسيقات التصدير
        String[] exportFormats = {"Excel بسيط", "Excel متقدم", "PDF بسيط", "PDF مع تنسيق", "CSV", "JSON"};
        ArrayAdapter<String> exportAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, exportFormats);
        exportFormatSpinner.setAdapter(exportAdapter);

        setupListeners();
    }

    private void setupListeners() {
        btnAddAnnotate.setOnClickListener(v -> addAnnotateRow());
        btnAddFilter.setOnClickListener(v -> addFilterRow());
        btnAddAggregate.setOnClickListener(v -> addAggregateRow());
        btnSaveReport.setOnClickListener(v -> generateFinalQuery());
        btnGenerateFullCode.setOnClickListener(v -> generateFullDjangoCode());
        btnExportExcel.setOnClickListener(v -> generateExcelExportCode());
        btnExportPDF.setOnClickListener(v -> generatePDFExportCode());
        btnPreviewTemplate.setOnClickListener(v -> previewTemplate());

        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (availableModels != null && position < availableModels.size()) {
                    loadModelRelations(availableModels.get(position).id);
                    updateFieldsList(availableModels.get(position).id);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        // عند النقر على حقل في القائمة
        fieldsAdapter.setOnFieldClickListener(position -> {
            Field field = selectedFields.get(position);
            showFieldOptionsDialog(field);
        });
    }

    private void loadModels() {
        availableModels = dbHelper.getModelsObjectsByProject(projectId);
        List<String> names = new ArrayList<>();
        for (ModelObj m : availableModels) names.add(m.name);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, names);
        modelSpinner.setAdapter(adapter);
    }

    private void loadAllFields() {
        allFields = new ArrayList<>();
        for (ModelObj model : availableModels) {
            List<Field> modelFields = dbHelper.getFieldsByModelId(model.id);
            for (Field field : modelFields) {
                field.setModelName(model.name); // إضافة اسم النموذج للحقل
                allFields.add(field);
            }
        }
    }

    private void updateFieldsList(long modelId) {
        selectedFields.clear();
        List<Field> modelFields = dbHelper.getFieldsByModelId(modelId);
        selectedFields.addAll(modelFields);
        fieldsAdapter.notifyDataSetChanged();
    }
     private void showFieldOptionsDialog(Field field) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("خيارات الحقل: " + field.getName());
    
    // إنشاء واجهة مباشرة بدون ملف XML
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(32, 32, 32, 32);
    
    // اسم العرض
    TextView tvDisplayName = new TextView(this);
    tvDisplayName.setText("اسم العرض:");
    layout.addView(tvDisplayName);
    
    EditText etDisplayName = new EditText(this);
    etDisplayName.setText(field.getName());
    etDisplayName.setHint("اسم العرض");
    layout.addView(etDisplayName);
    
    // CheckBox للتضمين
    CheckBox cbInclude = new CheckBox(this);
    cbInclude.setText("تضمين في التقرير");
    cbInclude.setChecked(field.isIncludeInReport());
    layout.addView(cbInclude);
    
    // CheckBox لـ Group By
    CheckBox cbGroupBy = new CheckBox(this);
    cbGroupBy.setText("Group By");
    cbGroupBy.setChecked(field.isGroupBy());
    layout.addView(cbGroupBy);
    
    // CheckBox لـ Order By
    CheckBox cbOrderBy = new CheckBox(this);
    cbOrderBy.setText("Order By");
    cbOrderBy.setChecked(field.isOrderBy());
    layout.addView(cbOrderBy);
    
    builder.setView(layout);
    
    builder.setPositiveButton("حفظ", (dialog, which) -> {
        field.setDisplayName(etDisplayName.getText().toString());
        field.setIncludeInReport(cbInclude.isChecked());
        field.setGroupBy(cbGroupBy.isChecked());
        field.setOrderBy(cbOrderBy.isChecked());
        
        if (fieldsAdapter != null) {
            fieldsAdapter.notifyDataSetChanged();
        }
    });
    
    builder.setNegativeButton("إلغاء", null);
    builder.show();
}
    private void loadModelRelations(long modelId) {
        relatedContainer.removeAllViews();
        List<Field> relations = dbHelper.getRelationsByModelId(modelId);

        if (relations.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("لا توجد علاقات مرتبطة بهذا النموذج.");
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

            // زر لإظهار خيارات العلاقة
            Button btnOptions = new Button(this);
            btnOptions.setText("خيارات");
            btnOptions.setOnClickListener(v -> showRelationOptionsDialog(field));

            row.addView(cb);
            row.addView(btnOptions);
            relatedContainer.addView(row);
        }
    }

    /*
    private void addAnnotateRow() {
        View row = getLayoutInflater().inflate(R.layout.row_annotate_enhanced, null);
        
        Spinner funcSpinner = row.findViewById(R.id.func_spinner);
        String[] funcs = {"Count", "Sum", "Avg", "Max", "Min", "StdDev", "Variance"};
        funcSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, funcs));
        
        // Auto-complete للحقول
        AutoCompleteTextView fieldInput = row.findViewById(R.id.field_input);
        List<String> fieldNames = new ArrayList<>();
        for (Field field : allFields) {
            fieldNames.add(field.getModelName() + "." + field.getName());
        }
        ArrayAdapter<String> fieldAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, fieldNames);
        fieldInput.setAdapter(fieldAdapter);
        
        // خيارات إضافية
        CheckBox cbDistinct = row.findViewById(R.id.cb_annotate_distinct);
        CheckBox cbFilter = row.findViewById(R.id.cb_annotate_filter);
        
        row.findViewById(R.id.btn_remove).setOnClickListener(v -> annotateContainer.removeView(row));
        
        // زر إضافة فلتر للـ annotate
        row.findViewById(R.id.btn_add_filter_to_annotate).setOnClickListener(v -> {
            if (cbFilter.isChecked()) {
                addFilterToAnnotate(row);
            }
        });
        
        annotateContainer.addView(row);
    }
    */
    private void addAnnotateRow() {
    /* تعليق مؤقت
    View row = getLayoutInflater().inflate(R.layout.row_annotate_enhanced, null);
    ... */
    
    // بدلاً من ذلك، استخدم تخطيط بسيط
    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.VERTICAL);
    row.setPadding(16, 16, 16, 16);
    
    // إضافة عناصر بسيطة
    TextView label = new TextView(this);
    label.setText("دالة التجميع");
    row.addView(label);
    
    EditText etAlias = new EditText(this);
    etAlias.setHint("اسم المعطى");
    row.addView(etAlias);
    
    EditText etField = new EditText(this);
    etField.setHint("اسم الحقل");
    row.addView(etField);
    
    Button btnRemove = new Button(this);
    btnRemove.setText("حذف");
    btnRemove.setOnClickListener(v -> annotateContainer.removeView(row));
    row.addView(btnRemove);
    
    annotateContainer.addView(row);
}

private void addFilterRow() {
    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.VERTICAL);
    row.setPadding(16, 16, 16, 16);
    
    // حقل الفلتر
    EditText etField = new EditText(this);
    etField.setHint("اسم الحقل");
    row.addView(etField);
    
    EditText etValue = new EditText(this);
    etValue.setHint("القيمة");
    row.addView(etValue);
    
    Button btnRemove = new Button(this);
    btnRemove.setText("حذف");
    btnRemove.setOnClickListener(v -> filterContainer.removeView(row));
    row.addView(btnRemove);
    
    filterContainer.addView(row);
}

    private void addFilterRow() {
        View row = getLayoutInflater().inflate(R.layout.row_filter, null);
        
        Spinner fieldSpinner = row.findViewById(R.id.filter_field_spinner);
        Spinner operatorSpinner = row.findViewById(R.id.filter_operator_spinner);
        EditText etValue = row.findViewById(R.id.filter_value);
        Spinner logicSpinner = row.findViewById(R.id.filter_logic_spinner);
        
        // تعبئة الحقول
        List<String> fieldOptions = new ArrayList<>();
        fieldOptions.add("-- اختر حقل --");
        for (Field field : selectedFields) {
            fieldOptions.add(field.getName());
        }
        ArrayAdapter<String> fieldAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, fieldOptions);
        fieldSpinner.setAdapter(fieldAdapter);
        
        // مشغلي الفلتر
        String[] operators = {"=", "!=", ">", "<", ">=", "<=", "contains", "startswith", 
                              "endswith", "in", "isnull", "range"};
        ArrayAdapter<String> operatorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, operators);
        operatorSpinner.setAdapter(operatorAdapter);
        
        // AND/OR
        String[] logic = {"AND", "OR", "AND NOT", "OR NOT"};
        ArrayAdapter<String> logicAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, logic);
        logicSpinner.setAdapter(logicAdapter);
        
        row.findViewById(R.id.btn_remove_filter).setOnClickListener(v -> 
            filterContainer.removeView(row));
            
        filterContainer.addView(row);
    }

    private void addAggregateRow() {
        View row = getLayoutInflater().inflate(R.layout.row_aggregate, null);
        
        Spinner aggregateSpinner = row.findViewById(R.id.aggregate_spinner);
        String[] aggregates = {"Count", "Sum", "Avg", "Min", "Max", "StdDev", "Variance"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, aggregates);
        aggregateSpinner.setAdapter(adapter);
        
        row.findViewById(R.id.btn_remove_aggregate).setOnClickListener(v -> 
            aggregateContainer.removeView(row));
            
        aggregateContainer.addView(row);
    }

    private void addFilterToAnnotate(View annotateRow) {
        View filterRow = getLayoutInflater().inflate(R.layout.row_annotate_filter, null);
        
        // ربط الفلتر بـ annotateRow
        LinearLayout filterContainer = annotateRow.findViewById(R.id.annotate_filter_container);
        if (filterContainer != null) {
            filterContainer.addView(filterRow);
        }
    }
    /*

    private void showFieldOptionsDialog(Field field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("خيارات الحقل: " + field.getName());
        
        View view = getLayoutInflater().inflate(R.layout.dialog_field_options, null);
        
        CheckBox cbInclude = view.findViewById(R.id.cb_include_in_report);
        CheckBox cbGroupBy = view.findViewById(R.id.cb_group_by_field);
        CheckBox cbOrderBy = view.findViewById(R.id.cb_order_by_field);
        Spinner spOrderDirection = view.findViewById(R.id.sp_order_direction);
        EditText etDisplayName = view.findViewById(R.id.et_display_name);
        EditText etFormat = view.findViewById(R.id.et_format);
        
        // تعيين القيم الحالية
        etDisplayName.setText(field.getDisplayName());
        
        builder.setView(view);
        
        builder.setPositiveButton("حفظ", (dialog, which) -> {
            field.setDisplayName(etDisplayName.getText().toString());
            field.setIncludeInReport(cbInclude.isChecked());
            field.setGroupBy(cbGroupBy.isChecked());
            field.setOrderBy(cbOrderBy.isChecked());
            field.setOrderDirection(spOrderDirection.getSelectedItem().toString());
            field.setFormat(etFormat.getText().toString());
            
            fieldsAdapter.notifyDataSetChanged();
        });
        
        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }
    */

    private void showRelationOptionsDialog(Field field) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("خيارات العلاقة: " + field.getName());
        
        View view = getLayoutInflater().inflate(R.layout.dialog_relation_options, null);
        
        Spinner spJoinType = view.findViewById(R.id.sp_join_type);
        CheckBox cbOnly = view.findViewById(R.id.cb_only_fields);
        CheckBox cbDefer = view.findViewById(R.id.cb_defer_fields);
        EditText etPrefetchTo = view.findViewById(R.id.et_prefetch_to);
        
        String[] joinTypes = {"INNER JOIN", "LEFT JOIN", "SELECT RELATED", "PREFETCH RELATED"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, joinTypes);
        spJoinType.setAdapter(adapter);
        
        builder.setView(view);
        
        builder.setPositiveButton("تطبيق", (dialog, which) -> {
            // حفظ خيارات العلاقة
            field.setJoinType(spJoinType.getSelectedItem().toString());
            field.setUseOnly(cbOnly.isChecked());
            field.setUseDefer(cbDefer.isChecked());
            field.setPrefetchTo(etPrefetchTo.getText().toString());
        });
        
        builder.setNegativeButton("إلغاء", null);
        builder.show();
    }

    private void generateFinalQuery() {
        try {
            String reportName = etReportName.getText().toString();
            String description = etReportDescription.getText().toString();
            
            if (reportName.isEmpty()) {
                Toast.makeText(this, "يرجى إدخال اسم التقرير", Toast.LENGTH_SHORT).show();
                return;
            }
            
            StringBuilder djangoCode = new StringBuilder();
            String baseModel = modelSpinner.getSelectedItem().toString();
            
            // بداية الاستعلام
            djangoCode.append("# تقرير: ").append(reportName).append("\n");
            djangoCode.append("# ").append(description).append("\n\n");
            djangoCode.append("from django.db.models import ");
            
            // إضافة الواردات المطلوبة
            List<String> imports = new ArrayList<>();
            imports.add("Count");
            imports.add("Sum");
            imports.add("Avg");
            imports.add("Max");
            imports.add("Min");
            imports.add("Q");
            imports.add("F");
            
            // التحقق مما إذا كانت هناك تواريخ للتعامل مع Trunc
            boolean hasDateFields = selectedFields.stream()
                .anyMatch(f -> f.getType().contains("Date"));
            if (hasDateFields) {
                imports.add("TruncDate");
                imports.add("TruncMonth");
                imports.add("TruncYear");
            }
            
            djangoCode.append(String.join(", ", imports)).append("\n\n");
            
            // بناء الاستعلام الأساسي
            djangoCode.append("query = ").append(baseModel).append(".objects");
            
            // 1. select_related و prefetch_related
            List<String> selectRelated = new ArrayList<>();
            List<String> prefetchRelated = new ArrayList<>();
            List<String> onlyFields = new ArrayList<>();
            List<String> deferFields = new ArrayList<>();
            
            for (int i = 0; i < relatedContainer.getChildCount(); i++) {
                View v = relatedContainer.getChildAt(i);
                if (v instanceof LinearLayout) {
                    LinearLayout row = (LinearLayout) v;
                    CheckBox cb = (CheckBox) row.getChildAt(0);
                    if (cb.isChecked()) {
                        Field field = (Field) cb.getTag();
                        if (field.getType().equals("ManyToManyField")) {
                            prefetchRelated.add("'" + field.getName() + "'");
                        } else {
                            selectRelated.add("'" + field.getName() + "'");
                        }
                        
                        if (field.isUseOnly()) {
                            onlyFields.add("'" + field.getName() + "'");
                        }
                        if (field.isUseDefer()) {
                            deferFields.add("'" + field.getName() + "'");
                        }
                    }
                }
            }
            
            if (!selectRelated.isEmpty()) {
                djangoCode.append("\n    .select_related(").append(String.join(", ", selectRelated)).append(")");
            }
            
            if (!prefetchRelated.isEmpty()) {
                djangoCode.append("\n    .prefetch_related(").append(String.join(", ", prefetchRelated)).append(")");
            }
            
            if (!onlyFields.isEmpty()) {
                djangoCode.append("\n    .only(").append(String.join(", ", onlyFields)).append(")");
            }
            
            if (!deferFields.isEmpty()) {
                djangoCode.append("\n    .defer(").append(String.join(", ", deferFields)).append(")");
            }
            
            // 2. الفلاتر
            List<String> filters = new ArrayList<>();
            for (int i = 0; i < filterContainer.getChildCount(); i++) {
                View row = filterContainer.getChildAt(i);
                if (row != null) {
                    String filter = buildFilterFromRow(row);
                    if (!filter.isEmpty()) {
                        filters.add(filter);
                    }
                }
            }
            
            if (!filters.isEmpty()) {
                djangoCode.append("\n    .filter(");
                for (int i = 0; i < filters.size(); i++) {
                    if (i > 0) djangoCode.append(" & ");
                    djangoCode.append(filters.get(i));
                }
                djangoCode.append(")");
            }
            
            // 3. annotate
            if (annotateContainer.getChildCount() > 0) {
                djangoCode.append("\n    .annotate(");
                for (int i = 0; i < annotateContainer.getChildCount(); i++) {
                    View row = annotateContainer.getChildAt(i);
                    String annotate = buildAnnotateFromRow(row);
                    if (!annotate.isEmpty()) {
                        djangoCode.append("\n        ").append(annotate);
                        if (i < annotateContainer.getChildCount() - 1) {
                            djangoCode.append(",");
                        }
                    }
                }
                djangoCode.append("\n    )");
            }
            
            // 4. aggregate
            if (aggregateContainer.getChildCount() > 0) {
                djangoCode.append("\n    .aggregate(");
                for (int i = 0; i < aggregateContainer.getChildCount(); i++) {
                    View row = aggregateContainer.getChildAt(i);
                    String aggregate = buildAggregateFromRow(row);
                    if (!aggregate.isEmpty()) {
                        djangoCode.append("\n        ").append(aggregate);
                        if (i < aggregateContainer.getChildCount() - 1) {
                            djangoCode.append(",");
                        }
                    }
                }
                djangoCode.append("\n    )");
            }
            
            // 5. Group By
            if (cbGroupBy.isChecked()) {
                List<String> groupFields = new ArrayList<>();
                for (Field field : selectedFields) {
                    if (field.isGroupBy()) {
                        groupFields.add("'" + field.getName() + "'");
                    }
                }
                if (!groupFields.isEmpty()) {
                    djangoCode.append("\n    .values(").append(String.join(", ", groupFields)).append(")");
                }
            }
            
            // 6. Order By
            if (cbOrderBy.isChecked()) {
                List<String> orderFields = new ArrayList<>();
                for (Field field : selectedFields) {
                    if (field.isOrderBy()) {
                        String direction = field.getOrderDirection().equals("DESC") ? "-" : "";
                        orderFields.add("'" + direction + field.getName() + "'");
                    }
                }
                if (!orderFields.isEmpty()) {
                    djangoCode.append("\n    .order_by(").append(String.join(", ", orderFields)).append(")");
                }
            }
            
            // 7. Distinct
            if (cbDistinct.isChecked()) {
                djangoCode.append("\n    .distinct()");
            }
            
            // 8. التقطيع (Slicing)
            EditText etLimit = findViewById(R.id.et_limit);
            EditText etOffset = findViewById(R.id.et_offset);
            
            String limit = etLimit.getText().toString();
            String offset = etOffset.getText().toString();
            
            if (!limit.isEmpty()) {
                djangoCode.append("\n    [:").append(limit).append("]");
            } else if (!offset.isEmpty()) {
                djangoCode.append("\n    [").append(offset).append(":]");
            }
            
            djangoCode.append("\n\n# استخدم query لاحقاً في الـ view");
            
            // حفظ التقرير في قاعدة البيانات
            saveReportToDatabase(reportName, description, djangoCode.toString());
            
            // عرض النتيجة
            showResultDialog(djangoCode.toString());
            
        } catch (Exception e) {
            Toast.makeText(this, "خطأ في بناء الاستعلام: " + e.getMessage(), 
                          Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private String buildFilterFromRow(View row) {
        try {
            Spinner fieldSpinner = row.findViewById(R.id.filter_field_spinner);
            Spinner operatorSpinner = row.findViewById(R.id.filter_operator_spinner);
            EditText etValue = row.findViewById(R.id.filter_value);
            Spinner logicSpinner = row.findViewById(R.id.filter_logic_spinner);
            
            String field = fieldSpinner.getSelectedItem().toString();
            String operator = operatorSpinner.getSelectedItem().toString();
            String value = etValue.getText().toString();
            String logic = logicSpinner.getSelectedItem().toString();
            
            if (field.equals("-- اختر حقل --") || value.isEmpty()) {
                return "";
            }
            
            StringBuilder filter = new StringBuilder();
            
            if (!logic.equals("AND")) {
                filter.append("Q(");
            }
            
            filter.append(field);
            
            switch (operator) {
                case "=":
                    filter.append("=").append(formatValue(value));
                    break;
                case "!=":
                    filter.append("!=").append(formatValue(value));
                    break;
                case ">":
                case "<":
                case ">=":
                case "<=":
                    filter.append("__").append(operator.toLowerCase())
                          .append("=").append(formatValue(value));
                    break;
                case "contains":
                    filter.append("__icontains=").append(formatValue(value));
                    break;
                case "startswith":
                    filter.append("__istartswith=").append(formatValue(value));
                    break;
                case "endswith":
                    filter.append("__iendswith=").append(formatValue(value));
                    break;
                case "in":
                    filter.append("__in=[").append(formatListValue(value)).append("]");
                    break;
                case "isnull":
                    filter.append("__isnull=").append(value.equalsIgnoreCase("true"));
                    break;
                case "range":
                    String[] range = value.split(",");
                    if (range.length == 2) {
                        filter.append("__range=(").append(formatValue(range[0]))
                              .append(", ").append(formatValue(range[1])).append(")");
                    }
                    break;
            }
            
            if (!logic.equals("AND")) {
                filter.append(")");
            }
            
            return filter.toString();
            
        } catch (Exception e) {
            return "";
        }
    }

    private String buildAnnotateFromRow(View row) {
        try {
            Spinner funcSpinner = row.findViewById(R.id.func_spinner);
            EditText etAlias = row.findViewById(R.id.alias_input);
            AutoCompleteTextView fieldInput = row.findViewById(R.id.field_input);
            CheckBox cbDistinct = row.findViewById(R.id.cb_annotate_distinct);
            
            String func = funcSpinner.getSelectedItem().toString();
            String alias = etAlias.getText().toString();
            String field = fieldInput.getText().toString();
            
            if (alias.isEmpty() || field.isEmpty()) {
                return "";
            }
            
            StringBuilder annotate = new StringBuilder();
            annotate.append(alias).append("=").append(func).append("('").append(field).append("'");
            
            if (cbDistinct.isChecked()) {
                annotate.append(", distinct=True");
            }
            
            // إضافة فلتر إذا كان موجوداً
            LinearLayout filterContainer = row.findViewById(R.id.annotate_filter_container);
            if (filterContainer != null && filterContainer.getChildCount() > 0) {
                View filterRow = filterContainer.getChildAt(0);
                String filter = buildFilterFromRow(filterRow);
                if (!filter.isEmpty()) {
                    annotate.append(", filter=").append(filter);
                }
            }
            
            annotate.append(")");
            return annotate.toString();
            
        } catch (Exception e) {
            return "";
        }
    }

    private String buildAggregateFromRow(View row) {
        try {
            Spinner aggregateSpinner = row.findViewById(R.id.aggregate_spinner);
            EditText etAggregateField = row.findViewById(R.id.et_aggregate_field);
            EditText etAggregateAlias = row.findViewById(R.id.et_aggregate_alias);
            
            String func = aggregateSpinner.getSelectedItem().toString();
            String field = etAggregateField.getText().toString();
            String alias = etAggregateAlias.getText().toString();
            
            if (func.isEmpty() || field.isEmpty()) {
                return "";
            }
            
            if (alias.isEmpty()) {
                alias = field.toLowerCase() + "_" + func.toLowerCase();
            }
            
            return alias + "=" + func + "('" + field + "')";
            
        } catch (Exception e) {
            return "";
        }
    }

    private String formatValue(String value) {
        // محاولة معرفة نوع القيمة
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return value;
        }
        
        try {
            Double.parseDouble(value);
            return value;
        } catch (NumberFormatException e) {
            return "'" + value + "'";
        }
    }

    private String formatListValue(String value) {
        String[] items = value.split(",");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
            result.append(formatValue(items[i].trim()));
            if (i < items.length - 1) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    private void saveReportToDatabase(String name, String description, String code) {
        // حفظ التقرير في قاعدة البيانات المحلية
        boolean saved = dbHelper.saveReport(projectId, name, description, code);
        
        if (saved) {
            Toast.makeText(this, "تم حفظ التقرير بنجاح", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "خطأ في حفظ التقرير", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateFullDjangoCode() {
        // توليد كود Django كامل مع views و templates
        Intent intent = new Intent(this, FullCodeGeneratorActivity.class);
        intent.putExtra("PROJECT_ID", projectId);
        intent.putExtra("REPORT_NAME", etReportName.getText().toString());
        startActivity(intent);
    }

    private void generateExcelExportCode() {
        String template = exportTemplates.get("excel_simple");
        String selectedFormat = exportFormatSpinner.getSelectedItem().toString();
        
        StringBuilder excelCode = new StringBuilder();
        excelCode.append("# كود تصدير إلى Excel - ").append(selectedFormat).append("\n\n");
        
        if (selectedFormat.contains("متقدم")) {
            excelCode.append("import pandas as pd\n");
            excelCode.append("from django.http import HttpResponse\n");
            excelCode.append("from django.utils import timezone\n");
            excelCode.append("from openpyxl import Workbook\n");
            excelCode.append("from openpyxl.styles import Font, Alignment, PatternFill\n\n");
            
            excelCode.append("def export_to_excel_advanced(request, queryset, filename=None):\n");
            excelCode.append("    if filename is None:\n");
            excelCode.append("        filename = 'report_' + timezone.now().strftime('%Y%m%d_%H%M%S') + '.xlsx'\n");
            excelCode.append("    \n");
            excelCode.append("    response = HttpResponse(content_type='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet')\n");
            excelCode.append("    response['Content-Disposition'] = f'attachment; filename=\"{filename}\"'\n");
            excelCode.append("    \n");
            excelCode.append("    # إنشاء workbook\n");
            excelCode.append("    wb = Workbook()\n");
            excelCode.append("    ws = wb.active\n");
            excelCode.append("    ws.title = 'التقرير'\n");
            excelCode.append("    \n");
            excelCode.append("    # إضافة رأس الجدول\n");
            excelCode.append("    headers = [");
            for (Field field : selectedFields) {
                if (field.isIncludeInReport()) {
                    excelCode.append("'").append(field.getDisplayName()).append("', ");
                }
            }
            excelCode.append("]\n");
            excelCode.append("    \n");
            excelCode.append("    for col_num, header in enumerate(headers, 1):\n");
            excelCode.append("        cell = ws.cell(row=1, column=col_num)\n");
            excelCode.append("        cell.value = header\n");
            excelCode.append("        cell.font = Font(bold=True)\n");
            excelCode.append("        cell.fill = PatternFill(start_color='CCCCCC', end_color='CCCCCC', fill_type='solid')\n");
            excelCode.append("    \n");
            excelCode.append("    # إضافة البيانات\n");
            excelCode.append("    for row_num, obj in enumerate(queryset, 2):\n");
            excelCode.append("        for col_num, field in enumerate([");
            for (Field field : selectedFields) {
                if (field.isIncludeInReport()) {
                    excelCode.append("'").append(field.getName()).append("', ");
                }
            }
            excelCode.append("], 1):\n");
            excelCode.append("            value = getattr(obj, field)\n");
            excelCode.append("            # تنسيق القيمة حسب نوع الحقل\n");
            excelCode.append("            if isinstance(value, datetime.datetime):\n");
            excelCode.append("                value = value.strftime('%Y-%m-%d %H:%M')\n");
            excelCode.append("            elif isinstance(value, decimal.Decimal):\n");
            excelCode.append("                value = float(value)\n");
            excelCode.append("            ws.cell(row=row_num, column=col_num).value = value\n");
            excelCode.append("    \n");
            excelCode.append("    # ضبط عرض الأعمدة\n");
            excelCode.append("    for column in ws.columns:\n");
            excelCode.append("        max_length = 0\n");
            excelCode.append("        column_letter = column[0].column_letter\n");
            excelCode.append("        for cell in column:\n");
            excelCode.append("            if cell.value:\n");
            excelCode.append("                max_length = max(max_length, len(str(cell.value)))\n");
            excelCode.append("        ws.column_dimensions[column_letter].width = max_length + 2\n");
            excelCode.append("    \n");
            excelCode.append("    # حفظ إلى response\n");
            excelCode.append("    wb.save(response)\n");
            excelCode.append("    return response\n");
        } else {
            excelCode.append(template);
        }
        
        showExportCodeDialog("كود تصدير Excel", excelCode.toString());
    }

    private void generatePDFExportCode() {
        String selectedFormat = exportFormatSpinner.getSelectedItem().toString();
        
        StringBuilder pdfCode = new StringBuilder();
        pdfCode.append("# كود تصدير إلى PDF - ").append(selectedFormat).append("\n\n");
        
        if (selectedFormat.contains("مع تنسيق")) {
            pdfCode.append("from reportlab.pdfgen import canvas\n");
            pdfCode.append("from reportlab.lib.pagesizes import A4, landscape\n");
            pdfCode.append("from reportlab.lib import colors\n");
            pdfCode.append("from reportlab.platypus import Table, TableStyle, Paragraph, Spacer\n");
            pdfCode.append("from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle\n");
            pdfCode.append("from reportlab.lib.units import inch, cm\n");
            pdfCode.append("from django.http import HttpResponse\n");
            pdfCode.append("from django.utils import timezone\n");
            pdfCode.append("import arabic_reshaper\n");
            pdfCode.append("from bidi.algorithm import get_display\n\n");
            
            pdfCode.append("def export_to_pdf_formatted(request, queryset, title=None):\n");
            pdfCode.append("    \"\"\"تصدير إلى PDF مع تنسيق عربي\"\"\"\n");
            pdfCode.append("    response = HttpResponse(content_type='application/pdf')\n");
            pdfCode.append("    filename = 'report_' + timezone.now().strftime('%Y%m%d_%H%M%S') + '.pdf'\n");
            pdfCode.append("    response['Content-Disposition'] = f'attachment; filename=\"{filename}\"'\n");
            pdfCode.append("    \n");
            pdfCode.append("    # إنشاء PDF\n");
            pdfCode.append("    doc = SimpleDocTemplate(response, pagesize=landscape(A4))\n");
            pdfCode.append("    elements = []\n");
            pdfCode.append("    styles = getSampleStyleSheet()\n");
            pdfCode.append("    \n");
            pdfCode.append("    # إضافة أسلوب عربي\n");
            pdfCode.append("    arabic_style = ParagraphStyle(\n");
            pdfCode.append("        'ArabicStyle',\n");
            pdfCode.append("        parent=styles['Normal'],\n");
            pdfCode.append("        fontName='Helvetica',\n");
            pdfCode.append("        fontSize=10,\n");
            pdfCode.append("        alignment=2,\n");
            pdfCode.append("        rightIndent=0,\n");
            pdfCode.append("        wordWrap='RTL',\n");
            pdfCode.append("    )\n");
            pdfCode.append("    \n");
            pdfCode.append("    # العنوان\n");
            pdfCode.append("    if title is None:\n");
            pdfCode.append("        title = 'تقرير'\n");
            pdfCode.append("    title_text = arabic_reshaper.reshape(title)\n");
            pdfCode.append("    title_text = get_display(title_text)\n");
            pdfCode.append("    title_para = Paragraph(title_text, styles['Title'])\n");
            pdfCode.append("    elements.append(title_para)\n");
            pdfCode.append("    elements.append(Spacer(1, 0.25*inch))\n");
            pdfCode.append("    \n");
            pdfCode.append("    # معلومات التقرير\n");
            pdfCode.append("    info_text = f\"تاريخ التصدير: {timezone.now().strftime('%Y-%m-%d %H:%M')}\"\n");
            pdfCode.append("    info_para = Paragraph(arabic_reshaper.reshape(info_text), arabic_style)\n");
            pdfCode.append("    elements.append(info_para)\n");
            pdfCode.append("    elements.append(Spacer(1, 0.25*inch))\n");
            pdfCode.append("    \n");
            pdfCode.append("    # تحضير بيانات الجدول\n");
            pdfCode.append("    data = []\n");
            pdfCode.append("    \n");
            pdfCode.append("    # رأس الجدول\n");
            pdfCode.append("    headers = [\n");
            for (Field field : selectedFields) {
                if (field.isIncludeInReport()) {
                    pdfCode.append("        '").append(field.getDisplayName()).append("',\n");
                }
            }
            pdfCode.append("    ]\n");
            pdfCode.append("    \n");
            pdfCode.append("    # تنسيق الرأس للغة العربية\n");
            pdfCode.append("    arabic_headers = []\n");
            pdfCode.append("    for header in headers:\n");
            pdfCode.append("        reshaped = arabic_reshaper.reshape(header)\n");
            pdfCode.append("        arabic_headers.append(get_display(reshaped))\n");
            pdfCode.append("    \n");
            pdfCode.append("    data.append(arabic_headers)\n");
            pdfCode.append("    \n");
            pdfCode.append("    # البيانات\n");
            pdfCode.append("    for obj in queryset:\n");
            pdfCode.append("        row = []\n");
            pdfCode.append("        for field in [");
            for (Field field : selectedFields) {
                if (field.isIncludeInReport()) {
                    pdfCode.append("'").append(field.getName()).append("', ");
                }
            }
            pdfCode.append("]:\n");
            pdfCode.append("            value = getattr(obj, field)\n");
            pdfCode.append("            if value is None:\n");
            pdfCode.append("                row.append('-')\n");
            pdfCode.append("            elif isinstance(value, datetime.datetime):\n");
            pdfCode.append("                row.append(value.strftime('%Y-%m-%d %H:%M'))\n");
            pdfCode.append("            elif isinstance(value, decimal.Decimal):\n");
            pdfCode.append("                row.append(f'{value:,.2f}')\n");
            pdfCode.append("            else:\n");
            pdfCode.append("                row.append(str(value))\n");
            pdfCode.append("        \n");
            pdfCode.append("        # تنسيق الصف للغة العربية\n");
            pdfCode.append("        arabic_row = []\n");
            pdfCode.append("        for cell in row:\n");
            pdfCode.append("            reshaped = arabic_reshaper.reshape(str(cell))\n");
            pdfCode.append("            arabic_row.append(get_display(reshaped))\n");
            pdfCode.append("        data.append(arabic_row)\n");
            pdfCode.append("    \n");
            pdfCode.append("    # إنشاء الجدول\n");
            pdfCode.append("    table = Table(data)\n");
            pdfCode.append("    table.setStyle(TableStyle([\n");
            pdfCode.append("        ('BACKGROUND', (0, 0), (-1, 0), colors.grey),\n");
            pdfCode.append("        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),\n");
            pdfCode.append("        ('ALIGN', (0, 0), (-1, -1), 'CENTER'),\n");
            pdfCode.append("        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),\n");
            pdfCode.append("        ('FONTSIZE', (0, 0), (-1, 0), 12),\n");
            pdfCode.append("        ('BOTTOMPADDING', (0, 0), (-1, 0), 12),\n");
            pdfCode.append("        ('GRID', (0, 0), (-1, -1), 1, colors.black),\n");
            pdfCode.append("        ('ROWBACKGROUNDS', (0, 1), (-1, -1), [colors.white, colors.whitesmoke]),\n");
            pdfCode.append("    ]))\n");
            pdfCode.append("    \n");
            pdfCode.append("    elements.append(table)\n");
            pdfCode.append("    \n");
            pdfCode.append("    # بناء PDF\n");
            pdfCode.append("    doc.build(elements)\n");
            pdfCode.append("    return response\n");
        } else {
            pdfCode.append(exportTemplates.get("pdf_simple"));
        }
        
        showExportCodeDialog("كود تصدير PDF", pdfCode.toString());
    }

    private void previewTemplate() {
        // معاينة قالب HTML للتقارير
        StringBuilder html = new StringBuilder();
        html.append("<!-- templates/reports/").append(etReportName.getText()).append(".html -->\n");
        html.append("{% extends 'base.html' %}\n\n");
        html.append("{% block content %}\n");
        html.append("<div class=\"container mt-4\">\n");
        html.append("    <h2>").append(etReportName.getText()).append("</h2>\n");
        html.append("    <p class=\"text-muted\">").append(etReportDescription.getText()).append("</p>\n");
        html.append("    \n");
        html.append("    <div class=\"card\">\n");
        html.append("        <div class=\"card-header d-flex justify-content-between\">\n");
        html.append("            <h5 class=\"mb-0\">البيانات</h5>\n");
        html.append("            <div>\n");
        html.append("                <a href=\"{% url 'export_excel' %}\" class=\"btn btn-success btn-sm\">\n");
        html.append("                    <i class=\"fas fa-file-excel\"></i> Excel\n");
        html.append("                </a>\n");
        html.append("                <a href=\"{% url 'export_pdf' %}\" class=\"btn btn-danger btn-sm\">\n");
        html.append("                    <i class=\"fas fa-file-pdf\"></i> PDF\n");
        html.append("                </a>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("        <div class=\"card-body\">\n");
        html.append("            <div class=\"table-responsive\">\n");
        html.append("                <table class=\"table table-striped table-hover\">\n");
        html.append("                    <thead>\n");
        html.append("                        <tr>\n");
        
        for (Field field : selectedFields) {
            if (field.isIncludeInReport()) {
                html.append("                            <th>").append(field.getDisplayName()).append("</th>\n");
            }
        }
        
        html.append("                        </tr>\n");
        html.append("                    </thead>\n");
        html.append("                    <tbody>\n");
        html.append("                        {% for item in data %}\n");
        html.append("                        <tr>\n");
        
        for (Field field : selectedFields) {
            if (field.isIncludeInReport()) {
                html.append("                            <td>{{ item.").append(field.getName()).append(" }}</td>\n");
            }
        }
        
        html.append("                        </tr>\n");
        html.append("                        {% empty %}\n");
        html.append("                        <tr>\n");
        html.append("                            <td colspan=\"").append(selectedFields.size()).append("\" class=\"text-center\">\n");
        html.append("                                لا توجد بيانات\n");
        html.append("                            </td>\n");
        html.append("                        </tr>\n");
        html.append("                        {% endfor %}\n");
        html.append("                    </tbody>\n");
        html.append("                </table>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("</div>\n");
        html.append("{% endblock %}\n");
        
        showExportCodeDialog("معاينة القالب", html.toString());
    }

    private void showResultDialog(String code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("استعلام Django المُنشأ");
        
        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setText(code);
        textView.setPadding(16, 16, 16, 16);
        textView.setTextIsSelectable(true);
        scrollView.addView(textView);
        
        builder.setView(scrollView);
        
        builder.setPositiveButton("نسخ الكود", (dialog, which) -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Django Query", code);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "تم نسخ الكود", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("حفظ", (dialog, which) -> {
            // يمكن إضافة خيارات حفظ إضافية هنا
        });
        
        builder.setNeutralButton("مشاركة", (dialog, which) -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, code);
            startActivity(Intent.createChooser(shareIntent, "مشاركة الكود"));
        });
        
        builder.show();
    }

    private void showExportCodeDialog(String title, String code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        
        ScrollView scrollView = new ScrollView(this);
        TextView textView = new TextView(this);
        textView.setText(code);
        textView.setPadding(16, 16, 16, 16);
        textView.setTextIsSelectable(true);
        scrollView.addView(textView);
        
        builder.setView(scrollView);
        
        builder.setPositiveButton("نسخ", (dialog, which) -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(title, code);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "تم النسخ", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("إغلاق", null);
        
        builder.show();
    }
}

// فئة Field الموسعة
class Field {
    private long id;
    private String name;
    private String type;
    private String displayName;
    private String modelName;
    private boolean includeInReport = true;
    private boolean groupBy = false;
    private boolean orderBy = false;
    private String orderDirection = "ASC";
    private String format = "";
    private String joinType = "INNER JOIN";
    private boolean useOnly = false;
    private boolean useDefer = false;
    private String prefetchTo = "";
    
    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getDisplayName() { 
        return displayName != null ? displayName : name; 
    }
    public void setDisplayName(String displayName) { 
        this.displayName = displayName; 
    }
    
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    
    public boolean isIncludeInReport() { return includeInReport; }
    public void setIncludeInReport(boolean includeInReport) { 
        this.includeInReport = includeInReport; 
    }
    
    public boolean isGroupBy() { return groupBy; }
    public void setGroupBy(boolean groupBy) { this.groupBy = groupBy; }
    
    public boolean isOrderBy() { return orderBy; }
    public void setOrderBy(boolean orderBy) { this.orderBy = orderBy; }
    
    public String getOrderDirection() { return orderDirection; }
    public void setOrderDirection(String orderDirection) { 
        this.orderDirection = orderDirection; 
    }
    
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    
    public String getJoinType() { return joinType; }
    public void setJoinType(String joinType) { this.joinType = joinType; }
    
    public boolean isUseOnly() { return useOnly; }
    public void setUseOnly(boolean useOnly) { this.useOnly = useOnly; }
    
    public boolean isUseDefer() { return useDefer; }
    public void setUseDefer(boolean useDefer) { this.useDefer = useDefer; }
    
    public String getPrefetchTo() { return prefetchTo; }
    public void setPrefetchTo(String prefetchTo) { this.prefetchTo = prefetchTo; }
}