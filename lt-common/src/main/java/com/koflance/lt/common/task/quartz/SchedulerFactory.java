package com.koflance.lt.common.task.quartz;

import com.koflance.lt.common.DateUtils;
import com.koflance.lt.common.task.Task;
import lombok.AllArgsConstructor;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.PropertySettingJobFactory;
import org.quartz.spi.TriggerFiredBundle;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 任务调度器创建工厂
 * <p>
 * Created by liujun on 2018/1/26.
 */
public class SchedulerFactory {

    // 名称后缀
    private String suffix = "@" + DateUtils.formatNow(DateUtils.YYYY_MM_DD_HH_MM_SS_COMPOSE);
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final Task task;
    // 配置属性
    private final Properties props;

    public SchedulerFactory(Task task) {
        this.task = task;
        props = new Properties();
        props.setProperty("org.quartz.threadPool.threadCount", String.valueOf(task.getThreadSize()));
        props.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
    }

    /**
     * 每次都创建一个新的
     *
     * @return
     */
    public Scheduler newScheduler() {
        try {
            props.setProperty("org.quartz.scheduler.instanceName",
                    task.getName() + "#" + threadNumber.getAndIncrement() + suffix);
            StdSchedulerFactory factory = new StdSchedulerFactory(props);
            Scheduler scheduler = factory.getScheduler();
            scheduler.setJobFactory(new DefaultJobFactory(task));
            return scheduler;
        } catch (SchedulerException e) {
            throw new RuntimeException("failToCreateScheduler!", e);
        }
    }

    @AllArgsConstructor
    static class DefaultJobFactory extends PropertySettingJobFactory {

        private Task task;

        @Override
        public Job newJob(TriggerFiredBundle bundle, Scheduler scheduler) throws SchedulerException {
            Job job = super.newJob(bundle, scheduler);
            addTaskInstance(job, task);
            return job;
        }

        // 通过反射方式，这是task属性
        private void addTaskInstance(Job job, Task task) throws SchedulerException {
            BeanInfo bi = null;

            try {
                bi = Introspector.getBeanInfo(job.getClass());
            } catch (IntrospectionException var15) {
                this.handleError("Unable to introspect Job class.", var15);
            }
            PropertyDescriptor[] propDescs = bi.getPropertyDescriptors();
            String methName = "setTask";
            Method setMeth = this.getSetMethod(methName, propDescs);
            Class<?> paramType = null;
            Object o = null;
            try {
                if (setMeth == null) {
                    this.handleError("No setter on Job class " + job.getClass().getName() + " for property 'task'");
                } else {
                    paramType = setMeth.getParameterTypes()[0];
                    if (!paramType.isPrimitive()) {
                        setMeth.invoke(job, new Object[]{task});
                    }
                }
            } catch (Exception e) {
                this.handleError("The setter on Job class " + job.getClass().getName() + " for property 'task' could not be accessed.", e);
            }
        }

        private void handleError(String message, Exception e) throws SchedulerException {
            if (this.isThrowIfPropertyNotFound()) {
                throw new SchedulerException(message, e);
            } else {
                if (this.isWarnIfPropertyNotFound()) {
                    if (e == null) {
                        this.getLog().warn(message);
                    } else {
                        this.getLog().warn(message, e);
                    }
                }

            }
        }

        private Method getSetMethod(String name, PropertyDescriptor[] props) {
            for (int i = 0; i < props.length; ++i) {
                Method wMeth = props[i].getWriteMethod();
                if (wMeth != null && wMeth.getParameterTypes().length == 1 && wMeth.getName().equals(name)) {
                    return wMeth;
                }
            }
            return null;
        }

        private void handleError(String message) throws SchedulerException {
            this.handleError(message, (Exception) null);
        }
    }
}
