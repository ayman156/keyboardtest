package com.sampro.customkeyboard;

import android.inputmethodservice.InputMethodService;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class CustomKeyboardService extends InputMethodService implements View.OnClickListener {

    private LinearLayout layoutEnglish;
    private LinearLayout layoutArabic;
    private boolean isArabic = false;
    private ClipboardManager clipboardManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateInputView() {
        // لود الواجهة الرئيسية
        View keyboardRootView = getLayoutInflater().inflate(R.layout.keyboard_view, null);

        // ربط الحاويات والأزرار الوظيفية
        layoutEnglish = keyboardRootView.findViewById(R.id.layout_english);
        layoutArabic = keyboardRootView.findViewById(R.id.layout_arabic);
        
        Button btnChangeLang = keyboardRootView.findViewById(R.id.btn_change_lang);
        Button btnNormalPaste = keyboardRootView.findViewById(R.id.btn_normal_paste);
        Button btnSimulatePaste = keyboardRootView.findViewById(R.id.btn_simulate_paste);
        Button btnSpace = keyboardRootView.findViewById(R.id.btn_space);
        Button btnDelete = keyboardRootView.findViewById(R.id.btn_delete);
        Button btnEnter = keyboardRootView.findViewById(R.id.btn_enter);

        // إعداد الحافظة
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        // تعيين الأحداث للأزرار الرئيسية
        btnChangeLang.setOnClickListener(v -> switchLanguage());
        btnNormalPaste.setOnClickListener(v -> handleNormalPaste());
        btnSimulatePaste.setOnClickListener(v -> handleSimulatedPaste());
        btnSpace.setOnClickListener(v -> sendText(" "));
        btnDelete.setOnClickListener(v -> handleDelete());
        btnEnter.setOnClickListener(v -> sendEnter());

        // تعيين مستمع الضغط لجميع أزرار الحروف تلقائياً
        setKeyListeners(keyboardRootView);

        return keyboardRootView;
    }

    // ليف ممتد للبحث عن أزرار الحروف وتعيين الـ OnClickListener لها
    private void setKeyListeners(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof Button) {
                    int id = child.getId();
                    // تخطي الأزرار التحكمية حتى لا يتم طباعة نصوصها
                    if (id != R.id.btn_change_lang && id != R.id.btn_normal_paste && 
                        id != R.id.btn_simulate_paste && id != R.id.btn_space && 
                        id != R.id.btn_delete && id != R.id.btn_enter) {
                        child.setOnClickListener(this);
                    }
                } else {
                    setKeyListeners(child);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v instanceof Button) {
            String text = ((Button) v).getText().toString();
            sendText(text);
        }
    }

    // إرسال النص إلى الحقل الفعال
    private void sendText(String text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.commitText(text, 1);
        }
    }

    // عملية مسح الحرف (Backspace)
    private void handleDelete() {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.deleteSurroundingText(1, 0);
        }
    }

    // عملية النزول لسطر جديد
    private void sendEnter() {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
        }
    }

    // تبديل لغة الكيبورد واجهاتها
    private void switchLanguage() {
        isArabic = !isArabic;
        if (isArabic) {
            layoutEnglish.setVisibility(View.GONE);
            layoutArabic.setVisibility(View.VISIBLE);
        } else {
            layoutEnglish.setVisibility(View.VISIBLE);
            layoutArabic.setVisibility(View.GONE);
        }
    }

    // جلب النص الحالي المتواجد في كليب بورد النظام
    private String getClipboardText() {
        if (clipboardManager != null && clipboardManager.hasPrimaryClip()) {
            ClipData clipData = clipboardManager.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                CharSequence text = clipData.getItemAt(0).getText();
                return text != null ? text.toString() : "";
            }
        }
        return "";
    }

    // 1. اللصق العادي الدفعي
    private void handleNormalPaste() {
        String text = getClipboardText();
        if (!text.isEmpty()) {
            sendText(text);
        } else {
            Toast.makeText(this, "الحافظة فارغة", Toast.LENGTH_SHORT).show();
        }
    }

    // 2. محاكاة الكتابة التلقائية (حرف بحرف مع تايمر)
    private void handleSimulatedPaste() {
        final String text = getClipboardText();
        if (text.isEmpty()) {
            Toast.makeText(this, "الحافظة فارغة", Toast.LENGTH_SHORT).show();
            return;
        }

        // تشغيل العملية في خلفية منفصلة حتى لا يتجمد تطبيق الكيبورد
        new Thread(() -> {
            InputConnection ic = getCurrentInputConnection();
            if (ic == null) return;

            // سرعة الكتابة: 80 ميلي ثانية بين كل حرف وآخير
            int delayBetweenChars = 80; 

            for (int i = 0; i < text.length(); i++) {
                final String character = String.valueOf(text.charAt(i));
                
                // إرسال الحرف للواجهة الرئيسية عبر الـ Handler
                mainHandler.post(() -> ic.commitText(character, 1));

                try {
                    Thread.sleep(delayBetweenChars);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }
}
