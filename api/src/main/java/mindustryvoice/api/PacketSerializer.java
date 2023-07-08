package mindustryvoice.api;

import java.nio.ByteBuffer;

import arc.net.FrameworkMessage;
import arc.net.FrameworkMessage.DiscoverHost;
import arc.net.FrameworkMessage.KeepAlive;
import arc.net.FrameworkMessage.Ping;
import arc.net.FrameworkMessage.RegisterTCP;
import arc.net.FrameworkMessage.RegisterUDP;
import arc.util.io.ByteBufferInput;
import arc.util.io.ByteBufferOutput;
import arc.net.NetSerializer;

public class PacketSerializer implements NetSerializer {

    @Override
    public void write(ByteBuffer buffer, Object object) {
        ByteBufferOutput out = new ByteBufferOutput(buffer);

        if (object instanceof FrameworkMessage m) {
            out.write(-2);
            writeFramework(buffer, m);
        } else if (object instanceof VoiceMessage m) {
            out.write(1);
            out.write(m.playerId);
            out.write(m.samples.length);
            out.write(m.samples);
        }
    }

    @Override
    public Object read(ByteBuffer buffer) {
        ByteBufferInput in = new ByteBufferInput(buffer);
        byte type = in.readByte();

        if (type == -2) {
            return readFramework(buffer);
        } else if (type == 1) {
            var message = new VoiceMessage();

            message.playerId = in.readInt();
            
            message.samples = new byte[in.readInt()];
            in.readFully(message.samples);

            return message;
        }

        return null;
    }

    private void writeFramework(ByteBuffer buffer, FrameworkMessage message) {
        if (message instanceof Ping p) {
            buffer.put((byte) 0);
            buffer.putInt(p.id);
            buffer.put(p.isReply ? 1 : (byte) 0);
        } else if (message instanceof DiscoverHost) {
            buffer.put((byte) 1);
        } else if (message instanceof KeepAlive) {
            buffer.put((byte) 2);
        } else if (message instanceof RegisterUDP p) {
            buffer.put((byte) 3);
            buffer.putInt(p.connectionID);
        } else if (message instanceof RegisterTCP p) {
            buffer.put((byte) 4);
            buffer.putInt(p.connectionID);
        }
    }

    private FrameworkMessage readFramework(ByteBuffer buffer) {
        byte id = buffer.get();

        if (id == 0) {
            Ping p = new Ping();
            p.id = buffer.getInt();
            p.isReply = buffer.get() == 1;
            return p;
        } else if (id == 1) {
            return FrameworkMessage.discoverHost;
        } else if (id == 2) {
            return FrameworkMessage.keepAlive;
        } else if (id == 3) {
            RegisterUDP p = new RegisterUDP();
            p.connectionID = buffer.getInt();
            return p;
        } else if (id == 4) {
            RegisterTCP p = new RegisterTCP();
            p.connectionID = buffer.getInt();
            return p;
        } else {
            throw new RuntimeException("Unknown framework message!");
        }
    }

}
