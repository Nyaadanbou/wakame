package cc.mewcraft.wakame.ability2.meta

import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.adventure.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

class AbilityMeta(
    val id: Identifier,
    val dataConfig: AbilityMetaContainer,
) : Examinable {
    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("id", id),
    )

    override fun toString(): String = toSimpleString()
}