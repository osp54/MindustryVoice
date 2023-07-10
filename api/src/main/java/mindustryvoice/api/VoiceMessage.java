package mindustryvoice.api;

public class VoiceMessage {
    public int playerId;

    public VoiceMessage setPlayerId(int playerId) {
        this.playerId = playerId;
        return this;
    }

    public byte[] samples;

	public VoiceMessage(byte[] samples) {
		this.samples = samples;
	}

    public VoiceMessage() {
    }
}