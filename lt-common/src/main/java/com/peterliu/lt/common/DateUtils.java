package com.peterliu.lt.common;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liujun on 2018/1/26.
 */
@Slf4j
public class DateUtils  extends org.apache.commons.lang.time.DateUtils {



    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD_HH_MM_SS_COMPOSE = "yyyy-MM-dd_HH_mm_ss";
    public static final String NOW = "now";
    private static final Pattern PATTERN = Pattern.compile("\\$\\{([yMdEaHhms :\\-\\u4e00-\\u9fa5]+) *\\|(( *[-]?[0-9]+[dMyHmsE] *,?)+)\\}");

    public static Date parse(String dateStr, String pattern) {
        if (StringUtils.isBlank(dateStr) || StringUtils.isBlank(pattern)) {
            return null;
        }
        try {
            return getSimpleDateFormat(pattern).parse(dateStr);
        } catch (Exception e) {
            ;
        }
        return null;
    }

    public static String formatNow(String pattern) {
        return format(new Date(), pattern);
    }

    /**
     * 格式化时间
     *
     * @param date    如果为null，则取当前时间
     * @param pattern 支持两种格式，1、java date的formate格式，例如yyyyMMdd；2、beerus自定义格式，例如${yyyyMMdd|-1d}，其中yyyyMMdd是输出的日期格式，-1d是需要前一天的日期，也可以d-天；M-月；y-年；H-小时；m-分钟；s秒；E-星期
     * @return
     */
    public static String format(Date date, String pattern) {
        if (date == null || StringUtils.isBlank(pattern)) {
            return null;
        }
        try {
            String originPattern = pattern;
            Date originDate = date;
            Matcher matcher = PATTERN.matcher(pattern);
            boolean isFind = false;
            while (matcher.find()) {
                /*beerus自定义格式*/
                isFind = true;
                /*重置初始时间*/
                date = originDate;
                String needReplace = matcher.group(0);
                String newPattern = matcher.group(1);
                String[] offsets = StringUtils.split(matcher.group(2), ",");
                if (offsets != null && offsets.length > 0) {
                    for (String offset : offsets) {
                        offset = StringUtils.trim(offset);
                        int offsetNum = Integer.parseInt(StringUtils.substring(offset, 0, offset.length() - 1));
                        String offsetUnit = StringUtils.substring(offset, offset.length() - 1, offset.length());
                        if (StringUtils.equals(offsetUnit, "d")) {
                            date = addDays(date, offsetNum);
                        } else if (StringUtils.equals(offsetUnit, "d")) {
                            date = addDays(date, offsetNum);
                        } else if (StringUtils.equals(offsetUnit, "M")) {
                            date = addMonths(date, offsetNum);
                        } else if (StringUtils.equals(offsetUnit, "y")) {
                            date = addYears(date, offsetNum);
                        } else if (StringUtils.equals(offsetUnit, "H")) {
                            date = addHours(date, offsetNum);
                        } else if (StringUtils.equals(offsetUnit, "m")) {
                            date = addMinutes(date, offsetNum);
                        } else if (StringUtils.equals(offsetUnit, "s")) {
                            date = addSeconds(date, offsetNum);
                        } else if (StringUtils.equals(offsetUnit, "E")) {
                            date = addWeeks(date, offsetNum);
                        }
                    }
                }
                String format = getSimpleDateFormat(newPattern).format(date);
                originPattern = StringUtils.replace(originPattern, needReplace, format);
            }
            if (isFind) {
                /*beerus自定义格式*/
                return originPattern;
            } else {
                /*java date默认格式*/
                return getSimpleDateFormat(pattern).format(date);
            }
        } catch (Exception e) {
            ;
        }
        return null;
    }

    private static ThreadLocal<Map<String, SimpleDateFormat>> SIMPLE_MAP = new ThreadLocal<Map<String, SimpleDateFormat>>() {
        @Override
        protected Map<String, SimpleDateFormat> initialValue() {
            return Maps.newHashMap();
        }
    };

    private static SimpleDateFormat getSimpleDateFormat(String pattern) {
        Map<String, SimpleDateFormat> stringSimpleDateFormatMap = SIMPLE_MAP.get();
        if (stringSimpleDateFormatMap.containsKey(pattern)) {
            return stringSimpleDateFormatMap.get(pattern);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        stringSimpleDateFormatMap.put(pattern, simpleDateFormat);
        return simpleDateFormat;
    }

    /**
     * 获取指定日期的date对象
     *
     * @param mmdd 例如 11-11
     * @return 返回当前年份对应日期的时间
     */
    public static Date fromMMdd(String mmdd) {
        if (StringUtils.equalsIgnoreCase(mmdd, NOW)) {
            return new Date();
        } else if (StringUtils.startsWithIgnoreCase(mmdd, NOW)) {
            int amount = 0;
            try {
                amount = Integer.valueOf(StringUtils.substring(mmdd, NOW.length()));
            } catch (Exception e) {
                log.error("failToParseMMDD", e);
            }
            return addDays(new Date(), amount);
        }
        String[] split = StringUtils.split(mmdd, "-");
        if (split != null && split.length == 2) {
            Date date = new Date();
            date = setMonths(date, Integer.valueOf(split[0]) - 1);
            date = setDays(date, Integer.valueOf(split[1]));
            return date;
        }
        return null;
    }
}
