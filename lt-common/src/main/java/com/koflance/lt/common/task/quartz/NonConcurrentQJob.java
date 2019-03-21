package com.koflance.lt.common.task.quartz;

import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.quartz.*;

/**
 * 不允许并发调用
 * Created by liujun on 2018/1/27.
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class NonConcurrentQJob implements InterruptableJob {

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

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        this.task.setJobInterrupted(true);
    }
}
