package anti.chunk.client.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FriendlyByteBuf.class)
public abstract class MixinPacketByteBuf {
    @Shadow
    public abstract Tag readNbt(NbtAccounter sizeTracker);

    @Inject(
            method = "readNbt()Lnet/minecraft/nbt/CompoundTag;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onReadNbt(CallbackInfoReturnable<CompoundTag> cir) {
            Tag result = this.readNbt(NbtAccounter.unlimitedHeap()); // Allows unlimited amount of nbt
            if (result instanceof CompoundTag compound) {
                cir.setReturnValue(compound);
            } else if (result == null) {
                cir.setReturnValue(null);
            } else {
                cir.setReturnValue(null);
            }
    }
}

