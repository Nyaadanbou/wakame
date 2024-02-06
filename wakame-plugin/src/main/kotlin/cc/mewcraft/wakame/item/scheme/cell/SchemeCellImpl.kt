package cc.mewcraft.wakame.item.scheme.cell

internal class SchemeCellImpl(
    override val keepEmpty: Boolean,
    override val canReforge: Boolean,
    override val canOverride: Boolean,
    override val coreSelector: SchemeCoreGroup,
    override val curseSelector: SchemeCurseGroup,
) : SchemeCell