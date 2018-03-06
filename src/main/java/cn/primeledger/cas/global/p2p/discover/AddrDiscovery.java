package cn.primeledger.cas.global.p2p.discover;


import cn.primeledger.cas.global.p2p.exception.ParseAddrException;

import java.io.IOException;
import java.net.InetAddress;

/**
 * A  interface for a task that uses an external entity to report back the IP address of the host
 * that is executing this application.
 *
 * @author zhao xiaogang
 */

public interface AddrDiscovery {

    /**
     * Returns the IP address as reported by the external entity.
     *
     * @return The IP address (none null).
     * @throws IOException    When communication with the external entity fails.
     * @throws ParseAddrException When the response of the external entity cannot be parsed as an IP address.
     */
    String resolveAddress() throws IOException, ParseAddrException;

    /**
     * Returns the execution duration, in milliseconds. This value is to be framed to the most recent
     * executions, and should include only executions that were successful.
     *
     * @return an duration in milliseconds. Zero when no successful executions have occurred.
     */
    long getDuration();
}
