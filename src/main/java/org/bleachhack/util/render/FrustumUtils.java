/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.render;

import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class FrustumUtils {

	private static Frustum frustum;

	public static void setFrustum(Frustum newFrustum) {
		frustum = newFrustum;
	}

	public static Frustum getFrustum() {
		return frustum;
	}

	public static boolean isBoxVisible(Box box) {
		return frustum == null || frustum.isVisible(box);
	}

	public static boolean isPointVisible(Vec3d vec) {
		return isPointVisible(vec.x, vec.y, vec.z);
	}

	public static boolean isPointVisible(double x, double y, double z) {
		return frustum == null || frustum.intersectPoint(x, y, z);
	}
}
