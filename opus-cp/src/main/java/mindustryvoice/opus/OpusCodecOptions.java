package mindustryvoice.opus;

public class OpusCodecOptions {
    public final int frameSize = 960;
    public final int sampleRate = 48000;
    public final int channels = 1;
    public final int bitrate = 64000;
    public final int maxFrameSize = 6 * 960;
    public final int maxPacketSize = 3 * 1276;

    public OpusCodecOptions() {
    }
}