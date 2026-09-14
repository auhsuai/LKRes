# LKRes — Runbook vận hành (đọc trước khi build / cài / verify)

> File này là ghi chú riêng của project, bổ sung cho protocol global
> (`C:\Users\admin\.config\opencode\AGENTS.md`) — session sau đọc và làm theo.

## Môi trường máy dev
- Máy yếu (i5-3570, 8GB RAM, thường đang mở browser/terminal) → **KHÔNG build local** (Gradle spike ~2-3GB RAM). Máy **không có JDK / Android SDK / gradle wrapper** (repo cố ý như vậy).
- **Build + unit test CHỈ qua GitHub Actions**: push lên `main` → workflow `android-ci` (chạy `:app:testDebugUnitTest` + `:app:assembleDebug`). Repo: https://github.com/auhsuai/LKRes (public). Artifact: `LKRes-debug-apk`.
- adb đã cài sẵn: `E:\tools\platform-tools\adb.exe`.
- APK tải từ CI về đặt tại: `E:\LKRes-build\app-debug.apk`.

## Vòng lặp sửa → xem trên điện thoại (~3-4 phút, 0 RAM máy)
1. Sửa code → commit → `git push`.
2. Poll CI (khớp `head_sha`, 30s/lần, tối đa 15 phút, chờ `status=completed`):
```powershell
$h=@{ "User-Agent"="lkres" }
$runs=Invoke-RestMethod "https://api.github.com/repos/auhsuai/LKRes/actions/runs?per_page=5" -Headers $h
$r=$runs.workflow_runs | Where-Object { $_.head_sha -like "<sha>*" } | Select-Object -First 1
```
3. Tải artifact APK (API cần auth — dùng credential git đã lưu, **TUYỆT ĐỐI không in token ra log**):
```powershell
$cred=("protocol=https`nhost=github.com`n`n" | git credential fill 2>$null | Out-String)
# lấy username/password → Basic base64 → GET actions/runs/{runId}/artifacts → artifact id
# → GET actions/artifacts/{id}/zip → Invoke-WebRequest -OutFile → Expand-Archive ra E:\LKRes-build\
```
4. Cài vào điện thoại (mục dưới).

## Cài qua Wi-Fi (adb wireless) — cách chuẩn, KHÔNG cần cáp
1. Trên điện thoại (iQOO Z9 Turbo): **Cài đặt → Hệ thống → Tùy chọn nhà phát triển → Gỡ lỗi không dây** → bật ON.
2. Bấm **"Ghép nối thiết bị bằng mã ghép nối"** → lấy `IP:CỔNG_PAIR` + mã 6 số (mã hết hạn nhanh).
3. `& E:\tools\platform-tools\adb.exe pair <IP:CỔNG_PAIR> <MÃ>` → "Successfully paired".
4. Lấy cổng connect (KHÁC cổng pair, đổi mỗi phiên): `adb mdns services` → dòng `_adb-tls-connect._tcp` cho ra `IP:CỔNG_CONNECT`.
5. `adb connect <IP:CỔNG_CONNECT>` → `adb devices` phải thấy `device`.
6. Nếu trùng 2 entry (qua IP + qua mDNS) → thêm `-s <IP:CỔNG_CONNECT>` vào MỌI lệnh adb.
7. Cài + mở:
```
adb -s <IP:PORT> uninstall com.lkres.app        # BẮT BUỘC nếu đã có bản cũ (xem Bẫy keystore)
adb -s <IP:PORT> install E:\LKRes-build\app-debug.apk
adb -s <IP:PORT> shell am start -n com.lkres.app/.MainActivity
```
8. Rớt kết nối (điện thoại ngủ / khoá Wi-Fi / đổi IP DHCP) → chạy lại bước 4-5. Nếu cổng connect đổi thì lấy lại từ `adb mdns services` hoặc màn hình Wireless debugging.

## Bẫy đã gặp (quan trọng)
- **Mỗi run CI ký APK bằng debug keystore mới** → cài đè báo `INSTALL_FAILED_UPDATE_INCOMPATIBLE` → phải `uninstall` trước khi `install` (mất dữ liệu app: mode, setting, trạng thái đã lưu). Muốn update giữ nguyên setting: cần thêm keystore debug cố định vào repo + cấu hình `signingConfig` (chưa làm — đề xuất khi cần).
- Cáp USB với iQOO báo `Device Descriptor Request Failed` (lỗi cáp/cổng, không phải driver) → ưu tiên đường Wi-Fi.
- Artifact API không cho tải anonymous (401) — phải auth bằng credential git đã lưu; **không log token**.
- Wireless debugging: cổng pair ≠ cổng connect; IP + cổng đổi mỗi phiên; pairing vẫn nhớ sau khi kết nối lại (thường chỉ cần `adb connect`).

## Preview UI nhanh (0 RAM, không cần build — dùng khi chỉ tinh chỉnh hình/text)
- Mirror đúng toán học Canvas Kotlin vào `.superpowers/sdd/canvas-preview.html` → chụp bằng Playwright MCP (`browser_run_code_unsafe` + `page.goto('file:///...')` vì navigate tool chặn `file:`) → PNG `canvas-preview-vN.png` để user xem trước rồi mới chốt.
- HTML phải khớp Kotlin, KHÔNG sửa Kotlin cho khớp HTML.

## Địa chỉ khác
- Plan đang chạy: `docs/superpowers/plans/2026-09-14-lkres-v1.2.md`.
- Report từng task: `.superpowers/sdd/task-N-report.md` (gitignored).
- `core/ValueToColors.kt` có mojibake sẵn trong comment (pre-existing) — không đụng.
