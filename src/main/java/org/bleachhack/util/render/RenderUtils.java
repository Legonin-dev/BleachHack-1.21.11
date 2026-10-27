/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.render;

public class RenderUtils {

	/**
	 * Minecraft 1.21+ text colors are ARGB, while the mod historically used plain RGB
	 * values (which have an alpha of 0 and would render completely invisible).
	 */
	public static int fixColor(int color) {
		return (color & 0xff000000) == 0 ? color | 0xff000000 : color;
	}
}
