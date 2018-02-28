package cn.primeledger.cas.global.p2p.handler;

import cn.primeledger.cas.global.p2p.channel.Channel;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * frame <--> byte and snappy compress
 *
 * @author yuanjiantao
 * @since 2/24/2018
 **/

public class FrameHandler extends ByteToMessageCodec<FrameHandler.Frame> {

    public Channel channel;

    /**
     * 16mb
     */
    private final static int MAX_SIZE = 16 * 1024 * 1024;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Frame frame, ByteBuf byteBuf) throws Exception {

        if (frame.size > MAX_SIZE) {
            return;
        }
        writeFrame(frame, byteBuf);

    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        Frame frame = readFrame(byteBuf);
        list.add(frame);
    }


    @NoArgsConstructor
    @AllArgsConstructor
    public static class Frame {

        int cmd;
        int size;
        int totalFrameSize = -1;
        int contextId = -1;
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

    }

    public void writeFrame(Frame frame, ByteBuf byteBuf) {
        byteBuf.writeInt(frame.cmd);
        byteBuf.writeInt(frame.size);
        byteBuf.writeInt(frame.totalFrameSize);
        byteBuf.writeInt(frame.contextId);
        byteBuf.writeBytes(frame.payload);
    }

    public Frame readFrame(ByteBuf byteBuf) {
        int type = byteBuf.readInt();
        int size = byteBuf.readInt();
        int totalFrameSize = byteBuf.readInt();
        int contextId = byteBuf.readInt();
        byte[] payload = new byte[size];
        byteBuf.readBytes(payload);
        return new Frame(type, totalFrameSize, contextId, payload);
    }
}
