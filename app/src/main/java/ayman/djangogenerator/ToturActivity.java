package ayman.djangogenerator;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import aym.lib.*;
import com.google.android.material.appbar.AppBarLayout;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

public class ToturActivity extends AppCompatActivity {
	
	private Toolbar _toolbar;
	private AppBarLayout _app_bar;
	private CoordinatorLayout _coordinator;
	
	private ScrollView vscroll1;
	private LinearLayout linear1;
	private TextView textview1;
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		setContentView(R.layout.totur);
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
		_app_bar = findViewById(R.id._app_bar);
		_coordinator = findViewById(R.id._coordinator);
		_toolbar = findViewById(R.id._toolbar);
		setSupportActionBar(_toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		_toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _v) {
				onBackPressed();
			}
		});
		vscroll1 = findViewById(R.id.vscroll1);
		linear1 = findViewById(R.id.linear1);
		textview1 = findViewById(R.id.textview1);
	}
	
	private void initializeLogic() {
		textview1.setText("📱 شرح طريقة استخدام مصمم تقارير Django\n\n📖 نظرة عامة\n\nهذا التطبيق يسمح لك بتصميم استعلامات Django بصرياً وإنشاء كود جاهز للاستخدام في مشاريع Django الخاصة بك.\n\n🚀 خطوات الاستخدام\n\n1️⃣ بدء تصميم تقرير جديد\n\nأولاً: معلومات التقرير\n\n1. اسم التقرير: أدخل اسم واضح للتقرير (مطلوب)\n2. وصف التقرير: اكتب وصفاً مختصراً لما يحتويه التقرير\n\nhttps://via.placeholder.com/400x100/2196F3/FFFFFF?text=معلومات+التقرير\n\n---\n\n2️⃣ اختيار النموذج الأساسي\n\nاختيار النموذج\n\n1. اختر النموذج من القائمة المنسدلة\n2. سيقوم التطبيق تلقائياً:\n   · تحميل جميع حقول النموذج\n   · عرض العلاقات المرتبطة (ForeignKeys, ManyToMany)\n\nhttps://via.placeholder.com/400x100/4CAF50/FFFFFF?text=اختيار+النموذج\n\n---\n\n3️⃣ تخصيص الحقول\n\nإدارة الحقول في القائمة\n\n1. الحقول المتاحة: ستظهر جميع حقول النموذج في قائمة\n2. تحديد الحقول: استخدم CheckBox لتحديد الحقول المراد تضمينها في التقرير\n3. خيارات الحقل: انقر على أي حقل لفتح نافذة الخيارات\n\nخيارات كل حقل:\n\n· ✔️ تضمين في التقرير: إظهار/إخفاء الحقل\n· 📊 Group By: استخدام الحقل للتجميع\n· ⬆️⬇️ Order By: ترتيب النتائج حسب هذا الحقل\n· ↕️ اتجاه الترتيب: تصاعدي (ASC) أو تنازلي (DESC)\n\nhttps://via.placeholder.com/400x150/FF9800/FFFFFF?text=تخصيص+الحقول\n\n---\n\n4️⃣ إضافة العلاقات (Relations)\n\nإدارة العلاقات\n\n1. العلاقات المرتبطة: ستظهر تلقائياً عند اختيار النموذج\n2. اختر العلاقات: حدد العلاقات التي تريد تضمينها\n3. أنواع العلاقات:\n   · ForeignKey → ستستخدم select_related()\n   · ManyToManyField → ستستخدم prefetch_related()\n\nمثال عملي:\n\n```\n✅ Workshop (النموذج الأساسي)\n   ├── ✅ participants (ManyToMany → Employee)\n   ├── ✅ trainer (ForeignKey → Employee)\n   └── ✅ department (ForeignKey → Department)\n```\n\nhttps://via.placeholder.com/400x100/9C27B0/FFFFFF?text=العلاقات+المرتبطة\n\n---\n\n5️⃣ إضافة دوال التجميع (Annotate)\n\nإضافة Annotate جديدة\n\n1. انقر على \"إضافة\" في قسم Annotate\n2. املأ البيانات:\n   · 👁️ اسم النتيجة: اسم المتغير الذي سيخزن النتيجة (مثال: total_count)\n   · 🔤 اسم الحقل: الحقل المراد تطبيق الدالة عليه (مثال: id)\n   · ⚙️ نوع الدالة: اختر من القائمة:\n     · Count - عد العناصر\n     · Sum - جمع القيم\n     · Avg - حساب المتوسط\n     · Max - القيمة القصوى\n     · Min - القيمة الدنيا\n\nأمثلة Annotate:\n\n```\n👁️ اسم النتيجة: employee_count\n🔤 اسم الحقل: employee\n⚙️ نوع الدالة: Count\n\n👁️ اسم النتيجة: total_salary\n🔤 اسم الحقل: salary\n⚙️ نوع الدالة: Sum\n```\n\nhttps://via.placeholder.com/400x150/00BCD4/FFFFFF?text=دوال+التجميع\n\n---\n\n6️⃣ إضافة الفلاتر (Filters)\n\nإضافة فلتر جديد\n\n1. انقر على \"إضافة\" في قسم الفلاتر\n2. املأ البيانات:\n   · 📝 اسم الحقل: الحقل المراد تصفيته\n   · ⚖️ المشغل: اختر المشغل المناسب:\n     · = - يساوي\n     · != - لا يساوي\n     · > - أكبر من\n     · < - أصغر من\n     · contains - يحتوي على نص\n     · startswith - يبدأ بـ\n     · isnull - فارغ/غير فارغ\n   · 🔢 القيمة: القيمة المراد المقارنة بها\n\nأمثلة الفلاتر:\n\n```\n📝 اسم الحقل: status\n⚖️ المشغل: =\n🔢 القيمة: active\n\n📝 اسم الحقل: created_at\n⚖️ المشغل: >\n🔢 القيمة: 2024-01-01\n\n📝 اسم الحقل: name\n⚖️ المشغل: contains\n🔢 القيمة: أحمد\n```\n\nhttps://via.placeholder.com/400x150/F44336/FFFFFF?text=الفلاتر\n\n---\n\n7️⃣ الخيارات الإضافية\n\nتخصيص الاستعلام\n\n1. Group By: ✅ تفعيل التجميع حسب الحقول المحددة\n2. Order By: ✅ تفعيل الترتيب حسب الحقول المحددة\n3. Distinct: ✅ إزالة التكرارات من النتائج\n4. Limit: 🔢 تحديد عدد النتائج (مثال: 100)\n5. Offset: 🔢 تخطي عدد من النتائج (مثال: 50)\n\nhttps://via.placeholder.com/400x150/795548/FFFFFF?text=خيارات+إضافية\n\n---\n\n8️⃣ إنشاء الكود\n\nإنشاء استعلام Django\n\n1. انقر على \"إنشاء استعلام Django\"\n2. سيعرض التطبيق الكود الناتج\n3. يمكنك نسخ الكود للاستخدام\n\nمثال على الكود الناتج:\n\n```python\n# تقرير: تقرير الموظفين\n# تقرير مفصل للموظفين النشطين\n\nfrom django.db.models import Count, Sum, Avg, Max, Min, Q, F\n\nquery = Employee.objects\n    .select_related('department')\n    .prefetch_related('projects')\n    .filter(\n        status='active',\n        salary__gt=5000\n    )\n    .annotate(\n        project_count=Count('projects'),\n        total_bonus=Sum('bonus')\n    )\n    .order_by('-salary')\n    [:50]\n\n# الاستعلام جاهز للاستخدام\n```\n\nhttps://via.placeholder.com/400x100/2196F3/FFFFFF?text=إنشاء+استعلام+Django\n\n---\n\n9️⃣ تصدير الكود المتقدم\n\nتصدير إلى Excel\n\n1. انقر على \"كود Excel\"\n2. ستحصل على كود Python جاهز لتصدير البيانات إلى Excel\n3. مميزات الكود:\n   · تلقائي مع التنسيق\n   · دعم اللغة العربية\n   · أسماء الملفات بتواريخ\n   · إمكانية التخصيص\n\nكود Excel النموذجي:\n\n```python\ndef export_to_excel(request, queryset):\n    import pandas as pd\n    from django.http import HttpResponse\n    from django.utils import timezone\n    \n    filename = f'employees_report_{timezone.now().strftime(\"%Y%m%d\")}.xlsx'\n    response = HttpResponse(content_type='application/vnd.ms-excel')\n    response['Content-Disposition'] = f'attachment; filename=\"{filename}\"'\n    \n    df = pd.DataFrame(list(queryset.values()))\n    df.to_excel(response, index=False)\n    \n    return response\n```\n\nhttps://via.placeholder.com/400x100/4CAF50/FFFFFF?text=كود+Excel\n\n---\n\nتصدير إلى PDF\n\n1. انقر على \"كود PDF\"\n2. ستحصل على كود Python جاهز لتصدير البيانات إلى PDF\n3. مميزات الكود:\n   · دعم النصوص العربية\n   · تنسيق جدول احترافي\n   · رؤوس وألوان مميزة\n   · صفحات متعددة تلقائياً\n\nكود PDF النموذجي:\n\n```python\ndef export_to_pdf(request, queryset):\n    from reportlab.pdfgen import canvas\n    from reportlab.lib.pagesizes import A4\n    from reportlab.lib import colors\n    from reportlab.platypus import Table, TableStyle\n    from django.http import HttpResponse\n    \n    response = HttpResponse(content_type='application/pdf')\n    response['Content-Disposition'] = 'attachment; filename=\"report.pdf\"'\n    \n    # إنشاء PDF مع البيانات\n    p = canvas.Canvas(response, pagesize=A4)\n    \n    # رسم الجدول\n    data = [['الاسم', 'القسم', 'الراتب']]\n    for emp in queryset:\n        data.append([emp.name, emp.department.name, emp.salary])\n    \n    table = Table(data)\n    table.setStyle(TableStyle([\n        ('BACKGROUND', (0, 0), (-1, 0), colors.grey),\n        ('GRID', (0, 0), (-1, -1), 1, colors.black)\n    ]))\n    \n    # حفظ PDF\n    p.save()\n    return response\n```\n\nhttps://via.placeholder.com/400x100/F44336/FFFFFF?text=كود+PDF\n\n---\n\n🔟 معاينة القالب\n\nمعاينة قالب HTML\n\n1. انقر على \"معاينة القالب\"\n2. ستحصل على قالب HTML كامل للعرض في Django\n3. مميزات القالب:\n   · متجاوب مع Bootstrap\n   · أزرار تصدير Excel/PDF\n   · تصميم عربي محترف\n   · سهلة التخصيص\n\nمقطع من القالب الناتج:\n\n```html\n<div class=\"container mt-4\">\n    <div class=\"report-header p-4 rounded shadow\">\n        <h2>تقرير الموظفين</h2>\n        <p>تقرير شامل للموظفين النشطين</p>\n    </div>\n    \n    <div class=\"card mt-4 shadow\">\n        <div class=\"card-header bg-white\">\n            <h5 class=\"mb-0\">البيانات</h5>\n            <div class=\"export-buttons\">\n                <a href=\"#\" class=\"btn btn-success btn-sm\">\n                    <i class=\"fas fa-file-excel\"></i> Excel\n                </a>\n                <a href=\"#\" class=\"btn btn-danger btn-sm\">\n                    <i class=\"fas fa-file-pdf\"></i> PDF\n                </a>\n            </div>\n        </div>\n        \n        <div class=\"card-body\">\n            <table class=\"table table-striped table-hover\">\n                <!-- البيانات ستظهر هنا -->\n            </table>\n        </div>\n    </div>\n</div>\n```\n\nhttps://via.placeholder.com/400x100/00BCD4/FFFFFF?text=معاينة+القالب\n\n---\n\n🎯 أمثلة عملية كاملة\n\nالمثال 1: تقرير ورش العمل\n\nالإعدادات:\n\n1. النموذج: Workshop\n2. الحقول المختارة: title, start_date, location, status\n3. العلاقات: trainer (select_related), participants (prefetch_related)\n4. Annotate: participant_count = Count('participants')\n5. Filter: status = 'completed'\n6. Order By: start_date تنازلي\n\nالكود الناتج:\n\n```python\nfrom django.db.models import Count\n\nquery = Workshop.objects\n    .select_related('trainer')\n    .prefetch_related('participants')\n    .filter(status='completed')\n    .annotate(participant_count=Count('participants'))\n    .order_by('-start_date')\n```\n\n---\n\nالمثال 2: تقرير المبيعات\n\nالإعدادات:\n\n1. النموذج: Sale\n2. الحقول المختارة: product, quantity, price, date\n3. العلاقات: customer (select_related), products (prefetch_related)\n4. Annotate: total = F('quantity') * F('price')\n5. Group By: product, date\n6. Filter: date >= '2024-01-01'\n\n---\n\n📋 نصائح وإرشادات\n\n💡 نصائح للاستخدام الفعال:\n\n1. ابدأ بسيطاً: أضف عناصر واحدة تلو الأخرى\n2. اختبر الكود: انسخ الكود وقم بتجربته في Django Shell أولاً\n3. استخدم الأسماء الواضحة: خاصة في أسماء Annotate\n4. احفظ التصاميم: يمكنك تطويرها لاحقاً\n\n🔧 استكشاف الأخطاء وإصلاحها:\n\n1. إذا لم تظهر الحقول: تأكد من اختيار النموذج أولاً\n2. إذا ظهر خطأ في الكود: تحقق من أسماء الحقول والعلاقات\n3. إذا لم يعمل التصدير: تأكد من تثبيت المكتبات المطلوبة (pandas, reportlab)\n\n🚀 الخطوات التالية:\n\n1. نسخ الكود ولصقه في views.py\n2. تعديل القالب حسب احتياجاتك\n3. إضافة Authentication إذا كان التقرير خاصاً\n4. إضافة Pagination للبيانات الكبيرة\n\n---\n\n🎨 تخصيص إضافي\n\nإضافة خيارات متقدمة:\n\n1. TruncDate: لتجميع البيانات حسب اليوم/الشهر/السنة\n2. Conditional Annotate: annotate مع شروط\n3. Multiple Ordering: ترتيب بأكثر من حقل\n4. Subqueries: استعلامات فرعية متقدمة\n\nدمج مع مشاريع حقيقية:\n\n1. أضف الكود إلى views.py في مشروع Django\n2. أنشئ URLs للتقارير\n3. أضف صلاحيات باستخدام decorators\n4. خزن النتائج في cache للأداء\n\n---\n\n📞 المساعدة والدعم\n\nإذا واجهت مشكلة:\n\n1. تأكد من أن النموذج يحتوي على حقول\n2. تحقق من أسماء الحقول في قاعدة البيانات\n3. استخدم البيانات التجريبية أولاً\n4. راجع الأخطاء في Logcat\n\nللأسئلة الشائعة:\n\n· س: لماذا لا تظهر العلاقات؟\n· ج: تأكد أن النموذج يحتوي على ForeignKey أو ManyToManyField\n· س: كيف أختبر الكود الناتج؟\n· ج: استخدم python manage.py shell لتجربة الاستعلام\n\n---\n\n✅ ملخص سريع\n\n1. أدخل معلومات التقرير 📝\n2. اختر النموذج الأساسي 🎯\n3. حدد الحقول والعلاقات ✅\n4. أضف Annotate و Filters ⚙️\n5. اضبط الخيارات الإضافية 🔧\n6. أنشئ الكود ونسخه 📋\n7. استخدم في مشروع Django 🚀\n\nبهذه الطريقة، يمكنك إنشاء تقارير Django معقدة في دقائق بدلاً من ساعات! 🎉");
	}
	
	
	@Deprecated
	public void showMessage(String _s) {
		Toast.makeText(getApplicationContext(), _s, Toast.LENGTH_SHORT).show();
	}
	
	@Deprecated
	public int getLocationX(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[0];
	}
	
	@Deprecated
	public int getLocationY(View _v) {
		int _location[] = new int[2];
		_v.getLocationInWindow(_location);
		return _location[1];
	}
	
	@Deprecated
	public int getRandom(int _min, int _max) {
		Random random = new Random();
		return random.nextInt(_max - _min + 1) + _min;
	}
	
	@Deprecated
	public ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
		ArrayList<Double> _result = new ArrayList<Double>();
		SparseBooleanArray _arr = _list.getCheckedItemPositions();
		for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
			if (_arr.valueAt(_iIdx))
			_result.add((double)_arr.keyAt(_iIdx));
		}
		return _result;
	}
	
	@Deprecated
	public float getDip(int _input) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, getResources().getDisplayMetrics());
	}
	
	@Deprecated
	public int getDisplayWidthPixels() {
		return getResources().getDisplayMetrics().widthPixels;
	}
	
	@Deprecated
	public int getDisplayHeightPixels() {
		return getResources().getDisplayMetrics().heightPixels;
	}
}