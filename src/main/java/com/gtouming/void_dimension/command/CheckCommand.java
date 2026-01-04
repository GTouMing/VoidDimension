package com.gtouming.void_dimension.command;

import com.gtouming.void_dimension.DimensionData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class CheckCommand {
    
    /**
     * 注册/check命令
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("check")
            .requires(source -> source.hasPermission(0)) // 基础权限要求
            .then(Commands.literal("totalPowerLevel")
                .requires(source -> source.hasPermission(2)) // 需要管理员权限
                .executes(CheckCommand::checkTotalPowerLevel))
            .then(Commands.literal("anchorList")
                .requires(source -> source.hasPermission(2)) // 需要管理员权限
                .executes(CheckCommand::checkAnchorList))
            .then(Commands.literal("respawnPoint")
                .executes(context -> checkRespawnPoint(context, null))
                .then(Commands.argument("player", EntityArgument.player())
                    .requires(source -> source.hasPermission(2)) // 需要管理员权限查看其他玩家
                    .executes(context -> checkRespawnPoint(context, EntityArgument.getPlayer(context, "player")))))
        );
    }
    
    /**
     * 查看总能量等级
     */
    private static int checkTotalPowerLevel(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        
        DimensionData data = DimensionData.getData(level);
        int totalPowerLevel = data.getTotalPowerLevel();
        
        source.sendSuccess(() -> Component.literal("虚空维度总能量等级: " + totalPowerLevel), false);
        return 1;
    }
    
    /**
     * 查看锚点列表
     */
    private static int checkAnchorList(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        
        DimensionData data = DimensionData.getData(level);
        var anchorPosList = data.getAnchorPosList();
        
        if (anchorPosList.isEmpty()) {
            source.sendSuccess(() -> Component.literal("虚空维度中没有锚点"), false);
        } else {
            source.sendSuccess(() -> Component.literal("虚空维度锚点列表 (" + anchorPosList.size() + " 个):"), false);
            for (int i = 0; i < anchorPosList.size(); i++) {
                var pos = anchorPosList.get(i);
                final int index = i;
                source.sendSuccess(() -> Component.literal("  " + (index + 1) + ". " + pos.toShortString()), false);
            }
        }
        return 1;
    }
    
    /**
     * 统一检查重生点方法
     */
    private static int checkRespawnPoint(CommandContext<CommandSourceStack> context, ServerPlayer targetPlayer) {
        CommandSourceStack source = context.getSource();
        
        // 如果没有指定玩家，则检查执行命令的玩家
        if (targetPlayer == null) {
            if (!(source.getEntity() instanceof ServerPlayer)) {
                source.sendFailure(Component.literal("只有玩家可以执行此命令"));
                return 0;
            }
            targetPlayer = (ServerPlayer) source.getEntity();
        }
        
        BlockPos respawnPos = targetPlayer.getRespawnPosition();
        Component respawnInfo;
        if (respawnPos != null) {
            respawnInfo = Component.literal("§a当前重生点: " +
                    respawnPos.getX() + ", " +
                    respawnPos.getY() + ", " +
                    respawnPos.getZ());
        } else {
            respawnInfo = Component.literal("§c未设置重生点");
        }
        
        // 根据是否查看自己来显示不同的消息
        if (targetPlayer == source.getEntity()) {
            source.sendSuccess(() -> Component.literal("你的重生点信息: ").append(respawnInfo), false);
        } else {
            ServerPlayer finalTargetPlayer = targetPlayer;
            source.sendSuccess(() -> Component.literal("玩家 " + finalTargetPlayer.getScoreboardName() + " 的重生点信息: ").append(respawnInfo), false);
        }
        return 1;
    }
}