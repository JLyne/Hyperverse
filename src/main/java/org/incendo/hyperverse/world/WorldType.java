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

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@NullMarked
public enum WorldType {
    NORMAL(org.bukkit.WorldType.NORMAL, "normal"),
    FLATLAND(org.bukkit.WorldType.FLAT, "flat"),
    AMPLIFIED(org.bukkit.WorldType.AMPLIFIED, "amplified"),
    BUFFET("BUFFET", "buffet");

    private static final IllegalStateException INCOMPATIBLE_VERSION = new IllegalStateException(
            "WorldType is unavailable on this version.");
    private final org.bukkit.@Nullable WorldType bukkitType;
    private final String name;

    WorldType(
            final org.bukkit.WorldType bukkitType,
            final String name
    ) {
        this.bukkitType = Objects.requireNonNull(bukkitType);
        this.name = name;
    }

    WorldType(
            final String enumName,
            final String name
    ) {
        org.bukkit.WorldType worldType;
        try {
            worldType = org.bukkit.WorldType.valueOf(enumName);
        } catch (IllegalArgumentException ex) {
            worldType = null;
        }
        this.bukkitType = worldType;
        this.name = name;
    }

    /**
     * Get a {@link WorldType} instance from its name
     *
     * @param name The name
     * @return Optional that contains the {@link WorldType} instance, if found
     */
    public static Optional<WorldType> fromString(final String name) {
        Objects.requireNonNull(name);
        for (final WorldType worldType : values()) {
            if (worldType.name.equals(name.toLowerCase(Locale.ENGLISH)) && worldType.bukkitType != null) {
                return Optional.of(worldType);
            }
        }
        return Optional.empty();
    }

    /**
     * Create a {@link WorldType} from a Bukkit {@link org.bukkit.WorldType}
     *
     * @param bukkitType Bukkit {@link org.bukkit.WorldType}
     * @return A {@link WorldType} instance, or {@code null} if there's no Hyperverse equivalent
     */
    public static @Nullable WorldType fromBukkit(final org.bukkit.WorldType bukkitType) {
        Objects.requireNonNull(bukkitType);
        for (final WorldType worldType : values()) {
            if (worldType.getBukkitType() == bukkitType) {
                return worldType;
            }
        }
        return null;
    }

    /**
     * Get the Bukkit enum world type for this world feature.
     *
     * @return Returns a never null {@link org.bukkit.WorldType}.
     * @throws IllegalStateException Thrown if the bukkit world type is not available.
     *                               This method will always throw an {@link IllegalStateException}
     *                               if {@link #isAvailable()} returns false.
     */
    public org.bukkit.WorldType getBukkitType() throws IllegalStateException {
        if (this.bukkitType == null) {
            throw INCOMPATIBLE_VERSION;
        }
        return this.bukkitType;
    }

    /**
     * Whether the feature type is available on the server
     *
     * @return {@code true} if the feature is available, else {@code false}
     */
    public boolean isAvailable() {
        return this.bukkitType != null;
    }

    public String getName() {
        return name;
    }
}
