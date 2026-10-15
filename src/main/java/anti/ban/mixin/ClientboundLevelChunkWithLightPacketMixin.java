package anti.ban.mixin;

import anti.ban.ChunkCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientboundLevelChunkWithLightPacket.class)
public class ClientboundLevelChunkWithLightPacketMixin {
    @Shadow
    @Final
    @Mutable
    public static StreamCodec<RegistryFriendlyByteBuf, ClientboundLevelChunkWithLightPacket> STREAM_CODEC;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void discardTrailing(CallbackInfo ci) {
        STREAM_CODEC = ChunkCodecs.discardTrailing(STREAM_CODEC);
    }
}

