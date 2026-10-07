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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EnumArgumentType<E extends Enum<E>> implements CustomArgumentType.Converted<E, String> {
    private final List<E> values;
    private final String label;
    
    public EnumArgumentType(Class<E> enumType, String label) {
        values = Arrays.asList(enumType.getEnumConstants());
        this.label = label;
    }

    @Override
    public E convert(final String nativeType) throws CommandSyntaxException {
        return values.stream().filter(e -> e.name().equals(nativeType.toUpperCase())).findFirst()
                .orElseThrow(() -> new SimpleCommandExceptionType(new LiteralMessage("Invalid " + label)).create());
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        String input = builder.getRemainingLowerCase();
        
        values.stream().map(Enum::name).map(String::toLowerCase).filter(n -> n.startsWith(input)).forEach(builder::suggest);
        
        return builder.buildFuture();
    }
}
