package net.luminis.quic.proxy;

import net.luminis.quic.*;
import net.luminis.quic.impl.QuicClientConnectionImpl;
import net.luminis.quic.log.Logger;
import net.luminis.quic.log.SysOutLogger;
import net.luminis.quic.server.ServerConnectionConfig;
import net.luminis.quic.server.ServerConnector;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClientSession {
    private InetAddress clientAddress;
    private int clientPort;
    private InetAddress remoteAddress;
    private int remotePort;
    private QuicClientConnectionImpl clientConnection;
    private ServerConnector serverConnector;
    private DatagramSocket remoteSocket;
    private long lastActivityTime;

    public ClientSession(InetAddress clientAddress, int clientPort, InetAddress remoteAddress, int remotePort,
                         QuicClientConnectionImpl clientConnection, ServerConnector serverConnector, DatagramSocket remoteSocket) {
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.clientConnection = clientConnection;
        this.serverConnector = serverConnector;
        this.remoteSocket = remoteSocket;
        this.lastActivityTime = System.currentTimeMillis();
    }

    public void updateActivityTime() {
        this.lastActivityTime = System.currentTimeMillis();
    }

    public boolean isExpired(long timeout) {
        return (System.currentTimeMillis() - lastActivityTime) > timeout;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }

    public DatagramSocket getRemoteDatagramSocket() {
        return remoteSocket;
    }

    public int getClientPort() {
        return clientPort;
    }

    public InetAddress getRemoteAddress() {
        return remoteAddress;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public QuicClientConnectionImpl getClientConnection() {
        return clientConnection;
    }

    public ServerConnector getServerConnector() {
        return serverConnector;
    }
}