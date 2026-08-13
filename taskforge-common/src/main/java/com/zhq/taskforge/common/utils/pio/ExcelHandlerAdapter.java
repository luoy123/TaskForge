package com.zhq.taskforge.common.utils.pio;

public interface ExcelHandlerAdapter {
    /**
     * 格式化
     *
     * @param value 单元格数据值
     * @param args  excel注解args参数组
     * @return 处理后的值
     */
    Object format(Object value, String[] args);
}