package org.incendo.hyperverse.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.util.MessageUtil;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class FindCommand implements HyperCommand {
    @Override
    public CommandNode<CommandSourceStack> getCommand() {
        return literal("find")
                .requires(ctx -> ctx.getSender().hasPermission("hyperverse.find"))
                .then(argument("player", ArgumentTypes.player())
                        .executes(this::doFind))
                .build();
    }

    @Override
    public List<String> getAliases() {
        return List.of("where");
    }

    @Override
    public List<String> getRootAliases() {
        return List.of("hvf", "hvfind");
    }

    @Override
    public @Nullable String getDescription() {
        return Messages.commandDescriptionFind.toString();
    }

    private int doFind(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSender sender = ctx.getSource().getSender();
        Player player = (Player) ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());

        final Location location = player.getLocation();
        final DecimalFormat format = Messages.miscCoordinateDecimalFormat;
        MessageUtil
                .sendMessage(sender, Messages.messagePlayerCurrentWorld, "%player%", player.getName(), "%world%",
                        player.getWorld().getName(), "%x%",
                        format.format(location.getX()), "%y%", format.format(location.getY()),
                        "%z%", format.format(location.getZ())
                );

        return Command.SINGLE_SUCCESS;
    }
}
