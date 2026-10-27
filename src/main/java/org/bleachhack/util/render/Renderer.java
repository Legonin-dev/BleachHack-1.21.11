/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.render;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.bleachhack.util.Boxes;
import org.bleachhack.util.render.color.LineColor;
import org.bleachhack.util.render.color.QuadColor;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RotationAxis;

public class Renderer {

	private static final RenderLayer LINES_NO_DEPTH = createLinesNoDepth();

	// RenderLayer.of is package private in 1.21.11, so the layer is created through
	// a lookup in the (unnamed) game module. Falls back to the depth tested lines
	// if that is not possible.
	private static RenderLayer createLinesNoDepth() {
		try {
			RenderPipeline pipeline = RenderPipeline.builder()
					.withLocation("pipeline/bleachhack_lines_no_depth")
					.withVertexShader("core/rendertype_lines")
					.withFragmentShader("core/rendertype_lines")
					.withBlend(BlendFunction.TRANSLUCENT)
					.withCull(false)
					.withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
					.withDepthWrite(false)
					.withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH, VertexFormat.DrawMode.LINES)
					.build();

			RenderSetup setup = RenderSetup.builder(pipeline).translucent().build();

			return (RenderLayer) MethodHandles.privateLookupIn(RenderLayer.class, MethodHandles.lookup())
					.findStatic(RenderLayer.class, "of", MethodType.methodType(RenderLayer.class, String.class, RenderSetup.class))
					.invoke("bleachhack_lines_no_depth", setup);
		} catch (Throwable e) {
			return RenderLayers.lines();
		}
	}

	// -------------------- Fill + Outline Boxes --------------------

	public static void drawBoxBoth(BlockPos blockPos, QuadColor color, float lineWidth, Direction... excludeDirs) {
		drawBoxBoth(new Box(blockPos), color, lineWidth, excludeDirs);
	}

	public static void drawBoxBoth(Box box, QuadColor color, float lineWidth, Direction... excludeDirs) {
		QuadColor outlineColor = color.clone();
		outlineColor.overwriteAlpha(255);

		drawBoxBoth(box, color, outlineColor, lineWidth, excludeDirs);
	}

	public static void drawBoxBoth(BlockPos blockPos, QuadColor fillColor, QuadColor outlineColor, float lineWidth, Direction... excludeDirs) {
		drawBoxBoth(new Box(blockPos), fillColor, outlineColor, lineWidth, excludeDirs);
	}

	public static void drawBoxBoth(Box box, QuadColor fillColor, QuadColor outlineColor, float lineWidth, Direction... excludeDirs) {
		drawBoxFill(box, fillColor, excludeDirs);
		drawBoxOutline(box, outlineColor, lineWidth, excludeDirs);
	}

	// -------------------- Fill Boxes --------------------

	public static void drawBoxFill(BlockPos blockPos, QuadColor color, Direction... excludeDirs) {
		drawBoxFill(new Box(blockPos), color, excludeDirs);
	}

	public static void drawBoxFill(Box box, QuadColor color, Direction... excludeDirs) {
		if (!FrustumUtils.isBoxVisible(box)) {
			return;
		}

		MatrixStack matrices = matrixFrom(box.minX, box.minY, box.minZ);

		BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		Vertexer.vertexBoxQuads(matrices, buffer, Boxes.moveToZero(box), color, excludeDirs);
		drawFilled(buffer);
	}

	// -------------------- Outline Boxes --------------------

	public static void drawBoxOutline(BlockPos blockPos, QuadColor color, float lineWidth, Direction... excludeDirs) {
		drawBoxOutline(new Box(blockPos), color, lineWidth, excludeDirs);
	}

	public static void drawBoxOutline(Box box, QuadColor color, float lineWidth, Direction... excludeDirs) {
		if (!FrustumUtils.isBoxVisible(box)) {
			return;
		}

		MatrixStack matrices = matrixFrom(box.minX, box.minY, box.minZ);

		BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH);
		Vertexer.vertexBoxLines(matrices, buffer, Boxes.moveToZero(box), color, lineWidth, excludeDirs);
		drawLines(buffer);
	}

	// -------------------- Quads --------------------

	public static void drawQuadFill(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, int cullMode, QuadColor color) {
		if (!FrustumUtils.isPointVisible(x1, y1, z1) && !FrustumUtils.isPointVisible(x2, y2, z2)
				&& !FrustumUtils.isPointVisible(x3, y3, z3) && !FrustumUtils.isPointVisible(x4, y4, z4)) {
			return;
		}

		MatrixStack matrices = matrixFrom(x1, y1, z1);

		BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		Vertexer.vertexQuad(matrices, buffer,
				0f, 0f, 0f,
				(float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1),
				(float) (x3 - x1), (float) (y3 - y1), (float) (z3 - z1),
				(float) (x4 - x1), (float) (y4 - y1), (float) (z4 - z1),
				cullMode, color);
		drawFilled(buffer);
	}

	public static void drawQuadOutline(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, float lineWidth, QuadColor color) {
		if (!FrustumUtils.isPointVisible(x1, y1, z1) && !FrustumUtils.isPointVisible(x2, y2, z2)
				&& !FrustumUtils.isPointVisible(x3, y3, z3) && !FrustumUtils.isPointVisible(x4, y4, z4)) {
			return;
		}

		MatrixStack matrices = matrixFrom(x1, y1, z1);

		BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH);

		int[] colors = color.getAllColors();

		Vertexer.vertexLine(matrices, buffer, 0f, 0f, 0f, (float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1), LineColor.gradient(colors[0], colors[1]), lineWidth);
		Vertexer.vertexLine(matrices, buffer, (float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1), (float) (x3 - x1), (float) (y3 - y1), (float) (z3 - z1), LineColor.gradient(colors[1], colors[2]), lineWidth);
		Vertexer.vertexLine(matrices, buffer, (float) (x3 - x1), (float) (y3 - y1), (float) (z3 - z1), (float) (x4 - x1), (float) (y4 - y1), (float) (z4 - z1), LineColor.gradient(colors[2], colors[3]), lineWidth);
		Vertexer.vertexLine(matrices, buffer, (float) (x4 - x1), (float) (y4 - y1), (float) (z4 - z1), 0f, 0f, 0f, LineColor.gradient(colors[3], colors[0]), lineWidth);
		drawLines(buffer);
	}

	// -------------------- Lines --------------------

	public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, LineColor color, float width) {
		drawLine(x1, y1, z1, x2, y2, z2, color, width, false);
	}

	public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, LineColor color, float width, boolean depthTest) {
		if (!FrustumUtils.isPointVisible(x1, y1, z1) && !FrustumUtils.isPointVisible(x2, y2, z2)) {
			return;
		}

		MatrixStack matrices = matrixFrom(x1, y1, z1);

		BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR_NORMAL_LINE_WIDTH);
		Vertexer.vertexLine(matrices, buffer, 0f, 0f, 0f, (float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1), color, width);
		drawLines(buffer, depthTest);
	}

	// -------------------- Utils --------------------

	public static void drawFilled(BufferBuilder buffer) {
		BuiltBuffer built = buffer.endNullable();
		if (built != null) {
			RenderLayers.debugFilledBox().draw(built);
		}
	}

	public static void drawLines(BufferBuilder buffer) {
		drawLines(buffer, true);
	}

	public static void drawLines(BufferBuilder buffer, boolean depthTest) {
		BuiltBuffer built = buffer.endNullable();
		if (built != null) {
			(depthTest ? RenderLayers.lines() : LINES_NO_DEPTH).draw(built);
		}
	}

	public static MatrixStack matrixFrom(double x, double y, double z) {
		MatrixStack matrices = new MatrixStack();

		Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

		matrices.translate(x - camera.getCameraPos().x, y - camera.getCameraPos().y, z - camera.getCameraPos().z);

		return matrices;
	}

	public static Vec3d getInterpolationOffset(Entity e) {
		if (MinecraftClient.getInstance().isPaused()) {
			return Vec3d.ZERO;
		}

		double tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false);
		return new Vec3d(
				e.getX() - MathHelper.lerp(tickDelta, e.lastRenderX, e.getX()),
				e.getY() - MathHelper.lerp(tickDelta, e.lastRenderY, e.getY()),
				e.getZ() - MathHelper.lerp(tickDelta, e.lastRenderZ, e.getZ()));
	}

	public static void setup() {
		// Pipeline state is now defined by the render layer used for drawing.
	}

	public static void cleanup() {
	}
}
