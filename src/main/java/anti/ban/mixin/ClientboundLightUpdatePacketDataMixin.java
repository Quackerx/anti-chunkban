package anti.ban.mixin;

import anti.ban.ChunkPacketState;
import anti.ban.Saver;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;
import java.util.List;

@Mixin(ClientboundLightUpdatePacketData.class)
public class ClientboundLightUpdatePacketDataMixin {

    // SANITIZES the light data in this packet, important for chunkbans

    @Shadow @Final @Mutable
    private BitSet skyYMask;

    @Shadow @Final @Mutable
    private BitSet blockYMask;

    @Shadow @Final @Mutable
    private BitSet emptySkyYMask;

    @Shadow @Final @Mutable
    private BitSet emptyBlockYMask;

    @Shadow @Final @Mutable
    private List<byte[]> skyUpdates;

    @Shadow @Final @Mutable
    private List<byte[]> blockUpdates;

    @Inject(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;II)V", at = @At("RETURN"))
    private void sanitizeLightData(FriendlyByteBuf input, int x, int z, CallbackInfo ci) {
        if (!Saver.chunkban) {return;}

        if (!ChunkPacketState.consumeBadLightData()) {
            return;
        }

        this.skyYMask = new BitSet();
        this.blockYMask = new BitSet();

        this.emptySkyYMask = new BitSet();
        this.emptyBlockYMask = new BitSet();

        this.emptySkyYMask.set(0, 256);
        this.emptyBlockYMask.set(0, 256);

        this.skyUpdates = List.of();
        this.blockUpdates = List.of();

        input.readerIndex(input.writerIndex());
    }
}

