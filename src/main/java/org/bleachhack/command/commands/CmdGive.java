/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.math.NumberUtils;
import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.command.exception.CmdSyntaxException;
import org.bleachhack.util.BleachLogger;

import java.util.Locale;

public class CmdGive extends Command {

	public CmdGive() {
		super("give", "Gives you an item.", "give <item> <count> <damage> <nbt>", CommandCategory.CREATIVE);
	}

	@Override
	public void onCommand(String alias, String[] args) throws Exception {
		if (!mc.interactionManager.getCurrentGameMode().isCreative()) {
			BleachLogger.error("Not In Creative Mode!");
			return;
		}

		if (args.length == 0) {
			throw new CmdSyntaxException();
		}

		if (args[0].equalsIgnoreCase("preset")) {
			// The old presets were built from raw item NBT (negative damage, oversized stacks,
			// bookban pages). Raw item NBT was replaced by data components in 1.21, so there is
			// no equivalent for these exploit items anymore.
			BleachLogger.error("Item presets are not supported on 1.21.11.");
			return;
		}

		ItemStack item = new ItemStack(
				Registries.ITEM.get(Identifier.of("minecraft:" + args[0].toLowerCase(Locale.ENGLISH))));

		if (item.getItem() instanceof AirBlockItem)
			throw new CmdSyntaxException();

		if (args.length >= 2 && NumberUtils.isCreatable(args[1]))
			item.setCount(NumberUtils.createNumber(args[1]).intValue());
		if (args.length >= 3 && NumberUtils.isCreatable(args[2]))
			item.setDamage(NumberUtils.createNumber(args[2]).intValue());
		if (args.length >= 4)
			try {
				NbtComponent.set(DataComponentTypes.CUSTOM_DATA, item, StringNbtReader.readCompound(args[3]));
			} catch (CommandSyntaxException ignored) {
			}

		mc.player.getInventory().insertStack(item);
	}

}
