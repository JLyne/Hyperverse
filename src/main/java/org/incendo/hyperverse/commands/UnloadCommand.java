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

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class UnloadCommand implements HyperCommand {
    private final HyperWorldArgumentType loadedWorldArgumentType;
    
    @Inject
    UnloadCommand(final Hyperverse hyperverse) {
        loadedWorldArgumentType = new HyperWorldArgumentType(hyperverse, HyperWorldArgumentType.HyperWorldState.LOADED_ONLY);
    }
    
    @Override
    public CommandNode<CommandSourceStack> getCommand() {
        return literal("unload")
                .requires(ctx -> ctx.getSender().hasPermission("hyperverse.unload"))
                .then(argument("world", loadedWorldArgumentType)
                        .executes(ctx -> doUnload(ctx, true))
                        .then(argument("save", bool())
                                .executes(ctx -> doUnload(ctx, ctx.getArgument("save", Boolean.class)))))
                .build();
    }

    private int doUnload(final CommandContext<CommandSourceStack> ctx, final boolean saveWorld) {
        CommandSender sender = ctx.getSource().getSender();
        HyperWorld world = ctx.getArgument("world", HyperWorld.class);

        if (!world.isLoaded()) {
            MessageUtil.sendMessage(sender, Messages.messageWorldNotLoaded);
            return Command.SINGLE_SUCCESS;
        }

        final HyperWorld.WorldUnloadResult worldUnloadResult = world.unloadWorld(saveWorld);
        if (worldUnloadResult == HyperWorld.WorldUnloadResult.SUCCESS) {
            MessageUtil.sendMessage(sender, Messages.messageWorldUnloaded);
        } else {
            MessageUtil.sendMessage(sender, Messages.messageWorldUnloadFailed,
                    "%reason%", worldUnloadResult.getDescription()
            );
        }

        return Command.SINGLE_SUCCESS;
    }
}
