package com.zhq.taskforge.common;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.zhq.taskforge.model.system.entity.SysMenu;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class TreeSelect {

    private Long id;
    private String label;

    /**
     * 当children需要转化成json对象时，
     * 当children为null,或者children != null 但是 isempty()为true时，children会被忽略，不会包含在生成的json中。
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TreeSelect> children;

    public TreeSelect(SysMenu menu){
        this.id = menu.getMenuId();
        this.label = menu.getMenuName();

        if(menu.getChildren() != null){
            this.children = menu.getChildren().stream()
                    .map(TreeSelect::new)
                    .collect(Collectors.toList());
        }
    }

}
