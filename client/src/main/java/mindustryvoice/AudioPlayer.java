package mindustryvoice;

import arc.util.*;
import mindustryvoice.api.Internal;

public abstract class AudioPlayer {
    public static AudioPlayer create() {
        if (OS.isAndroid) {
            return new AndroidAudioPlayer();
        } else {
            return new DesktopAudioPlayer();
        }
    }

    public abstract void writeSamples(byte[] samples);

    public abstract void dispose();
}

class AndroidAudioPlayer extends AudioPlayer {
    private android.media.AudioTrack track;

    public AndroidAudioPlayer() {
        int bufferSize = android.media.AudioTrack.getMinBufferSize(Internal.SAMPLE_RATE, android.media.AudioFormat.CHANNEL_OUT_MONO, android.media.AudioFormat.ENCODING_PCM_16BIT);
        track = new android.media.AudioTrack.Builder()
            .setAudioAttributes(new android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
            .setAudioFormat(new android.media.AudioFormat.Builder()
                .setEncoding(android.media.AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(Internal.SAMPLE_RATE)
                .setChannelMask(android.media.AudioFormat.CHANNEL_OUT_MONO)
                .build())
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(android.media.AudioTrack.MODE_STREAM)
            .build();
        track.play();
    }

    @Override
    public void writeSamples(byte[] samples) {
        track.write(samples, 0, samples.length);
    }

    @Override
    public void dispose() {
        track.stop();
        track.release();
    }
}

class DesktopAudioPlayer extends AudioPlayer {
    private javax.sound.sampled.SourceDataLine line;

    public DesktopAudioPlayer() {
        try {
            javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(Internal.SAMPLE_RATE, 16, 1, true, false);
            line = javax.sound.sampled.AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
        } catch (Exception e) {
            throw new ArcRuntimeException(e);
        }
    }

    @Override
    public void writeSamples(byte[] samples) {
        line.write(samples, 0, samples.length);
    }

    @Override
    public void dispose() {
        line.stop();
        line.close();
    }
}