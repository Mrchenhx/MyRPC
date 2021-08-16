package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.RpcServer;

import java.io.File;
import java.io.FileFilter;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @Author: Richard
 * @Create: 2021/08/10 21:24:00
 * @Description: 工具类：获取类对象
 */
public class ClassUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    public static List<Class<?>> getClassList(String packName) {
        List<Class<?>> classList = new ArrayList<Class<?>>();
        try {
            // 从包名获取 URL 类型的资源
            // 通过当前线程获取线程上下文类加载器
            // 再利用加载器获取 URL
            Enumeration<URL> urls = Thread.currentThread()
                    .getContextClassLoader()
                    .getResources(packName.replace(".", "/"));
            // 遍历 URL 资源
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                if (url != null) {
                    // 获取协议名 （类型有 file / jar）
                    String protocol = url.getProtocol();
                    if (protocol.equals("file")) {
                        // 若在 class 目录中，则执行添加类操作
                        String packagePath = url.getPath().replaceAll("%20", " ");
                        addClass(classList, packagePath, packName);
                    } else if (protocol.equals("jar")) {
                        // URL 在 jar 包中，解析 jar 包中的 entry
                        JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
                        JarFile jarFile = jarURLConnection.getJarFile();
                        Enumeration<JarEntry> jarEntries = jarFile.entries();
                        while (jarEntries.hasMoreElements()){
                            JarEntry jarEntry = jarEntries.nextElement();
                            String jarEntryName = jarEntry.getName();
                            // 判断是否 entry 是否为 class
                            if(jarEntryName.endsWith(".class")){
                                // 获取类名
                                String className = jarEntryName.substring(
                                                            0, jarEntryName.lastIndexOf(".")
                                                    ).replaceAll("/", ".");
                                // 执行添加类操作
                                doAddClass(classList, className);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("获取类出错！", e);
        }
        return classList;
    }

    private static void addClass(List<Class<?>> classList, String packagePath,
                                 String packageName) {
        try {
            // 获取包名下的 class 文件或目录
            final File[] files = new File(packagePath).listFiles(new FileFilter() {
                public boolean accept(File file) {
                    return (file.isFile() && file.getName().endsWith(".class")
                            || file.isDirectory());
                }
            });

            // 遍历包名下的文件或目录
            for (File file : files) {
                String fileName = file.getName();
                // 判断是否为文件或目录
                if (file.isFile()) {
                    // 文件类型，获取类名
                    String className = fileName.substring(0, fileName.lastIndexOf("."));
                    if (StringUtil.isNotEmpty(packageName)) {
                        className = packageName + "." + className;
                    }
                    // 执行添加类操作
                    doAddClass(classList, className);
                } else {
                    // 获取子包
                    String subPackagePath = fileName;
                    if (StringUtil.isNotEmpty(packagePath)) {
                        subPackagePath = packagePath + "/" + subPackagePath;
                    }
                    // 子包名
                    String subPackageName = fileName;
                    if (StringUtil.isNotEmpty(packageName)) {
                        subPackageName = packageName + "." + subPackageName;
                    }
                    addClass(classList, subPackagePath, subPackageName);
                }
            }
        } catch (Exception e) {
            LOGGER.error("添加类出错！", e);
        }
    }

    private static void doAddClass(List<Class<?>> classList, String className) {
        // 加载类
        Class<?> cls;
        try {
            cls = Class.forName(className, true,
                    Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            LOGGER.error("类加载错误！", e);
            // 打完日志再向外抛出
            throw new RuntimeException(e);
        }
        classList.add(cls);
    }
}
