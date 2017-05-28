package com.traits.db.dao;

import com.traits.model.TaskEntity;
import org.apache.commons.lang3.StringUtils;
import com.traits.db.handler.MySQLHandler;
import com.traits.model.ProjectEntity;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by YeFeng on 2016/7/16.
 */
public class MySQLDao extends BaseDao {

    private String host;
    private int port;
    private String database;
    private String user;
    private String passwd;
    private MySQLHandler handler;

    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public MySQLDao(String host, int port, String database, String user, String passwd) throws SQLException {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.passwd = passwd;
        this.handler = new MySQLHandler(host, port, database, user, passwd);
    }

    public void release() {
        handler.release();
    }

    public ArrayList<ProjectEntity> getProjects() throws SQLException {
        HashMap<String, ArrayList<Object>> result = handler.query("SELECT * from `projectdb`");
        return ProjectEntity.load(result, handler.getCurrentCount());
    }

    public ArrayList<ProjectEntity> getProjects(String[] fields) throws SQLException {
        HashMap<String, ArrayList<Object>> result = handler.query("SELECT %s from `projectdb`",
                new String[]{StringUtils.join(fields, ", ")});
        return ProjectEntity.load(result, handler.getCurrentCount());
    }

    public ProjectEntity getProjectById(String pid) throws SQLException {
        HashMap<String, ArrayList<Object>> result = handler.query("SELECT * from `projectdb` WHERE `id`='%s'",
                new String[]{pid});
        ArrayList<ProjectEntity> projects = ProjectEntity.load(result, handler.getCurrentCount());
        return projects.size() == 0 ? null : projects.get(0);
    }

    public boolean saveOneTask(TaskEntity task) throws SQLException {
        boolean flag =  task.saveTask(handler);
        return flag;
    }

    public ArrayList<TaskEntity> getInitTasks() throws Exception {
        HashMap<String, ArrayList<Object>> result = handler.query("SELECT * from `taskdb` where `status`=0");
        return TaskEntity.load(result, handler.getCurrentCount());
    }

    public ArrayList<TaskEntity> getCheckingTasks() throws Exception {
        HashMap<String, ArrayList<Object>> result = handler.query("SELECT * from `taskdb` where `status`=7");
        return TaskEntity.load(result, handler.getCurrentCount());
    }

    public ArrayList<TaskEntity> getActiveTasks() throws Exception {
        HashMap<String, ArrayList<Object>> result = handler.query("SELECT * from `taskdb` where `status`=1");
        return TaskEntity.load(result, handler.getCurrentCount());
    }

    public HashSet<String> getSuccessOrPassedTasks() throws Exception {

        Date current = new Date();
        Date beforeDate = new Date(current.getTime() - 60L * 60L * 24L * 31L * 1000L);

        HashMap<String, ArrayList<Object>> result = handler.query(
                "SELECT `id` from `taskdb` where (`status`=3 OR `status`=8) AND `lunchtime` > '%s'",
                new String[]{df.format(beforeDate)}
        );

        HashSet<String> taskSet = new HashSet<String>();
        for (int i = 0; i < handler.getCurrentCount(); ++i) {
            taskSet.add((String) result.get("id").get(i));
        }

        return taskSet;
    }

    public static void main(String[] args) throws SQLException {
        MySQLDao ms = new MySQLDao("127.0.0.1", 3306, "scheduler", "dev", "123456");
        ArrayList<ProjectEntity> projects = ms.getProjects();

        for (ProjectEntity i : projects) {
            System.out.println(i.toString());
        }

        Date current = new Date();
        long tmp = current.getTime() - 86400L*1000*30;
        System.out.println(current.getTime());
        System.out.println(tmp);
        Date beforeDate = new Date(tmp);

        System.out.println(df.format(current));
        System.out.println(df.format(beforeDate));
    }
}
