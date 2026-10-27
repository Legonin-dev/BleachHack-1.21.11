/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import org.bleachhack.BleachHack;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.util.render.FrustumUtils;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.buffers.GpuBufferSlice;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.memory.ObjectAllocator;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void render_head(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera,
			Matrix4f positionMatrix, Matrix4f projectionMatrix, Matrix4f frustumMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean bl,
			CallbackInfo callback) {
		Vec3d cameraPos = camera.getCameraPos();
		Frustum frustum = new Frustum(positionMatrix, frustumMatrix);
		frustum.setPosition(cameraPos.getX(), cameraPos.getY(), cameraPos.getZ());
		FrustumUtils.setFrustum(frustum);

		EventWorldRender.Pre event = new EventWorldRender.Pre(tickCounter.getTickProgress(false), matrixFrom(positionMatrix));
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			callback.cancel();
		}
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void render_return(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera,
			Matrix4f positionMatrix, Matrix4f projectionMatrix, Matrix4f frustumMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean bl,
			CallbackInfo callback) {
		EventWorldRender.Post event = new EventWorldRender.Post(tickCounter.getTickProgress(false), matrixFrom(positionMatrix));
		BleachHack.eventBus.post(event);
	}

	private static MatrixStack matrixFrom(Matrix4f matrix) {
		MatrixStack matrices = new MatrixStack();
		matrices.multiplyPositionMatrix(matrix);
		return matrices;
	}

}
