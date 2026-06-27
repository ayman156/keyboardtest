# 1. استخدام صورة بايثون خفيفة ومستقرة
FROM python:3.11-slim

# 2. إعداد متغيرات البيئة
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV DJANGO_SETTINGS_MODULE=MyProjectname.settings

# 3. تحديد مجلد العمل
WORKDIR /app

# 4. تثبيت اعتماديات النظام (تصحيح السلاش والمجلدات المؤقتة)
RUN apt-get update && apt-get install -y \
    gcc \
    python3-dev \
    musl-dev \
    && rm -rf /var/lib/apt/lists/*

# 5. تثبيت مكتبات بايثون (قبل نسخ الكود لسرعة البناء لاحقاً)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 6. نسخ ملفات المشروع بالكامل
COPY . .

# 7. تجميع الملفات الثابتة (Static Files)
# ملاحظة: تأكد من وجود المجلد في الإعدادات
RUN python manage.py collectstatic --noinput

# 8. إعداد مستخدم غير جذري (Non-root) للأمان
RUN useradd -m django && chown -R django:django /app
USER django

# 9. أمر التشغيل باستخدام Gunicorn
CMD ["gunicorn", "--bind", "0.0.0.0:8000", "--workers", "3", "MyProjectname.wsgi:application"]
