/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.duck;

import net.minecraft.entity.Entity;

/**
 * Exposes the entity a render state was created from, since 1.21.11 render
 * states don't keep a reference to their entity.
 */
public interface EntityRenderStateAccess {

	Entity bleachhack$getEntity();

	void bleachhack$setEntity(Entity entity);

}
