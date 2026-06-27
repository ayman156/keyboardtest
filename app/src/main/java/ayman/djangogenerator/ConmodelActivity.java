package ayman.djangogenerator;

import android.Manifest;
import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.content.*;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
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
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import aym.lib.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

public class ConmodelActivity extends AppCompatActivity {
	
	public final int REQ_CD_F = 101;
	
	private String pythonContent = "";
	private String jsonResponse = "";
	private double appId = 0;
	
	private LinearLayout linear1;
	private ScrollView vscroll1;
	private LinearLayout linear3;
	private LinearLayout linear4;
	private TextView text_app_name;
	private TextView text_appid;
	private TextView textview2;
	private Button button3;
	private Button button1;
	private Button button2;
	private LinearLayout linear2;
	private EditText edittext1;
	private EditText edittext2;
	
	private DatabaseHelper dbHelper;
	private Intent f = new Intent(Intent.ACTION_GET_CONTENT);
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.conmodel);
		initialize(_savedInstanceState);
		
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
		} else {
			initializeLogic();
		}
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1000) {
			initializeLogic();
		}
	}
	
	private void initialize(Bundle _savedInstanceState) {
		linear1 = findViewById(R.id.linear1);
		vscroll1 = findViewById(R.id.vscroll1);
		linear3 = findViewById(R.id.linear3);
		linear4 = findViewById(R.id.linear4);
		text_app_name = findViewById(R.id.text_app_name);
		text_appid = findViewById(R.id.text_appid);
		textview2 = findViewById(R.id.textview2);
		button3 = findViewById(R.id.button3);
		button1 = findViewById(R.id.button1);
		button2 = findViewById(R.id.button2);
		linear2 = findViewById(R.id.linear2);
		edittext1 = findViewById(R.id.edittext1);
		edittext2 = findViewById(R.id.edittext2);
		dbHelper = new DatabaseHelper(this);
		f.setType("*/*");
		f.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
		
		button3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				startActivityForResult(f, REQ_CD_F);
			}
		});
		
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				pythonContent = edittext1.getText().toString();
				new Thread(() -> {
					String jsonResult = ModelParser.parsePythonModelToJson(pythonContent);
					
					// العودة للواجهة الرئيسية لتحديث النص
					runOnUiThread(() -> {
						if (jsonResult != null) {
							edittext2.setText(jsonResult);
						}
					});
				}).start();
				
			}
		});
		
		button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				if (text_appid.getText().toString().equals("00") || edittext2.getText().toString().equals("")) {
					SketchwareUtil.showMessage(getApplicationContext(), "البيانات غير صحيحه");
				} else {
					_addrul(edittext2.getText().toString(), Double.parseDouble(text_appid.getText().toString()));
				}
			}
		});
	}
	
	private void initializeLogic() {
		if (getIntent().hasExtra("APP_ID")) {
			// الحصول على الرقم (الرقم 0 هو القيمة الافتراضية في حال لم يجد المفتاح)
			int appId = getIntent().getIntExtra("APP_ID", 0); 
			text_appid.setText(String.valueOf(appId));
		}
		
		if (getIntent().hasExtra("APP_NAME")) {
			String targetAppName = getIntent().getStringExtra("APP_NAME");
			int app_id = dbHelper.get_appid(targetAppName);
			text_appid.setText(String.valueOf((long)(app_id)));
			text_app_name.setText("اسم التطبيق : ".concat(getIntent().getStringExtra("APP_NAME")));
		}
	}
	
	@Override
	protected void onActivityResult(int _requestCode, int _resultCode, Intent _data) {
		super.onActivityResult(_requestCode, _resultCode, _data);
		
		switch (_requestCode) {
			case REQ_CD_F:
			if (_resultCode == Activity.RESULT_OK) {
				ArrayList<String> _filePath = new ArrayList<>();
				if (_data != null) {
					if (_data.getClipData() != null) {
						for (int _index = 0; _index < _data.getClipData().getItemCount(); _index++) {
							ClipData.Item _item = _data.getClipData().getItemAt(_index);
							_filePath.add(FileUtil.convertUriToFilePath(getApplicationContext(), _item.getUri()));
						}
					}
					else {
						_filePath.add(FileUtil.convertUriToFilePath(getApplicationContext(), _data.getData()));
					}
				}
				if (_filePath.get((int)(0)).contains("models.py")) {
					edittext1.setText(FileUtil.readFile(_filePath.get((int)(0))));
				} else {
					SketchwareUtil.showMessage(getApplicationContext(), "ملف غير صالح استورد models.py");
				}
			}
			else {
				
			}
			break;
			default:
			break;
		}
	}
	
	public void _mor() {
		
	}
	
	
	public void _addrul(final String _jsoo, final double _idapp) {
		appId = _idapp;
		jsonResponse = _jsoo;
		//String jsonResponse
		DatabaseHelper dbHelper = new DatabaseHelper(this);
		
		try {
			JSONObject root = new JSONObject(jsonResponse);
			JSONArray modelsArray = root.getJSONArray("models");
			
			for (int i = 0; i < modelsArray.length(); i++) {
				JSONObject modelJson = modelsArray.getJSONObject(i);
				
				// 1. إنشاء كائن DjangoModel وتعبئته
				DjangoModel djangoModel = new DjangoModel();
				djangoModel.setAppId((int)appId);
				djangoModel.setName(modelJson.getString("name"));
				// تخزين الخيارات كنص JSON
				djangoModel.setOptions(modelJson.getJSONObject("model_options"));
				
				// 2. إضافة الموديل لقاعدة البيانات والحصول على الـ ID الخاص به
				long modelId = dbHelper.addModel(djangoModel);
				
				// 3. معالجة الحقول التابعة لهذا الموديل
				JSONArray fieldsArray = modelJson.getJSONArray("fields");
				for (int j = 0; j < fieldsArray.length(); j++) {
					JSONObject fieldJson = fieldsArray.getJSONObject(j);
					
					Field field = new Field();
					field.setModelId((int) modelId); // ربط الحقل بالموديل الذي أُضيف للتو
					field.setName(fieldJson.getString("name"));
					field.setType(fieldJson.getString("type"));
					field.setOptions(fieldJson.getJSONObject("field_options"));
					field.setOrder(j); // ترتيب الحقل بناءً على موقعه في المصفوفة
					
					// 4. إضافة الحقل لقاعدة البيانات
					dbHelper.addField(field);
				}
			}
			
			Toast.makeText(this, "تم حفظ الموديلات والحقول بنجاح", Toast.LENGTH_SHORT).show();
			
		} catch (JSONException e) {
			Log.e("DB_ERROR", "خطأ في تحليل البيانات: " + e.getMessage());
			Toast.makeText(this, "فشل في حفظ البيانات", Toast.LENGTH_SHORT).show();
		}
		
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