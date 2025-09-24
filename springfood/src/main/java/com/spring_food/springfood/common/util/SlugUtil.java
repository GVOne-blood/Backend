package com.spring_food.springfood.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Lớp tiện ích để tạo slug thân thiện với URL từ một chuỗi bất kỳ,
 * hỗ trợ tốt cho tiếng Việt.
 */
public final class SlugUtil {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGESDHASHES = Pattern.compile("(^-|-$)");

    private SlugUtil() {
    }

    public static String toSlug(String input) {
        if (input == null) {
            return "";
        }

        // 2. Thay thế khoảng trắng bằng dấu gạch ngang
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");

        // 3. Chuẩn hóa chuỗi về dạng NFD (Canonical Decomposition)
        // Dạng này sẽ tách một ký tự có dấu thành ký tự gốc và dấu riêng
        // Ví dụ: 'á' -> 'a' + '´'
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);

        // 4. Loại bỏ tất cả các dấu (diacritics) đã được tách ra ở bước 3
        String slug = NONLATIN.matcher(normalized).replaceAll("");

        // 5. Chuyển đổi ký tự 'đ' và 'Đ' thành 'd'
        slug = slug.replace('đ', 'd').replace('Đ', 'D');

        // 6. Chuyển sang chữ thường và xử lý các trường hợp đặc biệt
        slug = slug.toLowerCase(Locale.ENGLISH);

        // 7. Loại bỏ các dấu gạch ngang thừa ở đầu hoặc cuối chuỗi
        slug = EDGESDHASHES.matcher(slug).replaceAll("");

        // 8. Thay thế nhiều dấu gạch ngang liền nhau bằng một dấu duy nhất
        slug = slug.replaceAll("-{2,}", "-");

        return slug;
    }

//    public static void main(String[] args) {
//        System.out.println(SlugUtil.toSlug("Thịt Ba Rọi"));
//    }
}