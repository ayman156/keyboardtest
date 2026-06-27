import os
import subprocess
import tarfile

def run_command(command, description):
    print(f"[*] {description}...")
    result = subprocess.run(command, shell=True, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"[!] Error: {result.stderr}")
        return False
    print(f"[+] Success!")
    return True

def package_project():
    project_name = "djan_project_package"
    
    # 1. بناء صورة الـ Docker
    # سيقوم هذا الأمر بقراءة الـ Dockerfile الذي صممناه سابقاً
    if not run_command("docker build -t djan_app:latest .", "Building Docker Image"):
        return

    # 2. تصدير الصورة إلى ملف (هذا ما ترسله للمستخدم)
    print("[*] Exporting Docker Image to tar file...")
    if not run_command("docker save djan_app:latest -o djan_app.tar", "Saving Image to djan_app.tar"):
        return

    # 3. ضغط ملفات المشروع (docker-compose و ملف الإعدادات) مع الصورة
    files_to_include = ['docker-compose.yml', 'nginx.conf', 'djan_app.tar']
    
    with tarfile.open(f"{project_name}.tar.gz", "w:gz") as tar:
        for file in files_to_include:
            if os.path.exists(file):
                tar.add(file)
                print(f"[+] Added {file} to the final package.")

    print(f"\n[DONE] تم إنتاج الملف النهائي: {project_name}.tar.gz")
    print("يمكنك إرسال هذا الملف للمستخدم الآن.")

if __name__ == "__main__":
    package_project()
