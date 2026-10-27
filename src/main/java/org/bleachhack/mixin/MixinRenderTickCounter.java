/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.Timer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraft.client.render.RenderTickCounter;

@Mixin(RenderTickCounter.Dynamic.class)
public abstract class MixinRenderTickCounter {

	@Shadow private float dynamicDeltaTicks;
	@Shadow private float tickProgress;
	@Shadow private long lastTimeMillis;
	@Shadow @Final private float tickTime;
	@Shadow @Final private FloatUnaryOperator targetMillisPerTick;

	@Inject(method = "beginRenderTick(J)I", at = @At("HEAD"), cancellable = true)
	private void beginRenderTick(long timeMillis, CallbackInfoReturnable<Integer> ci) {
		Module timer = ModuleManager.getModule(Timer.class);

		if (timer.isEnabled()) {
			this.dynamicDeltaTicks = (float) (((timeMillis - this.lastTimeMillis) / this.targetMillisPerTick.apply(this.tickTime))
					* timer.getSetting(0).asSlider().getValue());
			this.lastTimeMillis = timeMillis;
			this.tickProgress += this.dynamicDeltaTicks;
			int i = (int) this.tickProgress;
			this.tickProgress -= i;

			ci.setReturnValue(i);
		}
	}

}
