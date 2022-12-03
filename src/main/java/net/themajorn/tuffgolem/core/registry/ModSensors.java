package net.themajorn.tuffgolem.core.registry;

import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.sensor.TemptationsSensor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.themajorn.tuffgolem.TuffGolem;
import net.themajorn.tuffgolem.common.ai.TuffGolemAi;
import net.themajorn.tuffgolem.common.ai.sensors.NearestItemFrameSensor;

import java.util.function.Supplier;

public class ModSensors<U extends Sensor<?>> {

    public static final SensorType<NearestItemFrameSensor> NEAREST_ITEM_FRAMES = register("nearest_item_frames", NearestItemFrameSensor::new);

    public static final SensorType<TemptationsSensor> TUFF_GOLEM_TEMPTATIONS = register("tuff_golem_temptations",
            () -> new TemptationsSensor(TuffGolemAi.getTemptItems()));

    private static <U extends Sensor<?>> SensorType<U> register(String id, Supplier<U> factory) {
        return Registry.register(Registry.SENSOR_TYPE, new Identifier(TuffGolem.MOD_ID, id), new SensorType<U>(factory));
    }

    public static void registerSensors() {
        TuffGolem.LOGGER.debug("Registering Sensors for " + TuffGolem.MOD_ID);
    }
}
