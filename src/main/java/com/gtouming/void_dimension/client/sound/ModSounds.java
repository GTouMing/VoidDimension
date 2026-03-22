package com.gtouming.void_dimension.client.sound;

import com.gtouming.void_dimension.VoidDimension;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, VoidDimension.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> GUI_OPEN = registerSoundEvent("gui_open");

    public static final DeferredHolder<SoundEvent, SoundEvent> BUTTON_RELEASE = registerSoundEvent("button_release");

    // 注册第二个声音事件
    public static final DeferredHolder<SoundEvent, SoundEvent> SLIDER_ADJUST = registerSoundEvent("slider_adjust");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(VoidDimension.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
