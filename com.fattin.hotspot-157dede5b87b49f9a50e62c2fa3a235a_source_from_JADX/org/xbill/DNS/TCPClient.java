package org.xbill.DNS;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

final class TCPClient extends Client {
    public TCPClient(long endTime) throws IOException {
        super(SocketChannel.open(), endTime);
    }

    void bind(SocketAddress addr) throws IOException {
        ((SocketChannel) this.key.channel()).socket().bind(addr);
    }

    void connect(SocketAddress addr) throws IOException {
        SocketChannel channel = (SocketChannel) this.key.channel();
        if (!channel.connect(addr)) {
            this.key.interestOps(8);
            while (!channel.finishConnect()) {
                try {
                    if (!this.key.isConnectable()) {
                        Client.blockUntil(this.key, this.endTime);
                    }
                } catch (Throwable th) {
                    if (this.key.isValid()) {
                        this.key.interestOps(0);
                    }
                }
            }
            if (this.key.isValid()) {
                this.key.interestOps(0);
            }
        }
    }

    void send(byte[] data) throws IOException {
        SocketChannel channel = (SocketChannel) this.key.channel();
        Client.verboseLog("TCP write", data);
        byte[] lengthArray = new byte[]{(byte) (data.length >>> 8), (byte) (data.length & KEYRecord.PROTOCOL_ANY)};
        ByteBuffer[] buffers = new ByteBuffer[]{ByteBuffer.wrap(lengthArray), ByteBuffer.wrap(data)};
        int nsent = 0;
        this.key.interestOps(4);
        while (nsent < data.length + 2) {
            if (this.key.isWritable()) {
                long n = channel.write(buffers);
                if (n < 0) {
                    throw new EOFException();
                }
                nsent += (int) n;
                try {
                    if (nsent < data.length + 2 && System.currentTimeMillis() > this.endTime) {
                        throw new SocketTimeoutException();
                    }
                } catch (Throwable th) {
                    if (this.key.isValid()) {
                        this.key.interestOps(0);
                    }
                }
            } else {
                Client.blockUntil(this.key, this.endTime);
            }
        }
        if (this.key.isValid()) {
            this.key.interestOps(0);
        }
    }

    private byte[] _recv(int length) throws IOException {
        SocketChannel channel = (SocketChannel) this.key.channel();
        int nrecvd = 0;
        byte[] data = new byte[length];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        this.key.interestOps(1);
        while (nrecvd < length) {
            if (this.key.isReadable()) {
                long n = (long) channel.read(buffer);
                if (n < 0) {
                    throw new EOFException();
                }
                nrecvd += (int) n;
                if (nrecvd < length) {
                    try {
                        if (System.currentTimeMillis() > this.endTime) {
                            throw new SocketTimeoutException();
                        }
                    } catch (Throwable th) {
                        if (this.key.isValid()) {
                            this.key.interestOps(0);
                        }
                    }
                } else {
                    continue;
                }
            } else {
                Client.blockUntil(this.key, this.endTime);
            }
        }
        if (this.key.isValid()) {
            this.key.interestOps(0);
        }
        return data;
    }

    byte[] recv() throws IOException {
        byte[] buf = _recv(2);
        byte[] data = _recv(((buf[0] & KEYRecord.PROTOCOL_ANY) << 8) + (buf[1] & KEYRecord.PROTOCOL_ANY));
        Client.verboseLog("TCP read", data);
        return data;
    }

    static byte[] sendrecv(SocketAddress local, SocketAddress remote, byte[] data, long endTime) throws IOException {
        TCPClient client = new TCPClient(endTime);
        if (local != null) {
            try {
                client.bind(local);
            } catch (Throwable th) {
                client.cleanup();
            }
        }
        client.connect(remote);
        client.send(data);
        byte[] recv = client.recv();
        client.cleanup();
        return recv;
    }

    static byte[] sendrecv(SocketAddress addr, byte[] data, long endTime) throws IOException {
        return sendrecv(null, addr, data, endTime);
    }
}
