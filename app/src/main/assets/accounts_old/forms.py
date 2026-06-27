from django import forms
from django.contrib.auth.forms import UserCreationForm, AuthenticationForm
from django.contrib.auth.models import User
from django.utils.translation import gettext_lazy as _

from django import forms
from django.contrib.auth.forms import (
    UserCreationForm, 
    AuthenticationForm, 
    UserChangeForm,
    PasswordChangeForm
)
from django.contrib.auth.models import User
from django.utils.translation import gettext_lazy as _
from django.core.exceptions import ValidationError
import re

class CustomUserCreationForm(UserCreationForm):
    email = forms.EmailField(
        label=_("البريد الإلكتروني"),
        required=True,
        widget=forms.EmailInput(attrs={
            'class': 'form-control',
            'placeholder': _('example@domain.com'),
            'dir': 'ltr'
        })
    )
    
    first_name = forms.CharField(
        label=_("الاسم الأول"),
        required=False,
        widget=forms.TextInput(attrs={
            'class': 'form-control',
            'placeholder': _('الاسم الأول')
        })
    )
    
    last_name = forms.CharField(
        label=_("اسم العائلة"),
        required=False,
        widget=forms.TextInput(attrs={
            'class': 'form-control',
            'placeholder': _('اسم العائلة')
        })
    )

    class Meta:
        model = User
        fields = ("username", "email", "first_name", "last_name", "password1", "password2")
        widgets = {
            'username': forms.TextInput(attrs={
                'class': 'form-control',
                'placeholder': _('اسم المستخدم')
            }),
        }
        labels = {
            'username': _('اسم المستخدم'),
        }
    
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        # تحسين رسائل المساعدة
        self.fields['password1'].help_text = _(
            '• يجب أن تحتوي على 8 أحرف على الأقل<br>'
            '• لا تكون كلمة مرور شائعة<br>'
            '• لا تكون مؤلفة من أرقام فقط<br>'
            '• لا تكون مشابهة لمعلوماتك الشخصية'
        )
        self.fields['password1'].widget.attrs.update({'class': 'form-control'})
        self.fields['password2'].widget.attrs.update({'class': 'form-control'})
    
    def clean_email(self):
        email = self.cleaned_data.get('email')
        if User.objects.filter(email=email).exists():
            raise ValidationError(_('هذا البريد الإلكتروني مسجل بالفعل.'))
        return email
    
    def clean_username(self):
        username = self.cleaned_data.get('username')
        if not re.match(r'^[\w.@+-]+\Z', username):
            raise ValidationError(_('اسم المستخدم يحتوي على أحرف غير مسموحة.'))
        if User.objects.filter(username=username).exists():
            raise ValidationError(_('اسم المستخدم هذا مسجل بالفعل.'))
        return username

class CustomAuthenticationForm(AuthenticationForm):
    username = forms.CharField(
        label=_("اسم المستخدم أو البريد الإلكتروني"),
        widget=forms.TextInput(attrs={
            'class': 'form-control',
            'placeholder': _('اسم المستخدم أو البريد الإلكتروني')
        })
    )
    password = forms.CharField(
        label=_("كلمة المرور"),
        widget=forms.PasswordInput(attrs={
            'class': 'form-control',
            'placeholder': _('كلمة المرور')
        })
    )
    
    def clean(self):
        cleaned_data = super().clean()
        username = cleaned_data.get('username')
        
        # السماح بتسجيل الدخول باستخدام البريد الإلكتروني أيضاً
        if username and '@' in username:
            try:
                user = User.objects.get(email=username)
                cleaned_data['username'] = user.username
            except User.DoesNotExist:
                pass
                
        return cleaned_data

class ProfileUpdateForm(UserChangeForm):
    password = None  # إزالة حقل كلمة المرور من النموذج
    
    class Meta:
        model = User
        fields = ('username', 'email', 'first_name', 'last_name')
        widgets = {
            'username': forms.TextInput(attrs={
                'class': 'form-control',
                'readonly': 'readonly'  # لا يمكن تغيير اسم المستخدم
            }),
            'email': forms.EmailInput(attrs={
                'class': 'form-control',
                'dir': 'ltr'
            }),
            'first_name': forms.TextInput(attrs={'class': 'form-control'}),
            'last_name': forms.TextInput(attrs={'class': 'form-control'}),
        }
        labels = {
            'username': _('اسم المستخدم'),
            'email': _('البريد الإلكتروني'),
            'first_name': _('الاسم الأول'),
            'last_name': _('اسم العائلة'),
        }
    
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        # جعل اسم المستخدم للقراءة فقط
        self.fields['username'].widget.attrs['readonly'] = True

class CustomPasswordChangeForm(PasswordChangeForm):
    old_password = forms.CharField(
        label=_("كلمة المرور الحالية"),
        widget=forms.PasswordInput(attrs={'class': 'form-control'})
    )
    new_password1 = forms.CharField(
        label=_("كلمة المرور الجديدة"),
        widget=forms.PasswordInput(attrs={'class': 'form-control'})
    )
    new_password2 = forms.CharField(
        label=_("تأكيد كلمة المرور الجديدة"),
        widget=forms.PasswordInput(attrs={'class': 'form-control'})
    )