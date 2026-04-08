package cc.mewcraft.wakame.bridge;

import net.minecraft.core.component.DataComponentType;

import java.util.Optional;

public interface ExtraDataComponentPatch$Builder {

    <T> Optional<T> koish$get(DataComponentType<T> type);
}
