package com.traits.executor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.traits.model.entity.TaskInst;

import java.io.IOException;

/**
 * Created by YeFeng on 2016/7/24.
 */
public class PythonScript extends BaseExecutor{

    public PythonScript(TaskInst taskInst) {
        this.taskInst = taskInst;
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
                // TODO for windows replace("\"", "\\\"")
                sb.append(String.format("%s=%s\n", i, obj.getString((String) i)));
            }
        }
        // TODO for windows replace("\"", "\\\"")
        sb.append(script);
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
            BaseExecutor error = new BaseExecutor(process.getErrorStream(), "stderr", taskInst);
            BaseExecutor output = new BaseExecutor(process.getInputStream(), "stdout", taskInst);
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
