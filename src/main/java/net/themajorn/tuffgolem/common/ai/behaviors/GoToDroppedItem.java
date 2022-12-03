package net.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;

import java.util.function.Predicate;

public class GoToDroppedItem<E extends LivingEntity> extends Task<TuffGolemEntity> {
    private final Predicate<E> predicate;
    private final int maxDistToWalk;
    private final float speedModifier;

    public GoToDroppedItem(Predicate<E> predicate, float speedMod, boolean memStatus, int interestRadius) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED,
                MemoryModuleType.WALK_TARGET, memStatus ? MemoryModuleState.REGISTERED : MemoryModuleState.VALUE_ABSENT,
                MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleState.VALUE_PRESENT));
        this.predicate = predicate;
        this.maxDistToWalk = interestRadius;
        this.speedModifier = speedMod;
    }

    protected boolean shouldRun(ServerWorld serverLevel, TuffGolemEntity entity) {
        return !this.isOnPickupCooldown(entity)
                && this.predicate.test((E) entity)
                && this.getClosestDroppedItem(entity).isInRange(entity, this.maxDistToWalk)
                && entity.hasCloak()
                && !entity.hasItemInHand()
                && !entity.isPetrified();
    }

    protected void run(ServerWorld serverLevel, TuffGolemEntity entity, long l) {
            LookTargetUtil.walkTowards(entity, this.getClosestDroppedItem(entity), this.speedModifier, 0);
    }

    private boolean isOnPickupCooldown(TuffGolemEntity entity) {
        return entity.getBrain().isMemoryInState(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleState.VALUE_PRESENT);
    }

    private ItemEntity getClosestDroppedItem(TuffGolemEntity entity) {
        return entity.getBrain().getOptionalMemory(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM).get();
    }
}
