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
import org.bukkit.Bukkit;
import org.incendo.hyperverse.configuration.Messages;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class LegacyImportCandidateArgumentType implements CustomArgumentType.Converted<String, String> {
    private static final LiteralMessage invalidWorldMessage = new LiteralMessage(Messages.messageNoSuchWorld.toString());
    
    @Override
    public String convert(final String value) throws CommandSyntaxException {        
        @SuppressWarnings("UsagesOfObsoleteApi") final File baseDirectory = Bukkit.getWorldContainer();
        final File worldDirectory = new File(baseDirectory, value);
        final File levelDat = new File(worldDirectory, "level.dat");
        
        if (!worldDirectory.isDirectory() || !levelDat.isFile()) {
            throw new SimpleCommandExceptionType(invalidWorldMessage).create();
        }
        
        return value;
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        String input = builder.getRemainingLowerCase();

        // Pre 26.1 worlds
        @SuppressWarnings("UsagesOfObsoleteApi") final File baseDirectory = Bukkit.getWorldContainer();
        try (final Stream<Path> files = Files.list(baseDirectory.toPath())) {
            files.filter(path -> {
                        final File file = path.toFile();
                        return file.isDirectory() && new File(file, "level.dat").isFile();
                    })
                    .map(path -> path.toFile().getName()).sorted(Comparator.naturalOrder())
                    .filter(v -> v.toLowerCase().startsWith(input))
                    .forEach(builder::suggest);
        } catch (IOException _) {}
        
        return CompletableFuture.completedFuture(builder.build());
    }
}
