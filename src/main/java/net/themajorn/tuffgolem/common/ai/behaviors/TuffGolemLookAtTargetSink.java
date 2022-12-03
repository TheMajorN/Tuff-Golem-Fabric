package net.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;

public class TuffGolemLookAtTargetSink extends Task<TuffGolemEntity> {
    public TuffGolemLookAtTargetSink(int p_23478_, int p_23479_) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_PRESENT), p_23478_, p_23479_);
    }

    protected boolean checkExtraStartConditions(ServerWorld serverLevel, TuffGolemEntity tuffGolem) {
        return !tuffGolem.isPetrified();
    }

    protected boolean shouldKeepRunning(ServerWorld p_23481_, TuffGolemEntity tuffGolem, long p_23483_) {
        return !tuffGolem.isPetrified() && tuffGolem.getBrain().getOptionalMemory(MemoryModuleType.LOOK_TARGET).filter((tracker) -> tracker.isSeenBy(tuffGolem)).isPresent();
    }

    protected void finishRunning(ServerWorld p_23492_, TuffGolemEntity p_23493_, long p_23494_) {
        p_23493_.getBrain().forget(MemoryModuleType.LOOK_TARGET);
    }

    protected void keepRunning(ServerWorld p_23503_, TuffGolemEntity p_23504_, long p_23505_) {
        p_23504_.getBrain().getOptionalMemory(MemoryModuleType.LOOK_TARGET).ifPresent((p_23486_) -> {
            p_23504_.getLookControl().lookAt(p_23486_.getPos());
        });
    }
}
