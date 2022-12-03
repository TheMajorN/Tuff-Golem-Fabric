package net.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.passive.GoatBrain;
import net.minecraft.server.world.ServerWorld;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;

import java.util.function.Function;
import java.util.function.Predicate;

public class TuffGolemWalkTargetFromLookTarget extends Task<TuffGolemEntity> {
    private final Function<LivingEntity, Float> speedModifier;
    private final int closeEnoughDistance;
    private final Predicate<LivingEntity> canSetWalkTargetPredicate;

    public TuffGolemWalkTargetFromLookTarget(float p_24084_, int p_24085_) {
        this((p_182369_) -> {
            return true;
        }, (p_182364_) -> {
            return p_24084_;
        }, p_24085_);
    }

    public TuffGolemWalkTargetFromLookTarget(Predicate<LivingEntity> p_182359_, Function<LivingEntity, Float> p_182360_, int p_182361_) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.LOOK_TARGET, MemoryModuleState.VALUE_PRESENT));
        this.speedModifier = p_182360_;
        this.closeEnoughDistance = p_182361_;
        this.canSetWalkTargetPredicate = p_182359_;
    }

    protected boolean shouldRun(ServerWorld serverLevel, TuffGolemEntity tuffGolem) {
        return this.canSetWalkTargetPredicate.test(tuffGolem) && !tuffGolem.isPetrified();
    }

    protected void run(ServerWorld serverLevel, TuffGolemEntity tuffGolem, long l) {
        Brain<?> brain = tuffGolem.getBrain();
        LookTarget positiontracker = brain.getOptionalMemory(MemoryModuleType.LOOK_TARGET).get();
        brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(positiontracker, this.speedModifier.apply(tuffGolem), this.closeEnoughDistance));
    }
}