package com.traits.model.machine;

import com.traits.model.Configure;

/**
 * Created by yefeng on 17/5/28.
 */
abstract public class Machine {

    protected Configure conf = null;

    public Machine(Configure conf) {
        this.conf = conf;
    }

     void monitor() {}
     void init() {}
     void start() {}
     void stop() {}
     void destory() {}
}
