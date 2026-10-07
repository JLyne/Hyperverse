package org.incendo.hyperverse.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.hyperverse.Hyperverse;
import org.incendo.hyperverse.commands.arguments.HyperWorldArgumentType;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.util.MessageUtil;
import org.incendo.hyperverse.world.HyperWorld;
import org.incendo.hyperverse.world.WorldManager;

import java.text.DecimalFormat;
import java.util.StringJoiner;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class WhoCommand implements HyperCommand {
    private final PlainTextComponentSerializer plain = PlainTextComponentSerializer.plainText();
    private final WorldManager worldManager;
    private final HyperWorldArgumentType loadedWorldArgumentType;

    @Inject
    WhoCommand(final Hyperverse hyperverse, final WorldManager worldManager) {
        this.worldManager = worldManager;
        loadedWorldArgumentType = new HyperWorldArgumentType(hyperverse, HyperWorldArgumentType.HyperWorldState.LOADED_ONLY);
    }
    
    @Override
    public CommandNode<CommandSourceStack> getCommand() {
        return literal("who")
                .requires(ctx -> ctx.getSender().hasPermission("hyperverse.who"))
                .executes(this::doWho)
                .then(argument("world", loadedWorldArgumentType)
                        .executes(ctx -> doWho(ctx, ctx.getArgument("world", HyperWorld.class))))
                .build();
    }

    private int doWho(CommandContext<CommandSourceStack> ctx) {
        for (final HyperWorld world : worldManager.getWorlds()) {
            if (world.isLoaded()) {
                doWho(ctx, world);
            }
        }

        return Command.SINGLE_SUCCESS;
    }
    
    private int doWho(CommandContext<CommandSourceStack> ctx, HyperWorld world) {
        CommandSender sender = ctx.getSource().getSender();

        if (!world.isLoaded()) {
            MessageUtil.sendMessage(sender, Messages.messageWorldNotLoaded);
            return Command.SINGLE_SUCCESS;
        }

        World bukkitWorld = world.getBukkitWorld();
        final DecimalFormat format = Messages.miscCoordinateDecimalFormat;
        if (bukkitWorld.getPlayers().isEmpty()) {
            MessageUtil.sendMessage(sender, Messages.messageNoPlayersInWorld, "%world%", world.getDisplayName());
            return Command.SINGLE_SUCCESS;
        }

        final StringJoiner players = new StringJoiner(", ");
        for (final Player player : bukkitWorld.getPlayers()) {
            final Location location = player.getLocation();
            players.add(MessageUtil
                    .format(Messages.messageListEntryPlayer.toString(), "%player%",
                            plain.serialize(player.displayName()), "%world%", world.getDisplayName(), "%x%",
                            format.format(location.getX()), "%y%", format.format(location.getY()),
                            "%z%", format.format(location.getZ())
                    ));
        }
        MessageUtil.sendMessage(sender, Messages.messageListEntryWorld, "%players%",
                players.toString().trim(), "%world%", world.getDisplayName()
        );

        return Command.SINGLE_SUCCESS;
    }
}
