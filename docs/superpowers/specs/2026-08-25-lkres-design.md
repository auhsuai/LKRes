# LKRes — Design Document

**Ngày:** 2026-08-25
**Trạng thái:** Chờ user duyệt

## 1. Tổng quan

LKRes là app Android thuần (offline) để **tra cứu điện trở**:

1. **Tab Dải màu (lõi):** chọn màu từng dải (3–6 dải) → app tính và hiển thị giá trị điện trở kèm hình trở mô phỏng cập nhật trực quan theo màu đã chọn.
2. **Tab SMD:** nhập mã in trên điện trở SMD (`472`, `4R7`, `4702`, `01C`) → hiện giá trị.
3. **Tab Tham khảo:** bảng màu chuẩn, bảng hệ số nhân, bảng dung sai, bảng TCR, bảng EIA-96.

**Không phải** app học tập/luyện tập. Không có backend, không xin bất kỳ quyền nào (camera/internet/storage), chạy offline 100%.

## 2. Công nghệ

| Hạng mục | Lựa chọn |
|---|---|
| Ngôn ngữ | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Kiến trúc | Single Activity + Navigation Compose + bottom navigation 3 tab |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | Mới nhất theo bản Android Studio hiện hành (xác nhận lại khi setup) |
| State | Compose state thuần (không cần ViewModel phức tạp, không cần DB) |
| Dependency ngoài | Tối thiểu — chỉ Compose BOM, Navigation, Activity Compose |

Lý do chọn native: app nhỏ, offline, không cần mở rộng iOS, APK nhẹ.

## 3. Tab Dải màu (màn hình lõi)

### 3.1 Layout

- **Segmented button** chọn số dải: 3 / 4 / 5 / 6 (mặc định 4).
- **Hình điện trở mô phỏng** nằm ngang giữa màn hình (chi tiết visual ở mục 4).
- **Hàng picker màu** theo từng dải: mỗi dải 1 hàng chip màu, tap là cập nhật hình + giá trị tức thì. Số hàng picker = số dải đã chọn.
- **Kết quả:** giá trị định dạng kỹ thuật (`4.7 kΩ ±5%`), kèm TCR (ppm/°C) nếu là 6 dải.
- **Cảnh báo nhẹ** (banner nhỏ, không chặn) khi tổ hợp hiếm gặp trong thực tế: digit đầu là đen/trắng, hoặc dải dung sai chọn trắng.

### 3.2 Quy tắc vị trí dải theo số dải

| Số dải | Digit | Multiplier | Tolerance | TCR |
|---|---|---|---|---|
| 3 | 2 dải đầu | dải 3 | ngầm định ±20% | — |
| 4 | 2 dải đầu | dải 3 | dải 4 | — |
| 5 | 3 dải đầu | dải 4 | dải 5 | — |
| 6 | 3 dải đầu | dải 4 | dải 5 | dải 6 |

### 3.3 Palette màu

12 màu chung: đen, nâu, đỏ, cam, vàng, lục, lam, tím, xám, trắng, vàng kim, bạc.
- Dải digit/multiplier: 10 màu đầu + vàng kim/bạc (multiplier).
- Dải tolerance: nâu, đỏ, lục, lam, tím, xám, vàng kim, bạc (không có đen/cam/vàng/trắng).
- Dải TCR (dải 6): bảng TCR riêng — xác minh bảng chuẩn khi triển khai (nguồn DuckDuckGo/context7, Luật 3).

Mỗi chip màu hiển thị đúng màu vật lý (vàng kim/bạc vẽ gradient kim loại, không tô phẳng).

## 4. Spec hình trở mô phỏng (Canvas)

Vẽ 100% bằng **Compose Canvas** — không dùng file ảnh. Mẫu tham chiếu: ảnh điện trở qua lỗ thật (thân be, chân bạc) — **nằm ngang thẳng**, không xéo.

Các lớp vẽ (thứ tự từ dưới lên):

1. **2 chân kim loại:** đường thẳng bạc, gradient ngang kim loại, đầu xa hơi mờ/nhỏ dần.
2. **Thân trở màu be** (base ~`#E8D5A3`): dáng cổ điển — giữa phình tròn, 2 đầu cong thắt lại vào chân (path cong kiểu "đậu phộng", không phải hình chữ nhật bo góc).
3. **Gradient dọc thân:** sáng trên → tối giữa-dưới → dải phản chiếu sáng dưới → tạo khối trụ 3D.
4. **Vệt bóng men:** dải sáng mờ chạy dọc phần trên thân.
5. **Dải màu:** tô đè lên thân, mỗi dải là dải dọc cong theo thân; nhân gradient bóng của thân (sáng/tối theo độ cong) để dải "quấn" quanh trụ chứ không phải sticker phẳng. Vị trí các dải phân bố đều trong vùng thân phình, tính theo tỷ lệ % chiều rộng thân → tự co giãn cho đủ 3–6 dải.
6. **Bóng đổ nhẹ** dưới thân (tùy chọn, tăng chiều sâu).

Yêu cầu chất lượng: scale theo mọi kích thước màn hình không vỡ (vector), trạng thái "trắng trơn" khi chưa chọn gì = thân be + chân bạc, không có dải màu.

## 5. Tab SMD

