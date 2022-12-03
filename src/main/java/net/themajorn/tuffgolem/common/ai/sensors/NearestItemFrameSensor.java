package net.themajorn.tuffgolem.common.ai.sensors;

import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.themajorn.tuffgolem.core.registry.ModMemoryModules;

import java.util.*;
import java.util.stream.Stream;

public class NearestItemFrameSensor extends Sensor<MobEntity> {
    private static final long XZ_RANGE = 32L;
    private static final long Y_RANGE = 16L;
    public static final int MAX_DISTANCE_TO_WANTED_ITEM = 32;

    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.of(ModMemoryModules.NEAREST_VISIBLE_ITEM_FRAME);
    }

    protected void sense(ServerWorld level, MobEntity mob) {
        Brain<?> brain = mob.getBrain();
        List<ItemFrameEntity> list = level.getEntitiesByClass(ItemFrameEntity.class, mob.getBoundingBox().expand(32.0D, 16.0D, 32.0D), (entity) -> true);
        list.sort(Comparator.comparingDouble(mob::squaredDistanceTo));
        Optional<ItemFrameEntity> optional = list.stream().filter((itemFrame) -> {
                    if (!itemFrame.getHeldItemStack().isEmpty()) {
                        return mob.canGather(itemFrame.getHeldItemStack());
                    }
                    return !mob.canGather(itemFrame.getHeldItemStack());
                })
                .filter((itemFrame) -> {
                    if (!itemFrame.getHeldItemStack().isEmpty()) {
                        return itemFrame.isInRange(mob, 32.0D);
                    }
                    return false;
                })
                .filter(mob::canSee).findFirst();
        brain.remember(ModMemoryModules.NEAREST_VISIBLE_ITEM_FRAME, optional);
    }
}
