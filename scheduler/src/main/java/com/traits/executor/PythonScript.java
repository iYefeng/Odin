package com.traits.executor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.traits.model.BaseTask;

import java.io.IOException;

/**
 * Created by YeFeng on 2016/7/24.
 */
public class PythonScript extends BaseExecutor{

    public PythonScript(BaseTask task) {
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
                sb.append(String.format("%s=%s\n", (String) i, obj.getString((String) i).replace("\"", "\\\"")));
            }
        }
        sb.append(script.replace("\"", "\\\""));
        logger.debug(sb.toString());
        String pys = sb.toString();

        String cmd = String.format("python << FEOF\n%s\nFEOF\n", pys);
        logger.debug(cmd);

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