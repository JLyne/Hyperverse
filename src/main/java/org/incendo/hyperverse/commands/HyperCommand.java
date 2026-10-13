package org.incendo.hyperverse.commands;

import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public interface HyperCommand {
    CommandNode<CommandSourceStack> getCommand();
    
    default List<String> getAliases() {
        return Collections.emptyList();
    }
    
    default List<String> getRootAliases() {
        return Collections.emptyList();
    }
    
    default @Nullable String getDescription() {
        return null;
    }
}
