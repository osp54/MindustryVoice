package mindustryvoice.opus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.NoSuchElementException;

public class OpusCodec {
    private final OpusCodecOptions opusOptions = new OpusCodecOptions();

    private OpusCodec() {

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

    /**
     * Encodes a chunk of raw PCM data.
     *
     * @param bytes data to encode. Must have a length of CHANNELS * FRAMESIZE * 2.
     * @return encoded data
     *         <p>
     *         throws {@link IllegalArgumentException} if length is invalid
     */
    public byte[] encodeFrame(byte[] bytes, int offset, int length) {
        if (length != this.getChannels() * this.getFrameSize() * 2)
            throw new IllegalArgumentException(
                    String.format("data length must be == CHANNELS * FRAMESIZE * 2 (%d bytes) but is %d bytes",
                            this.getChannels() * this.getFrameSize() * 2, bytes.length));
        return this.encodeFrame(bytes, offset, length);
    }

    private native byte[] encodeFrame(long encoder, byte[] in, int offset, int length);

    /**
     * Decodes a chunk of opus encoded pcm data.
     *
     * @param bytes data to decode. Length may vary because the less complex the
     *              encoded pcm data is, the compressed data size is smaller.
     * @return encoded data.
     */
    private native byte[] decodeFrame(byte[] out);

    private native void destroyEncoder();

    private native void destroyDecoder();
}