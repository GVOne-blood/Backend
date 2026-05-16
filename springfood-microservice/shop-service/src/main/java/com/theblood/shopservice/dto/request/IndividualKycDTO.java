package com.theblood.shopservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IndividualKycDTO {


    // Thông tin nhập tay (pre-fill từ NFC chip)
    @NotBlank
    @Pattern(regexp = "^\\d{12}$", message = "Số CCCD phải đủ 12 chữ số")
    private String idNumber;            // Số CCCD 12 chữ số

    @NotBlank
    @Size(max = 100)
    private String fullName;            // Họ và tên (đúng như trên CCCD)

    @NotNull
    private LocalDate dateOfBirth;

    @Size(max = 10)
    private String gender;              // MALE | FEMALE

    @Size(max = 500)
    private String permanentAddress;    // Nơi thường trú

    private LocalDate issuedDate;       // Ngày cấp
    @Size(max = 255)
    private String issuedPlace;         // Nơi cấp

    // ---- Ảnh giấy tờ ----
    @NotBlank
    private String frontImageMediaId;   // Ảnh mặt trước CCCD → media_file.id

    @NotBlank
    private String backImageMediaId;    // Ảnh mặt sau CCCD → media_file.id

    @NotBlank
    private String selfieMediaId;       // Ảnh selfie / liveness → media_file.id

    // ---- NFC CCCD gắn chip ----
    // Client (mobile app) đọc NFC xong gửi kết quả lên
    private Boolean nfcVerified;        // true nếu đọc NFC thành công

    @Size(max = 4000)
    private String nfcRawData;          // Encrypted NFC chip data từ thiết bị
    // Backend verify lại bằng eKYC provider (VnPT/FPT)


}
