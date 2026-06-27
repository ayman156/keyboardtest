from django.shortcuts import render, redirect
from django.contrib.auth import login, authenticate, logout, update_session_auth_hash
from django.contrib.auth.decorators import login_required
from django.contrib import messages
from django.utils.translation import gettext_lazy as _
from django.urls import reverse_lazy
from django.contrib.auth.views import PasswordResetView
from .forms import (
    CustomUserCreationForm, 
    CustomAuthenticationForm, 
    ProfileUpdateForm,
    CustomPasswordChangeForm
)

def register_view(request):
    """عرض تسجيل مستخدم جديد"""
    if request.user.is_authenticated:
        messages.info(request, _('أنت مسجل الدخول بالفعل!'))
        return redirect('index')
    
    if request.method == 'POST':
        form = CustomUserCreationForm(request.POST)
        if form.is_valid():
            user = form.save()
            
            # تسجيل الدخول تلقائياً بعد التسجيل
            login(request, user)
            
            messages.success(
                request, 
                _('🎉 تم إنشاء حسابك بنجاح! مرحباً بك في نظامنا.')
            )
            return redirect('index')
        else:
            # عرض الأخطاء بشكل مفصل
            for field, errors in form.errors.items():
                for error in errors:
                    messages.error(request, f"{form.fields[field].label}: {error}")
    else:
        form = CustomUserCreationForm()
    
    context = {
        'form': form,
        'title': _('إنشاء حساب جديد'),
        'active_tab': 'register',
    }
    return render(request, 'accounts/register.html', context)

def login_view(request):
    """عرض تسجيل الدخول"""
    if request.user.is_authenticated:
        return redirect('index')
    
    if request.method == 'POST':
        form = CustomAuthenticationForm(request, data=request.POST)
        if form.is_valid():
            user = form.get_user()  # ✅ استخدام get_user() الصحيح
            login(request, user)
            
            messages.success(
                request, 
                _('مرحباً بعودتك، ') + user.get_full_name() or user.username + '! 👋'
            )
            
            # توجيه المستخدم إلى الصفحة التي حاول الوصول إليها
            next_url = request.GET.get('next', 'index')
            return redirect(next_url)
        else:
            messages.error(
                request, 
                _('❌ اسم المستخدم/البريد الإلكتروني أو كلمة المرور غير صحيحة.')
            )
    else:
        form = CustomAuthenticationForm()
    
    context = {
        'form': form,
        'title': _('تسجيل الدخول'),
        'active_tab': 'login',
    }
    return render(request, 'accounts/login.html', context)

def logout_view(request):
    """تسجيل الخروج"""
    if request.user.is_authenticated:
        username = request.user.username
        logout(request)
        messages.info(request, _('تم تسجيل الخروج بنجاح. نراك قريباً! 👋'))
    return redirect('accounts:login')

@login_required
def profile_view(request):
    """عرض وتحديث الملف الشخصي"""
    if request.method == 'POST':
        # التحقق من أي نموذج تم إرساله
        if 'update_profile' in request.POST:
            form = ProfileUpdateForm(request.POST, instance=request.user)
            if form.is_valid():
                form.save()
                messages.success(request, _('✅ تم تحديث الملف الشخصي بنجاح.'))
                return redirect('accounts:profile')
        
        elif 'change_password' in request.POST:
            password_form = CustomPasswordChangeForm(request.user, request.POST)
            if password_form.is_valid():
                user = password_form.save()
                update_session_auth_hash(request, user)  # تحديث الجلسة
                messages.success(request, _('✅ تم تغيير كلمة المرور بنجاح.'))
                return redirect('accounts:profile')
    else:
        form = ProfileUpdateForm(instance=request.user)
        password_form = CustomPasswordChangeForm(request.user)
    
    context = {
        'form': form,
        'password_form': password_form,
        'title': _('الملف الشخصي'),
        'active_tab': 'profile',
    }
    return render(request, 'accounts/profile.html', context)

# إضافة عرض لحذف الحساب
@login_required
def delete_account_view(request):
    """حذف الحساب"""
    if request.method == 'POST':
        # يمكنك إضافة تأكيد بكلمة المرور هنا
        user = request.user
        logout(request)
        user.delete()
        messages.success(request, _('تم حذف حسابك بنجاح. نأمل أن تعود إلينا قريباً!'))
        return redirect('index')
    
    return render(request, 'accounts/delete_account.html', {
        'title': _('حذف الحساب'),
        'active_tab': 'delete',
    })

# الكلاس المخصص لإعادة تعيين كلمة المرور
class CustomPasswordResetView(PasswordResetView):
    template_name = 'accounts/password_reset.html'
    email_template_name = 'accounts/password_reset_email.html'
    subject_template_name = 'accounts/password_reset_subject.txt'
    success_url = reverse_lazy('accounts:password_reset_done')
    
    def form_valid(self, form):
        messages.info(self.request, _('تم إرسال رابط إعادة التعيين إلى بريدك الإلكتروني.'))
        return super().form_valid(form)