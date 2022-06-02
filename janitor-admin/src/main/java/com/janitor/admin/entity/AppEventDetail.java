package com.janitor.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.janitor.admin.base.BaseModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

/**
 * <p>
 * 应用事件明细
 * </p>
 *
 * @author 曦逆
 * @since 2022-05-24
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_app_event_detail")
@ApiModel("应用事件明细实体类")
public class AppEventDetail extends BaseModel {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(hidden = true)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 事件id
     */
    @ApiModelProperty("事件id")
    private Long eventId;

    /**
     * 应用名称
     */
    @ApiModelProperty("应用名称")
    private String appName;

    /**
     * 推送ip
     */
    @ApiModelProperty("推送ip")
    private String targetIp;

    /**
     * 推送结果 2-未知 1-成功 0-失败
     */
    @ApiModelProperty("推送结果 2-未知 1成功 0-失败")
    private Integer pushResult;

    /**
     * 错误信息
     */
    @ApiModelProperty("错误信息")
    private String errorContent;

    /**
     * 实际重试次数
     */
    @ApiModelProperty(hidden = true)
    private Integer retryActive;

    /**
     * 计划重试次数
     */
    @ApiModelProperty(hidden = true)
    private Integer retryPlan;

}
