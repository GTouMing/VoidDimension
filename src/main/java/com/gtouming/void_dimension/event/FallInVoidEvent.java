package com.gtouming.void_dimension.event;

import com.gtouming.void_dimension.dimension.ModDimensions;
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

        if (ModDimensions.PlayerInVoidDimension(player)) {
            if (player.getY() < -64) {
                player.setPos(player.getX(), 320, player.getZ());
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
}

