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

package org.incendo.hyperverse.world;

import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.Optional;

/**
 * World environment, mirroring Bukkit's {@link World.Environment}
 */
@NullMarked
public enum WorldEnvironment {
    OVER_WORLD(World.Environment.NORMAL, "overworld"),
    NETHER(World.Environment.NETHER, "nether"),
    END(World.Environment.THE_END, "the_end");

    private final World.Environment bukkitEnvironment;
    private final String name;

    WorldEnvironment(
            final World.Environment bukkitEnvironment,
            final String name
    ) {
        this.bukkitEnvironment = bukkitEnvironment;
        this.name = name;
    }

    /**
     * Attempt to map a string to a world environment
     *
     * @param string String to match
     * @return Optional containing the environment, if found
     */
    public static Optional<WorldEnvironment> fromString(final String string) {
        final String normalized = Objects.requireNonNull(string.toLowerCase());
        for (final WorldEnvironment worldEnvironment : values()) {
            if (worldEnvironment.getName().equals(normalized)) {
                return Optional.of(worldEnvironment);
            }
        }
        return Optional.empty();
    }

    /**
     * Get the world environment from a bukkit environment
     *
     * @param environment Bukkit environment
     * @return Equivalent Hyperverse world environment
     */
    public static WorldEnvironment fromBukkit(final World.Environment environment) {
        for (final WorldEnvironment worldEnvironment : values()) {
            if (worldEnvironment.getBukkitType() == environment) {
                return worldEnvironment;
            }
        }
        return WorldEnvironment.OVER_WORLD;
    }

    /**
     * Get the bukkit equivalent
     *
     * @return Bukkit equivalent
     */
    public World.Environment getBukkitType() {
        return this.bukkitEnvironment;
    }

    /**
     * Get all aliases of the environment name
     *
     * @return Name aliases
     */
    public String getName() {
        return this.name;
    }
}
