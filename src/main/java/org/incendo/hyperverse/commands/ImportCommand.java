package org.incendo.hyperverse.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.hyperverse.commands.arguments.GeneratorArgumentType;
import org.incendo.hyperverse.commands.arguments.LegacyImportCandidateArgumentType;
import org.incendo.hyperverse.commands.arguments.WorldEnvironmentArgumentType;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.exception.HyperWorldValidationException;
import org.incendo.hyperverse.modules.HyperWorldFactory;
import org.incendo.hyperverse.util.MessageUtil;
import org.incendo.hyperverse.util.WorldUtil;
import org.incendo.hyperverse.world.HyperWorld;
import org.incendo.hyperverse.world.WorldConfigurationBuilder;
import org.incendo.hyperverse.world.WorldEnvironment;
import org.incendo.hyperverse.world.WorldManager;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class ImportCommand implements HyperCommand {
    private final LegacyImportCandidateArgumentType legacyImportCandidateArgumentType;
    private final GeneratorArgumentType generatorArgumentType;
    private final WorldEnvironmentArgumentType environmentArgumentType;
    private final WorldManager worldManager;
    private final HyperWorldFactory hyperWorldFactory;

    @Inject
    public ImportCommand(final WorldManager worldManager, final HyperWorldFactory hyperWorldFactory) {
        this.hyperWorldFactory = hyperWorldFactory;
        this.worldManager = worldManager;
        legacyImportCandidateArgumentType = new LegacyImportCandidateArgumentType();
        generatorArgumentType = new GeneratorArgumentType();
        environmentArgumentType = new WorldEnvironmentArgumentType();
    }
    
    @Override
    public CommandNode<CommandSourceStack> getCommand() {
        return literal("import")
                .requires(ctx -> ctx.getSender().hasPermission("hyperverse.import"))
                .then(literal("legacy")
                        .then(argument("world", legacyImportCandidateArgumentType)
                                .executes(ctx -> 
                                        doLegacyImport(ctx, WorldEnvironment.OVER_WORLD, new String[]{"", ""}))
                                .then(argument("environment", environmentArgumentType)
                                        .executes(ctx -> 
                                                doLegacyImport(ctx, ctx.getArgument("environment", WorldEnvironment.class),
                                                         new String[]{"", ""}))
                                        .then(argument("generator", generatorArgumentType)
                                                .executes(ctx -> 
                                                        doLegacyImport(ctx,
                                                                ctx.getArgument("environment", WorldEnvironment.class),
                                                                ctx.getArgument("generator", String[].class)))))))
                // TODO: Modern worlds
                .build();
    }

    @Override
    public List<String> getRootAliases() {
        return List.of("hvimport");
    }

    @Override
    public @Nullable String getDescription() {
        return Messages.commandDescriptionImport.toString();
    }

    private int doLegacyImport(
            final CommandContext<CommandSourceStack> ctx,
            final WorldEnvironment worldEnvironment,
            final String[] generator
    ) {
        CommandSender sender = ctx.getSource().getSender();
        String worldName = ctx.getArgument("world", String.class);
        
        if (this.worldManager.getWorld(worldName) != null) {
            MessageUtil.sendMessage(sender, Messages.messageWorldAlreadyImported);
            return Command.SINGLE_SUCCESS;
        }
        
        if (!WorldUtil.isSuitableImportCandidate(worldName, this.worldManager)) {
            MessageUtil.sendMessage(sender, Messages.messageNoSuchWorld);
            return Command.SINGLE_SUCCESS;
        }
        
        this.worldManager.ignoreWorld(worldName); //Make sure we don't auto register on init
        final HyperWorld hyperWorld = this.hyperWorldFactory.create(
                UUID.randomUUID(),
                new WorldConfigurationBuilder().setName(worldName)
                        .setGenerator(generator[0]).setGeneratorArg(generator[1])
                        .setType(worldEnvironment).createWorldConfiguration()
        );
        final World bukkitWorld;
        
        try {
            hyperWorld.createBukkitWorld();
            bukkitWorld = hyperWorld.getBukkitWorld();
            assert bukkitWorld != null;
        } catch (HyperWorldValidationException e) {
            MessageUtil.sendMessage(sender, Messages.messageWorldImportFailure, "%world%", worldName,
                    "%result%", e.getMessage()
            );
            return Command.SINGLE_SUCCESS;
        }
        
        this.worldManager.addWorld(hyperWorld);
        MessageUtil.sendMessage(sender, Messages.messageWorldImportFinished);

        if (sender instanceof Player player) {
            hyperWorld.teleportPlayer(player);
        }
        
        return Command.SINGLE_SUCCESS;
    }
}
