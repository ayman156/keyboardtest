package ayman.djangogenerator;

import android.Manifest;
import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.*;
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
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
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
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import android.content.Intent;
import android.content.Context;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.widget.*;
import org.json.JSONArray;

public class FromsetActivity extends AppCompatActivity {
	
	private String projectname = "";
	
	private LinearLayout linear1;
	private TextView textview13;
	private TextView textview8;
	private Spinner parent_model_spinner;
	private TextView textview9;
	private Spinner child_model_spinner;
	private TextView textview10;
	private EditText relationship_name;
	private LinearLayout linear3;
	private TextView textview12;
	private EditText prefix;
	private LinearLayout linear2;
	private Button save_btn;
	private ListView formsetsListView;
	private TextView textview7;
	private EditText extra_fields;
	private TextView textview11;
	private CheckBox can_delete;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.fromset);
		initialize(_savedInstanceState);
		
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
		|| ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
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
		textview13 = findViewById(R.id.textview13);
		textview8 = findViewById(R.id.textview8);
		parent_model_spinner = findViewById(R.id.parent_model_spinner);
		textview9 = findViewById(R.id.textview9);
		child_model_spinner = findViewById(R.id.child_model_spinner);
		textview10 = findViewById(R.id.textview10);
		relationship_name = findViewById(R.id.relationship_name);
		linear3 = findViewById(R.id.linear3);
		textview12 = findViewById(R.id.textview12);
		prefix = findViewById(R.id.prefix);
		linear2 = findViewById(R.id.linear2);
		save_btn = findViewById(R.id.save_btn);
		formsetsListView = findViewById(R.id.formsetsListView);
		textview7 = findViewById(R.id.textview7);
		extra_fields = findViewById(R.id.extra_fields);
		textview11 = findViewById(R.id.textview11);
		can_delete = findViewById(R.id.can_delete);
	}
	
	private void initializeLogic() {
		dbHelper = new DatabaseHelper(this);
		projectId = getIntent().getLongExtra("PROJECT_ID", -1);
		
		loadModels();
		loadFormsets();
		// زر الحفظ
		Button saveBtn = findViewById(R.id.save_btn);
		saveBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveFormset();
			}
		});
		if (getIntent().hasExtra("PROJECT_NAME")) {
			projectname = "/django_projects/".concat(getIntent().getStringExtra("PROJECT_NAME"));
			textview13.setText(getIntent().getStringExtra("PROJECT_NAME"));
		} else {
			
		}
	}
	
	private List<FormsetConfig> formsetsList;
	
	
	private DatabaseHelper dbHelper;
	private long projectId;
	
	
	private void loadModels() {
		// جلب كل النماذج في المشروع
		List<ModelObj> models = dbHelper.getModelsObjectsByProject(projectId);
		
		// Adapter مخصص لعرض الاسم مع ID
		ArrayAdapter<ModelObj> adapter = new ArrayAdapter<ModelObj>(
		this, android.R.layout.simple_spinner_item, models) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView textView = (TextView) view;
				ModelObj model = getItem(position);
				textView.setText(model.name + " (ID: " + model.id + ")");
				return view;
			}
			
			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				View view = super.getDropDownView(position, convertView, parent);
				TextView textView = (TextView) view;
				ModelObj model = getItem(position);
				textView.setText(model.name + " (ID: " + model.id + ")");
				return view;
			}
		};
		
		parent_model_spinner.setAdapter(adapter);
		child_model_spinner.setAdapter(adapter);
	}
	
	private void saveFormset() {
		ModelObj parentModel = (ModelObj) parent_model_spinner.getSelectedItem();
		ModelObj childModel = (ModelObj) child_model_spinner.getSelectedItem();
		
		if (parentModel == null || childModel == null) {
			Toast.makeText(this, "Please select both models", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (parentModel.id == childModel.id) {
			Toast.makeText(this, "Parent and child cannot be the same model", 
			Toast.LENGTH_SHORT).show();
			return;
		}
		
		FormsetConfig formset = new FormsetConfig();
		formset.setParentModelId(parentModel.id);
		formset.setChildModelId(childModel.id);
		
		// إذا لم يدخل اسم العلاقة، نستخدم اسم النموذج الابن
		String relName = relationship_name.getText().toString().trim();
		if (relName.isEmpty()) {
			relName = childModel.name.toLowerCase() + "_set";
		}
		formset.setRelationshipName(relName);
		
		// Extra fields
		try {
			formset.setExtraFields(Integer.parseInt(extra_fields.getText().toString()));
		} catch (NumberFormatException e) {
			formset.setExtraFields(1);
		}
		
		formset.setCanDelete(can_delete.isChecked());
		
		// Prefix
		String prefixx = prefix.getText().toString().trim();
		if (prefixx.isEmpty()) {
			prefixx = childModel.name.toLowerCase();
		}
		formset.setPrefix(prefixx);
		
		long id = dbHelper.addFormset(formset);
		
		if (id > 0) {
			Toast.makeText(this, "Formset saved successfully!", Toast.LENGTH_SHORT).show();
			loadFormsets();
			// عرض خيارات التوليد
			// showGenerationOptions(parentModel, childModel);
		} else {
			Toast.makeText(this, "Error saving formset", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void showGenerationOptions(ModelObj parentModel, ModelObj childModel) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Generate Django Code");
		builder.setMessage("Generate for: " + parentModel.name + " → " + childModel.name);
		
		builder.setPositiveButton("Full Package", (dialog, which) -> {
			try {
				generateFullCode();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		});
		
		builder.setNegativeButton("Views Only", (dialog, which) -> {
			try {
				generateViewsOnly();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		});
		
		builder.setNeutralButton("Just Save", null);
		builder.show();
	}
	
	private void generateFullCode() throws JSONException {
		JSONObject projectJson = dbHelper.generateProjectJSON(projectId);
		
		String viewsCode = DjangoFormsetGenerator.generateViewsCode(projectJson);
		String formsCode = DjangoFormsetGenerator.generateFormsCode(projectJson);
		String templatesCode = DjangoFormsetGenerator.generateTemplateCode(projectJson);
		String urlsCode = DjangoFormsetGenerator.generateUrlsCode(projectJson);
		
		// حفظ الأكواد في ملفات منفصلة
		saveCodeToFiles(viewsCode, formsCode, templatesCode, urlsCode);
		
		// عرض نافذة النجاح
		Toast.makeText(this, "Code generated successfully!", Toast.LENGTH_LONG).show();
		// finish(); // العودة للشاشة السابقة
	}
	
	private void generateViewsOnly() throws JSONException {
		JSONObject projectJson = dbHelper.generateProjectJSON(projectId);
		String viewsCode = DjangoFormsetGenerator.generateViewsCode(projectJson);
		
		// عرض الكود
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Generated Views Code");
		builder.setMessage(viewsCode.substring(0, Math.min(viewsCode.length(), 2000)));
		builder.setPositiveButton("OK", null);
		builder.show();
	}
	
	private void saveCodeToFiles(String viewsCode, String formsCode, 
	String templatesCode, String urlsCode) {
		try {
			//((ClipboardManager) getSystemService(getApplicationContext().CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", viewsCode));
			//حفظ الى المشروع
			
			
			
			
			
			FileUtil.makeDir(FileUtil.getExternalStorageDir().concat(projectname.concat("/form_set/")));
			FileUtil.writeFile(FileUtil.getExternalStorageDir().concat(projectname.concat("/form_set/view.py")), viewsCode);
			FileUtil.writeFile(FileUtil.getExternalStorageDir().concat(projectname.concat("/form_set/forms_code.py")), formsCode);
			FileUtil.writeFile(FileUtil.getExternalStorageDir().concat(projectname.concat("/form_set/templates_code.html")), templatesCode);
			FileUtil.writeFile(FileUtil.getExternalStorageDir().concat(projectname.concat("/form_set/forms_url.py")), urlsCode);
			Toast.makeText(this, "Code generated (file saving not implemented yet)", 
			Toast.LENGTH_SHORT).show();
			
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Error: " + e.getMessage(), 
			Toast.LENGTH_LONG).show();
		}
	}
	
	//عرض المحفوظ
	
	private void loadFormsets() {
		// 1. جلب كل الـ Formsets الخاصة بالمشروع مباشرة (أداء أسرع بكثير)
		formsetsList = dbHelper.getAllFormsetsForProject(projectId);
		
		// 2. تجهيز القائمة للعرض
		List<String> displayItems = new ArrayList<>();
		
		if (formsetsList == null || formsetsList.isEmpty()) {
			displayItems.add("No formsets created yet");
		} else {
			for (FormsetConfig formset : formsetsList) {
				// جلب أسماء الموديلات لعرضها
				String parentName = dbHelper.getModelNameById(formset.getParentModelId());
				String childName = dbHelper.getModelNameById(formset.getChildModelId());
				
				String displayName = (parentName != null ? parentName : "Unknown") 
				+ " → " 
				+ (childName != null ? childName : "Unknown");
				displayItems.add(displayName);
			}
		}
		
		// 3. تحديث الـ ListView
		ArrayAdapter<String> adapter = new ArrayAdapter<>(
		this, android.R.layout.simple_list_item_1, displayItems);
		formsetsListView.setAdapter(adapter);
		
		// 4. معالجة النقر
		formsetsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// نتأكد أننا لم نضغط على رسالة "No formsets"
				if (formsetsList != null && !formsetsList.isEmpty() && position < formsetsList.size()) {
					FormsetConfig formset = formsetsList.get(position);
					showFormsetOptions(formset);
				}
			}
		});
	}
	private void showFormsetOptions(FormsetConfig formset) {
		String[] options = {
			"Generate Code",
			"Delete Formset"
		};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Formset Options");
		builder.setItems(options, (dialog, which) -> {
			switch (which) {
				case 0: // Generate Code
				generateFormsetCode(formset);
				break;
				case 1: // Delete
				deleteFormset(formset);
				break;
			}
		});
		builder.show();
	}
	
	private void generateFormsetCode(FormsetConfig formset) {
		try {
			JSONObject projectJson = dbHelper.generateProjectJSON(projectId);
			String viewsCode = DjangoFormsetGenerator.generateViewsCode(projectJson);
			
			// عرض الكود
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Generated Views Code");
			builder.setMessage(viewsCode);
			
			builder.setPositiveButton("OK", null);
			builder.show();
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void deleteFormset(FormsetConfig formset) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Delete Formset");
		builder.setMessage("Are you sure you want to delete this formset?");
		
		builder.setPositiveButton("Delete", (dialog, which) -> {
			boolean deleted = dbHelper.deleteFormset(formset.getId());
			if (deleted) {
				Toast.makeText(this, "Formset deleted", Toast.LENGTH_SHORT).show();
				loadFormsets(); // تحديث القائمة
			} else {
				Toast.makeText(this, "Error deleting formset", Toast.LENGTH_SHORT).show();
			}
		});
		
		builder.setNegativeButton("Cancel", null);
		builder.show();
	}
	
	{
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