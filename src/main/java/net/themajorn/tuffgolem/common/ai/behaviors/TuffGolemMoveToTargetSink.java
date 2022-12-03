package net.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;

import javax.annotation.Nullable;
import java.util.Optional;

public class TuffGolemMoveToTargetSink extends Task<TuffGolemEntity> {
    private static final int MAX_COOLDOWN_BEFORE_RETRYING = 40;
    private int remainingCooldown;
    @Nullable
    private Path path;
    @Nullable
    private BlockPos lastTargetPos;
    private float speedModifier;

    public TuffGolemMoveToTargetSink() {
        this(150, 250);
    }

    public TuffGolemMoveToTargetSink(int p_23573_, int p_23574_) {
        super(ImmutableMap.of(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleState.REGISTERED, MemoryModuleType.PATH, MemoryModuleState.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_PRESENT), p_23573_, p_23574_);
    }

    protected boolean shouldRun(ServerWorld serverLevel, TuffGolemEntity tuffGolem) {
        if (this.remainingCooldown > 0 || tuffGolem.isPetrified()) {
            --this.remainingCooldown;
            return false;
        } else {
            Brain<?> brain = tuffGolem.getBrain();
            WalkTarget walktarget = brain.getOptionalMemory(MemoryModuleType.WALK_TARGET).get();
            boolean flag = this.reachedTarget(tuffGolem, walktarget);
            if (!flag && this.tryComputePath(tuffGolem, walktarget, serverLevel.getTime()) && !tuffGolem.isPetrified()) {
                this.lastTargetPos = walktarget.getLookTarget().getBlockPos();
                return true;
            } else {
                brain.forget(MemoryModuleType.WALK_TARGET);
                if (flag) {
                    brain.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
                }

                return false;
            }
        }
    }

    protected boolean shouldKeepRunning(ServerWorld serverLevel, TuffGolemEntity tuffGolem, long l) {
        if (this.path != null && this.lastTargetPos != null) {
            Optional<WalkTarget> optional = tuffGolem.getBrain().getOptionalMemory(MemoryModuleType.WALK_TARGET);
            EntityNavigation pathNavigation = tuffGolem.getNavigation();
            return !pathNavigation.isIdle() && optional.isPresent() && !this.reachedTarget(tuffGolem, optional.get()) && !tuffGolem.isPetrified();
        } else {
            return false;
        }
    }

    protected void finishRunning(ServerWorld p_23601_, TuffGolemEntity p_23602_, long p_23603_) {
        if (p_23602_.getBrain().hasMemoryModule(MemoryModuleType.WALK_TARGET) && !this.reachedTarget(p_23602_, p_23602_.getBrain().getOptionalMemory(MemoryModuleType.WALK_TARGET).get()) && p_23602_.getNavigation().isNearPathStartPos()) {
            this.remainingCooldown = p_23601_.getRandom().nextInt(40);
        }

        p_23602_.getNavigation().stop();
        p_23602_.getBrain().forget(MemoryModuleType.WALK_TARGET);
        p_23602_.getBrain().forget(MemoryModuleType.PATH);
        this.path = null;
    }

    protected void run(ServerWorld p_23609_, TuffGolemEntity p_23610_, long p_23611_) {
        p_23610_.getBrain().remember(MemoryModuleType.PATH, this.path);
        p_23610_.getNavigation().startMovingAlong(this.path, (double)this.speedModifier);
    }

    protected void keepRunning(ServerWorld p_23617_, TuffGolemEntity p_23618_, long p_23619_) {
        Path path = p_23618_.getNavigation().getCurrentPath();
        Brain<?> brain = p_23618_.getBrain();
        if (this.path != path) {
            this.path = path;
            brain.remember(MemoryModuleType.PATH, path);
        }

        if (path != null && this.lastTargetPos != null) {
            WalkTarget walktarget = brain.getOptionalMemory(MemoryModuleType.WALK_TARGET).get();
            if (walktarget.getLookTarget().getBlockPos().getSquaredDistance(this.lastTargetPos) > 4.0D && this.tryComputePath(p_23618_, walktarget, p_23617_.getTime())) {
                this.lastTargetPos = walktarget.getLookTarget().getBlockPos();
                this.run(p_23617_, p_23618_, p_23619_);
            }

        }
    }

    private boolean tryComputePath(TuffGolemEntity p_23593_, WalkTarget p_23594_, long p_23595_) {
        BlockPos blockpos = p_23594_.getLookTarget().getBlockPos();
        this.path = p_23593_.getNavigation().findPathTo(blockpos, 0);
        this.speedModifier = p_23594_.getSpeed();
        Brain<?> brain = p_23593_.getBrain();
        if (this.reachedTarget(p_23593_, p_23594_)) {
            brain.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        } else {
            boolean flag = this.path != null && this.path.reachesTarget();
            if (flag) {
                brain.forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            } else if (!brain.hasMemoryModule(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)) {
                brain.remember(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, p_23595_);
            }

            if (this.path != null) {
                return true;
            }

            Vec3d vec3 = NoPenaltyTargeting.findTo((PathAwareEntity) p_23593_, 10, 7, Vec3d.ofBottomCenter(blockpos), (double)((float)Math.PI / 2F));
            if (vec3 != null) {
                this.path = p_23593_.getNavigation().findPathTo(vec3.x, vec3.y, vec3.z, 0);
                return this.path != null;
            }
        }

        return false;
    }

    private boolean reachedTarget(TuffGolemEntity p_23590_, WalkTarget p_23591_) {
        return p_23591_.getLookTarget().getBlockPos().getManhattanDistance(p_23590_.getBlockPos()) <= p_23591_.getCompletionRange();
    }
}
