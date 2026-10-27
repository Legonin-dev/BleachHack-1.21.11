/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.duck;

import net.minecraft.block.entity.BlockEntity;

/**
 * Exposes the block entity a render state was created from, since 1.21.11
 * render states don't keep a reference to their block entity.
 */
public interface BlockEntityRenderStateAccess {

	BlockEntity bleachhack$getBlockEntity();

	void bleachhack$setBlockEntity(BlockEntity blockEntity);

}
