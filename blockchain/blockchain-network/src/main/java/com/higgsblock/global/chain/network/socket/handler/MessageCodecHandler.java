package com.higgsblock.global.chain.network.socket.handler;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import org.xerial.snappy.Snappy;

import java.util.List;

/**
 * frame <--> byte
 *
 * @author yuanjiantao
 * @since 2/24/2018
 **/
@Slf4j
public class MessageCodecHandler extends ByteToMessageCodec<String> {

    private static final int BASIC_SIZE = 4;

    /**
     * 16mb
     */
    private final static int MAX_SIZE = 16 * 1024 * 1024;
    private final static int DATA_MAX_SIZE = MAX_SIZE - 4;

    @Override
    protected void encode(ChannelHandlerContext ctx, String message, ByteBuf byteBuf) throws Exception {
        ByteBuf tempBuf = byteBuf.alloc().buffer();
        byte[] data = message.getBytes(Charsets.UTF_8);
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

        if (size > DATA_MAX_SIZE) {
            throw new Exception("find huge packet and consider it is an attacker");
        }

        if (in.readableBytes() < size) {
            in.readerIndex(readerIndex);
            return;
        }
        byte[] bytes = new byte[size];
        in.readBytes(bytes);
        byte[] data = Snappy.uncompress(bytes);
        list.add(new String(data, Charsets.UTF_8));
    }
}
