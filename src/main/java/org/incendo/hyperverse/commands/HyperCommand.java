package org.incendo.hyperverse.commands;

import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public interface HyperCommand {
    CommandNode<CommandSourceStack> getCommand();
}
