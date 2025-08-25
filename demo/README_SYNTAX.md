# Cú pháp Markdown cho README

## 1. Tiêu đề (Headers)

# Tiêu đề cấp 1
## Tiêu đề cấp 2
### Tiêu đề cấp 3
#### Tiêu đề cấp 4
##### Tiêu đề cấp 5
###### Tiêu đề cấp 6

Hoặc:

Tiêu đề cấp 1
=============

Tiêu đề cấp 2
-------------

## 2. Định dạng văn bản (Text Formatting)

**Chữ đậm** hoặc __chữ đậm__

*Chữ nghiêng* hoặc _chữ nghiêng_

***Chữ đậm và nghiêng*** hoặc ___chữ đậm và nghiêng___

~~Chữ gạch ngang~~

## 3. Danh sách (Lists)

### Danh sách không có thứ tự:
- Mục 1
- Mục 2
  - Mục con 2.1
  - Mục con 2.2
- Mục 3

Hoặc:
* Mục 1
* Mục 2
  * Mục con 2.1
  * Mục con 2.2

Hoặc:
+ Mục 1
+ Mục 2

### Danh sách có thứ tự:
1. Mục đầu tiên
2. Mục thứ hai
   1. Mục con 2.1
   2. Mục con 2.2
3. Mục thứ ba

### Danh sách công việc (Task Lists):
- [x] Công việc đã hoàn thành
- [ ] Công việc chưa hoàn thành
- [ ] Công việc khác

## 4. Liên kết (Links)

