package mindustryvoice;

import java.io.IOException;
import java.net.ServerSocket;

import arc.Events;
import arc.util.Log;
import mindustry.game.EventType.PlayerJoin;
import mindustry.game.EventType.PlayerLeave;
import mindustry.gen.Call;
import mindustry.mod.Plugin;
import mindustry.net.Administration;

public class VoiceServerPlugin extends Plugin {
    private VoiceServer server;

    @Override
    public void init() {
        var voicePort = new Administration.Config(
            "voicePort",
            "Port on which the voice server will be running", 0, "voicePort");
        
        int port = voicePort.num();

        if (port == 0) {
            try {
                port = getRandomAvailablePort();
            } catch (IOException e) {
                Log.err("Failed to find available port. Please specify voice port using config voicePort <number>");
            }
        }

        server = new VoiceServer(port);
        try {
			server.start();
            Log.info("Voice Server running on localhost:@", port);
		} catch (IOException e) {
			Log.err("Exception occurred while starting the server", e);
            return;
		}

        Events.on(PlayerJoin.class, e -> {
            if (server.isRunning()) Call.clientPacketReliable(e.player.con, "voice_server_port", String.valueOf(server.port));
        });

        Events.on(PlayerLeave.class, e -> {
            VoiceConnection voiceCon = server.connections.find(con -> con.player.equals(e.player));
            if (voiceCon != null) voiceCon.close();
        });
    }

    private static int getRandomAvailablePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }
}
