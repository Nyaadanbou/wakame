/*
 * This file is part of helper, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package cc.mewcraft.wakame.util.metadata;

import cc.mewcraft.wakame.KoishHolder;
import cc.mewcraft.wakame.api.Koish;
import cc.mewcraft.wakame.util.event.Events;
import cc.mewcraft.wakame.util.metadata.type.BlockMetadataRegistry;
import cc.mewcraft.wakame.util.metadata.type.EntityMetadataRegistry;
import cc.mewcraft.wakame.util.metadata.type.PlayerMetadataRegistry;
import cc.mewcraft.wakame.util.metadata.type.WorldMetadataRegistry;
import io.papermc.paper.math.Position;
import io.papermc.paper.util.Tick;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jspecify.annotations.NullMarked;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides access to {@link MetadataRegistry} instances bound to players, entities, blocks and worlds.
 */
@NullMarked
public final class Metadata {

    private static final AtomicBoolean SETUP = new AtomicBoolean(false);

    // lazily load
    private static void ensureSetup() {
        if (SETUP.get()) {
            return;
        }

        if (!SETUP.getAndSet(true)) {

            // remove player metadata when they leave the server
            Events.subscribe(PlayerQuitEvent.class, EventPriority.MONITOR)
                    .handler(e -> StandardMetadataRegistries.PLAYER.remove(e.getPlayer().getUniqueId()));

            // cache housekeeping task
            Bukkit.getScheduler().runTaskTimer(KoishHolder.INSTANCE, () -> {
                for (MetadataRegistry<?> registry : StandardMetadataRegistries.values()) {
                    registry.cleanup();
                }
            }, 0L, Tick.tick().fromDuration(Duration.ofMinutes(1)));
        }
    }

    /**
     * Gets the {@link MetadataRegistry} for {@link Player}s.
     *
     * @return the {@link PlayerMetadataRegistry}
     */
    public static PlayerMetadataRegistry players() {
        ensureSetup();
        return StandardMetadataRegistries.PLAYER;
    }

    /**
     * Gets the {@link MetadataRegistry} for {@link Entity}s.
     *
     * @return the {@link EntityMetadataRegistry}
     */
    public static EntityMetadataRegistry entities() {
        ensureSetup();
        return StandardMetadataRegistries.ENTITY;
    }

    /**
     * Gets the {@link MetadataRegistry} for {@link Block}s.
     *
     * @return the {@link BlockMetadataRegistry}
     */
    public static BlockMetadataRegistry blocks() {
        ensureSetup();
        return StandardMetadataRegistries.BLOCK;
    }

    /**
     * Gets the {@link MetadataRegistry} for {@link World}s.
     *
     * @return the {@link WorldMetadataRegistry}
     */
    public static WorldMetadataRegistry worlds() {
        ensureSetup();
        return StandardMetadataRegistries.WORLD;
    }

    /**
     * Produces a {@link MetadataMap} for the given object.
     * <p>
     * A map will only be returned if the object is an instance of
     * {@link Player}, {@link UUID}, {@link Entity}, {@link Block} or {@link World}.
     *
     * @param obj the object
     * @return a metadata map
     */
    public static MetadataMap provide(Object obj) {
        Objects.requireNonNull(obj, "obj");
        if (obj instanceof Player) {
            return provideForPlayer(((Player) obj));
        } else if (obj instanceof UUID) {
            return provideForPlayer(((UUID) obj));
        } else if (obj instanceof Entity) {
            return provideForEntity(((Entity) obj));
        } else if (obj instanceof Block) {
            return provideForBlock(((Block) obj));
        } else if (obj instanceof World) {
            return provideForWorld(((World) obj));
        } else {
            throw new IllegalArgumentException("Unknown object type: " + obj.getClass());
        }
    }

    /**
     * Gets a {@link MetadataMap} for the given object, if one already exists and has
     * been cached in this registry.
     * <p>
     * A map will only be returned if the object is an instance of
     * {@link Player}, {@link UUID}, {@link Entity}, {@link Block} or {@link World}.
     *
     * @param obj the object
     * @return a metadata map
     */
    public static Optional<MetadataMap> get(Object obj) {
        Objects.requireNonNull(obj, "obj");
        if (obj instanceof Player) {
            return getForPlayer(((Player) obj));
        } else if (obj instanceof UUID) {
            return getForPlayer(((UUID) obj));
        } else if (obj instanceof Entity) {
            return getForEntity(((Entity) obj));
        } else if (obj instanceof Block) {
            return getForBlock(((Block) obj));
        } else if (obj instanceof World) {
            return getForWorld(((World) obj));
        } else {
            throw new IllegalArgumentException("Unknown object type: " + obj.getClass());
        }
    }

    public static MetadataMap provideForPlayer(UUID uuid) {
        return players().provide(uuid);
    }

    public static MetadataMap provideForPlayer(Player player) {
        return players().provide(player);
    }

    public static Optional<MetadataMap> getForPlayer(UUID uuid) {
        return players().get(uuid);
    }

    public static Optional<MetadataMap> getForPlayer(Player player) {
        return players().get(player);
    }

    public static <T> Map<Player, T> lookupPlayersWithKey(MetadataKey<T> key) {
        return players().getAllWithKey(key);
    }

    public static MetadataMap provideForEntity(UUID uuid) {
        return entities().provide(uuid);
    }

    public static MetadataMap provideForEntity(Entity entity) {
        return entities().provide(entity);
    }

    public static Optional<MetadataMap> getForEntity(UUID uuid) {
        return entities().get(uuid);
    }

    public static Optional<MetadataMap> getForEntity(Entity entity) {
        return entities().get(entity);
    }

    public static <T> Map<Entity, T> lookupEntitiesWithKey(MetadataKey<T> key) {
        return entities().getAllWithKey(key);
    }

    public static MetadataMap provideForBlock(Position block) {
        return blocks().provide(block);
    }

    public static MetadataMap provideForBlock(Block block) {
        return blocks().provide(block);
    }

    public static Optional<MetadataMap> getForBlock(Position block) {
        return blocks().get(block);
    }

    public static Optional<MetadataMap> getForBlock(Block block) {
        return blocks().get(block);
    }

    public static <T> Map<Position, T> lookupBlocksWithKey(MetadataKey<T> key) {
        return blocks().getAllWithKey(key);
    }

    public static MetadataMap provideForWorld(UUID uid) {
        return worlds().provide(uid);
    }

    public static MetadataMap provideForWorld(World world) {
        return worlds().provide(world);
    }

    public static Optional<MetadataMap> getForWorld(UUID uid) {
        return worlds().get(uid);
    }

    public static Optional<MetadataMap> getForWorld(World world) {
        return worlds().get(world);
    }

    public static <T> Map<World, T> lookupWorldsWithKey(MetadataKey<T> key) {
        return worlds().getAllWithKey(key);
    }

    private Metadata() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
