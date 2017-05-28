package com.traits.model.machine;

import com.traits.model.Configure;

/**
 * Created by yefeng on 17/5/28.
 */
public class Agent extends Machine {

    private WorkersPool workersPool = null;

    public Agent(Configure conf) {
        super(conf);
        this.workersPool = WorkersPool.getSingleton();
    }

    public WorkersPool getWorkersPool() {
        return workersPool;
    }
}
