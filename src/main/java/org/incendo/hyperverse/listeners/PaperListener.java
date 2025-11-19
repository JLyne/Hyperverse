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

package org.incendo.hyperverse.listeners;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.hyperverse.flags.implementation.AdvancementFlag;
import org.incendo.hyperverse.world.HyperWorld;
import org.incendo.hyperverse.world.WorldManager;

public final class PaperListener implements Listener {

    private final WorldManager worldManager;

    PaperListener(final @NonNull WorldManager worldManager) {
        this.worldManager = worldManager;
    }

    @EventHandler
    public void onAdvancementGrant(final @NonNull PlayerAdvancementCriterionGrantEvent event) {
        final HyperWorld hyperWorld = this.worldManager.getWorld(event.getPlayer().getWorld());
        if (hyperWorld == null) {
            return;
        }
        if (hyperWorld.getFlag(AdvancementFlag.class)) {
            return;
        }
        event.setCancelled(true);
    }

}
