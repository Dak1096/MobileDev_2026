# Email-Client

Ứng dụng **email client trên Android** (Java + XML layouts). Ứng dụng cho phép đăng nhập, xem hộp thư, đọc chi tiết thư, soạn và gửi thư, đồng thời tự động lưu bản nháp

## Tính năng

- **Đăng nhập** bằng địa chỉ email
- **Quên mật khẩu**: màn hình nhập email để nhận hướng dẫn đặt lại
- **Hộp thư (Inbox)** hiển thị danh sách thư theo từng thư mục
- **Xem chi tiết thư**
- **Soạn và gửi thư** (kiểm tra người nhận và tiêu đề trước khi gửi, chặn bấm gửi nhiều lần)
- **Tự động lưu nháp** khi thoát màn hình soạn thư mà chưa gửi; xóa nháp khi đã gửi hoặc khi nội dung bị xóa hết
- **Quản lý thư**: chuyển vào thùng rác, báo spam / bỏ spam, chuyển về Inbox, xóa vĩnh viễn

## Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java |
| Giao diện | XML layouts, AppCompat |
| IDE | Android Studio |
| Kiến trúc | Activity / Fragment + Repository + ApiClient |

## Cấu trúc thư mục

```
app/src/main/
├── java/com/example/emailclient/
│   ├── LoginActivity.java           # Đăng nhập
│   ├── ForgotPasswordActivity.java  # Quên mật khẩu
│   ├── InboxActivity.java           # Màn hình chính chứa hộp thư
│   ├── MailListFragment.java        # Danh sách thư
│   ├── MailDetailActivity.java      # Chi tiết thư
│   ├── ComposeActivity.java         # Soạn / gửi thư, lưu nháp
│   ├── EmailAdapter.java            # Adapter cho danh sách thư
│   ├── Email.java                   # Model dữ liệu thư
│   ├── EmailRepository.java         # Lớp truy cập dữ liệu
│   └── ApiClient.java               # Gọi API / xử lý callback
└── res/
    ├── layout/                      # activity_*.xml, fragment_mail_list.xml, item_email.xml
    └── values/                      # strings.xml, colors.xml, themes.xml
```
## Phân tầng

- **`ApiClient`** là cơ chế GET/POST chung, ở tầng **transport** (làm sao gửi)
- **`EmailRepository`** là lớp nghiệp vụ cụ thể, dùng `ApiClient` để thực hiện các thao tác email, ở tầng **domain** (gửi gì, xử lý thế nào)
- **Trước login:** `LoginActivity` : form đăng nhập (có `ForgotPasswordActivity` cho quên mật khẩu)
- **Sau login:** `InboxActivity` : khung chính (Toolbar + Drawer + FAB), từ đây chọn folder và mở màn soạn thư
- **Trong khung:** `MailListFragment` : danh sách thư, được thay theo folder đang chọn
- **Mở thành màn hình riêng (qua Intent):** `ComposeActivity` (soạn thư, mở lại nháp) và `MailDetailActivity` (xem chi tiết thư), được gọi từ FAB hoặc khi bấm một thư trong danh sách

## Khởi tạo Activity: `onCreate`

Bốn dòng đầu của `onCreate` có mặt ở mọi màn hình. Chúng chuẩn bị Activity trước khi chạy logic riêng của từng màn.

| Thành phần | Vai trò | Lưu ý |
| --- | --- | --- |
| `Bundle savedInstanceState` | Chứa trạng thái cũ của Activity | `null` ở lần mở đầu tiên; có dữ liệu khi Android tạo lại Activity (ví dụ xoay màn hình) |
| `super.onCreate(...)` | Khôi phục trạng thái nội bộ của Activity | Bắt buộc, đặt ở dòng đầu tiên |
| `setContentView(...)` | Gắn layout XML vào màn hình | Phải gọi trước `findViewById` |
| `findViewById(...)` | Lấy `View` theo `id` để điều khiển bằng code | Chỉ dùng được sau `setContentView`, nếu không kết quả là `null` |

**Thứ tự bắt buộc:** khôi phục trạng thái, gắn giao diện, rồi mới lấy view để dùng.

