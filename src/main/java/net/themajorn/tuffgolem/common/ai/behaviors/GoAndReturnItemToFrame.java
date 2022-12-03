package net.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.themajorn.tuffgolem.core.registry.ModMemoryModules;

import java.util.function.Predicate;

public class GoAndReturnItemToFrame<E extends LivingEntity> extends Task<TuffGolemEntity> {
    private final UniformIntProvider timeBetweenGoToItemFrame;
    private final Predicate<E> predicate;
    private final int maxDistToWalk;
    private final float speedModifier;


    public GoAndReturnItemToFrame(UniformIntProvider cooldownTicks, Predicate<E> predicate, float speedMod, boolean memStatus, int interestRadius) {
        super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED,
                MemoryModuleType.WALK_TARGET, memStatus ? MemoryModuleState.REGISTERED : MemoryModuleState.VALUE_ABSENT,
                ModMemoryModules.SELECTED_ITEM_FRAME, MemoryModuleState.VALUE_PRESENT));
        this.timeBetweenGoToItemFrame = cooldownTicks;
        this.predicate = predicate;
        this.maxDistToWalk = interestRadius;
        this.speedModifier = speedMod;
    }

    protected boolean shouldRun(ServerWorld serverLevel, TuffGolemEntity entity) {
        boolean validStartConditions = !this.isOnGoToCooldown(entity)
                && this.getSelectedItemFrame(entity).isInRange(entity, (double)this.maxDistToWalk)
                && getSelectedItemFrame(entity).getHeldItemStack().isEmpty()
                && entity.hasCloak()
                && entity.hasItemInHand()
                && !entity.isPetrified();
        if (!validStartConditions) {
            entity.getBrain().forget(ModMemoryModules.SELECTED_ITEM_FRAME);
            entity.getBrain().remember(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS, this.timeBetweenGoToItemFrame.get(serverLevel.random) / 6);
        }
        return validStartConditions;
    }

    protected void run(ServerWorld serverLevel, TuffGolemEntity entity, long l) {
        LookTargetUtil.walkTowards(entity, this.getSelectedItemFrame(entity), this.speedModifier, 1);
        entity.putBackItem();
    }

    protected boolean shouldKeepRunning(ServerWorld serverLevel, TuffGolemEntity entity, long l) {
        boolean validContinueConditions = getSelectedItemFrame(entity).getHeldItemStack().isEmpty() && entity.hasItemInHand();
        if (!validContinueConditions) {
            entity.getBrain().forget(ModMemoryModules.SELECTED_ITEM_FRAME);
            entity.getBrain().remember(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS, this.timeBetweenGoToItemFrame.get(serverLevel.random) / 6);
        }
        return validContinueConditions;
    }

    private boolean isOnGoToCooldown(TuffGolemEntity entity) {
        return entity.getBrain().isMemoryInState(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS, MemoryModuleState.VALUE_PRESENT);
    }

    private ItemFrameEntity getSelectedItemFrame(TuffGolemEntity entity) {
        return entity.getBrain().getOptionalMemory(ModMemoryModules.SELECTED_ITEM_FRAME).get();
    }

}