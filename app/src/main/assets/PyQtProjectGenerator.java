package com.example.pyqtgenerator;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PyQt5ProjectGenerator {
    
    private Context context;
    private JSONObject projectConfig;
    private File projectDir;
    
    // نوعيات الحقول وتحويلاتها
    private Map<String, String> fieldTypeToPython = new HashMap<>();
    private Map<String, String> fieldTypeToWidget = new HashMap<>();
    private Map<String, String> fieldTypeToSql = new HashMap<>();
    
    public PyQt5ProjectGenerator(Context context, String jsonConfig) throws JSONException {
        this.context = context;
        this.projectConfig = new JSONObject(jsonConfig);
        initTypeMappings();
    }
    
    private void initTypeMappings() {
        // تحويل أنواع الحقول إلى Python
        fieldTypeToPython.put("integer", "int");
        fieldTypeToPython.put("varchar", "str");
        fieldTypeToPython.put("text", "str");
        fieldTypeToPython.put("decimal", "float");
        fieldTypeToPython.put("boolean", "bool");
        fieldTypeToPython.put("date", "datetime.date");
        fieldTypeToPython.put("datetime", "datetime.datetime");
        fieldTypeToPython.put("foreign_key", "int");
        
        // تحويل أنواع الحقول إلى ويدجت PyQt5
        fieldTypeToWidget.put("integer", "QSpinBox");
        fieldTypeToWidget.put("varchar", "QLineEdit");
        fieldTypeToWidget.put("text", "QTextEdit");
        fieldTypeToWidget.put("decimal", "QDoubleSpinBox");
        fieldTypeToWidget.put("boolean", "QCheckBox");
        fieldTypeToWidget.put("date", "QDateEdit");
        fieldTypeToWidget.put("datetime", "QDateTimeEdit");
        fieldTypeToWidget.put("foreign_key", "QComboBox");
        
        // تحويل أنواع الحقول إلى SQL
        fieldTypeToSql.put("integer", "INTEGER");
        fieldTypeToSql.put("varchar", "VARCHAR");
        fieldTypeToSql.put("text", "TEXT");
        fieldTypeToSql.put("decimal", "DECIMAL(10,2)");
        fieldTypeToSql.put("boolean", "BOOLEAN");
        fieldTypeToSql.put("date", "DATE");
        fieldTypeToSql.put("datetime", "DATETIME");
        fieldTypeToSql.put("foreign_key", "INTEGER");
    }
    
    public boolean generateProject() {
        try {
            String projectName = projectConfig.getJSONObject("project").getString("name");
            
            // إنشاء مجلد المشروع
            projectDir = new File(context.getExternalFilesDir(null), projectName);
            if (!projectDir.exists()) {
                projectDir.mkdirs();
            }
            
            Log.i("PyQtGenerator", "إنشاء مشروع في: " + projectDir.getAbsolutePath());
            
            // توليد جميع الملفات
            generateRequirementsFile();
            generateMainFile();
            generateDatabaseFile();
            generateConfigFile();
            generateModelsFile();
            generateMainWindowFile();
            generateTableViewFiles();
            generateFormFiles();
            generateMenuFile();
            
            Log.i("PyQtGenerator", "تم إنشاء المشروع بنجاح!");
            return true;
            
        } catch (Exception e) {
            Log.e("PyQtGenerator", "خطأ في إنشاء المشروع", e);
            return false;
        }
    }
    
    private void generateRequirementsFile() throws IOException {
        String content = """
            PyQt5==5.15.9
            pandas==1.5.3
            openpyxl==3.0.10
            sqlite3
            """;
        
        writeFile("requirements.txt", content);
    }
    
    private void generateMainFile() throws IOException, JSONException {
        String projectName = projectConfig.getJSONObject("project").getString("name");
        String projectTitle = projectConfig.getJSONObject("project").getString("title");
        
        StringBuilder sb = new StringBuilder();
        sb.append("#!/usr/bin/env python3\n");
        sb.append("# -*- coding: utf-8 -*-\n\n");
        sb.append("import sys\n");
        sb.append("import os\n");
        sb.append("sys.path.append(os.path.dirname(os.path.abspath(__file__)))\n\n");
        sb.append("from PyQt5.QtWidgets import QApplication\n");
        sb.append("from PyQt5.QtGui import QFont\n");
        sb.append("from PyQt5.QtCore import Qt\n");
        sb.append("from main_window import MainWindow\n");
        sb.append("from database import Database\n");
        sb.append("from config import Config\n\n");
        sb.append("def main():\n");
        sb.append("    # إنشاء تطبيق Qt\n");
        sb.append("    app = QApplication(sys.argv)\n");
        sb.append("    \n");
        sb.append("    # تحميل الإعدادات\n");
        sb.append("    config = Config()\n");
        sb.append("    \n");
        sb.append("    # ضبط الخط للغة العربية\n");
        sb.append("    font = QFont('Arial', 10)\n");
        sb.append("    app.setFont(font)\n");
        sb.append("    \n");
        sb.append("    # إنشاء اتصال قاعدة البيانات\n");
        sb.append("    db = Database()\n");
        sb.append("    db.connect()\n");
        sb.append("    \n");
        sb.append("    # إنشاء النوافذ\n");
        sb.append("    window = MainWindow(db, config)\n");
        sb.append("    window.setWindowTitle('").append(projectTitle).append("')\n");
        sb.append("    window.show()\n");
        sb.append("    \n");
        sb.append("    # تشغيل التطبيق\n");
        sb.append("    sys.exit(app.exec_())\n\n");
        sb.append("if __name__ == '__main__':\n");
        sb.append("    main()\n");
        
        writeFile("main.py", sb.toString());
    }
    
    private void generateDatabaseFile() throws IOException, JSONException {
        JSONObject dbConfig = projectConfig.getJSONObject("database");
        String engine = dbConfig.getString("engine");
        String filename = dbConfig.getString("filename");
        
        StringBuilder sb = new StringBuilder();
        sb.append("#!/usr/bin/env python3\n");
        sb.append("# -*- coding: utf-8 -*-\n\n");
        sb.append("import sqlite3\n");
        sb.append("import json\n");
        sb.append("import os\n");
        sb.append("from typing import List, Dict, Any, Optional\n\n");
        sb.append("class Database:\n");
        sb.append("    def __init__(self):\n");
        sb.append("        self.db_file = '").append(filename).append("'\n");
        sb.append("        self.connection = None\n");
        sb.append("        self.cursor = None\n\n");
        
        sb.append("    def connect(self):\n");
        sb.append("        \"\"\"إنشاء اتصال بقاعدة البيانات\"\"\"\n");
        sb.append("        self.connection = sqlite3.connect(self.db_file)\n");
        sb.append("        self.connection.row_factory = sqlite3.Row\n");
        sb.append("        self.cursor = self.connection.cursor()\n");
        sb.append("        print(f'تم الاتصال بقاعدة البيانات: {self.db_file}')\n");
        sb.append("        self.create_tables()\n\n");
        
        sb.append("    def create_tables(self):\n");
        sb.append("        \"\"\"إنشاء الجداول من التكوين\"\"\"\n");
        sb.append("        tables_config = ");
        sb.append(projectConfig.getJSONArray("tables").toString(2)).append("\n");
        sb.append("        \n");
        sb.append("        for table_config in tables_config:\n");
        sb.append("            table_name = table_config['name']\n");
        sb.append("            fields = table_config['fields']\n");
        sb.append("            \n");
        sb.append("            # بناء استعلام CREATE TABLE\n");
        sb.append("            sql_parts = []\n");
        sb.append("            for field in fields:\n");
        sb.append("                field_name = field['name']\n");
        sb.append("                field_type = field['type']\n");
        sb.append("                \n");
        sb.append("                # تحويل النوع إلى SQL\n");
        sb.append("                sql_type = self._map_field_type(field_type)\n");
        sb.append("                \n");
        sb.append("                field_sql = f\"{field_name} {sql_type}\"\n");
        sb.append("                \n");
        sb.append("                # إضافة خصائص الحقل\n");
        sb.append("                if field.get('primary_key', False):\n");
        sb.append("                    field_sql += \" PRIMARY KEY\"\n");
        sb.append("                if field.get('auto_increment', False):\n");
        sb.append("                    field_sql += \" AUTOINCREMENT\"\n");
        sb.append("                if field.get('required', False):\n");
        sb.append("                    field_sql += \" NOT NULL\"\n");
        sb.append("                if field.get('unique', False):\n");
        sb.append("                    field_sql += \" UNIQUE\"\n");
        sb.append("                \n");
        sb.append("                sql_parts.append(field_sql)\n");
        sb.append("            \n");
        sb.append("            # إنشاء الجدول\n");
        sb.append("            create_sql = f\"CREATE TABLE IF NOT EXISTS {table_name} (\"\n");
        sb.append("            create_sql += \", \".join(sql_parts)\n");
        sb.append("            create_sql += \")\"\n");
        sb.append("            \n");
        sb.append("            try:\n");
        sb.append("                self.cursor.execute(create_sql)\n");
        sb.append("                self.connection.commit()\n");
        sb.append("                print(f'تم إنشاء الجدول: {table_name}')\n");
        sb.append("            except Exception as e:\n");
        sb.append("                print(f'خطأ في إنشاء الجدول {table_name}: {e}')\n\n");
        
        // CRUD operations
        sb.append("    # ========== عمليات CRUD ==========\n\n");
        
        sb.append("    def insert(self, table_name: str, data: Dict) -> int:\n");
        sb.append("        \"\"\"إضافة سجل جديد\"\"\"\n");
        sb.append("        columns = ', '.join(data.keys())\n");
        sb.append("        placeholders = ', '.join(['?' for _ in data])\n");
        sb.append("        values = list(data.values())\n");
        sb.append("        \n");
        sb.append("        sql = f\"INSERT INTO {table_name} ({columns}) VALUES ({placeholders})\"\n");
        sb.append("        self.cursor.execute(sql, values)\n");
        sb.append("        self.connection.commit()\n");
        sb.append("        return self.cursor.lastrowid\n\n");
        
        sb.append("    def update(self, table_name: str, record_id: int, data: Dict) -> bool:\n");
        sb.append("        \"\"\"تحديث سجل\"\"\"\n");
        sb.append("        set_clause = ', '.join([f\"{k} = ?\" for k in data.keys()])\n");
        sb.append("        values = list(data.values())\n");
        sb.append("        values.append(record_id)\n");
        sb.append("        \n");
        sb.append("        sql = f\"UPDATE {table_name} SET {set_clause} WHERE id = ?\"\n");
        sb.append("        self.cursor.execute(sql, values)\n");
        sb.append("        self.connection.commit()\n");
        sb.append("        return self.cursor.rowcount > 0\n\n");
        
        sb.append("    def delete(self, table_name: str, record_id: int) -> bool:\n");
        sb.append("        \"\"\"حذف سجل\"\"\"\n");
        sb.append("        sql = f\"DELETE FROM {table_name} WHERE id = ?\"\n");
        sb.append("        self.cursor.execute(sql, (record_id,))\n");
        sb.append("        self.connection.commit()\n");
        sb.append("        return self.cursor.rowcount > 0\n\n");
        
        sb.append("    def select_all(self, table_name: str) -> List[Dict]:\n");
        sb.append("        \"\"\"استرجاع جميع السجلات\"\"\"\n");
        sb.append("        sql = f\"SELECT * FROM {table_name} ORDER BY id DESC\"\n");
        sb.append("        self.cursor.execute(sql)\n");
        sb.append("        rows = self.cursor.fetchall()\n");
        sb.append("        return [dict(row) for row in rows]\n\n");
        
        sb.append("    def select_by_id(self, table_name: str, record_id: int) -> Optional[Dict]:\n");
        sb.append("        \"\"\"استرجاع سجل برقمه\"\"\"\n");
        sb.append("        sql = f\"SELECT * FROM {table_name} WHERE id = ?\"\n");
        sb.append("        self.cursor.execute(sql, (record_id,))\n");
        sb.append("        row = self.cursor.fetchone()\n");
        sb.append("        return dict(row) if row else None\n\n");
        
        sb.append("    def execute_query(self, sql: str, params: tuple = ()) -> List[Dict]:\n");
        sb.append("        \"\"\"تنفيذ استعلام مخصص\"\"\"\n");
        sb.append("        self.cursor.execute(sql, params)\n");
        sb.append("        rows = self.cursor.fetchall()\n");
        sb.append("        return [dict(row) for row in rows]\n\n");
        
        sb.append("    def _map_field_type(self, field_type: str) -> str:\n");
        sb.append("        \"\"\"تحويل نوع الحقل إلى نوع SQL\"\"\"\n");
        sb.append("        type_map = {\n");
        for (Map.Entry<String, String> entry : fieldTypeToSql.entrySet()) {
            sb.append("            '").append(entry.getKey()).append("': '").append(entry.getValue()).append("',\n");
        }
        sb.append("        }\n");
        sb.append("        return type_map.get(field_type, 'TEXT')\n\n");
        
        sb.append("    def close(self):\n");
        sb.append("        \"\"\"إغلاق الاتصال\"\"\"\n");
        sb.append("        if self.cursor:\n");
        sb.append("            self.cursor.close()\n");
        sb.append("        if self.connection:\n");
        sb.append("            self.connection.close()\n");
        sb.append("        print('تم إغلاق اتصال قاعدة البيانات')\n");
        
        writeFile("database.py", sb.toString());
    }
    
    private void generateMainWindowFile() throws IOException, JSONException {
        StringBuilder sb = new StringBuilder();
        sb.append("#!/usr/bin/env python3\n");
        sb.append("# -*- coding: utf-8 -*-\n\n");
        sb.append("from PyQt5.QtWidgets import *\n");
        sb.append("from PyQt5.QtCore import *\n");
        sb.append("from PyQt5.QtGui import *\n");
        sb.append("import sys\n");
        sb.append("import os\n\n");
        
        sb.append("class MainWindow(QMainWindow):\n");
        sb.append("    def __init__(self, db, config):\n");
        sb.append("        super().__init__()\n");
        sb.append("        self.db = db\n");
        sb.append("        self.config = config\n");
        sb.append("        self.table_windows = {}\n");
        sb.append("        \n");
        sb.append("        self.init_ui()\n\n");
        
        sb.append("    def init_ui(self):\n");
        sb.append("        # إعداد النافذة الرئيسية\n");
        sb.append("        self.setGeometry(100, 100, 1200, 700)\n");
        sb.append("        \n");
        sb.append("        # إنشاء القائمة الرئيسية\n");
        sb.append("        self.create_menu()\n");
        sb.append("        \n");
        sb.append("        # إنشاء شريط الأدوات\n");
        sb.append("        self.create_toolbar()\n");
        sb.append("        \n");
        sb.append("        # إنشاء الواجهة الرئيسية\n");
        sb.append("        self.create_main_layout()\n");
        sb.append("        \n");
        sb.append("        # تحميل الجداول\n");
        sb.append("        self.load_tables()\n\n");
        
        sb.append("    def create_menu(self):\n");
        sb.append("        menubar = self.menuBar()\n");
        sb.append("        \n");
        sb.append("        # قائمة ملف\n");
        sb.append("        file_menu = menubar.addMenu('ملف')\n");
        sb.append("        \n");
        sb.append("        exit_action = QAction('خروج', self)\n");
        sb.append("        exit_action.triggered.connect(self.close)\n");
        sb.append("        file_menu.addAction(exit_action)\n");
        sb.append("        \n");
        sb.append("        # قائمة عرض\n");
        sb.append("        view_menu = menubar.addMenu('عرض')\n");
        sb.append("        \n");
        sb.append("        # قائمة أدوات\n");
        sb.append("        tools_menu = menubar.addMenu('أدوات')\n");
        sb.append("        \n");
        sb.append("        backup_action = QAction('نسخ احتياطي', self)\n");
        sb.append("        tools_menu.addAction(backup_action)\n");
        sb.append("        \n");
        sb.append("        # قائمة مساعدة\n");
        sb.append("        help_menu = menubar.addMenu('مساعدة')\n");
        sb.append("        \n");
        sb.append("        about_action = QAction('حول', self)\n");
        sb.append("        about_action.triggered.connect(self.show_about)\n");
        sb.append("        help_menu.addAction(about_action)\n\n");
        
        sb.append("    def create_toolbar(self):\n");
        sb.append("        toolbar = QToolBar()\n");
        sb.append("        self.addToolBar(toolbar)\n");
        sb.append("        \n");
        sb.append("        refresh_btn = QAction(QIcon(), 'تحديث', self)\n");
        sb.append("        refresh_btn.triggered.connect(self.refresh_all)\n");
        sb.append("        toolbar.addAction(refresh_btn)\n\n");
        
        sb.append("    def create_main_layout(self):\n");
        sb.append("        # إنشاء الـ Widget المركزي\n");
        sb.append("        central_widget = QWidget()\n");
        sb.append("        self.setCentralWidget(central_widget)\n");
        sb.append("        \n");
        sb.append("        # تخطيط رئيسي\n");
        sb.append("        main_layout = QHBoxLayout()\n");
        sb.append("        \n");
        sb.append("        # إنشاء الشريط الجانبي\n");
        sb.append("        sidebar = self.create_sidebar()\n");
        sb.append("        main_layout.addWidget(sidebar, 1)\n");
        sb.append("        \n");
        sb.append("        # منطقة المحتوى\n");
        sb.append("        self.content_stack = QStackedWidget()\n");
        sb.append("        main_layout.addWidget(self.content_stack, 4)\n");
        sb.append("        \n");
        sb.append("        central_widget.setLayout(main_layout)\n\n");
        
        sb.append("    def create_sidebar(self):\n");
        sb.append("        sidebar = QWidget()\n");
        sb.append("        sidebar.setFixedWidth(250)\n");
        sb.append("        sidebar.setStyleSheet(\"\"\"\n");
        sb.append("            QWidget {\n");
        sb.append("                background-color: #2c3e50;\n");
        sb.append("                color: white;\n");
        sb.append("            }\n");
        sb.append("            QPushButton {\n");
        sb.append("                text-align: right;\n");
        sb.append("                padding: 12px;\n");
        sb.append("                border: none;\n");
        sb.append("                background-color: transparent;\n");
        sb.append("                color: white;\n");
        sb.append("                font-size: 14px;\n");
        sb.append("            }\n");
        sb.append("            QPushButton:hover {\n");
        sb.append("                background-color: #34495e;\n");
        sb.append("            }\n");
        sb.append("        \"\"\")\n");
        sb.append("        \n");
        sb.append("        layout = QVBoxLayout()\n");
        sb.append("        \n");
        sb.append("        # عنوان الشريط الجانبي\n");
        sb.append("        title = QLabel('القوائم')\n");
        sb.append("        title.setAlignment(Qt.AlignCenter)\n");
        sb.append("        title.setStyleSheet('font-size: 18px; font-weight: bold; padding: 20px;')\n");
        sb.append("        layout.addWidget(title)\n");
        sb.append("        \n");
        sb.append("        # أزرار الجداول\n");
        sb.append("        self.table_buttons = {}\n");
        sb.append("        layout.addWidget(QLabel('الجداول:'))\n");
        sb.append("        \n");
        sb.append("        # سيتم إضافة الأزرار ديناميكياً\n");
        sb.append("        self.sidebar_layout = QVBoxLayout()\n");
        sb.append("        layout.addLayout(self.sidebar_layout)\n");
        sb.append("        \n");
        sb.append("        layout.addStretch()\n");
        sb.append("        sidebar.setLayout(layout)\n");
        sb.append("        return sidebar\n\n");
        
        sb.append("    def load_tables(self):\n");
        sb.append("        \"\"\"تحميل قائمة الجداول وإضافة أزرارها\"\"\"\n");
        sb.append("        tables_config = ");
        sb.append(projectConfig.getJSONArray("tables").toString(2)).append("\n");
        sb.append("        \n");
        sb.append("        for table_config in tables_config:\n");
        sb.append("            table_name = table_config['name']\n");
        sb.append("            arabic_name = table_config.get('arabic_name', table_name)\n");
        sb.append("            \n");
        sb.append("            # إنشاء زر للجدول\n");
        sb.append("            btn = QPushButton(arabic_name)\n");
        sb.append("            btn.clicked.connect(lambda checked, t=table_name: self.show_table(t))\n");
        sb.append("            self.sidebar_layout.addWidget(btn)\n");
        sb.append("            \n");
        sb.append("            # إنشاء نافذة العرض للجدول\n");
        sb.append("            table_window = self.create_table_window(table_config)\n");
        sb.append("            self.content_stack.addWidget(table_window)\n");
        sb.append("            self.table_windows[table_name] = table_window\n");
        sb.append("        \n");
        sb.append("        # إضافة زر الإعدادات\n");
        sb.append("        self.sidebar_layout.addSpacing(20)\n");
        sb.append("        settings_btn = QPushButton('الإعدادات')\n");
        sb.append("        settings_btn.clicked.connect(self.show_settings)\n");
        sb.append("        self.sidebar_layout.addWidget(settings_btn)\n\n");
        
        sb.append("    def create_table_window(self, table_config):\n");
        sb.append("        \"\"\"إنشاء نافذة عرض للجدول\"\"\"\n");
        sb.append("        from table_view import TableView\n");
        sb.append("        return TableView(self.db, table_config)\n\n");
        
        sb.append("    def show_table(self, table_name):\n");
        sb.append("        \"\"\"عرض نافذة الجدول\"\"\"\n");
        sb.append("        if table_name in self.table_windows:\n");
        sb.append("            window = self.table_windows[table_name]\n");
        sb.append("            self.content_stack.setCurrentWidget(window)\n");
        sb.append("            window.refresh_data()\n\n");
        
        sb.append("    def refresh_all(self):\n");
        sb.append("        \"\"\"تحديث جميع الجداول\"\"\"\n");
        sb.append("        for window in self.table_windows.values():\n");
        sb.append("            window.refresh_data()\n\n");
        
        sb.append("    def show_settings(self):\n");
        sb.append("        QMessageBox.information(self, 'الإعدادات', 'شاشة الإعدادات ستظهر هنا')\n\n");
        
        sb.append("    def show_about(self):\n");
        sb.append("        QMessageBox.about(self, 'حول البرنامج', \n");
        sb.append("                         'تم إنشاء هذا البرنامج تلقائياً بواسطة PyQt Generator')\n");
        
        writeFile("main_window.py", sb.toString());
    }
    
    private void generateTableViewFiles() throws IOException, JSONException {
        JSONArray tables = projectConfig.getJSONArray("tables");
        
        // إنشاء ملف table_view.py العام
        StringBuilder tvSb = new StringBuilder();
        tvSb.append("#!/usr/bin/env python3\n");
        tvSb.append("# -*- coding: utf-8 -*-\n\n");
        tvSb.append("from PyQt5.QtWidgets import *\n");
        tvSb.append("from PyQt5.QtCore import *\n");
        tvSb.append("from PyQt5.QtGui import *\n");
        tvSb.append("from form_dialog import FormDialog\n");
        tvSb.append("import pandas as pd\n\n");
        
        tvSb.append("class TableView(QWidget):\n");
        tvSb.append("    \"\"\"نافذة عرض الجدول مع عمليات CRUD\"\"\"\n\n");
        
        tvSb.append("    def __init__(self, db, table_config):\n");
        tvSb.append("        super().__init__()\n");
        tv.append("        self.db = db\n");
        tvSb.append("        self.table_config = table_config\n");
        tvSb.append("        self.table_name = table_config['name']\n");
        tvSb.append("        self.arabic_name = table_config.get('arabic_name', self.table_name)\n");
        tvSb.append("        \n");
        tvSb.append("        self.init_ui()\n");
        tvSb.append("        self.refresh_data()\n\n");
        
        tvSb.append("    def init_ui(self):\n");
        tvSb.append("        layout = QVBoxLayout()\n");
        tvSb.append("        \n");
        tvSb.append("        # شريط الأدوات\n");
        tvSb.append("        toolbar = QHBoxLayout()\n");
        tvSb.append("        \n");
        tvSb.append("        self.add_btn = QPushButton('إضافة جديد')\n");
        tvSb.append("        self.add_btn.clicked.connect(self.add_record)\n");
        tvSb.append("        toolbar.addWidget(self.add_btn)\n");
        tvSb.append("        \n");
        tvSb.append("        self.edit_btn = QPushButton('تعديل')\n");
        tvSb.append("        self.edit_btn.clicked.connect(self.edit_record)\n");
        tvSb.append("        toolbar.addWidget(self.edit_btn)\n");
        tvSb.append("        \n");
        tvSb.append("        self.delete_btn = QPushButton('حذف')\n");
        tvSb.append("        self.delete_btn.clicked.connect(self.delete_record)\n");
        tvSb.append("        toolbar.addWidget(self.delete_btn)\n");
        tvSb.append("        \n");
        tvSb.append("        self.export_btn = QPushButton('تصدير Excel')\n");
        tvSb.append("        self.export_btn.clicked.connect(self.export_to_excel)\n");
        tvSb.append("        toolbar.addWidget(self.export_btn)\n");
        tvSb.append("        \n");
        tvSb.append("        toolbar.addStretch()\n");
        tvSb.append("        \n");
        tvSb.append("        # صندوق البحث\n");
        tvSb.append("        toolbar.addWidget(QLabel('بحث:'))\n");
        tvSb.append("        self.search_input = QLineEdit()\n");
        tvSb.append("        self.search_input.textChanged.connect(self.filter_table)\n");
        tvSb.append("        self.search_input.setFixedWidth(200)\n");
        tvSb.append("        toolbar.addWidget(self.search_input)\n");
        tvSb.append("        \n");
        tvSb.append("        layout.addLayout(toolbar)\n");
        tvSb.append("        \n");
        tvSb.append("        # إنشاء الجدول\n");
        tvSb.append("        self.table = QTableWidget()\n");
        tvSb.append("        self.table.setAlternatingRowColors(True)\n");
        tvSb.append("        self.table.setSelectionBehavior(QTableWidget.SelectRows)\n");
        tvSb.append("        self.table.setSelectionMode(QTableWidget.SingleSelection)\n");
        tvSb.append("        self.table.horizontalHeader().setStretchLastSection(True)\n");
        tvSb.append("        self.table.cellDoubleClicked.connect(self.edit_record)\n");
        tvSb.append("        \n");
        tvSb.append("        layout.addWidget(self.table)\n");
        tvSb.append("        \n");
        tvSb.append("        # شريط الحالة\n");
        tvSb.append("        self.status_label = QLabel('جاهز')\n");
        tvSb.append("        layout.addWidget(self.status_label)\n");
        tvSb.append("        \n");
        tvSb.append("        self.setLayout(layout)\n\n");
        
        tvSb.append("    def setup_table_columns(self):\n");
        tvSb.append("        \"\"\"إعداد أعمدة الجدول\"\"\"\n");
        tvSb.append("        fields = self.table_config['fields']\n");
        tvSb.append("        \n");
        tvSb.append("        # إعداد العناوين\n");
        tvSb.append("        headers = []\n");
        tvSb.append("        for field in fields:\n");
        tvSb.append("            arabic_label = field.get('arabic_label', field['name'])\n");
        tvSb.append("            headers.append(arabic_label)\n");
        tvSb.append("        \n");
        tvSb.append("        self.table.setColumnCount(len(headers))\n");
        tvSb.append("        self.table.setHorizontalHeaderLabels(headers)\n");
        tvSb.append("        \n");
        tvSb.append("        # ضبط عرض الأعمدة\n");
        tvSb.append("        for i in range(len(headers)):\n");
        tvSb.append("            self.table.setColumnWidth(i, 150)\n\n");
        
        tvSb.append("    def refresh_data(self):\n");
        tvSb.append("        \"\"\"تحديث البيانات في الجدول\"\"\"\n");
        tvSb.append("        try:\n");
        tvSb.append("            # إعداد الأعمدة أولاً\n");
        tvSb.append("            self.setup_table_columns()\n");
        tvSb.append("            \n");
        tvSb.append("            # جلب البيانات من قاعدة البيانات\n");
        tvSb.append("            records = self.db.select_all(self.table_name)\n");
        tvSb.append("            \n");
        tvSb.append("            # تعبئة الجدول\n");
        tvSb.append("            self.table.setRowCount(len(records))\n");
        tvSb.append("            \n");
        tvSb.append("            for row_idx, record in enumerate(records):\n");
        tvSb.append("                col_idx = 0\n");
        tvSb.append("                for field in self.table_config['fields']:\n");
        tvSb.append("                    field_name = field['name']\n");
        tvSb.append("                    value = record.get(field_name, '')\n");
        tvSb.append("                    \n");
        tvSb.append("                    # تحويل القيم\n");
        tvSb.append("                    if value is None:\n");
        tvSb.append("                        display_value = ''\n");
        tvSb.append("                    elif field.get('type') == 'boolean':\n");
        tvSb.append("                        display_value = 'نعم' if value else 'لا'\n");
        tvSb.append("                    else:\n");
        tvSb.append("                        display_value = str(value)\n");
        tvSb.append("                    \n");
        tvSb.append("                    item = QTableWidgetItem(display_value)\n");
        tvSb.append("                    item.setData(Qt.UserRole, record.get('id'))\n");
        tvSb.append("                    self.table.setItem(row_idx, col_idx, item)\n");
        tvSb.append("                    col_idx += 1\n");
        tvSb.append("            \n");
        tvSb.append("            self.status_label.setText(f'عدد السجلات: {len(records)}')\n");
        tvSb.append("            \n");
        tvSb.append("        except Exception as e:\n");
        tvSb.append("            QMessageBox.critical(self, 'خطأ', f'فشل تحميل البيانات: {str(e)}')\n\n");
        
        tvSb.append("    def add_record(self):\n");
        tvSb.append("        \"\"\"إضافة سجل جديد\"\"\"\n");
        tvSb.append("        dialog = FormDialog(self.db, self.table_config, None)\n");
        tvSb.append("        if dialog.exec_() == QDialog.Accepted:\n");
        tvSb.append("            self.refresh_data()\n");
        tvSb.append("            QMessageBox.information(self, 'نجاح', 'تمت الإضافة بنجاح')\n\n");
        
        tvSb.append("    def edit_record(self):\n");
        tvSb.append("        \"\"\"تعديل سجل\"\"\"\n");
        tvSb.append("        selected_row = self.table.currentRow()\n");
        tvSb.append("        if selected_row < 0:\n");
        tvSb.append("            QMessageBox.warning(self, 'تحذير', 'يرجى اختيار سجل للتعديل')\n");
        tvSb.append("            return\n");
        tvSb.append("        \n");
        tvSb.append("        # الحصول على ID السجل\n");
        tvSb.append("        record_id = self.table.item(selected_row, 0).data(Qt.UserRole)\n");
        tvSb.append("        \n");
        tvSb.append("        dialog = FormDialog(self.db, self.table_config, record_id)\n");
        tvSb.append("        if dialog.exec_() == QDialog.Accepted:\n");
        tvSb.append("            self.refresh_data()\n");
        tvSb.append("            QMessageBox.information(self, 'نجاح', 'تم التعديل بنجاح')\n\n");
        
        tvSb.append("    def delete_record(self):\n");
        tvSb.append("        \"\"\"حذف سجل\"\"\"\n");
        tvSb.append("        selected_row = self.table.currentRow()\n");
        tvSb.append("        if selected_row < 0:\n");
        tvSb.append("            QMessageBox.warning(self, 'تحذير', 'يرجى اختيار سجل للحذف')\n");
        tvSb.append("            return\n");
        tvSb.append("        \n");
        tvSb.append("        reply = QMessageBox.question(self, 'تأكيد', \n");
        tvSb.append("                                   'هل أنت متأكد من حذف هذا السجل؟',\n");
        tvSb.append("                                   QMessageBox.Yes | QMessageBox.No)\n");
        tvSb.append("        \n");
        tvSb.append("        if reply == QMessageBox.Yes:\n");
        tvSb.append("            record_id = self.table.item(selected_row, 0).data(Qt.UserRole)\n");
        tvSb.append("            success = self.db.delete(self.table_name, record_id)\n");
        tvSb.append("            \n");
        tvSb.append("            if success:\n");
        tvSb.append("                self.refresh_data()\n");
        tvSb.append("                QMessageBox.information(self, 'نجاح', 'تم الحذف بنجاح')\n");
        tvSb.append("            else:\n");
        tvSb.append("                QMessageBox.warning(self, 'تحذير', 'فشل حذف السجل')\n\n");
        
        tvSb.append("    def export_to_excel(self):\n");
        tvSb.append("        \"\"\"تصدير إلى Excel\"\"\"\n");
        tvSb.append("        try:\n");
        tvSb.append("            file_name, _ = QFileDialog.getSaveFileName(self, \n");
        tvSb.append("                                                   'حفظ كملف Excel',\n");
        tvSb.append("                                                   '',\n");
        tvSb.append("                                                   'Excel Files (*.xlsx)')\n");
        tvSb.append("            \n");
        tvSb.append("            if file_name:\n");
        tvSb.append("                records = self.db.select_all(self.table_name)\n");
        tvSb.append("                \n");
        tvSb.append("                # تحويل إلى DataFrame\n");
        tvSb.append("                df = pd.DataFrame(records)\n");
        tvSb.append("                \n");
        tvSb.append("                # حفظ في Excel\n");
        tvSb.append("                df.to_excel(file_name, index=False)\n");
        tvSb.append("                \n");
        tvSb.append("                QMessageBox.information(self, 'نجاح', f'تم التصدير إلى: {file_name}')\n");
        tvSb.append("                \n");
        tvSb.append("        except Exception as e:\n");
        tvSb.append("            QMessageBox.critical(self, 'خطأ', f'فشل التصدير: {str(e)}')\n\n");
        
        tvSb.append("    def filter_table(self):\n");
        tvSb.append("        \"\"\"تصفية الجدول حسب البحث\"\"\"\n");
        tvSb.append("        search_text = self.search_input.text().lower()\n");
        tvSb.append("        \n");
        tvSb.append("        for row in range(self.table.rowCount()):\n");
        tvSb.append("            show_row = False\n");
        tvSb.append("            for col in range(self.table.columnCount()):\n");
        tvSb.append("                item = self.table.item(row, col)\n");
        tvSb.append("                if item and search_text in item.text().lower():\n");
        tvSb.append("                    show_row = True\n");
        tvSb.append("                    break\n");
        tvSb.append("            self.table.setRowHidden(row, not show_row)\n");
        
        writeFile("table_view.py", tvSb.toString());
        
        // إنشاء ملف form_dialog.py
        generateFormDialogFile();
    }
    
    private void generateFormDialogFile() throws IOException, JSONException {
        StringBuilder sb = new StringBuilder();
        sb.append("#!/usr/bin/env python3\n");
        sb.append("# -*- coding: utf-8 -*-\n\n");
        sb.append("from PyQt5.QtWidgets import *\n");
        sb.append("from PyQt5.QtCore import *\n");
        sb.append("from PyQt5.QtGui import *\n");
        sb.append("from datetime import datetime\n\n");
        
        sb.append("class FormDialog(QDialog):\n");
        sb.append("    \"\"\"نافذة النموذج للإضافة والتعديل\"\"\"\n\n");
        
        sb.append("    def __init__(self, db, table_config, record_id=None):\n");
        sb.append("        super().__init__()\n");
        sb.append("        self.db = db\n");
        sb.append("        self.table_config = table_config\n");
        sb.append("        self.table_name = table_config['name']\n");
        sb.append("        self.record_id = record_id\n");
        sb.append("        self.arabic_name = table_config.get('arabic_name', self.table_name)\n");
        sb.append("        \n");
        sb.append("        self.widgets = {}\n");
        sb.append("        \n");
        sb.append("        self.init_ui()\n");
        sb.append("        \n");
        sb.append("        if record_id:\n");
        sb.append("            self.load_record_data()\n");
        sb.append("            self.setWindowTitle(f'تعديل {self.arabic_name}')\n");
        sb.append("        else:\n");
        sb.append("            self.setWindowTitle(f'إضافة {self.arabic_name} جديد')\n\n");
        
        sb.append("    def init_ui(self):\n");
        sb.append("        layout = QVBoxLayout()\n");
        sb.append("        \n");
        sb.append("        # إنشاء الحقول\n");
        sb.append("        form_layout = QGridLayout()\n");
        sb.append("        \n");
        sb.append("        row = 0\n");
        sb.append("        for field in self.table_config['fields']:\n");
        sb.append("            field_name = field['name']\n");
        sb.append("            \n");
        sb.append("            # تخطي حقل الـ ID في النموذج\n");
        sb.append("            if field.get('primary_key', False):\n");
        sb.append("                continue\n");
        sb.append("            \n");
        sb.append("            arabic_label = field.get('arabic_label', field_name)\n");
        sb.append("            field_type = field.get('type', 'varchar')\n");
        sb.append("            \n");
        sb.append("            # إنشاء التسمية\n");
        sb.append("            label = QLabel(arabic_label + ':')\n");
        sb.append("            label.setAlignment(Qt.AlignRight | Qt.AlignVCenter)\n");
        sb.append("            form_layout.addWidget(label, row, 0)\n");
        sb.append("            \n");
        sb.append("            # إنشاء عنصر الإدخال المناسب\n");
        sb.append("            widget = self.create_input_widget(field)\n");
        sb.append("            form_layout.addWidget(widget, row, 1)\n");
        sb.append("            \n");
        sb.append("            self.widgets[field_name] = widget\n");
        sb.append("            row += 1\n");
        sb.append("        \n");
        sb.append("        layout.addLayout(form_layout)\n");
        sb.append("        \n");
        sb.append("        # أزرار الحفظ والإلغاء\n");
        sb.append("        button_layout = QHBoxLayout()\n");
        sb.append("        \n");
        sb.append("        self.save_btn = QPushButton('حفظ')\n");
        sb.append("        self.save_btn.clicked.connect(self.save_record)\n");
        sb.append("        button_layout.addWidget(self.save_btn)\n");
        sb.append("        \n");
        sb.append("        cancel_btn = QPushButton('إلغاء')\n");
        sb.append("        cancel_btn.clicked.connect(self.reject)\n");
        sb.append("        button_layout.addWidget(cancel_btn)\n");
        sb.append("        \n");
        sb.append("        layout.addLayout(button_layout)\n");
        sb.append("        self.setLayout(layout)\n\n");
        
        sb.append("    def create_input_widget(self, field):\n");
        sb.append("        \"\"\"إنشاء عنصر إدخال مناسب لنوع الحقل\"\"\"\n");
        sb.append("        field_type = field.get('type', 'varchar')\n");
        sb.append("        widget_type = field.get('widget', '')\n");
        sb.append("        \n");
        sb.append("        if field_type == 'integer' or widget_type == 'QSpinBox':\n");
        sb.append("            widget = QSpinBox()\n");
        sb.append("            widget.setRange(-999999, 999999)\n");
        sb.append("            \n");
        sb.append("        elif field_type == 'decimal' or widget_type == 'QDoubleSpinBox':\n");
        sb.append("            widget = QDoubleSpinBox()\n");
        sb.append("            widget.setRange(-999999.99, 999999.99)\n");
        sb.append("            widget.setDecimals(2)\n");
        sb.append("            \n");
        sb.append("        elif field_type == 'boolean' or widget_type == 'QCheckBox':\n");
        sb.append("            widget = QCheckBox()\n");
        sb.append("            \n");
        sb.append("        elif field_type == 'date' or widget_type == 'QDateEdit':\n");
        sb.append("            widget = QDateEdit()\n");
        sb.append("            widget.setCalendarPopup(True)\n");
        sb.append("            widget.setDate(QDate.currentDate())\n");
        sb.append("            widget.setDisplayFormat('yyyy-MM-dd')\n");
        sb.append("            \n");
        sb.append("        elif field_type == 'datetime' or widget_type == 'QDateTimeEdit':\n");
        sb.append("            widget = QDateTimeEdit()\n");
        sb.append("            widget.setCalendarPopup(True)\n");
        sb.append("            widget.setDateTime(QDateTime.currentDateTime())\n");
        sb.append("            widget.setDisplayFormat('yyyy-MM-dd HH:mm')\n");
        sb.append("            \n");
        sb.append("        elif field_type == 'text' or widget_type == 'QTextEdit':\n");
        sb.append("            widget = QTextEdit()\n");
        sb.append("            widget.setMaximumHeight(100)\n");
        sb.append("            \n");
        sb.append("        elif field_type == 'foreign_key' or widget_type == 'QComboBox':\n");
        sb.append("            widget = QComboBox()\n");
        sb.append("            self.load_foreign_key_data(field, widget)\n");
        sb.append("            \n");
        sb.append("        else:  # varchar أو أي نوع نصي\n");
        sb.append("            widget = QLineEdit()\n");
        sb.append("            max_length = field.get('length', 255)\n");
        sb.append("            widget.setMaxLength(max_length)\n");
        sb.append("        \n");
        sb.append("        return widget\n\n");
        
        sb.append("    def load_foreign_key_data(self, field, combo_box):\n");
        sb.append("        \"\"\"تحميل بيانات الجداول المرتبطة\"\"\"\n");
        sb.append("        if 'references' in field:\n");
        sb.append("            ref = field['references']  # مثل 'departments.id'\n");
        sb.append("            ref_table = ref.split('.')[0]\n");
        sb.append("            \n");
        sb.append("            try:\n");
        sb.append("                # جلب البيانات من الجدول المرتبط\n");
        sb.append("                records = self.db.select_all(ref_table)\n");
        sb.append("                \n");
        sb.append("                combo_box.addItem('--- اختر ---', None)\n");
        sb.append("                for record in records:\n");
        sb.append("                    display_value = record.get('name', str(record.get('id', '')))\n");
        sb.append("                    combo_box.addItem(display_value, record['id'])\n");
        sb.append("                    \n");
        sb.append("            except Exception as e:\n");
        sb.append("                print(f'خطأ في تحميل البيانات المرتبطة: {e}')\n\n");
        
        sb.append("    def load_record_data(self):\n");
        sb.append("        \"\"\"تحميل بيانات السجل للتعديل\"\"\"\n");
        sb.append("        if not self.record_id:\n");
        sb.append("            return\n");
        sb.append("        \n");
        sb.append("        try:\n");
        sb.append("            record = self.db.select_by_id(self.table_name, self.record_id)\n");
        sb.append("            if not record:\n");
        sb.append("                return\n");
        sb.append("            \n");
        sb.append("            for field_name, widget in self.widgets.items():\n");
        sb.append("                value = record.get(field_name)\n");
        sb.append("                if value is not None:\n");
        sb.append("                    self.set_widget_value(widget, value)\n");
        sb.append("                    \n");
        sb.append("        except Exception as e:\n");
        sb.append("            print(f'خطأ في تحميل بيانات السجل: {e}')\n\n");
        
        sb.append("    def set_widget_value(self, widget, value):\n");
        sb.append("        \"\"\"تعيين قيمة للـ widget\"\"\"\n");
        sb.append("        if isinstance(widget, QLineEdit):\n");
        sb.append("            widget.setText(str(value))\n");
        sb.append("        elif isinstance(widget, QTextEdit):\n");
        sb.append("            widget.setText(str(value))\n");
        sb.append("        elif isinstance(widget, QSpinBox):\n");
        sb.append("            widget.setValue(int(value))\n");
        sb.append("        elif isinstance(widget, QDoubleSpinBox):\n");
        sb.append("            widget.setValue(float(value))\n");
        sb.append("        elif isinstance(widget, QCheckBox):\n");
        sb.append("            widget.setChecked(bool(value))\n");
        sb.append("        elif isinstance(widget, QDateEdit):\n");
        sb.append("            if isinstance(value, str):\n");
        sb.append("                date = QDate.fromString(value, 'yyyy-MM-dd')\n");
        sb.append("                if date.isValid():\n");
        sb.append("                    widget.setDate(date)\n");
        sb.append("        elif isinstance(widget, QComboBox):\n");
        sb.append("            for i in range(widget.count()):\n");
        sb.append("                if widget.itemData(i) == value:\n");
        sb.append("                    widget.setCurrentIndex(i)\n");
        sb.append("                    break\n\n");
        
        sb.append("    def save_record(self):\n");
        sb.append("        \"\"\"حفظ السجل\"\"\"\n");
        sb.append("        try:\n");
        sb.append("            data = {}\n");
        sb.append("            \n");
        sb.append("            for field_name, widget in self.widgets.items():\n");
        sb.append("                value = self.get_widget_value(widget)\n");
        sb.append("                if value is not None:\n");
        sb.append("                    data[field_name] = value\n");
        sb.append("            \n");
        sb.append("            if self.record_id:\n");
        sb.append("                # تحديث سجل موجود\n");
        sb.append("                success = self.db.update(self.table_name, self.record_id, data)\n");
        sb.append("                if not success:\n");
        sb.append("                    QMessageBox.warning(self, 'تحذير', 'فشل تحديث السجل')\n");
        sb.append("                    return\n");
        sb.append("            else:\n");
        sb.append("                # إضافة سجل جديد\n");
        sb.append("                record_id = self.db.insert(self.table_name, data)\n");
        sb.append("                if not record_id:\n");
        sb.append("                    QMessageBox.warning(self, 'تحذير', 'فشل إضافة السجل')\n");
        sb.append("                    return\n");
        sb.append("            \n");
        sb.append("            self.accept()\n");
        sb.append("            \n");
        sb.append("        except Exception as e:\n");
        sb.append("            QMessageBox.critical(self, 'خطأ', f'فشل حفظ السجل: {str(e)}')\n\n");
        
        sb.append("    def get_widget_value(self, widget):\n");
        sb.append("        \"\"\"استخراج القيمة من الـ widget\"\"\"\n");
        sb.append("        if isinstance(widget, QLineEdit):\n");
        sb.append("            text = widget.text().strip()\n");
        sb.append("            return text if text else None\n");
        sb.append("        elif isinstance(widget, QTextEdit):\n");
        sb.append("            text = widget.toPlainText().strip()\n");
        sb.append("            return text if text else None\n");
        sb.append("        elif isinstance(widget, QSpinBox):\n");
        sb.append("            return widget.value()\n");
        sb.append("        elif isinstance(widget, QDoubleSpinBox):\n");
        sb.append("            return widget.value()\n");
        sb.append("        elif isinstance(widget, QCheckBox):\n");
        sb.append("            return 1 if widget.isChecked() else 0\n");
        sb.append("        elif isinstance(widget, QDateEdit):\n");
        sb.append("            return widget.date().toString('yyyy-MM-dd')\n");
        sb.append("        elif isinstance(widget, QDateTimeEdit):\n");
        sb.append("            return widget.dateTime().toString('yyyy-MM-dd HH:mm:ss')\n");
        sb.append("        elif isinstance(widget, QComboBox):\n");
        sb.append("            return widget.currentData()\n");
        sb.append("        else:\n");
        sb.append("            return None\n");
        
        writeFile("form_dialog.py", sb.toString());
    }
    
    private void generateConfigFile() throws IOException, JSONException {
        StringBuilder sb = new StringBuilder();
        sb.append("#!/usr/bin/env python3\n");
        sb.append("# -*- coding: utf-8 -*-\n\n");
        sb.append("import json\n");
        sb.append("import os\n\n");
        
        sb.append("class Config:\n");
        sb.append("    def __init__(self, config_file='config.json'):\n");
        sb.append("        self.config_file = config_file\n");
        sb.append("        self.config = self.load_config()\n\n");
        
        sb.append("    def load_config(self):\n");
        sb.append("        default_config = {\n");
        sb.append("            'theme': 'light',\n");
        sb.append("            'language': 'ar',\n");
        sb.append("            'font_size': 10,\n");
        sb.append("            'recent_files': []\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        if os.path.exists(self.config_file):\n");
        sb.append("            try:\n");
        sb.append("                with open(self.config_file, 'r', encoding='utf-8') as f:\n");
        sb.append("                    user_config = json.load(f)\n");
        sb.append("                    default_config.update(user_config)\n");
        sb.append("            except:\n");
        sb.append("                pass\n");
        sb.append("        \n");
        sb.append("        return default_config\n\n");
        
        sb.append("    def save(self):\n");
        sb.append("        with open(self.config_file, 'w', encoding='utf-8') as f:\n");
        sb.append("            json.dump(self.config, f, indent=2, ensure_ascii=False)\n\n");
        
        sb.append("    def get(self, key, default=None):\n");
        sb.append("        return self.config.get(key, default)\n\n");
        
        sb.append("    def set(self, key, value):\n");
        sb.append("        self.config[key] = value\n");
        sb.append("        self.save()\n");
        
        writeFile("config.py", sb.toString());
    }
    private void generateModelsFile() throws IOException {
        // هذا الملف للتوافق مع هيكل Django
        String content = """
            # This file is for compatibility with Django-style structure
            # The actual database schema is defined in the JSON configuration
            
            class BaseModel:
                \"\"\"نموذج أساسي للتوافق\"\"\"
                pass
            """;
        
        writeFile("models.py", content);
    }
    
    private void generateMenuFile() throws IOException {
        String content = """
            # ملف القوائم - يمكن تخصيصه حسب الحاجة
            
            MAIN_MENU = {
                'file': {
                    'label': 'ملف',
                    'items': [
                        {'label': 'خروج', 'action': 'exit'}
                    ]
                },
                'tools': {
                    'label': 'أدوات',
                    'items': [
                        {'label': 'نسخ احتياطي', 'action': 'backup'},
                        {'label': 'استعادة', 'action': 'restore'}
                    ]
                }
            }
            """;
        
        writeFile("menu.py", content);
    }
    
    private void generateFormFiles() throws IOException {
        // يمكن إضافة ملفات إضافية للنماذج هنا
    }
    
    private void writeFile(String filename, String content) throws IOException {
        File file = new File(projectDir, filename);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        Log.i("PyQtGenerator", "تم إنشاء الملف: " + filename);
    }
}