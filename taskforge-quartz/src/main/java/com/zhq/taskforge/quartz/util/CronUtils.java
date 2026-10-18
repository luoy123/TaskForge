package com.zhq.taskforge.quartz.util;

import java.text.ParseException;
import java.util.Date;

import org.quartz.CronExpression;

public class CronUtils {

    /**
     * 判断表达式是否有效，有效会返回True,否则返回False
     */
    public static boolean isValid(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

    /**
     * 返回Cron表达式无效的信息
     * 无效返回错误描述，有效则返回null
     */
    public static String getInvalidMessage(String cronExpression) {
        try {
            new CronExpression(cronExpression);
            return null;
        } catch (ParseException pe) {
            return pe.getMessage();
        }
    }

    /**
     * 返回下一个执行时间根据Cron表达式
     * 如果存在则返回，没有的话会返回null
     */
    public static Date getNextValidTimeAfter(String cronExpression) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            return cron.getNextValidTimeAfter(new Date(System.currentTimeMillis()));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
