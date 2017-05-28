package com.traits.model.machine;

import com.traits.model.Configure;
import com.traits.model.TaskEntity;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by YeFeng on 2016/7/20.
 */
public class WorkersPool {

    static final Logger logger = Logger.getLogger("scheduler");
    private final static WorkersPool singleton = new WorkersPool();
    HashMap<TaskEntity, Future<Integer>> futureList = new HashMap<TaskEntity, Future<Integer>>();
    HashSet<String> runningTasks = new HashSet<String>();
    private ThreadPoolExecutor threadPool;
    private int POOL_SIZE = 4;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private WorkersPool() {
        Configure conf = Configure.getSingleton();
        POOL_SIZE = conf.POOL_SIZE;
        threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(POOL_SIZE);
    }

    public static WorkersPool getSingleton() {
        return singleton;
    }

    public int getWorkingCount() {
        return threadPool.getActiveCount();
    }

    public boolean submitTask(TaskEntity task) {
        Future<Integer> future = threadPool.submit(task);
        futureList.put(task, future);
        runningTasks.add(task.getId().split("#")[0]);
        return true;
    }

    public boolean removeTaskFromFutureList(TaskEntity t) {
        futureList.remove(t);
        return true;
    }

    public HashMap<TaskEntity, Future<Integer>> getFutureList() {
        return futureList;
    }

    public void setFutureList(HashMap<TaskEntity, Future<Integer>> futureList) {
        this.futureList = futureList;
    }

    public HashSet<String> getRunningTasks() {
        return runningTasks;
    }

    public void setRunningTasks(HashSet<String> runningTasks) {
        this.runningTasks = runningTasks;
    }
}
