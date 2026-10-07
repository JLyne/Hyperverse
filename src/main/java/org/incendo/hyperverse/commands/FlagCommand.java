package org.incendo.hyperverse.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.incendo.hyperverse.Hyperverse;
import org.incendo.hyperverse.commands.arguments.HyperWorldArgumentType;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.flags.FlagParseException;
import org.incendo.hyperverse.flags.GlobalWorldFlagContainer;
import org.incendo.hyperverse.flags.WorldFlag;
import org.incendo.hyperverse.flags.implementation.EndFlag;
import org.incendo.hyperverse.flags.implementation.NetherFlag;
import org.incendo.hyperverse.util.MessageUtil;
import org.incendo.hyperverse.world.HyperWorld;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class FlagCommand implements HyperCommand {
    private final GlobalWorldFlagContainer globalFlagContainer;
    private final HyperWorldArgumentType loadedWorldArgumentType;

    @Inject
    FlagCommand(final Hyperverse hyperverse, final GlobalWorldFlagContainer globalFlagContainer) {
        this.globalFlagContainer = globalFlagContainer;
        loadedWorldArgumentType = new HyperWorldArgumentType(hyperverse, HyperWorldArgumentType.HyperWorldState.LOADED_ONLY);
    }

    @Override
    public CommandNode<CommandSourceStack> getCommand() {
        RequiredArgumentBuilder<CommandSourceStack, HyperWorld> setNode = argument("world", loadedWorldArgumentType);
        RequiredArgumentBuilder<CommandSourceStack, HyperWorld> resetNode = argument("world", loadedWorldArgumentType);
        RequiredArgumentBuilder<CommandSourceStack, HyperWorld> removeNode = argument("world", loadedWorldArgumentType);
        RequiredArgumentBuilder<CommandSourceStack, HyperWorld> infoNode = argument("world", loadedWorldArgumentType);

        // Add each flag as a literal so we can specify the correct argument type for the value
        globalFlagContainer.getFlagMap().values().forEach(f -> {
            resetNode.then(literal(f.getName()).executes(this::doFlagReset));
            removeNode.then(literal(f.getName()).executes(this::doFlagRemove));
            infoNode.then(literal(f.getName()).executes(this::doFlagInfo));
            setNode.then(literal(f.getName()).then(argument("value", f.getArgumentType()).executes(this::doFlagSet)));
        });

        return literal("flag")
                .requires(ctx ->
                        ctx.getSender().hasPermission("hyperverse.flag.set")
                                || ctx.getSender().hasPermission("hyperverse.flag.info"))
                .then(literal("set")
                        .requires(ctx -> ctx.getSender().hasPermission("hyperverse.flag.set"))
                        .then(setNode))
                .then(literal("reset")
                        .requires(ctx -> ctx.getSender().hasPermission("hyperverse.flag.set"))
                        .then(resetNode))
                .then(literal("remove")
                        .requires(ctx -> ctx.getSender().hasPermission("hyperverse.flag.set"))
                        .then(removeNode))
                .then(literal("info")
                        .requires(ctx -> ctx.getSender().hasPermission("hyperverse.flag.info"))
                        .then(infoNode))
                .build();
    }

    private <T, F extends WorldFlag<T, F>> int doFlagSet(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        HyperWorld world = ctx.getArgument("world", HyperWorld.class);
        @SuppressWarnings("unchecked") WorldFlag<T, F> flag = ctx.getArgument("flag", WorldFlag.class);
        T value = ctx.getArgument("value", flag.valueClass());

        try {
            world.setFlagInstance(flag.flagOf(value));
            MessageUtil.sendMessage(sender, Messages.messageFlagSet);
        } catch (final FlagParseException e) {
            MessageUtil.sendMessage(
                    sender,
                    Messages.messageFlagParseError,
                    "%flag%",
                    e.getFlag().getName(),
                    "%value%",
                    e.getValue(),
                    "%reason%",
                    e.getErrorMessage()
            );
        }
            
        return Command.SINGLE_SUCCESS;
    }

    private <T> int doFlagReset(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        HyperWorld world = ctx.getArgument("world", HyperWorld.class);
        @SuppressWarnings("unchecked") WorldFlag<T, ?> flag = ctx.getArgument("flag", WorldFlag.class);

        final WorldFlag<?, ?> toSet;
        if (flag.getClass() == EndFlag.class) {
            final World bukkitWorld = world.getBukkitWorld();
            if (bukkitWorld == null) {
                toSet = globalFlagContainer.getFlag(flag.getClass());
            } else {
                WorldFlag<?, ?> temp;
                try {
                    temp = EndFlag.END_FLAG_DEFAULT.parse(bukkitWorld.getName() + "_the_end");
                } catch (FlagParseException ignored) {
                    temp = globalFlagContainer.getFlag(flag.getClass());
                }
                toSet = temp;
            }
        } else if (flag.getClass() == NetherFlag.class) {
            final World bukkitWorld = world.getBukkitWorld();
            if (bukkitWorld == null) {
                toSet = globalFlagContainer.getFlag(flag.getClass());
            } else {
                WorldFlag<?, ?> temp;
                try {
                    temp = NetherFlag.NETHER_FLAG_DEFAULT.parse(bukkitWorld.getName() + "_nether");
                } catch (FlagParseException ignored) {
                    temp = globalFlagContainer.getFlag(flag.getClass());
                }
                toSet = temp;
            }
        } else {
            toSet = globalFlagContainer.getFlag(flag.getClass());
        }

        world.setFlagInstance(toSet);
        MessageUtil.sendMessage(sender, Messages.messageFlagSet);
        return Command.SINGLE_SUCCESS;
    }

    private <T> int doFlagRemove(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        HyperWorld world = ctx.getArgument("world", HyperWorld.class);
        @SuppressWarnings("unchecked") WorldFlag<T, ?> flag = ctx.getArgument("flag", WorldFlag.class);

        world.removeFlag(flag);
        MessageUtil.sendMessage(sender, Messages.messageFlagRemoved);

        return Command.SINGLE_SUCCESS;
    }

    private <T> int doFlagInfo(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        HyperWorld world = ctx.getArgument("world", HyperWorld.class);
        @SuppressWarnings("unchecked") WorldFlag<T, ?> flag = ctx.getArgument("flag", WorldFlag.class);

        final String value = String.valueOf(world.getFlag((Class<? extends WorldFlag<T, ?>>) flag.getClass()));
        final String defaultValue = String.valueOf(this.globalFlagContainer.getFlag(flag.getClass()).getValue());
        MessageUtil.sendMessage(
                sender,
                Messages.messageFlagDisplayInfo,
                "%description%",
                flag.getFlagDescription().toString(),
                "%current%",
                value.isEmpty() ? "Unset" : value,
                "%default%",
                defaultValue.isEmpty() ? "Unset" : defaultValue
        );

        return Command.SINGLE_SUCCESS;
    }
}
