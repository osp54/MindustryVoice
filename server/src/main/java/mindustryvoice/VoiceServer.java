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
import mindustry.gen.Player;
import mindustryvoice.api.ConnectPacket;
import mindustryvoice.api.Internal;
import mindustryvoice.api.PacketSerializer;
import mindustryvoice.api.VoiceMessage;
import arc.net.NetListener.ThreadedListener;
import arc.struct.Seq;

public class VoiceServer {
    public Seq<VoiceConnection> connections = new Seq<>();

    private final Server server;
    public int port;

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
                if (connection.getArbitraryData() instanceof VoiceConnection con) {
                    connections.remove(con);
                    Log.info("Voice client @ (@) due to @.", con.player.plainName(), con.player.uuid(), reason);
                } else {
                    Log.info("Voice client @ has disconnected due to @.", connection.getRemoteAddressTCP(), reason);
                }
            }

            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof ConnectPacket packet && connection.getArbitraryData() == null) {
                    if (packet.uuid == null) {
                        connection.close(DcReason.closed);
                        return;
                    }
                    Player player = Groups.player.find(p -> p.uuid().equals(packet.uuid));

                    if (player != null) {
                        VoiceConnection voiceCon = new VoiceConnection(connection, player);

                        connection.setArbitraryData(voiceCon);
                        connections.add(voiceCon);
                    } else
                        connection.close(DcReason.closed);
                    return;
                }

                if (object instanceof VoiceMessage message && connection.getArbitraryData() instanceof VoiceConnection con) {
                    connections.each(other -> !other.equals(con), c -> c.send(message.setPlayerId(con.player.id), false));
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

    public boolean isRunning() {
        return updateThread != null && updateThread.isAlive();
    }
}
