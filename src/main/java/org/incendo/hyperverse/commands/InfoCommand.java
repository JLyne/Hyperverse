package org.incendo.hyperverse.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.incendo.hyperverse.Hyperverse;
import org.incendo.hyperverse.commands.arguments.HyperWorldArgumentType;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.util.MessageUtil;
import org.incendo.hyperverse.world.HyperWorld;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class InfoCommand implements HyperCommand {
    private final HyperWorldArgumentType worldArgumentType;
    
    @Inject
    InfoCommand(final Hyperverse hyperverse) {
        worldArgumentType = new HyperWorldArgumentType(hyperverse);
    }
    
    @Override
    public CommandNode<CommandSourceStack> getCommand() {
        return literal("info")
                .requires(ctx -> ctx.getSender().hasPermission("hyperverse.info"))
                .then(argument("world", worldArgumentType)
                        .executes(this::doInfo))
                .build();
    }

    private int doInfo(final CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        HyperWorld world = ctx.getArgument("world", HyperWorld.class);

        MessageUtil.sendMessage(sender, Messages.messageInfoHeader);
        world.sendWorldInfo(sender);

        return Command.SINGLE_SUCCESS;
    }
}
