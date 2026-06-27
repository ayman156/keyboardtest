import subprocess
import sys
import os

BASE_DIR = os.getcwd()

def run_command(command_list):
    try:
        subprocess.run(command_list, check=True)
    except subprocess.CalledProcessError as e:
        print(f"\n❌ خطأ أثناء تنفيذ الأمر: {e}")

def create_model(app_name):
    model_name = input("📦 أدخل اسم الموديل: ").strip()
    fields = []
    print("📌 أدخل الحقول (اكتب 'تم' عند الانتهاء):")
    while True:
        field = input("🔸 الحقل (مثال: name=CharField(max_length=100)): ")
        if field.lower() == 'تم':
            break
        fields.append(f"    {field}")
    
    model_code = f"\n\nclass {model_name}(models.Model):\n" + '\n'.join(fields) + "\n"
    models_path = os.path.join(BASE_DIR, app_name, "models.py")
    with open(models_path, "a") as f:
        f.write(model_code)
    
    print(f"✅ تم إضافة الموديل {model_name} في {models_path}")

def create_crud(app_name):
    model_name = input("🔧 اسم الموديل: ").strip()
    views_path = os.path.join(BASE_DIR, app_name, "views.py")
    urls_path = os.path.join(BASE_DIR, app_name, "urls.py")
    templates_dir = os.path.join(BASE_DIR, app_name, "templates", app_name)
    os.makedirs(templates_dir, exist_ok=True)

    views_code = f"""
from django.shortcuts import render, get_object_or_404, redirect
from .models import {model_name}
from .forms import {model_name}Form

def {model_name.lower()}_list(request):
    items = {model_name}.objects.all()
    return render(request, '{app_name}/{model_name.lower()}_list.html', {{'items': items}})

def {model_name.lower()}_create(request):
    form = {model_name}Form(request.POST or None)
    if form.is_valid():
        form.save()
        return redirect('{model_name.lower()}_list')
    return render(request, '{app_name}/{model_name.lower()}_form.html', {{'form': form}})

def {model_name.lower()}_update(request, pk):
    obj = get_object_or_404({model_name}, pk=pk)
    form = {model_name}Form(request.POST or None, instance=obj)
    if form.is_valid():
        form.save()
        return redirect('{model_name.lower()}_list')
    return render(request, '{app_name}/{model_name.lower()}_form.html', {{'form': form}})

def {model_name.lower()}_delete(request, pk):
    obj = get_object_or_404({model_name}, pk=pk)
    if request.method == "POST":
        obj.delete()
        return redirect('{model_name.lower()}_list')
    return render(request, '{app_name}/{model_name.lower()}_confirm_delete.html', {{'object': obj}})
"""

    urls_code = f"""
from django.urls import path
from . import views

urlpatterns = [
    path('', views.{model_name.lower()}_list, name='{model_name.lower()}_list'),
    path('create/', views.{model_name.lower()}_create, name='{model_name.lower()}_create'),
    path('update/<int:pk>/', views.{model_name.lower()}_update, name='{model_name.lower()}_update'),
    path('delete/<int:pk>/', views.{model_name.lower()}_delete, name='{model_name.lower()}_delete'),
]
"""

    form_code = f"""
from django import forms
from .models import {model_name}

class {model_name}Form(forms.ModelForm):
    class Meta:
        model = {model_name}
        fields = '__all__'
"""

    templates = {
        f"{model_name.lower()}_list.html": "<h1>قائمة</h1>\n{% for item in items %}<p>{{ item }}</p>{% endfor %}",
        f"{model_name.lower()}_form.html": "<h1>نموذج</h1>\n<form method='post'>{% csrf_token %}{{ form.as_p }}<button type='submit'>حفظ</button></form>",
        f"{model_name.lower()}_confirm_delete.html": "<h1>تأكيد الحذف</h1>\n<form method='post'>{% csrf_token %}<p>هل أنت متأكد؟</p><button type='submit'>نعم</button></form>",
    }

    with open(views_path, "a") as vf:
        vf.write(views_code)
    with open(urls_path, "w") as uf:
        uf.write(urls_code)

    forms_path = os.path.join(BASE_DIR, app_name, "forms.py")
    with open(forms_path, "w") as ff:
        ff.write(form_code)

    for filename, content in templates.items():
        with open(os.path.join(templates_dir, filename), "w") as tf:
            tf.write(content)

    print("✅ تم إنشاء CRUD كامل للموديل.")

def show_menu():
    print("\n🔧 أوامر Django المتقدمة:")
    print("1. startapp - إنشاء تطبيق")
    print("2. makemigrations - تجهيز الترحيلات")
    print("3. migrate - تطبيق الترحيلات")
    print("4. runserver - تشغيل السيرفر")
    print("5. createsuperuser - أدمن")
    print("6. shell - سطر أوامر Django")
    print("7. collectstatic - الملفات الثابتة")
    print("8. إنشاء موديل جديد")
    print("9. إنشاء CRUD تلقائي")
    print("10. تنفيذ أمر مخصص")
    print("0. خروج")

def main():
    while True:
        show_menu()
        choice = input("👉 اختر رقم: ").strip()
        if choice == "1":
            name = input("📦 اسم التطبيق: ")
            run_command([sys.executable, "manage.py", "startapp", name])
        elif choice == "2":
            run_command([sys.executable, "manage.py", "makemigrations"])
        elif choice == "3":
            run_command([sys.executable, "manage.py", "migrate"])
        elif choice == "4":
            run_command([sys.executable, "manage.py", "runserver"])
        elif choice == "5":
            run_command([sys.executable, "manage.py", "createsuperuser"])
        elif choice == "6":
            run_command([sys.executable, "manage.py", "shell"])
        elif choice == "7":
            run_command([sys.executable, "manage.py", "collectstatic"])
        elif choice == "8":
            app = input("📁 اسم التطبيق: ")
            create_model(app)
        elif choice == "9":
            app = input("📁 اسم التطبيق: ")
            create_crud(app)
        elif choice == "10":
            custom = input("🖥️ أدخل الأمر (مثال: showmigrations): ")
            run_command([sys.executable, "manage.py", custom])
        elif choice == "0":
            print("🚪 تم الخروج.")
            break
        else:
            print("❗ خيار غير صحيح.")

if __name__ == "__main__":
    main()