package cc.mewcraft.wakame.api;

import org.jetbrains.annotations.ApiStatus;

/**
 * 提供静态函数来获取 {@link Koish} 实例.
 */
public class KoishProvider {
    private static Koish koish = null;

    /**
     * 获取 {@link Koish} 实例.
     *
     * @return {@link Koish} 实例
     * @throws IllegalStateException 如果 {@link Koish} 尚未加载
     */
    public static Koish get() {
        if (koish == null) {
            throw new IllegalStateException("Koish is not loaded yet");
        }
        return koish;
    }

    @ApiStatus.Internal
    public static void register(Koish koish) {
        KoishProvider.koish = koish;
    }

    @ApiStatus.Internal
    public static void unregister() {
        KoishProvider.koish = null;
    }
}