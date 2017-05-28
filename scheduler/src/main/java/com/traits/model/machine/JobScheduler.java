package com.traits.model.machine;

import com.traits.model.Configure;
import org.apache.log4j.Logger;
import org.quartz.*;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by yefeng on 17/5/28.
 */
public class JobScheduler {

    static final Logger logger = Logger.getLogger("scheduler");
    private Configure conf = null;
    private SchedulerFactory schedFact;
    private static Scheduler sched;

    public JobScheduler(Configure conf) throws SchedulerException {
        this.conf = conf;
        schedFact = new org.quartz.impl.StdSchedulerFactory();
        sched = schedFact.getScheduler();
    }

    public void run() {
        logger.info("setup projectTrigger");
        // define the job and tie it to our HelloJob class
        JobDetail job = newJob(TaskRegister.class)
                .withIdentity("SystemJob", "projectTrigger")
                .build();
        // Trigger the job to run now, and then every 40 seconds
        Trigger trigger = newTrigger()
                .withIdentity("SysytemTrigger", "projectTrigger")
                .startNow()
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(10)
                        .repeatForever())
                .build();

        try {
            // Tell quartz to schedule the job using our trigger
            sched.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        logger.info("setup taskTrigger");
        JobGuard tt = new JobGuard();
        tt.initLoad();

        // define the job and tie it to our HelloJob class
        job = newJob(JobGuard.class)
                .withIdentity("SystemJob", "taskTrigger")
                .build();
        // Trigger the job to run now, and then every 40 seconds
        trigger = newTrigger()
                .withIdentity("SysytemTrigger", "taskTrigger")
                .startNow()
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(20)
                        .repeatForever())
                .build();

        try {
            // Tell quartz to schedule the job using our trigger
            sched.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        try {
            if (!sched.isShutdown())
                sched.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (!sched.isShutdown())
                sched.shutdown();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }


}
