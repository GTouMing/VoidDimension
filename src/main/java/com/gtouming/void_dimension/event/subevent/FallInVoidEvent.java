package com.gtouming.void_dimension.event.subevent;

import com.gtouming.void_dimension.dimension.ModDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;

import static com.gtouming.void_dimension.config.VoidDimensionConfig.enableFallVoid;

public class FallInVoidEvent {
    private static final Map<Player, Boolean> playerHasFallDamage = new HashMap<>();
    public static void onFallInVoid(PlayerTickEvent.Pre event) {
        if (!enableFallVoid) return;

        Player player = event.getEntity();

        if (!(player.level() instanceof ServerLevel level)) return;
        
        if (!ModDimensions.PlayerInVoidDimension(player)) return;
        if (player.getY() < -64) {
            ((ServerPlayer) player).teleportTo(level, player.getX(), 320, player.getZ(), player.getYRot(), player.getXRot());
            playerHasFallDamage.put(player, false);
        }
        if (!playerHasFallDamage.getOrDefault(player, true)) {
            player.fallDistance = 0;
        }
        if (player.onGround()) {
            playerHasFallDamage.put(player, true);
        }
    }
}