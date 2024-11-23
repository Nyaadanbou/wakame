package cc.mewcraft.wakame.damage

/**
 * 创建一个 [DamageTags].
 */
fun DamageTags(vararg tags: DamageTag): DamageTags {
    return DamageTagsImpl(*tags)
}

/**
 * 创建一个 [DamageTags].
 */
fun DamageTags(tags: List<DamageTag>): DamageTags {
    return DamageTagsImpl(tags)
}

internal object DefaultDamageTagsFactory : DamageTagsFactory {
    override fun create(vararg tags: DamageTag): DamageTags {
        return DamageTagsImpl(*tags)
    }

    override fun create(tags: List<DamageTag>): DamageTags {
        return DamageTagsImpl(tags)
    }
}

private class DamageTagsImpl(
    vararg tags: DamageTag,
) : DamageTags {
    private val tagSet: MutableSet<DamageTag> = mutableSetOf(*tags)

    constructor(tags: List<DamageTag>) : this(*tags.toTypedArray())

    override fun isEmpty(): Boolean {
        return tagSet.isEmpty()
    }

    override fun add(tag: DamageTag): Boolean {
        return tagSet.add(tag)
    }

    override fun remove(tag: DamageTag): Boolean {
        return tagSet.remove(tag)
    }

    override fun contains(tag: DamageTag): Boolean {
        return tag in tagSet
    }

    override fun tags(): Set<DamageTag> {
        return tagSet.toSet()
    }
}