package com.janitor.admin.entity.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName AppConfQuery
 * Description TODO
 *
 * @author 曦逆
 * Date 2022/5/31 17:21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("应用配置查询Query实体类")
public class AppConfQuery {

    @ApiModelProperty("应用名称")
    private String appName;

    @ApiModelProperty("配置KEY")
    private String confKey;

    @ApiModelProperty("分页-页码")
    private Long pageNo;

    @ApiModelProperty("分页-页大小")
    private Long pageSize;

}
