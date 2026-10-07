package org.incendo.hyperverse.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.incendo.hyperverse.Hyperverse;

import static io.papermc.paper.command.brigadier.Commands.literal;

public class ReloadCommand implements HyperCommand {
    @Override
    public CommandNode<CommandSourceStack> getCommand() {
        return literal("reload")
                .requires(ctx -> ctx.getSender().hasPermission("hyperverse.reload"))
                .executes(this::doConfigReload)
                .build();
    }

    private int doConfigReload(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSender sender = ctx.getSource().getSender();
        if (!Hyperverse.getPlugin(Hyperverse.class).reloadConfiguration(sender)) {
            throw new SimpleCommandExceptionType(new LiteralMessage("Failed to reload configuration files")).create();
        }

        return Command.SINGLE_SUCCESS;
    }
}
