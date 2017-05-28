package com.traits.util;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Created by yefeng on 17-5-28.
 */
public final class LinuxSystemTool {

    /*
     * 获取内存使用情况
     * return: int[4]
     *          MemTotal
     *          MemFree
     *          SwapTotal
     *          SwapFree
     */
    public static int[] getMemInfo() throws IOException, InterruptedException {
        File file = new File("/proc/meminfo");
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file)));
        int[] result = new int[4];
        String str = null;
        StringTokenizer token = null;
        while ((str = br.readLine()) != null) {
            token = new StringTokenizer(str);
            if (!token.hasMoreTokens())
                continue;

            str = token.nextToken();
            if (!token.hasMoreTokens())
                continue;

            if (str.equalsIgnoreCase("MemTotal:"))
                result[0] = Integer.parseInt(token.nextToken());
            else if (str.equalsIgnoreCase("MemFree:"))
                result[1] = Integer.parseInt(token.nextToken());
            else if (str.equalsIgnoreCase("SwapTotal:"))
                result[2] = Integer.parseInt(token.nextToken());
            else if (str.equalsIgnoreCase("SwapFree:"))
                result[3] = Integer.parseInt(token.nextToken());
        }
        br.close();
        return result;
    }

    /*
     * 获取CPU使用率
     * return: float
     */
    public static float getCpuInfo() throws IOException, InterruptedException {
        File file = new File("/proc/stat");
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file)));
        StringTokenizer token = new StringTokenizer(br.readLine());
        token.nextToken();
        int user1 = Integer.parseInt(token.nextToken());
        int nice1 = Integer.parseInt(token.nextToken());
        int sys1 = Integer.parseInt(token.nextToken());
        int idle1 = Integer.parseInt(token.nextToken());

        Thread.sleep(1000);

        br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file)));
        token = new StringTokenizer(br.readLine());
        token.nextToken();
        int user2 = Integer.parseInt(token.nextToken());
        int nice2 = Integer.parseInt(token.nextToken());
        int sys2 = Integer.parseInt(token.nextToken());
        int idle2 = Integer.parseInt(token.nextToken());

        br.close();
        return (float) ((user2 + sys2 + nice2) - (user1 + sys1 + nice1))
                / (float) ((user2 + nice2 + sys2 + idle2) -
                (user1 + nice1 + sys1 + idle1));
    }

    /*
     *　获取磁盘使用率
     * return: float
     */
    public static float getIOInfo() throws IOException {
        float ioUsage = 0.0f;
        Process pro = null;
        Runtime r = Runtime.getRuntime();
        String command = "iostat -d -x";
        pro = r.exec(command);
        BufferedReader br = new BufferedReader(new InputStreamReader(pro.getInputStream()));
        String line = null;
        int count = 0;
        while ((line = br.readLine()) != null) {
            if (++count >= 4) {
                String[] temp = line.split("\\s+");
                if (temp.length > 1) {
                    float util = Float.parseFloat(temp[temp.length - 1]);
                    ioUsage = (ioUsage > util) ? ioUsage : util;
                }
            }
        }
        if (ioUsage > 0) {
            ioUsage /= 100;
        }
        br.close();
        pro.destroy();

        return ioUsage;
    }

    /*
     * 获取无网络流量
     * return: float   Mbps
     */
    public static float getNetInfo() throws IOException, InterruptedException {

        float netUsage = 0.0f;
        Process pro1, pro2;
        Runtime r = Runtime.getRuntime();
        String command = "cat /proc/net/dev";
        //第一次采集流量数据
        long startTime = System.currentTimeMillis();
        pro1 = r.exec(command);
        BufferedReader in1 = new BufferedReader(new InputStreamReader(pro1.getInputStream()));
        String line = null;
        long inSize1 = 0, outSize1 = 0;
        while ((line = in1.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("eth0") || line.startsWith("ens33")) {
                String[] temp = line.split("\\s+");
                inSize1 = Long.parseLong(temp[1]); //Receive bytes,单位为Byte
                outSize1 = Long.parseLong(temp[9]);             //Transmit bytes,单位为Byte
                break;
            }
        }
        in1.close();
        pro1.destroy();

        Thread.sleep(1000);

        //第二次采集流量数据
        long endTime = System.currentTimeMillis();
        pro2 = r.exec(command);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(pro2.getInputStream()));
        long inSize2 = 0, outSize2 = 0;
        while ((line = in2.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("eth0") || line.startsWith("ens33")) {
                String[] temp = line.split("\\s+");
                inSize2 = Long.parseLong(temp[1]);
                outSize2 = Long.parseLong(temp[9]);
                break;
            }
        }
        if (inSize1 != 0 && outSize1 != 0 && inSize2 != 0 && outSize2 != 0) {
            float interval = (float) (endTime - startTime) / 1000;
            //网口传输速度,单位为bps
            float curRate = (float) (inSize2 - inSize1 + outSize2 - outSize1) * 8 / (1000000 * interval);
            netUsage = curRate;
        }
        in2.close();
        pro2.destroy();

        return netUsage;
    }


    public static void main(String args[]) throws IOException, InterruptedException {
        int[] meminfo = getMemInfo();
        float cpuInfo = getCpuInfo();
        System.out.println(String.format("%s %s %s %s",
                meminfo[0], meminfo[1], meminfo[2], meminfo[3]));
        for (int i = 0; i < 1; ++i) {
            cpuInfo = getCpuInfo();
            System.out.println(String.format("%s", cpuInfo));
            Thread.sleep(1000);
        }
        float ioinfo = getIOInfo();
        System.out.println(ioinfo);
        float netinfo = getNetInfo();
        System.out.println(netinfo);
    }
}
