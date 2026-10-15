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

    // SANITIZES NBT, important for certain chunkbans / nbt malforms


    @Shadow
    public abstract Tag readNbt(NbtAccounter sizeTracker);

    @Inject(method = "readNbt()Lnet/minecraft/nbt/CompoundTag;", at = @At("HEAD"), cancellable = true)
    private void onReadNbt(CallbackInfoReturnable<CompoundTag> cir) {
        Tag result;
        try {
            result = this.readNbt(NbtAccounter.create(2_000_000L));
        } catch (Exception e) {
            cir.setReturnValue(new CompoundTag());
            return;
        }
        cir.setReturnValue(result instanceof CompoundTag compound ? compound : new CompoundTag());
    }

    @Inject(method = "readNbt(Lio/netty/buffer/ByteBuf;)Lnet/minecraft/nbt/CompoundTag;", at = @At("HEAD"), cancellable = true)
    private static void safeReadNbt(ByteBuf input, CallbackInfoReturnable<CompoundTag> cir) {
        try {
            Tag tag = FriendlyByteBuf.readNbt(input, NbtAccounter.defaultQuota());
            cir.setReturnValue(tag == null ? null : tag instanceof CompoundTag c ? c : new CompoundTag());
        } catch (RuntimeException e) {
            ChunkPacketState.markBadBlockEntityData();
            input.setZero(input.readerIndex(), input.readableBytes());
            cir.setReturnValue(new CompoundTag());
        }
    }

    @Redirect(method = "readLongArray(Lio/netty/buffer/ByteBuf;)[J", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/VarInt;read(Lio/netty/buffer/ByteBuf;)I"))
    private static int handleHugeLongArray(ByteBuf input) {
        final int size;

        try {
            size = VarInt.read(input);
        } catch (RuntimeException e) {
            ChunkPacketState.markBadLightData();
            input.setZero(input.readerIndex(), input.readableBytes());
            return 0;
        }

        int maxSize = input.readableBytes() / Long.BYTES;

        if (size < 0 || size > maxSize) {
            ChunkPacketState.markBadLightData();
            input.setZero(input.readerIndex(), input.readableBytes());
            return 0;
        }

        return size;
    }

}

