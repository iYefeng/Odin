package com.traits.executor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.traits.model.TaskEntity;

import java.io.IOException;

/**
 * Created by YeFeng on 2016/7/23.
 */
public class ShellScript extends BaseExecutor {

    public ShellScript(TaskEntity task) {
        this.task = task;
    }

    public int exec(String script, String args)
    {
        if (script == null || script.equals("")) {
            return 0;
        }
        StringBuilder sb = new StringBuilder();
        if (args != null) {
            JSONObject obj = JSON.parseObject(args);
            for (Object i : obj.keySet()) {
                sb.append(String.format("%s='%s'\n", (String) i, obj.getString((String) i)).replace("\"", "\\\""));
            }
        }
        sb.append(script);
        logger.debug(sb.toString());
        String cmd = sb.toString();

        String[] cmds = { "bash", "-c", cmd };
        Process process;
        int status = -1;
        try
        {
            process = Runtime.getRuntime().exec(cmds);
            BaseExecutor error = new BaseExecutor(process.getErrorStream(), "stderr", task);
            BaseExecutor output = new BaseExecutor(process.getInputStream(), "stdout", task);
            error.start();
            output.start();
            try
            {
                process.waitFor();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            status = process.exitValue();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return status;
    }
}
