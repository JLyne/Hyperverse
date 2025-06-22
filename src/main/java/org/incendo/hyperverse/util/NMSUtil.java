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

package org.incendo.hyperverse.util;

import java.lang.reflect.Field;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public final class NMSUtil {

    private Field entitySectionManager;
    private Field entityLookup;
    private org.apache.logging.log4j.core.Logger worldServerLogger;

    @Nullable public Location findBedRespawn(@NotNull final Location spawnLocation) {
        final CraftWorld craftWorld = (CraftWorld) spawnLocation.getWorld();
        if (craftWorld == null) {
            return null;
        }

        ServerPlayer.RespawnConfig respawnConfig = new ServerPlayer.RespawnConfig(
                craftWorld.getHandle().dimension(),
                new BlockPos(spawnLocation.getBlockX(), spawnLocation.getBlockY(), spawnLocation.getBlockZ()),
                0, true);

        return ServerPlayer.findRespawnAndUseSpawnBlock(craftWorld.getHandle(), respawnConfig, false)
                .map(ServerPlayer.RespawnPosAngle::position)
                .map(pos -> new Location(spawnLocation.getWorld(), pos.x(), pos.y(), pos.z()))
                .orElse(null);
    }
}
