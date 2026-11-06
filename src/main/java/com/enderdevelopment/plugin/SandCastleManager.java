package com.enderdevelopment.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public final class SandCastleManager implements Listener {
    private static final int SIZE = 10;
    private static final int WALL_HEIGHT = 4;
    private static final long LIFETIME_TICKS = 600L;
    private static final double TELEPORT_RADIUS_SQUARED = 100.0;
    private static final Material CASTLE_MATERIAL = Material.SAND;

    private final Main plugin;
    private final Map<UUID, Castle> castles = new HashMap<>();
    private final Set<BlockKey> protectedBlocks = new HashSet<>();

    public SandCastleManager(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSandPlaced(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() == CASTLE_MATERIAL) {
            createCastle(event.getBlockPlaced(), event.getPlayer());
        }
    }

    @EventHandler
    public void onProtectedBlockBreak(BlockBreakEvent event) {
        if (protectedBlocks.contains(BlockKey.from(event.getBlock()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplosion(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> protectedBlocks.contains(BlockKey.from(block)));
    }

    @EventHandler
    public void onBlockExplosion(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> protectedBlocks.contains(BlockKey.from(block)));
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.getBlocks().stream().anyMatch(block -> protectedBlocks.contains(BlockKey.from(block)))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.getBlocks().stream().anyMatch(block -> protectedBlocks.contains(BlockKey.from(block)))) {
            event.setCancelled(true);
        }
    }

    private void createCastle(Block trigger, Player owner) {
        Castle castle = new Castle(trigger.getLocation().add(0.5, 0.0, 0.5));
        castle.blocks.add(BlockKey.from(trigger));
        buildCastle(castle, trigger);
        castles.put(castle.id, castle);
        protectedBlocks.addAll(castle.blocks);

        owner.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 600, 1, false, true, true));
        teleportNearbyPlayers(castle);
        castle.removalTask = Bukkit.getScheduler().runTaskLater(plugin, () -> removeCastle(castle), LIFETIME_TICKS);
    }

    private void buildCastle(Castle castle, Block trigger) {
        World world = trigger.getWorld();
        int minX = trigger.getX() - 4;
        int minZ = trigger.getZ() - 4;
        int baseY = trigger.getY();
        int maxX = minX + SIZE - 1;
        int maxZ = minZ + SIZE - 1;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (x == minX || x == maxX || z == minZ || z == maxZ) {
                    for (int y = baseY; y < baseY + WALL_HEIGHT; y++) {
                        placeIfEmpty(castle, world.getBlockAt(x, y, z));
                    }
                } else {
                    placeIfEmpty(castle, world.getBlockAt(x, baseY - 1, z));
                }
            }
        }

        for (int x : new int[] {minX, maxX}) {
            for (int z : new int[] {minZ, maxZ}) {
                for (int y = baseY + WALL_HEIGHT; y < baseY + WALL_HEIGHT + 2; y++) {
                    placeIfEmpty(castle, world.getBlockAt(x, y, z));
                }
            }
        }
    }

    private void placeIfEmpty(Castle castle, Block block) {
        if (block.getType().isAir()) {
            block.setType(CASTLE_MATERIAL, false);
            castle.blocks.add(BlockKey.from(block));
        }
    }

    private void teleportNearbyPlayers(Castle castle) {
        Location center = castle.center;
        for (Player player : center.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(center) <= TELEPORT_RADIUS_SQUARED) {
                player.teleport(center.clone().add(0.0, 1.0, 0.0));
            }
        }
    }

    private void removeCastle(Castle castle) {
        if (!castles.remove(castle.id, castle)) {
            return;
        }
        if (castle.removalTask != null) {
            castle.removalTask.cancel();
        }
        for (BlockKey key : castle.blocks) {
            protectedBlocks.remove(key);
            Block block = key.getBlock();
            if (block != null && block.getType() == CASTLE_MATERIAL) {
                block.setType(Material.AIR, false);
            }
        }
    }

    public void removeAllCastles() {
        for (Castle castle : new ArrayList<>(castles.values())) {
            removeCastle(castle);
        }
        protectedBlocks.clear();
    }

    private static final class Castle {
        private final UUID id = UUID.randomUUID();
        private final Location center;
        private final Set<BlockKey> blocks = new HashSet<>();
        private BukkitTask removalTask;

        private Castle(Location center) {
            this.center = center;
        }
    }

    private record BlockKey(UUID worldId, int x, int y, int z) {
        private static BlockKey from(Block block) {
            return new BlockKey(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
        }

        private Block getBlock() {
            World world = Bukkit.getWorld(worldId);
            return world == null ? null : world.getBlockAt(x, y, z);
        }
    }
}
