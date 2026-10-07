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
import org.incendo.hyperverse.exception.HyperWorldValidationException;
import org.incendo.hyperverse.util.MessageUtil;
import org.incendo.hyperverse.world.HyperWorld;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class LoadCommand implements HyperCommand {
    private final HyperWorldArgumentType unloadedWorldArgumentType;
    
    @Inject
    LoadCommand(final Hyperverse hyperverse) {
        unloadedWorldArgumentType = new HyperWorldArgumentType(hyperverse, HyperWorldArgumentType.HyperWorldState.UNLOADED_ONLY);
    }

    @Override
    public CommandNode<CommandSourceStack> getCommand() {
        return literal("load")
                .requires(ctx -> ctx.getSender().hasPermission("hyperverse.load"))
                .then(argument("world", unloadedWorldArgumentType)
                        .executes(this::doLoad))
                .build();
    }

    private int doLoad(final CommandContext<CommandSourceStack> ctx) {
        CommandSender sender = ctx.getSource().getSender();
        HyperWorld world = ctx.getArgument("world", HyperWorld.class);

        if (world.isLoaded()) {
            MessageUtil.sendMessage(sender, Messages.messageWorldAlreadyLoaded);
            return Command.SINGLE_SUCCESS;
        }

        try {
            world.createBukkitWorld();
        } catch (final HyperWorldValidationException e) {
            MessageUtil.sendMessage(sender, Messages.messageWorldImportFailure,
                    "%world%", world.getConfiguration().getName(), "%result%", e.getMessage()
            );
            return Command.SINGLE_SUCCESS;
        }

        world.getConfiguration().setLoaded(true);
        world.saveConfiguration();

        MessageUtil.sendMessage(sender, Messages.messageWorldLoadedSuccessfully);
        return Command.SINGLE_SUCCESS;
    }
}
