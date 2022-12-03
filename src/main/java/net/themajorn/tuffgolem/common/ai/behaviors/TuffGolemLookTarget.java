package net.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;

import java.util.Optional;
import java.util.function.Predicate;

public class TuffGolemLookTarget extends Task<TuffGolemEntity> {
    private final Predicate<LivingEntity> predicate;
    private final float maxDistSqr;
    private Optional<LivingEntity> nearestEntityMatchingTest = Optional.empty();

    public TuffGolemLookTarget(Predicate<LivingEntity> entities, float maxDist) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.VISIBLE_MOBS, MemoryModuleState.VALUE_PRESENT));
        this.predicate = entities;
        this.maxDistSqr = maxDist * maxDist;
    }

    protected boolean shouldRun(ServerWorld serverLevel, TuffGolemEntity tuffGolem) {
        LivingTargetCache nearestvisiblelivingentities = tuffGolem.getBrain().getOptionalMemory(MemoryModuleType.VISIBLE_MOBS).get();
        this.nearestEntityMatchingTest = nearestvisiblelivingentities.findFirst(this.predicate.and((p_186053_) -> p_186053_.squaredDistanceTo(tuffGolem) <= (double)this.maxDistSqr));
        return this.nearestEntityMatchingTest.isPresent() && !tuffGolem.isPetrified();
    }

    protected void run(ServerWorld serverLevel, TuffGolemEntity tuffGolem, long l) {
        tuffGolem.getBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(this.nearestEntityMatchingTest.get(), true));
        this.nearestEntityMatchingTest = Optional.empty();
    }
}