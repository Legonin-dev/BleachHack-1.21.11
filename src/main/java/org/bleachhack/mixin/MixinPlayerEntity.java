/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import net.minecraft.registry.tag.FluidTags;
import org.bleachhack.BleachHack;
import org.bleachhack.event.events.EventReach;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.SpeedMine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity {

	private MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "getBlockBreakingSpeed", at = @At("HEAD"), cancellable = true)
	private void getBlockBreakingSpeed(BlockState block, CallbackInfoReturnable<Float> ci) {
		Module speedMine = ModuleManager.getModule(SpeedMine.class);

		if (speedMine.isEnabled()) {
			float breakingSpeed = getMainHandStack().getMiningSpeedMultiplier(block);

			if (breakingSpeed > 1.0F) {
				breakingSpeed += (float) getAttributeValue(EntityAttributes.MINING_EFFICIENCY);
			}

			if (StatusEffectUtil.hasHaste(this)) {
				breakingSpeed *= 1.0F + (StatusEffectUtil.getHasteAmplifier(this) + 1) * 0.2F;
			}

			if (!speedMine.getSetting(4).asToggle().getState()) {
				if (this.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
					float fatigueMult;
					switch (this.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
						case 0:
							fatigueMult = 0.3F;
							break;
						case 1:
							fatigueMult = 0.09F;
							break;
						case 2:
							fatigueMult = 0.0027F;
							break;
						case 3:
						default:
							fatigueMult = 8.1E-4F;
					}

					breakingSpeed *= fatigueMult;
				}
			}

			breakingSpeed *= (float) getAttributeValue(EntityAttributes.BLOCK_BREAK_SPEED);

			if (!speedMine.getSetting(5).asToggle().getState()) {
				if (this.isSubmergedIn(FluidTags.WATER)) {
					breakingSpeed *= (float) getAttributeValue(EntityAttributes.SUBMERGED_MINING_SPEED);
				}

				if (!this.isOnGround()) {
					breakingSpeed /= 5.0F;
				}
			}

			if (speedMine.getSetting(0).asMode().getMode() == 1)
				breakingSpeed *= speedMine.getSetting(3).asSlider().getValueFloat();

			ci.setReturnValue(breakingSpeed);
		}
	}

	@Inject(method = "getBlockInteractionRange", at = @At("RETURN"), cancellable = true)
	private void getBlockInteractionRange(CallbackInfoReturnable<Double> ci) {
		EventReach event = new EventReach(ci.getReturnValueD());
		BleachHack.eventBus.post(event);

		ci.setReturnValue(event.getReach());
	}

	@Inject(method = "getEntityInteractionRange", at = @At("RETURN"), cancellable = true)
	private void getEntityInteractionRange(CallbackInfoReturnable<Double> ci) {
		EventReach event = new EventReach(ci.getReturnValueD());
		BleachHack.eventBus.post(event);

		ci.setReturnValue(event.getReach());
	}
}
