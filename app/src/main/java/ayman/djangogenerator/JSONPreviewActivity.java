package ayman.djangogenerator;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import android.os.Bundle;
import android.content.Intent;
import android.content.Context;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;

public class JSONPreviewActivity extends AppCompatActivity {
    
    private TextView jsonTextView;
    private Button generateButton, copyButton, shareButton, backButton;
    
    private String jsonData;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_preview);
        
        initViews();
        setupListeners();
        
        Intent intent = getIntent();
        if (intent.hasExtra("JSON_DATA")) {
            jsonData = intent.getStringExtra("JSON_DATA");
            displayFormattedJSON();
        }
    }
    
    private void initViews() {
        jsonTextView = findViewById(R.id.json_textview);
        generateButton = findViewById(R.id.generate_button);
        copyButton = findViewById(R.id.copy_button);
        shareButton = findViewById(R.id.share_button);
        backButton = findViewById(R.id.back_button);
    }
    
    private void setupListeners() {
        generateButton.setOnClickListener(v -> generateProject());
        copyButton.setOnClickListener(v -> copyToClipboard());
        shareButton.setOnClickListener(v -> shareJSON());
        backButton.setOnClickListener(v -> finish());
    }
    
    private void displayFormattedJSON() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
            String prettyJson = gson.toJson(jsonObject);
            jsonTextView.setText(prettyJson);
        } catch (Exception e) {
            jsonTextView.setText(jsonData);
        }
    }
    
    private void generateProject() {
        Toast.makeText(this, "جاري توليد مشروع Django...", Toast.LENGTH_SHORT).show();
        // هنا يمكنك استدعاء AdvancedDjangoGenerator
        // واستخدام jsonData لإنشاء المشروع
          
            new Thread(() -> {
            try {
                AdvancedDjangoGenerator generator = new AdvancedDjangoGenerator(this, jsonData);
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
    }
    
    private void copyToClipboard() {
        Intent m = new Intent();  
        m.setClass(getApplicationContext(), WebviewActivity.class);
        m.putExtra("data", jsonData);
       startActivity(m);
/*
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("JSON Data", jsonData);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "تم نسخ JSON إلى الحافظة", Toast.LENGTH_SHORT).show();
        */
    }
    
    private void shareJSON() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, jsonData);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Django Project JSON");
        startActivity(Intent.createChooser(shareIntent, "مشاركة JSON"));
    }
}