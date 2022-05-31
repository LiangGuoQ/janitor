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
 * 应用配置历史记录表
 * </p>
 *
 * @author 曦逆
 * @since 2022-05-30
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_app_conf_his")
@ApiModel(value = "AppConfHis对象", description = "应用配置历史记录表")
public class AppConfHis extends BaseModel {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID", hidden = true)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("应用配置版本")
    private Long version;

    @ApiModelProperty("应用名称")
    private String appName;

    @ApiModelProperty("应用配置KEY")
    private String confKey;

    @ApiModelProperty("应用配置内容")
    private String confValue;

    @ApiModelProperty("备注")
    private String remark;


}
