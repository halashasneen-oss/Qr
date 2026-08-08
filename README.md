# QR Pro

تطبيق Android احترافي لمسح وإنشاء أكواد QR — Kotlin + XML + MVVM (بدون Jetpack Compose، بدون Room، بدون Hilt، حسب المواصفات).

## المكدّس التقني
- Kotlin + View Binding
- CameraX + ML Kit Barcode Scanning (مسح سريع بالكامل offline)
- ZXing (توليد أكواد QR وتخصيصها)
- Navigation Component
- MVVM + Repository Pattern (تخزين JSON قابل للاستبدال بقاعدة بيانات لاحقًا)
- Biometric API (قفل الخزنة بالبصمة)

## بناء APK بدون جهاز كمبيوتر (عبر GitHub Actions من الموبايل)

1. أنشئ مستودع جديد فاضي على GitHub (مثلاً `qr-pro`).
2. من Termux على الموبايل:
   ```bash
   pkg install git -y
   cd qrpro-project   # داخل مجلد المشروع بعد فك الضغط
   git init
   git add .
   git commit -m "QR Pro v1.0 - initial project"
   git branch -M main
   git remote add origin https://github.com/USERNAME/qr-pro.git
   git push -u origin main
   ```
3. روح لتبويب **Actions** بمستودعك على GitHub، رح تلاقي الـ workflow **Build QR Pro APK** يشتغل تلقائيًا.
4. لما يخلص (يأخذ عادة 3-6 دقائق)، افتح الـ run وانزل لقسم **Artifacts**، حمّل **QRPro-debug-apk**.
5. فك الضغط عن الملف المضغوط وثبّت `app-debug.apk` على جهازك.

## هيكلة المشروع
```
app/src/main/java/com/khaled/qrpro/
  data/       -> Models + Repositories + JSON Storage
  ui/         -> شاشة لكل ميزة (home, scan, result, create, history, vault, settings)
  util/       -> أدوات مشتركة (parser, extensions)
```

## ملاحظات النسخة 1.0
- لا توجد أي ميزات ذكاء اصطناعي بهذه النسخة (مؤجلة للإصدار 2.0 حسب الخطة).
- التخزين محلي بالكامل عبر JSON + SharedPreferences، لا حاجة لإنترنت في أي ميزة أساسية.
- طبقة الـ Repository تسمح باستبدال JSON بقاعدة بيانات حقيقية لاحقًا دون تعديل الشاشات.