### Liên kết bên ngoài:
[Văn bản hiển thị](https://www.example.com)

[Liên kết với tiêu đề](https://www.example.com "Tiêu đề khi hover")

[Liên kết tham chiếu][1]

[1]: https://www.example.com

Liên kết tự động: https://www.example.com

### Liên kết điều hướng nội bộ (Anchor Links):

#### Cách tạo liên kết đến một phần trong cùng file:

1. **Tạo tiêu đề**:
   ```markdown
   ## Tiêu đề của tôi
   ```

2. **Tạo liên kết đến tiêu đề**:
   ```markdown
   [Click vào đây](#tiêu-đề-của-tôi)
   ```

#### Quy tắc chuyển đổi tiêu đề thành anchor:
- Chuyển tất cả chữ thành **chữ thường**
- Thay thế **khoảng trắng** bằng dấu **gạch ngang** `-`
- Loại bỏ các ký tự đặc biệt (ngoại trừ `-` và `_`)
- Loại bỏ emoji và ký tự Unicode đặc biệt

#### Ví dụ chuyển đổi:

| Tiêu đề | Anchor link |
|---------|-------------|
| `# Hello World` | `[Link](#hello-world)` |
| `## Cài đặt & Sử dụng` | `[Link](#cài-đặt--sử-dụng)` |
| `### Version 2.0.1` | `[Link](#version-201)` |
| `#### What's New?` | `[Link](#whats-new)` |
| `## 🚀 Getting Started` | `[Link](#-getting-started)` |
| `## Hướng dẫn sử dụng` | `[Link](#hướng-dẫn-sử-dụng)` |

#### Ví dụ mục lục với anchor links:
```markdown
## Mục lục
- [Giới thiệu](#giới-thiệu)
- [Cài đặt](#cài-đặt)
  - [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
  - [Hướng dẫn cài đặt](#hướng-dẫn-cài-đặt)
- [Sử dụng](#sử-dụng)
- [API Documentation](#api-documentation)
- [Contributing](#contributing)

## Giới thiệu
[⬆ Quay lại mục lục](#mục-lục)

Nội dung giới thiệu...

## Cài đặt
[⬆ Quay lại mục lục](#mục-lục)

### Yêu cầu hệ thống
Nội dung yêu cầu...

### Hướng dẫn cài đặt
Nội dung hướng dẫn...
```

#### Lưu ý với tiêu đề trùng lặp:
Khi có nhiều tiêu đề giống nhau, GitHub sẽ thêm số:
```markdown
## Cài đặt
## Cài đặt  <!-- Tiêu đề thứ 2 -->

[Link đến tiêu đề đầu](#cài-đặt)
[Link đến tiêu đề thứ 2](#cài-đặt-1)
```

## 5. Hình ảnh (Images)

![Alt text](https://via.placeholder.com/150)

![Alt text](https://via.placeholder.com/150 "Tiêu đề hình ảnh")

Hình ảnh có liên kết:
[![Alt text](https://via.placeholder.com/150)](https://www.example.com)

## 6. Code

### Code inline:
Sử dụng `console.log()` để debug

### Code block:
```
function hello() {
    console.log("Hello World!");
}
```

### Code block với ngôn ngữ cụ thể:
```javascript
function hello() {
    console.log("Hello World!");
}
```

```python
def hello():
    print("Hello World!")
```

```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
```

## 7. Trích dẫn (Blockquotes)

> Đây là một trích dẫn
> 
> Trích dẫn có thể có nhiều dòng

> Trích dẫn cấp 1
>> Trích dẫn cấp 2
>>> Trích dẫn cấp 3

## 8. Đường kẻ ngang (Horizontal Rules)

---

Hoặc:

***

Hoặc:

___

## 9. Bảng (Tables)

| Tiêu đề 1 | Tiêu đề 2 | Tiêu đề 3 |
|-----------|-----------|-----------|
| Ô 1.1     | Ô 1.2     | Ô 1.3     |
| Ô 2.1     | Ô 2.2     | Ô 2.3     |

Căn chỉnh trong bảng:

| Trái | Giữa | Phải |
|:-----|:----:|-----:|
| Căn trái | Căn giữa | Căn phải |
| 123 | 456 | 789 |

## 10. HTML trong Markdown

<div align="center">
  <h3>Văn bản căn giữa</h3>
</div>

<details>
<summary>Click để xem chi tiết</summary>

Nội dung ẩn sẽ hiển thị khi click vào summary.

</details>

## 11. Emoji

:smile: :heart: :thumbsup: :star: :fire:

## 12. Footnotes (Chú thích cuối trang)

Đây là một văn bản có chú thích[^1].

[^1]: Đây là nội dung chú thích.

## 13. Badges (Huy hiệu)

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Version](https://img.shields.io/badge/version-1.0.0-blue)
![License](https://img.shields.io/badge/license-MIT-green)

## 14. Ví dụ README hoàn chỉnh

```markdown
# Tên Dự Án

![Logo](logo.png)

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://travis-ci.org/username/repo)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

Mô tả ngắn gọn về dự án.

## Mục lục

- [Cài đặt](#cài-đặt)
- [Sử dụng](#sử-dụng)
- [Tính năng](#tính-năng)
- [Đóng góp](#đóng-góp)
- [License](#license)

## Cài đặt

```bash
npm install ten-package
```

## Sử dụng

```javascript
const package = require('ten-package');
package.doSomething();
```

## Tính năng

- ✅ Tính năng 1
- ✅ Tính năng 2
- 🚧 Tính năng 3 (đang phát triển)

## Đóng góp

1. Fork dự án
2. Tạo branch (`git checkout -b feature/AmazingFeature`)
3. Commit thay đổi (`git commit -m 'Add some AmazingFeature'`)
4. Push lên branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

## License

Distributed under the MIT License. See `LICENSE` for more information.

## Liên hệ

Tên - [@twitter](https://twitter.com/username) - email@example.com

Project Link: [https://github.com/username/repo](https://github.com/username/repo)
```

## 15. Mẹo và lưu ý

1. **Dòng trống**: Cần có dòng trống giữa các phần tử khác nhau
2. **Escape characters**: Sử dụng `\` để escape các ký tự đặc biệt như \*, \_, \[, \]
3. **Nested lists**: Sử dụng 2 hoặc 4 khoảng trắng để tạo danh sách con
4. **Line breaks**: Sử dụng 2 khoảng trắng ở cuối dòng hoặc `<br>` để xuống dòng
5. **Comments**: Sử dụng `<!-- Comment -->` để thêm ghi chú ẩn

## 16. Công cụ hữu ích

- [Markdown Preview](https://markdownlivepreview.com/) - Xem trước Markdown online
- [Shields.io](https://shields.io/) - Tạo badges cho README
- [Markdown Tables Generator](https://www.tablesgenerator.com/markdown_tables) - Tạo bảng Markdown
- [Emoji Cheat Sheet](https://github.com/ikatyang/emoji-cheat-sheet) - Danh sách emoji cho GitHub
