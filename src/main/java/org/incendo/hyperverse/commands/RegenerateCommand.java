package org.incendo.hyperverse.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.hyperverse.Hyperverse;
import org.incendo.hyperverse.commands.arguments.HyperWorldArgumentType;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.exception.HyperWorldValidationException;
import org.incendo.hyperverse.modules.HyperWorldFactory;
import org.incendo.hyperverse.util.MessageUtil;
import org.incendo.hyperverse.util.SeedUtil;
import org.incendo.hyperverse.world.HyperWorld;
import org.incendo.hyperverse.world.WorldConfiguration;
import org.incendo.hyperverse.world.WorldManager;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class RegenerateCommand implements HyperCommand {
    private final WorldManager worldManager;
    private final HyperWorldFactory hyperWorldFactory;
    private final HyperWorldArgumentType worldArgumentType;

    @Inject
    RegenerateCommand(final Hyperverse hyperverse, final WorldManager worldManager, final HyperWorldFactory hyperWorldFactory) {
        this.worldManager = worldManager;
        this.hyperWorldFactory = hyperWorldFactory;
        worldArgumentType = new HyperWorldArgumentType(hyperverse);
    }
    
    @Override
    public CommandNode<CommandSourceStack> getCommand() {
        return literal("regenerate")
                .requires(ctx -> ctx.getSender().hasPermission("hyperverse.regenerate"))
                .then(argument("world", worldArgumentType)
                        .executes(ctx -> this.doRegenerate(ctx, false))
                        .then(argument("random seed", BoolArgumentType.bool())
                                .executes(ctx -> 
                                        this.doRegenerate(ctx, ctx.getArgument("random seed", Boolean.class)))))
                .build();
    }

    @Override
    public List<String> getAliases() {
        return List.of("regen");
    }

    @Override
    public @Nullable String getDescription() {
        return Messages.commandDescriptionRegenerate.toString();
    }

    private int doRegenerate(CommandContext<CommandSourceStack> ctx, final boolean randomiseSeed) {
        CommandSender sender = ctx.getSource().getSender();
        HyperWorld world = ctx.getArgument("world", HyperWorld.class);

        final WorldConfiguration configuration = world.getConfiguration().copy();
        if (randomiseSeed) {
            configuration.setSeed(SeedUtil.randomSeed());
        }

        world.deleteWorld(worldUnloadResult -> {
            if (worldUnloadResult != HyperWorld.WorldUnloadResult.SUCCESS) {
                MessageUtil.sendMessage(sender, Messages.messageWorldNotRemoved, "%reason%",
                        worldUnloadResult.getDescription()
                );
                return;
            }

            //FIXME
            final Path path = Bukkit.getWorldContainer().toPath()
                    .resolve(world.getConfiguration().getName());
            try (Stream<Path> walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (final Exception e) {
                MessageUtil.sendMessage(sender, Messages.messageWorldNotRemoved, "%reason%",
                        e.getMessage()
                );
                e.printStackTrace();
                return;
            }
            MessageUtil.sendMessage(sender, Messages.messageWorldRemoved);
            final HyperWorld hyperWorld = this.hyperWorldFactory.create(UUID.randomUUID(), configuration);
            MessageUtil.sendMessage(sender, Messages.messageWorldCreationStarted);
            hyperWorld.sendWorldInfo(sender);

            // Make sure we don't detect the world load
            this.worldManager.ignoreWorld(configuration.getName());

            try {
                hyperWorld.createBukkitWorld();
                // Register the world
                this.worldManager.addWorld(hyperWorld);
                MessageUtil.sendMessage(sender, Messages.messageWorldCreationFinished);
                if (sender instanceof Player) {
                    // Attempt to teleport them to the world
                    hyperWorld.teleportPlayer((Player) sender);
                }
            } catch (final HyperWorldValidationException validationException) {
                switch (validationException.getValidationResult()) {
                    case UNKNOWN_GENERATOR:
                        MessageUtil.sendMessage(sender, Messages.messageGeneratorInvalid,
                                "%world%", hyperWorld.getConfiguration().getName(),
                                "%generator%", hyperWorld.getConfiguration().getGenerator()
                        );
                        break; 
                    case UNKNOWN_BIOME_PROVIDER:
                        MessageUtil.sendMessage(sender, Messages.messageBiomeProviderInvalid,
                                "%world%", hyperWorld.getConfiguration().getName(),
                                "%biome_provider%", hyperWorld.getConfiguration().getBiomeProvider()
                        );
                        break;
                    case SUCCESS:
                        break;
                    default:
                        MessageUtil.sendMessage(sender, Messages.messageCreationUnknownFailure);
                        break;
                }
            } catch (final Exception e) {
                MessageUtil.sendMessage(sender, Messages.messageWorldCreationFailed,
                        "%reason%", e.getMessage()
                );
            }
        });

        return Command.SINGLE_SUCCESS;
    }
}
