package cc.mewcraft.wakame.util.nms

import cc.mewcraft.wakame.mixin.core.InvokerBundleContents
import cc.mewcraft.wakame.mixin.core.InvokerChargedProjectiles
import com.google.common.collect.Lists
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.BundleContents
import net.minecraft.world.item.component.ChargedProjectiles
import net.minecraft.world.item.component.ItemContainerContents

fun ItemContainerContents.isNotEmpty(): Boolean = this.items.isEmpty().not()
fun ItemContainerContents.copyItems(): List<ItemStack> = Lists.transform(this.items, ItemStack::copy)

fun BundleContents.isNotEmpty(): Boolean = this.isEmpty.not()
@Suppress("CAST_NEVER_SUCCEEDS")
fun BundleContents.copyItems(): List<ItemStack> = Lists.transform((this as InvokerBundleContents).items(), ItemStack::copy)

@Suppress("CAST_NEVER_SUCCEEDS")
fun ChargedProjectiles.isNotEmpty(): Boolean = (this as InvokerChargedProjectiles).items().isEmpty().not()
fun ChargedProjectiles.copyItems(): List<ItemStack> = this.items
