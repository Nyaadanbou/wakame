package cc.mewcraft.wakame.hook.impl.thebrewingproject.integration.item;

import cc.mewcraft.wakame.api.Koish;
import cc.mewcraft.wakame.api.item.KoishItem;
import dev.jsinco.brewery.bukkit.api.integration.ItemIntegration;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;


/**
 * 实现了 {@link ItemIntegration} 以便让 TheBrewingProject 识别 Koish 的物品.
 * <p>
 * 在 TheBrewingProject 中这样使用:
 * - `koish:example` -> id 为 koish:example 的 Koish 物品
 * - `koish:dir/example` -> id 为 koish:dir/example 的 Koish 物品
 */
public class KoishIntegration implements ItemIntegration {

    private static final CompletableFuture<Void> INITIALIZED_FUTURE = CompletableFuture.completedFuture(null);

    @Override
    public Optional<ItemStack> createItem(String id) {
        // id 如果没有命名空间, 则会自动加上 "koish" 的命名空间
        return Optional.ofNullable(Koish.get().getItemRegistry().getOrNull(id))
                .map(KoishItem::createItemStack);
    }

    @Override
    public boolean isIngredient(String id) {
        // id 如果没有命名空间, 则会自动加上 "koish" 的命名空间
        return Koish.get().getItemRegistry().getOrNull(id) != null;
    }

    @Override
    public @Nullable Component displayName(String id) {
        // id 如果没有命名空间, 则会自动加上 "koish" 的命名空间
        return Optional.ofNullable(Koish.get().getItemRegistry().getOrNull(id))
                .map(KoishItem::getName)
                .orElse(null);
    }

    @Override
    public @Nullable String getItemId(ItemStack itemStack) {
        return Optional.ofNullable(Koish.get().getItemRegistry().getOrNull(itemStack))
                .map(KoishItem::getId)
                .map(Key::value)
                .map(str -> str.replace('/', ':'))
                .orElse(null);
    }

    @Override
    public CompletableFuture<Void> initialized() {
        return INITIALIZED_FUTURE;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getId() {
        return "koish";
    }

}
