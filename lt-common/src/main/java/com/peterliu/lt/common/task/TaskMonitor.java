package com.peterliu.lt.common.task;

import com.peterliu.lt.common.task.quartz.QuartzTask;
import com.peterliu.lt.common.task.simple.DefaultTask;
import lombok.AllArgsConstructor;
import org.quartz.Trigger;

/**
 * Created by liujun on 2018/1/24.
 */
@AllArgsConstructor
public class TaskMonitor {

    private DefaultTask task;

    /**
     * 打印当前任务状态
     * @return
     */
    public String printLog(){
        StringBuilder log = new StringBuilder();
        log.append("Current DefaultTask Name:").append(task.getName()).append("\n");
        task.readLock().lock();
        try{
            if(task instanceof QuartzTask){
                for (int i = 0; i < task.getThreadSize(); i++) {
                    log.append("[" + i + "]: ");
                    Trigger trigger = ((QuartzTask)task).triggers[i];
                    if (trigger != null) {
                        log.append(trigger.getKey().getName()).append(" [status]: ").append(task.getStatus(i).name());
                    }
                    log.append("\n");
                }
            }else {
                for (int i = 0; i < task.getThreadSize(); i++) {
                    log.append("[" + i + "]: ");
                    Thread thread = task.threads[i];
                    if (thread != null) {
                        log.append(thread.getName()).append(" [status]: ").append(thread.getState().name());
                    }
                    log.append("\n");
                }
            }
        }finally {
            task.readLock().unlock();
        }
        return log.toString();
    }
}
