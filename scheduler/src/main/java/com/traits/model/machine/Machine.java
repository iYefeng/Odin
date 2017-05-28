package com.traits.model.machine;

import com.traits.model.Configure;

/**
 * Created by yefeng on 17/5/28.
 */
abstract public class Machine {

    protected Configure conf = null;
    protected Monitor monitor = null;

    public Machine(Configure conf) {
        this.conf = conf;
        this.monitor = Monitor.getInstance();
        this.monitor.start();
    }

    void init() {}
    void start() {}
    void stop() {}

    void destory() {
        this.monitor.stop();
    }
}
