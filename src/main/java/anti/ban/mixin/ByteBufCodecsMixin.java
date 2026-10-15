package anti.ban.mixin;

import anti.ban.ChunkPacketState;
import anti.ban.Saver;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.IdMap;

import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ByteBufCodecs.class)
public interface ByteBufCodecsMixin {

    @Redirect(method = "readCount", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/VarInt;read(Lio/netty/buffer/ByteBuf;)I"))
    private static int sanitizeCollectionCount(ByteBuf buf) {
        int count = VarInt.read(buf);

        if (count < 0 || count > buf.readableBytes()) {
            ChunkPacketState.markBadLightData();
            buf.setZero(buf.readerIndex(), buf.readableBytes());
            return 0;
        }

        return count;
    }
}

