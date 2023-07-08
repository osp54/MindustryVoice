package mindustryvoice.api;

public class VoiceMessage {
    public int playerId;
    public byte[] samples;

	public VoiceMessage(int playerId, byte[] samples) {
		this.playerId = playerId;
		this.samples = samples;
	}

    public VoiceMessage() {
    }
}