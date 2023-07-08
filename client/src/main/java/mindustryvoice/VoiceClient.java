package mindustryvoice;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;

import arc.net.Client;
import arc.net.Connection;
import arc.net.DcReason;
import arc.net.NetListener;
import arc.net.NetListener.ThreadedListener;
import arc.util.Log;
import arc.util.Threads;
import mindustryvoice.api.PacketSerializer;
import mindustryvoice.api.VoiceMessage;
import mindustryvoice.api.Internal;

public class VoiceClient {
    private final Client client;

    public String ip;

    public int port;

    public Thread updateThread;

    public VoiceClient() {
        this.client = new Client(Internal.BUFFER_SIZE, Internal.BUFFER_SIZE, new PacketSerializer());
        this.client.addListener(new ThreadedListener(new NetListener() {
            @Override
            public void connected(Connection connection) {
                Log.info("[Sock] Connected to Sock server @. (@)", connection.getID(),
                        connection.getRemoteAddressTCP());
            }

            @Override
            public void disconnected(Connection connection, DcReason reason) {
                Log.info("[Sock] Disconnected from Sock server @ due to @", connection.getID(), reason);
            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof VoiceMessage m) {
                    VoiceClientMod.instance.decodeAndPlay(m);
                }
            }
        }));
    }

    public void connect() throws IOException {
        this.updateThread = Threads.daemon("Voice Client", () -> {
            try {
                client.run();
            } catch (Throwable e) {
                if (!(e instanceof ClosedSelectorException))
                    Log.err(e);
            }
        });

        client.connect(1000, ip, port);
    }

    public void close() {
        client.close();
    }

    public void sendUDP(Object object) {
        client.sendUDP(object);
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
