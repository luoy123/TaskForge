package com.zhq.taskforge.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ServletUtils {

    public static String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    public static String getParameter(String name, String defaultValue) {
        String value = getRequest().getParameter(name);
        return value != null ? value : defaultValue;
    }

    public static Integer getParameterToInt(String name) {
        String value = getRequest().getParameter(name);
        return value != null ? Integer.parseInt(value) : null;
    }

    public static Integer getParameterToInt(String name, Integer defaultValue) {
        String value = getRequest().getParameter(name);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    public static Boolean getParameterToBool(String name) {
        String value = getRequest().getParameter(name);
        return value != null ? Boolean.parseBoolean(value) : null;
    }

    public static Map<String, String[]> getParams(HttpServletRequest request) {
        final Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }

    public static Map<String, String> getParamMap(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, String[]> entry : getParams(request).entrySet()) {
            params.put(entry.getKey(), StringUtils.join(entry.getValue(), ","));
        }
        return params;
    }

    public static HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }

    public static HttpServletResponse getResponse() {
        return getRequestAttributes().getResponse();
    }

    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    public static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (ServletRequestAttributes) attributes;
    }

    public static void renderString(HttpServletResponse response, String string) {
        try {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isAjaxRequest(HttpServletRequest request) {
        String accept = request.getHeader("accept");
        if (accept != null && accept.contains("application/json")) return true;
        String xRequestedWith = request.getHeader("X-Requested-With");
        if (xRequestedWith != null && xRequestedWith.contains("XMLHttpRequest")) return true;
        String uri = request.getRequestURI();
        if (StringUtils.inStringIgnoreCase(uri, ".json", ".xml")) return true;
        String ajax = request.getParameter("__ajax");
        return StringUtils.inStringIgnoreCase(ajax, "json", "xml");
    }
}
