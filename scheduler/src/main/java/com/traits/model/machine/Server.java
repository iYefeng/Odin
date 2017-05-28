package com.traits.model.machine;

import com.traits.model.Configure;

/**
 * Created by yefeng on 17/5/28.
 */
public class Server extends Machine {

    private Scheduler scheduler = null;

    public Server(Configure conf) {
        super(conf);
        this.scheduler = new Scheduler(conf);
    }
}
