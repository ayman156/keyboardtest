from django import forms
from django.contrib.auth.forms import UserCreationForm, AuthenticationForm
from django.contrib.auth.models import User
from django.utils.translation import gettext_lazy as _

class CustomUserCreationForm(UserCreationForm):
    email = forms.EmailField(
        label=_('البريد الإلكتروني'),
        required=True,
        widget=forms.EmailInput(attrs={'class': 'form-control', 'placeholder': _('أدخل بريدك الإلكتروني')})
    )
    username = forms.CharField(
        label=_('اسم المستخدم'),
        widget=forms.TextInput(attrs={'class': 'form-control', 'placeholder': _('أدخل اسم المستخدم')})
    )
    password1 = forms.CharField(
        label=_('كلمة المرور'),
        widget=forms.PasswordInput(attrs={'class': 'form-control', 'placeholder': _('أدخل كلمة المرور')})
    )
    password2 = forms.CharField(
        label=_('تأكيد كلمة المرور'),
        widget=forms.PasswordInput(attrs={'class': 'form-control', 'placeholder': _('أعد إدخال كلمة المرور')})
    )

    class Meta:
        model = User
        fields = ('username', 'email', 'password1', 'password2')

class CustomAuthenticationForm(AuthenticationForm):
    username = forms.CharField(
        label=_('اسم المستخدم أو البريد الإلكتروني'),
        widget=forms.TextInput(attrs={'class': 'form-control', 'placeholder': _('أدخل اسم المستخدم أو البريد الإلكتروني')})
    )
    password = forms.CharField(
        label=_('كلمة المرور'),
        widget=forms.PasswordInput(attrs={'class': 'form-control', 'placeholder': _('أدخل كلمة المرور')})
    )

class ProfileUpdateForm(forms.ModelForm):
    class Meta:
        model = User
        fields = ('first_name', 'last_name', 'email')
        widgets = {
            'first_name': forms.TextInput(attrs={'class': 'form-control'}),
            'last_name': forms.TextInput(attrs={'class': 'form-control'}),
            'email': forms.EmailInput(attrs={'class': 'form-control'}),
        }
        labels = {
            'first_name': _('الاسم الأول'),
            'last_name': _('الاسم الأخير'),
            'email': _('البريد الإلكتروني'),
        }