- Ô nhập code text → nút/nhập xong tự tính → hiện giá trị.
- Định dạng hỗ trợ:
  - **3 chữ số:** `472` = 47 × 10² = 4.7 kΩ. `0` = jumper 0Ω.
  - **4 chữ số:** `4702` = 470 × 10² = 47 kΩ.
  - **Ký tự R (dấu thập phân):** `4R7` = 4.7Ω, `0R22` = 0.22Ω.
  - **EIA-96:** 2 số + chữ cái, `01C` = 100 × 100 = 10 kΩ (tra bảng EIA-96 đầy đủ).
- Kết quả hiển thị cùng định dạng kỹ thuật như tab Dải màu.

### 5.1 Phân loại lỗi nhập (bắt buộc, không crash)

| Trường hợp | Thông báo |
|---|---|
| Rỗng | "Nhập mã SMD cần tra" |
| Ký tự không hợp lệ | "Mã chỉ gồm chữ số, chữ R, và mã EIA-96" |
| Độ dài lạ (2 số, >4 số…) | "Độ dài mã không nhận diện được (3–4 số, R-notation, hoặc EIA-96)" |
| EIA-96: cặp số/chữ không có trong bảng | "Mã EIA-96 không tồn tại trong bảng chuẩn" |

## 6. Tab Tham khảo

Các bảng tĩnh, cuộn dọc, style Material 3:

1. **Bảng màu chuẩn:** 12 hàng (màu + tên + giá trị digit + multiplier + tolerance tương ứng).
2. **Bảng hệ số nhân:** ×0.01 (bạc) → ×1G (trắng).
3. **Bảng dung sai:** từ ±0.05% (xám) → ±20% (không dải).
4. **Bảng TCR:** ppm/°C theo màu dải 6.
5. **Bảng EIA-96:** 96 cặp số × hệ số chữ cái.

## 7. Kiến trúc & module

```
app/
  core/            (thuần Kotlin, không dính Android/Compose)
    ColorCode.kt     — enum 12 màu + bảng digit/multiplier/tolerance/TCR
    ResistorCalculator.kt — list màu + số dải → Resistance(ohm, tolerance, tcr)
    SmdParser.kt     — string → Resistance | SmdParseError (sealed class)
    Format.kt        — format số kỹ thuật Ω/kΩ/MΩ/GΩ (auto đơn vị)
  ui/
    bands/           — màn Dải màu (state, picker, kết quả)
    resistor/        — ResistorCanvas (composable vẽ hình trở theo spec mục 4)
    smd/             — màn SMD
    reference/       — màn Tham khảo (data bảng hard-code từ core/)
    theme/           — Material 3 theme
```

- `core/` là nguồn sự thật duy nhất cho mọi con số; UI chỉ render. Không có logic tính toán nào trong composable.
- Không có state toàn cục cần chia sẻ giữa 3 tab → không cần ViewModel shared, mỗi màn giữ state cục bộ của nó.

## 8. Xử lý lỗi (chuẩn Luật 4)

- Toàn bộ parse SMD đi qua `SmdParser` trả sealed result (`Success` / `Error(kind)`), UI hiển thị đúng thông báo theo `kind` — không có nhánh `catch` nuốt lỗi.
- Tab Dải màu không có input ngoài → không có lỗi runtime có thể xảy ra; mọi tổ hợp màu đều tính được (chỉ thêm cảnh báo hiếm gặp, không phải lỗi).
- App không có network/file/DB → không cần retry/backoff.

## 9. Testing

1. **Unit test `core/` (TDD bắt buộc):**
   - ResistorCalculator: đủ 3/4/5/6 dải, case chuẩn (VD vàng-tím-đỏ-vàng kim = 4.7kΩ ±5%), multiplier vàng kim/bạc (giá trị < 1Ω), tolerance ngầm định 3 dải, format đơn vị biên (999Ω → 999Ω, 1000Ω → 1 kΩ).
   - SmdParser: 3 số, 4 số, R-notation (đầu/giữa/cuối), 0Ω jumper, EIA-96 hợp lệ + 4 loại lỗi ở mục 5.1.
2. **Compose UI test:** chọn dải → hình + text giá trị cập nhật đúng; đổi số dải 4→5 → picker và hình đổi theo.
3. **Verify thủ công:** build APK, cài emulator, đối chiếu hình trở với ảnh mẫu (mục 4) + chụp screenshot.

## 10. Ngoài phạm vi (YAGNI — không làm trong bản này)

- Quét camera nhận diện màu.
- Lưu lịch sử tra cứu / favorites / DB.
- iOS / web / desktop.
- Đa ngôn ngữ (UI tiếng Việt, có thể hard-code string; nếu sau này cần i18n thì tách `strings.xml`).
- Chế độ luyện tập/quiz.

## 11. Rủi ro đã nhận diện

| Rủi ro | Giảm thiểu |
|---|---|
| Bảng màu/TCR/EIA-96 lệch chuẩn nếu viết từ trí nhớ | Subagent bắt buộc tra cứu DuckDuckGo + context7 trước khi code (Luật 3), cross-verify bảng khi review (5C.5) |
| Hình Canvas xấu/xịt so với ảnh mẫu | Làm composable `ResistorCanvas` tách riêng, verify bằng screenshot emulator trước khi merge; tinh chỉnh gradient là việc chỉnh 1 file, không đụng logic |
| Compose Canvas API thay đổi theo version | Chốt version Compose BOM khi setup, tra tài liệu context7 đúng version đó |
