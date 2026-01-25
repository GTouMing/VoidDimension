package com.gtouming.void_dimension.dimension;

import com.gtouming.void_dimension.VoidDimension;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModBiomeModifiers {
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, VoidDimension.MOD_ID);

    public static final MapCodec<VDBiomeModifier> VOID_SPAWN_MODIFIER = MapCodec.unit(VDBiomeModifier::new);

    static {
        BIOME_MODIFIER_SERIALIZERS.register("void_spawn_modifier", () -> VOID_SPAWN_MODIFIER);
    }
}