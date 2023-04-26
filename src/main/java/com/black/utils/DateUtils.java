package com.black.utils;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SuppressWarnings("all")
public class DateUtils {

    public static String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String format(Calendar calendar){
        return format(calendar, DEFAULT_TIME_FORMAT);
    }

    public static String format(Calendar calendar, String format){
        return new SimpleDateFormat(format).format(calendar.getTime());
    }

    public static Calendar setEndMin(Calendar calendar){
        calendar.set(Calendar.HOUR, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar;
    }

    /**
     * @Description: 获取月份第一天
     * @Author: wsp
     **/
    public static Calendar getMonthStart(Date date) { // 月份开始
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(date);
        //set方法将给定日历字段设置为给定值
        //下面代码作用是将日历设置为给定日期月份天数的第一天
        startCalendar.set(Calendar.DAY_OF_MONTH, 1);
        return startCalendar;
    }

    /**
     * @Description: 获取月份最后一天
     * @Author: wsp
     **/
    public static Calendar getMonthEnd(Date date) { // 月份结束
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(date);
        //getActualMaximum方法用来返回给定日历字段属性的最大值
        //endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH) 代码作用为返回日历该月份的最大值
        //set方法，将该日历的给定月份设置为最大天数
        endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return endCalendar;
    }

    /**
     * @Description: 获取季度第一天
     * 1.根据给定日期计算当前季度的第一个月份
     * 2.设置日历的月份为当前季度的第一个月份
     * 3.最后设置日历月份天数为第一天即可
     * @Author: wsp
     **/
    public static Calendar getQuarterStart(Date date) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(date);
        //get方法：获取给定日历属性的值，如 endCalendar.get(Calendar.MONTH) 获取日历的月份
        //计算季度数：由于月份从0开始，即1月份的Calendar.MONTH值为0,所以计算季度的第一个月份只需 月份 / 3 * 3
        startCalendar.set(Calendar.MONTH, (((int) startCalendar.get(Calendar.MONTH)) / 3) * 3);
        startCalendar.set(Calendar.DAY_OF_MONTH, 1);
        return startCalendar;
    }

    /**
     * @Description: 获取季度最后一天
     * @Author: wsp
     **/
    public static Calendar getQuarterEnd(Date date) { // 季度结束
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(date);
        //计算季度数：由于月份从0开始，即1月份的Calendar.MONTH值为0,所以计算季度的第三个月份只需 月份 / 3 * 3 + 2
        endCalendar.set(Calendar.MONTH, (((int) endCalendar.get(Calendar.MONTH)) / 3) * 3 + 2);
        endCalendar.set(Calendar.DAY_OF_MONTH, endCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return endCalendar;
    }

    /**
     * @Description: 获取年份第一天
     * @Author: wsp
     **/
    public static Calendar getYearStart(Date date) { // 年份开始
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(date);
        startCalendar.set(Calendar.DAY_OF_YEAR, 1);
        return startCalendar;
    }

    /**
     * @Description: 获取年份最后一天
     * @Author: wsp
     **/
    public static Calendar getYearEnd(Date date) { // 年份结束
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(date);
        endCalendar.set(Calendar.DAY_OF_YEAR, endCalendar.getActualMaximum(Calendar.DAY_OF_YEAR));
        return endCalendar;
    }

    /**
     * @Description: 获取当前周第一天（周一）
     * @Author: wsp
     **/
    public static Date getWeekStart(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //获取传入日期属于星期几，根据星期几进行不同处理
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        //周日：需要减去6天为周一。（当传入日期为周日时，若我们直接设置日历天数为周一，则日期会变为下一周的周一，而非当前周）
        if (dayOfWeek == 1) {
            calendar.add(Calendar.DAY_OF_MONTH, -6);
        } else {
            //周二 至 周六：直接获取周一即可
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        }
        return calendar.getTime();
    }

    /**
     * @Description: 获取当前周最后一天（周日）
     * @Author: wsp
     **/
    public static Date getWeekEnd(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getWeekStart(date));
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        return calendar.getTime();
    }

}
