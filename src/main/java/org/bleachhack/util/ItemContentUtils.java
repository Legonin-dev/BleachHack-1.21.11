/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;

public class ItemContentUtils {

	public static List<ItemStack> getItemsInContainer(ItemStack item) {
		DefaultedList<ItemStack> items = DefaultedList.ofSize(27, ItemStack.EMPTY);
		ContainerComponent container = item.get(DataComponentTypes.CONTAINER);

		if (container != null) {
			container.copyTo(items);
		}

		return items;
	}

	public static List<List<String>> getTextInBook(ItemStack item) {
		List<String> pages = new ArrayList<>();

		if (item.getItem() == Items.WRITABLE_BOOK) {
			WritableBookContentComponent content = item.get(DataComponentTypes.WRITABLE_BOOK_CONTENT);

			if (content != null) {
				content.pages().forEach(page -> pages.add(page.raw()));
			}
		} else {
			WrittenBookContentComponent content = item.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);

			if (content != null) {
				content.getPages(false).forEach(page -> pages.add(page.getString()));
			}
		}

		List<List<String>> finalPages = new ArrayList<>();

		for (String s : pages) {
			String buffer = "";
			List<String> pageBuffer = new ArrayList<>();

			for (char c : s.toCharArray()) {
				if (MinecraftClient.getInstance().textRenderer.getWidth(buffer) > 114 || buffer.endsWith("\n")) {
					pageBuffer.add(buffer.replace("\n", ""));
					buffer = "";
				}

				buffer += c;
			}

			pageBuffer.add(buffer);
			finalPages.add(pageBuffer);
		}

		return finalPages;
	}
}
