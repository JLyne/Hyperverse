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
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.world.WorldEnvironment;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class WorldEnvironmentArgumentType implements CustomArgumentType.Converted<WorldEnvironment, String> {
    private static final LiteralMessage invalidWorldEnvironmentMessage =
            new LiteralMessage(Messages.messageInvalidWorldType.toString());

    @Override
    public WorldEnvironment convert(final String input) throws CommandSyntaxException {
        return WorldEnvironment.fromString(input).orElseThrow(() -> 
                new SimpleCommandExceptionType(invalidWorldEnvironmentMessage).create());
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        String input = builder.getRemainingLowerCase();

        Arrays.stream(WorldEnvironment.values())
                .map(WorldEnvironment::getName)
                .filter(name -> name.startsWith(input))
                .forEach(builder::suggest);
        
        return CompletableFuture.completedFuture(builder.build());
    }
}
