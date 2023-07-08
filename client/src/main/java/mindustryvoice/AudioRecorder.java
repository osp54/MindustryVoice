package mindustryvoice;

import arc.*;
import arc.util.*;
import mindustryvoice.api.Internal;

import java.nio.*;

public abstract class AudioRecorder {
    public static AudioRecorder create() {
        if (OS.isAndroid) {
            return new AndroidAudioRecorder();
        } else {
            return new DesktopAudioRecorder();
        }
    }

    public abstract byte[] read(int numSamples);

    public abstract void dispose();
}

class AndroidAudioRecorder extends AudioRecorder {
    private android.media.AudioRecord recorder;

    public AndroidAudioRecorder() {
        int bufferSize = android.media.AudioRecord.getMinBufferSize(Internal.SAMPLE_RATE, android.media.AudioFormat.CHANNEL_IN_MONO, android.media.AudioFormat.ENCODING_PCM_16BIT);
        recorder = new android.media.AudioRecord(android.media.MediaRecorder.AudioSource.MIC, Internal.SAMPLE_RATE, android.media.AudioFormat.CHANNEL_IN_MONO, android.media.AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        recorder.startRecording();
    }

    @Override
    public byte[] read(int numSamples) {
        short[] samples = new short[numSamples];
        recorder.read(samples, 0, samples.length);
        ByteBuffer buffer = ByteBuffer.allocate(samples.length * 2);
        buffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(samples);
        return buffer.array();
    }

    @Override
    public void dispose() {
        recorder.stop();
        recorder.release();
    }
}

class DesktopAudioRecorder extends AudioRecorder {
    private javax.sound.sampled.TargetDataLine line;

    public DesktopAudioRecorder() {
        try {
            javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(Internal.SAMPLE_RATE, 16, 1, true, false);
            line = javax.sound.sampled.AudioSystem.getTargetDataLine(format);
            line.open(format);
            line.start();
        } catch (Exception e) {
            throw new ArcRuntimeException(e);
        }
    }

    @Override
    public byte[] read(int numSamples) {
        byte[] data = new byte[numSamples * 2];
        line.read(data, 0, data.length);
        return data;
    }

    @Override
    public void dispose() {
        line.stop();
        line.close();
    }
}