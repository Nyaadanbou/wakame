package cc.mewcraft.wakame.mixin.support;

import net.kyori.adventure.key.Key;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface MythicApi {

    @Nullable
    EntityType getEntityType(Key id);

    void writeIdMark(Entity entity, Key id);
}
