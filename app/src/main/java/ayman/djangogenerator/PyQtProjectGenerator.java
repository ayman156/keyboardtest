package ayman.djangogenerator;


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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PyQtProjectGenerator {
    private Context context;
    private JSONObject projectConfig;
    private Map<String, String> fieldTypeToWidget = new HashMap<>();
    private Map<String, String> fieldTypeToPythonType = new HashMap<>();
    private Set<String> requiredImports = new HashSet<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private String font_family = "Arial";
    private String font_size = "14";
    
    public PyQtProjectGenerator(Context context, String jsonConfig) throws JSONException {
        this.context = context;
        this.projectConfig = new JSONObject(jsonConfig);
        initializeTypeMappings();
    }
    
    private void initializeTypeMappings() {
        // Mapping from field type to Qt widget
        fieldTypeToWidget.put("CharField", "QLineEdit");
        fieldTypeToWidget.put("TextField", "QTextEdit");
        fieldTypeToWidget.put("IntegerField", "QSpinBox");
        fieldTypeToWidget.put("DecimalField", "QDoubleSpinBox");
        fieldTypeToWidget.put("BooleanField", "QCheckBox");
        fieldTypeToWidget.put("DateField", "QDateEdit");
        fieldTypeToWidget.put("DateTimeField", "QDateTimeEdit");
        fieldTypeToWidget.put("EmailField", "QLineEdit");
        fieldTypeToWidget.put("ImageField", "QPushButton");
        fieldTypeToWidget.put("ForeignKey", "QComboBox");
        fieldTypeToWidget.put("OneToOneField", "QLineEdit");
        
        // Mapping from field type to Python type
        fieldTypeToPythonType.put("CharField", "str");
        fieldTypeToPythonType.put("TextField", "str");
        fieldTypeToPythonType.put("IntegerField", "int");
        fieldTypeToPythonType.put("DecimalField", "float");
        fieldTypeToPythonType.put("BooleanField", "bool");
        fieldTypeToPythonType.put("DateField", "datetime.date");
        fieldTypeToPythonType.put("DateTimeField", "datetime.datetime");
        fieldTypeToPythonType.put("EmailField", "str");
        fieldTypeToWidget.put("ImageField", "str");
        fieldTypeToPythonType.put("ForeignKey", "int");
        fieldTypeToPythonType.put("OneToOneField", "int");
    }
    
    public boolean generateProject() {
        try {
            String projectName = projectConfig.getString("project_name");
            File projectDir = new File(FileUtil.getExternalStorageDir(), "django_projects/pyqt5/" + projectName);
            //new File(context.getExternalFilesDir(null), projectName);
            
            if (!projectDir.exists()) {
                projectDir.mkdirs();
            }
            
            // Generate all files
            generateMainPy(projectDir);
            generateDatabasePy(projectDir);
            generateLoginPy(projectDir);
            generateMainWindowPy(projectDir);
            generateThemeManagerPy(projectDir);
            generateConfigPy(projectDir);
            generateModels(projectDir);
            generateUiFiles(projectDir);
            generateToolsUiPy(projectDir);
            
            Log.i("PyQtGenerator", "Project generated successfully at: " + projectDir.getAbsolutePath());
            return true;
        } catch (Exception e) {
            Log.e("PyQtGenerator", "Error generating project", e);
            return false;
        }
    }
    
    private void generateMainPy(File projectDir) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("#!/usr/bin/env python3\n");
        sb.append("# -*- coding: utf-8 -*-\n\n");
        sb.append("import sys\n");
        sb.append("import os\n");
        sb.append("from PyQt5.QtWidgets import QApplication, QMessageBox\n");
        sb.append("from PyQt5.QtGui import QFontDatabase, QFont\n");
        sb.append("from PyQt5.QtCore import QTranslator, QLocale, QFile, QTextStream\n");
        sb.append("from database import DatabaseManager\n");
        sb.append("from login import LoginWindow\n");
        sb.append("from config import ConfigManager\n");
        sb.append("from theme_manager import ThemeManager\n\n");
        
        sb.append("def load_stylesheet(theme='light'):\n");
        sb.append("    \"\"\"Load stylesheet based on theme\"\"\"\n");
        sb.append("    if theme == 'dark':\n");
        sb.append("        return '''\n");
        sb.append("        QWidget {\n");
        sb.append("            background-color: #2b2b2b;\n");
        sb.append("            color: #ffffff;\n");
        sb.append("        }\n");
        sb.append("        QPushButton {\n");
        sb.append("            background-color: #3c3c3c;\n");
        sb.append("            border: 1px solid #555;\n");
        sb.append("            padding: 5px;\n");
        sb.append("            border-radius: 3px;\n");
        sb.append("        }\n");
        sb.append("        QLineEdit, QTextEdit, QComboBox, QSpinBox, QDateEdit {\n");
        sb.append("            background-color: #3c3c3c;\n");
        sb.append("            border: 1px solid #555;\n");
        sb.append("            padding: 3px;\n");
        sb.append("        }\n");
        sb.append("        '''\n");
        sb.append("    else:\n");
        sb.append("        return '''\n");
        sb.append("        QWidget {\n");
        sb.append("            background-color: #f5f5f5;\n");
        sb.append("            color: #333333;\n");
        sb.append("        }\n");
        sb.append("        QPushButton {\n");
        sb.append("            background-color: #0078d7;\n");
        sb.append("            color: white;\n");
        sb.append("            border: none;\n");
        sb.append("            padding: 8px;\n");
        sb.append("            border-radius: 4px;\n");
        sb.append("        }\n");
        sb.append("        QLineEdit, QTextEdit, QComboBox, QSpinBox, QDateEdit {\n");
        sb.append("            border: 1px solid #ddd;\n");
        sb.append("            padding: 5px;\n");
        sb.append("            border-radius: 3px;\n");
        sb.append("        }\n");
        sb.append("        '''\n\n");
        
        sb.append("def main():\n");
        sb.append("    app = QApplication(sys.argv)\n\n");
        
        sb.append("    # Load fonts\n");
        sb.append("    font_id = QFontDatabase.addApplicationFont(\"fonts/arial.ttf\")\n");
        sb.append("    if font_id != -1:\n");
        sb.append("        font_family = QFontDatabase.applicationFontFamilies(font_id)[0]\n");
        sb.append("        app.setFont(QFont(font_family, 10))\n\n");
        
        sb.append("    # Load configuration\n");
        sb.append("    config = ConfigManager()\n");
        sb.append("    theme = config.get('theme', 'light')\n");
        sb.append("    language = config.get('language', 'ar')\n\n");
        
        sb.append("    # Apply theme\n");
        sb.append("    app.setStyleSheet(load_stylesheet(theme))\n\n");
        
        sb.append("    # Initialize database\n");
        sb.append("    try:\n");
        sb.append("        db_config = config.get_database_config()\n");
        sb.append("        db_manager = DatabaseManager(**db_config)\n");
        sb.append("        db_manager.connect()\n");
        sb.append("    except Exception as e:\n");
        sb.append("        QMessageBox.critical(None, \"Database Error\", f\"Failed to connect to database: {str(e)}\")\n");
        sb.append("        sys.exit(1)\n\n");
        
        sb.append("    # Show login window\n");
        sb.append("    login_window = LoginWindow(db_manager, config)\n");
        sb.append("    login_window.show()\n\n");
        
        sb.append("    sys.exit(app.exec_())\n\n");
        
        sb.append("if __name__ == \"__main__\":\n");
        sb.append("    main()\n");
        
        saveToFile(new File(projectDir, "main.py"), sb.toString());
    }
    
    private void generateDatabasePy(File projectDir) throws JSONException, IOException {
        JSONObject dbSettings = projectConfig.getJSONObject("database_settings");
        
        StringBuilder sb = new StringBuilder();
        sb.append("#!/usr/bin/env python3\n");
        sb.append("# -*- coding: utf-8 -*-\n\n");
        sb.append("import sqlite3\n");
        sb.append("import mysql.connector\n");
        sb.append("import pymysql\n");
        sb.append("import json\n");
        sb.append("import os\n");
        sb.append("import zipfile\n");
        sb.append("import shutil\n");
        sb.append("from datetime import datetime, date\n");
        sb.append("from typing import Any, List, Dict, Optional, Tuple\n");
        sb.append("import pandas as pd\n");
        sb.append("from openpyxl import Workbook\n");
        sb.append("import traceback\n\n");
        
        sb.append("class DatabaseManager:\n");
        sb.append("    def __init__(self, engine='sqlite3', db_name='database.db', host='localhost', \n");
        sb.append("                 port='', user='', password=''):\n");
        sb.append("        self.engine = engine\n");
        sb.append("        self.db_name = db_name\n");
        sb.append("        self.host = host\n");
        sb.append("        self.port = port\n");
        sb.append("        self.user = user\n");
        sb.append("        self.password = password\n");
        sb.append("        self.connection = None\n");
        sb.append("        self.cursor = None\n\n");
        
        sb.append("    def connect(self):\n");
        sb.append("        \"\"\"Connect to database based on engine\"\"\"\n");
        sb.append("        try:\n");
        sb.append("            if self.engine == 'sqlite3':\n");
        sb.append("                self.connection = sqlite3.connect(self.db_name)\n");
        sb.append("                self.connection.row_factory = sqlite3.Row\n");
        sb.append("            elif self.engine == 'mysql':\n");
        sb.append("                if self.port:\n");
        sb.append("                    self.connection = mysql.connector.connect(\n");
        sb.append("                        host=self.host,\n");
        sb.append("                        port=int(self.port),\n");
        sb.append("                        user=self.user,\n");
        sb.append("                        password=self.password,\n");
        sb.append("                        database=self.db_name\n");
        sb.append("                    )\n");
        sb.append("                else:\n");
        sb.append("                    self.connection = mysql.connector.connect(\n");
        sb.append("                        host=self.host,\n");
        sb.append("                        user=self.user,\n");
        sb.append("                        password=self.password,\n");
        sb.append("                        database=self.db_name\n");
        sb.append("                    )\n");
        sb.append("            self.cursor = self.connection.cursor()\n");
        sb.append("            print(f\"Connected to {self.engine} database: {self.db_name}\")\n");
        sb.append("        except Exception as e:\n");
        sb.append("            print(f\"Connection error: {e}\")\n");
        sb.append("            raise\n\n");
        
        sb.append("    def execute_query(self, query: str, params: tuple = ()) -> Any:\n");
        sb.append("        \"\"\"Execute SQL query\"\"\"\n");
        sb.append("        try:\n");
        sb.append("            self.cursor.execute(query, params)\n");
        sb.append("            if query.strip().upper().startswith(\"SELECT\"):\n");
        sb.append("                return self.cursor.fetchall()\n");
        sb.append("            else:\n");
        sb.append("                self.connection.commit()\n");
        sb.append("                return self.cursor.rowcount\n");
        sb.append("        except Exception as e:\n");
        sb.append("            self.connection.rollback()\n");
        sb.append("            print(f\"Query error: {e}\")\n");
        sb.append("            raise\n\n");
        
        sb.append("    def create_tables_from_config(self, config: dict):\n");
        sb.append("        \"\"\"Create tables based on JSON configuration\"\"\"\n");
        sb.append("        for table_name, table_config in config.items():\n");
        sb.append("            self.create_table(table_name, table_config)\n\n");
        
        sb.append("    def create_table(self, table_name: str, table_config: dict):\n");
        sb.append("        \"\"\"Create a single table\"\"\"\n");
        sb.append("        columns = []\n");
        sb.append("        for field in table_config.get('fields', []):\n");
        sb.append("            field_name = field['name']\n");
        sb.append("            field_type = self._map_field_type(field['type'])\n");
            sb.append("            column_def = f\"{field_name} {field_type}\"\n");
        sb.append("            \n");
        sb.append("            if field.get('unique', False):\n");
        sb.append("                column_def += \" UNIQUE\"\n");
        sb.append("            if field.get('required', False):\n");
        sb.append("                column_def += \" NOT NULL\"\n");
        sb.append("            \n");
        sb.append("            columns.append(column_def)\n");
        sb.append("        \n");
        sb.append("        # Add primary key\n");
        sb.append("        columns.append(\"id INTEGER PRIMARY KEY AUTOINCREMENT\")\n");
        sb.append("        \n");
        sb.append("        create_sql = f\"CREATE TABLE IF NOT EXISTS {table_name} (\\n    \"\n");
        sb.append("        create_sql += \",\\n    \".join(columns)\n");
        sb.append("        create_sql += \"\\n)\"\n");
        sb.append("        \n");
        sb.append("        self.execute_query(create_sql)\n");
        sb.append("        print(f\"Table '{table_name}' created or already exists\")\n\n");
        
        sb.append("    def _map_field_type(self, field_type: str) -> str:\n");
        sb.append("        \"\"\"Map Django field types to SQL types\"\"\"\n");
        sb.append("        type_mapping = {\n");
        sb.append("            'CharField': 'VARCHAR(255)',\n");
        sb.append("            'TextField': 'TEXT',\n");
        sb.append("            'IntegerField': 'INTEGER',\n");
        sb.append("            'DecimalField': 'DECIMAL(10,2)',\n");
        sb.append("            'BooleanField': 'BOOLEAN',\n");
        sb.append("            'DateField': 'DATE',\n");
        sb.append("            'DateTimeField': 'DATETIME',\n");
        sb.append("            'EmailField': 'VARCHAR(255)',\n");
        sb.append("            'ImageField': 'VARCHAR(500)',\n");
        sb.append("            'ForeignKey': 'INTEGER',\n");
        sb.append("            'OneToOneField': 'INTEGER'\n");
        sb.append("        }\n");
        sb.append("        return type_mapping.get(field_type, 'TEXT')\n\n");
        
        sb.append("    # === BACKUP & RESTORE METHODS ===\n");
        sb.append("    \n");
        sb.append("    def backup_database(self, backup_path: str) -> bool:\n");
        sb.append("        \"\"\"Create a backup of the database\"\"\"\n");
        sb.append("        try:\n");
        sb.append("            timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')\n");
        sb.append("            backup_file = os.path.join(backup_path, f\"backup_{timestamp}\")\n");
        sb.append("            \n");
        sb.append("            if self.engine == 'sqlite3':\n");
        sb.append("                # For SQLite, just copy the file\n");
        sb.append("                shutil.copy2(self.db_name, f\"{backup_file}.db\")\n");
        sb.append("                # Also create SQL dump\n");
        sb.append("                self._dump_sqlite_to_sql(f\"{backup_file}.sql\")\n");
        sb.append("            elif self.engine == 'mysql':\n");
        sb.append("                # For MySQL, create SQL dump\n");
        sb.append("                self._dump_mysql_to_sql(f\"{backup_file}.sql\")\n");
        sb.append("            \n");
        sb.append("            # Create metadata file\n");
        sb.append("            metadata = {\n");
        sb.append("                'backup_date': datetime.now().isoformat(),\n");
        sb.append("                'database_name': self.db_name,\n");
        sb.append("                'engine': self.engine,\n");
        sb.append("                'tables': self.get_table_list()\n");
        sb.append("            }\n");
        sb.append("            \n");
        sb.append("            with open(f\"{backup_file}.json\", 'w', encoding='utf-8') as f:\n");
        sb.append("                json.dump(metadata, f, indent=2, ensure_ascii=False)\n");
        sb.append("            \n");
        sb.append("            # Create zip archive\n");
        sb.append("            with zipfile.ZipFile(f\"{backup_file}.zip\", 'w') as zipf:\n");
        sb.append("                for file in os.listdir(backup_path):\n");
        sb.append("                    if file.startswith(f\"backup_{timestamp}\"):\n");
        sb.append("                        zipf.write(os.path.join(backup_path, file), file)\n");
        sb.append("                        os.remove(os.path.join(backup_path, file))\n");
        sb.append("            \n");
        sb.append("            return True\n");
        sb.append("        except Exception as e:\n");
        sb.append("            print(f\"Backup failed: {e}\")\n");
        sb.append("            traceback.print_exc()\n");
        sb.append("            return False\n\n");
        
        sb.append("    def _dump_sqlite_to_sql(self, output_file: str):\n");
        sb.append("        \"\"\"Dump SQLite database to SQL file\"\"\"\n");
        sb.append("        try:\n");
        sb.append("            with open(output_file, 'w', encoding='utf-8') as f:\n");
        sb.append("                for line in self.connection.iterdump():\n");
        sb.append("                    f.write(f\"{line}\\n\")\n");
        sb.append("        except Exception as e:\n");
        sb.append("            print(f\"SQL dump failed: {e}\")\n\n");
        
        sb.append("    def restore_database(self, backup_file: str) -> bool:\n");
        sb.append("        \"\"\"Restore database from backup\"\"\"\n");
        sb.append("        try:\n");
        sb.append("            # Extract zip if needed\n");
        sb.append("            temp_dir = \"temp_restore\"\n");
        sb.append("            if backup_file.endswith('.zip'):\n");
        sb.append("                with zipfile.ZipFile(backup_file, 'r') as zipf:\n");
        sb.append("                    zipf.extractall(temp_dir)\n");
        sb.append("                # Find extracted files\n");
        sb.append("                for file in os.listdir(temp_dir):\n");
        sb.append("                    if file.endswith('.db') or file.endswith('.sql'):\n");
        sb.append("                        backup_file = os.path.join(temp_dir, file)\n");
        sb.append("                        break\n");
        sb.append("            \n");
        sb.append("            if self.engine == 'sqlite3':\n");
        sb.append("                if backup_file.endswith('.db'):\n");
        sb.append("                    # Close current connection\n");
        sb.append("                    self.close()\n");
        sb.append("                    # Replace database file\n");
        sb.append("                    shutil.copy2(backup_file, self.db_name)\n");
        sb.append("                    # Reconnect\n");
        sb.append("                    self.connect()\n");
        sb.append("                elif backup_file.endswith('.sql'):\n");
        sb.append("                    # Execute SQL file\n");
        sb.append("                    self._execute_sql_file(backup_file)\n");
        sb.append("            \n");
        sb.append("            # Cleanup\n");
        sb.append("            if os.path.exists(temp_dir):\n");
        sb.append("                shutil.rmtree(temp_dir)\n");
        sb.append("            \n");
        sb.append("            return True\n");
        sb.append("        except Exception as e:\n");
        sb.append("            print(f\"Restore failed: {e}\")\n");
        sb.append("            traceback.print_exc()\n");
        sb.append("            return False\n\n");
        
        sb.append("    def _execute_sql_file(self, sql_file: str):\n");
        sb.append("        \"\"\"Execute SQL commands from file\"\"\"\n");
        sb.append("        with open(sql_file, 'r', encoding='utf-8') as f:\n");
        sb.append("            sql_commands = f.read().split(';')\n");
        sb.append("            \n");
        sb.append("            for command in sql_commands:\n");
        sb.append("                command = command.strip()\n");
        sb.append("                if command:\n");
        sb.append("                    try:\n");
        sb.append("                        self.execute_query(command)\n");
        sb.append("                    except Exception as e:\n");
        sb.append("                        print(f\"Error executing command: {command[:50]}...\")\n");
        sb.append("                        print(f\"Error: {e}\")\n\n");
        
        sb.append("    # === EXCEL IMPORT/EXPORT METHODS ===\n");
        sb.append("    \n");
        sb.append("    def export_to_excel(self, table_name: str, output_file: str) -> bool:\n");
        sb.append("        \"\"\"Export table data to Excel file\"\"\"\n");
        sb.append("        try:\n");
        sb.append("            # Get data\n");
        sb.append("            query = f\"SELECT * FROM {table_name}\"\n");
        sb.append("            self.cursor.execute(query)\n");
        sb.append("            rows = self.cursor.fetchall()\n");
        sb.append("            \n");
        sb.append("            if not rows:\n");
        sb.append("                print(f\"No data found in table {table_name}\")\n");
        sb.append("                return False\n");
        sb.append("            \n");
        sb.append("            # Get column names\n");
        sb.append("            column_names = [desc[0] for desc in self.cursor.description]\n");
        sb.append("            \n");
        sb.append("            # Create DataFrame\n");
        sb.append("            df = pd.DataFrame(rows, columns=column_names)\n");
        sb.append("            \n");
        sb.append("            # Export to Excel\n");
        sb.append("            df.to_excel(output_file, index=False)\n");
        sb.append("            print(f\"Data exported to {output_file}\")\n");
        sb.append("            return True\n");
        sb.append("            \n");
        sb.append("        except Exception as e:\n");
        sb.append("            print(f\"Export failed: {e}\")\n");
        sb.append("            traceback.print_exc()\n");
        sb.append("            return False\n\n");
        
        sb.append("    def import_from_excel(self, table_name: str, excel_file: str, \n");
        sb.append("                          foreign_key_mappings: dict = None) -> Tuple[bool, str]:\n");
        sb.append("        \"\"\"Import data from Excel file to database\"\"\"\n");
        sb.append("        try:\n");
        sb.append("            # Read Excel file\n");
        sb.append("            df = pd.read_excel(excel_file)\n");
        sb.append("            \n");
        sb.append("            # Get table structure\n");
        sb.append("            self.cursor.execute(f\"PRAGMA table_info({table_name})\")\n");
        sb.append("            table_info = self.cursor.fetchall()\n");
        sb.append("            \n");
        sb.append("            # Prepare data for insertion\n");
        sb.append("            success_count = 0\n");
        sb.append("            error_count = 0\n");
        sb.append("            error_messages = []\n");
        sb.append("            \n");
        sb.append("            for _, row in df.iterrows():\n");
        sb.append("                try:\n");
        sb.append("                    # Convert row to dict\n");
        sb.append("                    row_dict = row.to_dict()\n");
        sb.append("                    \n");
        sb.append("                    # Handle foreign keys\n");
        sb.append("                    if foreign_key_mappings:\n");
        sb.append("                        for fk_field, lookup_config in foreign_key_mappings.items():\n");
        sb.append("                            if fk_field in row_dict:\n");
        sb.append("                                lookup_value = row_dict[fk_field]\n");
        sb.append("                                fk_id = self._lookup_foreign_key(\n");
        sb.append("                                    lookup_config['table'],\n");
        sb.append("                                    lookup_config['display_field'],\n");
        sb.append("                                    lookup_value\n");
        sb.append("                                )\n");
        sb.append("                                if fk_id:\n");
        sb.append("                                    row_dict[fk_field] = fk_id\n");
        sb.append("                                else:\n");
        sb.append("                                    raise ValueError(\n");
        sb.append("                                        f\"Foreign key not found: {lookup_value} in {lookup_config['table']}\"\n");
        sb.append("                                    )\n");
        sb.append("                    \n");
        sb.append("                    # Build insert query\n");
        sb.append("                    columns = list(row_dict.keys())\n");
        sb.append("                    values = list(row_dict.values())\n");
        sb.append("                    placeholders = ','.join(['?' for _ in values])\n");
        sb.append("                    \n");
        sb.append("                    query = f\"INSERT INTO {table_name} ({','.join(columns)}) VALUES ({placeholders})\"\n");
        sb.append("                    self.execute_query(query, tuple(values))\n");
        sb.append("                    success_count += 1\n");
        sb.append("                    \n");
        sb.append("                except Exception as e:\n");
        sb.append("                    error_count += 1\n");
        sb.append("                    error_messages.append(f\"Row {_}: {str(e)}\")\n");
        sb.append("            \n");
        sb.append("            message = f\"Import completed. Success: {success_count}, Failed: {error_count}\"\n");
        sb.append("            if error_messages:\n");
        sb.append("                message += \"\\nErrors: \" + \"\\n\".join(error_messages[:5])\n");
        sb.append("            \n");
        sb.append("            return (success_count > 0, message)\n");
        sb.append("            \n");
        sb.append("        except Exception as e:\n");
        sb.append("            return (False, f\"Import failed: {str(e)}\")\n\n");
        
        sb.append("    def _lookup_foreign_key(self, table_name: str, display_field: str, value: Any) -> Optional[int]:\n");
        sb.append("        \"\"\"Look up foreign key ID based on display value\"\"\"\n");
        sb.append("        try:\n");
        sb.append("            query = f\"SELECT id FROM {table_name} WHERE {display_field} = ?\"\n");
        sb.append("            self.cursor.execute(query, (value,))\n");
        sb.append("            result = self.cursor.fetchone()\n");
        sb.append("            return result[0] if result else None\n");
        sb.append("        except:\n");
        sb.append("            return None\n\n");
        
        sb.append("    # === UTILITY METHODS ===\n");
        sb.append("    \n");
        sb.append("    def get_table_list(self) -> List[str]:\n");
        sb.append("        \"\"\"Get list of all tables\"\"\"\n");
        sb.append("        if self.engine == 'sqlite3':\n");
        sb.append("            self.cursor.execute(\"SELECT name FROM sqlite_master WHERE type='table'\")\n");
        sb.append("        elif self.engine == 'mysql':\n");
        sb.append("            self.cursor.execute(\"SHOW TABLES\")\n");
        sb.append("        return [row[0] for row in self.cursor.fetchall()]\n\n");
        
        sb.append("    def get_table_data(self, table_name: str, limit: int = 1000) -> List[Dict]:\n");
        sb.append("        \"\"\"Get data from table\"\"\"\n");
        sb.append("        query = f\"SELECT * FROM {table_name} LIMIT ?\"\n");
        sb.append("        self.cursor.execute(query, (limit,))\n");
        sb.append("        rows = self.cursor.fetchall()\n");
        sb.append("        \n");
        sb.append("        # Convert rows to dict\n");
        sb.append("        result = []\n");
        sb.append("        for row in rows:\n");
        sb.append("            if isinstance(row, dict):\n");
        sb.append("                result.append(row)\n");
        sb.append("            else:\n");
        sb.append("                result.append(dict(zip([desc[0] for desc in self.cursor.description], row)))\n");
        sb.append("        return result\n\n");
        
        sb.append("    def close(self):\n");
        sb.append("        \"\"\"Close database connection\"\"\"\n");
        sb.append("        if self.cursor:\n");
        sb.append("            self.cursor.close()\n");
        sb.append("        if self.connection:\n");
        sb.append("            self.connection.close()\n");
        sb.append("        print(\"Database connection closed\")\n\n");
        
        sb.append("    def __del__(self):\n");
        sb.append("        self.close()\n");
        
        saveToFile(new File(projectDir, "database.py"), sb.toString());
    }
    
    private void generateLoginPy(File projectDir) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("#!/usr/bin/env python3\n");
        sb.append("# -*- coding: utf-8 -*-\n\n");
        sb.append("import sys\n");
        sb.append("from PyQt5.QtWidgets import (\n");
        sb.append("    QWidget, QVBoxLayout, QHBoxLayout, QLabel, \n");
        sb.append("    QLineEdit, QPushButton, QMessageBox, QFrame\n");
        sb.append(")\n");
        sb.append("from PyQt5.QtCore import Qt, pyqtSignal\n");
        sb.append("from PyQt5.QtGui import QFont, QIcon\n");
        sb.append("from main_window import MainWindow\n\n");
        
        sb.append("class LoginWindow(QWidget):\n");
        sb.append("    login_successful = pyqtSignal(str)  # Signal for successful login\n\n");
        
        sb.append("    def __init__(self, db_manager, config):\n");
        sb.append("        super().__init__()\n");
        sb.append("        self.db_manager = db_manager\n");
        sb.append("        self.config = config\n");
        sb.append("        self.main_window = None\n");
        sb.append("        self.init_ui()\n\n");
        
        sb.append("    def init_ui(self):\n");
        sb.append("        self.setWindowTitle(\"نظام إدارة الموارد البشرية - تسجيل الدخول\")\n");
        sb.append("        self.setFixedSize(400, 500)\n");
        sb.append("        \n");
        sb.append("        # Main layout\n");
        sb.append("        main_layout = QVBoxLayout()\n");
        sb.append("        main_layout.setSpacing(20)\n");
        sb.append("        main_layout.setContentsMargins(40, 40, 40, 40)\n\n");
        
        sb.append("        # Title\n");
        sb.append("        title_label = QLabel(\"تسجيل الدخول\")\n");
        sb.append("        title_label.setAlignment(Qt.AlignCenter)\n");
        sb.append("        title_font = QFont()\n");
        sb.append("        title_font.setPointSize(20)\n");
        sb.append("        title_font.setBold(True)\n");
        sb.append("        title_label.setFont(title_font)\n");
        sb.append("        main_layout.addWidget(title_label)\n\n");
        
        sb.append("        # Separator\n");
        sb.append("        separator = QFrame()\n");
        sb.append("        separator.setFrameShape(QFrame.HLine)\n");
        sb.append("        separator.setFrameShadow(QFrame.Sunken)\n");
        sb.append("        main_layout.addWidget(separator)\n\n");
        
        sb.append("        # Username field\n");
        sb.append("        username_layout = QHBoxLayout()\n");
        sb.append("        username_label = QLabel(\"اسم المستخدم:\")\n");
        sb.append("        self.username_input = QLineEdit()\n");
        sb.append("        self.username_input.setPlaceholderText(\"أدخل اسم المستخدم\")\n");
        sb.append("        username_layout.addWidget(username_label)\n");
        sb.append("        username_layout.addWidget(self.username_input)\n");
        sb.append("        main_layout.addLayout(username_layout)\n\n");
        
        sb.append("        # Password field\n");
        sb.append("        password_layout = QHBoxLayout()\n");
        sb.append("        password_label = QLabel(\"كلمة المرور:\")\n");
        sb.append("        self.password_input = QLineEdit()\n");
        sb.append("        self.password_input.setPlaceholderText(\"أدخل كلمة المرور\")\n");
        sb.append("        self.password_input.setEchoMode(QLineEdit.Password)\n");
        sb.append("        password_layout.addWidget(password_label)\n");
        sb.append("        password_layout.addWidget(self.password_input)\n");
        sb.append("        main_layout.addLayout(password_layout)\n\n");
        
        sb.append("        # Remember me checkbox\n");
        sb.append("        self.remember_checkbox = QCheckBox(\"تذكرني\")\n");
        sb.append("        main_layout.addWidget(self.remember_checkbox)\n\n");
        
        sb.append("        # Login button\n");
        sb.append("        self.login_button = QPushButton(\"تسجيل الدخول\")\n");
        sb.append("        self.login_button.setFixedHeight(40)\n");
        sb.append("        self.login_button.clicked.connect(self.authenticate)\n");
        sb.append("        main_layout.addWidget(self.login_button)\n\n");
        
        sb.append("        # Error message label\n");
        sb.append("        self.error_label = QLabel(\"\")\n");
        sb.append("        self.error_label.setStyleSheet(\"color: red;\")\n");
        sb.append("        self.error_label.setAlignment(Qt.AlignCenter)\n");
        sb.append("        main_layout.addWidget(self.error_label)\n\n");
        
        sb.append("        # Set layout\n");
        sb.append("        self.setLayout(main_layout)\n\n");
        
        sb.append("        # Set focus to username field\n");
        sb.append("        self.username_input.setFocus()\n\n");
        
        sb.append("        # Connect return key to login\n");
        sb.append("        self.username_input.returnPressed.connect(self.authenticate)\n");
        sb.append("        self.password_input.returnPressed.connect(self.authenticate)\n\n");
        
        sb.append("        # Load saved credentials if remember me was checked\n");
        sb.append("        self.load_saved_credentials()\n\n");
        
        sb.append("    def authenticate(self):\n");
        sb.append("        username = self.username_input.text().strip()\n");
        sb.append("        password = self.password_input.text().strip()\n");
        sb.append("        \n");
        sb.append("        if not username or not password:\n");
        sb.append("            self.error_label.setText(\"يرجى إدخال اسم المستخدم وكلمة المرور\")\n");
        sb.append("            return\n");
        sb.append("        \n");
        sb.append("        try:\n");
        sb.append("            # Check credentials in database\n");
        sb.append("            query = \"\"\"\n");
        sb.append("                SELECT * FROM auth_user \n");
        sb.append("                WHERE username = ? AND password = ?\n");
        sb.append("            \"\"\"\n");
        sb.append("            \n");
        sb.append("            # In production, use hashed passwords!\n");
        sb.append("            result = self.db_manager.execute_query(query, (username, password))\n");
        sb.append("            \n");
        sb.append("            if result:\n");
        sb.append("                # Save credentials if remember me is checked\n");
        sb.append("                if self.remember_checkbox.isChecked():\n");
        sb.append("                    self.config.set('remember_username', username)\n");
        sb.append("                else:\n");
        sb.append("                    self.config.remove('remember_username')\n");
        sb.append("                \n");
        sb.append("                self.error_label.setText(\"\")\n");
        sb.append("                self.show_main_window(username)\n");
        sb.append("            else:\n");
        sb.append("                self.error_label.setText(\"اسم المستخدم أو كلمة المرور غير صحيحة\")\n");
        sb.append("                \n");
        sb.append("        except Exception as e:\n");
        sb.append("            self.error_label.setText(f\"خطأ في الاتصال: {str(e)}\")\n\n");
        
        sb.append("    def show_main_window(self, username):\n");
        sb.append("        self.main_window = MainWindow(self.db_manager, self.config, username)\n");
        sb.append("        self.main_window.show()\n");
        sb.append("        self.close()\n\n");
        
        sb.append("    def load_saved_credentials(self):\n");
        sb.append("        saved_username = self.config.get('remember_username', '')\n");
        sb.append("        if saved_username:\n");
        sb.append("            self.username_input.setText(saved_username)\n");
        sb.append("            self.remember_checkbox.setChecked(True)\n");
        sb.append("            self.password_input.setFocus()\n");
        
        saveToFile(new File(projectDir, "login.py"), sb.toString());
    }
    
    private void generateMainWindowPy(File projectDir) throws JSONException, IOException {
        JSONObject modelsConfig = projectConfig.getJSONObject("models_config");
        
        StringBuilder sb = new StringBuilder();
        sb.append("#!/usr/bin/env python3\n");
        sb.append("# -*- coding: utf-8 -*-\n\n");
        sb.append("import sys\n");
        sb.append("from PyQt5.QtWidgets import (\n");
        sb.append("    QMainWindow, QWidget, QVBoxLayout, QHBoxLayout, \n");
        sb.append("    QPushButton, QLabel, QFrame, QStackedWidget,\n");
        sb.append("    QTableWidget, QTableWidgetItem, QHeaderView,\n");
        sb.append("    QMessageBox, QInputDialog, QFileDialog,\n");
        sb.append("    QToolBar, QStatusBar, QMenuBar, QMenu,\n");
        sb.append("    QAction, QComboBox, QLineEdit, QDateEdit,\n");
        sb.append("    QTextEdit, QSpinBox, QDoubleSpinBox, QCheckBox,\n");
        sb.append("    QGridLayout, QGroupBox, QScrollArea, QTabWidget\n");
        sb.append(")\n");
        sb.append("from PyQt5.QtCore import Qt, QDate, QDateTime, pyqtSlot\n");
        sb.append("from PyQt5.QtGui import QIcon, QFont, QPixmap\n");
        sb.append("import os\n");
        sb.append("import json\n\n");
        
        sb.append("class MainWindow(QMainWindow):\n");
        sb.append("    def __init__(self, db_manager, config, username):\n");
        sb.append("        super().__init__()\n");
        sb.append("        self.db_manager = db_manager\n");
        sb.append("        self.config = config\n");
        sb.append("        self.username = username\n");
        sb.append("        self.current_module = None\n");
        sb.append("        self.model_widgets = {}\n");
        sb.append("        self.init_ui()\n");
        sb.append("        self.load_models()\n\n");
        
        sb.append("    def init_ui(self):\n");
        sb.append("        self.setWindowTitle(f\"نظام إدارة الموارد البشرية - {self.username}\")\n");
        sb.append("        self.setGeometry(100, 100, 1200, 700)\n\n");
        
        sb.append("        # Create central widget\n");
        sb.append("        central_widget = QWidget()\n");
        sb.append("        self.setCentralWidget(central_widget)\n");
        sb.append("        \n");
        sb.append("        # Main layout\n");
        sb.append("        main_layout = QHBoxLayout()\n");
        sb.append("        main_layout.setContentsMargins(0, 0, 0, 0)\n");
        sb.append("        main_layout.setSpacing(0)\n\n");
        
        sb.append("        # Create sidebar\n");
        sb.append("        self.sidebar = self.create_sidebar()\n");
        sb.append("        main_layout.addWidget(self.sidebar, 1)\n\n");
        
        sb.append("        # Create main content area\n");
        sb.append("        self.content_stack = QStackedWidget()\n");
        sb.append("        main_layout.addWidget(self.content_stack, 4)\n\n");
        
        sb.append("        # Set main layout\n");
        sb.append("        central_widget.setLayout(main_layout)\n\n");
        
        sb.append("        # Create menu bar\n");
        sb.append("        self.create_menu_bar()\n\n");
        
        sb.append("        # Create status bar\n");
        sb.append("        self.statusBar().showMessage(\"جاهز\")\n\n");
        
        sb.append("        # Add dashboard as default view\n");
        sb.append("        self.show_dashboard()\n\n");
        
        sb.append("    def create_sidebar(self):\n");
        sb.append("        sidebar = QWidget()\n");
        sb.append("        sidebar.setFixedWidth(250)\n");
        sb.append("        sidebar.setStyleSheet(\"\"\"\n");
        sb.append("            QWidget {\n");
        sb.append("                background-color: #2c3e50;\n");
        sb.append("                color: white;\n");
        sb.append("            }\n");
        sb.append("            QPushButton {\n");
        sb.append("                text-align: left;\n");
        sb.append("                padding: 10px;\n");
        sb.append("                border: none;\n");
        sb.append("                background-color: transparent;\n");
        sb.append("                color: white;\n");
        sb.append("            }\n");
        sb.append("            QPushButton:hover {\n");
        sb.append("                background-color: #34495e;\n");
        sb.append("            }\n");
        sb.append("            QPushButton:pressed {\n");
        sb.append("                background-color: #1abc9c;\n");
        sb.append("            }\n");
        sb.append("        \"\"\")\n");
        sb.append("        \n");
        sb.append("        layout = QVBoxLayout()\n");
        sb.append("        layout.setContentsMargins(0, 0, 0, 0)\n");
        sb.append("        layout.setSpacing(0)\n\n");
        
        sb.append("        # Logo/Title area\n");
        sb.append("        title_widget = QWidget()\n");
        sb.append("        title_layout = QVBoxLayout()\n");
        sb.append("        title_label = QLabel(\"HR System\")\n");
        sb.append("        title_label.setAlignment(Qt.AlignCenter)\n");
        sb.append("        title_label.setStyleSheet(\"font-size: 18px; font-weight: bold; padding: 20px;\")\n");
        sb.append("        title_layout.addWidget(title_label)\n");
        sb.append("        title_widget.setLayout(title_layout)\n");
        sb.append("        layout.addWidget(title_widget)\n\n");
        
        sb.append("        # Separator\n");
        sb.append("        separator = QFrame()\n");
        sb.append("        separator.setFrameShape(QFrame.HLine)\n");
        sb.append("        separator.setFrameShadow(QFrame.Sunken)\n");
        sb.append("        separator.setStyleSheet(\"background-color: #34495e;\")\n");
        sb.append("        layout.addWidget(separator)\n\n");
        
        sb.append("        # Navigation buttons\n");
        sb.append("        nav_layout = QVBoxLayout()\n");
        sb.append("        nav_layout.setSpacing(1)\n\n");
        
        sb.append("        # Dashboard button\n");
        sb.append("        self.dashboard_btn = QPushButton(\"الرئيسية\")\n");
        sb.append("        self.dashboard_btn.setIcon(QIcon.fromTheme(\"go-home\"))\n");
        sb.append("        self.dashboard_btn.clicked.connect(self.show_dashboard)\n");
        sb.append("        nav_layout.addWidget(self.dashboard_btn)\n\n");
        
        sb.append("        # HR Module\n");
        sb.append("        hr_label = QLabel(\"الموارد البشرية\")\n");
        sb.append("        hr_label.setStyleSheet(\"padding: 10px 5px; color: #95a5a6; font-weight: bold;\")\n");
        sb.append("        nav_layout.addWidget(hr_label)\n");
        sb.append("        \n");
        sb.append("        self.employees_btn = QPushButton(\"الموظفون\")\n");
        sb.append("        self.employees_btn.clicked.connect(lambda: self.show_model('employees.Employee'))\n");
        sb.append("        nav_layout.addWidget(self.employees_btn)\n");
        sb.append("        \n");
        sb.append("        self.departments_btn = QPushButton(\"الأقسام\")\n");
        sb.append("        self.departments_btn.clicked.connect(lambda: self.show_model('core.Department'))\n");
        sb.append("        nav_layout.addWidget(self.departments_btn)\n");
        sb.append("        \n");
        sb.append("        self.job_titles_btn = QPushButton(\"المسميات الوظيفية\")\n");
        sb.append("        self.job_titles_btn.clicked.connect(lambda: self.show_model('core.JobTitle'))\n");
        sb.append("        nav_layout.addWidget(self.job_titles_btn)\n\n");
        
        sb.append("        # Training Module\n");
        sb.append("        training_label = QLabel(\"التدريب\")\n");
        sb.append("        training_label.setStyleSheet(\"padding: 10px 5px; color: #95a5a6; font-weight: bold;\")\n");
        sb.append("        nav_layout.addWidget(training_label)\n");
        sb.append("        \n");
        sb.append("        self.courses_btn = QPushButton(\"الدورات\")\n");
        sb.append("        self.courses_btn.clicked.connect(lambda: self.show_model('training.Course'))\n");
        sb.append("        nav_layout.addWidget(self.courses_btn)\n");
        sb.append("        \n");
        sb.append("        self.participants_btn = QPushButton(\"المشاركون\")\n");
        sb.append("        self.participants_btn.clicked.connect(lambda: self.show_model('training.CourseParticipant'))\n");
        sb.append("        nav_layout.addWidget(self.participants_btn)\n\n");
        
        sb.append("        # Tools Module\n");
        sb.append("        tools_label = QLabel(\"الأدوات\")\n");
        sb.append("        tools_label.setStyleSheet(\"padding: 10px 5px; color: #95a5a6; font-weight: bold;\")\n");
        sb.append("        nav_layout.addWidget(tools_label)\n");
        sb.append("        \n");
        sb.append("        self.tools_btn = QPushButton(\"إدارة البيانات\")\n");
        sb.append("        self.tools_btn.clicked.connect(self.show_tools)\n");
        sb.append("        nav_layout.addWidget(self.tools_btn)\n");
        sb.append("        \n");
        sb.append("        self.settings_btn = QPushButton(\"الإعدادات\")\n");
        sb.append("        self.settings_btn.clicked.connect(self.show_settings)\n");
        sb.append("        nav_layout.addWidget(self.settings_btn)\n\n");
        
        sb.append("        # Add stretch at the bottom\n");
        sb.append("        nav_layout.addStretch()\n\n");
        
        sb.append("        # Logout button\n");
        sb.append("        self.logout_btn = QPushButton(\"تسجيل الخروج\")\n");
        sb.append("        self.logout_btn.clicked.connect(self.logout)\n");
        sb.append("        self.logout_btn.setStyleSheet(\"\"\"\n");
        sb.append("            QPushButton {\n");
        sb.append("                background-color: #e74c3c;\n");
        sb.append("                margin: 10px;\n");
        sb.append("                padding: 10px;\n");
        sb.append("                border-radius: 3px;\n");
        sb.append("            }\n");
        sb.append("            QPushButton:hover {\n");
        sb.append("                background-color: #c0392b;\n");
        sb.append("            }\n");
        sb.append("        \"\"\")\n");
        sb.append("        nav_layout.addWidget(self.logout_btn)\n\n");
        
        sb.append("        # Add navigation to main layout\n");
        sb.append("        nav_widget = QWidget()\n");
        sb.append("        nav_widget.setLayout(nav_layout)\n");
        sb.append("        layout.addWidget(nav_widget)\n\n");
        
        sb.append("        sidebar.setLayout(layout)\n");
        sb.append("        return sidebar\n\n");
        
        sb.append("    def create_menu_bar(self):\n");
        sb.append("        menubar = self.menuBar()\n");
        sb.append("        \n");
        sb.append("        # File menu\n");
        sb.append("        file_menu = menubar.addMenu('ملف')\n");
        sb.append("        \n");
        sb.append("        export_action = QAction('تصدير البيانات', self)\n");
        sb.append("        export_action.triggered.connect(self.export_data)\n");
        sb.append("        file_menu.addAction(export_action)\n");
        sb.append("        \n");
        sb.append("        import_action = QAction('استيراد البيانات', self)\n");
        sb.append("        import_action.triggered.connect(self.import_data)\n");
        sb.append("        file_menu.addAction(import_action)\n");
        sb.append("        \n");
        sb.append("        file_menu.addSeparator()\n");
        sb.append("        \n");
        sb.append("        exit_action = QAction('خروج', self)\n");
        sb.append("        exit_action.triggered.connect(self.close)\n");
        sb.append("        file_menu.addAction(exit_action)\n");
        sb.append("        \n");
        sb.append("        # View menu\n");
        sb.append("        view_menu = menubar.addMenu('عرض')\n");
        sb.append("        \n");
        sb.append("        refresh_action = QAction('تحديث', self)\n");
        sb.append("        refresh_action.triggered.connect(self.refresh_current_view)\n");
        sb.append("        refresh_action.setShortcut('F5')\n");
        sb.append("        view_menu.addAction(refresh_action)\n");
        sb.append("        \n");
        sb.append("        # Tools menu\n");
        sb.append("        tools_menu = menubar.addMenu('أدوات')\n");
        sb.append("        \n");
        sb.append("        backup_action = QAction('نسخ احتياطي', self)\n");
        sb.append("        backup_action.triggered.connect(self.backup_database)\n");
        sb.append("        tools_menu.addAction(backup_action)\n");
        sb.append("        \n");
        sb.append("        restore_action = QAction('استعادة نسخة', self)\n");
        sb.append("        restore_action.triggered.connect(self.restore_database)\n");
        sb.append("        tools_menu.addAction(restore_action)\n");
        sb.append("        \n");
        sb.append("        # Help menu\n");
        sb.append("        help_menu = menubar.addMenu('مساعدة')\n");
        sb.append("        \n");
        sb.append("        about_action = QAction('حول', self)\n");
        sb.append("        about_action.triggered.connect(self.show_about)\n");
        sb.append("        help_menu.addAction(about_action)\n\n");
        
        sb.append("    def load_models(self):\n");
        sb.append("        \"\"\"Load all models from configuration\"\"\"\n");
        sb.append("        models_config = ");
        sb.append(modelsConfig.toString(2).replace("\n", "\n        "));
        sb.append("\n        \n");
        sb.append("        for model_name, model_config in models_config.items():\n");
        sb.append("            self.create_model_widget(model_name, model_config)\n\n");
        
        sb.append("    def create_model_widget(self, model_name, model_config):\n");
        sb.append("        \"\"\"Create a widget for a model\"\"\"\n");
        sb.append("        widget = QWidget()\n");
        sb.append("        layout = QVBoxLayout()\n\n");
        
        sb.append("        # Title\n");
        sb.append("        title = QLabel(model_config.get('verbose_name', model_name))\n");
        sb.append("        title.setStyleSheet(\"font-size: 18px; font-weight: bold; padding: 10px;\")\n");
        sb.append("        layout.addWidget(title)\n\n");
        
        sb.append("        # Toolbar\n");
        sb.append("        toolbar = QHBoxLayout()\n");
        sb.append("        \n");
        sb.append("        add_btn = QPushButton(\"إضافة\")\n");
        sb.append("        add_btn.clicked.connect(lambda: self.show_add_form(model_name, model_config))\n");
        sb.append("        toolbar.addWidget(add_btn)\n");
        sb.append("        \n");
        sb.append("        edit_btn = QPushButton(\"تعديل\")\n");
        sb.append("        edit_btn.clicked.connect(lambda: self.edit_selected(model_name))\n");
        sb.append("        toolbar.addWidget(edit_btn)\n");
        sb.append("        \n");
        sb.append("        delete_btn = QPushButton(\"حذف\")\n");
        sb.append("        delete_btn.clicked.connect(lambda: self.delete_selected(model_name))\n");
        sb.append("        toolbar.addWidget(delete_btn)\n");
        sb.append("        \n");
        sb.append("        refresh_btn = QPushButton(\"تحديث\")\n");
        sb.append("        refresh_btn.clicked.connect(lambda: self.refresh_table(model_name))\n");
        sb.append("        toolbar.addWidget(refresh_btn)\n");
        sb.append("        \n");
        sb.append("        export_btn = QPushButton(\"تصدير إلى Excel\")\n");
        sb.append("        export_btn.clicked.connect(lambda: self.export_to_excel(model_name))\n");
        sb.append("        toolbar.addWidget(export_btn)\n");
        sb.append("        \n");
        sb.append("        toolbar.addStretch()\n");
        sb.append("        \n");
        sb.append("        # Search box\n");
        sb.append("        search_label = QLabel(\"بحث:\")\n");
        sb.append("        toolbar.addWidget(search_label)\n");
        sb.append("        \n");
        sb.append("        search_input = QLineEdit()\n");
        sb.append("        search_input.setFixedWidth(200)\n");
        sb.append("        search_input.textChanged.connect(lambda text: self.search_table(model_name, text))\n");
        sb.append("        toolbar.addWidget(search_input)\n");
        sb.append("        \n");
        sb.append("        layout.addLayout(toolbar)\n\n");
        
        sb.append("        # Create table\n");
        sb.append("        table = QTableWidget()\n");
        sb.append("        table.setAlternatingRowColors(True)\n");
        sb.append("        table.horizontalHeader().setStretchLastSection(True)\n");
        sb.append("        table.verticalHeader().setVisible(False)\n");
        sb.append("        table.setSelectionBehavior(QTableWidget.SelectRows)\n");
        sb.append("        table.setSelectionMode(QTableWidget.SingleSelection)\n");
        sb.append("        \n");
        sb.append("        # Store references\n");
        sb.append("        self.model_widgets[model_name] = {\n");
        sb.append("            'widget': widget,\n");
        sb.append("            'table': table,\n");
        sb.append("            'config': model_config,\n");
        sb.append("            'search_input': search_input\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        layout.addWidget(table)\n");
        sb.append("        widget.setLayout(layout)\n");
        sb.append("        \n");
        sb.append("        # Add to stack\n");
        sb.append("        self.content_stack.addWidget(widget)\n");
        sb.append("        \n");
        sb.append("        # Load initial data\n");
        sb.append("        self.refresh_table(model_name)\n\n");
        
        sb.append("    def show_add_form(self, model_name, model_config):\n");
        sb.append("        \"\"\"Show form to add new record\"\"\"\n");
        sb.append("        dialog = QDialog(self)\n");
        sb.append("        dialog.setWindowTitle(f\"إضافة {model_config.get('verbose_name', model_name)}\")\n");
        sb.append("        dialog.setModal(True)\n");
        sb.append("        \n");
        sb.append("        layout = QVBoxLayout()\n");
        sb.append("        \n");
        sb.append("        # Create form based on fields\n");
        sb.append("        form_widget = self.create_form_widget(model_config['fields'])\n");
        sb.append("        layout.addWidget(form_widget)\n");
        sb.append("        \n");
        sb.append("        # Buttons\n");
        sb.append("        button_layout = QHBoxLayout()\n");
        sb.append("        \n");
        sb.append("        save_btn = QPushButton(\"حفظ\")\n");
        sb.append("        save_btn.clicked.connect(lambda: self.save_record(model_name, form_widget, dialog))\n");
        sb.append("        button_layout.addWidget(save_btn)\n");
        sb.append("        \n");
        sb.append("        cancel_btn = QPushButton(\"إلغاء\")\n");
        sb.append("        cancel_btn.clicked.connect(dialog.reject)\n");
        sb.append("        button_layout.addWidget(cancel_btn)\n");
        sb.append("        \n");
        sb.append("        layout.addLayout(button_layout)\n");
        sb.append("        dialog.setLayout(layout)\n");
        sb.append("        dialog.exec_()\n\n");
        
        sb.append("    def create_form_widget(self, fields):\n");
        sb.append("        \"\"\"Create form widget with fields arranged in grid\"\"\"\n");
        sb.append("        widget = QWidget()\n");
        sb.append("        \n");
        sb.append("        # Determine number of rows needed (4 fields per row)\n");
        sb.append("        num_fields = len(fields)\n");
        sb.append("        num_rows = (num_fields + 3) // 4  # Ceiling division\n");
        sb.append("        \n");
        sb.append("        grid = QGridLayout()\n");
        sb.append("        grid.setSpacing(15)\n");
        sb.append("        grid.setContentsMargins(20, 20, 20, 20)\n");
        sb.append("        \n");
        sb.append("        row, col = 0, 0\n");
        sb.append("        field_widgets = {}\n");
        sb.append("        \n");
        sb.append("        for field in fields:\n");
        sb.append("            field_name = field['name']\n");
        sb.append("            field_label = field.get('label', field_name)\n");
        sb.append("            field_type = field.get('type', 'CharField')\n");
        sb.append("            \n");
        sb.append("            # Create label\n");
        sb.append("            label = QLabel(field_label + \":\")\n");
        sb.append("            label.setAlignment(Qt.AlignRight | Qt.AlignVCenter)\n");
        sb.append("            grid.addWidget(label, row * 2, col)\n");
        sb.append("            \n");
        sb.append("            # Create input widget based on type\n");
        sb.append("            input_widget = self.create_input_widget(field)\n");
        sb.append("            grid.addWidget(input_widget, row * 2 + 1, col)\n");
        sb.append("            \n");
        sb.append("            field_widgets[field_name] = input_widget\n");
        sb.append("            \n");
        sb.append("            # Move to next column/row\n");
        sb.append("            col += 1\n");
        sb.append("            if col >= 4:\n");
        sb.append("                col = 0\n");
        sb.append("                row += 1\n");
        sb.append("        \n");
        sb.append("        widget.setLayout(grid)\n");
        sb.append("        widget.field_widgets = field_widgets  # Store reference\n");
        sb.append("        return widget\n\n");
        
        sb.append("    def create_input_widget(self, field):\n");
        sb.append("        \"\"\"Create appropriate input widget for field type\"\"\"\n");
        sb.append("        field_type = field.get('type', 'CharField')\n");
        sb.append("        field_name = field['name']\n");
        sb.append("        \n");
        sb.append("        if field_type == 'CharField':\n");
        sb.append("            widget = QLineEdit()\n");
        sb.append("            if 'max_length' in field:\n");
        sb.append("                widget.setMaxLength(field['max_length'])\n");
        sb.append("        elif field_type == 'TextField':\n");
        sb.append("            widget = QTextEdit()\n");
        sb.append("            widget.setMaximumHeight(100)\n");
        sb.append("        elif field_type == 'IntegerField':\n");
        sb.append("            widget = QSpinBox()\n");
        sb.append("            widget.setRange(-999999, 999999)\n");
        sb.append("        elif field_type == 'DecimalField':\n");
        sb.append("            widget = QDoubleSpinBox()\n");
        sb.append("            widget.setRange(-999999.99, 999999.99)\n");
        sb.append("            widget.setDecimals(2)\n");
        sb.append("        elif field_type == 'BooleanField':\n");
        sb.append("            widget = QCheckBox()\n");
        sb.append("        elif field_type == 'DateField':\n");
        sb.append("            widget = QDateEdit()\n");
        sb.append("            widget.setCalendarPopup(True)\n");
        sb.append("            widget.setDate(QDate.currentDate())\n");
        sb.append("        elif field_type == 'DateTimeField':\n");
        sb.append("            widget = QDateTimeEdit()\n");
        sb.append("            widget.setCalendarPopup(True)\n");
        sb.append("            widget.setDateTime(QDateTime.currentDateTime())\n");
        sb.append("        elif field_type == 'EmailField':\n");
        sb.append("            widget = QLineEdit()\n");
        sb.append("        elif field_type == 'ForeignKey':\n");
        sb.append("            widget = QComboBox()\n");
        sb.append("            # Will be populated when shown\n");
        sb.append("        elif field_type == 'OneToOneField':\n");
        sb.append("            widget = QLineEdit()\n");
        sb.append("        else:\n");
        sb.append("            widget = QLineEdit()\n");
        sb.append("        \n");
        sb.append("        # Set placeholder if available\n");
        sb.append("        if 'label' in field and hasattr(widget, 'setPlaceholderText'):\n");
        sb.append("            widget.setPlaceholderText(field['label'])\n");
        sb.append("        \n");
        sb.append("        return widget\n\n");
        
        // ... continue with other methods
        
        sb.append("    def show_dashboard(self):\n");
        sb.append("        \"\"\"Show dashboard widget\"\"\"\n");
        sb.append("        if not hasattr(self, 'dashboard_widget'):\n");
        sb.append("            self.create_dashboard()\n");
        sb.append("        self.content_stack.setCurrentWidget(self.dashboard_widget)\n");
        sb.append("        self.statusBar().showMessage(\"لوحة التحكم\")\n\n");
        
        sb.append("    def create_dashboard(self):\n");
        sb.append("        \"\"\"Create dashboard widget\"\"\"\n");
        sb.append("        self.dashboard_widget = QWidget()\n");
        sb.append("        layout = QVBoxLayout()\n");
        sb.append("        \n");
        sb.append("        # Welcome message\n");
        sb.append("        welcome_label = QLabel(f\"مرحباً بك، {self.username}!\")\n");
        sb.append("        welcome_label.setStyleSheet(\"font-size: 24px; font-weight: bold; padding: 20px;\")\n");
        sb.append("        layout.addWidget(welcome_label)\n");
        sb.append("        \n");
        sb.append("        # Statistics\n");
        sb.append("        stats_widget = self.create_statistics_widget()\n");
        sb.append("        layout.addWidget(stats_widget)\n");
        sb.append("        \n");
        sb.append("        # Quick actions\n");
        sb.append("        quick_actions = self.create_quick_actions()\n");
        sb.append("        layout.addWidget(quick_actions)\n");
        sb.append("        \n");
        sb.append("        layout.addStretch()\n");
        sb.append("        self.dashboard_widget.setLayout(layout)\n");
        sb.append("        self.content_stack.addWidget(self.dashboard_widget)\n\n");
        
        sb.append("    def show_model(self, model_name):\n");
        sb.append("        \"\"\"Show widget for specific model\"\"\"\n");
        sb.append("        if model_name in self.model_widgets:\n");
        sb.append("            widget = self.model_widgets[model_name]['widget']\n");
        sb.append("            self.content_stack.setCurrentWidget(widget)\n");
        sb.append("            self.statusBar().showMessage(f\"عرض {model_name}\")\n");
        sb.append("        else:\n");
        sb.append("            QMessageBox.warning(self, \"تحذير\", f\"النموذج {model_name} غير موجود\")\n\n");
        
        sb.append("    def show_tools(self):\n");
        sb.append("        \"\"\"Show tools widget\"\"\"\n");
        sb.append("        from tools_ui import ToolsUI\n");
        sb.append("        self.tools_ui = ToolsUI(self.db_manager, self)\n");
        sb.append("        self.content_stack.addWidget(self.tools_ui)\n");
        sb.append("        self.content_stack.setCurrentWidget(self.tools_ui)\n");
        sb.append("        self.statusBar().showMessage(\"أدوات إدارة البيانات\")\n\n");
        
        sb.append("    def show_settings(self):\n");
        sb.append("        \"\"\"Show settings widget\"\"\"\n");
        sb.append("        from theme_manager import ThemeManager\n");
        sb.append("        self.theme_manager = ThemeManager(self.config, self)\n");
        sb.append("        self.content_stack.addWidget(self.theme_manager)\n");
        sb.append("        self.content_stack.setCurrentWidget(self.theme_manager)\n");
        sb.append("        self.statusBar().showMessage(\"الإعدادات والمظهر\")\n\n");
        
        sb.append("    def logout(self):\n");
        sb.append("        \"\"\"Logout and close application\"\"\"\n");
        sb.append("        reply = QMessageBox.question(self, 'تسجيل الخروج', \n");
        sb.append("                                   'هل تريد تسجيل الخروج؟',\n");
        sb.append("                                   QMessageBox.Yes | QMessageBox.No)\n");
        sb.append("        \n");
        sb.append("        if reply == QMessageBox.Yes:\n");
        sb.append("            self.db_manager.close()\n");
        sb.append("            self.close()\n");
        
        saveToFile(new File(projectDir, "main_window.py"), sb.toString());
    }
    
    private void generateThemeManagerPy(File projectDir) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("#!/usr/bin/env python3\n");
        sb.append("# -*- coding: utf-8 -*-\n\n");
        sb.append("from PyQt5.QtWidgets import (\n");
        sb.append("    QWidget, QVBoxLayout, QHBoxLayout, QLabel, \n");
        sb.append("    QPushButton, QComboBox, QColorDialog, QGroupBox,\n");
        sb.append("    QGridLayout, QMessageBox, QSpinBox\n");
        sb.append(")\n");
        sb.append("from PyQt5.QtGui import QColor, QPalette\n");
        sb.append("from PyQt5.QtCore import Qt\n");
        sb.append("import json\n\n");
        
        sb.append("class ThemeManager(QWidget):\n");
        sb.append("    def __init__(self, config, parent=None):\n");
        sb.append("        super().__init__(parent)\n");
        sb.append("        self.config = config\n");
        sb.append("        self.parent = parent\n");
        sb.append("        self.init_ui()\n");
        sb.append("        self.load_current_settings()\n\n");
        
        sb.append("    def init_ui(self):\n");
        sb.append("        self.setWindowTitle(\"إدارة المظهر والإعدادات\")\n");
        sb.append("        \n");
        sb.append("        main_layout = QVBoxLayout()\n");
        sb.append("        main_layout.setSpacing(20)\n");
        sb.append("        main_layout.setContentsMargins(30, 30, 30, 30)\n\n");
        
        sb.append("        # Theme Selection\n");
        sb.append("        theme_group = QGroupBox(\"إعدادات المظهر\")\n");
        sb.append("        theme_layout = QGridLayout()\n");
        sb.append("        \n");
        sb.append("        theme_label = QLabel(\"السمة:\")\n");
        sb.append("        self.theme_combo = QComboBox()\n");
        sb.append("        self.theme_combo.addItems([\"فاتح\", \"غامق\", \"مخصص\"])\n");
        sb.append("        self.theme_combo.currentTextChanged.connect(self.on_theme_changed)\n");
        sb.append("        theme_layout.addWidget(theme_label, 0, 0)\n");
        sb.append("        theme_layout.addWidget(self.theme_combo, 0, 1)\n");
        sb.append("        \n");
        sb.append("        # Color selectors (initially hidden)\n");
        sb.append("        self.color_widgets = {}\n");
        sb.append("        colors = [\n");
        sb.append("            (\"لون الخلفية\", \"background_color\", \"#ffffff\"),\n");
        sb.append("            (\"لون النص\", \"text_color\", \"#000000\"),\n");
        sb.append("            (\"لون الأزرار\", \"button_color\", \"#0078d7\"),\n");
        sb.append("            (\"لون التحديد\", \"selection_color\", \"#1e90ff\"),\n");
        sb.append("            (\"لون الحدود\", \"border_color\", \"#cccccc\")\n");
        sb.append("        ]\n");
        sb.append("        \n");
        sb.append("        for i, (label, key, default) in enumerate(colors):\n");
        sb.append("            color_label = QLabel(label + \":\")\n");
        sb.append("            color_btn = QPushButton()\n");
        sb.append("            color_btn.setFixedSize(60, 30)\n");
        sb.append("            color_btn.setStyleSheet(f\"background-color: {default}; border: 1px solid #999;\")\n");
        sb.append("            color_btn.clicked.connect(lambda checked, k=key, b=color_btn: self.choose_color(k, b))\n");
        sb.append("            \n");
        sb.append("            theme_layout.addWidget(color_label, i + 1, 0)\n");
        sb.append("            theme_layout.addWidget(color_btn, i + 1, 1)\n");
        sb.append("            \n");
        sb.append("            self.color_widgets[key] = color_btn\n");
        sb.append("            color_btn.setVisible(False)\n");
        sb.append("        \n");
        sb.append("        theme_group.setLayout(theme_layout)\n");
        sb.append("        main_layout.addWidget(theme_group)\n\n");
        
        sb.append("        # Font Settings\n");
        sb.append("        font_group = QGroupBox(\"إعدادات الخط\")\n");
        sb.append("        font_layout = QGridLayout()\n");
        sb.append("        \n");
        sb.append("        font_size_label = QLabel(\"حجم الخط:\")\n");
        sb.append("        self.font_size_spin = QSpinBox()\n");
        sb.append("        self.font_size_spin.setRange(8, 24)\n");
        sb.append("        self.font_size_spin.setValue(10)\n");
        sb.append("        font_layout.addWidget(font_size_label, 0, 0)\n");
        sb.append("        font_layout.addWidget(self.font_size_spin, 0, 1)\n");
        sb.append("        \n");
        sb.append("        font_family_label = QLabel(\"نوع الخط:\")\n");
        sb.append("        self.font_family_combo = QComboBox()\n");
        sb.append("        self.font_family_combo.addItems([\"Arial\", \"Tahoma\", \"Times New Roman\", \"Segoe UI\"])\n");
        sb.append("        font_layout.addWidget(font_family_label, 1, 0)\n");
        sb.append("        font_layout.addWidget(self.font_family_combo, 1, 1)\n");
        sb.append("        \n");
        sb.append("        font_group.setLayout(font_layout)\n");
        sb.append("        main_layout.addWidget(font_group)\n\n");
        
        sb.append("        # Language Settings\n");
        sb.append("        lang_group = QGroupBox(\"إعدادات اللغة\")\n");
        sb.append("        lang_layout = QGridLayout()\n");
        sb.append("        \n");
        sb.append("        lang_label = QLabel(\"اللغة:\")\n");
        sb.append("        self.lang_combo = QComboBox()\n");
        sb.append("        self.lang_combo.addItems([\"العربية\", \"English\"])\n");
        sb.append("        lang_layout.addWidget(lang_label, 0, 0)\n");
        sb.append("        lang_layout.addWidget(self.lang_combo, 0, 1)\n");
        sb.append("        \n");
        sb.append("        lang_group.setLayout(lang_layout)\n");
        sb.append("        main_layout.addWidget(lang_group)\n\n");
        
        sb.append("        # Buttons\n");
        sb.append("        button_layout = QHBoxLayout()\n");
        sb.append("        \n");
        sb.append("        self.apply_btn = QPushButton(\"تطبيق\")\n");
        sb.append("        self.apply_btn.clicked.connect(self.apply_settings)\n");
        sb.append("        button_layout.addWidget(self.apply_btn)\n");
        sb.append("        \n");
        sb.append("        self.reset_btn = QPushButton(\"إعادة تعيين\")\n");
        sb.append("        self.reset_btn.clicked.connect(self.reset_settings)\n");
        sb.append("        button_layout.addWidget(self.reset_btn)\n");
        sb.append("        \n");
        sb.append("        self.save_btn = QPushButton(\"حفظ\")\n");
        sb.append("        self.save_btn.clicked.connect(self.save_settings)\n");
        sb.append("        button_layout.addWidget(self.save_btn)\n");
        sb.append("        \n");
        sb.append("        main_layout.addLayout(button_layout)\n");
        sb.append("        main_layout.addStretch()\n");
        sb.append("        \n");
        sb.append("        self.setLayout(main_layout)\n\n");
        
        sb.append("    def load_current_settings(self):\n");
        sb.append("        \"\"\"Load current settings from config\"\"\"\n");
        sb.append("        theme = self.config.get('theme', 'light')\n");
        sb.append("        if theme == 'light':\n");
        sb.append("            self.theme_combo.setCurrentText(\"فاتح\")\n");
        sb.append("        elif theme == 'dark':\n");
        sb.append("            self.theme_combo.setCurrentText(\"غامق\")\n");
        sb.append("        else:\n");
        sb.append("            self.theme_combo.setCurrentText(\"مخصص\")\n");
        sb.append("            \n");
        sb.append("        # Load custom colors\n");
        sb.append("        custom_colors = self.config.get('custom_colors', {})\n");
        sb.append("        for key, btn in self.color_widgets.items():\n");
        sb.append("            if key in custom_colors:\n");
        sb.append("                btn.setStyleSheet(f\"background-color: {custom_colors[key]}; border: 1px solid #999;\")\n");
        sb.append("        \n");
        sb.append("        # Load font settings\n");
        sb.append("        font_size = self.config.get('font_size', 10)\n");
        sb.append("        self.font_size_spin.setValue(font_size)\n");
        sb.append("        \n");
        sb.append("        font_family = self.config.get('font_family', 'Arial')\n");
        sb.append("        self.font_family_combo.setCurrentText(font_family)\n");
        sb.append("        \n");
        sb.append("        # Load language\n");
        sb.append("        language = self.config.get('language', 'ar')\n");
        sb.append("        self.lang_combo.setCurrentText(\"العربية\" if language == 'ar' else \"English\")\n\n");
        
        sb.append("    def on_theme_changed(self, theme):\n");
        sb.append("        \"\"\"Handle theme selection change\"\"\"\n");
        sb.append("        show_colors = (theme == \"مخصص\")\n");
        sb.append("        for btn in self.color_widgets.values():\n");
        sb.append("            btn.setVisible(show_colors)\n\n");
        
        sb.append("    def choose_color(self, color_key, button):\n");
        sb.append("        \"\"\"Open color dialog\"\"\"\n");
        sb.append("        current_color = button.styleSheet().split(\"background-color: \")[1].split(\";\")[0]\n");
        sb.append("        color = QColorDialog.getColor(QColor(current_color), self, f\"اختر {color_key}\")\n");
        sb.append("        \n");
        sb.append("        if color.isValid():\n");
        sb.append("            button.setStyleSheet(f\"background-color: {color.name()}; border: 1px solid #999;\")\n\n");
        
        sb.append("    def apply_settings(self):\n");
        sb.append("        \"\"\"Apply settings without saving\"\"\"\n");
        sb.append("        self.update_theme_preview()\n");
        sb.append("        QMessageBox.information(self, \"تم\", \"تم تطبيق الإعدادات بنجاح\")\n\n");
        
        sb.append("    def update_theme_preview(self):\n");
        sb.append("        \"\"\"Update theme preview on parent window\"\"\"\n");
        sb.append("        theme = self.theme_combo.currentText()\n");
        sb.append("        \n");
        sb.append("        if theme == \"فاتح\":\n");
        sb.append("            stylesheet = self.generate_light_stylesheet()\n");
        sb.append("        elif theme == \"غامق\":\n");
        sb.append("            stylesheet = self.generate_dark_stylesheet()\n");
        sb.append("        else:\n");
        sb.append("            stylesheet = self.generate_custom_stylesheet()\n");
        
        sb.append("        if self.parent:\n");
        sb.append("            self.parent.setStyleSheet(stylesheet)\n\n");
        
        sb.append("    def generate_light_stylesheet(self):\n");
        sb.append("        return '''\n");
        sb.append("        QWidget {\n");
        sb.append("            background-color: #f5f5f5;\n");
        sb.append("            color: #333333;\n");
        // تصحيح السطرين القادمين
        sb.append("            font-size: " + font_size + "px;\n");
        sb.append("            font-family: " + font_family + ";\n");
        sb.append("        }\n");
        sb.append("        QPushButton {\n");
        sb.append("            background-color: #0078d7;\n");
        sb.append("            color: white;\n");
        sb.append("            border: none;\n");
        sb.append("            padding: 8px;\n");
        sb.append("            border-radius: 4px;\n");
        sb.append("        }\n");
        sb.append("        QLineEdit, QTextEdit, QComboBox, QSpinBox, QDateEdit {\n");
        sb.append("            border: 1px solid #ddd;\n");
        sb.append("            padding: 5px;\n");
        sb.append("            border-radius: 3px;\n");
        sb.append("        }\n");
        sb.append("        '''\n\n");
        
        sb.append("    def generate_dark_stylesheet(self):\n");
        sb.append("        return '''\n");
        sb.append("        QWidget {\n");
        sb.append("            background-color: #2b2b2b;\n");
        sb.append("            color: #ffffff;\n");
        // تصحيح السطرين القادمين في قسم الـ Dark Mode
        sb.append("            font-size: " + font_size + "px;\n");
        sb.append("            font-family: " + font_family + ";\n");
        sb.append("        }\n");

        sb.append("        QPushButton {\n");
        sb.append("            background-color: #3c3c3c;\n");
        sb.append("            border: 1px solid #555;\n");
        sb.append("            padding: 5px;\n");
        sb.append("            border-radius: 3px;\n");
        sb.append("        }\n");
        sb.append("        QLineEdit, QTextEdit, QComboBox, QSpinBox, QDateEdit {\n");
        sb.append("            background-color: #3c3c3c;\n");
        sb.append("            border: 1px solid #555;\n");
        sb.append("            padding: 3px;\n");
        sb.append("        }\n");
        sb.append("        '''\n\n");
        
        sb.append("    def save_settings(self):\n");
        sb.append("        \"\"\"Save settings to config file\"\"\"\n");
        sb.append("        try:\n");
        sb.append("            # Theme\n");
        sb.append("            theme_map = {\"فاتح\": \"light\", \"غامق\": \"dark\", \"مخصص\": \"custom\"}\n");
        sb.append("            self.config.set('theme', theme_map[self.theme_combo.currentText()])\n");
        sb.append("            \n");
        sb.append("            # Custom colors\n");
        sb.append("            if self.theme_combo.currentText() == \"مخصص\":\n");
        sb.append("                custom_colors = {}\n");
        sb.append("                for key, btn in self.color_widgets.items():\n");
        sb.append("                    color_str = btn.styleSheet().split(\"background-color: \")[1].split(\";\")[0]\n");
        sb.append("                    custom_colors[key] = color_str\n");
        sb.append("                self.config.set('custom_colors', custom_colors)\n");
        sb.append("            \n");
        sb.append("            # Font settings\n");
        sb.append("            self.config.set('font_size', self.font_size_spin.value())\n");
        sb.append("            self.config.set('font_family', self.font_family_combo.currentText())\n");
        sb.append("            \n");
        sb.append("            # Language\n");
        sb.append("            lang_map = {\"العربية\": \"ar\", \"English\": \"en\"}\n");
        sb.append("            self.config.set('language', lang_map[self.lang_combo.currentText()])\n");
        sb.append("            \n");
        sb.append("            # Apply changes\n");
        sb.append("            self.apply_settings()\n");
        sb.append("            \n");
        sb.append("            QMessageBox.information(self, \"تم\", \"تم حفظ الإعدادات بنجاح\")\n");
        sb.append("            \n");
        sb.append("        except Exception as e:\n");
        sb.append("            QMessageBox.critical(self, \"خطأ\", f\"فشل حفظ الإعدادات: {str(e)}\")\n\n");
        
        sb.append("    def reset_settings(self):\n");
        sb.append("        \"\"\"Reset to default settings\"\"\"\n");
        sb.append("        reply = QMessageBox.question(self, \"تأكيد\", \n");
        sb.append("                                   \"هل تريد استعادة الإعدادات الافتراضية؟\",\n");
        sb.append("                                   QMessageBox.Yes | QMessageBox.No)\n");
        sb.append("        \n");
        sb.append("        if reply == QMessageBox.Yes:\n");
        sb.append("            self.config.reset_to_defaults()\n");
        sb.append("            self.load_current_settings()\n");
        sb.append("            self.apply_settings()\n");
        
        saveToFile(new File(projectDir, "theme_manager.py"), sb.toString());
    }
    
    private void generateConfigPy(File projectDir) throws JSONException, IOException {
        JSONObject dbSettings = projectConfig.getJSONObject("database_settings");
        
        StringBuilder sb = new StringBuilder();
        sb.append("#!/usr/bin/env python3\n");
        sb.append("# -*- coding: utf-8 -*-\n\n");
        sb.append("import json\n");
        sb.append("import os\n");
        sb.append("from typing import Any, Dict\n\n");
        
        sb.append("class ConfigManager:\n");
        sb.append("    def __init__(self, config_file='config.json'):\n");
        sb.append("        self.config_file = config_file\n");
        sb.append("        self.config = self.load_config()\n\n");
        
        sb.append("    def load_config(self) -> Dict:\n");
        sb.append("        \"\"\"Load configuration from file\"\"\"\n");
        sb.append("        default_config = {\n");
        sb.append("            'theme': 'light',\n");
        sb.append("            'language': 'ar',\n");
        sb.append("            'font_size': 10,\n");
        sb.append("            'font_family': 'Arial',\n");
        sb.append("            'database': ");
        sb.append(dbSettings.toString(2).replace("\n", "\n                "));
        sb.append("\n");
        sb.append("        }\n");
        sb.append("        \n");
        sb.append("        if os.path.exists(self.config_file):\n");
        sb.append("            try:\n");
        sb.append("                with open(self.config_file, 'r', encoding='utf-8') as f:\n");
        sb.append("                    user_config = json.load(f)\n");
        sb.append("                    # Merge with defaults\n");
        sb.append("                    default_config.update(user_config)\n");
        sb.append("            except Exception as e:\n");
        sb.append("                print(f\"Error loading config: {e}\")\n");
        sb.append("        \n");
        sb.append("        return default_config\n\n");
        
        sb.append("    def save_config(self):\n");
        sb.append("        \"\"\"Save configuration to file\"\"\"\n");
        sb.append("        try:\n");
        sb.append("            with open(self.config_file, 'w', encoding='utf-8') as f:\n");
        sb.append("                json.dump(self.config, f, indent=2, ensure_ascii=False)\n");
        sb.append("        except Exception as e:\n");
        sb.append("            print(f\"Error saving config: {e}\")\n\n");
        
        sb.append("    def get(self, key: str, default: Any = None) -> Any:\n");
        sb.append("        \"\"\"Get configuration value\"\"\"\n");
        sb.append("        return self.config.get(key, default)\n\n");
        
        sb.append("    def set(self, key: str, value: Any):\n");
        sb.append("        \"\"\"Set configuration value\"\"\"\n");
        sb.append("        self.config[key] = value\n");
        sb.append("        self.save_config()\n\n");
        
        sb.append("    def remove(self, key: str):\n");
        sb.append("        \"\"\"Remove configuration key\"\"\"\n");
        sb.append("        if key in self.config:\n");
        sb.append("            del self.config[key]\n");
        sb.append("            self.save_config()\n\n");
        
        sb.append("    def get_database_config(self) -> Dict:\n");
        sb.append("        \"\"\"Get database configuration\"\"\"\n");
        sb.append("        return self.config.get('database', {})\n\n");
        
        sb.append("    def reset_to_defaults(self):\n");
        sb.append("        \"\"\"Reset configuration to defaults\"\"\"\n");
        sb.append("        self.config = self.load_config()  # This loads defaults\n");
        sb.append("        self.save_config()\n");
        
        saveToFile(new File(projectDir, "config.py"), sb.toString());
    }
    
    private void generateToolsUiPy(File projectDir) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("#!/usr/bin/env python3\n");
        sb.append("# -*- coding: utf-8 -*-\n\n");
        sb.append("from PyQt5.QtWidgets import (\n");
        sb.append("    QWidget, QVBoxLayout, QHBoxLayout, QPushButton, \n");
        sb.append("    QLabel, QGroupBox, QGridLayout, QFileDialog,\n");
        sb.append("    QMessageBox, QProgressDialog, QComboBox, QLineEdit,\n");
        sb.append("    QListWidget, QListWidgetItem, QTabWidget\n");
        sb.append(")\n");
        sb.append("from PyQt5.QtCore import Qt, QThread, pyqtSignal\n");
        sb.append("import os\n");
        sb.append("import shutil\n");
        sb.append("import zipfile\n");
        sb.append("from datetime import datetime\n\n");
        
        sb.append("class ToolsUI(QWidget):\n");
        sb.append("    def __init__(self, db_manager, parent=None):\n");
        sb.append("        super().__init__(parent)\n");
        sb.append("        self.db_manager = db_manager\n");
        sb.append("        self.parent = parent\n");
        sb.append("        self.init_ui()\n\n");
        
        sb.append("    def init_ui(self):\n");
        sb.append("        self.setWindowTitle(\"أدوات إدارة البيانات\")\n");
        sb.append("        \n");
        sb.append("        main_layout = QVBoxLayout()\n");
        sb.append("        main_layout.setSpacing(20)\n");
        sb.append("        main_layout.setContentsMargins(30, 30, 30, 30)\n\n");
        
        sb.append("        # Create tab widget\n");
        sb.append("        tabs = QTabWidget()\n\n");
        
        sb.append("        # Backup/Restore Tab\n");
        sb.append("        backup_tab = self.create_backup_tab()\n");
        sb.append("        tabs.addTab(backup_tab, \"النسخ الاحتياطي\")\n\n");
        
        sb.append("        # Import/Export Tab\n");
        sb.append("        import_tab = self.create_import_tab()\n");
        sb.append("        tabs.addTab(import_tab, \"الاستيراد والتصدير\")\n\n");
        
        sb.append("        # Database Tools Tab\n");
        sb.append("        db_tools_tab = self.create_db_tools_tab()\n");
        sb.append("        tabs.addTab(db_tools_tab, \"أدوات قاعدة البيانات\")\n\n");
        
        sb.append("        main_layout.addWidget(tabs)\n");
        sb.append("        self.setLayout(main_layout)\n\n");
        
        sb.append("    def create_backup_tab(self):\n");
        sb.append("        tab = QWidget()\n");
        sb.append("        layout = QVBoxLayout()\n");
        sb.append("        layout.setSpacing(15)\n\n");
        
        sb.append("        # Backup section\n");
        sb.append("        backup_group = QGroupBox(\"إنشاء نسخة احتياطية\")\n");
        sb.append("        backup_layout = QGridLayout()\n");
        sb.append("        \n");
        sb.append("        backup_label = QLabel(\"مسار الحفظ:\")\n");
        sb.append("        self.backup_path_edit = QLineEdit()\n");
        sb.append("        self.backup_path_edit.setText(os.path.expanduser(\"~/backups\"))\n");
        sb.append("        backup_browse_btn = QPushButton(\"استعرض...\")\n");
        sb.append("        backup_browse_btn.clicked.connect(self.browse_backup_path)\n");
        sb.append("        \n");
        sb.append("        backup_layout.addWidget(backup_label, 0, 0)\n");
        sb.append("        backup_layout.addWidget(self.backup_path_edit, 0, 1)\n");
        sb.append("        backup_layout.addWidget(backup_browse_btn, 0, 2)\n");
        sb.append("        \n");
        sb.append("        self.backup_btn = QPushButton(\"إنشاء نسخة احتياطية الآن\")\n");
        sb.append("        self.backup_btn.clicked.connect(self.create_backup)\n");
        sb.append("        self.backup_btn.setStyleSheet(\"padding: 10px; font-weight: bold;\")\n");
        sb.append("        backup_layout.addWidget(self.backup_btn, 1, 0, 1, 3)\n");
        sb.append("        \n");
        sb.append("        backup_group.setLayout(backup_layout)\n");
        sb.append("        layout.addWidget(backup_group)\n\n");
        
        sb.append("        # Restore section\n");
        sb.append("        restore_group = QGroupBox(\"استعادة نسخة احتياطية\")\n");
        sb.append("        restore_layout = QGridLayout()\n");
        sb.append("        \n");
        sb.append("        restore_label = QLabel(\"النسخة الاحتياطية:\")\n");
        sb.append("        self.restore_path_edit = QLineEdit()\n");
        sb.append("        restore_browse_btn = QPushButton(\"استعرض...\")\n");
        sb.append("        restore_browse_btn.clicked.connect(self.browse_restore_file)\n");
        sb.append("        \n");
        sb.append("        restore_layout.addWidget(restore_label, 0, 0)\n");
        sb.append("        restore_layout.addWidget(self.restore_path_edit, 0, 1)\n");
        sb.append("        restore_layout.addWidget(restore_browse_btn, 0, 2)\n");
        sb.append("        \n");
        sb.append("        self.restore_btn = QPushButton(\"استعادة النسخة المحددة\")\n");
        sb.append("        self.restore_btn.clicked.connect(self.restore_backup)\n");
        sb.append("        self.restore_btn.setStyleSheet(\"padding: 10px; font-weight: bold; background-color: #ff9800;\")\n");
        sb.append("        restore_layout.addWidget(self.restore_btn, 1, 0, 1, 3)\n");
        sb.append("        \n");
        sb.append("        # Backup list\n");
        sb.append("        self.backup_list = QListWidget()\n");
        sb.append("        self.load_backup_list()\n");
        sb.append("        restore_layout.addWidget(QLabel(\"النسخ المتاحة:\"), 2, 0)\n");
        sb.append("        restore_layout.addWidget(self.backup_list, 3, 0, 1, 3)\n");
        sb.append("        \n");
        sb.append("        restore_group.setLayout(restore_layout)\n");
        sb.append("        layout.addWidget(restore_group)\n\n");
        
        sb.append("        layout.addStretch()\n");
        sb.append("        tab.setLayout(layout)\n");
        sb.append("        return tab\n\n");
        
        sb.append("    def create_import_tab(self):\n");
        sb.append("        tab = QWidget()\n");
        sb.append("        layout = QVBoxLayout()\n");
        sb.append("        layout.setSpacing(15)\n\n");
        
        sb.append("        # Export section\n");
        sb.append("        export_group = QGroupBox(\"تصدير البيانات إلى Excel\")\n");
        sb.append("        export_layout = QGridLayout()\n");
        sb.append("        \n");
        sb.append("        table_label = QLabel(\"الجدول:\")\n");
        sb.append("        self.table_combo = QComboBox()\n");
        sb.append("        tables = self.db_manager.get_table_list()\n");
        sb.append("        self.table_combo.addItems(tables)\n");
        sb.append("        \n");
        sb.append("        export_layout.addWidget(table_label, 0, 0)\n");
        sb.append("        export_layout.addWidget(self.table_combo, 0, 1)\n");
        sb.append("        \n");
        sb.append("        export_path_label = QLabel(\"مسار الحفظ:\")\n");
        sb.append("        self.export_path_edit = QLineEdit()\n");
        sb.append("        export_browse_btn = QPushButton(\"استعرض...\")\n");
        sb.append("        export_browse_btn.clicked.connect(self.browse_export_path)\n");
        sb.append("        \n");
        sb.append("        export_layout.addWidget(export_path_label, 1, 0)\n");
        sb.append("        export_layout.addWidget(self.export_path_edit, 1, 1)\n");
        sb.append("        export_layout.addWidget(export_browse_btn, 1, 2)\n");
        sb.append("        \n");
        sb.append("        self.export_btn = QPushButton(\"تصدير إلى Excel\")\n");
        sb.append("        self.export_btn.clicked.connect(self.export_table)\n");
        sb.append("        self.export_btn.setStyleSheet(\"padding: 10px; font-weight: bold; background-color: #4caf50;\")\n");
        sb.append("        export_layout.addWidget(self.export_btn, 2, 0, 1, 3)\n");
        sb.append("        \n");
        sb.append("        export_group.setLayout(export_layout)\n");
        sb.append("        layout.addWidget(export_group)\n\n");
        
        sb.append("        # Import section\n");
        sb.append("        import_group = QGroupBox(\"استيراد البيانات من Excel\")\n");
        sb.append("        import_layout = QGridLayout()\n");
        sb.append("        \n");
        sb.append("        import_table_label = QLabel(\"الجدول الهدف:\")\n");
        sb.append("        self.import_table_combo = QComboBox()\n");
        sb.append("        self.import_table_combo.addItems(tables)\n");
        sb.append("        \n");
        sb.append("        import_layout.addWidget(import_table_label, 0, 0)\n");
        sb.append("        import_layout.addWidget(self.import_table_combo, 0, 1)\n");
        sb.append("        \n");
        sb.append("        import_file_label = QLabel(\"ملف Excel:\")\n");
        sb.append("        self.import_file_edit = QLineEdit()\n");
        sb.append("        import_file_browse_btn = QPushButton(\"استعرض...\")\n");
        sb.append("        import_file_browse_btn.clicked.connect(self.browse_import_file)\n");
        sb.append("        \n");
        sb.append("        import_layout.addWidget(import_file_label, 1, 0)\n");
        sb.append("        import_layout.addWidget(self.import_file_edit, 1, 1)\n");
        sb.append("        import_layout.addWidget(import_file_browse_btn, 1, 2)\n");
        sb.append("        \n");
        sb.append("        self.import_btn = QPushButton(\"استيراد البيانات\")\n");
        sb.append("        self.import_btn.clicked.connect(self.import_excel)\n");
        sb.append("        self.import_btn.setStyleSheet(\"padding: 10px; font-weight: bold; background-color: #2196f3;\")\n");
        sb.append("        import_layout.addWidget(self.import_btn, 2, 0, 1, 3)\n");
        sb.append("        \n");
        sb.append("        import_group.setLayout(import_layout)\n");
        sb.append("        layout.addWidget(import_group)\n\n");
        
        sb.append("        layout.addStretch()\n");
        sb.append("        tab.setLayout(layout)\n");
        sb.append("        return tab\n\n");
        
        sb.append("    def create_backup(self):\n");
        sb.append("        \"\"\"Create database backup\"\"\"\n");
        sb.append("        backup_path = self.backup_path_edit.text().strip()\n");
        sb.append("        \n");
        sb.append("        if not backup_path:\n");
        sb.append("            QMessageBox.warning(self, \"تحذير\", \"يرجى تحديد مسار لحفظ النسخة الاحتياطية\")\n");
        sb.append("            return\n");
        sb.append("        \n");
        sb.append("        # Create directory if it doesn't exist\n");
        sb.append("        os.makedirs(backup_path, exist_ok=True)\n");
        sb.append("        \n");
        sb.append("        # Show progress dialog\n");
        sb.append("        progress = QProgressDialog(\"جاري إنشاء النسخة الاحتياطية...\", \"إلغاء\", 0, 0, self)\n");
        sb.append("        progress.setWindowTitle(\"النسخ الاحتياطي\")\n");
        sb.append("        progress.setModal(True)\n");
        sb.append("        progress.show()\n");
        sb.append("        \n");
        sb.append("        try:\n");
        sb.append("            success = self.db_manager.backup_database(backup_path)\n");
        sb.append("            progress.close()\n");
        sb.append("            \n");
        sb.append("            if success:\n");
        sb.append("                QMessageBox.information(self, \"نجاح\", \"تم إنشاء النسخة الاحتياطية بنجاح\")\n");
        sb.append("                self.load_backup_list()\n");
        sb.append("            else:\n");
        sb.append("                QMessageBox.critical(self, \"خطأ\", \"فشل إنشاء النسخة الاحتياطية\")\n");
        sb.append("                \n");
        sb.append("        except Exception as e:\n");
        sb.append("            progress.close()\n");
        sb.append("            QMessageBox.critical(self, \"خطأ\", f\"حدث خطأ: {str(e)}\")\n\n");
        
        // ... continue with other methods
        
        saveToFile(new File(projectDir, "tools_ui.py"), sb.toString());
    }
    
    private void generateModels(File projectDir) throws JSONException, IOException {
        JSONObject modelsConfig = projectConfig.getJSONObject("models_config");
        Iterator<String> modelKeys = modelsConfig.keys();
        
        while (modelKeys.hasNext()) {
            String modelKey = modelKeys.next();
            JSONObject modelConfig = modelsConfig.getJSONObject(modelKey);
            generateModelFile(projectDir, modelKey, modelConfig);
        }
    }
    
    private void generateModelFile(File projectDir, String modelName, JSONObject modelConfig) throws IOException {
        String fileName = modelName.replace(".", "_") + "_ui.py";
        File modelFile = new File(projectDir, fileName);
        
        StringBuilder sb = new StringBuilder();
        sb.append("#!/usr/bin/env python3\n");
        sb.append("# -*- coding: utf-8 -*-\n\n");
        sb.append("# Auto-generated UI for model: ").append(modelName).append("\n");
        sb.append("# This file was automatically generated by PyQtProjectGenerator\n\n");
        
        // Add more model-specific UI code here
        
        saveToFile(modelFile, sb.toString());
    }
    
    private void generateUiFiles(File projectDir) throws IOException {
        // Create UI directory
        File uiDir = new File(projectDir, "ui");
        if (!uiDir.exists()) {
            uiDir.mkdirs();
        }
        
        // Generate UI files as needed
        // This would contain .ui files if using Qt Designer
    }
    
    private void saveToFile(File file, String content) throws IOException {
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        
        Log.i("PyQtGenerator", "File saved: " + file.getAbsolutePath());
    }
}