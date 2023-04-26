package com.black.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

@SuppressWarnings("all")
public class CorrectTime {

    /** 日志 */
    private Logger logger = LoggerFactory.getLogger(CorrectTime.class);
    public static Integer[] odd = new Integer[]{1,3,5,7,8,10,12}; /** 一月有31天 */
    public static Integer[] even = new Integer[]{4,6,9,11};   /** 一月有30天 */
    private boolean leapYear = false;    /** 是否是闰年(闰年29天。平年28天) */
    private boolean isOdd = false;          /** 是否是 31 天月份 */
    private boolean isSecondMonth = false;  /** 优先级更高，是否是二月 */
    private @Setter Calendar calendar;
    /***
     * 目地，传递任意一个 min，hour，day，month 数值，则都可以传遍为
     * yyyy-MM-dd mm:hh:ss 格式的时间，并且如果传递的是负值，那么也应该减去值
     */

    /** 初始化时间 */
    public void initTime(){
        calendar = Calendar.getInstance();
    }

    public CorrectTime(){
        this(Calendar.getInstance());
    }

    public CorrectTime(Calendar calendar){
        this.calendar = calendar;
    }

    @Data
    @Builder
    @AllArgsConstructor
    static class AycTime{

        /** 封装 */
        Integer min , hour , day , month , year;
    }

    public String handlerTimeStr(Integer hour, Integer day)
    {
        return handlerTimeStr(0, hour, day);
    }

