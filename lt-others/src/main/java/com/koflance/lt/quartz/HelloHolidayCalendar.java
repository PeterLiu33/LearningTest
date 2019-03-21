package com.koflance.lt.quartz;

import com.google.common.collect.Sets;
import lombok.Getter;
import org.quartz.impl.calendar.HolidayCalendar;

import java.util.Date;
import java.util.Set;

/**
 * 测试HolidayCalendar类，用于指定排除掉的商业日期，在商业日期内不执行
 * <p>
 * Created by liujun on 2018/1/26.
 */
public class HelloHolidayCalendar {

    @Getter
    HolidayCalendar cal = new HolidayCalendar();

    // 商业节假日期
    Set<Date> holidaySet = Sets.newHashSet();

    /**
     * 添加商业节假日
     *
     * @param holiday
     */
    public void addHoliday(Date holiday) {
        cal.addExcludedDate(holiday);
    }

    /**
     * 删除指定的商业节假日
     *
     * @param holiday
     */
    public void removeHoliday(Date holiday) {
        holidaySet.remove(holiday);
        cal.removeExcludedDate(holiday);
    }

    /**
     * 清除所有的商业节假日
     */
    public void removeAll(){
        for (Date date : holidaySet) {
            cal.removeExcludedDate(date);
        }
        holidaySet.clear();
    }
}
