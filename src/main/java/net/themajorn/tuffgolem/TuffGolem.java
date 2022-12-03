package net.themajorn.tuffgolem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.themajorn.tuffgolem.core.registry.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib3.GeckoLib;

public class TuffGolem implements ModInitializer {
	public static final String MOD_ID = "tuffgolem";
	public static final Logger LOGGER = LoggerFactory.getLogger("tuffgolem");

	@Override
	public void onInitialize() {

		ModActivities.registerActivities();
		ModMemoryModules.registerMemoryModules();
		ModSensors.registerSensors();
		ModSounds.registerSounds();

		GeckoLib.initialize();

		FabricDefaultAttributeRegistry.register(ModEntities.TUFF_GOLEM, TuffGolemEntity.setAttributes());
	}
}
