package cc.mewcraft.wakame.api;

import org.jetbrains.annotations.ApiStatus;

/**
 * 提供静态函数来获取 Nekoo 实例.
 */
public class NekooProvider {
    private static Nekoo nekoo = null;

    /**
     * 获取 Nekoo 实例.
     *
     * @return Nekoo 实例
     * @throws IllegalStateException 如果 Nekoo 尚未加载
     */
    public static Nekoo get() {
        if (nekoo == null) {
            throw new IllegalStateException("Nekoo is not loaded yet");
        }
        return nekoo;
    }

    @ApiStatus.Internal
    public static void register(Nekoo nekoo) {
        NekooProvider.nekoo = nekoo;
    }

    @ApiStatus.Internal
    public static void unregister() {
        NekooProvider.nekoo = null;
    }
}