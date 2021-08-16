package server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import model.Request;
import model.Response;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @Author: Richard
 * @Create: 2021/08/10 23:47:00
 * @Description: 处理器
 */
public class ServerHandler extends SimpleChannelInboundHandler<Request> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);

    // 存放接口与服务Bean之间的映射关系
    private final Map<String, Object> handlerMap;

    public ServerHandler(Map<String, Object> map) {
        this.handlerMap = map;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, Request request) throws Exception {
        Response response = new Response();
        request.setRequestId(request.getRequestId());
        try {
            // 执行服务器端的服务
            Object result = handle(request);
            // 设置结果
            response.setResult(result);
        } catch (Throwable throwable) {
            response.setError(throwable);
        }
        // 将处理完的结果发送出去
        ctx.writeAndFlush(response)
                // 添加一个监听器到 channelFuture 来检测是否所有的数据包都发出，然后关闭通道
                .addListener(ChannelFutureListener.CLOSE);
    }

    // 使用反射或cglib调用服务
    private Object handle(Request request) throws Throwable{
        // 获取类名
        String className = request.getClassName();
        // 在映射表中获取服务对象
        Object serviceBean = handlerMap.get(className);
        // 获取类对象
        Class<?> serviceClass = serviceBean.getClass();
        // 获取服务名
        String methodName = request.getMethodName();
        // 获取参数类型
        Class<?>[] parameterTypes = request.getParameterTypes();
        // 获取参数
        Object[] parameters = request.getParameters();
        // cglib调用方法
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod =
                serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("server handler caught exception", cause);
        ctx.close();
    }
}
