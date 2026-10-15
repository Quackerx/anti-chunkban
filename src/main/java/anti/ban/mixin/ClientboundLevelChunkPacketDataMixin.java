package anti.ban.mixin;

import anti.ban.ChunkPacketState;
import anti.ban.Saver;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
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
import java.util.Map;

@Mixin(ClientboundLevelChunkPacketData.class)
public class ClientboundLevelChunkPacketDataMixin {

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/codec/ByteBufCodecs;byteArray(I)Lnet/minecraft/network/codec/StreamCodec;"), index = 0)
    private static int increaseChunkDataLimit(int original) {
        return 16 * 1024 * 1024;
    }

    @Shadow
    @Final
    @Mutable
    private List<?> blockEntitiesData;

    @Shadow
    @Final
    private byte[] buffer;

    @Inject(method = "<init>(Ljava/util/Map;[BLjava/util/List;)V", at = @At("RETURN"))
    private void validateChunkBuffer(Map<?, ?> heightmaps, byte[] buffer, List<?> blockEntitiesData, CallbackInfo ci) {
        if (!trySectionParse()) {
            ChunkPacketState.markBadBlockEntityData();
            ChunkPacketState.markBadLightData();
            this.blockEntitiesData = List.of();
        }
    }

    @Unique
    private boolean trySectionParse() {
        ClientLevel level = Minecraft.getInstance().level;

        if (level == null) {
            return false;
        }

        RegistryAccess registryAccess = level.registryAccess();
        int sectionCount = level.getSectionsCount();

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(this.buffer));

        try {
            Strategy<BlockState> blockStrategy = Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY);

            Strategy<Holder<Biome>> biomeStrategy = Strategy.createForBiomes(
                    registryAccess.lookupOrThrow(Registries.BIOME).asHolderIdMap());

            Holder<Biome> defaultBiome = registryAccess.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);

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

