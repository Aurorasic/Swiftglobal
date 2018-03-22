package cn.primeledger.cas.global.p2p.handler;

import cn.primeledger.cas.global.p2p.message.BaseMessage;
import cn.primeledger.cas.global.p2p.message.MessageFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.tuple.Pair;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.primeledger.cas.global.p2p.handler.FrameCodecHandler.Frame;
import static java.lang.Math.min;

/**
 * frame <---> message
 *
 * @author yuanjiantao
 * @since 2/24/2018
 **/

@Slf4j
public class MessageCodecHandler extends MessageToMessageCodec<Frame, BaseMessage> {

    public static final int NO_FRAMING = Integer.MAX_VALUE >> 1;

    private int maxFramePayloadSize = NO_FRAMING;
    AtomicInteger contextIdCounter = new AtomicInteger(1);
    Map<Integer, Pair<? extends List<Frame>, AtomicInteger>> cacheFrames = new LRUMap<>(16);
    private MessageFactory messageFactory = new MessageFactory();


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, BaseMessage message, List<Object> list) throws Exception {

        byte[] data = message.getEncoded();
        byte[] bytes = Snappy.compress(data);
        int cmd = message.getCmd();
        List<Frame> ret = new ArrayList<>();
        int curPos = 0;
        while (curPos < bytes.length) {
            int newPos = min(curPos + maxFramePayloadSize, bytes.length);
            byte[] frameBytes = curPos == 0 && newPos == bytes.length ? bytes :
                    Arrays.copyOfRange(bytes, curPos, newPos);
            ret.add(new Frame(cmd, bytes.length, frameBytes));
            curPos = newPos;
        }

        if (ret.size() > 1) {
            int contextId = contextIdCounter.getAndIncrement();
            ret.get(0).totalFrameSize = bytes.length;
            for (Frame frame : ret) {
                frame.contextId = contextId;
            }
        }
        list.addAll(ret);

    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Frame frame, List<Object> list) throws Exception {
        if (frame.isChunked()) {
            Pair<? extends List<Frame>, AtomicInteger> frameParts = cacheFrames.get(frame.contextId);
            if (frameParts == null) {
                BaseMessage message = decodeMessage(Collections.singletonList(frame));
                list.add(message);
                return;
            } else {
                frameParts = Pair.of(new ArrayList<Frame>(), new AtomicInteger(0));
                cacheFrames.put(frame.contextId, frameParts);
            }
            frameParts.getLeft().add(frame);
            int curSize = frameParts.getRight().addAndGet(frame.size);
            if (curSize == frameParts.getLeft().get(0).totalFrameSize) {
                BaseMessage message = decodeMessage(frameParts.getLeft());
                cacheFrames.remove(frame.contextId);
                list.add(message);
            }
        } else {
            list.add(decodeMessage(Collections.singletonList(frame)));
        }
    }

    protected BaseMessage decodeMessage(List<Frame> frames) throws Exception {
        if (frames == null || frames.isEmpty()) {
            throw new Exception("Frames can't be null or empty");
        }
        Frame head = frames.get(0);

        int cmd = head.cmd;
        int packetSize = head.totalFrameSize;
        byte[] data = new byte[packetSize];
        int pos = 0;
        for (Frame frame : frames) {
            System.arraycopy(frame.payload, 0, data, pos, frame.size);
            pos += frame.size;
        }
        try {
            int length = Snappy.uncompressedLength(data);

            data = Snappy.uncompress(data);
        } catch (IOException e) {
            throw new Exception(e);
        }

        return messageFactory.create(cmd, data);
    }

}
