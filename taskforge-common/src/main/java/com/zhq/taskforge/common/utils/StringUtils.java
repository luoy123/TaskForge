package com.zhq.taskforge.common.utils;

import org.springframework.util.AntPathMatcher;

import java.util.*;

public class StringUtils extends org.apache.commons.lang3.StringUtils {
    private static final String NULLSTR = "";
    private static final char SEPARATOR = '_';

    public static <T> T nvl(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    public static boolean isEmpty(Collection<?> coll) {
        return isNull(coll) || coll.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> coll) {
        return !isEmpty(coll);
    }

    public static boolean isEmpty(Object[] objects) {
        return isNull(objects) || (objects.length == 0);
    }

    public static boolean isNotEmpty(Object[] objects) {
        return !isEmpty(objects);
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return isNull(map) || map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static boolean isEmpty(String str) {
        return isNull(str) || NULLSTR.equals(str.trim());
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isNull(Object object) {
        return object == null;
    }

    public static boolean isNotNull(Object object) {
        return !isNull(object);
    }

    public static boolean isArray(Object object) {
        return isNotNull(object) && object.getClass().isArray();
    }

    public static String trim(String str) {
        return (str == null ? "" : str.trim());
    }

    public static String substring(final String str, int start) {
        if (str == null) return NULLSTR;
        if (start < 0) start = str.length() + start;
        if (start < 0) start = 0;
        if (start > str.length()) return NULLSTR;
        return str.substring(start);
    }

    public static String substring(final String str, int start, int end) {
        if (str == null) return NULLSTR;
        if (end < 0) end = str.length() + end;
        if (start < 0) start = str.length() + start;
        if (end > str.length()) end = str.length();
        if (start > end) return NULLSTR;
        if (start < 0) start = 0;
        if (end < 0) end = 0;
        return str.substring(start, end);
    }

    public static String format(String template, Object... params) {
        if (isEmpty(params) || isEmpty(template)) return template;
        int start = 0;
        int paramIndex = 0;
        StringBuilder sb = new StringBuilder();
        while (true) {
            int braceStart = template.indexOf("{}", start);
            if (braceStart == -1 || paramIndex >= params.length) {
                sb.append(template.substring(start));
                break;
            }
            sb.append(template, start, braceStart);
            sb.append(params[paramIndex++]);
            start = braceStart + 2;
        }
        return sb.toString();
    }

    public static boolean ishttp(String link) {
        return StringUtils.startsWithAny(link, "http://", "https://");
    }

    public static Set<String> str2Set(String str, String sep) {
        return new HashSet<>(str2List(str, sep, true, false));
    }

    public static List<String> str2List(String str, String sep, boolean filterBlank, boolean trim) {
        List<String> list = new ArrayList<>();
        if (StringUtils.isEmpty(str)) return list;
        if (filterBlank && StringUtils.isBlank(str)) return list;
        String[] split = str.split(sep);
        for (String string : split) {
            if (filterBlank && StringUtils.isBlank(string)) continue;
            if (trim) string = string.trim();
            list.add(string);
        }
        return list;
    }

    public static boolean containsAny(Collection<String> collection, String... array) {
        if (isEmpty(collection) || isEmpty(array)) return false;
        for (String str : array) {
            if (collection.contains(str)) return true;
        }
        return false;
    }

    public static String toUnderScoreCase(String str) {
        if (str == null) return null;
        StringBuilder sb = new StringBuilder();
        boolean preCharIsUpperCase = true;
        boolean curreCharIsUpperCase = true;
        boolean nexteCharIsUpperCase = true;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i > 0) preCharIsUpperCase = Character.isUpperCase(str.charAt(i - 1));
            else preCharIsUpperCase = false;
            curreCharIsUpperCase = Character.isUpperCase(c);
            if (i < (str.length() - 1)) nexteCharIsUpperCase = Character.isUpperCase(str.charAt(i + 1));
            if (preCharIsUpperCase && curreCharIsUpperCase && !nexteCharIsUpperCase) {
                sb.append(SEPARATOR);
            } else if ((i != 0 && !preCharIsUpperCase) && curreCharIsUpperCase) {
                sb.append(SEPARATOR);
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    public static boolean inStringIgnoreCase(String str, String... strs) {
        if (str != null && strs != null) {
            for (String s : strs) {
                if (str.equalsIgnoreCase(trim(s))) return true;
            }
        }
        return false;
    }

    public static String convertToCamelCase(String name) {
        StringBuilder result = new StringBuilder();
        if (name == null || name.isEmpty()) return "";
        if (!name.contains("_")) return name.substring(0, 1).toUpperCase() + name.substring(1);
        String[] camels = name.split("_");
        for (String camel : camels) {
            if (camel.isEmpty()) continue;
            result.append(camel.substring(0, 1).toUpperCase());
            result.append(camel.substring(1).toLowerCase());
        }
        return result.toString();
    }

    public static String toCamelCase(String s) {
        if (s == null) return null;
        s = s.toLowerCase();
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == SEPARATOR) upperCase = true;
            else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else sb.append(c);
        }
        return sb.toString();
    }

    public static boolean matches(String str, List<String> strs) {
        if (isEmpty(str) || isEmpty(strs)) return false;
        for (String pattern : strs) {
            if (isMatch(pattern, str)) return true;
        }
        return false;
    }

    public static boolean isMatch(String pattern, String url) {
        AntPathMatcher matcher = new AntPathMatcher();
        return matcher.match(pattern, url);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }
}
