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

package org.incendo.hyperverse.platform.v1_21_1;

import co.aikar.taskchain.TaskChainFactory;
import com.google.inject.Inject;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityLookup;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.RegexFilter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.incendo.hyperverse.util.NMS;

@SuppressWarnings("unused")
public class NMSImpl implements NMS {

    private Field entitySectionManager;
    private Field entityLookup;
    private org.apache.logging.log4j.core.Logger worldServerLogger;

    @Override @Nullable public Location findBedRespawn(@NotNull final Location spawnLocation) {
        final CraftWorld craftWorld = (CraftWorld) spawnLocation.getWorld();
        if (craftWorld == null) {
            return null;
        }

        return ServerPlayer.findRespawnAndUseSpawnBlock(craftWorld.getHandle(), new BlockPos(spawnLocation.getBlockX(),
                        spawnLocation.getBlockY(), spawnLocation.getBlockZ()), 0, true, false)
                .map(ServerPlayer.RespawnPosAngle::position)
                .map(pos -> new Location(spawnLocation.getWorld(), pos.x(), pos.y(), pos.z()))
                .orElse(null);
    }
}
