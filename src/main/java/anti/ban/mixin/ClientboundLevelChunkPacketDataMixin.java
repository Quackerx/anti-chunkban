package anti.ban.mixin;

import anti.ban.ChunkPacketState;
import anti.ban.Saver;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.Strategy;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ClientboundLevelChunkPacketData.class)
public class ClientboundLevelChunkPacketDataMixin {

    // SANITIZES level chunk data JUST in case


    @ModifyConstant(method = "<init>(Lnet/minecraft/network/RegistryFriendlyByteBuf;II)V", constant = @Constant(intValue = 2097152))
    private static int increaseChunkDataLimit(int original) {
        if (!Saver.chunkban) {return original;}

        return 16 * 1024 * 1024;
    }

    @Redirect(method = "<init>(Lnet/minecraft/network/RegistryFriendlyByteBuf;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/RegistryFriendlyByteBuf;readVarInt()I"))
    private int sanitizeChunkSize(RegistryFriendlyByteBuf input) {
        int size = input.readVarInt();
        if (!Saver.chunkban) return size;

        int available = input.readableBytes();
        if (size < 0 || size > available) {
            ChunkPacketState.markBadBlockEntityData();
            return 0;
        }
        return size;
    }

    @Shadow
    @Final
    @Mutable
    private List<?> blockEntitiesData;

    @Shadow
    @Final
    private byte[] buffer;

    @Inject(method = "<init>(Lnet/minecraft/network/RegistryFriendlyByteBuf;II)V", at = @At("RETURN"))
    private void validateChunkBuffer(RegistryFriendlyByteBuf input, int x, int z, CallbackInfo ci) {
        if (!Saver.chunkban) {
            return;
        }

        if (!trySectionParse(input)) {

            ChunkPacketState.markBadBlockEntityData();
            ChunkPacketState.markBadLightData();
            this.blockEntitiesData = List.of();
        }
    }

    @Unique
    private boolean trySectionParse(RegistryFriendlyByteBuf input) {
        ClientLevel level = Minecraft.getInstance().level;

        if (level == null) {
            return false;
        }

        int sectionCount = level.getSectionsCount();

        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(this.buffer), input.registryAccess());

        try {
            Strategy<BlockState> blockStrategy = Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY);

            Strategy<Holder<Biome>> biomeStrategy = Strategy.createForBiomes(input.registryAccess().lookupOrThrow(Registries.BIOME).asHolderIdMap());

            Holder<Biome> defaultBiome = input.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);

            for (int i = 0; i < sectionCount; i++) {
                PalettedContainer<BlockState> states = new PalettedContainer<>(Blocks.AIR.defaultBlockState(), blockStrategy);

                PalettedContainer<Holder<Biome>> biomes = new PalettedContainer<>(defaultBiome, biomeStrategy);

                LevelChunkSection section = new LevelChunkSection(states, biomes);

                section.read(buf);
            }

            return buf.readerIndex() == buf.writerIndex();

        } catch (Throwable t) {
            return false;
        } finally {
            buf.release();
        }
    }
}

