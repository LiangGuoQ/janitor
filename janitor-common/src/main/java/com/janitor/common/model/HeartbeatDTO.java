package com.janitor.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * ClassName HeartbeatDTO
 * Description
 *
 * @author 曦逆
 * Date 2022/5/16 18:03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeartbeatDTO {
    @NotNull(
            message = "应用名[app]参数缺失"
    )
    private String app;
}
