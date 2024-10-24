module tech.kwik.core {
    requires tech.kwik.agent15;
    requires at.favre.lib.hkdf;

    exports net.luminis.quic;
    exports net.luminis.quic.concurrent;
    exports net.luminis.quic.generic;
    exports net.luminis.quic.server;
    exports net.luminis.quic.log;
    exports net.luminis.quic.common to tech.kwik.qlog, tech.kwik.proxy;
    exports net.luminis.quic.frame to tech.kwik.qlog, tech.kwik.proxy;
    exports net.luminis.quic.packet to tech.kwik.qlog, tech.kwik.proxy;
    exports net.luminis.quic.util to tech.kwik.qlog, tech.kwik.proxy;
    exports net.luminis.quic.impl;
    exports net.luminis.quic.ack;
    exports net.luminis.quic.cid;
    exports net.luminis.quic.client;
    exports net.luminis.quic.crypto;
    exports net.luminis.quic.receive;
    exports net.luminis.quic.send;
    exports net.luminis.quic.stream;
    exports net.luminis.quic.tls;
    exports net.luminis.quic.recovery;
    exports net.luminis.quic.cc;
}
