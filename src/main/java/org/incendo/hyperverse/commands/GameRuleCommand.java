package org.incendo.hyperverse.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.GameRule;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftGameRule;
import org.incendo.hyperverse.Hyperverse;
import org.incendo.hyperverse.commands.arguments.HyperWorldArgumentType;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.util.MessageUtil;
import org.incendo.hyperverse.world.HyperWorld;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class GameRuleCommand implements HyperCommand {
    private final HyperWorldArgumentType loadedWorldArgumentType;

    @Inject
    GameRuleCommand(final Hyperverse hyperverse) {
        loadedWorldArgumentType = new HyperWorldArgumentType(hyperverse, HyperWorldArgumentType.HyperWorldState.LOADED_ONLY);
    }
    
    @Override
    public CommandNode<CommandSourceStack> getCommand() {
        RequiredArgumentBuilder<CommandSourceStack, HyperWorld> setNode = argument("world", loadedWorldArgumentType);
        RequiredArgumentBuilder<CommandSourceStack, HyperWorld> resetNode = argument("world", loadedWorldArgumentType);

        // Add each gamerule as a literal so we can specify the correct argument type for the value
        RegistryAccess.registryAccess().getRegistry(RegistryKey.GAME_RULE).stream().forEach(gr -> {
            resetNode.then(literal(gr.key().asString())
                            .executes(ctx -> doGameRuleReset(ctx, gr)))
                    .then(literal(gr.key().value())
                            .executes(ctx -> doGameRuleReset(ctx, gr)));

            setNode.then(literal(gr.key().asString())
                    .then(argument("value", CraftGameRule.bukkitToMinecraft(gr).argument())
                            .executes(ctx -> doGameRuleSet(ctx, gr)))
                    .then(literal(gr.key().value())
                            .then(argument("value", CraftGameRule.bukkitToMinecraft(gr).argument())
                                    .executes(ctx -> doGameRuleSet(ctx, gr)))));
        });

        return literal("gamerule")
                .requires(ctx -> ctx.getSender().hasPermission("hyperverse.gamerule.set"))
                .then(literal("set").then(setNode))
                .then(literal("reset").then(resetNode))
                .build();
    }

    private <T> int doGameRuleSet(final CommandContext<CommandSourceStack> ctx, final GameRule<T> gameRule) {
        CommandSender sender = ctx.getSource().getSender();
        HyperWorld world = ctx.getArgument("world", HyperWorld.class);
        T value = ctx.getArgument("value", gameRule.getType());

        if (!world.isLoaded()) {
            MessageUtil.sendMessage(sender, Messages.messageWorldNotLoaded);
            return Command.SINGLE_SUCCESS;
        }

        world.getBukkitWorld().setGameRule(gameRule, value);
        MessageUtil.sendMessage(sender, Messages.messageGameRuleSet);

        return Command.SINGLE_SUCCESS;
    }

    private <T> int doGameRuleReset(final CommandContext<CommandSourceStack> ctx, final GameRule<T> gameRule) {
        CommandSender sender = ctx.getSource().getSender();
        HyperWorld world = ctx.getArgument("world", HyperWorld.class);

        if (!world.isLoaded()) {
            MessageUtil.sendMessage(sender, Messages.messageWorldNotLoaded);
            return Command.SINGLE_SUCCESS;
        }

        world.getBukkitWorld().setGameRule(gameRule, gameRule.getDefaultValue());
        MessageUtil.sendMessage(sender, Messages.messageGameRuleRemoved);
        return Command.SINGLE_SUCCESS;
    }
}
