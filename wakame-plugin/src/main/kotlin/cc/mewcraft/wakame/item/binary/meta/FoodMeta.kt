package cc.mewcraft.wakame.item.binary.meta

import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.NoopLoreLine
import cc.mewcraft.wakame.item.ItemMetaConstants
import cc.mewcraft.wakame.item.schema.meta.Food
import cc.mewcraft.wakame.util.CompoundShadowTag
import cc.mewcraft.wakame.util.getCompoundOrNull
import cc.mewcraft.wakame.util.toStableFloat
import cc.mewcraft.wakame.util.toStableInt
import me.lucko.helper.nbt.ShadowTagType
import net.kyori.adventure.key.Key
import org.bukkit.potion.PotionEffect

@JvmInline
value class BFoodMeta(
    private val accessor: ItemMetaAccessor,
) : BinaryItemMeta<Food> {
    companion object {
        private const val NUTRITION_TAG = "nutrition"
        private const val SATURATION_TAG = "saturation"
        private const val IS_MEAT_TAG = "is_meat"
        private const val CAN_ALWAYS_EAT_TAG = "can_always_eat"
        private const val EAT_SECONDS_TAG = "eat_seconds"
        private const val EFFECTS_TAG = "effects"
    }

    override val key: Key
        get() = ItemMetaConstants.createKey { FOOD }
    override val exists: Boolean
        get() = accessor.rootOrNull?.contains(ItemMetaConstants.FOOD, ShadowTagType.COMPOUND) ?: false

    /**
     * Gets the value of `nutrition`.
     */
    fun nutrition(): Int {
        return accessor.rootOrNull?.getCompoundOrNull(key.value())?.getInt(NUTRITION_TAG) ?: 0
    }

    /**
     * Sets the value of `nutrition`.
     *
     * @throws IllegalStateException
     */
    fun nutrition(value: Int) {
        val compound = accessor.rootOrNull?.getCompoundOrNull(key.value())
        if (compound != null) {
            compound.putInt(NUTRITION_TAG, value)
        } else throw IllegalStateException("Can't set 'nutrition' for empty food")
    }

    /**
     * Gets the value of `saturation`.
     */
    fun saturation(): Float {
        return accessor.rootOrNull?.getCompoundOrNull(key.value())?.getFloat(SATURATION_TAG) ?: 0F
    }

    /**
     * Sets the value of `saturation`.
     *
     * @throws IllegalStateException
     */
    fun saturation(value: Float) {
        val compound = accessor.rootOrNull?.getCompoundOrNull(key.value())
        if (compound != null) {
            compound.putFloat(SATURATION_TAG, value)
        } else throw IllegalStateException("Can't set 'saturation' for empty food")
    }

    /**
     * Gets the value of `is meat`.
     */
    fun isMeat(): Boolean {
        return accessor.rootOrNull?.getCompoundOrNull(key.value())?.getBoolean(IS_MEAT_TAG) ?: false
    }

    /**
     * Sets the value of `is meat`.
     *
     * @throws IllegalStateException
     */
    fun isMeat(value: Boolean) {
        val compound = accessor.rootOrNull?.getCompoundOrNull(key.value())
        if (compound != null) {
            compound.putBoolean(IS_MEAT_TAG, value)
        } else throw IllegalStateException("Can't set 'isMeat' for empty food")
    }

    /**
     * Gets the value of `can always eat`.
     */
    fun canAlwaysEat(): Boolean {
        return accessor.rootOrNull?.getCompoundOrNull(key.value())?.getBoolean(CAN_ALWAYS_EAT_TAG) ?: false
    }

    /**
     * Sets the value of `can always eat`.
     *
     * @throws IllegalStateException
     */
    fun canAlwaysEat(value: Boolean) {
        val compound = accessor.rootOrNull?.getCompoundOrNull(key.value())
        if (compound != null) {
            compound.putBoolean(CAN_ALWAYS_EAT_TAG, value)
        } else throw IllegalStateException("Can't set 'canAlwaysEat' for empty food")
    }

    /**
     * Gets the value of `eat seconds`.
     */
    fun eatSeconds(): Float {
        return accessor.rootOrNull?.getCompoundOrNull(key.value())?.getFloat(EAT_SECONDS_TAG) ?: 0F
    }

    /**
     * Sets the value of `eat seconds`.
     *
     * @throws IllegalStateException
     */
    fun eatSeconds(value: Float) {
        val compound = accessor.rootOrNull?.getCompoundOrNull(key.value())
        if (compound != null) {
            compound.putFloat(EAT_SECONDS_TAG, value)
        } else throw IllegalStateException("Can't set 'eatSeconds' for empty food")
    }

    /**
     * Gets the value of `effects`.
     */
    fun effects(): Map<PotionEffect, Float> {
        // TODO 等待组件相关API的到来
        return emptyMap()
    }

    /**
     * Sets the value of `effects`.
     *
     * @throws IllegalStateException
     */
    fun effects(value: Map<PotionEffect, Float>) {
        val compound = accessor.rootOrNull?.getCompoundOrNull(key.value())
        if (compound != null) {
            // TODO 等待组件相关API的到来
        } else throw IllegalStateException("Can't set 'effects' for empty food")
    }

    override fun getOrNull(): Food? {
        // TODO 等待组件相关API的到来
        return accessor.rootOrNull?.getCompoundOrNull(key.value())?.let { compound ->
            val nutrition = compound.getInt(NUTRITION_TAG)
            val saturation = compound.getFloat(SATURATION_TAG)
            val isMeat = compound.getBoolean(IS_MEAT_TAG)
            val canAlwaysEat = compound.getBoolean(CAN_ALWAYS_EAT_TAG)
            val eatSeconds = compound.getFloat(EAT_SECONDS_TAG)
            val effects: Map<PotionEffect, Float> = emptyMap()
            Food(nutrition, saturation, isMeat, canAlwaysEat, eatSeconds, effects)
        }
    }

    override fun set(value: Food) {
        // TODO 等待组件相关API的到来
        accessor.rootOrCreate.put(key.value(), CompoundShadowTag {
            putInt(NUTRITION_TAG, value.nutrition.toStableInt())
            putFloat(SATURATION_TAG, value.saturation.toStableFloat())
            putBoolean(IS_MEAT_TAG, value.isMeat)
            putBoolean(CAN_ALWAYS_EAT_TAG, value.canAlwaysEat)
            putFloat(EAT_SECONDS_TAG, value.eatSeconds)
            // putMap effects
        })
    }

    override fun remove() {
        accessor.rootOrNull?.remove(key.value())
    }

    override fun provideDisplayLore(): LoreLine {
        // TODO("等待组件相关API的到来")
        return NoopLoreLine
    }
}