Trong project, `savedInstanceState == null` được dùng ở `InboxActivity` để chỉ nạp folder INBOX ở lần mở đầu tiên, tránh nạp lại mỗi lần xoay màn hình.

## Luồng tải danh sách thư (`fetchFolder`)

Ví dụ khi người dùng chọn folder **Drafts**:

```text
Chọn folder "Drafts"
      │
      ▼
fetchFolder(ctx, "Drafts", cb)
      │   path = "/comments?postId=3"
      ▼
ApiClient.get (chạy nền)
      │
      ▼   (khi server phản hồi)
onSuccess(body JSON)
      │   ├─ Parse JSON → danh sách Email
      │   └─ Lọc bỏ thư có key nằm trong hidden
      ▼
result.addAll(0, local["DRAFTS"])   ← chèn nháp của người dùng lên đầu
      │
      ▼
cb.onSuccess([nháp_user, mail1, mail2, ...])
      │
      ▼
EmailAdapter hiển thị danh sách
```

| Bước | Việc làm |
| --- | --- |
| Chọn đường dẫn API | Mỗi folder dùng một `postId` khác nhau để có dữ liệu riêng (Drafts dùng `postId=3`) |
| Gọi mạng | `ApiClient.get` chạy ở luồng nền nên giao diện không bị đóng băng |
| Parse JSON | Mỗi comment được ánh xạ thành một `Email` (`name` → tiêu đề, `email` → người gửi, `body` → nội dung, `id` → mã) |
| Lọc `hidden` | Bỏ các thư người dùng đã xóa hoặc chuyển đi, vì server giả vẫn trả chúng về |
| Chèn thư local | `addAll(0, ...)` đặt thư của người dùng ở **đầu** danh sách, phía dưới là thư từ server |
| Trả kết quả | `cb.onSuccess(...)` báo danh sách về cho Fragment, `EmailAdapter` hiển thị |

## Intent là gì?

**Intent** là cách để các thành phần trong Android giao tiếp với nhau và yêu cầu một hành động, ví dụ mở một màn hình mới kèm dữ liệu.

### Vấn đề cần giải quyết
Khi người dùng bấm vào một thư trong danh sách, ứng dụng cần:

1. Mở sang màn hình chi tiết thư (`MailDetailActivity`).
2. Mang theo thông tin của thư đó (người gửi, tiêu đề, ngày, nội dung...) để màn hình mới hiển thị đúng thư vừa chọn.

**Intent** chính là công cụ để làm cả hai việc trên:

- Intent xác định **màn hình đích** cần mở.
- **Extras** (các cặp key-value) mang dữ liệu của thư sang màn hình đó.


### Intent trong project này

`EmailAdapter` và `InboxActivity` dùng Intent để mở màn hình mới và truyền dữ liệu qua extras:

| Mở màn hình | Từ | Extras truyền đi |
| --- | --- | --- |
| `MailDetailActivity` | `EmailAdapter` (bấm một thư) | `sender`, `subject`, `date`,

## Adapter là gì?

Trong Android, **Adapter** đóng vai trò cầu nối giữa:

- **Dữ liệu**: danh sách `List<Email>`
- **Giao diện**: `RecyclerView`

Nó quyết định:

- Có bao nhiêu item cần hiển thị
- Mỗi item trông như thế nào (layout)
- Dữ liệu nào được gán vào từng item

### Ba hàm chính của Adapter

| Hàm | Việc làm |
| --- | --- |
| `onCreateViewHolder` | Nạp (inflate) layout cho một dòng, ở đây là `item_email.xml` |
| `onBindViewHolder` | Gán dữ liệu của một `Email` vào dòng đó và gắn sự kiện bấm |
| `getItemCount` | Trả về số item cần hiển thị |

`RecyclerView` chỉ tạo số dòng vừa đủ cho màn hình rồi **tái sử dụng** khi cuộn, nên danh sách dài vẫn mượt.

### `EmailAdapter` trong project này

