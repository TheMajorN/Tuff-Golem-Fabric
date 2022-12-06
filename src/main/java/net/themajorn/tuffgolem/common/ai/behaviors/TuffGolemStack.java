package net.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import net.themajorn.tuffgolem.TuffGolem;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.themajorn.tuffgolem.core.registry.ModMemoryModules;

import java.util.Optional;

public class TuffGolemStack extends Task<TuffGolemEntity> {
    private static final int BREED_RANGE = 3;
    private static final int MIN_DURATION = 60;
    private static final int MAX_DURATION = 110;
    private final EntityType<? extends TuffGolemEntity> partnerType;
    private final float speedModifier;
    private long stackAtTime;

    public TuffGolemStack(EntityType<? extends TuffGolemEntity> entityType, float speedMod) {
        super(ImmutableMap.of(
                MemoryModuleType.VISIBLE_MOBS, MemoryModuleState.VALUE_PRESENT,
                ModMemoryModules.STACK_TARGET, MemoryModuleState.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED), 110);
        this.partnerType = entityType;
        this.speedModifier = speedMod;
    }

    protected boolean shouldRun(ServerWorld serverLevel, TuffGolemEntity tuffGolem) {
        return  tuffGolem.wantsToStack()
                && !tuffGolem.isPetrified()
                && this.findValidStackPartner(tuffGolem).isPresent()
                && tuffGolem.getBottomTuffGolem(tuffGolem).getNumOfTuffGolemsAbove(tuffGolem, 1) < tuffGolem.getMaxStackSize();
    }

    protected void run(ServerWorld serverLevel, TuffGolemEntity tuffGolem, long l) {
        TuffGolemEntity stackTarget = this.findValidStackPartner(tuffGolem).get();
        tuffGolem.getBrain().remember(ModMemoryModules.STACK_TARGET, stackTarget);
        stackTarget.getBrain().remember(ModMemoryModules.STACK_TARGET, tuffGolem);
        LookTargetUtil.lookAtAndWalkTowardsEachOther(tuffGolem, stackTarget, this.speedModifier);
        int i = 60 + tuffGolem.getRandom().nextInt(50);
        this.stackAtTime = l + (long) i;
    }

    protected boolean shouldKeepRunning(ServerWorld serverLevel, TuffGolemEntity tuffGolem, long l) {
        if (!this.hasStackTargetOfRightType(tuffGolem)) {
            return false;
        } else {
            TuffGolemEntity stackTarget = this.getStackTarget(tuffGolem);
            return stackTarget.isAlive() && !tuffGolem.isPetrified() && LookTargetUtil.canSee(tuffGolem.getBrain(), stackTarget) && l <= this.stackAtTime;
        }
    }

    protected void keepRunning(ServerWorld serverLevel, TuffGolemEntity tuffGolem, long l) {
        TuffGolemEntity stackTarget = this.getStackTarget(tuffGolem);
        LookTargetUtil.lookAtAndWalkTowardsEachOther(tuffGolem, stackTarget, this.speedModifier);
        if (tuffGolem.isInRange(stackTarget, 2.0D)) {
            if (l >= this.stackAtTime && !serverLevel.isClient) {
                tuffGolem.startRiding(stackTarget);
                tuffGolem.setBodyYaw(stackTarget.getBodyYaw());
                tuffGolem.resetDimensionState();
                tuffGolem.setMountedHeightOffset(0.9D);
                tuffGolem.getBrain().forget(ModMemoryModules.STACK_TARGET);
                stackTarget.setHeightDimensionState(stackTarget.getNumOfTuffGolemsAbove(stackTarget, 1));
                stackTarget.setWidthDimensionState(2);
                stackTarget.setMountedHeightOffset(stackTarget.getNumOfTuffGolemsAbove(stackTarget, 1) - 0.1);
                stackTarget.getBrain().forget(ModMemoryModules.STACK_TARGET);
            }
        }
    }

    protected void finishRunning(ServerWorld serverLevel, TuffGolemEntity tuffGolem, long l) {
        tuffGolem.getBrain().forget(ModMemoryModules.STACK_TARGET);
        tuffGolem.getBrain().forget(MemoryModuleType.WALK_TARGET);
        tuffGolem.getBrain().forget(MemoryModuleType.LOOK_TARGET);
        tuffGolem.resetWantsToStack();
        this.stackAtTime = 0L;
    }

    private TuffGolemEntity getStackTarget(TuffGolemEntity tuffGolem) {
        return tuffGolem.getBrain().getOptionalMemory(ModMemoryModules.STACK_TARGET).get();
    }

    private boolean hasStackTargetOfRightType(TuffGolemEntity tuffGolem) {
        Brain<?> brain = tuffGolem.getBrain();
        return brain.hasMemoryModule(ModMemoryModules.STACK_TARGET) && brain.getOptionalMemory(ModMemoryModules.STACK_TARGET).get().getType() == this.partnerType;
    }

    private Optional<? extends TuffGolemEntity> findValidStackPartner(TuffGolemEntity tuffGolem) {
        return tuffGolem.getBrain().getOptionalMemory(MemoryModuleType.VISIBLE_MOBS).get().findFirst((entity) -> {
            if (entity.getType() == this.partnerType && entity instanceof TuffGolemEntity) {
                return !tuffGolem.isPetrified();
            }
            return false;
        }).map(TuffGolemEntity.class::cast);
    }
}