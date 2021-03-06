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
 * 应用配置
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
@TableName("t_app_conf")
@ApiModel(value = "AppConf对象", description = "应用配置")
public class AppConf extends BaseModel {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("应用名称")
    private String appName;

    @ApiModelProperty("应用配置KEY")
    private String confKey;

    @ApiModelProperty("应用配置内容")
    private String confValue;

    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty(value = "0-已发布 1-新增 2-修改 3-删除 4-发布后修改", hidden = true)
    private Integer status;
}