- **Hiển thị:** gán 5 trường vào 5 `TextView`: người gửi, tiêu đề, xem trước, ngày và avatar. Avatar là chữ cái đầu của người gửi (rỗng thì hiện `?`).
- **Xử lý bấm vào một thư:** rẽ nhánh theo folder hiện tại:
  - Folder Drafts: mở `ComposeActivity` kèm `draftKey`, `to`, `subject`, `body` để sửa nháp.
  - Folder khác: mở `MailDetailActivity` kèm `sender`, `subject`, `date`, `body`, `folder`.
- **Nhớ folder hiện tại:** `setFolder()` được gọi từ `MailListFragment` trước khi gắn Adapter, vì biến này quyết định nhánh ở trên. Quên gọi thì mặc định là Inbox và bấm vào nháp sẽ mở nhầm màn chi tiết.
- **Phụ thuộc:** model `Email`, layout `item_email.xml`, và `EmailRepository.key(Email)` để tạo `draftKey`.

## CRUD in-memory qua Adapter

Dữ liệu thư được quản lý trong `EmailRepository`. `EmailAdapter` chỉ **đọc** danh sách đó để hiển thị và điều hướng tới màn hình thực hiện các thao tác thay đổi dữ liệu.

```text
┌─────────────────────────────────────────────────────────────┐
│ EmailRepository (in-memory "DB")                            │
│   local:  Map<folder, List<Email>>   dữ liệu chính          │
│   hidden: Set<key>                   thư đã xóa/chuyển đi   │
│                                                             │
│   Create: sendEmail(), saveDraft()                          │
│   Read:   fetchFolder() = (server − hidden) + local         │
│   Update: saveDraft() (xóa cũ, thêm mới)                    │
│   Move:   moveEmail() chuyển thư sang folder khác           │
│   Delete: deleteForever(), discardDraft()                   │
└──────────────────────────────┬──────────────────────────────┘
                               │ trả List<Email> đã chuẩn bị
                               ▼
┌─────────────────────────────────────────────────────────────┐
│ EmailAdapter (chỉ Read)                                     │
│   list   ──► onBindViewHolder ──► TextView                  │
│   folder ──► quyết định click mở màn nào                    │
│                                                             │
│   Click Drafts ──► ComposeActivity     (sửa nháp)           │
│   Click khác   ──► MailDetailActivity  (xem chi tiết)       │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
          ComposeActivity / MailDetailActivity
          gọi EmailRepository để Create / Update / Move / Delete
```

### Thành phần của `EmailAdapter`

| Thành phần | Vai trò | Ghi chú |
| --- | --- | --- |
| `list` (constructor) | Nguồn dữ liệu hiển thị | Tham chiếu tới danh sách `Email` mà Repository đã chuẩn bị |
| `folder` / `setFolder()` | Ngữ cảnh của danh sách | Quyết định một lần bấm sẽ mở màn nào |
| `ViewHolder` | Tối ưu hiển thị | Lưu sẵn các view, tránh gọi `findViewById` lặp lại |
| `onCreateViewHolder` | Tạo khung cho một dòng | Inflate `item_email.xml` mỗi khi cần ViewHolder mới |
| `onBindViewHolder` | Hiển thị chính | Gán dữ liệu, tạo avatar, gắn sự kiện bấm |
| `getItemCount` | Số dòng cần hiển thị | Trả về `list.size()` |
| Click handler | Điều hướng | Drafts mở `ComposeActivity`, folder khác mở `MailDetailActivity` |

### Ai thực hiện thao tác nào

| Thao tác | Kích hoạt từ | Hàm trong Repository |
| --- | --- | --- |
| Create | `ComposeActivity` (bấm Gửi) | `sendEmail` |
| Create / Update | `ComposeActivity` (thoát khi chưa gửi) | `saveDraft` |
| Read | `MailListFragment` + `EmailAdapter` | `fetchFolder` |
| Move | `MailDetailActivity` (Xóa, Spam) | `moveEmail` |
| Delete | `MailDetailActivity` (trong Trash), `ComposeActivity` (bỏ nháp) | `deleteForever`, `discardDraft` |

### Điểm cần lưu ý

