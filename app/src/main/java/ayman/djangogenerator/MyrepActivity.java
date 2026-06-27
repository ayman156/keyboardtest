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
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.*;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import aym.lib.*;
import com.google.android.material.appbar.AppBarLayout;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

public class MyrepActivity extends AppCompatActivity {
	
	private Toolbar _toolbar;
	private AppBarLayout _app_bar;
	private CoordinatorLayout _coordinator;
	
	private LinearLayout main_container;
	private CardView cardview11;
	private CardView cardview12;
	private CardView cardview13;
	private CardView cardview14;
	private CardView cardview15;
	private CardView cardview16;
	private CardView cardview17;
	private CardView cardview18;
	private LinearLayout linear38;
	private LinearLayout linear25;
	private TextView textview27;
	private EditText et_report_name;
	private EditText et_report_description;
	private LinearLayout linear26;
	private TextView textview28;
	private Spinner model_spinner;
	private LinearLayout linear27;
	private TextView textview29;
	private RecyclerView rv_selected_fields;
	private LinearLayout linear28;
	private TextView textview30;
	private LinearLayout related_container;
	private LinearLayout linear29;
	private LinearLayout linear30;
	private LinearLayout annotate_container;
	private TextView textview31;
	private Button btn_add_annotate;
	private LinearLayout linear31;
	private LinearLayout linear32;
	private LinearLayout filter_container;
	private TextView textview32;
	private Button btn_add_filter;
	private LinearLayout linear33;
	private TextView textview33;
	private LinearLayout linear34;
	private LinearLayout linear35;
	private CheckBox cb_group_by;
	private CheckBox cb_order_by;
	private CheckBox cb_distinct;
	private TextView textview34;
	private EditText et_limit;
	private TextView textview35;
	private EditText et_offset;
	private LinearLayout linear36;
	private TextView textview36;
	private LinearLayout linear37;
	private Button btnPreviewTemplate;
	private Button btnExportExcel;
	private Button btnExportPDF;
	private Button btnGenerateFullCode;
	private Button btn_save_report;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.myrep);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		_app_bar = findViewById(R.id._app_bar);
		_coordinator = findViewById(R.id._coordinator);
		_toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				onBackPressed();
			}
		});
		main_container = findViewById(R.id.main_container);
		cardview11 = findViewById(R.id.cardview11);
		cardview12 = findViewById(R.id.cardview12);
		cardview13 = findViewById(R.id.cardview13);
		cardview14 = findViewById(R.id.cardview14);
		cardview15 = findViewById(R.id.cardview15);
		cardview16 = findViewById(R.id.cardview16);
		cardview17 = findViewById(R.id.cardview17);
		cardview18 = findViewById(R.id.cardview18);
		linear38 = findViewById(R.id.linear38);
		linear25 = findViewById(R.id.linear25);
		textview27 = findViewById(R.id.textview27);
		et_report_name = findViewById(R.id.et_report_name);
		et_report_description = findViewById(R.id.et_report_description);
		linear26 = findViewById(R.id.linear26);
		textview28 = findViewById(R.id.textview28);
		model_spinner = findViewById(R.id.model_spinner);
		linear27 = findViewById(R.id.linear27);
		textview29 = findViewById(R.id.textview29);
		rv_selected_fields = findViewById(R.id.rv_selected_fields);
		linear28 = findViewById(R.id.linear28);
		textview30 = findViewById(R.id.textview30);
		related_container = findViewById(R.id.related_container);
		linear29 = findViewById(R.id.linear29);
		linear30 = findViewById(R.id.linear30);
		annotate_container = findViewById(R.id.annotate_container);
		textview31 = findViewById(R.id.textview31);
		btn_add_annotate = findViewById(R.id.btn_add_annotate);
		linear31 = findViewById(R.id.linear31);
		linear32 = findViewById(R.id.linear32);
		filter_container = findViewById(R.id.filter_container);
		textview32 = findViewById(R.id.textview32);
		btn_add_filter = findViewById(R.id.btn_add_filter);
		linear33 = findViewById(R.id.linear33);
		textview33 = findViewById(R.id.textview33);
		linear34 = findViewById(R.id.linear34);
		linear35 = findViewById(R.id.linear35);
		cb_group_by = findViewById(R.id.cb_group_by);
		cb_order_by = findViewById(R.id.cb_order_by);
		cb_distinct = findViewById(R.id.cb_distinct);
		textview34 = findViewById(R.id.textview34);
		et_limit = findViewById(R.id.et_limit);
		textview35 = findViewById(R.id.textview35);
		et_offset = findViewById(R.id.et_offset);
		linear36 = findViewById(R.id.linear36);
		textview36 = findViewById(R.id.textview36);
		linear37 = findViewById(R.id.linear37);
		btnPreviewTemplate = findViewById(R.id.btnPreviewTemplate);
		btnExportExcel = findViewById(R.id.btnExportExcel);
		btnExportPDF = findViewById(R.id.btnExportPDF);
		btnGenerateFullCode = findViewById(R.id.btnGenerateFullCode);
		btn_save_report = findViewById(R.id.btn_save_report);
	}
	
	private void initializeLogic() {
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