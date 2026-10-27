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
import org.bleachhack.BleachHack;
import org.bleachhack.event.events.EventBlockEntityRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRenderManager;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;

@Mixin(BlockEntityRenderManager.class)
public class MixinBlockEntityRenderManager {

	@Inject(method = "getRenderState", at = @At("RETURN"))
	private <E extends BlockEntity, S extends BlockEntityRenderState> void getRenderState(E blockEntity, float tickDelta,
			ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay, CallbackInfoReturnable<S> ci) {
		if (ci.getReturnValue() != null) {
			((BlockEntityRenderStateAccess) ci.getReturnValue()).bleachhack$setBlockEntity(blockEntity);
		}
	}

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private <S extends BlockEntityRenderState> void render(S state, MatrixStack matrices, OrderedRenderCommandQueue queue,
			CameraRenderState cameraState, CallbackInfo ci) {
		BlockEntity blockEntity = ((BlockEntityRenderStateAccess) state).bleachhack$getBlockEntity();

		if (blockEntity == null) {
			return;
		}

		EventBlockEntityRender.Single.Pre event = new EventBlockEntityRender.Single.Pre(blockEntity, matrices, null);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			ci.cancel();
		}
	}

}
