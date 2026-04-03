package com.scsdky.quartz.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


/**
 * 传输数据
 */
public class SendRequestUtil {

    /**
     * 发送POST请求传输数据
     */
    public static String sendPostOrGet(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("connection", "keep-alive");
            //  conn.setRequestProperty("Content-Type", "application/json");
            //  conn.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
            //  发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.write(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            try {
                in = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
            } catch (Exception e) {
                in = new BufferedReader(
                        new InputStreamReader(((HttpURLConnection) conn).getErrorStream()));
            }

            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result.toString();
    }
}