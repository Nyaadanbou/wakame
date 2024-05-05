package cc.mewcraft.wakame.display

import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * Represents something that can provide [TagResolver].
 */
interface TagResolverProvider {
    /**
     * Generates [TagResolver] for play.
     *
     * @return an instance of [TagResolver]
     */
    fun provideTagResolverForPlay(): TagResolver

    /**
     * Generates [TagResolver] for show.
     *
     * @return an instance of [TagResolver]
     */
    fun provideTagResolverForShow(): TagResolver
}