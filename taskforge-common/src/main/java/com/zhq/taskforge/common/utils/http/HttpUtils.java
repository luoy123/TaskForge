package com.zhq.taskforge.common.utils.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HttpUtils {
    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

    public static String sendGet(String url, String param, String charset) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        try {
            String urlName = url + "?" + URLEncoder.encode(param, charset);
            URL realUrl = new URL(urlName);
            HttpURLConnection connection = (HttpURLConnection) realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), charset));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            log.error("发送GET请求异常: {}", e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                log.error("关闭流异常: {}", e.getMessage());
            }
        }
        return result.toString();
    }
}
