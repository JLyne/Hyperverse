package org.incendo.hyperverse.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
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
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class CreateCommand implements HyperCommand {
    private final Hyperverse hyperverse;
    private final WorldManager worldManager;
    private final WorldConfigurationFactory worldConfigurationFactory;
    private final HyperWorldFactory hyperWorldFactory;
    private WorldConfigurationBuilder builder;

    private final List<Argument<?>> optionalArguments;

    @Inject
    CreateCommand(final Hyperverse hyperverse, final WorldManager worldManager, 
                  final WorldConfigurationFactory worldConfigurationFactory,
                  final HyperWorldFactory hyperWorldFactory) {
        this.hyperverse = hyperverse;
        this.worldManager = worldManager;
        this.worldConfigurationFactory = worldConfigurationFactory;
        this.hyperWorldFactory = hyperWorldFactory;
        
        builder = this.worldConfigurationFactory.builder();

        final GeneratorArgumentType generatorArgumentType = new GeneratorArgumentType();
        final WorldEnvironmentArgumentType worldEnvironmentArgumentType = new WorldEnvironmentArgumentType();
        final WorldTypeArgumentType worldTypeArgumentType = new WorldTypeArgumentType();

        optionalArguments = List.of(
            new Argument<>("generator", generatorArgumentType, String[].class, 
                    (builder, value) -> builder.setGenerator(value[0]).setGeneratorArg(value[1])),
            new Argument<>("biomeprovider", generatorArgumentType, String[].class,
                    (builder, value) -> builder.setBiomeProvider(value[0]).setBiomeProviderArg(value[1])),
            new Argument<>("environment", worldEnvironmentArgumentType, WorldEnvironment.class,
                    WorldConfigurationBuilder::setType),
            new Argument<>("type", worldTypeArgumentType, WorldType.class, WorldConfigurationBuilder::setWorldFeatures),
            new Argument<>("typesettings", StringArgumentType.string(), String.class, 
                    WorldConfigurationBuilder::setSettings),
            new Argument<>("seed", LongArgumentType.longArg(), long.class, WorldConfigurationBuilder::setSeed),
            new Argument<>("generatestructures", BoolArgumentType.bool(), boolean.class,
                    WorldConfigurationBuilder::setGenerateStructures),
            new Argument<>("hardcore", BoolArgumentType.bool(), boolean.class, WorldConfigurationBuilder::setHardcore),
            new Argument<>("bonuschest", BoolArgumentType.bool(), boolean.class, WorldConfigurationBuilder::setBonusChest)
        );
    }

    @Override
    public CommandNode<CommandSourceStack> getCommand() {        
        // Required world name argument
        ArgumentCommandNode<CommandSourceStack, String> worldNameNode = argument("name", StringArgumentType.string())
                .executes(this::doCreate).build();
        
        // The create command has 9 optional arguments, so we use a name -> value structure and a redirect loop to allow any 
        // number of them to be entered in any order.
        // world name -> argument name -> argument value -> redirect to world name
        // Unfortunately due to https://github.com/Mojang/brigadier/issues/137 argument values are lost when a redirect occurs.
        // To work around this we add a redirect modifier to every redirect, which is able to see the argument values before 
        // they disappear, then use that to populate the WorldArgumentBuilder as we go.
        // The redirect modifier won't be called for the final argument, so we also need to do the same thing in the executor.
        optionalArguments.forEach(argument -> {
            LiteralCommandNode<CommandSourceStack> nameNode = literal(argument.name)
                    .then(argument(argument.name, argument.argumentType)
                    .redirect(worldNameNode, this::handleArguments) // Redirect modifier
                    .executes(this::doCreate))
                    .build();

            worldNameNode.addChild(nameNode);
        });

        return literal("create")
                .requires(ctx -> ctx.getSender().hasPermission("hyperverse.create"))
                .then(worldNameNode)
                .build();
    }
    
    // Workaround for https://github.com/Mojang/brigadier/issues/137
    // Populates the WorldConfigurationBuilder with all the arguments that exist before any redirects can remove them
    // If the name argument exists we assume this is a new command execution and reset the builder
    private CommandSourceStack handleArguments(CommandContext<CommandSourceStack> ctx) {
        // Reset builder if the name argument is present
        try {
            String name = ctx.getArgument("name", String.class);
            builder = worldConfigurationFactory.builder().setName(name);
        } catch (IllegalArgumentException _) {}

        optionalArguments.forEach(a -> a.handle(ctx, builder));
        return ctx.getSource();
    }

    @Override
    public @Nullable String getDescription() {
        return Messages.commandDescriptionCreate.toString();
    }

    private int doCreate(CommandContext<CommandSourceStack> ctx) {
        // Redirect handler isn't called for final argument, so handle it here
        handleArguments(ctx);
        
        CommandSender sender = ctx.getSource().getSender();
        WorldConfiguration configuration = builder.createWorldConfiguration();

        // Check if the name already exists
        for (final HyperWorld hyperWorld : this.worldManager.getWorlds()) {
            if (hyperWorld.getConfiguration().getName().equalsIgnoreCase(configuration.getName())) {
                MessageUtil.sendMessage(sender, Messages.messageWorldExists);
                return Command.SINGLE_SUCCESS;
            }
        }
        
        // Double check that Bukkit doesn't have the world stored
        if (Bukkit.getWorld(configuration.getName()) != null) {
            MessageUtil.sendMessage(sender, Messages.messageWorldExists);
            return Command.SINGLE_SUCCESS;
        }
        
        // Now validate the world name
        if (!WorldUtil.validateName(configuration.getName())) {
            MessageUtil.sendMessage(sender, Messages.messageWorldNameInvalid);
            return Command.SINGLE_SUCCESS;
        }
        
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
    
    private record Argument<T>(String name, ArgumentType<T> argumentType, Class<T> valueClass,
                               BiConsumer<WorldConfigurationBuilder, T> handler) {
        public void handle(CommandContext<CommandSourceStack> ctx, WorldConfigurationBuilder builder) {
            try {
                handler.accept(builder, ctx.getArgument(name, valueClass));
            } catch(IllegalArgumentException _) {}
        }
    }
}
