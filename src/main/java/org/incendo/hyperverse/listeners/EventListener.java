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

import io.papermc.paper.block.bed.BedEnterAction;
import io.papermc.paper.block.bed.BedRuleResult;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.hyperverse.configuration.HyperConfiguration;
import org.incendo.hyperverse.configuration.Messages;
import org.incendo.hyperverse.database.HyperDatabase;
import org.incendo.hyperverse.database.LocationType;
import org.incendo.hyperverse.database.PersistentLocation;
import org.incendo.hyperverse.events.PlayerSeekSpawnEvent;
import org.incendo.hyperverse.events.PlayerSetSpawnEvent;
import org.incendo.hyperverse.flags.implementation.EndFlag;
import org.incendo.hyperverse.flags.implementation.GamemodeFlag;
import org.incendo.hyperverse.flags.implementation.LocalRespawnFlag;
import org.incendo.hyperverse.flags.implementation.NetherFlag;
import org.incendo.hyperverse.flags.implementation.PveFlag;
import org.incendo.hyperverse.flags.implementation.RespawnWorldFlag;
import org.incendo.hyperverse.modules.HyperEventFactory;
import org.incendo.hyperverse.util.MessageUtil;
import org.incendo.hyperverse.world.HyperWorld;
import org.incendo.hyperverse.world.WorldManager;
import org.incendo.hyperverse.world.WorldType;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public final class EventListener implements Listener {

    private final WorldManager worldManager;
    private final HyperDatabase hyperDatabase;
    private final HyperConfiguration hyperConfiguration;
    private final Plugin plugin;
    private final HyperEventFactory hyperEventFactory;

    @Inject
    public EventListener(
            final @NonNull WorldManager worldManager,
            final @NonNull HyperDatabase hyperDatabase,
            final @NonNull HyperConfiguration hyperConfiguration,
            final @NonNull HyperEventFactory hyperEventFactory,
            final @NonNull PluginManager pluginManager,
            final @NonNull BukkitScheduler scheduler,
            final @NonNull Plugin plugin
    ) {
        this.worldManager = worldManager;
        this.hyperDatabase = hyperDatabase;
        this.hyperEventFactory = hyperEventFactory;
        this.hyperConfiguration = hyperConfiguration;
        this.plugin = plugin;

        pluginManager.registerEvents(new PaperListener(this.worldManager), plugin);
    }

    @EventHandler
    public void onPlayerLogin(final @NonNull AsyncPlayerPreLoginEvent event) {
        if (this.hyperConfiguration.shouldPersistLocations()) {
            try {
                this.hyperDatabase.getLocations(event.getUniqueId()).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onTeleport(final @NonNull PlayerTeleportEvent event) {
        if (this.hyperConfiguration.shouldPersistLocations()) {
            final Location from = event.getFrom();
            final Location to = event.getTo();
            if (Objects.equals(from.getWorld(), Objects.requireNonNull(to).getWorld())) {
                // The player was moving inside of the world, so we don't
                // need to update the location
                return;
            }
            // The player moved between two different worlds, so we
            // need to update
            final UUID uuid = event.getPlayer().getUniqueId();
            this.hyperDatabase.storeLocation(PersistentLocation.fromLocation(uuid, from,
                    LocationType.PLAYER_LOCATION
            ), true, false);
            this.hyperDatabase.storeLocation(PersistentLocation.fromLocation(uuid, to,
                    LocationType.PLAYER_LOCATION
            ), true, false);
        }
    }

    @EventHandler
    public void onPlayerJoin(final @NonNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final HyperWorld hyperWorld = this.worldManager.getWorld(player.getWorld());
        if (hyperWorld == null) {
            return;
        }

        this.setDefaultGameMode(player, hyperWorld);
    }

    @EventHandler
    public void onPlayerQuit(final @NonNull PlayerQuitEvent event) {
        if (this.hyperConfiguration.shouldPersistLocations()) {
            // Persist the locations when the player quits
            final UUID uuid = event.getPlayer().getUniqueId();
            this.hyperDatabase.storeLocation(
                    PersistentLocation.fromLocation(uuid, event.getPlayer().getLocation(),
                            LocationType.PLAYER_LOCATION
                    ), false,
                    true
            );
            this.hyperDatabase.clearLocations(uuid);
        }
    }

    @EventHandler
    public void onWorldChange(final @NonNull PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();
        final HyperWorld hyperWorld = this.worldManager.getWorld(player.getWorld());
        if (hyperWorld == null) {
            return;
        }

        this.setDefaultGameMode(player, hyperWorld);
    }

    private boolean setDefaultGameMode(final @NonNull Player player, final @NonNull HyperWorld world) {
        if (player.hasPermission("hyperverse.override.gamemode")) {
            if (world.getFlag(GamemodeFlag.class) != player.getGameMode()) {
                MessageUtil.sendMessage(player, Messages.messageGameModeOverride, "%mode%",
                        world.getFlag(GamemodeFlag.class).name().toLowerCase()
                );
            }
            return false;
        }
        player.setGameMode(world.getFlag(GamemodeFlag.class));
        return true;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onRespawn(final @NonNull PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final HyperWorld hyperWorld = this.worldManager.getWorld(player.getWorld());
        if (hyperWorld == null) {
            return;
        }

        if (hyperWorld.getConfiguration().getType() == WorldType.END
                && event.getPlayer().getLocation().getBlock().getType() == Material.END_PORTAL) {
            final Location destination =
                    hyperWorld.getTeleportationManager().endDestination(event.getPlayer());
            if (destination != null) {
                final boolean allowedEntry = hyperWorld.getTeleportationManager()
                        .allowedTeleport(event.getPlayer(), destination).getNow(false);
                if (!allowedEntry) {
                    MessageUtil.sendMessage(event.getPlayer(), Messages.messageNotPermittedEntry);
                } else {
                    event.setRespawnLocation(destination);
                }
            }
            return;
        }

        Location spawnLocation = event.getRespawnLocation();

        if (hyperWorld.getFlag(LocalRespawnFlag.class)) {
            spawnLocation = hyperWorld.getTeleportationManager().getSpawnLocation(player, hyperWorld);
        } else if (!hyperWorld.getFlag(RespawnWorldFlag.class).isEmpty()) {
            final HyperWorld respawnWorld = this.worldManager.getWorld(hyperWorld.getFlag(RespawnWorldFlag.class));
            if (respawnWorld != null) {
                spawnLocation = respawnWorld.getTeleportationManager().getSpawnLocation(player, respawnWorld);
            } else {
                MessageUtil.sendMessage(player, Messages.messageRespawnWorldNonExistent);
            }
        }

        final PlayerSeekSpawnEvent seekSpawnEvent = this.hyperEventFactory.callPlayerSeekSpawn(
                player,
                hyperWorld,
                spawnLocation
        );
        if (seekSpawnEvent.isCancelled()) {
            return;
        }

        event.setRespawnLocation(seekSpawnEvent.getRespawnLocation());
    }

    @EventHandler
    public void onEntityDamageEvent(final @NonNull EntityDamageByEntityEvent event) {
        final HyperWorld hyperWorld = this.worldManager.getWorld(event.getEntity().getWorld());
        if (hyperWorld == null) {
            return;
        }
        final Entity first = event.getEntity();
        final Entity second = event.getDamager();
        if (first.getType() == EntityType.PLAYER || second.getType() == EntityType.PLAYER) {
            if (first.getType() != second.getType()) {
                if (!hyperWorld.getFlag(PveFlag.class)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerPortalEvent(final @NonNull PlayerPortalEvent event) {
        final HyperWorld hyperWorld = this.worldManager.getWorld(event.getPlayer().getWorld());
        if (hyperWorld == null) {
            this.plugin.getLogger().warning(String.format(
                    "(PlayerPortalEvent) Player %s entered world '%s' but no world could be found",
                    event.getPlayer().getName(), event.getPlayer().getWorld().getName()
            ));
            return;
        }

        final Location destination;
        final boolean isNether;

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            destination = hyperWorld.getTeleportationManager()
                    .netherDestination(event.getPlayer(), event.getFrom());
            isNether = true;
        } else if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            Location portalLocation;
            final Location current = event.getFrom();
            final DragonBattle battle = current.getWorld().getEnderDragonBattle();
            if (battle != null && (portalLocation = battle.getEndPortalLocation()) != null) {
                current.clone().setY(portalLocation.getY());
                if (portalLocation.distanceSquared(current) > 9) {
                    return;
                }
            }
            destination = hyperWorld.getTeleportationManager().endDestination(event.getPlayer());
            isNether = false;
        } else {
            return;
        }

        if (destination != null) {
            final boolean allowedEntry =
                    hyperWorld.getTeleportationManager().allowedTeleport(event.getPlayer(), destination)
                            .getNow(false);
            if (!allowedEntry) {
                MessageUtil.sendMessage(event.getPlayer(), Messages.messageNotPermittedEntry);
            } else {
                event.setTo(destination);
            }
        } else {
            final String flag =
                    isNether ? hyperWorld.getFlag(NetherFlag.class) : hyperWorld.getFlag(EndFlag.class);
            if (!flag.isEmpty()) {
                event.setCancelled(
                        true); // We do not want to allow default teleportation unless it has
                // been configured
                MessageUtil.sendMessage(event.getPlayer(), Messages.messagePortalNotLinked);
            }
        }
    }

    @EventHandler
    public void onEntityPortalEvent(final @NonNull EntityPortalEvent event) {
        final HyperWorld hyperWorld =
                this.worldManager.getWorld(Objects.requireNonNull(event.getFrom().getWorld()));
        if (hyperWorld == null) {
            return;
        }

        Location destination = null;
        boolean isNether = false;

        if (event.getPortalType() == PortalType.NETHER) {
            isNether = true;
            destination = hyperWorld.getTeleportationManager()
                 .netherDestination(event.getEntity(), event.getFrom());
        } else if (event.getPortalType() == PortalType.ENDER) {
            Location portalLocation;
            final Location current = event.getFrom();
            final DragonBattle battle = current.getWorld().getEnderDragonBattle();
            if (battle != null && (portalLocation = battle.getEndPortalLocation()) != null) {
                current.clone().setY(portalLocation.getY());
                if (portalLocation.distanceSquared(current) > 9) {
                    return;
                }
            }

            destination = hyperWorld.getTeleportationManager()
                 .endDestination(event.getEntity());
        }

        if (destination != null) {
            event.setTo(destination);
        } else {
            final String flag =
                    isNether ? hyperWorld.getFlag(NetherFlag.class) : hyperWorld.getFlag(EndFlag.class);
            if (!flag.isEmpty()) {
                event.setCancelled(true); // We do not want to allow default teleportation unless it has
                // been configured
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSleep(final @NonNull PlayerBedEnterEvent event) {
        if (!this.hyperConfiguration.shouldPersistLocations()) {
            return;
        }

        final @NotNull BedEnterAction bedEnterAction = event.enterAction();
        if (bedEnterAction.problem() != null || bedEnterAction.canSleep() != BedRuleResult.ALLOWED
                || bedEnterAction.canSetSpawn() != BedRuleResult.ALLOWED) {
            return;
        }

        final HyperWorld hyperWorld = this.worldManager.getWorld(event.getBed().getWorld());
        if (hyperWorld == null) {
            return;
        }

        final PlayerSetSpawnEvent playerSetSpawnEvent = this.hyperEventFactory.callPlayerSetSpawn(event.getPlayer(), hyperWorld);
        if (playerSetSpawnEvent.isCancelled()) {
            return;
        }

        this.hyperDatabase.storeLocation(PersistentLocation.fromLocation(event.getPlayer().getUniqueId(),
                event.getBed().getLocation(), LocationType.BED_SPAWN
        ), true, false);
    }

}
