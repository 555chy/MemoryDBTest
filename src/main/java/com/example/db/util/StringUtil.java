package com.example.db.util;

public class StringUtil {

    public static void appendLine(StringBuilder sb, String str) {
        sb.append(str);
        sb.append("\n");
    }

    public static void appendLine(StringBuilder sb, String key, Object value) {
        sb.append(key);
        sb.append(": ");
        sb.append(value instanceof String ? value : String.valueOf(value));
        sb.append("\n");
    }

    public static String join(String separator, int[] result) {
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<result.length; i++) {
            if(sb.length() != 0) {
                sb.append(",");
            }
            sb.append(result[i]);
        }
        return sb.toString();
    }
}
