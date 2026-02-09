package com.theblood.springfood.actionlog.service.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogActionAnnualUpdateExportDTO {

    List<LogActionAnnualUpdateDTO> posts;
}
