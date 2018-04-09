package cn.primeledger.cas.global.network.socket.handler;

import cn.primeledger.cas.global.network.socket.connection.Connection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * frame <--> byte
 *
 * @author yuanjiantao
 * @since 2/24/2018
 **/
@Deprecated
@Slf4j
public class FrameCodecHandler extends ByteToMessageCodec<FrameCodecHandler.Frame> {
    private static final int BASIC_SIZE = 16;
    /**
     * 16mb
     */
    private final static int MAX_SIZE = 16 * 1024 * 1024;
    public Connection connection;

    @Override
    protected void encode(ChannelHandlerContext ctx, Frame frame, ByteBuf byteBuf) throws Exception {
        if (frame.size > MAX_SIZE) {
            LOGGER.error("");
            return;
        }

        ByteBuf tempBuf = byteBuf.alloc().buffer(BASIC_SIZE + frame.getSize());

        tempBuf.writeInt(frame.cmd);
        tempBuf.writeInt(frame.size);
        tempBuf.writeInt(frame.totalFrameSize);
        tempBuf.writeInt(frame.contextId);
        tempBuf.writeBytes(frame.payload);

        ctx.write(tempBuf);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception {
        if (in.readableBytes() < BASIC_SIZE) {
            return;
        }

        int readerIndex = in.readerIndex();
        int type = in.readInt();
        int size = in.readInt();
        int totalFrameSize = in.readInt();
        int contextId = in.readInt();

        if (in.readableBytes() < size) {
            in.readerIndex(readerIndex);
        } else {
            byte[] payload = new byte[size];
            in.readBytes(payload);
            list.add(new Frame(type, totalFrameSize, contextId, payload));
        }
    }


    @NoArgsConstructor
    @AllArgsConstructor
    public static class Frame {
        int cmd;
        int size;
        int totalFrameSize = 0;
        int contextId = 0;
        byte[] payload;

        public Frame(int cmd, byte[] payload) {
            this.cmd = cmd;
            this.size = payload.length;
            this.payload = payload;
        }

        public Frame(int cmd, int totalFrameSize, int contextId, byte[] payload) {
            this.cmd = cmd;
            this.size = payload.length;
            this.totalFrameSize = totalFrameSize;
            this.contextId = contextId;
            this.payload = payload;
        }

        public Frame(int cmd, int totalFrameSize, byte[] payload) {
            this.cmd = cmd;
            this.size = payload.length;
            this.totalFrameSize = totalFrameSize;
            this.payload = payload;
        }

        public boolean isChunked() {
            return contextId >= 0;
        }

        public int getSize() {
            return size;
        }

    }
}
