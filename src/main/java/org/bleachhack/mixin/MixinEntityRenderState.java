/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import org.bleachhack.duck.EntityRenderStateAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;

@Mixin(EntityRenderState.class)
public class MixinEntityRenderState implements EntityRenderStateAccess {

	@Unique private Entity bleachhackEntity;

	@Override
	public Entity bleachhack$getEntity() {
		return bleachhackEntity;
	}

	@Override
	public void bleachhack$setEntity(Entity entity) {
		this.bleachhackEntity = entity;
	}

}
