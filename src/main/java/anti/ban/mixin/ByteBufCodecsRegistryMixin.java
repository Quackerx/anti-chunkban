package anti.ban.mixin;

import anti.ban.ChunkPacketState;
import anti.ban.Saver;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.IdMap;

import net.minecraft.network.VarInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.network.codec.ByteBufCodecs$29")
public class ByteBufCodecsRegistryMixin {

    // HANDLES wrong ids and large Varints

    @Redirect(method = "decode(Lnet/minecraft/network/RegistryFriendlyByteBuf;)Ljava/lang/Object;", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/IdMap;byIdOrThrow(I)Ljava/lang/Object;"))
    private Object handleInvalidRegistryId(IdMap<?> idMap, int id) {
        if (!Saver.chunkban) {
            return idMap.byIdOrThrow(id);
        }

        try {
            return idMap.byIdOrThrow(id);
        } catch (IllegalArgumentException e) {
            ChunkPacketState.markBadBlockEntityData();
            return idMap.byIdOrThrow(0);
        }
    }

    @Redirect(method = "decode(Lnet/minecraft/network/RegistryFriendlyByteBuf;)Ljava/lang/Object;", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/VarInt;read(Lio/netty/buffer/ByteBuf;)I"))
    private int handleInvalidRegistryVarInt(ByteBuf input) {
        try {
            return VarInt.read(input);
        } catch (RuntimeException e) {
            ChunkPacketState.markBadBlockEntityData();
            input.setZero(input.readerIndex(), input.readableBytes());
            return 0;
        }
    }


}
