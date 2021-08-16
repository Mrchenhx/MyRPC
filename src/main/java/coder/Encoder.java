package coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import util.SerializationUtil;

/**
 * @Author: Richard
 * @Create: 2021/08/10 21:08:00
 * @Description: 一次编码器
 */
public class Encoder extends MessageToByteEncoder {
    private Class<?> genericClass;

    public Encoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if (genericClass.isInstance(in)) {
            // 使用序列化工具序列化in对象
            byte[] data = SerializationUtil.serialize(in);
            // 向ByteBuf中写入data的长度
            out.writeInt(data.length);
            // 写入data数据
            out.writeBytes(data);
        }
    }
}
