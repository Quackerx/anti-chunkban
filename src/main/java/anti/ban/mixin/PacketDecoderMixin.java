package anti.ban.mixin;

import anti.ban.Saver;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(PacketDecoder.class)
public class PacketDecoderMixin<T extends PacketListener> {


    // FALLBACK (or not rly but the last layer)
    @Inject(method = "decode", at = @At(value = "INVOKE", target = "Lio/netty/buffer/ByteBuf;readableBytes()I", ordinal = 2), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void saver$salvageAbnormalChunk(ChannelHandlerContext ctx, ByteBuf input, List<Object> out, CallbackInfo ci, int readableBytes, Packet<? super T> packet, PacketType<? extends Packet<? super T>> packetId) {
        if (!Saver.chunkban) {
            return;
        }

        if (packet instanceof ClientboundLevelChunkWithLightPacket && input.readableBytes() > 0) {
            int extra = input.readableBytes();
            input.skipBytes(extra);
        }
    }
}
