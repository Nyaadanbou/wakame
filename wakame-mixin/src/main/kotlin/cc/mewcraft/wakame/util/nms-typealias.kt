package cc.mewcraft.wakame.util

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

typealias MojangUnit = net.minecraft.util.Unit
typealias MojangResourceKey<T> = ResourceKey<T>
typealias MojangResourceLocation = ResourceLocation
typealias MojangRegistry<T> = Registry<T>
typealias MojangEnchantment = net.minecraft.world.item.enchantment.Enchantment
typealias MojangStack = net.minecraft.world.item.ItemStack
typealias MojangEntity = net.minecraft.world.entity.Entity
typealias MojangDamageSource = net.minecraft.world.damagesource.DamageSource
typealias MojangIngredient = net.minecraft.world.item.crafting.Ingredient
typealias MojangLootTable = net.minecraft.world.level.storage.loot.LootTable
typealias MojangLootParams = net.minecraft.world.level.storage.loot.LootParams
typealias MojangLootParamsBuilder = net.minecraft.world.level.storage.loot.LootParams.Builder