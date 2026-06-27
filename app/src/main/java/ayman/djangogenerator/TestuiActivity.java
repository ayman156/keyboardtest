package ayman.djangogenerator;

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
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.*;


public class TestuiActivity extends AppCompatActivity {
	
	private LinearLayout linear5;
	private TextView textview9;
	private TextView textview10;
	private EditText editSiteTitle;
	private Spinner spinnerTheme;
	private TextView textview11;
	private CheckBox checkHideSidebar;
	private CheckBox checkTopMenu;
	private CheckBox checkCompactSidebar;
	private CheckBox checkShowIcons;
	private TextView textview12;
	private EditText editLogoText;
	private EditText editLogoImage;
	private TextView textview13;
	private LinearLayout linear6;
	private LinearLayout linear7;
	private TextView textview16;
	private CheckBox checkShowUiTweaks;
	private CheckBox checkCustomCss;
	private EditText editCustomCss;
	private LinearLayout linear8;
	private TextView txtResult;
	private Button btnCopy;
	private TextView textview14;
	private Button btnPickSidebarColor;
	private TextView textview15;
	private Button btnPickBgColor;
	private Button btnReset;
	private Button btnGenerate;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.testui);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		linear5 = findViewById(R.id.linear5);
		textview9 = findViewById(R.id.textview9);
		textview10 = findViewById(R.id.textview10);
		editSiteTitle = findViewById(R.id.editSiteTitle);
		spinnerTheme = findViewById(R.id.spinnerTheme);
		textview11 = findViewById(R.id.textview11);
		checkHideSidebar = findViewById(R.id.checkHideSidebar);
		checkTopMenu = findViewById(R.id.checkTopMenu);
		checkCompactSidebar = findViewById(R.id.checkCompactSidebar);
		checkShowIcons = findViewById(R.id.checkShowIcons);
		textview12 = findViewById(R.id.textview12);
		editLogoText = findViewById(R.id.editLogoText);
		editLogoImage = findViewById(R.id.editLogoImage);
		textview13 = findViewById(R.id.textview13);
		linear6 = findViewById(R.id.linear6);
		linear7 = findViewById(R.id.linear7);
		textview16 = findViewById(R.id.textview16);
		checkShowUiTweaks = findViewById(R.id.checkShowUiTweaks);
		checkCustomCss = findViewById(R.id.checkCustomCss);
		editCustomCss = findViewById(R.id.editCustomCss);
		linear8 = findViewById(R.id.linear8);
		txtResult = findViewById(R.id.txtResult);
		btnCopy = findViewById(R.id.btnCopy);
		textview14 = findViewById(R.id.textview14);
		btnPickSidebarColor = findViewById(R.id.btnPickSidebarColor);
		textview15 = findViewById(R.id.textview15);
		btnPickBgColor = findViewById(R.id.btnPickBgColor);
		btnReset = findViewById(R.id.btnReset);
		btnGenerate = findViewById(R.id.btnGenerate);
	}
	
	private void initializeLogic() {
		
		setupSpinner();
		setupListeners();
	}
	
	
	
	private String sidebarColor = "#343A40";
	private String bgColor = "#FFFFFF";
	
	
	
	
	private void setupSpinner() {
		String[] themes = {"فاتح (الافتراضي)", "غامق", "شمسي", "بحري", "أزرق"};
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
		android.R.layout.simple_spinner_item, themes);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerTheme.setAdapter(adapter);
	}
	
	private void setupListeners() {
		checkCustomCss.setOnCheckedChangeListener((buttonView, isChecked) -> {
			editCustomCss.setVisibility(isChecked ? View.VISIBLE : View.GONE);
		});
		
		btnGenerate.setOnClickListener(v -> generateCode());
		btnCopy.setOnClickListener(v -> copyToClipboard());
		btnReset.setOnClickListener(v -> resetAllSettings());
		
		btnPickSidebarColor.setOnClickListener(v -> showColorPicker(true));
		btnPickBgColor.setOnClickListener(v -> showColorPicker(false));
	}
	
	private void generateCode() {
		StringBuilder code = new StringBuilder();
		
		code.append("JAZZMIN_SETTINGS = {\n");
		
		// Site title
		String siteTitle = editSiteTitle.getText().toString();
		if (!siteTitle.isEmpty()) {
			code.append("    \"site_title\": \"").append(siteTitle).append("\",\n");
		}
		
		// Site logo
		String logoText = editLogoText.getText().toString();
		String logoImage = editLogoImage.getText().toString();
		code.append("    \"site_logo\": {\n");
		if (!logoText.isEmpty()) {
			code.append("        \"text\": \"").append(logoText).append("\",\n");
		}
		if (!logoImage.isEmpty()) {
			code.append("        \"image\": \"").append(logoImage).append("\",\n");
		}
		code.append("    },\n");
		
		// UI Tweaks
		code.append("    \"ui_tweaks\": {\n");
		code.append("        \"sidebar\": {\n");
		code.append("            \"hide\": ").append(checkHideSidebar.isChecked()).append(",\n");
		code.append("            \"compact\": ").append(checkCompactSidebar.isChecked()).append(",\n");
		code.append("        },\n");
		code.append("        \"topmenu\": {\n");
		code.append("            \"enabled\": ").append(checkTopMenu.isChecked()).append(",\n");
		code.append("        },\n");
		code.append("        \"show_icons\": ").append(checkShowIcons.isChecked()).append(",\n");
		code.append("        \"enabled\": ").append(checkShowUiTweaks.isChecked()).append(",\n");
		code.append("    },\n");
		
		// Theme
		String selectedTheme = spinnerTheme.getSelectedItem().toString();
		code.append("    \"theme\": \"").append(mapTheme(selectedTheme)).append("\",\n");
		
		// Colors
		code.append("    \"colors\": {\n");
		code.append("        \"sidebar\": \"").append(sidebarColor).append("\",\n");
		code.append("        \"background\": \"").append(bgColor).append("\",\n");
		code.append("    },\n");
		
		// Custom CSS
		if (checkCustomCss.isChecked() && !editCustomCss.getText().toString().isEmpty()) {
			code.append("    \"custom_css\": \"\"\"\n");
			code.append(editCustomCss.getText().toString()).append("\n");
			code.append("    \"\"\",\n");
		}
		
		code.append("}\n");
		
		// JAZZMIN_UI_TWEAKS
		code.append("\nJAZZMIN_UI_TWEAKS = {\n");
		code.append("    \"theme\": \"").append(mapTheme(selectedTheme)).append("\",\n");
		code.append("    \"dark_mode_theme\": null,\n");
		code.append("    \"navbar\": \"navbar-dark\",\n");
		code.append("    \"sidebar\": {\n");
		code.append("        \"style\": \"").append(checkCompactSidebar.isChecked() ? "compact" : "default").append("\",\n");
		code.append("        \"show\": ").append(!checkHideSidebar.isChecked()).append(",\n");
		code.append("    },\n");
		code.append("}\n");
		
		txtResult.setText(code.toString());
		btnCopy.setVisibility(View.VISIBLE);
	}
	
	private String mapTheme(String theme) {
		switch(theme) {
			case "غامق": return "dark";
			case "شمسي": return "solar";
			case "بحري": return "sea";
			case "أزرق": return "blue";
			default: return "light";
		}
	}
	
	private void copyToClipboard() {
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("Jazzmin Code", txtResult.getText().toString());
		clipboard.setPrimaryClip(clip);
		Toast.makeText(this, "تم نسخ الكود إلى الحافظة", Toast.LENGTH_SHORT).show();
	}
	
	private void resetAllSettings() {
		checkHideSidebar.setChecked(false);
		checkTopMenu.setChecked(false);
		checkCompactSidebar.setChecked(false);
		checkShowIcons.setChecked(false);
		checkShowUiTweaks.setChecked(false);
		checkCustomCss.setChecked(false);
		
		editSiteTitle.setText("");
		editLogoText.setText("");
		editLogoImage.setText("");
		editCustomCss.setText("");
		editCustomCss.setVisibility(View.GONE);
		
		spinnerTheme.setSelection(0);
		sidebarColor = "#343A40";
		bgColor = "#FFFFFF";
		btnPickSidebarColor.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(sidebarColor)));
		btnPickBgColor.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(bgColor)));
		
		txtResult.setText("");
		btnCopy.setVisibility(View.GONE);
		
		Toast.makeText(this, "تم إعادة تعيين جميع الإعدادات", Toast.LENGTH_SHORT).show();
	}
	
	private void showColorPicker(boolean isSidebar) {
		// هنا يمكن إضافة مكتبة ColorPicker مثل AmbilWarna
		// هذا مثال بسيط:
		int currentColor = Color.parseColor(isSidebar ? sidebarColor : bgColor);
		
		// باستخدام AmbilWarna
		/*
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, currentColor, 
            new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    String hexColor = String.format("#%06X", (0xFFFFFF & color));
                    if (isSidebar) {
                        sidebarColor = hexColor;
                        btnPickSidebarColor.setBackgroundTintList(ColorStateList.valueOf(color));
                    } else {
                        bgColor = hexColor;
                        btnPickBgColor.setBackgroundTintList(ColorStateList.valueOf(color));
                    }
                }
                
                @Override
                public void onCancel(AmbilWarnaDialog dialog) {
                    // إلغاء
                }
            });
        colorPicker.show();
        */
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