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

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Objects;

/**
 * World creator instance used to create a {@link HyperWorld}
 */
public final class HyperWorldCreator extends WorldCreator {

    private final HyperWorld hyperWorld;

    @Inject
    public HyperWorldCreator(@Assisted final @NonNull HyperWorld hyperWorld) {
        super(Objects.requireNonNull(hyperWorld).getConfiguration().getName());
        this.hyperWorld = hyperWorld;
    }

    /**
     * Validate the world configuration
     *
     * @return Result of the validation
     */
    public @NonNull ValidationResult validate() {
        final WorldConfiguration worldConfiguration = this.hyperWorld.getConfiguration();
        if (worldConfiguration.getGenerator() != null
                && !worldConfiguration.getGenerator().equalsIgnoreCase("vanilla")) {
            final ChunkGenerator chunkGenerator =
                    getGeneratorForName(worldConfiguration.getName(), this.getGenerator(), null);
            if (chunkGenerator == null) {
                return ValidationResult.UNKNOWN_GENERATOR;
            }
        }
        if (worldConfiguration.getBiomeProvider() != null
                && !worldConfiguration.getBiomeProvider().equalsIgnoreCase("vanilla")) {
            final BiomeProvider biomeProvider =
                    getBiomeProviderForName(worldConfiguration.getName(), this.getBiomeProvider(), null);
            if (biomeProvider == null) {
                return ValidationResult.UNKNOWN_BIOME_PROVIDER;
            }
        }
        return ValidationResult.SUCCESS;
    }

    /**
     * Configure the world creator
     */
    public void configure() {
        final WorldConfiguration worldConfiguration = this.hyperWorld.getConfiguration();
        if (worldConfiguration.getWorldFeatures() != null) {
            this.type(worldConfiguration.getWorldFeatures().getBukkitType());
        } else {
            this.type(WorldType.NORMAL);
        }
        this.environment(worldConfiguration.getType().getBukkitType());
        this.generatorSettings(worldConfiguration.getSettings());
        this.seed(worldConfiguration.getSeed());
        this.generateStructures(worldConfiguration.isGenerateStructures());
        this.hardcore(worldConfiguration.isHardcore());
        this.bonusChest(worldConfiguration.isBonusChest());
        this.generator(this.getGenerator());
        this.biomeProvider(this.getBiomeProvider());
    }

    private @NonNull String getGenerator() {
        if (this.hyperWorld.getConfiguration().getGeneratorArg() == null) {
            return this.hyperWorld.getConfiguration().getGenerator();
        }
        return String.format("%s:%s", this.hyperWorld.getConfiguration().getGenerator(),
                this.hyperWorld.getConfiguration().getGeneratorArg()
        );
    }
    
    private @NonNull String getBiomeProvider() {
        if (this.hyperWorld.getConfiguration().getBiomeProviderArg() == null) {
            return this.hyperWorld.getConfiguration().getBiomeProvider();
        }
        return String.format("%s:%s", this.hyperWorld.getConfiguration().getBiomeProvider(),
                this.hyperWorld.getConfiguration().getBiomeProviderArg()
        );
    }

    /**
     * Result of configuration validation
     */
    public enum ValidationResult {
        SUCCESS,
        UNKNOWN_GENERATOR,
        UNKNOWN_BIOME_PROVIDER,
        NAME_TAKEN,
        UNKNOWN_ERROR
    }

}
