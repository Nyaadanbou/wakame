package cc.mewcraft.wakame.reforge.common

import org.bukkit.inventory.ItemStack

interface PriceModifier {
    val name: String
    val expression: String
    val operation: Operation

    fun evaluate(item: ItemStack): Double

    enum class Operation {
        ADD_VALUE,
        ADD_MULTIPLIED_BASE,
        ADD_MULTIPLIED_TOTAL,
        ;

        fun byName(name: String): Operation? {
            return when (name) {
                "add_value" -> ADD_VALUE
                "add_multiplied_base" -> ADD_MULTIPLIED_BASE
                "add_multiplied_total" -> ADD_MULTIPLIED_TOTAL
                else -> null
            }
        }
    }
}
