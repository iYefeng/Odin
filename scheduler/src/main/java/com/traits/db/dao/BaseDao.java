package com.traits.db.dao;

import com.traits.model.entity.TaskDef;
import com.traits.model.entity.TaskInst;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by YeFeng on 2016/7/16.
 */
public class BaseDao {

    public BaseDao() {}

    public void release() {}

    public BaseDao(String host, int port, String database, String user, String passwd) {}

    public ArrayList<TaskDef> getProjects() throws Exception {
        return new ArrayList<TaskDef>();
    }

    public ArrayList<TaskDef> getProjects(String[] fields) throws SQLException {
        return new ArrayList<TaskDef>();
    }

    public TaskDef getProjectById(String pid) throws SQLException {
        return new TaskDef();
    }

    public ArrayList<TaskInst> getInitTasks() throws Exception {
        return new ArrayList<TaskInst>();
    }

    public ArrayList<TaskInst> getCheckingTasks() throws Exception {
        return new ArrayList<TaskInst>();
    }

    public ArrayList<TaskInst> getActiveTasks() throws Exception {
        return new ArrayList<TaskInst>();
    }

    public HashSet<String> getSuccessOrPassedTasks() throws Exception {
        return new HashSet<String>();
    }

    public boolean saveOneTask(TaskInst taskInst) throws Exception {
        return false;
    }


}
