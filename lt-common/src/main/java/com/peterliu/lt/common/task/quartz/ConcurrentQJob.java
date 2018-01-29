package com.peterliu.lt.common.task.quartz;

import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

/**
 * 允许并发的job, qz会并发调用
 * Created by liujun on 2018/1/27.
 */
@PersistJobDataAfterExecution
public class ConcurrentQJob  implements Job {

    @Setter
    private QuartzTask task;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String name = jobExecutionContext.getTrigger().getKey().getName();
        String index = StringUtils.substringAfter(name, "#");
        Integer integer = Integer.valueOf(index);
        if (integer != null) {
            task.run(integer);
        }else{
            task.run(0);
        }
    }
}