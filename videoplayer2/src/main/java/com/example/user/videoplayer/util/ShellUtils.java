package com.example.user.videoplayer.util;

import java.io.DataOutputStream;
import java.io.OutputStream;

/**
 * Copyright  : 2015-2033 Beijing Startimes Communication & Network Technology Co.Ltd
 * Created by Chenc on 2016/1/5.
 * ClassName    :
 * Description  :
 */
public class ShellUtils {
    public static void execShell(String cmd) {
        try {
            // 权限设置
            Process p = Runtime.getRuntime().exec("sh");
            // 获取输出流
            OutputStream outputStream = p.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(
                    outputStream);
            // 将命令写入
            dataOutputStream.writeBytes(cmd);
            // 提交命令
            dataOutputStream.flush();
            // 关闭流操作
            dataOutputStream.close();
            outputStream.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