- `EmailAdapter` **chỉ đọc** danh sách và vẽ lên màn hình, không tạo, sửa hay xóa dữ liệu.
- `folder` là "ngữ cảnh" biến cùng một lần bấm thành hai hành động khác nhau: bấm nháp thì mở `ComposeActivity` để sửa, bấm thư thường thì mở `MailDetailActivity` để xem.
- Vì vậy `setFolder()` phải được gọi **trước khi** gắn Adapter vào `RecyclerView`. Nếu quên, Adapter mặc định là Inbox và bấm vào nháp sẽ mở nhầm màn chi tiết.

### Callback `onSuccess` / `onError`

Các thao tác gọi mạng (đăng nhập, tải danh sách thư, gửi thư) chạy **bất đồng bộ**. Kết quả được báo về qua một callback có hai phương thức: `onSuccess` và `onError`. Callback này đóng vai trò như một "hợp đồng hai chiều" giữa bên gọi API và bên cung cấp API:

| Bên | Cam kết |
| --- | --- |
| **Bên gọi** (Activity / Fragment) | Giao việc và cung cấp hai "cửa" để nhận kết quả |
| **Bên cung cấp** (`EmailRepository` / `ApiClient`) | Thành công thì gọi `onSuccess`, lỗi thì gọi `onError` |

**Lợi ích:**

- Giao diện không bị đóng băng trong lúc chờ mạng.
- Mọi kết quả, cả thành công lẫn thất bại, đều có chỗ xử lý.

## Khi mở INBOX(hộp thư đến)

1. `InboxActivity` → `MailListFragment.newInstance("INBOX")`
2. Fragment `onResume()` → `EmailRepository.fetchFolder("INBOX", callback)`
3. Repository → `ApiClient.get("/comments?_limit=30")` → JSONPlaceholder trả 30 comment giả (bất đồng bộ, kết quả báo về qua callback)
4. Repository parse JSON → tạo `List<Email>` → lọc `hidden`, chèn thư local lên đầu → báo kết quả về Fragment
5. Fragment tạo `EmailAdapter(list)`, gọi `setFolder("INBOX")`, gắn vào RecyclerView → hiện danh sách thư

`AndroidManifest.xml`
- Khai báo quyền `INTERNET` (bắt buộc để app gọi mạng, tức gửi/nhận thư qua API)
- Đăng ký đủ 5 Activity: `LoginActivity`, `InboxActivity`, `ComposeActivity`, `MailDetailActivity`, `ForgotPasswordActivity` Activity nào thiếu khai báo thì khi mở bằng Intent app sẽ crash
- `LoginActivity` là **màn hình khởi động** (intent-filter `MAIN` + `LAUNCHER`), nên phải đặt `exported="true"`
- `exported="false"` ở 4 màn còn lại: chỉ app này mới mở được, app khác không gọi vào được (bảo mật)
- `windowSoftInputMode="adjustResize"` ở Login, Inbox, Compose: khi bàn phím hiện lên, layout co lại để các ô nhập không bị che

## Dữ liệu local trong `EmailRepository`

`local` là một `Map` lưu thư theo **tên folder**, đóng vai trò "cơ sở dữ liệu in-memory" của app:

```text
┌─────────────────────────────────────────────┐
│  local (Map<String, List<Email>>)           │
├─────────────────────────────────────────────┤
│  "INBOX"  →  [Email1, Email2, Email3]       │
│  "SENT"   →  [Email4, Email5]               │
│  "DRAFTS" →  [Email6]                       │
│  "SPAM"   →  []                             │
│  "TRASH"  →  [Email7, Email8]               │
└─────────────────────────────────────────────┘
```

Mọi thao tác đều bắt đầu bằng việc **tìm folder theo khóa**:

| Thao tác | Cách làm | Độ phức tạp thuật toán |
| --- | --- | --- |
| Lấy thư của Inbox | `local.get("INBOX")` | Tìm folder O(1) |
| Thêm thư vào Spam | `local.get("SPAM").add(email)` | Tìm folder O(1), thêm vào cuối danh sách O(1) |
| Xóa thư khỏi Trash | `local.get("TRASH").remove(email)` | Tìm folder O(1), tìm thư trong danh sách O(n) |
| Chuyển Inbox → Trash | `remove` khỏi Inbox, thêm khóa vào `hidden`, `add` vào Trash | Tìm folder O(1), `remove` O(n) |
| Kiểm tra folder tồn tại | `local.containsKey("INBOX")` | O(1) |

