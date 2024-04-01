package cc.mewcraft.wakame.item.schema.cell

internal data class SchemaCellImpl(
    override val keepEmpty: Boolean,
    override val canReforge: Boolean,
    override val canOverride: Boolean,
    override val coreSelector: SchemaCoreGroup,
    override val curseSelector: SchemaCurseGroup,
) : SchemaCell