package ayman.djangogenerator;

import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.content.*;
import android.content.Intent;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.net.Uri;
import android.os.*;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import aym.lib.*;
import java.io.*;
import java.io.InputStream;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

public class GenerateActivity extends AppCompatActivity {
	
	private String jsonConfig = "";
	
	private LinearLayout linear8;
	private TextView textview13;
	private TextView textview14;
	private EditText json_input;
	private Button generate_button;
	private Button button1;
	private TextView status_text;
	
	private Intent m = new Intent();
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.generate);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		linear8 = findViewById(R.id.linear8);
		textview13 = findViewById(R.id.textview13);
		textview14 = findViewById(R.id.textview14);
		json_input = findViewById(R.id.json_input);
		generate_button = findViewById(R.id.generate_button);
		button1 = findViewById(R.id.button1);
		status_text = findViewById(R.id.status_text);
		
		generate_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				String jsonString = json_input.getText().toString();
				
				if (jsonString.trim().isEmpty()) {
					Toast.makeText(getApplicationContext(), "الرجاء إدخال بيانات JSON", Toast.LENGTH_SHORT).show();
					return;
				}
				
				new Thread(() -> {
					try {
						DjangoProjectGenerator generator = new DjangoProjectGenerator(getApplicationContext(), jsonString);
						File zipFile = generator.generateProject();
						
						runOnUiThread(() -> {
							status_text.setText("تم إنشاء المشروع بنجاح!\n" + zipFile.getAbsolutePath());
							Toast.makeText(getApplicationContext(), "تم الإنشاء بنجاح!", Toast.LENGTH_LONG).show();
							
							// يمكنك هنا مشاركة الملف المضغوط
							//shareFile(zipFile);
						});
						
					} catch (Exception e) {
						runOnUiThread(() -> {
							status_text.setText("خطأ: " + e.getMessage());
							Toast.makeText(getApplicationContext(), "حدث خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show();
						});
						e.printStackTrace();
					}
				}).start();
			}
		});
		
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				// في Activity الخاصة بك
				try {
					// قراءة ملف JSON
					String jsonConfig = json_input.getText().toString();
					
					// إنشاء المولد
					PyQtProjectGenerator generator = new PyQtProjectGenerator(getApplicationContext(), jsonConfig);
					
					// توليد المشروع
					boolean success = generator.generateProject();
					
					if (success) {
						Toast.makeText(getApplicationContext(), "تم توليد المشروع بنجاح", Toast.LENGTH_LONG).show();
					}
				} catch (Exception e) {
					Log.e("pyqt5 generation", "Error generating project", e);
				}
				/*


new Thread(() -> {
            try {
                AdvancedDjangoGenerator generator = new AdvancedDjangoGenerator(this, jsonConfig);
                File zipFile = generator.generateProject();

                // العودة لواجهة المستخدم لإظهار النتيجة
                runOnUiThread(() -> {
                    Toast.makeText(this, "تم إنشاء المشروع بنجاح: " + zipFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "حدث خطأ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();


*/
			}
		});
	}
	
	private void initializeLogic() {
		if (getIntent().hasExtra("data")) {
			json_input.setText(getIntent().getStringExtra("data"));
		}
	}
	
	public void _mor() {
		/*


package com.example.djangogenerator;

import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import java.io.File;

public class MainActivity extends AppCompatActivity {
    
    private EditText jsonInput;
    private Button generateButton;
    private TextView statusText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        jsonInput = findViewById(R.id.json_input);
        generateButton = findViewById(R.id.generate_button);
        statusText = findViewById(R.id.status_text);
        
        generateButton.setOnClickListener(v -> generateDjangoProject());
        
        // تعبئة نموذج JSON مبدئي
        loadSampleJson();
    }
    
    private void loadSampleJson() {
        String sampleJson = "{\n" +
                "  \"project_name\": \"my_blog\",\n" +
                "  \"apps\": [\n" +
                "    {\n" +
                "      \"name\": \"blog\",\n" +
                "      \"models\": [\n" +
                "        {\n" +
                "          \"name\": \"Post\",\n" +
                "          \"fields\": [\n" +
                "            {\n" +
                "              \"name\": \"title\",\n" +
                "              \"type\": \"CharField\",\n" +
                "              \"max_length\": 200\n" +
                "            },\n" +
                "            {\n" +
                "              \"name\": \"content\",\n" +
                "              \"type\": \"TextField\"\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ],\n" +
                "      \"views\": [\n" +
                "        {\n" +
                "          \"name\": \"PostListView\",\n" +
                "          \"type\": \"ListView\",\n" +
                "          \"model\": \"Post\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"urls\": [\n" +
                "        {\n" +
                "          \"path\": \"posts/\",\n" +
                "          \"view\": \"PostListView\",\n" +
                "          \"name\": \"post_list\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"settings\": {\n" +
                "    \"installed_apps\": [\n" +
                "      \"django.contrib.admin\",\n" +
                "      \"django.contrib.auth\"\n" +
                "    ],\n" +
                "    \"database\": {\n" +
                "      \"engine\": \"django.db.backends.sqlite3\",\n" +
                "      \"name\": \"db.sqlite3\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        
        jsonInput.setText(sampleJson);
    }
    
    private void generateDjangoProject() {
        String jsonString = jsonInput.getText().toString();
        
        if (jsonString.trim().isEmpty()) {
            Toast.makeText(this, "الرجاء إدخال بيانات JSON", Toast.LENGTH_SHORT).show();
            return;
        }
        
        new Thread(() -> {
            try {
                DjangoProjectGenerator generator = new DjangoProjectGenerator(this, jsonString);
                File zipFile = generator.generateProject();
                
                runOnUiThread(() -> {
                    statusText.setText("تم إنشاء المشروع بنجاح!\n" + zipFile.getAbsolutePath());
                    Toast.makeText(this, "تم الإنشاء بنجاح!", Toast.LENGTH_LONG).show();
                    
                    // يمكنك هنا مشاركة الملف المضغوط
                    shareFile(zipFile);
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    statusText.setText("خطأ: " + e.getMessage());
                    Toast.makeText(this, "حدث خطأ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    private void shareFile(File file) {
        // تنفيذ مشاركة الملف
        // يمكن استخدام FileProvider لمشاركة الملف مع التطبيقات الأخرى
    }
}


*/
	}
	
	
	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}
	
	@Deprecated
	public int getLocationX(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}
	
	@Deprecated
	public int getLocationY(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[1];
	}
	
	@Deprecated
	public int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}
	
	@Deprecated
	public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
		ArrayList<Double> _result = new ArrayList<Double>();
		SparseBooleanArray _arr = _list.getCheckedItemPositions();
		for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
			if (_arr.valueAt(_iIdx))
			_result.add((double)_arr.keyAt(_iIdx));
		}
		return _result;
	}
	
	@Deprecated
	public float getDip(int _input) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}
	
	@Deprecated
	public int getDisplayWidthPixels() {
		return getResources().getDisplayMetrics().widthPixels;
	}
	
	@Deprecated
	public int getDisplayHeightPixels() {
		return getResources().getDisplayMetrics().heightPixels;
	}
}