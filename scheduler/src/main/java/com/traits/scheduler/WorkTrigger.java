package com.traits.scheduler;

import com.traits.db.handler.RedisHandler;
import com.traits.model.*;
import com.traits.db.dao.BaseDao;
import com.traits.db.dao.MongoDBDao;
import com.traits.db.dao.MySQLDao;
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
        ProjectEntity project = null;

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

            Iterator<Map.Entry<TaskEntity, Future<Integer>>> it = _workers.getFutureList().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<TaskEntity, Future<Integer>> kvTask = it.next();
                TaskEntity task = kvTask.getKey();
                Future<Integer> future = kvTask.getValue();
                if (future.isDone())  {
                    task.setUpdatetime(((double) (new Date()).getTime()) / 1000.0);

                    try {
                        if (future.get() == 0) {
                            task.setEndtime(((double) (new Date()).getTime()) / 1000.0);
                            task.setStatus(TaskEntity.Status.SUCCESS);
                        } else {
                            if (task.getRetry_count() >= task.get_project().getRetry()) {
                                task.setEndtime(((double) (new Date()).getTime()) / 1000.0);
                                task.setStatus(TaskEntity.Status.FAIL);
                            } else {
                                logger.debug("Retry this task");
                                task.setStatus(TaskEntity.Status.ACTIVE);
                                task.setRetry_count(task.getRetry_count() + 1);
                                task.set_project(null);
                                // push to redis
                                redis_handler.getHandler().rpush(
                                        "scheduler.task.queue".getBytes(),
                                        SerializeUtil.serialize(task));
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
                    _workers.getRunningTasks().remove(task.getId().split("#")[0]);
                } else if (future.isCancelled()) {
                    task.setUpdatetime(((double) (new Date()).getTime()) / 1000.0);
                    task.setEndtime(((double) (new Date()).getTime()) / 1000.0);
                    task.setStatus(TaskEntity.Status.DELETE);
                    it.remove();
                    _workers.getRunningTasks().remove(task.getId().split("#")[0]);
                } else {
                    task.setUpdatetime(((double) (new Date()).getTime()) / 1000.0);
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


            byte[] task_s = redis_handler.getHandler().lpop("scheduler.task.queue".getBytes());
            if (task_s == null) return;
            TaskEntity task = (TaskEntity) SerializeUtil.unserialize(task_s);
            if (task == null) return;

            if (_workers.getRunningTasks().contains(task.getId().split("#")[0])) {
                redis_handler.getHandler().rpush(
                        "scheduler.task.queue".getBytes(),
                        SerializeUtil.serialize(task));
                return;
            }

            task.setUpdatetime(((double) (new Date()).getTime()) / 1000.0);

            try {
                project = _storage.getProjectById(task.getProject_id());
            } catch (SQLException e) {
                logger.error("get project error");
                e.printStackTrace();
            }

            if (project == null) {
                logger.info("project is not found");
                task.setStatus(TaskEntity.Status.DELETE);
                try {
                    _storage.saveOneTask(task);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
                return;
            }

            if (project.getStatus() == ProjectEntity.Status.RUNNING
                    || project.getStatus() == ProjectEntity.Status.DEBUG) {
                // run task
                task.setStatus(TaskEntity.Status.RUNNING);
                try {
                    _storage.saveOneTask(task);
                } catch (Exception e) {
                    logger.error("save task error");
                    e.printStackTrace();
                }
                task.set_project(project);
                _workers.submitTask(task);
            } else if (project.getStatus() == ProjectEntity.Status.DELETE) {
                // pass
                task.setStatus(TaskEntity.Status.DELETE);
                try {
                    _storage.saveOneTask(task);
                } catch (Exception e) {
                    logger.error("save task error");
                    e.printStackTrace();
                }
            } else {
                task.setStatus(TaskEntity.Status.STOP);
                try {
                    _storage.saveOneTask(task);
                } catch (Exception e) {
                    logger.error("save task error");
                    e.printStackTrace();
                }
            }
        }
        _storage.release();
        logger.info("<< WorkTrigger execute");
    }
}
