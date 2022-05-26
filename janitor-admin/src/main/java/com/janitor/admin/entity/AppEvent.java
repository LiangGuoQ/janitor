package com.janitor.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.janitor.admin.base.BaseModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * <p>
 * 应用事件
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
@TableName("t_app_event")
@ApiModel("应用事件实体类")
public class AppEvent extends BaseModel {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(hidden = true)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 应用名称
     */
    @ApiModelProperty("应用名称")
    private String appName;

    /**
     * 事件类型
     */
    @ApiModelProperty("事件类型")
    private String eventType;

    /**
     * 事件内容
     */
    @ApiModelProperty("事件内容")
    private String eventValue;

    /**
     * 推送ip列表
     */
    @ApiModelProperty("推送ip列表")
    private String targetIpList;


}
