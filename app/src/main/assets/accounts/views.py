from django.shortcuts import render, redirect
from django.contrib.auth import login, authenticate, logout
from django.contrib.auth.decorators import login_required
from django.contrib import messages
from django.utils.translation import gettext as _
from .forms import CustomUserCreationForm, CustomAuthenticationForm, ProfileUpdateForm

def register_view(request):
    if request.method == 'POST':
        form = CustomUserCreationForm(request.POST)
        if form.is_valid():
            user = form.save()
            login(request, user)
            messages.success(request, _('تم إنشاء حسابك بنجاح! مرحبًا بك في ' + request.site.name))
            return redirect('index')
        else:
            messages.error(request, _('حدث خطأ في عملية التسجيل. يرجى التحقق من البيانات المدخلة.'))
    else:
        form = CustomUserCreationForm()
    
    context = {
        'form': form,
        'title': _('إنشاء حساب جديد'),
    }
    return render(request, 'accounts/register.html', context)

def login_view(request):
    if request.user.is_authenticated:
        return redirect('index')
    
    if request.method == 'POST':
        form = CustomAuthenticationForm(request, data=request.POST)
        if form.is_valid():
            username = form.cleaned_data.get('username')
            password = form.cleaned_data.get('password')
            user = authenticate(username=username, password=password)
            if user is not None:
                login(request, user)
                messages.success(request, _('مرحبًا بعودتك، ') + user.username + '!')
                next_url = request.GET.get('next', 'index')
                return redirect(next_url)
        else:
            messages.error(request, _('اسم المستخدم أو كلمة المرور غير صحيحة'))
    else:
        form = CustomAuthenticationForm()
    
    context = {
        'form': form,
        'title': _('تسجيل الدخول'),
    }
    return render(request, 'accounts/login.html', context)

def logout_view(request):
    logout(request)
    messages.info(request, _('تم تسجيل الخروج بنجاح'))
    return redirect('accounts:login')

@login_required
def profile_view(request):
    if request.method == 'POST':
        form = ProfileUpdateForm(request.POST, instance=request.user)
        if form.is_valid():
            form.save()
            messages.success(request, _('تم تحديث الملف الشخصي بنجاح'))
            return redirect('profile')
    else:
        form = ProfileUpdateForm(instance=request.user)
    
    context = {
        'form': form,
        'title': _('الملف الشخصي'),
    }
    return render(request, 'accounts/profile.html', context)

def password_reset_view(request):
    context = {'title': _('استعادة كلمة المرور')}
    return render(request, 'accounts/password_reset.html', context)
