package anti.ban;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public final class ChunkCodecs {
    private ChunkCodecs() {}

    public static <B extends ByteBuf, T> StreamCodec<B, T> discardTrailing(StreamCodec<B, T> real) {
        return new StreamCodec<>() {
            @Override public T decode(B buf) {
                T packet = real.decode(buf);
                if (buf.readableBytes() > 0) buf.skipBytes(buf.readableBytes());
                return packet;
            }
            @Override public void encode(B buf, T value) { real.encode(buf, value); }
        };
    }

    // registry codec that survives bad ids
    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> safeRegistry(ResourceKey<? extends Registry<T>> key) {
        return new StreamCodec<>() {
            @Override public T decode(RegistryFriendlyByteBuf buf) {
                int id;
                try {
                    id = VarInt.read(buf);
                } catch (RuntimeException e) {
                    ChunkPacketState.markBadBlockEntityData();
                    buf.setZero(buf.readerIndex(), buf.readableBytes());
                    id = 0;
                }
                Registry<T> registry = buf.registryAccess().lookupOrThrow(key);
                try {
                    return registry.byIdOrThrow(id);
                } catch (IllegalArgumentException e) {
                    ChunkPacketState.markBadBlockEntityData();
                    return registry.byIdOrThrow(0);
                }
            }
            @Override public void encode(RegistryFriendlyByteBuf buf, T value) {
                ByteBufCodecs.registry(key).encode(buf, value);
            }
        };
    }
}
