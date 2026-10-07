package org.incendo.hyperverse.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.hyperverse.Hyperverse;
import org.incendo.hyperverse.commands.arguments.GeneratorArgumentType;
import org.incendo.hyperverse.commands.arguments.WorldEnvironmentArgumentType;
import org.incendo.hyperverse.commands.arguments.WorldTypeArgumentType;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.exception.HyperWorldValidationException;
import org.incendo.hyperverse.modules.HyperWorldFactory;
import org.incendo.hyperverse.modules.WorldConfigurationFactory;
import org.incendo.hyperverse.util.MessageUtil;
import org.incendo.hyperverse.util.WorldUtil;
import org.incendo.hyperverse.world.HyperWorld;
import org.incendo.hyperverse.world.WorldConfiguration;
import org.incendo.hyperverse.world.WorldConfigurationBuilder;
import org.incendo.hyperverse.world.WorldEnvironment;
import org.incendo.hyperverse.world.WorldManager;
import org.incendo.hyperverse.world.WorldType;

import java.util.UUID;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class CreateCommand implements HyperCommand {
    private final Hyperverse hyperverse;
    private final WorldManager worldManager;
    private final WorldConfigurationFactory worldConfigurationFactory;
    private final HyperWorldFactory hyperWorldFactory;
    private final GeneratorArgumentType generatorArgumentType;
    private final WorldEnvironmentArgumentType worldEnvironmentArgumentType;
    private final WorldTypeArgumentType worldTypeArgumentType;

    @Inject
    CreateCommand(final Hyperverse hyperverse, final WorldManager worldManager, 
                  final WorldConfigurationFactory worldConfigurationFactory,
                  final HyperWorldFactory hyperWorldFactory) {
        this.hyperverse = hyperverse;
        this.worldManager = worldManager;
        this.worldConfigurationFactory = worldConfigurationFactory;
        this.hyperWorldFactory = hyperWorldFactory;
        generatorArgumentType = new GeneratorArgumentType();
        worldEnvironmentArgumentType = new WorldEnvironmentArgumentType();
        worldTypeArgumentType = new WorldTypeArgumentType();
    }

    @Override
    public CommandNode<CommandSourceStack> getCommand() {
        ArgumentCommandNode<CommandSourceStack, String> worldNode = argument("world", StringArgumentType.word())
                .executes(this::doCreate)
                .build();
        
        // Add a redirect at the end of every argument so any of them can be specified in any order
        // This does allow the same arguments to be specified multiple times, too bad
        worldNode.addChild(literal("generator")
                .then(argument("generator", generatorArgumentType).executes(this::doCreate).redirect(worldNode)).build());
        
        worldNode.addChild(literal("biomeprovider")
                .then(argument("biomeprovider", generatorArgumentType).executes(this::doCreate).redirect(worldNode)).build());
        
        worldNode.addChild(literal("environment")
                .then(argument("environment", worldEnvironmentArgumentType).executes(this::doCreate).redirect(worldNode)).build());
        
        worldNode.addChild(literal("type")
                .then(argument("type", worldTypeArgumentType).executes(this::doCreate).redirect(worldNode)).build());
        
        worldNode.addChild(literal("typesettings")
                .then(argument("typesettings", StringArgumentType.string()).executes(this::doCreate).redirect(worldNode)).build());
        
        worldNode.addChild(literal("seed")
                .then(argument("seed", LongArgumentType.longArg()).executes(this::doCreate).redirect(worldNode)).build());
        
        worldNode.addChild(literal("generatestructures")
                .then(argument("generatestructures", BoolArgumentType.bool()).executes(this::doCreate).redirect(worldNode)).build());
        
        worldNode.addChild(literal("hardcore")
                .then(argument("hardcore", BoolArgumentType.bool()).executes(this::doCreate).redirect(worldNode)).build());
        
        worldNode.addChild(literal("bonuschest")
                .then(argument("bonuschest", BoolArgumentType.bool()).executes(this::doCreate).redirect(worldNode)).build());
        
        return literal("create")
                .requires(ctx -> ctx.getSender().hasPermission("hyperverse.create"))
                .then(worldNode)
                .build();
    }
    
    private int doCreate(CommandContext<CommandSourceStack> ctx) {
        String name = ctx.getArgument("world", String.class);
        CommandSender sender = ctx.getSource().getSender();

        // Check if the name already exists
        for (final HyperWorld hyperWorld : this.worldManager.getWorlds()) {
            if (hyperWorld.getConfiguration().getName().equalsIgnoreCase(name)) {
                MessageUtil.sendMessage(sender, Messages.messageWorldExists);
                return Command.SINGLE_SUCCESS;
            }
        }
        
        // Double check that Bukkit doesn't have the world stored
        if (Bukkit.getWorld(name) != null) {
            MessageUtil.sendMessage(sender, Messages.messageWorldExists);
            return Command.SINGLE_SUCCESS;
        }
        
        // Now validate the world name
        if (!WorldUtil.validateName(name)) {
            MessageUtil.sendMessage(sender, Messages.messageWorldNameInvalid);
            return Command.SINGLE_SUCCESS;
        }
        
        final WorldConfigurationBuilder worldConfiguration = this.worldConfigurationFactory.builder()
                .setName(name);
        
        try {
            String[] generator = ctx.getArgument("generator", String[].class);
            worldConfiguration.setGenerator(generator[0]).setGeneratorArg(generator[1]);
        } catch (IllegalArgumentException _) {}        
        
        try {
            String[] biomeProvider = ctx.getArgument("biomeprovider", String[].class);
            worldConfiguration.setBiomeProvider(biomeProvider[0]).setBiomeProviderArg(biomeProvider[1]);
        } catch (IllegalArgumentException _) {}
        
        try {
            WorldEnvironment environment = ctx.getArgument("environment", WorldEnvironment.class);
            worldConfiguration.setType(environment);
        } catch (IllegalArgumentException _) {}
        
        try {
            WorldType type = ctx.getArgument("type", WorldType.class);
            worldConfiguration.setWorldFeatures(type);
        } catch (IllegalArgumentException _) {}
        
        try {
            String typeSettings = ctx.getArgument("typesettings", String.class);
            worldConfiguration.setSettings(typeSettings);
        } catch (IllegalArgumentException _) {}
        
        try {
            long seed = ctx.getArgument("seed", Long.class);
            worldConfiguration.setSeed(seed);
        } catch (IllegalArgumentException _) {}
        
        try {
            boolean generateStructures = ctx.getArgument("generatestructures", Boolean.class);
            worldConfiguration.setGenerateStructures(generateStructures);
        } catch (IllegalArgumentException _) {}
        
        try {
            boolean hardcore = ctx.getArgument("hardcore", Boolean.class);
            worldConfiguration.setHardcore(hardcore);
        } catch (IllegalArgumentException _) {}
        
        try {
            boolean bonusChest = ctx.getArgument("bonuschest", Boolean.class);
            worldConfiguration.setBonusChest(bonusChest);
        } catch (IllegalArgumentException _) {}
        
        WorldConfiguration configuration = worldConfiguration.createWorldConfiguration();
        createWorld(configuration, sender);
        
        return Command.SINGLE_SUCCESS;
    }

    private void createWorld(WorldConfiguration configuration, CommandSender sender) {
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
            hyperverse.getSLF4JLogger().error("Failed to create world {}", hyperWorld.getDisplayName(), e);
        }
    }
}
