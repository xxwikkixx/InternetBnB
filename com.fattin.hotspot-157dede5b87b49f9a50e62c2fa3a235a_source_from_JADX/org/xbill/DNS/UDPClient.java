package org.xbill.DNS;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.SecureRandom;

final class UDPClient extends Client {
    private static final int EPHEMERAL_RANGE = 64511;
    private static final int EPHEMERAL_START = 1024;
    private static final int EPHEMERAL_STOP = 65535;
    private static SecureRandom prng;
    private static volatile boolean prng_initializing;
    private boolean bound;

    final class 1 implements Runnable {
        1() {
        }

        public void run() {
            int n = UDPClient.access$000().nextInt();
            UDPClient.access$102(false);
        }
    }

    static SecureRandom access$000() {
        return prng;
    }

    static boolean access$102(boolean x0) {
        prng_initializing = x0;
        return x0;
    }

    static {
        prng = new SecureRandom();
        prng_initializing = true;
        new Thread(new 1()).start();
    }

    public UDPClient(long endTime) throws IOException {
        super(DatagramChannel.open(), endTime);
        this.bound = false;
    }

    private void bind_random(InetSocketAddress addr) throws IOException {
        if (prng_initializing) {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
            }
            if (prng_initializing) {
                return;
            }
        }
        DatagramChannel channel = (DatagramChannel) this.key.channel();
        int i = 0;
        while (i < EPHEMERAL_START) {
            try {
                InetSocketAddress temp;
                int port = prng.nextInt(EPHEMERAL_RANGE) + EPHEMERAL_START;
                if (addr != null) {
                    temp = new InetSocketAddress(addr.getAddress(), port);
                } else {
                    temp = new InetSocketAddress(port);
                }
                channel.socket().bind(temp);
                this.bound = true;
                return;
            } catch (SocketException e2) {
                i++;
            }
        }
    }

    void bind(SocketAddress addr) throws IOException {
        if (addr == null || ((addr instanceof InetSocketAddress) && ((InetSocketAddress) addr).getPort() == 0)) {
            bind_random((InetSocketAddress) addr);
            if (this.bound) {
                return;
            }
        }
        if (addr != null) {
            ((DatagramChannel) this.key.channel()).socket().bind(addr);
            this.bound = true;
        }
    }

    void connect(SocketAddress addr) throws IOException {
        if (!this.bound) {
            bind(null);
        }
        ((DatagramChannel) this.key.channel()).connect(addr);
    }

    void send(byte[] data) throws IOException {
        DatagramChannel channel = (DatagramChannel) this.key.channel();
        Client.verboseLog("UDP write", data);
        channel.write(ByteBuffer.wrap(data));
    }

    byte[] recv(int max) throws IOException {
        DatagramChannel channel = (DatagramChannel) this.key.channel();
        byte[] temp = new byte[max];
        this.key.interestOps(1);
        while (!this.key.isReadable()) {
            try {
                Client.blockUntil(this.key, this.endTime);
            } finally {
                if (this.key.isValid()) {
                    this.key.interestOps(0);
                }
            }
        }
        long ret = (long) channel.read(ByteBuffer.wrap(temp));
        if (ret <= 0) {
            throw new EOFException();
        }
        int len = (int) ret;
        byte[] data = new byte[len];
        System.arraycopy(temp, 0, data, 0, len);
        Client.verboseLog("UDP read", data);
        return data;
    }

    static byte[] sendrecv(SocketAddress local, SocketAddress remote, byte[] data, int max, long endTime) throws IOException {
        UDPClient client = new UDPClient(endTime);
        try {
            client.bind(local);
            client.connect(remote);
            client.send(data);
            byte[] recv = client.recv(max);
            return recv;
        } finally {
            client.cleanup();
        }
    }

    static byte[] sendrecv(SocketAddress addr, byte[] data, int max, long endTime) throws IOException {
        return sendrecv(null, addr, data, max, endTime);
    }
}
