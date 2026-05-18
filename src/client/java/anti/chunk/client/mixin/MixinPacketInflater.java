package anti.chunk.client.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.CompressionDecoder;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.zip.Inflater;

@Mixin(CompressionDecoder.class)
public class MixinPacketInflater {

    @Shadow
    private Inflater inflater;

    @Inject(method = "decode", at = @At("HEAD"), cancellable = true)
    private void onDecode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out, CallbackInfo ci) throws Exception {
            ci.cancel();

            if (buf.readableBytes() != 0) {
                FriendlyByteBuf packetBuf = new FriendlyByteBuf(buf);
                int uncompressedSize = packetBuf.readVarInt();

                if (uncompressedSize == 0) {
                    out.add(packetBuf.readBytes(packetBuf.readableBytes()));
                } else {
                    if (uncompressedSize > 500_000_000) { // 500 MB, protects your pc from getting OOM (out of memory) error
                        throw new DecoderException("Badly compressed packet - size of " + uncompressedSize / 1_000_000 + "MB is too large");
                    }

                    byte[] compressedData = new byte[packetBuf.readableBytes()];
                    packetBuf.readBytes(compressedData);

                    this.inflater.setInput(compressedData);
                    byte[] decompressed = new byte[uncompressedSize];
                    this.inflater.inflate(decompressed);

                    out.add(Unpooled.wrappedBuffer(decompressed));

                    this.inflater.reset();
                }
        }
    }
}
