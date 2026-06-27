#!/bin/bash
# start.sh
# Shell script to show Django and Docker management options

while true; do
    echo "------------------------------------------"
    echo "إدارة مشروع Django و Docker"
    echo "------------------------------------------"
    echo "1. Start new Django project"
    echo "2. Start new Django app"
    echo "3. Make migrations"
    echo "4. Apply migrations"
    echo "5. Run development server (Local)"
    echo "6. Create superuser"
    echo "7. Export requirements"
    echo "8. Install requirements"
    echo "9. Collect static files"
    echo "10. Show Django version"
    echo "11. Run tests"
    echo "12. Show URLs"
    echo "--- خيارات Docker ---"
    echo "13. Build Docker Images"
    echo "14. Run Docker Containers (Background)"
    echo "15. Stop & Remove Containers (Down)"
    echo "16. Show Docker Logs"
    echo "17. Create Superuser in Docker"
    echo "18. Apply Migrations in Docker"
    echo "19. Run Forever (Restart Always)"
    echo "20. Check Containers Status (ps)"
    echo "21. Exit"
    echo "------------------------------------------"

    read -p "أدخل رقم الخيار (1-21): " choice

    case "$choice" in
        1) read -p "Enter project name: " pname; django-admin startproject "$pname" ;;
        2) read -p "Enter app name: " aname; python manage.py startapp "$aname" ;;
        3) python manage.py makemigrations ;;
        4) python manage.py migrate ;;
        5) python manage.py runserver ;;
        6) python manage.py createsuperuser ;;
        7) pip freeze > requirements.txt; echo "requirements.txt exported." ;;
        8) pip install -r requirements.txt ;;
        9) python manage.py collectstatic ;;
        10) python -m django --version ;;
        11) python manage.py test ;;
        12) python manage.py show_urls 2>/dev/null || echo "Install django-extensions for this feature." ;;
        13) echo "Building Docker images..."; docker-compose build ;;
        14) echo "Starting Docker containers..."; docker-compose up -d ;;
        15) echo "Stopping Docker containers..."; docker-compose down ;;
        16) echo "Showing Docker logs (Press Ctrl+C to stop)..."; docker-compose logs -f ;;
        17) echo "Creating Superuser in Docker..."; docker-compose exec web python manage.py createsuperuser ;;
        18) echo "Applying Migrations in Docker..."; docker-compose exec web python manage.py migrate ;;
        19) 
            echo "تشغيل المشروع بشكل دائم (حتى بعد إعادة تشغيل السيرفر)..."
            # نقوم بتشغيلها مع سياسة إعادة التشغيل
            docker-compose up -d --remove-orphans
            # نحدث الحاويات لضمان بقائها تعمل دائماً
            docker update --restart always $(docker ps -q)
            echo "تم الإعداد. الحاويات ستعمل الآن بشكل دائم."
            ;;
        20) docker-compose ps ;;
        21) echo "Exiting..."; break ;;
        *) echo "خيار غير صحيح." ;;
    esac
    echo
done