    public String handlerTimeStr(Integer min,Integer hour,Integer day) {
        return handlerTimeStr(min, hour, day, calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR));
    }

    /***
     * 返回加工过后的时间字符串，目前为止，允许hour可以写任意值，然后
     * 再进行修正,之后 min 也会可以进行修正
     * @param min 分钟数
     * @param hour 小时数
     * @param day 天数
     * @param month 月份数
     * @param year 年份数
     * @return 返回时间字符串例如: 2021-09-22 13:43:00
     * @throws Exception 中途可能出现的所有异常
     */
    public String handlerTimeStr(Integer min, Integer hour, Integer day, Integer month, Integer year)
   {

        String str;
        try {

            //判断值是否正常
            inspectVal(day, month, year);

            //判断年份
            judgeLeapYear(year);

            //判断月份的范围
            judgeLeapMonth(month);
            AycTime aycTime;
            //修正 min
            recursiveRepairMin(aycTime = new AycTime(min, hour, day, month, year));
            //修正 hour
            recursiveRepairHour(aycTime);
            //修正 day
            recursiveRepairDay(aycTime);
            //修正 month
            recursiveRepairMonth(aycTime);
            //创建时间字符串
            str = buildStr(aycTime);
            logger.info("AYC:生成的时间字符串:"+str);
        }finally {
            isOdd = false;
            isSecondMonth = false;
            leapYear = false;
        }
        return str;
    }

    /** 判断值是否正常 */
    void inspectVal(Integer day, Integer month, Integer year){
        if (day == null || month == null || year == null)
            throw new RuntimeException("不允许空值: day:"+day+" month:"+month+" year:"+year);

        if (day < 0 || day > 31 || month < 0 || month > 12 || year < 0)
            throw new RuntimeException("传递时间值有问题:"+day+" -> "+month+" -> "+year);
    }

    /** 判断这个年份是否为闰年 */
    void judgeLeapYear(Integer year){
        leapYear = year%4 == 0 && year%100!=0 || year%400 == 0;
    }

    /** 判断月份的范围 **/
    void judgeLeapMonth(Integer month){
        if (month == 2){
            isSecondMonth = true;
            return;
        }else
            isSecondMonth = false;
        for (Integer oadd : odd) {

            if (oadd == month){
                isOdd = true;
                return;
            }
        }

        for (Integer e : even) {
            if (e == month){
                isOdd = false;
                return;
            }
        }
    }

    /** 修正分钟 */
    AycTime recursiveRepairMin(AycTime time){

        Integer min;
        if ((min = time.min) >= 0 && min < 60)
            return time;

        if (min < 0){
            time.setMin(60 + min);
            time.setHour(time.hour - 1);
        }else {
            time.setMin(min - 60);
            time.setHour(time.hour + 1);
        }
        return recursiveRepairMin(time);
    }

    /** 递归修正 hour */
    AycTime recursiveRepairHour(AycTime time){
        Integer hour;

        //正常范围:
        if ((hour = time.hour) >= 0 && hour < 24 )
            return time;

        if (hour < 0 ){
            time.setDay(time.day - 1);
            time.setHour(time.hour + 24);
        }
        else {
            time.setDay(time.day + 1);
            time.setHour(time.hour - 24);
        }
        return recursiveRepairHour(time);
    }

    /** 修正 day */
    AycTime recursiveRepairDay(AycTime time){

        Integer day = null;
        judgeLeapMonth(time.month);

        //  如果是2月，的正常情况
        if (isSecondMonth){
            if (leapYear){
                if ((day = time.day) > 0 && day <= 29)
                    return time;
            }else {
                if ((day = time.day) > 0 && day <= 28)
                    return time;
            }
        }

        // 30月份的正常情况
        if (isOdd && ((day = time.day) > 0 && day <= 31))
            return time;

        else if ((day = time.day) > 0 && day <= 30)
            return time;

        //如果是二月
        if (isSecondMonth){

                if (day < 0){
                    time.setDay(leapYear ? 31 : 30 + day);
                    time.setMonth(time.month - 1);
                    return recursiveRepairDay(time);
                }else {

                    time.setDay(day - (leapYear ? 29 : 28));
                    time.setMonth(time.month + 1);
                    return recursiveRepairDay(time);
                }
        }


        if (day <=  0){
            time.setMonth(time.month - 1);
            judgeLeapMonth(time.month);

            /* 防止出现3月 -1情况 */
            if (isSecondMonth)
                time.setDay((leapYear ? 29+day : 28+day));
            else
                time.setDay((isOdd ? 31 + day : 30 + day));

            //如果 day > 应该的天数
        }else {

            time.setMonth(time.month + 1);
            judgeLeapMonth(time.month);
            time.setDay(isOdd ? day - 31 : day - 30);
        }

        return recursiveRepairDay(time);
    }

    /** 修正 month */
    AycTime recursiveRepairMonth(AycTime time){

        Integer month;
        if ((month = time.month) > 0 && month <= 12)
            return time;
        if (month <= 0){
            time.setMonth(12 + month);
            time.setYear(time.year - 1);
        }else {

            time.setMonth(month - 12);
            time.setYear(time.year + 1);
        }
        return recursiveRepairMonth(time);
    }

    public String endThisMonth(Integer month){
        return endThisMonth(month, 0);
    }

    public String endThisMonth(Integer month, Integer hour)
    {
        return endThisMonth(month, hour, 0);
    }

    public String endThisMonth(Integer month, Integer hour, Integer min){
        int year;
        judgeLeapYear(year = calendar.get(Calendar.YEAR));
        AycTime aycTime;
        try {

            for (Integer o : odd) {
                if (month == o)
                {
                    isOdd = true;
                    break;
                }
            }

            if (!isOdd)
                isSecondMonth = month == 2;

            aycTime = AycTime.builder()
                    .day(isOdd ? 31 : (isSecondMonth ? (leapYear ? 29 : 28) : 30))
                    .min(min)
                    .hour(hour)
                    .year(year)
                    .month(month)
                    .build();

        }finally {
            isOdd = false;
            isSecondMonth = false;
            leapYear = false;
        }

        return buildStr(aycTime);
    }

    public String startThisMonth(Integer month){
        return startThisMonth(month, 0 ,0);
    }

    public String startThisMonth(Integer month, Integer hour){
        return startThisMonth(month, hour, 0);
    }

    public String startThisMonth(Integer month, Integer hour, Integer min){

        return buildStr(AycTime.builder()
                .year(calendar.get(Calendar.YEAR))
                .month(month)
                .day(1)
                .hour(hour)
                .min(min)
                .build());
    }

    String buildStr(AycTime aycTime){
        String str = aycTime.year + "-" + (aycTime.month < 10 ? "0" + aycTime.month : aycTime.month) + "-" + (aycTime.day < 10 ? "0" + aycTime.day : aycTime.day) + " "
                + (aycTime.hour < 10 ? "0" + aycTime.hour : aycTime.hour) + ":" + (aycTime.min < 10 ? "0" + aycTime.min : aycTime.min) + ":00";

        //logger.info(str);
        return str;
    }
}
