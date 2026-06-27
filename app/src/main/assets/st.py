import os
import sys
import django
import subprocess
from pathlib import Path

def get_project_name():
    """البحث التلقائي عن اسم مجلد الإعدادات"""
    # البحث عن أي مجلد يحتوي على ملف settings.py بجانب manage.py
    for path in Path('.').iterdir():
        if path.is_dir() and (path / 'settings.py').exists():
            return path.name
    return None

def initialize_project():
    project_name = get_project_name()
    
    if not project_name:
        print("❌ خطأ: لم يتم العثور على مجلد يحتوي على settings.py!")
        return

    print(f"✅ تم اكتشاف المشروع: {project_name}")
    
    # 1. إعداد بيئة Django ديناميكياً
    os.environ.setdefault('DJANGO_SETTINGS_MODULE', f'{project_name}.settings')
    
    try:
        django.setup()
    except Exception as e:
        print(f"❌ فشل في تهيئة Django: {e}")
        return

    from django.contrib.auth import get_user_model

    try:
        # 2. تنفيذ Migration
        print("--- 📂 جاري تجهيز الميجريشن... ---")
        subprocess.run([sys.executable, 'manage.py', 'makemigrations'], check=True)
        
        print("--- ⚙️ جاري تحديث قاعدة البيانات... ---")
        subprocess.run([sys.executable, 'manage.py', 'migrate'], check=True)

        # 3. إنشاء المستخدم الخارق
        User = get_user_model()
        username = 'admin'
        password = 'admin'
        email = 'admin@example.com'

        if not User.objects.filter(username=username).exists():
            print(f"--- 👤 جاري إنشاء المستخدم: {username} ---")
            User.objects.create_superuser(username=username, email=email, password=password)
            print("--- ✨ تم إنشاء المستخدم (admin/admin) بنجاح! ---")
        else:
            print(f"--- ℹ️ المستخدم '{username}' موجود مسبقاً. ---")

        print("\n🚀 اكتملت العملية! يمكنك الآن تشغيل السيرفر.")

    except subprocess.CalledProcessError as e:
        print(f"❌ خطأ في تنفيذ أوامر manage.py: {e}")
    except Exception as e:
        print(f"❌ حدث خطأ غير متوقع: {e}")

if __name__ == "__main__":
    initialize_project()

