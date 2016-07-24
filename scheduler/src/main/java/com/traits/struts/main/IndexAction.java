package com.traits.struts.main;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by YeFeng on 2016/7/24.
 */
public class IndexAction extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();

    public String execute() {
        request.setAttribute("hello", "hello world!");
        return SUCCESS;
    }
}
