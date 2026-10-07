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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class DeleteCommand implements HyperCommand {
    private final HyperWorldArgumentType loadedWorldArgumentType;
    private final Hyperverse hyperverse;

    @Inject
    DeleteCommand(final Hyperverse hyperverse) {
        this.hyperverse = hyperverse; 
        loadedWorldArgumentType = new HyperWorldArgumentType(hyperverse, HyperWorldArgumentType.HyperWorldState.LOADED_ONLY);
    }
    
    @Override
    public CommandNode<CommandSourceStack> getCommand() {
        return literal("delete")
                .requires(ctx -> ctx.getSender().hasPermission("hyperverse.delete"))
                .then(argument("world", loadedWorldArgumentType)
                        .executes(ctx -> doDelete(ctx, false))
                        .then(argument("remove files", bool())
                                .executes(ctx -> doDelete(ctx, ctx.getArgument("remove files", Boolean.class)))))
                .build();
    }
    
    private int doDelete(CommandContext<CommandSourceStack> ctx, final boolean removeFiles) {
        CommandSender sender = ctx.getSource().getSender();
        HyperWorld world = ctx.getArgument("world", HyperWorld.class);
        Path worldPath = world.getBukkitWorld().getWorldPath();

        world.deleteWorld(worldUnloadResult -> {
            if (worldUnloadResult != HyperWorld.WorldUnloadResult.SUCCESS) {
                MessageUtil.sendMessage(sender, Messages.messageWorldNotRemoved, "%reason%",
                        worldUnloadResult.getDescription()
                );
                return;
            }

            if (removeFiles) {
                try (Stream<Path> walk = Files.walk(worldPath)) {
                    walk.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } catch (final Exception e) {
                    MessageUtil.sendMessage(sender, Messages.messageWorldNotRemoved, "%reason%",
                            e.getMessage()
                    );
                    hyperverse.getSLF4JLogger().error("Failed to delete world files for {}", world.getDisplayName(), e);
                    return;
                }
            }

            MessageUtil.sendMessage(sender, Messages.messageWorldRemoved);
        });

        return Command.SINGLE_SUCCESS;
    }
}
