package com.janitor.admin.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ClassName BaseModel
 * Description
 *
 * @author 曦逆
 * Date 2022/5/24 17:16
 */
@Data
public abstract class BaseModel {

    public static final String CREATE_TIME_FIELD_NAME = "createTime";
    public static final String UPDATE_TIME_FIELD_NAME = "updateTime";

    /**
     * 创建时间
     */

    @ApiModelProperty(hidden = true)
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(hidden = true)
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
