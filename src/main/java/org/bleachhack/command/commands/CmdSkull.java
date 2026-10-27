/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command.commands;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import org.bleachhack.command.Command;
import org.bleachhack.command.CommandCategory;
import org.bleachhack.command.exception.CmdSyntaxException;
import org.bleachhack.util.BleachLogger;

import com.google.common.collect.HashMultimap;
import com.google.common.io.Resources;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class CmdSkull extends Command {

	public CmdSkull() {
		super("skull", "Gives you a player skull.", "skull <player> | skull img <image url>", CommandCategory.CREATIVE,
				"playerhead", "head");
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

		ItemStack item = new ItemStack(Items.PLAYER_HEAD, 64);

		if (args.length < 2) {
			try {
				JsonObject json = JsonParser.parseString(
						Resources.toString(new URL("https://api.mojang.com/users/profiles/minecraft/" + args[0]), StandardCharsets.UTF_8))
						.getAsJsonObject();

				JsonObject json2 = JsonParser.parseString(
						Resources.toString(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + json.get("id").getAsString()), StandardCharsets.UTF_8))
						.getAsJsonObject();

				String value = json2.get("properties").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();

				item.set(DataComponentTypes.PROFILE, ProfileComponent.ofStatic(createProfile(
						uuidFromUndashed(json.get("id").getAsString()), args[0], value)));
			} catch (Exception e) {
				e.printStackTrace();
				BleachLogger.error("Error getting head! (" + e.getClass().getSimpleName() + ")");
			}
		} else if (args[0].equalsIgnoreCase("img")) {
			item.set(DataComponentTypes.PROFILE, ProfileComponent.ofStatic(createProfile(
					UUID.randomUUID(), "BleachHead", encodeUrl(args[1]))));
			BleachLogger.logger.info("Set custom head texture");
		}

		mc.player.getInventory().insertStack(item);
	}

	private GameProfile createProfile(UUID id, String name, String texture) {
		PropertyMap properties = new PropertyMap(HashMultimap.create());
		properties.put("textures", new Property("textures", texture));

		return new GameProfile(id, name, properties);
	}

	private UUID uuidFromUndashed(String id) {
		return new UUID(
				Long.parseUnsignedLong(id.substring(0, 16), 16),
				Long.parseUnsignedLong(id.substring(16), 16));
	}

	private String encodeUrl(String url) {
		return Base64.getEncoder().encodeToString(("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}").getBytes());
	}

}
