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

package org.incendo.hyperverse.events;

import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.hyperverse.world.HyperWorld;

import java.util.Objects;

/**
 * Events involving {@link org.bukkit.entity.Player} inside of a
 * {@link org.incendo.hyperverse.world.HyperWorld}
 */
public abstract class HyperPlayerEvent extends HyperWorldEvent {

    private final Player player;

    public HyperPlayerEvent(
            final @NonNull Player player,
            final @NonNull HyperWorld hyperWorld
    ) {
        super(hyperWorld);
        this.player = Objects.requireNonNull(player, "player");
    }

    /**
     * Get the player involved in the event
     *
     * @return Player involved in the event
     */
    public final @NonNull Player getPlayer() {
        return this.player;
    }

}
