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

import com.google.gson.Gson;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration of a {@link HyperWorld}
 */
public final class WorldConfiguration implements Cloneable {

    private static final Gson GSON = SimpleWorldConfigurationFactory.GSON;

    private String name;
    private WorldEnvironment type;
    private WorldType worldType;
    private String settings;
    private long seed;
    private boolean generateStructures;
    private boolean hardcore;
    private boolean bonusChest;
    private String generator;
    private String generatorArg;
    private String biomeProvider;
    private String biomeProviderArg;
    // Mutable properties
    private boolean loaded = true;
    private Map<String, String> flags;

    WorldConfiguration(
            final @NonNull String name,
            final @NonNull WorldEnvironment type,
            final @NonNull WorldType worldType,
            final @NonNull String settings,
            final long seed,
            final boolean generateStructures,
            final boolean hardcore,
            final boolean bonusChest,
            final String generator,
            final String generatorArg,
            final String biomeProvider,
            final String biomeProviderArg
    ) {
        this.name = name;
        this.type = type;
        this.settings = settings;
        this.seed = seed;
        this.generateStructures = generateStructures;
        this.hardcore = hardcore;
        this.bonusChest = bonusChest;
        this.generator = generator;
        this.generatorArg = generatorArg;
        this.biomeProvider = biomeProvider;
        this.biomeProviderArg = biomeProviderArg;
        this.worldType = worldType;
        this.flags = new HashMap<>();
    }

    /**
     * Get a new {@link WorldConfigurationBuilder} instance
     *
     * @return New builder instance
     */
    @Deprecated
    public static @NonNull WorldConfigurationBuilder builder() {
        return new WorldConfigurationBuilder();
    }

    /**
     * Create a copy of the world configuration
     *
     * @return Copy of this configuration
     */
    public @NonNull WorldConfiguration copy() {
        try {
            return this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected @NonNull WorldConfiguration clone() throws CloneNotSupportedException {
        final WorldConfiguration other = (WorldConfiguration) super.clone();
        other.name = this.name;
        other.type = this.type;
        other.settings = this.settings;
        other.seed = this.seed;
        other.generateStructures = this.generateStructures;
        other.hardcore = this.hardcore;
        other.bonusChest = this.bonusChest;
        other.generator = this.generator;
        other.generatorArg = this.generatorArg;
        other.biomeProvider = this.biomeProvider;
        other.biomeProviderArg = this.biomeProviderArg;
        other.worldType = this.worldType;
        other.flags = new HashMap<>(this.flags);
        return other;
    }

    /**
     * Get the world features
     *
     * @return World features enum
     */
    public @NonNull WorldType getWorldFeatures() {
        return this.worldType;
    }

    /**
     * Get the world name
     *
     * @return World name
     */
    public @NonNull String getName() {
        return this.name;
    }

    /**
     * Get the world type
     *
     * @return World type
     */
    public @NonNull WorldEnvironment getType() {
        return this.type;
    }

    /**
     * Get the world setting string
     * This is primarily used by the flat world generator
     *
     * @return Setting string
     */
    public @NonNull String getSettings() {
        return this.settings;
    }

    /**
     * Get the world seed
     *
     * @return World seed
     */
    public long getSeed() {
        return this.seed;
    }

    /**
     * Set the seed
     *
     * @param seed New seed
     */
    public void setSeed(final long seed) {
        this.seed = seed;
    }

    /**
     * Check if the world should generate structures
     *
     * @return Structure generation status
     */
    public boolean isGenerateStructures() {
        return this.generateStructures;
    }

    /**
     * Check if hardcore mode is enabled for the world
     *
     * @return Hardcore mode status
     */
    public boolean isHardcore() {
        return hardcore;
    }

    /**
     * Check if the bonus chest will be generated
     *
     * @return Bonus chest status
     */
    public boolean isBonusChest() {
        return bonusChest;
    }

    /**
     * Get the world generator name
     *
     * @return World generator name
     */
    public String getGenerator() {
        return this.generator;
    }

    /**
     * Set the world generator
     *
     * @param generator New generator
     */
    public void setGenerator(final @NonNull String generator) {
        this.generator = generator;
    }

    /**
     * Get the world generator arguments
     *
     * @return World generator arguments
     */
    public String getGeneratorArg() {
        return this.generatorArg;
    }

    /**
     * Get the biome provider name
     *
     * @return World biome provider name
     */
    public String getBiomeProvider() {
        return biomeProvider;
    }

    /**
     * Get the biome provider arguments
     *
     * @return World biome provider arguments
     */
    public String getBiomeProviderArg() {
        return biomeProviderArg;
    }

    /**
     * Get an internal map containing all
     * configured flags
     *
     * @return All configured world flags
     */
    public @NonNull Map<@NonNull String, @NonNull String> getFlags() {
        if (this.flags == null) {
            this.flags = new HashMap<>();
        }
        return this.flags;
    }

    /**
     * Update a flag value internally
     *
     * @param flag      Flag name
     * @param flagValue Flag value
     */
    public void setFlagValue(
            final @NonNull String flag,
            final @Nullable String flagValue
    ) {
        if (flagValue == null) {
            this.flags.remove(flag);
        } else {
            this.flags.put(flag, flagValue);
        }
    }

    /**
     * Check whether a world is supposed to be loaded
     *
     * @return World load status
     */
    public boolean isLoaded() {
        return this.loaded;
    }

    /**
     * Set whether a world is supposed to be loaded
     *
     * @param loaded World load status
     */
    public void setLoaded(final boolean loaded) {
        this.loaded = loaded;
    }

    /**
     * Write this configuration to a file
     *
     * @param path File to write to
     */
    public void writeToFile(final @NonNull Path path) {
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try (final BufferedWriter bufferedWriter = Files
                .newBufferedWriter(Objects.requireNonNull(path))) {
            GSON.toJson(this, WorldConfiguration.class, GSON.newJsonWriter(bufferedWriter));
            return;
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "WorldConfiguration{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", worldType=" + worldType +
                ", settings='" + settings + '\'' +
                ", seed=" + seed +
                ", generateStructures=" + generateStructures +
                ", hardcore=" + hardcore +
                ", bonusChest=" + bonusChest +
                ", generator='" + generator + '\'' +
                ", generatorArg='" + generatorArg + '\'' +
                ", biomeProvider='" + biomeProvider + '\'' +
                ", biomeProviderArg='" + biomeProviderArg + '\'' +
                ", loaded=" + loaded +
                ", flags=" + flags +
                '}';
    }
}
