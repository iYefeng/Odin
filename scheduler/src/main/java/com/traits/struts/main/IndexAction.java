package com.traits.struts.main;

import com.opensymphony.xwork2.ActionSupport;
import com.traits.struts.authority.Authority;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by YeFeng on 2016/7/24.
 */
public class IndexAction extends ActionSupport {
    HttpServletRequest request = ServletActionContext.getRequest();

    @Authority(actionName="index", privilege="view")
    public String execute() {
        request.setAttribute("hello", "hello world!");
        request.setAttribute("message_count", 4);
        return SUCCESS;
    }



}
