package com.traits.executor;

import com.traits.model.entity.TaskInst;
import com.traits.model.Configure;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * Created by YeFeng on 2016/7/23.
 */
public class BaseExecutor extends Thread {

    static final Logger logger = Logger.getLogger("executor");

    InputStream is;
    String type;
    TaskInst taskInst;

    public BaseExecutor(InputStream is, String type, TaskInst taskInst) {
        this.is = is;
        this.type = type;
        this.taskInst = taskInst;
    }

    public BaseExecutor(TaskInst taskInst) {
        this.taskInst = taskInst;
    }

    public BaseExecutor() {
    }

    public boolean initWorkSpace() {

        return true;
    }

    public void run() {
        FileOutputStream out = null;
        BufferedOutputStream buffer = null;
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;

            String taskid = taskInst.getId();
            String user = taskInst.get_taskDef().getUser();
            Configure conf = Configure.getSingleton();

            String path = String.format("%stask_log/%s/tmp/%s/", conf.task_log_base_path, user, taskid);
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            logger.debug(String.format("output path: %s", path));

            if (type.equals("stderr")) {
                String tmp_path = path + "stderr.log";
                taskInst.setStderr_path(tmp_path);
                File tmp_file_dir = new File(tmp_path);
                out = new FileOutputStream(tmp_file_dir);
                buffer = new BufferedOutputStream(out);
            } else {
                String tmp_path = path + "stdout.log";
                taskInst.setStdout_path(tmp_path);
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
        System.out.println(String.format("\n*** Running taskInst %s\n", taskInst.getName()));
        for (int i = 0; i < 5; ++i) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(String.format("Sleeping TaskInst %s", taskInst.getName()));
        }

        System.out.println(String.format("\nRunning taskInst %s\n", taskInst.getName()));
        return status;
    }

    public static void main(String[] args) {
        BaseExecutor eval = new BaseExecutor();

        //eval.exec("cd /home/YeFeng/ \n pwd \n tree | wc -l \n date", null);
    }


}
