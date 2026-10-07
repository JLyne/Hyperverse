package org.incendo.hyperverse.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class GeneratorArgumentType implements CustomArgumentType.Converted<String[], String> {
    @Override
    public String[] convert(final String input) {
        final String generator;
        final String generatorArgs;

        if (input.contains(":")) {
            final String[] split = input.split(":");
            generator = split[0];
            generatorArgs = split[1];
        } else {
            generator = input;
            generatorArgs = "";
        }
        
        return new String[] {generator, generatorArgs};
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        String input = builder.getRemainingLowerCase();

        if (input.contains(":")) {
            return Suggestions.empty();
        }

        Arrays.stream(Bukkit.getPluginManager().getPlugins())
                .map(Plugin::getName)
                .map(String::toLowerCase)
                .filter(name -> name.startsWith(input))
                .forEach(builder::suggest);
        
        if ("vanilla".startsWith(input)) {
            builder.suggest("vanilla");
        }
        
        return CompletableFuture.completedFuture(builder.build());
    }
}
