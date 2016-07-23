package com.traits.executor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.traits.model.BaseTask;
import com.traits.model.Configure;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by YeFeng on 2016/7/23.
 */
public class BaseExecutor extends Thread {

    public static Logger logger = Logger.getLogger("executor");

    InputStream is;
    String type;
    BaseTask task;

    public BaseExecutor(InputStream is, String type, BaseTask task) {
        this.is = is;
        this.type = type;
        this.task = task;
    }

    public BaseExecutor(BaseTask task) {
        this.task = task;
    }

    public BaseExecutor() {
    }

    public void run() {
        FileOutputStream out = null;
        BufferedOutputStream buffer = null;
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;

            String taskid = task.getId();
            String user = task.get_project().getUser();
            Configure conf = Configure.getSingleton();

            String path = String.format("%sresult/%s/tmp/%s/", conf.task_result_base_path, user, taskid);
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            logger.debug(String.format("output path: %s", path));

            if (type.equals("stderr")) {
                String tmp_path = path + "stderr.log";
                task.setStderr_path(tmp_path);
                File tmp_file_dir = new File(tmp_path);
                out = new FileOutputStream(tmp_file_dir);
                buffer = new BufferedOutputStream(out);
            } else {
                String tmp_path = path + "stdout.log";
                task.setStdout_path(tmp_path);
                File tmp_file_dir = new File(tmp_path);
                out = new FileOutputStream(tmp_file_dir);
                buffer = new BufferedOutputStream(out);
            }

            while ((line = br.readLine()) != null) {
                line += "\r\n";
                if (type.equals("stderr")) {
                    buffer.write(line.getBytes());
                    System.out.print("stderr   : " + line);
                } else {
                    buffer.write(line.getBytes());
                    System.out.print("stdout   : " + line);
                }
            }

            buffer.close();
            out.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public int exec(String script, String args)
    {
        int status = 0;
        System.out.println(String.format("\n*** Running task %s\n", task.getName()));
        for (int i = 0; i < 5; ++i) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(String.format("Sleeping Task %s", task.getName()));
        }

        System.out.println(String.format("\nRunning task %s\n", task.getName()));
        return status;
    }

    public static void main(String[] args) {
        BaseExecutor eval = new BaseExecutor();

        eval.exec("cd /home/YeFeng/ \n pwd \n tree | wc -l \n date", null);
    }


}
