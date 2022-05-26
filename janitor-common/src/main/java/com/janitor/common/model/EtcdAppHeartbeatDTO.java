package com.janitor.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * ClassName EtcdAppHeartbeatDTO
 * Description
 *
 * @author 曦逆
 * Date 2022/5/16 17:52
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EtcdAppHeartbeatDTO implements Serializable {
    @NotNull(
            message = "心跳时间不能为空"
    )
    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "GMT+8"
    )
    private Date beatTime;
    @NotBlank(
            message = "ip地址不能为空"
    )
    private String ip;
    @NotNull(
            message = "应用名称不能为空"
    )
    private String appName;

    @Override
    public String toString() {
        return "EtcdAppHeartbeatDTO{beatTime='" + this.beatTime + '\'' + ", ip='" + this.ip + '\'' + ", appName='" + this.appName + '\'' + '}';
    }
}

