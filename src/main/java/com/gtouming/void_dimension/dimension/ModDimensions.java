package com.gtouming.void_dimension.dimension;

import com.gtouming.void_dimension.VoidDimension;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDimensions {
    public static final DeferredRegister<DimensionType> DIMENSION_TYPES = DeferredRegister.create(Registries.DIMENSION_TYPE, VoidDimension.MOD_ID);

    public static final DeferredRegister<MapCodec<? extends ChunkGenerator>> CHUNK_GENERATORS = DeferredRegister.create(Registries.CHUNK_GENERATOR, VoidDimension.MOD_ID);

    public static boolean PlayerInVoidDimension(Player player) {
        return player.level().dimension() == VoidDimensionType.VOID_DIMENSION;
    }

    public static void register(IEventBus modEventBus) {
        DIMENSION_TYPES.register("void_dimension", VoidDimensionType::createDimensionType);
        DIMENSION_TYPES.register(modEventBus);
        CHUNK_GENERATORS.register("void_generator", () -> VoidDimensionChunkGenerator.CODEC);
        CHUNK_GENERATORS.register(modEventBus);
    }
}
