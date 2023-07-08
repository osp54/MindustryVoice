package mindustryvoice;

import java.io.IOException;

import arc.Events;
import arc.util.Log;
import mindustry.game.EventType.PlayerJoin;
import mindustry.gen.Call;
import mindustry.mod.Plugin;

public class VoiceServerPlugin extends Plugin {
    @Override
    public void init() {
        VoiceServer server = new VoiceServer(3000);
        try {
			server.start();
		} catch (IOException e) {
			Log.err("Exception occurred while starting the server", e);
            return;
		}

        Events.on(PlayerJoin.class, e -> {
            Call.clientPacketReliable(e.player.con, "voice_server_port", "3000");
        });
    }
}
