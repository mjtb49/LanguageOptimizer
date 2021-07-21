package io.github.mjtb49.languageoptimizer.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.mjtb49.languageoptimizer.LanguageOptimizer;
import net.minecraft.command.arguments.ItemStackArgumentType;
import net.minecraft.item.Item;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class TargetItemCommand {
    public static void register(CommandDispatcher<ServerCommandSource> sourceCommandDispatcher) {
        sourceCommandDispatcher.register(
            literal("targetItems")
                .then(
                    literal("addDesired").then(
                        argument("item", ItemStackArgumentType.itemStack()).then(
                                argument("weight", integer()).executes( c -> {
                                    Item item =  ItemStackArgumentType.getItemStackArgument(c, "item").getItem();
                                    LanguageOptimizer.DESIRABLE_ITEMS.put(item, getInteger(c, "weight"));
                                    return 1;
                                })
                            )
                        )
                ).then(
                    literal("addRequired").then(
                        argument("item", ItemStackArgumentType.itemStack()).executes( c -> {
                            Item item =  ItemStackArgumentType.getItemStackArgument(c, "item").getItem();
                            LanguageOptimizer.REQUIRED_ITEMS.add(item);
                            return 1;
                        })
                    )
                ).then(
                    literal("removeRequired").then(
                        argument("item", ItemStackArgumentType.itemStack()).executes( c -> {
                            Item item =  ItemStackArgumentType.getItemStackArgument(c, "item").getItem();
                            LanguageOptimizer.REQUIRED_ITEMS.remove(item);
                            return 1;
                        })
                    )
                ).then(
                    literal("removeDesired").then(
                        argument("item", ItemStackArgumentType.itemStack()).executes( c -> {
                            Item item =  ItemStackArgumentType.getItemStackArgument(c, "item").getItem();
                            LanguageOptimizer.DESIRABLE_ITEMS.remove(item);
                            return 1;
                        })
                    )
                ).then(
                        literal("list").executes( c -> {
                                    for (Item item : LanguageOptimizer.REQUIRED_ITEMS) {
                                        c.getSource().getPlayer().sendMessage(new LiteralText("Required " + item.getName().getString()), false);
                                    }
                                    for (Item item : LanguageOptimizer.DESIRABLE_ITEMS.keySet()) {
                                        c.getSource().getPlayer().sendMessage(new LiteralText("Desired " + item.getName().getString() + " " + LanguageOptimizer.DESIRABLE_ITEMS.get(item)), false);
                                    }
                                    return 1;
                                }
                        )
                )
            );
    }
}
