package net.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ai.NoPenaltySolidTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;

import javax.annotation.Nullable;
import java.util.Optional;

public class TuffGolemRandomStroll extends Task<TuffGolemEntity> {
    private static final int MAX_XZ_DIST = 10;
    private static final int MAX_Y_DIST = 7;
    private final float speedModifier;
    protected final int maxHorizontalDistance;
    protected final int maxVerticalDistance;
    private final boolean mayStrollFromWater;

    public TuffGolemRandomStroll(float p_23744_) {
        this(p_23744_, true);
    }

    public TuffGolemRandomStroll(float p_182347_, boolean p_182348_) {
        this(p_182347_, 10, 7, p_182348_);
    }

    public TuffGolemRandomStroll(float p_23746_, int p_23747_, int p_23748_) {
        this(p_23746_, p_23747_, p_23748_, true);
    }

    public TuffGolemRandomStroll(float p_182342_, int p_182343_, int p_182344_, boolean p_182345_) {
        super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_ABSENT));
        this.speedModifier = p_182342_;
        this.maxHorizontalDistance = p_182343_;
        this.maxVerticalDistance = p_182344_;
        this.mayStrollFromWater = p_182345_;
    }

    protected boolean shouldRun(ServerWorld p_182353_, TuffGolemEntity tuffGolem) {
        return this.mayStrollFromWater || !tuffGolem.isInsideWaterOrBubbleColumn() && !tuffGolem.isPetrified();
    }

    protected void run(ServerWorld p_23754_, TuffGolemEntity p_23755_, long p_23756_) {
        Optional<Vec3d> optional = Optional.ofNullable(this.getTargetPos(p_23755_));
        p_23755_.getBrain().remember(MemoryModuleType.WALK_TARGET, optional.map((p_23758_) -> {
            return new WalkTarget(p_23758_, this.speedModifier, 0);
        }));
    }

    @Nullable
    protected Vec3d getTargetPos(TuffGolemEntity p_147851_) {
        Vec3d vec3d = p_147851_.getRotationVec(0.0F);
        return NoPenaltySolidTargeting.find(p_147851_, this.maxHorizontalDistance, this.maxVerticalDistance, -2, vec3d.x, vec3d.z, 1.5707963705062866);
    }
}