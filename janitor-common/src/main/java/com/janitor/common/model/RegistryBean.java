package com.janitor.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ClassName RegistryBean
 * Description
 *
 * @author 曦逆
 * Date 2022/5/16 18:04
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryBean {
    private String app;
    private String localPath;
    private List<String> data;
    private Boolean event;
}
