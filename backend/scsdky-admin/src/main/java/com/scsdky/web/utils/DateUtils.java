package com.scsdky.web.utils;

import com.scsdky.common.exception.base.BaseException;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.lang.management.ManagementFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author tubo
 * 日期计算工具类
 * @date 2024/01/12
 */
public class DateUtils {

    public static int getDay(String dateStr1,String dateStr2){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        try {
            dateStr1 = sdf.format(sdf2.parse(dateStr1));
            dateStr2 = sdf.format(sdf2.parse(dateStr2));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long time1 = time(dateStr1);
        long time2 = time(dateStr2);

        // 相减得出的天数mills
        long mills = time1> time2
                ? time1 - time2
                : time2 - time1;
        return (int) (mills / 1000 / 3600 / 24);
    }

    // 封装截取字符串
    public  static long time(String times){
        int year = Integer.parseInt(times.substring(0, 4));
        int month = Integer.parseInt(times.substring(4, 6));
        int day = Integer.parseInt(times.substring(6, 8));
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, day);
        return (c.getTimeInMillis());
    }
    public static String YYYY = "yyyy";

    public static String YYYY_MM = "yyyy-MM";

    public static String MM = "MM";

    public static String DD = "dd";

    public static String YYYY_MM_DD = "yyyy-MM-dd";

    public static String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private static String[] parsePatterns = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     * 获取当前Date型日期
     *
     * @return Date() 当前日期
     */
    public static Date getNowDate()
    {
        return new Date();
    }

    /**
     * 获取当前日期, 默认格式为yyyy-MM-dd
     *
     * @return String
     */
    public static String getDate()
    {
        return dateTimeNow(YYYY_MM_DD);
    }

    /**
     * 获取当前年  yyyy
     *
     * @return String
     */
    public static String getYear()
    {
        return dateTimeNow(YYYY);
    }

    public static String getMonth()
    {
        return dateTimeNow(MM);
    }

    public static String getDay()
    {
        return dateTimeNow(DD);
    }

    public static final String getTime()
    {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    public static final String dateTimeNow()
    {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    public static final String dateTimeNow(final String format)
    {
        return parseDateToStr(format, new Date());
    }

    public static final String dateTime(final Date date)
    {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    public static final String getYearAndMonth(final Date date)
    {
        return parseDateToStr(YYYY_MM, date);
    }

    public static final String parseDateToStr(final String format, final Date date)
    {
        return new SimpleDateFormat(format).format(date);
    }

    public static final Date dateTime(final String format, final String ts)
    {
        try
        {
            return new SimpleDateFormat(format).parse(ts);
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 日期路径 即年/月/日 如2018/08/08
     */
    public static final String datePath()
    {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyy/MM/dd");
    }

    /**
     * 日期路径 即年/月/日 如20180808
     */
    public static final String dateTime()
    {
        Date now = new Date();
        return DateFormatUtils.format(now, "yyyyMMdd");
    }

    public static String getFirstDay(String dateStr, String formatStr) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(formatStr);
            Calendar c = Calendar.getInstance();
            c.setTime(format.parse(dateStr));
            c.add(Calendar.MONTH, 0);
            c.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
            SimpleDateFormat yyyy_mm_dd = new SimpleDateFormat(YYYY_MM_DD);
            return yyyy_mm_dd.format(c.getTime());
        }catch(Exception e){
            throw new BaseException("日期格式错误");
        }
    }

    public static String getLastDay(String dateStr, String formatStr) {
        try {
            SimpleDateFormat format=new SimpleDateFormat(formatStr);
            Calendar c = Calendar.getInstance();
            c.setTime(format.parse(dateStr));
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            SimpleDateFormat yyyy_mm_dd=new SimpleDateFormat(YYYY_MM_DD);
            return yyyy_mm_dd.format(c.getTime());
        }catch(Exception e){
            throw new BaseException("日期格式错误");
        }
    }

    /**
     * 获取服务器启动时间
     */
    public static Date getServerStartDate()
    {
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        return new Date(time);
    }

    /**
     * 计算两个时间差
     */
    public static String getDatePoor(Date endDate, Date nowDate)
    {
        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;
        // long ns = 1000;
        // 获得两个时间的毫秒时间差异
        long diff = endDate.getTime() - nowDate.getTime();
        // 计算差多少天
        long day = diff / nd;
        // 计算差多少小时
        long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /**
     *
     * @param num -1 代表往前一年  1代表往后一年
     * @return
     */
    public static String getYearMonthDay(int num) {
        Calendar calendar2 = Calendar.getInstance();
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        calendar2.add(Calendar.YEAR, num);
        String date = sdf2.format(calendar2.getTime());
        return date;
    }


    /**
     *
     * @param num -1 代表往前一天 1代表后一天
     * @return
     */
    public static String getDay(int num,String time) throws ParseException {
        Calendar calendar2 = Calendar.getInstance();
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
        calendar2.setTime(sdf2.parse(time));
        calendar2.add(Calendar.DATE,  num);
        String date = sdf2.format(calendar2.getTime());
        return date;
    }

    public static void main(String[] args) throws ParseException {
        System.out.println(DateUtils.getDaysInMonth(2024,11));
    }

    /**
     *
     * @param num -1 代表往前一年  1代表往后一年
     * @return
     */
    public static String getYear(int num) {
        Calendar calendar2 = Calendar.getInstance();
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy");
        calendar2.add(Calendar.YEAR, num);
        String date = sdf2.format(calendar2.getTime());
        return date;
    }

    /**
     *
     * @param num -1 代表往前一年  1代表往后一年
     * @return
     */
    public static String getYear(int year,int num) {
        Calendar calendar2 = Calendar.getInstance();
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy");
        calendar2.set(Calendar.YEAR, year);
        calendar2.add(Calendar.YEAR,num);
        return sdf2.format(calendar2.getTime());
    }


    /**
     * 通过年份和月份判断当月天数
     * @param year
     * @param month
     * @return
     */
    public static int getDaysInMonth(int year, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }


    public static long getSecond(Date startDate,Date endDate ) {
        long diff = endDate.getTime() - startDate.getTime();
        return diff / (60 * 1000);
    }
}
