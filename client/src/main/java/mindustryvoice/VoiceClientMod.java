package mindustryvoice;

import java.io.IOException;

import arc.Events;
import arc.util.Log;
import arc.util.Threads;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.mod.Mod;
import mindustryvoice.api.VoiceMessage;
import net.labymod.opus.OpusCodec;

public class VoiceClientMod extends Mod {
    public static VoiceClientMod instance;

    private OpusCodec codec;
    private AudioPlayer audioPlayer;
    private AudioRecorder audioRecorder;

    private VoiceClient client;

    private Thread recordingThread;

    private String latestServerIp;

    public VoiceClientMod() {
        instance = this;
    }

    @Override
    public void init() {
        try {
			OpusCodec.setupWithTemporaryFolder();
		} catch (Exception e) {
			Log.err(e);
            Vars.ui.showException("Error ocurred while loading Opus Codec", e);
            return;
		}

        codec = OpusCodec.createDefault();
        audioPlayer = AudioPlayer.create();
        audioRecorder = AudioRecorder.create();

        client = new VoiceClient();

        Vars.ui.menufrag.addButton("Audio", () -> {
            AudioRecorder recorder = AudioRecorder.create();
            int sampleRate = 48000;
            int numSamples = sampleRate * 10; // Record for 10 seconds
            byte[] samples = recorder.read(numSamples);
            recorder.dispose();

            AudioPlayer player = AudioPlayer.create();
            player.writeSamples(samples);
            player.dispose();
        });

        Events.on(EventType.ClientServerConnectEvent.class, e -> latestServerIp=e.ip);
        Vars.netClient.addPacketHandler("voice_server_port", port -> {
            if (client.isConnected()) client.close();

            client.setIp(latestServerIp);
            client.setPort(Integer.parseInt(port));

            try {
				client.connect();
                
                startRecordingThread();
			} catch (Exception e) {
                Log.err(e);
				Vars.ui.showException("Error occurred while connecting to voice server", e);
			}
        });
    }

    public void startRecordingThread() {
        if (recordingThread != null && !recordingThread.isInterrupted()) recordingThread.interrupt();

        recordingThread = Threads.daemon("Recording Thread", () -> {
            while (!Thread.interrupted()) {
                byte[] data = new byte[codec.getChannels() * codec.getFrameSize() * 2];
                audioRecorder.read(data.length);

                byte[] decodedSamples = codec.decodeFrame(data);
                VoiceMessage message = new VoiceMessage(decodedSamples);
                if (client.isConnected()) client.sendUDP(message);
            }
        });
    }

    public void decodeAndPlay(VoiceMessage message) {
        byte[] decoded = codec.decodeFrame(message.samples);

        audioPlayer.writeSamples(decoded);
    }
}