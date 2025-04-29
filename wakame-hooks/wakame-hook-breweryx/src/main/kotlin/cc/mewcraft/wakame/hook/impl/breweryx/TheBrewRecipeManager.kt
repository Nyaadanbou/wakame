package cc.mewcraft.wakame.hook.impl.breweryx

import cc.mewcraft.wakame.brewery.BrewRecipe
import cc.mewcraft.wakame.brewery.BrewRecipeManager
import com.dre.brewery.recipe.BRecipe
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import java.time.Duration
import java.time.temporal.ChronoUnit


/**
 * 使用 BreweryX 实现的 [BrewRecipeManager].
 */
object TheBrewRecipeManager : BrewRecipeManager {

    // 由于 BreweryX 暂时没提供 API 来监听其是否执行了 reload,
    // 所以这里的缓存策略设置为定时自动刷新,
    // 即: 5 分钟刷新一次; 10 分钟后过期
    private val lookupCache: LoadingCache<String, BrewRecipe?> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.of(10, ChronoUnit.MINUTES)) // expire 要大于 refresh 否则没意义
        .refreshAfterWrite(Duration.of(5, ChronoUnit.MINUTES))
        .build { id ->
            BRecipe.getById(id)?.let(TheBrewRecipeAdapter::adapt)
        }

    override fun get(id: String): BrewRecipe? {
        return lookupCache.get(id)
    }

    override fun random(): BrewRecipe? {
        val recipe = BRecipe.getRecipes().randomOrNull() ?: return null
        return get(recipe.id)
    }

    override fun iterator(): Iterator<BrewRecipe> {
        val recipes = BRecipe.getRecipes()
        val sequence = recipes
            .asSequence()
            .mapNotNull { recipe -> get(recipe.id) }
        return sequence.iterator()
    }
}