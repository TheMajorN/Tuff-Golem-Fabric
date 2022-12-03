package net.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.themajorn.tuffgolem.common.ai.TuffGolemAi;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.themajorn.tuffgolem.core.registry.ModMemoryModules;
import org.jetbrains.annotations.NotNull;

public class PetrifyOrAnimate<E extends FabricEntityTypeBuilder.Mob> extends Task<TuffGolemEntity> {

    private final UniformIntProvider timeBetweenAnimateOrPetrify;
    private final SoundEvent getAnimateOrPetrifySound;


    public PetrifyOrAnimate(UniformIntProvider uniformInt, SoundEvent soundEvent) {
        super(ImmutableMap.of(
                ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS, MemoryModuleState.VALUE_ABSENT,
                ModMemoryModules.MID_ANIMATE_OR_PETRIFY, MemoryModuleState.VALUE_ABSENT), 200);
        this.timeBetweenAnimateOrPetrify = uniformInt;
        this.getAnimateOrPetrifySound = soundEvent;
    }

    protected boolean shouldRun(@NotNull ServerWorld serverLevel, TuffGolemEntity mob) {
        boolean isValidPosition = mob.isOnGround() && !mob.isSubmergedInWater() && !mob.isInLava() && !mob.stateLocked() && TuffGolemAi.isIdle(mob);
        if (!isValidPosition) {
            mob.getBrain().remember(ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS, this.timeBetweenAnimateOrPetrify.get(serverLevel.random) / 2);
        }
        return isValidPosition;
    }

    protected boolean shouldKeepRunning(@NotNull ServerWorld serverLevel, TuffGolemEntity mob, long i) {
        boolean validMorphConditions = !mob.isInsideWaterOrBubbleColumn() && TuffGolemAi.isIdle(mob);
        if (!validMorphConditions && mob.getBrain().getOptionalMemory(ModMemoryModules.MID_ANIMATE_OR_PETRIFY).isEmpty()) {
            mob.getBrain().remember(ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS, this.timeBetweenAnimateOrPetrify.get(serverLevel.random) / 2);
        }
        return validMorphConditions;
    }

    protected void run(@NotNull ServerWorld level, @NotNull TuffGolemEntity tuffGolem, long l) {
        if (tuffGolem.isPetrified()) {
            tuffGolem.animate();
        } else {
            tuffGolem.petrify();
        }
        tuffGolem.playSound(getAnimateOrPetrifySound, 1.0F, 1.0F);
        tuffGolem.getBrain().remember(ModMemoryModules.MID_ANIMATE_OR_PETRIFY, true);
    }

    protected void keepRunning(ServerWorld serverLevel, TuffGolemEntity mob, long l) {
        mob.getBrain().remember(ModMemoryModules.MID_ANIMATE_OR_PETRIFY, true);
    }
}
