package anti.ban.mixin;

import anti.ban.Saver;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ClientboundLevelChunkPacketData.class)
public class ClientboundLevelChunkPacketDataMixin {
    @ModifyConstant(method = "<init>(Lnet/minecraft/network/RegistryFriendlyByteBuf;II)V", constant = @Constant(intValue = 2097152))
    private static int duck$increaseChunkDataLimit(int original) {
        if (!Saver.chunkban) {return original;}

        return 16 * 1024 * 1024;
    }
}

