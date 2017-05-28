package com.traits.scheduler;

import com.traits.model.ProjectEntity;
import com.traits.model.Configure;
import com.traits.db.dao.BaseDao;
import com.traits.db.dao.MongoDBDao;
import com.traits.db.dao.MySQLDao;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by YeFeng on 2016/5/18.
 */
public class ProjectScanner {

    private final static ProjectScanner singleton = new ProjectScanner();
    private String dbtype, host, database, table, user, passwd;
    private int port;
    static final Logger logger = Logger.getLogger("scheduler");

    private Map<String, ProjectEntity> _projectMap = new HashMap<String, ProjectEntity>();


    private ProjectScanner() {
        Configure conf = Configure.getSingleton();
        dbtype = conf.dbtype;
        host = conf.host;
        port = conf.port;
        database = conf.database;
        user = conf.user;
        passwd = conf.passwd;
    }

    public static ProjectScanner getInstance() {
        return singleton;
    }

    public boolean loadProjects() {
        logger.info(">> loading projects");
        BaseDao _storage = null;

        try {
            if (dbtype.equals("mysql")) {
                _storage = new MySQLDao(host, port, database, user, passwd);
            } else if (dbtype.equals("mongodb")) {
                _storage = new MongoDBDao(host, port, database, user, passwd);
            } else {
                _storage = new MySQLDao(host, port, database, user, passwd);
            }

            for (Map.Entry<String, ProjectEntity> p: _projectMap.entrySet()) {  //// set project to delete
                p.getValue().set_sysStatus(ProjectEntity.Status.DELETE);
            }

            ArrayList<ProjectEntity> projects_tmp = _storage.getProjects();
            for (ProjectEntity tmp : projects_tmp) {
                String projectId = tmp.getId();
                if (_projectMap.containsKey(projectId)) {                    ///// for old project
                    ProjectEntity sysProject = _projectMap.get(projectId);
                    if (sysProject.getUpdatetime() < tmp.getUpdatetime()) {  // modified project
                        sysProject.copy(tmp);
                        sysProject.set_sysStatus(ProjectEntity.Status.MODIFIED);
                    } else {                                                 // unmodified project
                        sysProject.set_sysStatus(ProjectEntity.Status.DONE);
                    }
                } else {                                                     ///// for new project
                    ProjectEntity newproject = new ProjectEntity();
                    newproject.copy(tmp);
                    newproject.set_sysStatus(ProjectEntity.Status.ADDED);
                    _projectMap.put(newproject.getId(), newproject);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        }

        _storage.release();
        logger.info("<< loading projects");
        return true;
    }

    public Map<String, ProjectEntity> get_projectMap() {
        return _projectMap;
    }

}
