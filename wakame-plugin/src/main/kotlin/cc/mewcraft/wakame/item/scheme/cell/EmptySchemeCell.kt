package cc.mewcraft.wakame.item.scheme.cell

import cc.mewcraft.wakame.item.scheme.SchemeGenerationContext
import cc.mewcraft.wakame.item.scheme.core.SchemeCore
import cc.mewcraft.wakame.item.scheme.curse.SchemeCurse
import cc.mewcraft.wakame.random.Group

internal object EmptySchemeCell : SchemeCell {
    override val canReforge: Boolean = false
    override val canOverride: Boolean = false
    override val keepEmpty: Boolean = false
    override val coreSelector: Group<SchemeCore, SchemeGenerationContext> = Group.emptyGroup()
    override val curseSelector: Group<SchemeCurse, SchemeGenerationContext> = Group.emptyGroup()
}