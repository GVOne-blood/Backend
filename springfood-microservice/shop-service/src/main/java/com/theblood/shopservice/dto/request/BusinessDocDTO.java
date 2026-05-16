package com.theblood.shopservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessDocDTO {

    @NotBlank
    @Size(max = 255)
    private String companyName;             // Tên hộ KD / Công ty

    @NotBlank
    @Size(max = 100)
    private String businessRegNumber;       // Số đăng ký kinh doanh / MST DN

    @NotBlank
    private String licenseMediaId;          // Scan giấy phép KD → media_file.id

    @Size(max = 500)
    private String companyAddress;          // Địa chỉ đăng ký kinh doanh
}
