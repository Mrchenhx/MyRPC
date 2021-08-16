package coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import util.SerializationUtil;

import java.util.List;

/**
 * @Author: Richard
 * @Create: 2021/08/10 21:06:00
 * @Description: 一次解码器
 */
public class Decoder extends ByteToMessageDecoder {
    private Class<?> genericClass;

    public Decoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    //按照编码的格式解码
    // 第一部分：对象序列化数据的长度，起到校验的作用，若接收到的ByteBuf数据长度小于它，则此ByteBuf不对（接收到的信息不对）。
    // 第二部分：序列化数据
    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //可读字节小于4，无法再读int，否则会报错
        if (in.readableBytes() < 4) {
            return;
        }
        // 标记索引
        in.markReaderIndex();
        int dataLength = in.readInt();
        if (dataLength < 0) {
            ctx.close();
        }
        // 可读字节数不够
        if (in.readableBytes() < dataLength) {
            //将索引重置为标记的索引
            in.resetReaderIndex();
            return;
        }

        byte[] data = new byte[dataLength];
        // 将ByteBuf中的剩余内容读入data中
        in.readBytes(data);

        // 反序列化对象
        Object obj = SerializationUtil.deserialize(data, genericClass);
        out.add(obj);
    }
}