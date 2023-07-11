package mindustryvoice.opus;

import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

public class OpusCodec {
    private static OpusCodec INSTANCE;

    public static OpusCodec instance() {
        if (INSTANCE == null)
            return INSTANCE = new OpusCodec();
        return INSTANCE;
    }

    public final int frameSize = 960;
    public final int sampleRate = 48000;
    public final int channels = 1;
    public final int bitrate = 64000;
    public final int maxFrameSize = 6 * 960;
    public final int maxPacketSize = 3 * 1276;

    private final OpusCodecOptions opusOptions = new OpusCodecOptions();

    private OpusCodec() {
        System.loadLibrary("opus-cp");
    }

    public int getFrameSize() {
        return this.opusOptions.frameSize;
    }

    public int getSampleRate() {
        return this.opusOptions.sampleRate;
    }

    public int getChannels() {
        return this.opusOptions.channels;
    }

    public int getBitrate() {
        return this.opusOptions.bitrate;
    }

    public int getMaxFrameSize() {
        return this.opusOptions.maxFrameSize;
    }

    public int getMaxPacketSize() {
        return this.opusOptions.maxPacketSize;
    }

    /**
     * Encodes a chunk of raw PCM data.
     *
     * @param bytes data to encode. Must have a length of CHANNELS * FRAMESIZE * 2.
     * @return encoded data
     *         <p>
     *         throws {@link IllegalArgumentException} if bytes has an invalid
     *         length
     */
    public byte[] encodeFrame(byte[] bytes) {
        return this.encodeFrame(bytes, 0, bytes.length);
    }

    private native byte[] encodeFrame(byte[] in, int offset, int length);

    /**
     * Decodes a chunk of opus encoded pcm data.
     *
     * @param bytes data to decode. Length may vary because the less complex the
     *              encoded pcm data is, the compressed data size is smaller.
     * @return encoded data.
     */
    private native byte[] decodeFrame(byte[] out);

    public static void main(String[] args) throws LineUnavailableException, IOException {
        OpusCodec codec = OpusCodec.instance();

        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 48000, 16, 1, 2, 48000, false);

        TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
        microphone.open(microphone.getFormat());
        microphone.start();

        // get default speaker and open it at the selected format
        SourceDataLine speaker = AudioSystem.getSourceDataLine(format);
        speaker.open(microphone.getFormat());
        speaker.start();

        while (true) {
            // Reading microphone data
            byte[] data = new byte[codec.getChannels() * codec.getFrameSize() * 2];
            microphone.read(data, 0, data.length);

            // Encoding PCM data chunk
            byte[] encode = codec.encodeFrame(data);
            //System.out.println(Arrays.toString(encode));
            // Decoding PCM data chunk
            byte[] decoded = codec.decodeFrame(encode);
            speaker.write(decoded, 0, decoded.length);
        }
    }
}