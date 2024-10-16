package cc.mewcraft.wakame.item.template

interface ItemTemplateBridge<T : ItemTemplate<*>> {
    fun codec(id: String): ItemTemplateType<T>
}