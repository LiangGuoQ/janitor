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
 * 应用版本表
 * </p>
 *
 * @author 曦逆
 * @since 2022-05-31
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_app_version")
@ApiModel(value = "AppVersion对象", description = "应用版本表")
public class AppVersion extends BaseModel {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键ID", hidden = true)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("应用名称")
    private String appName;

    @ApiModelProperty("版本号")
    private Long version;


}
