package com.spring_food.springfood.common.util;

public class VNPayUtil {


    /**
     * Chuyển đổi mã vnp_TransactionStatus từ API QueryDR thành một thông điệp dễ hiểu.
     *
     * @param statusCode Mã trạng thái nhận được từ VNPay (ví dụ: "00", "02").
     * @return Một chuỗi String mô tả trạng thái đó.
     */
    public static String getTransactionStatusDescription(String statusCode) {
        if (statusCode == null || statusCode.isEmpty()) {
            return "Không nhận được trạng thái từ VNPay. Vui lòng thử lại sau ít phút.";
        }

        return switch (statusCode) {
            // == Trạng thái thành công ==
            case "00" -> "Giao dịch đã được thực hiện thành công.";

            // == Trạng thái đang chờ hoặc đang xử lý ==
            case "01" -> "Giao dịch chưa hoàn tất. Giao dịch có thể đang chờ xử lý hoặc đã bị hủy.";
            case "05" -> "Giao dịch đang được VNPAY xử lý (thường là giao dịch hoàn tiền). Vui lòng chờ.";
            case "06" -> "VNPAY đã gửi yêu cầu hoàn tiền sang ngân hàng. Vui lòng chờ đối soát.";

            // == Trạng thái lỗi ==
            case "02" -> "Giao dịch thất bại. Có thể do lỗi từ phía ngân hàng hoặc thông tin thẻ không chính xác.";
            case "04" ->
                    "Giao dịch đảo (đã bị hoàn tiền). Số tiền đã được hoặc sẽ sớm được hoàn lại vào tài khoản của bạn.";
            case "09" -> "Giao dịch hoàn trả bị ngân hàng từ chối. Vui lòng liên hệ ngân hàng phát hành thẻ.";

            // == Trạng thái đặc biệt ==
            case "07" -> "Giao dịch bị nghi ngờ có dấu hiệu gian lận. Vui lòng liên hệ bộ phận hỗ trợ của VNPAY.";
            case "08" -> "Giao dịch đã quá hạn thanh toán.";

            // == Trạng thái liên quan đến đơn hàng (ít gặp trong QueryDR) ==
            case "10" -> "Đã giao hàng.";
            case "11" -> "Giao dịch đã bị hủy.";
            case "20" -> "Giao dịch đã được thanh quyết toán cho merchant.";

            // == Trường hợp mặc định ==
            default -> "Trạng thái giao dịch không xác định. Mã trạng thái từ VNPAY: " + statusCode;
        };
    }

    public static String getRefundResponseDescription(String responseCode) {
        if (responseCode == null || responseCode.isEmpty()) {
            return "Không nhận được phản hồi từ VNPay.";
        }

        return switch (responseCode) {
            // == Thành công ==
            case "00" -> "Yêu cầu hoàn tiền đã được gửi thành công. Vui lòng chờ VNPAY xử lý.";

            // == Lỗi Dữ liệu đầu vào ==
            case "02" -> "Merchant không hợp lệ (kiểm tra lại vnp_TmnCode).";
            case "03" -> "Dữ liệu gửi sang không đúng định dạng.";
            case "91" -> "Không tìm thấy giao dịch yêu cầu hoàn trả.";
            case "93" ->
                    "Số tiền hoàn trả không hợp lệ. Số tiền hoàn trả phải nhỏ hơn hoặc bằng số tiền đã thanh toán.";
            case "97" -> "Chữ ký không hợp lệ (vnp_SecureHash sai).";

            // == Lỗi Trạng thái Nghiệp vụ ==
            case "16" -> "Giao dịch này không thể thực hiện hoàn tiền trong thời gian này.";
            case "94" -> "Giao dịch đã được gửi yêu cầu hoàn tiền trước đó. Yêu cầu này VNPAY đang xử lý.";
            case "95" -> "Giao dịch này không thành công bên phía VNPAY. VNPAY từ chối xử lý yêu cầu.";

            // == Lỗi Hệ thống ==
            case "08" -> "Hệ thống VNPay đang bảo trì. Vui lòng thử lại sau.";
            case "99" -> "Các lỗi không xác định khác. Vui lòng liên hệ VNPAY để được hỗ trợ.";

            // == Trường hợp mặc định ==
            default -> "Lỗi không xác định. Mã lỗi từ VNPAY: " + responseCode;
        };
    }
}