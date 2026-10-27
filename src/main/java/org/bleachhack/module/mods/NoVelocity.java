/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventPlayerPushed;
import org.bleachhack.event.events.EventPacket;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.setting.module.SettingToggle;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

/**
 * @author sl First Module utilizing EventBus!
 */
public class NoVelocity extends Module {

	public NoVelocity() {
		super("NoVelocity", KEY_UNBOUND, ModuleCategory.PLAYER, "If you take some damage, you don't move.",
				new SettingToggle("Knockback", true).withDesc("Reduces knockback from other entities.").withChildren(
						new SettingSlider("VelXZ", 0, 100, 0, 1).withDesc("How much horizontal velocity to keep."),
						new SettingSlider("VelY", 0, 100, 0, 1).withDesc("How much vertical velocity  to keep.")),
				new SettingToggle("Explosions", true).withDesc("Reduces explosion velocity.").withChildren(
						new SettingSlider("VelXZ", 0, 100, 0, 1).withDesc("How much horizontal velocity to keep."),
						new SettingSlider("VelY", 0, 100, 0, 1).withDesc("How much vertical velocity to keep.")),
				new SettingToggle("Pushing", true).withDesc("Reduces how much you get pushed by entitie.s").withChildren(
						new SettingSlider("Amount", 0, 100, 0, 1).withDesc("How much pushing to keep.")),
				new SettingToggle("Fluids", true).withDesc("Reduces how much you get pushed from fluids."));
	}

	@BleachSubscribe
	public void onPlayerPushed(EventPlayerPushed event) {
		if (getSetting(2).asToggle().getState()) {
			double amount = getSetting(2).asToggle().getChild(0).asSlider().getValue() / 100d;
			event.setPushX(event.getPushX() * amount);
			event.setPushY(event.getPushY() * amount);
			event.setPushZ(event.getPushZ() * amount);
		}
	}

	@BleachSubscribe
	public void readPacket(EventPacket.Read event) {
		if (mc.player == null)
			return;

		if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket && getSetting(0).asToggle().getState()) {
			EntityVelocityUpdateS2CPacket packet = (EntityVelocityUpdateS2CPacket) event.getPacket();
			if (packet.getEntityId() == mc.player.getId()) {
				double velXZ = getSetting(0).asToggle().getChild(0).asSlider().getValue() / 100;
				double velY = getSetting(0).asToggle().getChild(1).asSlider().getValue() / 100;

				net.minecraft.util.math.Vec3d velocity = packet.getVelocity();
				double pvelX = (velocity.x / 8000d - mc.player.getVelocity().x) * velXZ;
				double pvelY = (velocity.y / 8000d - mc.player.getVelocity().y) * velY;
				double pvelZ = (velocity.z / 8000d - mc.player.getVelocity().z) * velXZ;

				event.setPacket(new EntityVelocityUpdateS2CPacket(packet.getEntityId(), new net.minecraft.util.math.Vec3d(
						pvelX + mc.player.getVelocity().x, pvelY + mc.player.getVelocity().y, pvelZ + mc.player.getVelocity().z)));
			}
		} else if (event.getPacket() instanceof ExplosionS2CPacket && getSetting(1).asToggle().getState()) {
			ExplosionS2CPacket packet = (ExplosionS2CPacket) event.getPacket();

			double velXZ = getSetting(1).asToggle().getChild(0).asSlider().getValue() / 100;
			double velY = getSetting(1).asToggle().getChild(1).asSlider().getValue() / 100;

			if (packet.playerKnockback().isPresent()) {
				net.minecraft.util.math.Vec3d knockback = packet.playerKnockback().get();
				event.setPacket(new ExplosionS2CPacket(packet.center(), packet.radius(), packet.blockCount(),
						java.util.Optional.of(new net.minecraft.util.math.Vec3d(knockback.x * velXZ, knockback.y * velY, knockback.z * velXZ)),
						packet.explosionParticle(), packet.explosionSound(), packet.blockParticles()));
			}
		}
	}

	// Fluid handling in MixinFlowableFluid.getVelocity_hasNext()
}
