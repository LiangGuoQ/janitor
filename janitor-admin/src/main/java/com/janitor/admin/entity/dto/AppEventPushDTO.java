package com.janitor.admin.entity.dto;

import com.janitor.admin.entity.AppEvent;
import com.janitor.admin.entity.AppEventDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName AppEventPushDTO
 * Description
 *
 * @author lianggq4
 * Date 2022/5/26 14:22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("应用事件推送DTO实体类")
public class AppEventPushDTO {

    @ApiModelProperty("应用名称")
    private String appName;

    @ApiModelProperty("事件类型")
    private String eventType;

    @ApiModelProperty("事件内容")
    private String eventValue;

    @ApiModelProperty("推送ip列表")
    private List<String> targetIpList;

    public AppEvent toEvent() {
        return AppEvent.builder()
                .appName(this.appName)
                .eventType(this.eventType)
                .eventValue(this.eventValue)
                .targetIpList(String.join("|", this.targetIpList))
                .build();
    }

    public List<AppEventDetail> toEventDetailList(Long appEventId) {
        return this.getTargetIpList().stream()
                .map((ip) -> AppEventDetail.builder()
                        .appName(this.appName)
                        .eventId(appEventId)
                        .targetIp(ip)
                        .retryPlan(3)
                        .build()
                )
                .collect(Collectors.toList());
    }
}