- `hidden` là tập khóa các thư đã xóa hoặc chuyển đi. Server giả vẫn trả thư đó về, nên `fetchFolder` phải lọc chúng ra.
- Mỗi folder chỉ có vài chục thư nên O(n) không ảnh hưởng hiệu năng thực tế.

## Các hàm tiện ích trong `EmailRepository`

Ba hàm nhỏ dưới đây là thành phần quan trọng giữa các phần của app: chúng không xử lý nghiệp vụ lớn nhưng giữ cho dữ liệu nhất quán khi đi qua nhiều màn hình và nhiều lớp.

| Hàm | Vai trò | Dùng ở đâu |
| --- | --- | --- |
| `key(Email)` | Tạo định danh cho một thư (ghép `sender`, `subject`, `date`), dùng nhất quán xuyên các màn hình | `draftKey` khi mở lại nháp, tập `hidden`, so sánh thư khi `remove` |
| `canon(...)` | Chuẩn hóa đầu vào không đồng nhất thành một khóa thống nhất | Tra cứu `local` theo tên folder |
| `now()` | Tự sinh ngày gửi, vì dữ liệu từ server giả không có trường này | Thư mới gửi và bản nháp mới lưu |

## Kết quả `true` / `false` khi đăng nhập

`LoginActivity` nhận một giá trị `Boolean` trong `onSuccess` và rẽ nhánh hành vi theo giá trị đó:

| Kết quả | Ý nghĩa | Hành vi của app |
| --- | --- | --- |
| `true` | Email tồn tại | Chuyển sang `InboxActivity`, lưu phiên đăng nhập |
| `false` | Email không tồn tại | Hiện Toast "Sai tài khoản", ở lại `LoginActivity` |

- `true` / `false` chỉ cho biết **email có tồn tại hay không**. Trường hợp gọi mạng thất bại (mất mạng, lỗi server) được báo riêng qua `onError`, không đi qua nhánh này.
  
## Sơ đồ luồng hoạt động

### 1. Luồng tổng thể của app

```text
LoginActivity (MAIN + LAUNCHER, màn hình khởi động)
├─ Đăng nhập OK ───► InboxActivity
│                    ├─ Bấm một thư ────────────► MailDetailActivity ──finish──► Inbox
│                    └─ FAB hoặc bấm một nháp ──► ComposeActivity ─────finish──► Inbox
└─ Quên mật khẩu ──► ForgotPasswordActivity ──► quay lại Login
```

**Entry point duy nhất:** `LoginActivity` (intent-filter `MAIN` + `LAUNCHER`).

### 2. LoginActivity: đăng nhập

```text
Nhập email
├─ Bấm Đăng nhập
│    ├─ Gọi API kiểm tra tài khoản
│    ├─ Hợp lệ ────────────────► startActivity(InboxActivity) + finish()
│    └─ Không hợp lệ / lỗi mạng ► Toast báo lỗi
└─ Bấm Quên mật khẩu ► mở ForgotPasswordActivity
```

- `windowSoftInputMode="adjustResize"`: bàn phím không che ô nhập.
- Đăng nhập xong có `finish()`, nên bấm Back không quay lại màn Login.

### 3. ForgotPasswordActivity: quên mật khẩu

```text
Nhập email ► Gửi yêu cầu reset ► Toast kết quả ► finish() quay lại Login
```

### 4. InboxActivity: hộp thư đến

```text
onCreate: dựng Toolbar + Drawer + FAB
├─ Mặc định nạp INBOX ► MailListFragment hiển thị danh sách
├─ Chọn folder ở Drawer ► thay MailListFragment theo folder
├─ Bấm thư
│    ├─ Folder Drafts ───► ComposeActivity (kèm draftKey)
│    └─ Folder khác ─────► MailDetailActivity
└─ Bấm FAB ► ComposeActivity (form trống)
```

Intent extras khi mở `MailDetailActivity`:

| Key | Giá trị |
| --- | --- |
| `sender` | Địa chỉ người gửi |
| `subject` | Tiêu đề |
| `date` | Ngày gửi |
| `body` | Nội dung |
| `folder` | Tên folder hiện tại (ví dụ Inbox, Trash, Spam) |

