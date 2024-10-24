/*
 * Copyright © 2020, 2021, 2022, 2023, 2024 Peter Doornbosch
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
package net.luminis.quic.proxy.core;

import net.luminis.quic.QuicClientConnection;
import net.luminis.quic.QuicConnection;
import net.luminis.quic.impl.QuicClientConnectionImpl;
import net.luminis.quic.log.SysOutLogger;
import net.luminis.quic.proxy.core.ProxyConnection;
import net.luminis.quic.server.ApplicationProtocolConnection;
import net.luminis.quic.server.ApplicationProtocolConnectionFactory;
import net.luminis.quic.server.ServerConnectionConfig;
import net.luminis.quic.proxy.impl.ProxyQuicConnectionImpl;
import net.luminis.quic.proxy.impl.QuicProxyConnectionImpl;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.URI;
import java.time.Duration;

public class ProxyFuzzerApplicationProtocolFactory implements ApplicationProtocolConnectionFactory {
    
    public QuicClientConnection proxy_server;

    public ProxyFuzzerApplicationProtocolFactory(String localHost, int localPort, DatagramSocket socket,
            SysOutLogger log, ServerConnectionConfig serverConnectionConfig) {

        // Initialize and return Kwik client connection
        QuicProxyConnectionImpl.Builder builder = QuicProxyConnectionImpl.newBuilder();
        builder.version(QuicConnection.QuicVersion.V1);
        builder.applicationProtocol("hq-interop");
        builder.uri(URI.create("http://" + localHost + ":" + localPort));
        builder.logger(log);
        builder.noServerCertificateCheck();
        builder.initialRtt(100);
        builder.connectTimeout(Duration.ofSeconds(10));
        builder.proxy(localHost);
        builder.socketFactory((address) -> {
            return socket;
        });
         try {
             proxy_server = builder.build();
             proxy_server.connect();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
    }

    @Override
    public ApplicationProtocolConnection createConnection(String protocol, QuicConnection quicConnection) {
        System.out.println("**************** Creating App connection ****************");
         new Thread(() -> {
             try {
                 proxy_server.connect();
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }).start();
        return new ProxyConnection(quicConnection);
    }
}
