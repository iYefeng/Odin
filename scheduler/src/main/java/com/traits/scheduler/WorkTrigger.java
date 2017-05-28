package com.traits.scheduler;

import com.traits.db.handler.RedisHandler;
import com.traits.model.*;
import com.traits.db.dao.BaseDao;
import com.traits.db.dao.MongoDBDao;
import com.traits.db.dao.MySQLDao;
import com.traits.model.entity.TaskDef;
import com.traits.model.entity.TaskInst;
import com.traits.model.machine.WorkersPool;
import com.traits.util.SerializeUtil;
import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by YeFeng on 2016/7/19.
 */
@DisallowConcurrentExecution
public class WorkTrigger implements Job {

    static final Logger logger = Logger.getLogger("scheduler");

    private String dbtype, host, database, user, passwd, redis_host, redis_db;
    private int port, redis_port, maxActiveCount;

    private static RedisHandler redis_handler = null;

    public WorkTrigger() {
        Configure conf = Configure.getSingleton();
        dbtype = conf.dbtype;
        host = conf.host;
        port = conf.port;
        database = conf.database;
        user = conf.user;
        passwd = conf.passwd;

        redis_host = conf.redis_host;
        redis_port = conf.redis_port;
        redis_db = conf.redis_db;
        maxActiveCount = conf.maxActiveCount;

        if (redis_handler == null) {
            try {
                redis_handler = new RedisHandler(redis_host, redis_port, redis_db);
            } catch (Exception e) {
                logger.error("Redis init error");
                e.printStackTrace();
            }
        }
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {

        logger.info(">> WorkTrigger execute");

        BaseDao _storage = null;
        TaskDef taskDef = null;

        try {
            if (dbtype.equals("mysql")) {
                _storage = new MySQLDao(host, port, database, user, passwd);
            } else if (dbtype.equals("mongodb")) {
                _storage = new MongoDBDao(host, port, database, user, passwd);
            } else {
                _storage = new MySQLDao(host, port, database, user, passwd);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return;
        }

        if (redis_handler != null) {

            WorkersPool _workers = WorkersPool.getSingleton();

            Iterator<Map.Entry<TaskInst, Future<Integer>>> it = _workers.getFutureList().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<TaskInst, Future<Integer>> kvTask = it.next();
                TaskInst taskInst = kvTask.getKey();
                Future<Integer> future = kvTask.getValue();
                if (future.isDone())  {
                    taskInst.setUpdatetime(((double) (new Date()).getTime()) / 1000.0);

                    try {
                        if (future.get() == 0) {
                            taskInst.setEndtime(((double) (new Date()).getTime()) / 1000.0);
                            taskInst.setStatus(TaskInst.Status.SUCCESS);
                        } else {
                            if (taskInst.getRetry_count() >= taskInst.get_taskDef().getRetry()) {
                                taskInst.setEndtime(((double) (new Date()).getTime()) / 1000.0);
                                taskInst.setStatus(TaskInst.Status.FAIL);
                            } else {
                                logger.debug("Retry this taskInst");
                                taskInst.setStatus(TaskInst.Status.ACTIVE);
                                taskInst.setRetry_count(taskInst.getRetry_count() + 1);
                                taskInst.set_taskDef(null);
                                // push to redis
                                redis_handler.getHandler().rpush(
                                        "scheduler.taskInst.queue".getBytes(),
                                        SerializeUtil.serialize(taskInst));
                            }

                        }
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }

                    it.remove();
                    _workers.getRunningTasks().remove(taskInst.getId().split("#")[0]);
                } else if (future.isCancelled()) {
                    taskInst.setUpdatetime(((double) (new Date()).getTime()) / 1000.0);
                    taskInst.setEndtime(((double) (new Date()).getTime()) / 1000.0);
                    taskInst.setStatus(TaskInst.Status.DELETE);
                    it.remove();
                    _workers.getRunningTasks().remove(taskInst.getId().split("#")[0]);
                } else {
                    taskInst.setUpdatetime(((double) (new Date()).getTime()) / 1000.0);
                }

                try {
                    _storage.saveOneTask(kvTask.getKey());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
            }


            if (_workers.getWorkingCount() >= maxActiveCount) {
                logger.info("The number of active tasks is larger than maxActiveCount");
                return;
            }


            byte[] task_s = redis_handler.getHandler().lpop("scheduler.taskInst.queue".getBytes());
            if (task_s == null) return;
            TaskInst taskInst = (TaskInst) SerializeUtil.unserialize(task_s);
            if (taskInst == null) return;

            if (_workers.getRunningTasks().contains(taskInst.getId().split("#")[0])) {
                redis_handler.getHandler().rpush(
                        "scheduler.taskInst.queue".getBytes(),
                        SerializeUtil.serialize(taskInst));
                return;
            }

            taskInst.setUpdatetime(((double) (new Date()).getTime()) / 1000.0);

            try {
                taskDef = _storage.getProjectById(taskInst.getProject_id());
            } catch (SQLException e) {
                logger.error("get taskDef error");
                e.printStackTrace();
            }

            if (taskDef == null) {
                logger.info("taskDef is not found");
                taskInst.setStatus(TaskInst.Status.DELETE);
                try {
                    _storage.saveOneTask(taskInst);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
                return;
            }

            if (taskDef.getStatus() == TaskDef.Status.RUNNING
                    || taskDef.getStatus() == TaskDef.Status.DEBUG) {
                // run taskInst
                taskInst.setStatus(TaskInst.Status.RUNNING);
                try {
                    _storage.saveOneTask(taskInst);
                } catch (Exception e) {
                    logger.error("save taskInst error");
                    e.printStackTrace();
                }
                taskInst.set_taskDef(taskDef);
                _workers.submitTask(taskInst);
            } else if (taskDef.getStatus() == TaskDef.Status.DELETE) {
                // pass
                taskInst.setStatus(TaskInst.Status.DELETE);
                try {
                    _storage.saveOneTask(taskInst);
                } catch (Exception e) {
                    logger.error("save taskInst error");
                    e.printStackTrace();
                }
            } else {
                taskInst.setStatus(TaskInst.Status.STOP);
                try {
                    _storage.saveOneTask(taskInst);
                } catch (Exception e) {
                    logger.error("save taskInst error");
                    e.printStackTrace();
                }
            }
        }
        _storage.release();
        logger.info("<< WorkTrigger execute");
    }
}
