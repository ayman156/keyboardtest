#!/bin/bash
# run.sh
# Shell script to show Django and Docker management options

while true; do
    echo "------------------------------------------"
    echo "Select an action:"
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
    echo "13. Import data from CSV files"
    echo "--- Docker Options ---"
    echo "14. Build Docker Images (docker-compose build)"
    echo "15. Run Docker Containers (Up)"
    echo "16. Stop Docker Containers (Down)"
    echo "17. Show Docker Logs"
    echo "18. Create Superuser in Docker"
    echo "19. Exit"
    echo "------------------------------------------"

    read -p "Enter your choice (1-19): " choice

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
            python manage.py show_urls 2>/dev/null || echo "Install django-extensions for this feature."
            ;;
        13)
            python manage.py import_geodata data.csv --format csv
            ;;
        14)
            echo "Building Docker images..."
            docker-compose build
            ;;
        15)
            echo "Starting Docker containers..."
            docker-compose up -d
            echo "Containers are running in background at http://localhost"
            ;;
        16)
            echo "Stopping Docker containers..."
            docker-compose down
            ;;
        17)
            echo "Showing Docker logs (Press Ctrl+C to stop)..."
            docker-compose logs -f
            ;;
        18)
            echo "Creating Superuser inside Docker container..."
            docker-compose exec web python manage.py createsuperuser
            ;;
        19)
            echo "Exiting..."
            break
            ;;
        *)
            echo "Invalid choice."
            ;;
    esac
    echo # سطر فارغ للفصل
done
