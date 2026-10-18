package com.zhq.taskforge.common.constants;

/**
 * Quartz 调度常量（对齐若依 / pmhub）。
 */
public class ScheduleConstants {

    /** JobKey / TriggerKey 前缀 */
    public static final String TASK_CLASS_NAME = "TASK_CLASS_NAME";

    /** 放入 JobDataMap 的业务任务对象 key */
    public static final String TASK_PROPERTIES = "TASK_PROPERTIES";

    /**
     * misfire 策略（sys_job.misfire_policy）：
     * 0 = 默认（交给调度器默认策略）
     * 1 = 立即触发执行（ignore misfires）
     * 2 = 触发一次执行（fire and proceed）
     * 3 = 不触发立即执行（do nothing）
     */
    public static final String MISFIRE_DEFAULT = "0";

    /** 立即触发执行 */
    public static final String MISFIRE_IGNORE_MISFIRES = "1";

    /** 触发一次执行 */
    public static final String MISFIRE_FIRE_AND_PROCEED = "2";

    /** 不触发立即执行 */
    public static final String MISFIRE_DO_NOTHING = "3";

    public enum Status {
        /** 0 = 正常调度 */
        NORMAL("0"),
        /** 1 = 暂停 */
        PAUSE("1");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
