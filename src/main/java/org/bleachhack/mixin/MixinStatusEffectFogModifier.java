/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.NoRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.fog.StatusEffectFogModifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;

@Mixin(StatusEffectFogModifier.class)
public abstract class MixinStatusEffectFogModifier {

	@Inject(method = "shouldApply", at = @At("HEAD"), cancellable = true)
	private void shouldApply(CameraSubmersionType type, Entity entity, CallbackInfoReturnable<Boolean> ci) {
		if (((StatusEffectFogModifier) (Object) this).getStatusEffect().equals(StatusEffects.BLINDNESS)
				&& ModuleManager.getModule(NoRender.class).isOverlayToggled(0)) {
			ci.setReturnValue(false);
		}
	}
}
