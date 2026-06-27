package ayman.djangogenerator;

import android.animation.*;
import android.app.*;
import android.app.Activity;
import android.content.*;
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
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
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

public class WebviewActivity extends AppCompatActivity {
	
	private String jsonString = "";
	private String ht = "";
	
	private WebView webView;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.webview);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		webView = findViewById(R.id.webView);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setSupportZoom(true);
		
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView _param1, String _param2, Bitmap _param3) {
				final String _url = _param2;
				
				super.onPageStarted(_param1, _param2, _param3);
			}
			
			@Override
			public void onPageFinished(WebView _param1, String _param2) {
				final String _url = _param2;
				/*


_param1.evaluateJavascript(
"(function() {" +
"var meta = document.querySelector('meta[name=viewport]');" +
"if (meta) { meta.setAttribute('content', 'width=1024px'); }" +
"})();", 
null);


*/
				super.onPageFinished(_param1, _param2);
			}
		});
	}
	
	private void initializeLogic() {
		WebSettings webSettings = webView.getSettings(); 
		webSettings.setJavaScriptEnabled(true); 
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			webSettings.setAllowFileAccessFromFileURLs(true); 
			webSettings.setAllowUniversalAccessFromFileURLs(true);
		}
		webView.getSettings().setBuiltInZoomControls(true);
		if (getIntent().hasExtra("data")) {
			jsonString = getIntent().getStringExtra("data");
			
			
			
			JsonToErdConverter.convertJsonToErdHtml(
			this,
			jsonString,
			"erd_diagram",
			new JsonToErdConverter.ConversionCallback() {
				@Override
				public void onSuccess(String htmlFilePath) {
					runOnUiThread(() -> {
						// عرض الملف في WebView
						webView.getSettings().setJavaScriptEnabled(true);
						webView.loadDataWithBaseURL(null, htmlFilePath, "text/html", "UTF-8", null);
						
						ht = htmlFilePath;
						Toast.makeText(getApplicationContext(), 
						"تم إنشاء المخطط بنجاح", 
						Toast.LENGTH_LONG).show();
					});
				}
				
				@Override
				public void onError(String errorMessage) {
					runOnUiThread(() -> {
						Toast.makeText(getApplicationContext(), 
						"خطأ: " + errorMessage, 
						Toast.LENGTH_LONG).show();
					});
				}
			});
			
		} else {
			_read();
		}
	}
	
	public void _read() {
		try{
			java.io.InputStream jsonStringIn = WebviewActivity.this.getAssets().open("requirements.txt");
			int jsonStringSi = jsonStringIn.available();
			byte[] jsonStringBu = new byte[jsonStringSi];
			jsonStringIn.read(jsonStringBu);
			jsonStringIn.close();
			jsonString = new String(jsonStringBu, "UTF-8");
		}catch(Exception e){
			
		}
		webView.loadUrl("data:text/html ,<html>".concat(jsonString.concat("<html>")));
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