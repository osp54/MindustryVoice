package mindustryvoice;

import arc.net.Connection;
import arc.net.DcReason;
import arc.util.Log;
import mindustry.gen.Player;

public class VoiceConnection {
    public final Connection connection;
    public final Player player;

    public VoiceConnection(Connection connection, Player player) {
        this.connection = connection;
        this.player = player;
    }

    public void close() {
        connection.close(DcReason.closed);
    }

    public void send(Object object, boolean reliable) {
        if (reliable) {
            connection.sendTCP(object);
        } else {
            connection.sendUDP(object);
        }
    }
}
