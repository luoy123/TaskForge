package com.zhq.taskforge.common.filter;

import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;

public class PropertyPreExcludeFilter extends SimplePropertyPreFilter {
    public PropertyPreExcludeFilter() {}

    public PropertyPreExcludeFilter addExcludes(String... filters) {
        for(int i = 0; i < filters.length; i++) {
            //这里继承了SimpleProperyPreFilter,通过getExcludes()可以获取到set集合，然后通过add添加过滤词语
            this.getExcludes().add(filters[i]);
        }
        return this;
    }
}
