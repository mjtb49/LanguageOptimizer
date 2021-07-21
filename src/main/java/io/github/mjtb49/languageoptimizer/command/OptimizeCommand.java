package io.github.mjtb49.languageoptimizer.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.mjtb49.languageoptimizer.LanguageOptimizer;
import net.minecraft.server.command.ServerCommandSource;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class OptimizeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> sourceCommandDispatcher) {
        sourceCommandDispatcher.register(
                literal("optimize").then(
                        argument("minLength",integer()).then(
                                argument("maxLength", integer()).executes( c -> {
                                    LanguageOptimizer.MIN_LENGTH = getInteger(c,"minLength");
                                    LanguageOptimizer.MAX_LENGTH = getInteger(c,"maxLength");
                                    LanguageOptimizer.HAS_FIRED = false;
                                    return 1;
                                })
                        )
                )
        );
    }
}
