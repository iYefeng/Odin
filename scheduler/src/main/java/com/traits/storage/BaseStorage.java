package com.traits.storage;

import com.traits.model.ProjectEntity;
import com.traits.model.TaskEntity;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by YeFeng on 2016/7/16.
 */
public class BaseStorage {

    public BaseStorage() {}

    public void release() {}

    public BaseStorage(String host, int port, String database, String user, String passwd) {}

    public ArrayList<ProjectEntity> getProjects() throws Exception {
        return new ArrayList<ProjectEntity>();
    }

    public ArrayList<ProjectEntity> getProjects(String[] fields) throws SQLException {
        return new ArrayList<ProjectEntity>();
    }

    public ProjectEntity getProjectById(String pid) throws SQLException {
        return new ProjectEntity();
    }

    public ArrayList<TaskEntity> getInitTasks() throws Exception {
        return new ArrayList<TaskEntity>();
    }

    public ArrayList<TaskEntity> getCheckingTasks() throws Exception {
        return new ArrayList<TaskEntity>();
    }

    public ArrayList<TaskEntity> getActiveTasks() throws Exception {
        return new ArrayList<TaskEntity>();
    }

    public HashSet<String> getSuccessOrPassedTasks() throws Exception {
        return new HashSet<String>();
    }

    public boolean saveOneTask(TaskEntity task) throws Exception {
        return false;
    }


}
