package org.incendo.hyperverse.commands;

import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.incendo.hyperverse.configuration.Message;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.util.MessageUtil;
import org.incendo.hyperverse.world.HyperWorld;
import org.incendo.hyperverse.world.WorldConfiguration;
import org.incendo.hyperverse.world.WorldManager;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

public class ListCommand implements HyperCommand {
    private static final int WORLDS_PER_PAGE = 10;
    private final WorldManager worldManager;

    @Inject
    public ListCommand(final WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @Override
    public CommandNode<CommandSourceStack> getCommand() {
        return literal("list")
                .requires(ctx -> ctx.getSender().hasPermission("hyperverse.list"))
                .executes(ctx -> doList(ctx, 1))
                .then(argument("page", integer())
                        .executes(ctx -> doList(ctx, ctx.getArgument("page", Integer.class))))
                .build();
    }

    @Override
    public List<String> getRootAliases() {
        return List.of("hvl");
    }

    @Override
    public List<String> getAliases() {
        return List.of("l", "worlds");
    }

    @Override
    public @Nullable String getDescription() {
        return Messages.commandDescriptionList.toString();
    }

    private int doList(final CommandContext<CommandSourceStack> ctx, final int page) {
        CommandSender sender = ctx.getSource().getSender();
        final List<HyperWorld> worlds = new ArrayList<>(this.worldManager.getWorlds());
        worlds.sort(Comparator.comparing(world -> world.getConfiguration().getName()));

        final int pages = 1 + (int) ((double) worlds.size() / WORLDS_PER_PAGE);
        final int clampedPage = Math.clamp(page, 1, pages);
        final int min = WORLDS_PER_PAGE * (clampedPage - 1);
        final int max = Math.min(worlds.size(), WORLDS_PER_PAGE * clampedPage);

        MessageUtil.sendMessage(
                sender,
                Messages.messageListHeader,
                "%page%",
                Integer.toString(clampedPage),
                "%max%",
                Integer.toString(pages)
        );

        Stream<HyperWorld> stream = worlds.subList(min, max).stream().sorted(Comparator.comparing(world -> world
                .getConfiguration()
                .getName()));
        if (sender instanceof Entity) {
            stream = stream.sorted(Comparator
                    .comparing(world -> !((Entity) sender).getWorld().equals(world.getBukkitWorld())));
        }
        stream.forEachOrdered(hyperWorld -> {
            final WorldConfiguration configuration = hyperWorld.getConfiguration();

            // Format the generator name a little better
            String generator = configuration.getGenerator();
            if (generator.isEmpty()) {
                generator = "vanilla";
            } else {
                generator = generator.toLowerCase();
            }

            final String loadStatus;
            if (hyperWorld.isLoaded()) {
                loadStatus = "<green><hover:show_text:\"<gray>Click to unload</gray>\"><click:run_command:/hyperverse unload "
                        + configuration.getName() + ">loaded</click></hover></green>";
            } else {
                loadStatus = "<red><hover:show_text:\"<gray>Click to load</gray>\"><click:run_command:/hyperverse load "
                        + configuration.getName() + ">unloaded</click></hover></red>";
            }

            final Message message;
            if (sender instanceof Entity && ((Entity) sender).getWorld() == hyperWorld.getBukkitWorld()) {
                message = Messages.messageListEntryCurrentWorld;
            } else {
                message = Messages.messageListEntry;
            }

            MessageUtil.sendMessage(sender, message, "%name%", configuration.getName(),
                    "%display-name%", hyperWorld.getDisplayName(), "%generator%", generator, "%type%",
                    configuration.getType().name(), "%load_status%", loadStatus
            );
        });

        return Command.SINGLE_SUCCESS;
    }
}
