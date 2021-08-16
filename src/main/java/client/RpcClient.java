package client;// package client;

import coder.Decoder;
import coder.Encoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import model.Request;
import model.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: Richard
 * @Create: 2021/08/11 10:50:00
 * @Description: 客户端接收信息
 */
public class RpcClient extends SimpleChannelInboundHandler<Response> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    private String HOST;
    private int PORT;
    private Response response;

    // 锁对象
    private final Object obj = new Object();

    public RpcClient(String HOST, int PORT){
        this.HOST = HOST;
        this.PORT = PORT;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response msg) throws Exception {
        this.response = msg;
        synchronized (obj){
            obj.notifyAll();
        }
    }

    public Response send(Request request) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new Decoder(Response.class))
                                    .addLast(new Encoder(Request.class))
                                    .addLast(RpcClient.this); // 使用 client.RpcClient 发送 RPC 请求
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.connect(HOST, PORT).sync();

            future.channel().writeAndFlush(request).sync();

            // 未收到响应，使线程等待
            synchronized (obj){
                obj.wait();
            }
            if(response != null){
                future.channel().closeFuture().sync();
            }
            return response;
        } finally {
            group.shutdownGracefully();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("client caught exception", cause);
        ctx.close();
    }
}
