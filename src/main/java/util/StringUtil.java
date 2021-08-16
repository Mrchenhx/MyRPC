package util;

/**
 * @Author: Richard
 * @Create: 2021/08/10 21:45:00
 * @Description: TODO
 */
public class StringUtil {

    // 判断字符串是否为空
    public static boolean isNotEmpty(String str){
        return !isEmpty(str);
    }

    // 判断字符串是否为空
    private static boolean isEmpty(String str){
        return str == null || str.length() == 0;
    }
}
