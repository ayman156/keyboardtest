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
import br.tiagohm.codeview.CodeView;
import br.tiagohm.codeview.CodeView.OnHighlightListener;
import br.tiagohm.codeview.Language;
import br.tiagohm.codeview.Theme;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

public class CodeActivity extends AppCompatActivity {
	
	private LinearLayout linear3;
	private TextView textview3;
	private CodeView codeview1;
	private TextView textview4;
	private EditText edittext1;
	private LinearLayout linear4;
	private Button btnSave;
	private Button btnSettings;
	private Button btnInsertDjango;
	private EditText searchEditText;
	private Button btnSearch;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.code);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		linear3 = findViewById(R.id.linear3);
		textview3 = findViewById(R.id.textview3);
		codeview1 = findViewById(R.id.codeview1);
		textview4 = findViewById(R.id.textview4);
		edittext1 = findViewById(R.id.edittext1);
		linear4 = findViewById(R.id.linear4);
		btnSave = findViewById(R.id.btnSave);
		btnSettings = findViewById(R.id.btnSettings);
		btnInsertDjango = findViewById(R.id.btnInsertDjango);
		searchEditText = findViewById(R.id.searchEditText);
		btnSearch = findViewById(R.id.btnSearch);
	}
	
	private void initializeLogic() {
		if (getIntent().hasExtra("code")) {
			String code = getIntent().getStringExtra("code");
		}
		if (getIntent().hasExtra("id")) {
			String save_path = getIntent().getStringExtra("id");
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