from django.urls import path
from django.contrib.auth import views as auth_views
from . import views
from .views import CustomPasswordResetView

app_name = 'accounts'

urlpatterns = [
    # المصادقة الأساسية
    path('register/', views.register_view, name='register'),
    path('login/', views.login_view, name='login'),
    path('logout/', views.logout_view, name='logout'),
    
    # الملف الشخصي
    path('profile/', views.profile_view, name='profile'),
    path('profile/delete/', views.delete_account_view, name='delete_account'),
    
    # إعادة تعيين كلمة المرور
    path('password-reset/', 
         CustomPasswordResetView.as_view(), 
         name='password_reset'),
    
    path('password-reset/done/', 
         auth_views.PasswordResetDoneView.as_view(
             template_name='accounts/password_reset_done.html'
         ), 
         name='password_reset_done'),
    
    path('password-reset-confirm/<uidb64>/<token>/', 
         auth_views.PasswordResetConfirmView.as_view(
             template_name='accounts/password_reset_confirm.html',
             success_url='/accounts/password-reset-complete/'
         ), 
         name='password_reset_confirm'),
    
    path('password-reset-complete/', 
         auth_views.PasswordResetCompleteView.as_view(
             template_name='accounts/password_reset_complete.html'
         ), 
         name='password_reset_complete'),
    
    # تغيير كلمة المرور (للمستخدم المسجل)
    path('password-change/', 
         auth_views.PasswordChangeView.as_view(
             template_name='accounts/password_change.html',
             success_url='/accounts/profile/'
         ), 
         name='password_change'),
]