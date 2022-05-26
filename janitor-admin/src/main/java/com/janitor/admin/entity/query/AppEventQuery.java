package com.janitor.admin.entity.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName AppEventQuery
 * Description
 *
 * @author lianggq4
 * Date 2022/5/26 15:15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("应用事件查询Query实体类")
public class AppEventQuery {

    @ApiModelProperty("应用名称")
    private String appName;

    @ApiModelProperty("分页-页码")
    private Long pageNo;

    @ApiModelProperty("分页-页大小")
    private Long pageSize;
}
