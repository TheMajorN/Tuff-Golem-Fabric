package net.themajorn.tuffgolem.common.ai.goals;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.recipe.Ingredient;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class TuffGolemTemptGoal extends Goal {
    private static final TargetPredicate TEMPTING_ENTITY_PREDICATE = TargetPredicate.createNonAttackable().setBaseMaxDistance(10.0).ignoreVisibility();
    private final TargetPredicate predicate;
    protected final PathAwareEntity mob;
    private final double speedModifier;
    private double px;
    private double py;
    private double pz;
    private double pRotX;
    private double pRotY;
    @Nullable
    protected TuffGolemEntity tuffGolem;
    private int calmDown;
    private boolean isRunning;
    private final Ingredient items;
    private final boolean canScare;

    public TuffGolemTemptGoal(PathAwareEntity mob, double speedMod, Ingredient temptIngredient, boolean canScare) {
        this.mob = mob;
        this.speedModifier = speedMod;
        this.items = temptIngredient;
        this.canScare = canScare;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        this.predicate = TEMPTING_ENTITY_PREDICATE.copy().setPredicate(this::isTemptedBy);
    }

    public boolean canStart() {
        if (this.calmDown > 0) {
            --this.calmDown;
            return false;
        } else {
            this.tuffGolem = this.mob.world.getClosestEntity(TuffGolemEntity.class, this.predicate, this.mob, this.mob.getX(), this.mob.getY(), this.mob.getZ(), this.mob.getBoundingBox().expand(6.0D, 2.0D, 6.0D));
            return this.tuffGolem != null;
        }
    }

    private boolean isTemptedBy(LivingEntity livingEntity) {
        return this.items.test(livingEntity.getMainHandStack()) || this.items.test(livingEntity.getOffHandStack());
    }

    public boolean shouldContinue() {
        if (this.canScare()) {
            if (this.mob.squaredDistanceTo(this.tuffGolem) < 36.0D) {
                if (this.tuffGolem.squaredDistanceTo(this.px, this.py, this.pz) > 0.010000000000000002D) {
                    return false;
                }

                if (Math.abs((double)this.tuffGolem.getX() - this.pRotX) > 5.0D || Math.abs((double)this.tuffGolem.getY() - this.pRotY) > 5.0D) {
                    return false;
                }
            } else {
                this.px = this.tuffGolem.getX();
                this.py = this.tuffGolem.getY();
                this.pz = this.tuffGolem.getZ();
            }

            this.pRotX = (double)this.tuffGolem.getX();
            this.pRotY = (double)this.tuffGolem.getY();
        }

        return this.canStart();
    }

    protected boolean canScare() {
        return this.canScare;
    }

    public void start() {
        this.px = this.tuffGolem.getX();
        this.py = this.tuffGolem.getY();
        this.pz = this.tuffGolem.getZ();
        this.isRunning = true;
    }

    public void stop() {
        this.tuffGolem = null;
        this.mob.getNavigation().stop();
        this.calmDown = toGoalTicks(100);
        this.isRunning = false;
    }

    public void tick() {
        this.mob.getLookControl().lookAt(this.tuffGolem, (float)(this.mob.getHeadYaw() + 20), (float)this.mob.getMaxHeadRotation());
        if (this.mob.squaredDistanceTo(this.tuffGolem) < 6.25D) {
            this.mob.getNavigation().stop();
        } else {
            this.mob.getNavigation().startMovingTo(this.tuffGolem, this.speedModifier);
        }

    }

    public boolean isRunning() {
        return this.isRunning;
    }
}