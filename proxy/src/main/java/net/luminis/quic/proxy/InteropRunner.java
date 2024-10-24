/*
 * Copyright © 2024 Peter Doornbosch
 *
 * This file is part of Kwik, an implementation of the QUIC protocol in Java.
 *
 * Kwik is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * Kwik is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.luminis.quic.proxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.luminis.quic.KwikVersion;
import net.luminis.quic.QuicConnection;
import net.luminis.quic.log.FileLogger;
import net.luminis.quic.log.Logger;
import net.luminis.quic.log.SysOutLogger;
import net.luminis.quic.server.ApplicationProtocolConnectionFactory;
import net.luminis.quic.server.ServerConnectionConfig;
import net.luminis.quic.server.ServerConnector;

import net.luminis.quic.proxy.core.ProxyFuzzerApplicationProtocolFactory;

public class InteropRunner {

    private static final int BUFFER_SIZE = 65535;
    private static final int TIMEOUT = 10000;

    private static final Map<String, ClientSession> clientSessions = new HashMap<>();

    public static void main(String[] args) throws Exception {
        String localHost = "localhost";
        int localPort = 1883;

        String remoteHost = "localhost";
        int remotePort = 4433;
        System.out.println("Starting QUIC server on " + localHost + ":" + localPort);
        System.out.println("Forwarding to " + remoteHost + ":" + remotePort);
        DatagramSocket udp_server = new DatagramSocket(localPort, InetAddress.getByName(localHost)); //
        System.out.println("Successfully listening on " + localHost + ":" + localPort);

        byte[] buffer = new byte[BUFFER_SIZE];

        while (!udp_server.isClosed()) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            udp_server.receive(packet);

            InetAddress clientAddress = packet.getAddress();
            int clientPort = packet.getPort();
            System.out.println("New connection from client: " + clientAddress + ":" + clientPort);
            System.out.println("Received from client: " + packet.getLength() + " bytes");
            String clientKey = clientAddress.toString() + ":" + clientPort;

            ClientSession session;

            synchronized (clientSessions) {
                if (clientSessions.containsKey(clientKey)) {
                    session = clientSessions.get(clientKey);
                } else {
                    System.out.println("[==>] New session for client " + clientAddress + ":" + clientPort);

                    // Create a new session for the client
                    DatagramSocket remoteSocket = new DatagramSocket();
                    ServerConnector serverConnector = null;
                    try {

                        List<QuicConnection.QuicVersion> supportedVersions = List.of(QuicConnection.QuicVersion.V1);
                        System.out.println("Supported versions: " + supportedVersions);
                        ServerConnectionConfig serverConnectionConfig = ServerConnectionConfig.builder()
                                .maxIdleTimeoutInSeconds(30)
                                .maxUnidirectionalStreamBufferSize(1_000_000)
                                .maxBidirectionalStreamBufferSize(1_000_000)
                                .maxConnectionBufferSize(10_000_000)
                                .maxOpenPeerInitiatedUnidirectionalStreams(10)
                                .maxOpenPeerInitiatedBidirectionalStreams(100)
                                .connectionIdLength(8)
                                .build();
                        System.out.println("Server connection config: " + serverConnectionConfig);
                        System.out.println("Binding to port: " + remotePort);

                        SysOutLogger log = new SysOutLogger();
                        log.logPackets(true);
                        log.logInfo(true);
                        log.logDebug(true);

                        serverConnector = ServerConnector.builder()
                                .withPort(remotePort)
                                // .withKeyStore(keyStore, "secret", keyPassword.toCharArray())
                                .withCertificate(new FileInputStream("proxy/certs/cert.pem"),
                                        new FileInputStream("proxy/certs/key.pem"))
                                .withSupportedVersions(supportedVersions)
                                .withConfiguration(serverConnectionConfig)
                                .withLogger(log)
                                .withSocket(udp_server)
                                .build();

                        ProxyFuzzerApplicationProtocolFactory proxyApplicationProtocolFactory = new ProxyFuzzerApplicationProtocolFactory(
                                remoteHost, remotePort, remoteSocket, log, serverConnectionConfig);
                        String protocol = "hq-interop";
                        serverConnector.registerApplicationProtocol(protocol, proxyApplicationProtocolFactory);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    serverConnector.start();

                    // Create a new session for the client
                    session = new ClientSession(clientAddress, clientPort,
                            InetAddress.getByName(remoteHost), remotePort,
                            null, serverConnector, remoteSocket);
                    clientSessions.put(clientKey, session);
                }
            }
        }
    }
}
