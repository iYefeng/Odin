package com.traits.model.machine;

import com.traits.model.entity.TaskDef;
import com.traits.scheduler.ProjectScanner;
import com.traits.scheduler.SysScheduler;
import org.apache.log4j.Logger;
import org.quartz.*;

import java.util.Map;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;


/**
 * Created by YeFeng on 2016/5/18.
 */

@DisallowConcurrentExecution
public class TaskRegister implements Job {

    static final Logger logger = Logger.getLogger("scheduler");

    final String TRIGGER_GROUP_NAME = "PROJECTTRIGER";
    final String JOB_GROUP_NAME = "PROJECTJOB";
    Scheduler sched = SysScheduler.getScheduler();

    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info(">> TaskRegister STARTING");
        ProjectScanner _projectscanner = ProjectScanner.getInstance();
        boolean flag = _projectscanner.loadProjects();
        if (flag) {
            Map<String, TaskDef> projectMap = _projectscanner.get_projectMap();

            for (Map.Entry<String, TaskDef> item : projectMap.entrySet()) {
                TaskDef p = item.getValue();
                if (p.get_sysStatus() == TaskDef.Status.ADDED) {

                    try {
                        if (p.getStatus() == TaskDef.Status.RUNNING
                                || p.getStatus() == TaskDef.Status.DEBUG) {
                            logger.debug("Add a running project");
                            JobDetail jobDetail = newJob(TaskInstMaker.class)
                                    .withIdentity(p.getId(), JOB_GROUP_NAME).build();
                            jobDetail.getJobDataMap().put("currentProject", p);
                            Trigger trigger = null;
                            if (p.getCrontab() != null && p.getCrontab().equals("")) {
                                trigger = newTrigger()
                                        .withIdentity(p.getId(), TRIGGER_GROUP_NAME)
                                        .withSchedule(simpleSchedule().withRepeatCount(0))
                                        .forJob(jobDetail)
                                        .build();
                            } else if (p.getCrontab() != null) {
                                trigger = newTrigger()
                                        .withIdentity(p.getId(), TRIGGER_GROUP_NAME)
                                        .startNow()
                                        .withSchedule(cronSchedule(p.getCrontab()))
                                        .build();
                            }

                            if (trigger != null) {
                                sched.scheduleJob(jobDetail, trigger);
                            }
                        }

                        if (!sched.isShutdown()) {
                            sched.start(); //
                        }
                    } catch (SchedulerException e) {
                        // TODO Auto-generated catch block
                        logger.debug(e.getMessage());
                        e.printStackTrace();
                    } //

                    p.set_sysStatus(TaskDef.Status.DONE);
                } else if (p.get_sysStatus() == TaskDef.Status.MODIFIED) {

                    try {
                        sched.pauseTrigger(new TriggerKey(p.getId(), TRIGGER_GROUP_NAME));
                        sched.unscheduleJob(new TriggerKey(p.getId(), TRIGGER_GROUP_NAME));//
                        sched.deleteJob(new JobKey(p.getId(), JOB_GROUP_NAME)); //
                    } catch (Exception e) {
                        logger.debug(e.getMessage());
                        e.printStackTrace();
                    }

                    try {
                        // add new task
                        if (p.getStatus() == TaskDef.Status.RUNNING
                                || p.getStatus() == TaskDef.Status.DEBUG) {
                            JobDetail jobDetail = newJob(TaskInstMaker.class)
                                    .withIdentity(p.getId(), JOB_GROUP_NAME).build();
                            jobDetail.getJobDataMap().put("currentProject", p);
                            Trigger trigger = null;
                            if (p.getCrontab() != null && p.getCrontab().equals("")) {
                                trigger = newTrigger()
                                        .withIdentity(p.getId(), TRIGGER_GROUP_NAME)
                                        .withSchedule(simpleSchedule().withRepeatCount(0))
                                        .forJob(jobDetail)
                                        .build();
                            } else if (p.getCrontab() != null){
                                trigger = newTrigger()
                                        .withIdentity(p.getId(), TRIGGER_GROUP_NAME)
                                        .startNow()
                                        .withSchedule(cronSchedule(p.getCrontab()))
                                        .build();
                            }
                            if (trigger != null) {
                                sched.scheduleJob(jobDetail, trigger);
                            }
                        }

                        if (!sched.isShutdown()) {
                            sched.start(); //
                        }
                    } catch (SchedulerException e) {
                        logger.debug(e.getMessage());
                        e.printStackTrace();
                    }

                    p.set_sysStatus(TaskDef.Status.DONE);
                } else if (p.get_sysStatus() == TaskDef.Status.DELETE) {
                    try {
                        sched.pauseTrigger(new TriggerKey(p.getId(), TRIGGER_GROUP_NAME));
                        sched.unscheduleJob(new TriggerKey(p.getId(), TRIGGER_GROUP_NAME));
                        sched.deleteJob(new JobKey(p.getId(), JOB_GROUP_NAME));
                    } catch (SchedulerException e) {
                        // TODO Auto-generated catch block
                        logger.debug(e.getMessage());
                        e.printStackTrace();
                    }

                    p.setStatus(TaskDef.Status.DELETE);
                    p.set_sysStatus(TaskDef.Status.DONE);
                }
            }


        }

        logger.info("<< TaskRegister STOPPED");
    }

}
