package cc.mewcraft.wakame.mixin.support;

import net.kyori.adventure.key.Key;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface MythicApi {
    
    EntityType getEntityType(Key id);

    void writeMobId(Entity entity, Key id);
}
