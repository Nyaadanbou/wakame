package cc.mewcraft.wakame.mixin.support;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public final class MythicApiProvider {

    private static MythicApi instance = null;

    public static @NotNull MythicApi get() {
        MythicApi instance = MythicApiProvider.instance;
        if (instance == null) {
            throw new IllegalStateException("MythicMobsBridgeProvider has not been initialized");
        }
        return instance;
    }

    @ApiStatus.Internal
    public static void register(MythicApi instance) {
        MythicApiProvider.instance = instance;
    }

    @ApiStatus.Internal
    public static void unregister() {
        MythicApiProvider.instance = null;
    }
}
