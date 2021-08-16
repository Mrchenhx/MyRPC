package server;

/**
 * @Author: Richard
 * @Create: 2021/08/10 22:51:00
 * @Description: 常量
 */
public interface Constant {

    int ZK_SESSION_TIMEOUT = 20000;
    String ZK_REGISTRY_PATH = "/registry0000000000";
    String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";
}
