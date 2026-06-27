package ayman.djangogenerator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URLEncoder;

public class PasswordGuard {

    public interface PasswordListener {
        void onCorrectPassword();
    }
    
    // جميع النصوص كمتغيرات عادية - سيتم تشغيرها بواسطة ProGuard
    private static final String TEXT_REQUEST = "طلب تفعيل:   ";
    private static final String PHONE_NUMBER = "+967773314386";
    private static final String PREFS_IS_VERIFIED = "is_verified";
    private static final String PREFS_VIP = "vip";
    private static final String PREFS_VIPC = "vipc";
    private static final String PREFS_ALL = "all";
    private static final String TOAST_COPY_SUCCESS = "تم نسخ المعرف إلى الحافظة";
    private static final String TOAST_WHATSAPP_NOT_INSTALLED = "تطبيق واتساب غير مثبت";
    private static final String TOAST_ACTIVATION_SUCCESS = "تم التفعيل بنجاح";
    private static final String TOAST_WRONG_PASSWORD = "كلمة المرور خطأ، حاول مرة أخرى";
    private static final String LABEL_DEVICE_ID = "معرف جهازك الخاص:";
    private static final String LABEL_ACTIVATION_KEY = "مفتاح التفعيل:";
    private static final String DIALOG_TITLE = "نظام التحقق والأمان";
    private static final String BUTTON_COPY = "نسخ المعرف";
    private static final String BUTTON_SHARE_WHATSAPP = "مشاركة عبر واتساب";
    private static final String INPUT_HINT = "أدخل مفتاح التفعيل هنا...";
    private static final String BUTTON_ACTIVATE = "تفعيل الحساب";
    private static final String BUTTON_EXIT = "خروج";
    private static final String ERROR_PASSWORD = "كلمة المرور غير صحيحة";
    private static final String CONST_1205566 = "1205566";
    private static final String CONST_PHONE = "Phone";
    private static final String CONST_7 = "7";
    private static final String CONST_ERROR = "Error";

