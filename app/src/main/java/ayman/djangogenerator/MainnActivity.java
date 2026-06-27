package ayman.djangogenerator;

import android.Manifest;
import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.content.*;
import android.content.ClipData;
import android.content.Intent;
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
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.*;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import aym.lib.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

public class MainnActivity extends AppCompatActivity {
	
	public final int REQ_CD_F = 101;
	
	private String co = "";
	private String targetHash = "";
	
	private RecyclerView recyclerView;
	
	private Intent f = new Intent(Intent.ACTION_GET_CONTENT);
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.mainn);
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
		recyclerView = findViewById(R.id.recyclerView);
		f.setType("*/*");
		f.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
	}
	
	private void initializeLogic() {
		
		
		RecyclerView recyclerView = findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		
		List<ItemModel> dataList = new ArrayList<>();
		
		// هنا تضيف أي حقل برمجياً وبسهولة:
		// (العنوان، الأيقونة من drawable، كلاس الصفحة)
		/*
    dataList.add(new ItemModel("تشفير النصوص", R.drawable.ic_db, MainActivity.class));
    dataList.add(new ItemModel("قاعدة البيانات", R.drawable.ic_db, MainActivity.class));

    */
		dataList.add(new ItemModel("الرئيسية", R.drawable.ic_excel, MainActivity.class));
		dataList.add(new ItemModel("التعليمات", R.drawable.ic_template, ToturActivity.class));
		dataList.add(new ItemModel("sql", R.drawable.ic_template, MysqlActivity.class));
		dataList.add(new ItemModel("test", R.drawable.ic_template, CodeActivity.class));
		dataList.add(new ItemModel("مكاتب جانغو", R.drawable.ic_launcher_foreground, WebviewActivity.class));
		CustomAdapter adapter = new CustomAdapter(dataList, this);
		recyclerView.setAdapter(adapter);
		
		PasswordGuard.checkAndShowLock(MainnActivity.this, "000", new PasswordGuard.PasswordListener() {
			@Override
			public void onCorrectPassword() {
				// يتم تنفيذ الكود هنا فقط إذا كان الباسورد صحيحاً 
				// أو إذا كان المستخدم قد سجل دخوله بنجاح في وقت سابق.
			}
		});
		
		targetHash = "pA2oClnRcMqpUM8VwYxFTUejmyaYnYtkDs10W6cb9dw=";
		checkAppConfig(MainnActivity.this);
	}
	
	private void checkAppConfig(Context context) {
		try {
			// 1. تعريف البيانات الحساسة بشكل مجزأ لتمويه الـ Decompilers
			String pName = "ayman.djan" + "gogenerator"; 
			// String targetHash = "E5:C7:9C:C1:32:6C:42:BD:E6:C8:CB:C4:C6:83:4F:7A:48:2B:AB:D8:2A:99:F9:85:BA:C6:B9:46:93:A8:4C:A5"; // SHA-256 Base64
			
			// 2. التحقق من اسم البكج
			if (!context.getPackageName().equals(pName)) {
				android.os.Process.killProcess(android.os.Process.myPid());
			}
			
			// 3. استخراج التوقيع الحالي (باستخدام Flag متوافق مع الإصدارات القديمة والجديدة)
			android.content.pm.Signature[] sigs;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
				android.content.pm.SigningInfo signingInfo = context.getPackageManager()
				.getPackageInfo(context.getPackageName(), android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES)
				.signingInfo;
				sigs = signingInfo.getApkContentsSigners();
			} else {
				sigs = context.getPackageManager()
				.getPackageInfo(context.getPackageName(), android.content.pm.PackageManager.GET_SIGNATURES)
				.signatures;
			}
			
			// 4. مقارنة بصمة SHA-256
			for (android.content.pm.Signature sig : sigs) {
				java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
				md.update(sig.toByteArray());
				String currentHash = android.util.Base64.encodeToString(md.digest(), android.util.Base64.NO_WRAP);
				co = currentHash;
				if (targetHash.equals(currentHash)) {
					return; // التوقيع سليم، استمر في تشغيل التطبيق
				}
			}
			
			// إذا لم يتطابق أي توقيع، أغلق التطبيق
			// android.os.Process.killProcess(android.os.Process.myPid());
			// System.exit(0);
			
			
		} catch (Exception e) {
			// أي محاولة تلاعب تؤدي لخطأ ستغلق التطبيق فوراً
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	}
	
	private void getAndCopyAppSignature(Context context) {
		try {
			android.content.pm.Signature[] sigs;
			// التعامل مع اختلاف إصدارات أندرويد للحصول على التوقيع
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
				android.content.pm.PackageInfo pi = context.getPackageManager().getPackageInfo(
				context.getPackageName(), android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES);
				sigs = pi.signingInfo.getApkContentsSigners();
			} else {
				android.content.pm.PackageInfo pi = context.getPackageManager().getPackageInfo(
				context.getPackageName(), android.content.pm.PackageManager.GET_SIGNATURES);
				sigs = pi.signatures;
			}
			
			for (android.content.pm.Signature sig : sigs) {
				// تشفير التوقيع باستخدام SHA-256
				java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
				md.update(sig.toByteArray());
				byte[] digest = md.digest();
				
				// تحويله إلى صيغة Base64
				String base64Hash = android.util.Base64.encodeToString(digest, android.util.Base64.NO_WRAP);
				
				// 1. طباعته في الـ Logcat (ابحث عن كلمة "APP_SIG")
				android.util.Log.d("APP_SIG", "Your Hash: " + base64Hash);
				
				// 2. نسخه إلى الحافظة تلقائياً
				android.content.ClipboardManager clipboard = (android.content.ClipboardManager) 
				context.getSystemService(Context.CLIPBOARD_SERVICE);
				android.content.ClipData clip = android.content.ClipData.newPlainText("App Signature", base64Hash);
				clipboard.setPrimaryClip(clip);
				
				// إظهار رسالة تأكيد
				android.widget.Toast.makeText(context, "تم نسخ الهاش للحافظة!", android.widget.Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			android.util.Log.e("APP_SIG", "Error getting signature", e);
		}
	}
	
	
	{
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
				if (_filePath.get((int)(0)).endsWith(".db")) {
					FileUtil.copyFile(_filePath.get((int)(0)), "data/data/"+ getApplicationContext().getPackageName() +"/database".concat("/django_builder.db"));
					SketchwareUtil.showMessage(getApplicationContext(), "تم");
				} else {
					SketchwareUtil.showMessage(getApplicationContext(), "فشل");
				}
			}
			else {
				
			}
			break;
			default:
			break;
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