package ru.johnlife.lifetools.tools;

/**
 * Created by yanyu on 5/21/2016.
 */
public class StringUtil {
    public static String implode(String[] what, String delimeter) {
        StringBuilder b = new StringBuilder();
        for (String s : what) {
            b.append(s).append(delimeter);
        }
        b.delete(b.length()-delimeter.length(),b.length());
        return b.toString();
    }
}
