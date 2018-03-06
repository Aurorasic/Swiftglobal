package cn.primeledger.cas.global.p2p.discover;

import cn.primeledger.cas.global.p2p.exception.ParseAddrException;
import cn.primeledger.cas.global.p2p.message.BaseMessage;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;


/**
 * An abstract public ip address discover
 *
 * This implementation takes responsibility for making the request to the webservice and does bookkeeping to track
 * average duration . Subclasses are responsible for parsing the webservice response.
 *
 * @author zhao xiaogang
 */
public abstract class BaseAddrDiscovery implements AddrDiscovery {
    private final static int MAX_SIZE = 5;
    private static Map<String, Long> durationMap = new HashMap<>(MAX_SIZE);
    private URL serviceUrl;

    public BaseAddrDiscovery(URL serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    /**
     * Parses a webservice response into an IP address.
     *
     * @param content The webservice response (can be null).
     * @return An IP address (cannot be null).
     * @throws ParseAddrException When the provided content cannot be parsed.
     */
    public abstract String parse(String content) throws ParseAddrException;

    @Override
    public String resolveAddress() throws IOException, ParseAddrException {
        final long start = System.currentTimeMillis();

        String content;
        try (final InputStreamReader reader = new InputStreamReader(serviceUrl.openStream())) {
            char[] buffer = new char[39];
            final StringBuilder sb = new StringBuilder(buffer.length);

            int length;
            while ((length = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, length);
            }

            content = sb.toString();
        }

        final String result = parse(content);

        final long duration = System.currentTimeMillis() - start;
        durationMap.put(serviceUrl.getPath(), duration);

        return result;
    }

    @Override
    public long getDuration() {
        Long duration = durationMap.get(serviceUrl.getPath());
        return  duration == null ? 0 : duration;
    }
}
