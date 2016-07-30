package com.traits.struts.main;

import com.opensymphony.xwork2.ActionSupport;
import com.traits.struts.authority.User;
import org.apache.struts2.ServletActionContext;
import org.omg.CORBA.Object;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by yefeng on 16/7/30.
 */
public class LoginAction extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();

    public String target = "";
    public String email = "";
    public String password = "";

    public String login() {

        return "login";
    }

    public String check() {

        if (request.getSession().getAttribute("user") != null) {
            if (target.equals("")) {
                return SUCCESS;
            } else {
                return "success_target";
            }
        } else {
            User user = new User();
            user.setId(1);
            user.setUserName("test");
            user.setPwd("test");
            request.getSession().setAttribute("user", user);
            if (target.equals("")) {
                return SUCCESS;
            } else {
                return "success_target";
            }
        }

    }


}
