/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import org.bleachhack.duck.BlockEntityRenderStateAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;

@Mixin(BlockEntityRenderState.class)
public class MixinBlockEntityRenderState implements BlockEntityRenderStateAccess {

	@Unique private BlockEntity bleachhackBlockEntity;

	@Override
	public BlockEntity bleachhack$getBlockEntity() {
		return bleachhackBlockEntity;
	}

	@Override
	public void bleachhack$setBlockEntity(BlockEntity blockEntity) {
		this.bleachhackBlockEntity = blockEntity;
	}

}
