package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @Author: Richard
 * @Create: 2021/08/10 22:02:00
 * @Description: 属性文件操作 工具类
 */
public class PropertiesUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtil.class);

    // 加载属性文件
    public static Properties loadProps(String propertiesPath){
        Properties properties = new Properties();
        InputStream is = null;
        try {
            // 根据这个属性路径来进行加载
            if(propertiesPath == null || propertiesPath.length() == 0){
                throw new IllegalArgumentException();
            }
            // 如果没有后缀，则添加后缀
            String suffix = ".properties";
            if(propertiesPath.lastIndexOf(suffix) == -1){
                propertiesPath += suffix;
            }
            System.out.println("当前路径为  " + propertiesPath);
            // 获取输入流
            is = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream(propertiesPath);
            if(is != null){
                properties.load(is);
            }
        } catch (IOException e) {
            LOGGER.error("加载属性文件出错！", e);
            throw new RuntimeException(e);
        } finally {
            try {
                if(is != null){
                    is.close();
                }
            } catch (IOException e) {
                LOGGER.error("释放资源出错！", e);
            }
        }
        return properties;
    }

    // 获取字符型属性
    public static String getString(Properties properties, String key){
        String value = "";
        if(properties.containsKey(key)){
            value = properties.getProperty(key);
        }
        return value;
    }

    // 获取字符型属性（带有默认值）
    public static String getString(Properties properties, String key,
                                 String defaultValue){
        String value = defaultValue;
        if(properties.containsKey(key)){
            value = properties.getProperty(key);
        }
        return value;
    }
}
