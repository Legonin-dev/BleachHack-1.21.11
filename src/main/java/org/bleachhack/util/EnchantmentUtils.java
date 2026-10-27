/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;

/**
 * Small helpers to work with the component based enchantment api.
 */
public class EnchantmentUtils {

	public static int getLevel(RegistryKey<Enchantment> enchantment, ItemStack stack) {
		if (stack == null || stack.isEmpty())
			return 0;

		for (var e: EnchantmentHelper.getEnchantments(stack).getEnchantmentEntries()) {
			if (e.getKey().matchesKey(enchantment))
				return e.getIntValue();
		}

		return 0;
	}

	public static int getLevel(RegistryKey<Enchantment> enchantment, LivingEntity entity) {
		if (entity == null)
			return 0;

		int level = 0;

		for (EquipmentSlot slot: EquipmentSlot.values()) {
			level += getLevel(enchantment, entity.getEquippedStack(slot));
		}

		return level;
	}

	public static boolean isCurse(RegistryKey<Enchantment> enchantment) {
		return enchantment != null && enchantment.getValue().getPath().contains("curse");
	}

	public static boolean isCurse(net.minecraft.registry.entry.RegistryEntry<Enchantment> enchantment) {
		return enchantment != null && (enchantment.matchesKey(net.minecraft.enchantment.Enchantments.BINDING_CURSE)
				|| enchantment.matchesKey(net.minecraft.enchantment.Enchantments.VANISHING_CURSE));
	}

}
