package org.xbill.DNS;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import org.xbill.DNS.utils.hexdump;

class Client {
    protected long endTime;
    protected SelectionKey key;

    protected Client(SelectableChannel channel, long endTime) throws IOException {
        Selector selector = null;
        this.endTime = endTime;
        try {
            selector = Selector.open();
            channel.configureBlocking(false);
            this.key = channel.register(selector, 1);
            if (1 == null && selector != null) {
                selector.close();
            }
            if (1 == null) {
                channel.close();
            }
        } catch (Throwable th) {
            if (null == null && selector != null) {
                selector.close();
            }
            if (null == null) {
                channel.close();
            }
        }
    }

    protected static void blockUntil(SelectionKey key, long endTime) throws IOException {
        long timeout = endTime - System.currentTimeMillis();
        int nkeys = 0;
        if (timeout > 0) {
            nkeys = key.selector().select(timeout);
        } else if (timeout == 0) {
            nkeys = key.selector().selectNow();
        }
        if (nkeys == 0) {
            throw new SocketTimeoutException();
        }
    }

    protected static void verboseLog(String prefix, byte[] data) {
        if (Options.check("verbosemsg")) {
            System.err.println(hexdump.dump(prefix, data));
        }
    }

    void cleanup() throws IOException {
        this.key.selector().close();
        this.key.channel().close();
    }
}
