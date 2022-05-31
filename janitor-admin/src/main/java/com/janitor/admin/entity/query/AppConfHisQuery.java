package com.janitor.admin.entity.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName AppConfHisQuery
 * Description TODO
 *
 * @author 曦逆
 * Date 2022/5/31 13:45
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("应用配置历史记录查询Query实体类")
public class AppConfHisQuery {
    @ApiModelProperty("应用名称")
    private String appName;

    @ApiModelProperty("配置KEY")
    private String confKey;

    @ApiModelProperty("历史版本号")
    private Long version;

    @ApiModelProperty("分页-页码")
    private Long pageNo;

    @ApiModelProperty("分页-页大小")
    private Long pageSize;
}
