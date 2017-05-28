package com.traits.model.machine;

import com.traits.model.Configure;
import com.traits.util.LinuxSystemTool;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by yefeng on 17/5/28.
 */
public class Monitor implements Runnable {

    private final static Monitor singleton = new Monitor();
    static final Logger logger = Logger.getLogger("scheduler");
    private Configure conf = Configure.getSingleton();
    private boolean shutdown = false;
    private float cpuinfo = 0.0f;
    private int[] meminfo = null;
    private float ioinfo = 0.0f;
    private float netinfo = 0.0f;

    synchronized public float getCpuinfo() {
        return cpuinfo;
    }

    synchronized public void setCpuinfo(float cpuinfo) {
        this.cpuinfo = cpuinfo;
    }

    synchronized public int[] getMeminfo() {
        return meminfo;
    }

    synchronized public void setMeminfo(int[] meminfo) {
        this.meminfo = meminfo;
    }

    synchronized public float getIoinfo() {
        return ioinfo;
    }

    synchronized public void setIoinfo(float ioinfo) {
        this.ioinfo = ioinfo;
    }

    synchronized public float getNetinfo() {
        return netinfo;
    }

    synchronized public void setNetinfo(float netinfo) {
        this.netinfo = netinfo;
    }

    public void start() {
        this.shutdown = false;
        Thread thread = new Thread(singleton);
        thread.start();
    }

    public void stop() {
        this.shutdown = true;
    }

    private Monitor() {}

	public static Monitor getInstance() {
	    return singleton;
    }

    public void run() {
        int count = 0;
        while (true) {
            if (shutdown) {
                break;
            }
            if (count == 0) {
                try {
                    setCpuinfo(LinuxSystemTool.getCpuInfo());
                    setMeminfo(LinuxSystemTool.getMemInfo());
                    setIoinfo(LinuxSystemTool.getIOInfo());
                    setNetinfo(LinuxSystemTool.getNetInfo());

                    System.out.println(getCpuinfo());
                    System.out.println(getNetinfo());
                    System.out.println(getIoinfo());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (count++ == 60) count = 0;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Monitor monitor = Monitor.getInstance();
        monitor.start();
        Thread.sleep(70006);
        monitor.stop();
    }

}
