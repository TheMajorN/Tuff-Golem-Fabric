package net.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.themajorn.tuffgolem.common.ai.TuffGolemAi;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.themajorn.tuffgolem.core.registry.ModMemoryModules;

public class PetrifiedTime extends Task<TuffGolemEntity> {

    private final UniformIntProvider timeBetweenAnimateAndPetrify;

    public PetrifiedTime(UniformIntProvider betweenPetrify) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED,
                ModMemoryModules.MID_ANIMATE_OR_PETRIFY, MemoryModuleState.VALUE_PRESENT), 100);
        this.timeBetweenAnimateAndPetrify = betweenPetrify;
    }

    protected boolean shouldKeepRunning(ServerWorld serverLevel, TuffGolemEntity mob, long l) {
        return mob.isOnGround() && !mob.isSubmergedInWater() && !mob.isInLava() && !mob.stateLocked() && TuffGolemAi.isIdle(mob);
    }

    protected void run(ServerWorld serverLevel, TuffGolemEntity mob, long l) {

    }

    protected void finishRunning(ServerWorld serverLevel, TuffGolemEntity mob, long l) {
        mob.getBrain().forget(ModMemoryModules.MID_ANIMATE_OR_PETRIFY);
        mob.getBrain().remember(ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS, this.timeBetweenAnimateAndPetrify.get(serverLevel.random));
    }
}