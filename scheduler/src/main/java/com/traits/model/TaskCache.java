package com.traits.model;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by YeFeng on 2016/7/17.
 */
public class TaskCache {
    private final static TaskCache singleton = new TaskCache();

    static final Logger logger = Logger.getLogger("scheduler");

    TreeMap<Double, TaskEntity> _taskMap = new TreeMap<Double, TaskEntity>(
        new Comparator<Double>() {
            public int compare(Double o1, Double o2){
                if (o1 == null || o2 == null)
                    return 0;
                return o1.compareTo(o2);
            }
        }
    );

    public TreeMap<Double, TaskEntity> get_taskMap() {
        return _taskMap;
    }

    public void set_taskMap(TreeMap<Double, TaskEntity> _taskMap) {
        this._taskMap = _taskMap;
    }

    public static TaskCache getInstance() {
        return singleton;
    }

    public static void main(String[] args) {
        TaskCache t = TaskCache.getInstance();
        t.get_taskMap().put(12.0, new TaskEntity());
        t.get_taskMap().put(1.0, new TaskEntity());
        t.get_taskMap().put(22.0, new TaskEntity());

        System.out.println(t.get_taskMap().firstEntry().getKey());

        t.get_taskMap().put(0.1, new TaskEntity());

        //t.get_taskMap().pollFirstEntry();

        t.get_taskMap().put(2.0, new TaskEntity());

        for (Map.Entry<Double, TaskEntity> tt : t.get_taskMap().entrySet()) {
            System.out.println(tt.getKey());
        }
    }



}
