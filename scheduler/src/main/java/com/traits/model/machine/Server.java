package com.traits.model.machine;

import com.traits.model.Configure;
import org.quartz.SchedulerException;

/**
 * Created by yefeng on 17/5/28.
 */
public class Server extends Machine {

    private JobScheduler jobScheduler = null;

    private boolean isMaster = true;

    public Server(Configure conf) {
        super(conf);
        try {
            this.jobScheduler = new JobScheduler(conf);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void init() {

    }

    public void start() {

    }

    public void stop() {

    }
}