### 5. MailDetailActivity: chi tiết email

```text
Mở MailDetailActivity
│
├─ Đọc Intent extras (sender, subject, date, body, folder)
├─ Hiển thị lên TextView
├─ Xác định ngữ cảnh: inTrash / inSpam
├─ Đổi label nút (Xóa → "Xóa vĩnh viễn", Spam → "Không phải spam")
│
├─ Bấm Xóa
│    ├─ Đang ở Trash → deleteForever()
│    └─ Ngược lại    → moveEmail → "TRASH"
│
└─ Bấm Spam
     ├─ Đang ở Spam  → moveEmail → "INBOX"
     └─ Ngược lại    → moveEmail → "SPAM"

Sau mỗi thao tác: finish() → quay lại màn trước
```

Hành vi nút theo thư mục:

| Thư mục hiện tại | Nút Xóa | Nút Spam |
| --- | --- | --- |
| Inbox | Chuyển vào Trash | Chuyển vào Spam |
| Trash | Xóa vĩnh viễn (`deleteForever`) | Chuyển vào Spam |
| Spam | Chuyển vào Trash | Chuyển về Inbox ("Không phải spam") |

- Hàm `s(String v)` chống `NullPointerException`: trả `""` nếu `null`.
- `folder.contains("Trash")` / `contains("Spam")` xác định ngữ cảnh linh hoạt, không cần khớp tuyệt đối.

### 6. ComposeActivity: soạn email

```text
onCreate: đọc extras (draftKey, to, subject, body) ► điền sẵn 3 ô ► chờ người dùng

Bấm Gửi
├─ to rỗng ──────► Toast error_to_required (quay lại chờ)
├─ subject rỗng ─► Toast error_subject_required (quay lại chờ)
└─ Hợp lệ ► tắt nút Gửi ► EmailRepository.sendEmail
     ├─ Thành công (ok = true)
     │    ├─ Bật lại nút, sent = true
     │    ├─ draftKey != null ► discardDraft (xóa nháp cũ)
     │    └─ Toast sent_ok ► finish()
     ├─ Thất bại (ok = false) ► bật lại nút + Toast error_send_failed (cho thử lại)
     └─ Lỗi (onError) ───────► bật lại nút + Toast error_send_failed (cho thử lại)

Back / thoát khi chưa gửi ► onPause
├─ Không (isFinishing sai, hoặc sent = true) ► không lưu nháp
└─ Có (isFinishing đúng và sent = false) ► đọc lại to, subject, body
     ├─ Còn nội dung ─────────────────────► saveDraft + Toast draft_saved
     ├─ Hết nội dung, draftKey != null ───► discardDraft
     └─ Hết nội dung, draftKey = null ────► không làm gì
     (cuối cùng: đóng màn hình)
```

- `adjustResize`: bàn phím không che ô Body.
- Cờ `sent` giúp phân biệt "đóng vì đã gửi" và "đóng vì bỏ dở" (chỉ bỏ dở mới lưu nháp).
- Cần quyền `INTERNET` (đã khai báo trong Manifest).

### 7. Tương tác giữa các lớp

```text
Activity / Fragment / Adapter
        │
        ▼
 EmailRepository ──► dữ liệu local (thư local + hidden)
        │
        ▼
    ApiClient
        │
        ▼
 Server giả (JSONPlaceholder)
```

### Bảng tóm tắt

| Màn hình | Vào từ | Hành động chính | Ra tới |
| --- | --- | --- | --- |
| `LoginActivity` | Launcher | Đăng nhập, Quên mật khẩu | Inbox / ForgotPassword |
| `ForgotPasswordActivity` | Login | Gửi yêu cầu reset | Quay lại Login |
| `InboxActivity` | Login | Xem danh sách, chọn thư, soạn thư | MailDetail / Compose |
| `MailDetailActivity` | Inbox | Xóa / Spam theo ngữ cảnh | Quay lại Inbox |
| `ComposeActivity` | Inbox | Soạn, gửi, tự lưu nháp | Quay lại Inbox |
