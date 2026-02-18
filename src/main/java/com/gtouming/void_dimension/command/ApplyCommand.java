package com.gtouming.void_dimension.command;

import com.gtouming.void_dimension.data.VoidDimensionData;
import com.gtouming.void_dimension.dimension.VoidDimensionType;
import com.gtouming.void_dimension.util.DimRuleInvoker;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ApplyCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("apply")
                .then(Commands.literal("weather")
                        .then(Commands.literal("clear")
                                .executes(context -> applyWeather(context.getSource(), "clear")))
                        .then(Commands.literal("rain")
                                .executes(context -> applyWeather(context.getSource(), "rain")))
                        .then(Commands.literal("thunder")
                                .executes(context -> applyWeather(context.getSource(), "thunder")))
                        )
                    .then(Commands.literal("anchorlist")
                        .    then(Commands.literal("clear")
                                .executes(context -> applyAnchorList(context.getSource(), "clear"))))
        );

    }

    private static int applyWeather(CommandSourceStack source, String weather) {
        var voidDimension = source.getLevel().getServer().getLevel(VoidDimensionType.VOID_DIMENSION);
        if (voidDimension == null) {
            source.sendFailure(Component.literal("Void dimension not found"));
            return 0;
        }
        switch (weather) {
            case "clear":
                DimRuleInvoker.setVDWeatherClear(voidDimension, -1);
                source.sendSuccess(() -> Component.literal("Weather cleared"), true);
                break;
            case "rain":
                DimRuleInvoker.setVDWeatherRain(voidDimension, -1);
                source.sendSuccess(() -> Component.literal("Weather set to rain"), true);
                break;
            case "thunder":
                DimRuleInvoker.setVDWeatherThunder(voidDimension, -1);
                source.sendSuccess(() -> Component.literal("Weather set to thunder"), true);
                break;
        }
        return 1;
    }

    private static int applyAnchorList(CommandSourceStack source, String action) {
        switch (action) {
            case "clear":
                VoidDimensionData.getAnchorList(source.getLevel()).clear();
                source.sendSuccess(() -> Component.literal("Anchor list cleared"), true);
                break;
        }
        return 1;
    }
}
