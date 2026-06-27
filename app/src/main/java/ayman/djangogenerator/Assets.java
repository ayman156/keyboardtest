package ayman.djangogenerator;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Context;
import android.content.res.AssetManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Assets {

    private Context context;
    private String assetName;
    private String targetPath;

    // مشيد الكلاس (Constructor) لتعريف المتغيرات
    public Assets(Context context, String assetName, String targetPath) {
        this.context = context;
        this.assetName = assetName;
        this.targetPath = targetPath;
    }

    // إزالة static لكي نتمكن من الوصول للمتغيرات المعرفة في المشيد
    public boolean copyAssetToPath() {
        AssetManager assetManager = context.getAssets();
        InputStream in = null;
        OutputStream out = null;

        try {
            in = assetManager.open(assetName);
            
            // التأكد من أن مسار الهدف موجود، وإذا لم يكن موجوداً يتم إنشاؤه
            File targetDir = new File(targetPath);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            File outFile = new File(targetDir, assetName);
            out = new FileOutputStream(outFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            
            out.flush();
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    
    public static void copyAssetsFolder(Context context, String assetPath, String targetPath) {
        AssetManager assetManager = context.getAssets();
        String[] assets = null;
        try {
            // محاولة الحصول على قائمة بالملفات داخل المسار
            assets = assetManager.list(assetPath);
            
            if (assets == null || assets.length == 0) {
                // إذا لم تكن قائمة، فهذا يعني أنه ملف وليس مجلد
                copyFile(context, assetPath, targetPath);
            } else {
                // إذا كانت قائمة، فهذا مجلد، يجب إنشاؤه في الوجهة
                File targetDir = new File(targetPath);
                if (!targetDir.exists()) {
                    targetDir.mkdirs();
                }
                
                // نسخ كل عنصر داخل المجلد بشكل تكراري
                for (String asset : assets) {
                    String fullAssetPath = assetPath.equals("") ? asset : assetPath + "/" + asset;
                    String fullTargetPath = targetPath + "/" + asset;
                    copyAssetsFolder(context, fullAssetPath, fullTargetPath);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(Context context, String assetPath, String targetPath) {
        try (InputStream in = context.getAssets().open(assetPath);
             OutputStream out = new FileOutputStream(targetPath)) {
            
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
