package com.zhq.taskforge.auth.vo;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RouterVo {
    private String name;
    private String path;
    private boolean hidden;
    private String redirect;
    private String component;
    private String query;
    private Boolean alwaysShow;
    private MetaVo meta;
    private List<RouterVo> children;
}