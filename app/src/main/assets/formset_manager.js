// formset_manager.js
// إدارة Formsets ديناميكياً - نسخة متوافقة مع الجداول الأفقية

class FormsetManager {
    constructor(prefix, options = {}) {
        this.prefix = prefix;
        this.containerId = `id_${prefix}-forms`;
        this.totalFormsId = `id_${prefix}-TOTAL_FORMS`;
        this.emptyFormId = `${prefix}-empty-form`; // معرف القالب الفارغ
        this.options = {
            animationDuration: 200,
            confirmDelete: true,
            ...options
        };
        this.init();
    }
    
    init() {
        this.container = document.getElementById(this.containerId);
        this.totalForms = document.getElementById(this.totalFormsId);
        this.emptyFormTemplate = document.getElementById(this.emptyFormId);
        this.bindEvents();
    }
    
    // تحديث الأرقام التسلسلية لـ Django (TOTAL_FORMS)
    updateTotalForms() {
        if (this.totalForms && this.container) {
            // ملاحظة: Django يحتاج عدد النماذج الكلي (بما فيها المحذوفة مخفياً)
            const rows = this.container.querySelectorAll('.formset-row').length;
            this.totalForms.value = rows;
        }
    }
    
    // إضافة صف جديد باستخدام empty_form
    addRow() {
        if (!this.container || !this.emptyFormTemplate || !this.totalForms) return;
        
        const formIndex = parseInt(this.totalForms.value);
        let rowHtml = this.emptyFormTemplate.innerHTML.replace(/__prefix__/g, formIndex);
        
        // تحويل النص إلى عنصر TR
        const tempTable = document.createElement('table');
        tempTable.innerHTML = `<tbody>${rowHtml}</tbody>`;
        const newRow = tempTable.querySelector('tr');
        
        // إضافة معرف فريد للسطر ليسهل الوصول إليه
        newRow.id = `formset-row-${this.prefix}-${formIndex}`;
        
        // إعدادات التأثير البصري
        newRow.style.opacity = '0';
        newRow.style.transition = `opacity ${this.options.animationDuration}ms`;
        
        this.container.appendChild(newRow);
        
        // تفعيل التأثير
        setTimeout(() => {
            newRow.style.opacity = '1';
        }, 10);
        
        // تحديث العداد الإجمالي
        this.totalForms.value = formIndex + 1;
    }
    
    // حذف صف
    removeRow(rowElement) {
        if (!rowElement) return;
        
        // ابحث عن حقل DELETE (الذي يوفره Django Crispy)
        const deleteInput = rowElement.querySelector('input[name$="-DELETE"]');
        
        if (deleteInput) {
            // حالة السجل الموجود مسبقاً في قاعدة البيانات
            if (this.options.confirmDelete && !confirm('{% trans "هل أنت متأكد من حذف هذا السجل؟" %}')) {
                return;
            }
            deleteInput.checked = true;
            rowElement.style.display = 'none';
            rowElement.classList.add('deleted-row');
        } else {
            // حالة السجل الجديد الذي لم يحفظ بعد
            rowElement.style.opacity = '0';
            setTimeout(() => {
                rowElement.remove();
                // اختياري: تحديث TOTAL_FORMS هنا قد يسبب مشاكل في تسلسل الفهارس
                // لذا يفضل ترك الفهرس كما هو وDjango سيعالج الفجوات
            }, this.options.animationDuration);
        }
    }
    
    bindEvents() {
        // حدث إضافة سطر (مربوط بزر الإضافة الرئيسي)
        const addBtn = document.querySelector(`.add-formset-row[data-formset-prefix="${this.prefix}"]`);
        if (addBtn) {
            addBtn.addEventListener('click', () => this.addRow());
        }

        // حدث حذف سطر (تفويض الأحداث لجدول الـ Formset)
        if (this.container) {
            this.container.addEventListener('click', (e) => {
                const removeBtn = e.target.closest('.remove-formset-row');
                if (removeBtn) {
                    const row = removeBtn.closest('.formset-row');
                    this.removeRow(row);
                }
            });
        }
    }
}

// تهيئة جميع الـ Formsets تلقائياً
document.addEventListener('DOMContentLoaded', function() {
    window.formsetManagers = {};
    
    document.querySelectorAll('[id$="-forms"]').forEach(container => {
        const id = container.id;
        const prefix = id.replace('id_', '').replace('-forms', '');
        
        // إنشاء مدير لكل Formset موجود في الصفحة
        window.formsetManagers[prefix] = new FormsetManager(prefix);
    });
});
