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
import org.incendo.hyperverse.world.WorldType;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class WorldTypeArgumentType implements CustomArgumentType.Converted<WorldType, String> {
    private static final LiteralMessage invalidWorldTypeMessage =
            new LiteralMessage(Messages.messageInvalidWorldFeatures.toString());
    
    @Override
    public WorldType convert(final String input) throws CommandSyntaxException {
        return WorldType.fromString(input).orElseThrow(() -> new SimpleCommandExceptionType(invalidWorldTypeMessage).create());
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        String input = builder.getRemainingLowerCase();

        Arrays.stream(WorldType.values())
                .map(WorldType::getName)
                .filter(name -> name.startsWith(input))
                .forEach(builder::suggest);
        
        return CompletableFuture.completedFuture(builder.build());
    }
}
