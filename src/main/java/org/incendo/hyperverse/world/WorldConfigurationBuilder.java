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

import org.incendo.hyperverse.util.SeedUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Builder for {@link WorldConfiguration}
 */
public class WorldConfigurationBuilder {

    private String name;
    private WorldEnvironment type = WorldEnvironment.OVER_WORLD;
    private WorldType worldType = WorldType.NORMAL;
    private String settings = "";
    private Long seed = null;
    private boolean generateStructures = true;
    private boolean hardcore = false;
    private boolean bonusChest = false;
    private String generator;
    private String generatorArg;
    private String biomeProvider;
    private String biomeProviderArg;

    /**
     * Set the world name
     *
     * @param name New world name
     * @return {@code this}
     */
    public final @NonNull WorldConfigurationBuilder setName(final @NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Set the world type
     *
     * @param type New world type
     * @return {@code this}
     */
    public final @NonNull WorldConfigurationBuilder setType(final @NonNull WorldEnvironment type) {
        this.type = type;
        return this;
    }

    /**
     * Set the world settings
     *
     * @param settings New world settings
     * @return {@code this}
     */
    public final @NonNull WorldConfigurationBuilder setSettings(final @NonNull String settings) {
        this.settings = settings;
        return this;
    }

    /**
     * Set the world seed
     *
     * @param seed New seed
     * @return {@code this}
     */
    public final @NonNull WorldConfigurationBuilder setSeed(final long seed) {
        this.seed = seed;
        return this;
    }

    /**
     * Set whether structures should be generated
     *
     * @param generateStructures Whether structures should be generated
     * @return {@code this}
     */
    public @NonNull WorldConfigurationBuilder setGenerateStructures(final boolean generateStructures) {
        this.generateStructures = generateStructures;
        return this;
    }

    /**
     * Set whether hardcode mode should be enabled
     *
     * @param hardcore Whether hardcode mode should be enabled
     * @return {@code this}
     */
    public @NonNull WorldConfigurationBuilder setHardcore(final boolean hardcore) {
        this.hardcore = hardcore;
        return this;
    }

    /**
     * Set whether the bonus should be generated
     *
     * @param bonusChest Whether the bonus should be generated
     * @return {@code this}
     */
    public @NonNull WorldConfigurationBuilder setBonusChest(final boolean bonusChest) {
        this.bonusChest = bonusChest;
        return this;
    }

    /**
     * Set the generator
     *
     * @param generator New generator
     * @return {@code this}
     */
    public @NonNull WorldConfigurationBuilder setGenerator(final @Nullable String generator) {
        this.generator = generator;
        return this;
    }

    /**
     * Set the generator arguments
     *
     * @param generatorArg New generator arguments
     * @return {@code this}
     */
    public @NonNull WorldConfigurationBuilder setGeneratorArg(final @Nullable String generatorArg) {
        this.generatorArg = generatorArg;
        return this;
    }
    
    /**
     * Set the biome provider
     *
     * @param biomeProvider New biome provider
     * @return {@code this}
     */
    public @NonNull WorldConfigurationBuilder setBiomeProvider(final @Nullable String biomeProvider) {
        this.biomeProvider = biomeProvider;
        return this;
    }
    
    /**
     * Set the biome provider arguments
     *
     * @param biomeProviderArg New biome provider arguments
     * @return {@code this}
     */
    public @NonNull WorldConfigurationBuilder setBiomeProviderArg(final @Nullable String biomeProviderArg) {
        this.biomeProviderArg = biomeProviderArg;
        return this;
    }

    /**
     * Set the {@link WorldType}
     *
     * @param worldType New world features
     * @return {@code this}
     */
    public @NonNull WorldConfigurationBuilder setWorldFeatures(final @NonNull WorldType worldType) {
        this.worldType = worldType;
        return this;
    }

    /**
     * Create the {@link WorldConfiguration} instance from this builder
     *
     * @return Created instance
     */
    public @NonNull WorldConfiguration createWorldConfiguration() {
                
        return new WorldConfiguration(this.name, this.type, this.worldType, this.settings, 
                this.seed == null ? SeedUtil.randomSeed() : this.seed, this.generateStructures,
                this.hardcore, this.bonusChest, this.generator, this.generatorArg, 
                this.biomeProvider, this.biomeProviderArg
        );
    }

}
