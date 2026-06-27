#!/bin/bash
# qrun.sh
# Shell script to show Django management options

while true; do
    echo "Select a Django action:"
    echo "1. Start new Django project"
    echo "2. Start new Django app"
    echo "3. Make migrations"
    echo "4. Apply migrations"
    echo "5. Run development server"
    echo "6. Create superuser"
    echo "7. Export requirements"
    echo "8. Install requirements"
    echo "9. Collect static files"
    echo "10. Show Django version"
    echo "11. Run tests"
    echo "12. Show URLs"
    echo "13. import data from CSV files"
    echo "14. Exit"

    read -p "Enter your choice (1-14): " choice

    case "$choice" in
        1)
            read -p "Enter project name: " pname
            django-admin startproject "$pname"
            ;;
        2)
            read -p "Enter app name: " aname
            python manage.py startapp "$aname"
            ;;
        3)
            python manage.py makemigrations
            ;;
        4)
            python manage.py migrate
            ;;
        5)
            python manage.py runserver
            ;;
        6)
            python manage.py createsuperuser
            ;;
        7)
            pip freeze > requirements.txt
            echo "requirements.txt exported."
            ;;
        8)
            pip install -r requirements.txt
            ;;
        9)
            python manage.py collectstatic
            ;;
        10)
            python -m django --version
            ;;
        11)
            python manage.py test
            ;;
        12)
            python manage.py show_urls 2>/dev/null || echo "Install django-extensions and add to INSTALLED_APPS for this feature."
            ;;
        13)
          python manage.py import_geodata data.csv --format csv
            ;;
        14)
            echo "Exiting..."
            break
            ;;
        *)
            echo "Invalid choice."
            ;;
    esac
    echo # سطر فارغ للفصل بين العمليات
done