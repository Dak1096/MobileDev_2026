# Email-Client

Ứng dụng **email client trên Android** (Java + XML layouts), được xây dựng cho đồ án môn Lập trình di động. Ứng dụng cho phép đăng nhập, xem hộp thư, đọc chi tiết thư, soạn và gửi thư, đồng thời tự động lưu bản nháp.

## Tính năng

- **Đăng nhập** bằng địa chỉ email
- **Quên mật khẩu**: màn hình nhập email để nhận hướng dẫn đặt lại (hiện mới là giao diện, chưa kết nối backend)
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
