
    
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
    public List<ReportDesignerrActivity.Template> getTemplates(long projectId) {
        List<ReportDesignerrActivity.Template> templates = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query("templates",
            new String[]{"id", "name", "description", "html_content", "css_content", "js_content", "is_system"},
            "project_id = ?",
            new String[]{String.valueOf(projectId)},
            null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ReportDesignerrActivity.Template template = new ReportDesignerrActivity.Template();
                template.id = cursor.getLong(0);
                template.name = cursor.getString(1);
                template.description = cursor.getString(2);
                template.htmlContent = cursor.getString(3);
                template.cssContent = cursor.getString(4);
                template.jsContent = cursor.getString(5);
                template.isSystem = cursor.getInt(6) == 1;
                templates.add(template);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return templates;
    }
    
    // دالة الحصول على قالب بواسطة ID
    public ReportDesignerrActivity.Template getTemplateById(long templateId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("templates",
            null, "id = ?", new String[]{String.valueOf(templateId)},
            null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            ReportDesignerrActivity.Template template = new ReportDesignerrActivity.Template();
            template.id = cursor.getLong(0);
            template.name = cursor.getString(1);
            template.description = cursor.getString(2);
            template.htmlContent = cursor.getString(3);
            template.cssContent = cursor.getString(4);
            template.jsContent = cursor.getString(5);
            template.isSystem = cursor.getInt(6) == 1;
            cursor.close();
            db.close();
            return template;
        }
        db.close();
        return null;
    }
    
    // دالة حفظ القالب
    public long saveTemplate(ReportDesignerrActivity.Template template, long projectId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put("name", template.name);
        values.put("description", template.description);
        values.put("html_content", template.htmlContent);
        values.put("css_content", template.cssContent);
        values.put("js_content", template.jsContent);
        values.put("is_system", template.isSystem ? 1 : 0);
        values.put("project_id", projectId);
        
        long id = db.insert("templates", null, values);
        db.close();
        return id;
    }
