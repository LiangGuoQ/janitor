package com.janitor.admin.entity.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName AppEventDetailQuery
 * Description
 *
 * @author 曦逆
 * Date 2022/5/26 15:54
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("应用事件详细查询Query实体类")
public class AppEventDetailQuery {

    @ApiModelProperty("应用事件ID")
    private Long eventId;

    @ApiModelProperty("分页-页码")
    private Long pageNo;

    @ApiModelProperty("分页-页大小")
    private Long pageSize;

}
