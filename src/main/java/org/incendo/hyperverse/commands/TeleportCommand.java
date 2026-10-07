package org.incendo.hyperverse.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.hyperverse.Hyperverse;
import org.incendo.hyperverse.commands.arguments.HyperWorldArgumentType;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.util.MessageUtil;
import org.incendo.hyperverse.world.HyperWorld;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class TeleportCommand implements HyperCommand {

    private final HyperWorldArgumentType loadedWorldArgumentType;

    @Inject
    TeleportCommand(final Hyperverse hyperverse) {
        loadedWorldArgumentType = new HyperWorldArgumentType(hyperverse, HyperWorldArgumentType.HyperWorldState.LOADED_ONLY);
    }
    
    @Override
    public CommandNode<CommandSourceStack> getCommand() {
        return literal("teleport")
                .requires(ctx -> ctx.getSender().hasPermission("hyperverse.teleport"))
                .then(argument("world", loadedWorldArgumentType)
                        .executes(this::doTeleport)
                        .then(argument("players", ArgumentTypes.players())
                                .requires(ctx -> ctx.getSender().hasPermission("hyperverse.teleport.other"))
                                .executes(this::doTeleportOther)))
                .build();
    }

    private int doTeleport(CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        HyperWorld world = ctx.getArgument("world", HyperWorld.class);

        if (!world.isLoaded()) {
            MessageUtil.sendMessage(sender, Messages.messageWorldNotLoaded);
            return Command.SINGLE_SUCCESS;
        }

        if (!(sender instanceof Player player)) {
            MessageUtil.sendMessage(sender, Messages.messageSpecifyPlayer);
            return Command.SINGLE_SUCCESS;
        }

        if (world.getBukkitWorld() == player.getWorld()) {
            MessageUtil.sendMessage(player, Messages.messageAlreadyInWorld);
            return Command.SINGLE_SUCCESS;
        }

        MessageUtil.sendMessage(player, Messages.messageTeleporting, "%world%", world.getDisplayName());
        world.teleportPlayer(player);
        return Command.SINGLE_SUCCESS;
    }

    private int doTeleportOther(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSender sender = ctx.getSource().getSender();
        HyperWorld world = ctx.getArgument("world", HyperWorld.class);
        List<Player> players = ctx.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());

        if (!world.isLoaded()) {
            MessageUtil.sendMessage(sender, Messages.messageWorldNotLoaded);
            return Command.SINGLE_SUCCESS;
        }

        for (final Player player : players) {
            if (world.getBukkitWorld() == player.getWorld()) {
                MessageUtil.sendMessage(sender, Messages.messagePlayerAlreadyInWorld, "%player%",
                        player.getName()
                );
                continue;
            }
            MessageUtil.sendMessage(player, Messages.messageTeleporting, "%world%", world.getDisplayName());
            if (player != sender) {
                MessageUtil.sendMessage(sender, Messages.messageTeleportingPlayer, "%player%",
                        player.getName(), "%world%", world.getDisplayName()
                );
            }
            world.teleportPlayer(player);
        }

        return Command.SINGLE_SUCCESS;
    }
}