    public static void checkAndShowLock(final Activity activity, final String correctPassword, final PasswordListener listener) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREFS_ALL, Context.MODE_PRIVATE);
        
        // إذا كان المستخدم قد تحقق بالفعل سابقاً، لا تظهر الدايولاج
        if (sharedPreferences.getBoolean(PREFS_IS_VERIFIED, false)) {
            if (listener != null) listener.onCorrectPassword();
            return;
        }

        // استخراج المعرف وتجهيز المنطق
        final String deviceIdentifier = handleLogic(activity);

        // تصميم الواجهة برمجياً مع تحسينات
        LinearLayout mainContainer = new LinearLayout(activity);
        mainContainer.setOrientation(LinearLayout.VERTICAL);
        mainContainer.setPadding(60, 40, 60, 30);
        mainContainer.setBackgroundColor(Color.parseColor("#FFFFFF"));

        // عنوان الصندوق
        TextView headerTextView = new TextView(activity);
        headerTextView.setText(DIALOG_TITLE);
        headerTextView.setTextSize(20);
        headerTextView.setTypeface(null, Typeface.BOLD);
        headerTextView.setTextColor(Color.parseColor("#2C3E50"));
        headerTextView.setGravity(Gravity.CENTER);
        headerTextView.setPadding(0, 0, 0, 30);

        // بطاقة المعلومات
        LinearLayout infoCardLayout = new LinearLayout(activity);
        infoCardLayout.setOrientation(LinearLayout.VERTICAL);
        infoCardLayout.setBackgroundColor(Color.parseColor("#F8F9FA"));
        infoCardLayout.setPadding(30, 30, 30, 30);
        
        // إضافة حدود للبطاقة
        infoCardLayout.setBackground(new android.graphics.drawable.GradientDrawable() {
            {
                setColor(Color.parseColor("#F8F9FA"));
                setCornerRadius(16);
                setStroke(2, Color.parseColor("#E9ECEF"));
            }
        });

        TextView idLabelView = new TextView(activity);
        idLabelView.setText(LABEL_DEVICE_ID);
        idLabelView.setTextSize(16);
        idLabelView.setTextColor(Color.parseColor("#34495E"));
        idLabelView.setPadding(0, 0, 0, 15);

        final TextView idValueView = new TextView(activity);
        idValueView.setText(deviceIdentifier);
        idValueView.setTextSize(18);
        idValueView.setTypeface(null, Typeface.BOLD);
        idValueView.setTextColor(Color.parseColor("#27AE60"));
        idValueView.setPadding(0, 10, 0, 30);
        idValueView.setTextIsSelectable(true);
        idValueView.setGravity(Gravity.CENTER);
        
        // خلفية للمعرف
        idValueView.setBackground(new android.graphics.drawable.GradientDrawable() {
            {
                setColor(Color.parseColor("#E8F5E9"));
                setCornerRadius(12);
                setStroke(2, Color.parseColor("#C8E6C9"));
            }
        });

        // أزرار النسخ والمشاركة
        LinearLayout buttonsLayout = new LinearLayout(activity);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonsLayout.setGravity(Gravity.CENTER);
        buttonsLayout.setPadding(0, 15, 0, 30);

        Button copyButton = new Button(activity);
        copyButton.setText(BUTTON_COPY);
        copyButton.setBackgroundColor(Color.parseColor("#3498DB"));
        copyButton.setTextColor(Color.WHITE);
        copyButton.setPadding(20, 15, 20, 15);
        
        // تصميم زر النسخ
        copyButton.setBackground(new android.graphics.drawable.GradientDrawable() {
            {
                setColor(Color.parseColor("#3498DB"));
                setCornerRadius(8);
            }
        });
        
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("DeviceID", deviceIdentifier);
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(activity, TOAST_COPY_SUCCESS, Toast.LENGTH_SHORT).show();
            }
        });

        Button whatsappButton = new Button(activity);
        whatsappButton.setText(BUTTON_SHARE_WHATSAPP);
        whatsappButton.setBackgroundColor(Color.parseColor("#25D366"));
        whatsappButton.setTextColor(Color.WHITE);
        whatsappButton.setPadding(20, 15, 20, 15);
        
        // تصميم زر واتساب
        whatsappButton.setBackground(new android.graphics.drawable.GradientDrawable() {
            {
                setColor(Color.parseColor("#25D366"));
                setCornerRadius(8);
            }
        });
        
        whatsappButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_VIEW);
                    String whatsappUrl = "https://api.whatsapp.com/send?phone=" + PHONE_NUMBER + "&text=" + 
                            URLEncoder.encode(TEXT_REQUEST + deviceIdentifier, "UTF-8");
                    shareIntent.setData(Uri.parse(whatsappUrl));
                    activity.startActivity(shareIntent);
                } catch (Exception ex) {
                    Toast.makeText(activity, TOAST_WHATSAPP_NOT_INSTALLED, Toast.LENGTH_SHORT).show();
                }
            }
        });

        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        buttonLayoutParams.setMargins(8, 0, 8, 0);
        
        buttonsLayout.addView(copyButton, buttonLayoutParams);
        buttonsLayout.addView(whatsappButton, buttonLayoutParams);

        // حقل إدخال كلمة المرور
        TextView inputLabelView = new TextView(activity);
        inputLabelView.setText(LABEL_ACTIVATION_KEY);
        inputLabelView.setTextSize(16);
        inputLabelView.setTextColor(Color.parseColor("#34495E"));
        inputLabelView.setPadding(0, 30, 0, 15);

        final EditText passwordInput = new EditText(activity);
        passwordInput.setHint(INPUT_HINT);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordInput.setPadding(20, 18, 20, 18);
        passwordInput.setTextSize(16);
        
        // تصميم حقل الإدخال
        passwordInput.setBackground(new android.graphics.drawable.GradientDrawable() {
            {
                setColor(Color.WHITE);
                setCornerRadius(8);
                setStroke(2, Color.parseColor("#D1D5DB"));
            }
        });

        // تجميع المكونات
        infoCardLayout.addView(idLabelView);
        infoCardLayout.addView(idValueView);
        infoCardLayout.addView(buttonsLayout);
        
        mainContainer.addView(headerTextView);
        mainContainer.addView(infoCardLayout);
        mainContainer.addView(inputLabelView);
        mainContainer.addView(passwordInput);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(mainContainer)
           .setPositiveButton(BUTTON_ACTIVATE, null)
           .setNegativeButton(BUTTON_EXIT, (dialog, which) -> {
               activity.finishAffinity();
               System.exit(0);
           });

        final AlertDialog securityDialog = dialogBuilder.create();
        
        securityDialog.show();

        // تخصيص أزرار الدايالوج بعد الظهور
        Button positiveDialogButton = securityDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeDialogButton = securityDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        
        if (positiveDialogButton != null) {
            positiveDialogButton.setBackgroundColor(Color.parseColor("#27AE60"));
            positiveDialogButton.setTextColor(Color.WHITE);
            positiveDialogButton.setPadding(40, 15, 40, 15);
            
            // تصميم زر الإيجابي
            positiveDialogButton.setBackground(new android.graphics.drawable.GradientDrawable() {
                {
                    setColor(Color.parseColor("#27AE60"));
                    setCornerRadius(8);
                }
            });
        }
        
        if (negativeDialogButton != null) {
            negativeDialogButton.setBackgroundColor(Color.parseColor("#E74C3C"));
            negativeDialogButton.setTextColor(Color.WHITE);
            negativeDialogButton.setPadding(40, 15, 40, 15);
            
            // تصميم زر السلبي
            negativeDialogButton.setBackground(new android.graphics.drawable.GradientDrawable() {
                {
                    setColor(Color.parseColor("#E74C3C"));
                    setCornerRadius(8);
                }
            });
        }

        positiveDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEnteredPassword = passwordInput.getText().toString().trim();
                String storedPassword = sharedPreferences.getString(PREFS_VIP, "").trim();
                if (userEnteredPassword.equals(storedPassword)) {
                    // حفظ حالة التحقق لكي لا يظهر مرة أخرى
                    sharedPreferences.edit().putBoolean(PREFS_IS_VERIFIED, true).apply();
                    securityDialog.dismiss();
                    Toast.makeText(activity, TOAST_ACTIVATION_SUCCESS, Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onCorrectPassword();
                } else {
                    // إظهار خطأ
                    passwordInput.setError(ERROR_PASSWORD);
                    
                    // تأثير اهتزاز بسيط
                    Animation shakeAnimation = new android.view.animation.TranslateAnimation(0, 10, 0, 0);
                    shakeAnimation.setDuration(50);
                    shakeAnimation.setRepeatCount(5);
                    shakeAnimation.setRepeatMode(Animation.REVERSE);
                    passwordInput.startAnimation(shakeAnimation);
                    
                    Toast.makeText(activity, TOAST_WRONG_PASSWORD, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // دالة مساعدة لتحويل dp إلى pixels
    private static int convertDpToPixels(int dpValue, Context context) {
        return (int) (dpValue * context.getResources().getDisplayMetrics().density);
    }
    
    private static String handleLogic(Context context) {
        SharedPreferences appPreferences = context.getSharedPreferences(PREFS_ALL, Context.MODE_PRIVATE);
        String deviceUniqueId = getDeviceId();
        String finalResult = "";
        String encryptionKey = CONST_1205566;
        
        try {
            if (appPreferences.getString(PREFS_VIP, "").equals("")) {
                javax.crypto.SecretKey secretKey = generateSecretKey(encryptionKey);
                javax.crypto.Cipher cipherInstance = javax.crypto.Cipher.getInstance("AES");
                cipherInstance.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey);
                byte[] encryptedBytes = cipherInstance.doFinal(deviceUniqueId.getBytes());
                String encryptedValue = android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.DEFAULT);
                appPreferences.edit().putString(PREFS_VIP, encryptedValue).apply();
            }
            
            String vipData = appPreferences.getString(PREFS_VIP, "");
            javax.crypto.SecretKey secretKey = generateSecretKey(encryptionKey);
            javax.crypto.Cipher cipherInstance = javax.crypto.Cipher.getInstance("AES");
            cipherInstance.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipherInstance.doFinal(vipData.getBytes());
            finalResult = android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.DEFAULT);
            appPreferences.edit().putString(PREFS_VIPC, finalResult).apply();
            
        } catch (Exception ex) { 
            finalResult = CONST_ERROR; 
        }
        
        return finalResult;
    }

    private static javax.crypto.SecretKey generateSecretKey(String passwordValue) throws Exception {
        final java.security.MessageDigest messageDigest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] passwordBytes = passwordValue.getBytes("UTF-8");
        messageDigest.update(passwordBytes, 0, passwordBytes.length);
        return new javax.crypto.spec.SecretKeySpec(messageDigest.digest(), "AES");
    }

    public static String getDeviceId() {
        String systemInfo = Build.VERSION.RELEASE + Build.VERSION.INCREMENTAL + Build.DISPLAY + 
                          Build.FINGERPRINT + Build.HOST + Build.ID;
        return CONST_PHONE.concat(String.valueOf((long) (systemInfo.hashCode())).replace("-", CONST_7));
    }
}