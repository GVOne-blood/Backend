# Biểu thức chính quy (Regex) trong Java

Tài liệu này cung cấp một cái nhìn tổng quan chi tiết về cách sử dụng Biểu thức chính quy (Regular Expressions - Regex)
trong Java. Regex là một công cụ cực kỳ mạnh mẽ để tìm kiếm, khớp và thao tác với các chuỗi văn bản dựa trên một mẫu (
pattern) được định nghĩa trước.

Trong Java, việc sử dụng Regex chủ yếu xoay quanh hai lớp trong gói `java.util.regex`:

1. **`Pattern`**: Đại diện cho một biểu thức chính quy đã được biên dịch. Việc biên dịch trước pattern giúp tăng hiệu
   năng khi sử dụng lại nhiều lần.
2. **`Matcher`**: Là một "công cụ" được tạo ra từ một `Pattern` để thực hiện các thao tác khớp trên một chuỗi đầu vào cụ
   thể.

## Luồng làm việc cơ bản

1. **Biên dịch (Compile)**: Tạo một đối tượng `Pattern` từ chuỗi regex.
2. **Khớp (Match)**: Tạo một đối tượng `Matcher` từ `Pattern` và chuỗi đầu vào.
3. **Thao tác (Operate)**: Sử dụng các phương thức của `Matcher` để kiểm tra, tìm kiếm hoặc thay thế.

```java
import java.util.regex.Pattern;
import java.util.regex.Matcher;

// 1. Biên dịch pattern
Pattern pattern = Pattern.compile("(\\w+)(\\s+)(\\d+)");

        // Chuỗi đầu vào
        String input = "Java 8";

        // 2. Tạo Matcher
        Matcher matcher = pattern.matcher(input);

// 3. Thao tác
if(matcher.

        find()){
        System.out.

        println("Tìm thấy khớp!");
    System.out.

        println("Toàn bộ chuỗi khớp: "+matcher.group(0)); // "Java 8"
        System.out.

        println("Nhóm 1 (\\w+): "+matcher.group(1));     // "Java"
        System.out.

        println("Nhóm 2 (\\s+): "+matcher.group(2));     // " " (khoảng trắng)
        System.out.

        println("Nhóm 3 (\\d+): "+matcher.group(3));     // "8"
        }
```

---
Regex

| Ký tự / Cú pháp                        | Mô tả                                                                              | Ví dụ          | Chuỗi khớp                  |
|:---------------------------------------|:-----------------------------------------------------------------------------------|:---------------|:----------------------------|
| **Ký tự đơn**                          |                                                                                    |                |                             |
| `.`                                    | Khớp với bất kỳ ký tự nào (trừ ký tự xuống dòng)                                   | `a.c`          | `abc`, `a_c`, `a2c`         |
| `\d`                                   | Khớp với một chữ số. Tương đương `[0-9]`                                           | `\d{3}`        | `123`, `987`                |
| `\D`                                   | Khớp với một ký tự không phải chữ số. Tương đương `[^0-9]`                         | `\D+`          | `abc`, `!@#`                |
| `\w`                                   | Khớp với một ký tự "word" (chữ cái, số, dấu gạch dưới). Tương đương `[a-zA-Z0-9_]` | `\w+`          | `java_8`, `user1`           |
| `\W`                                   | Khớp với một ký tự không phải "word". Tương đương `[^a-zA-Z0-9_]`                  | `\W`           | `!`, ` `, `@`               |
| `\s`                                   | Khớp với một ký tự khoảng trắng (space, tab `\t`, newline `\n`,...)                | `hello\sworld` | `hello world`               |
| `\S`                                   | Khớp với một ký tự không phải khoảng trắng                                         | `\S+`          | `hello_world`               |
| **Lớp ký tự (Character Classes)**      |                                                                                    |                |                             |
| `[abc]`                                | Khớp với một trong các ký tự bên trong ngoặc                                       | `gr[ae]y`      | `gray`, `grey`              |
| `[^abc]`                               | Khớp với bất kỳ ký tự nào **không** có trong ngoặc                                 | `[^0-9]`       | `a`, `!`, ` `               |
| `[a-z]`                                | Khớp với một ký tự trong khoảng từ `a` đến `z`                                     | `[a-c]at`      | `aat`, `bat`, `cat`         |
| **Vị trí (Anchors)**                   |                                                                                    |                |                             |
| `^`                                    | Khớp với đầu chuỗi                                                                 | `^Start`       | `Start of line`             |
| `$`                                    | Khớp với cuối chuỗi                                                                | `end$`         | `This is the end`           |
| `\b`                                   | Khớp với ranh giới của một từ (word boundary)                                      | `\bword\b`     | `a word here` (khớp `word`) |
| `\B`                                   | Khớp với vị trí không phải ranh giới của từ                                        | `\Bword\B`     | `password` (khớp `word`)    |
| **Số lượng (Quantifiers)**             |                                                                                    |                |                             |
| `*`                                    | Khớp 0 hoặc nhiều lần                                                              | `a*`           | ``, `a`, `aa`, `aaa`        |
| `+`                                    | Khớp 1 hoặc nhiều lần                                                              | `a+`           | `a`, `aa`, `aaa`            |
| `?`                                    | Khớp 0 hoặc 1 lần                                                                  | `colou?r`      | `color`, `colour`           |
| `{n}`                                  | Khớp đúng `n` lần                                                                  | `\d{4}`        | `2023`                      |
| `{n,}`                                 | Khớp ít nhất `n` lần                                                               | `\d{2,}`       | `12`, `123`, `1234`         |
| `{n,m}`                                | Khớp từ `n` đến `m` lần                                                            | `\d{2,4}`      | `12`, `123`, `1234`         |
| **Nhóm và Logic (Grouping and Logic)** |                                                                                    |                |                             |
| `(...)`                                | Nhóm các ký tự lại với nhau và tạo một "capturing group"                           | `(Java)+`      | `Java`, `JavaJava`          |
| `(?:...)`                              | Nhóm không bắt giữ (non-capturing group)                                           | `(?:Java)+`    | `Java`, `JavaJava`          |
| `\|`                                   | Toán tử HOẶC (OR)                                                                  | `cat\|dog`     | `cat`, `dog`                |
| **Ký tự thoát (Escape)**               |                                                                                    |                |                             |
| `\`                                    | Dùng để thoát một ký tự đặc biệt, biến nó thành ký tự thường                       | `\.`           | `.` (dấu chấm)              |