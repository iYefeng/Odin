package com.traits.scheduler;

import com.traits.jython.JythonEvaluable;
import com.traits.model.entity.TaskDef;
import com.traits.model.entity.TaskInst;
import com.traits.model.Configure;
import com.traits.db.dao.BaseDao;
import com.traits.db.dao.MongoDBDao;
import com.traits.db.dao.MySQLDao;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.digest.DigestUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


/**
 * Created by YeFeng on 2016/5/18.
 */
public class ProjectExecutor implements Job {

    static final Logger logger = Logger.getLogger("scheduler");
    private TaskDef taskDef;
    private String dbtype, host, database, table, user, passwd;
    private int port;


    public ProjectExecutor() {
        Configure conf = Configure.getSingleton();
        dbtype = conf.dbtype;
        host = conf.host;
        port = conf.port;
        database = conf.database;
        user = conf.user;
        passwd = conf.passwd;
    }

    private static SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info(">> ProjectExecutor execute");
        BaseDao _storage = null;

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        this.taskDef = (TaskDef) jobDataMap.get("currentProject");

        logger.debug("running taskDef Id: " + taskDef.getId());
        TaskInst taskInst = new TaskInst();

        Date starttime = new Date();
        Date lunchTime;
        if (taskDef.getCron() != null) {
            ArrayList<Date> t = taskDef.getCron().getTimeBefore(new Date(context.getFireTime().getTime() + 1), 1);
            if (t != null) {
                lunchTime = t.get(0);
            } else {
                lunchTime = context.getFireTime();
            }

        } else {
            lunchTime = context.getFireTime();
        }

        String taskName = String.format("%s @ %s", taskDef.getId(), df.format(lunchTime));



        HashMap<String, Object> args = null;

        String script = taskDef.getArgs_script();
        logger.debug(">> jpython:" + script);
        if (script != null && !script.equals("")) {
            logger.debug(script);
            JythonEvaluable jpython = new JythonEvaluable(script);
            Object ret = jpython.evaluate();
            if (ret instanceof HashMap) {
                args = ((HashMap) ret);
            }
        }

        if (args == null) {
            args = new HashMap<String, Object>();
        }

        args.put("lunchTime", lunchTime.getTime());
        args.put("starttime", starttime.getTime());
        String argsJsonStr = JSON.toJSONString(args);
        logger.debug(argsJsonStr);

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
        }

        for (int j = 0; j < taskDef.getNum_workers(); ++j) {
            String taskId = DigestUtils.md5Hex(taskName) + "#" + String.valueOf(j);
            taskInst.setId(taskId);
            taskInst.setProject_id(taskDef.getId());
            taskInst.setName(taskName);
            taskInst.setLunchtime(((double) lunchTime.getTime()) / 1000.0);
            taskInst.setStarttime(((double) starttime.getTime()) / 1000.0);
            taskInst.setStatus(TaskInst.Status.INIT);
            taskInst.setArgs(argsJsonStr);
            taskInst.setUpdatetime(((double) starttime.getTime()) / 1000.0);
            if (_storage != null) {
                try {
                    _storage.saveOneTask(taskInst);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (_storage != null) {
            _storage.release();
        }
        logger.info("<< ProjectExecutor execute");
    }
}
