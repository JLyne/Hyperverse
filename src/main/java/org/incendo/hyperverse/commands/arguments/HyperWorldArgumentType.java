package org.incendo.hyperverse.commands.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.incendo.hyperverse.Hyperverse;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.world.HyperWorld;
import org.incendo.hyperverse.world.WorldManager;

import java.util.concurrent.CompletableFuture;

public class HyperWorldArgumentType implements CustomArgumentType.Converted<HyperWorld, String> {
    private static final LiteralMessage invalidWorldMessage = new LiteralMessage(Messages.messageNoSuchWorld.toString());
    private final WorldManager worldManager;
    private final HyperWorldState state;

    public HyperWorldArgumentType(Hyperverse plugin) {
        worldManager = plugin.getWorldManager();
        state = HyperWorldState.ANY;
    }
    
    public HyperWorldArgumentType(Hyperverse plugin, HyperWorldState state) {
        worldManager = plugin.getWorldManager();
        this.state = state;
    }
    
    @Override
    public HyperWorld convert(final String value) throws CommandSyntaxException {
        HyperWorld hyperWorld = worldManager.getWorld(value);
        
        if (hyperWorld == null) {
            throw new SimpleCommandExceptionType(invalidWorldMessage).create();
        }
        
        return hyperWorld;
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        String input = builder.getRemainingLowerCase();
        
        worldManager.getWorlds().stream()
                .filter(hw ->
                    switch (state) {
                        case LOADED_ONLY -> hw.isLoaded();
                        case UNLOADED_ONLY -> !hw.isLoaded();
                        case POPULATED_ONLY -> hw.isLoaded() && !hw.getBukkitWorld().getPlayers().isEmpty();
                        case UNPOPULATED_ONLY -> hw.isLoaded() && hw.getBukkitWorld().getPlayers().isEmpty();
                        default -> true;
                    }
                )
                .map(w -> w.getConfiguration().getName())
                .filter(name -> name.startsWith(input))
                .forEach(builder::suggest);
        
        return CompletableFuture.completedFuture(builder.build());
    }

    public enum HyperWorldState {
        ANY,
        LOADED_ONLY,
        UNLOADED_ONLY,
        POPULATED_ONLY,
        UNPOPULATED_ONLY
    }
}
