package anti.ban.mixin;

import anti.ban.ChunkPacketState;
import anti.ban.Saver;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.BitSet;

@Mixin(FriendlyByteBuf.class)
public abstract class PacketFriendlyByteBufMixin {
    @Shadow
    public abstract Tag readNbt(NbtAccounter sizeTracker);

    @Inject(method = "readNbt()Lnet/minecraft/nbt/CompoundTag;", at = @At("HEAD"), cancellable = true)
    private void onReadNbt(CallbackInfoReturnable<CompoundTag> cir) {
        if (!Saver.chunkban) {return;}


        Tag result;
        try {
            result = this.readNbt(NbtAccounter.create(2_000_000L));
        } catch (Exception e) {
            cir.setReturnValue(new CompoundTag());
            return;
        }
        cir.setReturnValue(result instanceof CompoundTag compound ? compound : new CompoundTag());
    }

    @Redirect(method = "readLongArray(Lio/netty/buffer/ByteBuf;)[J", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/VarInt;read(Lio/netty/buffer/ByteBuf;)I"))
    private static int duck$handleHugeLongArray(ByteBuf input) {
        int size = VarInt.read(input);
        int maxSize = input.readableBytes() / 8;

        if (!Saver.chunkban) {return size;}

        if (size > maxSize) {
            ChunkPacketState.markBadLightData();
            input.setZero(input.readerIndex(), input.readableBytes());
            return 0;
        }

        return size;
    }
}

