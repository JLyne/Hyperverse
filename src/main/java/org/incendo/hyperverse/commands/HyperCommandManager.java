//
//  Hyperverse - A minecraft world management plugin
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program. If not, see <http://www.gnu.org/licenses/>.
//

package org.incendo.hyperverse.commands;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.incendo.hyperverse.Hyperverse;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.papermc.paper.command.brigadier.Commands.literal;

public final class HyperCommandManager {
    private final Set<HyperCommand> commands;
    private boolean registered = false;
    
    @Inject
    public HyperCommandManager(final Hyperverse hyperverse) {
        commands = new HashSet<>();
        Injector injector = hyperverse.getInjector();
        
        // Built in commands
        addCommand(injector.getInstance(ReloadCommand.class));
        addCommand(injector.getInstance(ListCommand.class));
        addCommand(injector.getInstance(InfoCommand.class));
        addCommand(injector.getInstance(FlagCommand.class));
        addCommand(injector.getInstance(GameRuleCommand.class));
        addCommand(injector.getInstance(LoadCommand.class));
        addCommand(injector.getInstance(UnloadCommand.class));
        addCommand(injector.getInstance(TeleportCommand.class));
        addCommand(injector.getInstance(FindCommand.class));
        addCommand(injector.getInstance(WhoCommand.class));
        addCommand(injector.getInstance(RegenerateCommand.class));
        addCommand(injector.getInstance(DeleteCommand.class));
        addCommand(injector.getInstance(CreateCommand.class));
    }
    
    public void addCommand(HyperCommand command) {
        if(registered) {
            throw new IllegalStateException("Commands have already been registered");
        }
        
        commands.add(command);
    }
    
    public void register(Commands registrar) {
        registered = true;
        
        LiteralArgumentBuilder<CommandSourceStack> hyperverseCommand = literal("hyperverse")
                .requires(ctx-> ctx.getSender().hasPermission("hyperverse.worlds"));
        
        for(HyperCommand command: commands) {
            hyperverseCommand.then(command.getCommand());
            
            //TODO: Top level aliases
        }

        registrar.register(hyperverseCommand.build(), "Hyperverse command", List.of("hv", "worlds", "world"));
    }
}
