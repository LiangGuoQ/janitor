package com.janitor.admin.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * ClassName AppConfRefreshDTO
 * Description TODO
 *
 * @author 曦逆
 * Date 2022/5/31 10:22
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppConfRefreshDTO {
    @NotBlank(
            message = "应用名称不能为空"
    )
    private String appName;
}
