package mindustryvoice;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;

import arc.Core;
import arc.net.Connection;
import arc.net.DcReason;
import arc.net.NetListener;
import arc.net.Server;
import arc.util.Log;
import arc.util.Threads;
import mindustry.gen.Groups;
import mindustryvoice.api.Internal;
import mindustryvoice.api.PacketSerializer;
import mindustryvoice.api.VoiceMessage;
import arc.net.NetListener.ThreadedListener;

public class VoiceServer {
    private final Server server;
    private final int port;

    private Thread updateThread;

    public VoiceServer(int port) {
        this.port = port;
        this.server = new Server(Internal.BUFFER_SIZE, Internal.BUFFER_SIZE, new PacketSerializer());
        
        server.addListener(new ThreadedListener(new NetListener() {
            @Override
            public void connected(Connection connection) {
                Log.info("Voice client @ has connected. (@)", connection.getID(),
                        connection.getRemoteAddressTCP());
            }

            @Override
            public void disconnected(Connection connection, DcReason reason) {
                Log.info("Voice client @ has disconnected due to @.", connection.getID(), reason);
            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof VoiceMessage m) {
                    if (Groups.player.contains(p -> p.id == m.playerId));
                        server.sendToAllExceptTCP(connection.getID(), object);
                }
            }
        }));
    }

    public void start() throws IOException {
        server.bind(port);
        this.updateThread = Threads.daemon("Voice Server", () -> {
            try {
                server.run();
            } catch (Throwable e) {
                if (!(e instanceof ClosedSelectorException))
                    Log.err(e);
            }
        });
    }
}
