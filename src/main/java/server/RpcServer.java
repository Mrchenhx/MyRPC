package server;

import annotation.RpcService;
import coder.Decoder;
import coder.Encoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import model.Request;
import model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ClassUtil;
import util.PropertiesUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: Richard
 * @Create: 2021/08/10 21:37:00
 * @Description: rpc 服务启动类
 */
public class RpcServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    // 服务器地址
    private String serverAddress;
    // 服务注册
    private ServiceRegistry serviceRegistry;
    // 服务所在的包
    private String servicePackage;

    public Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    {
        // 获取属性文件
        Properties properties = PropertiesUtil.
                loadProps("server-config.properties");
        serverAddress = properties.getProperty("server.address");
        servicePackage = properties.getProperty("server.servicePackage");
        serviceRegistry = new ServiceRegistry(
                properties.getProperty("registry.address"));
        // 启动rpc服务
        getRpcService();
        // System.out.println("发现的服务有 " + serviceMap.size());
    }

    // 发现服务
    private void getRpcService(){
        // 获取指定路径下的所有类
        List<Class<?>> classList = ClassUtil.getClassList(servicePackage);
        if(classList != null && classList.size() > 0){
            for(Class c : classList){
                // 判断每个类是否有 RpcService 注解
                if(c.isAnnotationPresent(RpcService.class)){
                    // 获取接口名
                    String interfaceName = (
                            (RpcService)c.getAnnotation(RpcService.class))
                            .value().getName();
                    try {
                        // 将服务保存起来
                        serviceMap.put(interfaceName, c.newInstance());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // 添加服务
    public void addService(Object service){
        // 先获取服务的的类对象，通过类对象获取接口
        Class<?>[] interfaces = service.getClass().getInterfaces();
        for(Class c : interfaces){
            serviceMap.put(c.getName(), service);
        }
    }

    // 获取活跃的服务名
    public List<String> getActiveServiceName(){
        return new ArrayList<>(serviceMap.keySet());
    }

    public Object removeService(String serviceName) {
        return serviceMap.remove(serviceName);
    }

    // 服务启动方法
    public void start(){
        // 启动 RPC 服务
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new Decoder(Request.class))
                                    .addLast(new Encoder(Response.class))
                                    .addLast(new ServerHandler(serviceMap));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            String[] array = serverAddress.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);

            ChannelFuture future = bootstrap.bind(host, port).sync();
            LOGGER.debug("server started in {} on port {}", host, port);

            if(serviceRegistry != null){
                // 向注册中心注册服务地址
                serviceRegistry.register(serverAddress);
            }


            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
