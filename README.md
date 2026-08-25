# LKRes

App Android tra cứu điện trở: dải màu 3–6 (có hình mô phỏng trực quan),
mã SMD (3–4 số, R-notation, EIA-96), bảng tham khảo. Offline 100%.

## Build
Mở bằng Android Studio (JDK 17) hoặc: `gradle :app:assembleDebug`

## Test
`gradle :app:testDebugUnitTest`

CI: GitHub Actions tự build APK mỗi push, tải artifact `LKRes-debug-apk`.
