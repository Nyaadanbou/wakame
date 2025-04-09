package cc.mewcraft.wakame.mixin.support;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public final class MythicMobsBridgeProvider {
    private static MythicMobsBridge instance = null;

    public static @NotNull MythicMobsBridge get() {
        MythicMobsBridge instance = MythicMobsBridgeProvider.instance;
        if (instance == null) {
            throw new IllegalStateException("MythicMobsBridgeProvider has not been initialized");
        }
        return instance;
    }

    @ApiStatus.Internal
    public static void register(MythicMobsBridge instance) {
        MythicMobsBridgeProvider.instance = instance;
    }

    @ApiStatus.Internal
    public static void unregister() {
        MythicMobsBridgeProvider.instance = null;
    }
}
