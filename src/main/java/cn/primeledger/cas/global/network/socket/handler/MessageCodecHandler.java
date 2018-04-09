package cn.primeledger.cas.global.network.socket.handler;

import cn.primeledger.cas.global.network.socket.message.BaseMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.SerializationUtils;
import org.xerial.snappy.Snappy;

import java.util.List;

/**
 * frame <--> byte
 *
 * @author yuanjiantao
 * @since 2/24/2018
 **/
@Slf4j
public class MessageCodecHandler extends ByteToMessageCodec<BaseMessage> {

    private static final int BASIC_SIZE = 4;

    /**
     * 16mb
     */
    private final static int MAX_SIZE = 16 * 1024 * 1024;

    @Override
    protected void encode(ChannelHandlerContext ctx, BaseMessage message, ByteBuf byteBuf) throws Exception {
        ByteBuf tempBuf = byteBuf.alloc().buffer();
        byte[] data = SerializationUtils.serialize(message);
        byte[] bytes = Snappy.compress(data);
        tempBuf.writeInt(bytes.length);
        tempBuf.writeBytes(bytes);

        if (tempBuf.capacity() > MAX_SIZE) {
            LOGGER.warn("The news was discarded because it was too big");
            tempBuf.release();
            return;
        }

        ctx.write(tempBuf);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception {
        if (in.readableBytes() <= BASIC_SIZE) {
            return;
        }

        int readerIndex = in.readerIndex();
        int size = in.readInt();
        if (in.readableBytes() < size) {
            in.readerIndex(readerIndex);
            return;
        }
        byte[] bytes = new byte[size];
        in.readBytes(bytes);
        byte[] data = Snappy.uncompress(bytes);
        list.add(SerializationUtils.deserialize(data));
    }
}
