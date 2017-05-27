package com.traits.struts.authority;

import java.io.Serializable;

/**
 * Created by yefeng on 16/7/30.
 */
public class User implements Serializable {
    private Integer id;
    private String userName;
    private String pwd;
    
    public User() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

}
