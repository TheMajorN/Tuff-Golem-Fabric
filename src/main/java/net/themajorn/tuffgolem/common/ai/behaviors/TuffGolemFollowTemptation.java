package net.themajorn.tuffgolem.common.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.themajorn.tuffgolem.core.registry.ModMemoryModules;

import java.util.Optional;
import java.util.function.Function;

public class TuffGolemFollowTemptation extends Task<TuffGolemEntity> {
    public static final int TEMPTATION_COOLDOWN = 100;
    public static final double CLOSE_ENOUGH_DIST = 2.5D;
    private final Function<LivingEntity, Float> speedModifier;

    public TuffGolemFollowTemptation(Function<LivingEntity, Float> entityFloatFunction) {
        super(Util.make(() -> {
            ImmutableMap.Builder<MemoryModuleType<?>, MemoryModuleState> builder = ImmutableMap.builder();
            builder.put(MemoryModuleType.LOOK_TARGET, MemoryModuleState.REGISTERED);
            builder.put(MemoryModuleType.WALK_TARGET, MemoryModuleState.REGISTERED);
            builder.put(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleState.VALUE_ABSENT);
            builder.put(MemoryModuleType.IS_TEMPTED, MemoryModuleState.REGISTERED);
            builder.put(MemoryModuleType.TEMPTING_PLAYER, MemoryModuleState.VALUE_PRESENT);
            builder.put(ModMemoryModules.STACK_TARGET, MemoryModuleState.VALUE_ABSENT);
            return builder.build();
        }));
        this.speedModifier = entityFloatFunction;
    }

    protected float getSpeedModifier(TuffGolemEntity tuffGolem) {
        return this.speedModifier.apply(tuffGolem);
    }

    private Optional<PlayerEntity> getTemptingPlayer(TuffGolemEntity tuffGolem) {
        return tuffGolem.getBrain().getOptionalMemory(MemoryModuleType.TEMPTING_PLAYER);
    }

    protected boolean isTimeLimitExceeded(long l) {
        return false;
    }

    protected boolean shouldKeepRunning(ServerWorld serverLevel, TuffGolemEntity tuffGolem, long l) {
        return this.getTemptingPlayer(tuffGolem).isPresent() && !tuffGolem.getBrain().hasMemoryModule(ModMemoryModules.STACK_TARGET);
    }

    protected void run(ServerWorld serverLevel, TuffGolemEntity tuffGolem, long l) {
        tuffGolem.getBrain().remember(MemoryModuleType.IS_TEMPTED, true);
    }

    protected void finishRunning(ServerWorld serverLevel, TuffGolemEntity tuffGolem, long l) {
        Brain<?> brain = tuffGolem.getBrain();
        brain.remember(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, 100);
        brain.remember(MemoryModuleType.IS_TEMPTED, false);
        brain.forget(MemoryModuleType.WALK_TARGET);
        brain.forget(MemoryModuleType.LOOK_TARGET);
    }

    protected void keepRunning(ServerWorld serverLevel, TuffGolemEntity tuffGolem, long l) {
        PlayerEntity player = this.getTemptingPlayer(tuffGolem).get();
        Brain<?> brain = tuffGolem.getBrain();
        brain.remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(player, true));
        if (tuffGolem.squaredDistanceTo(player) < 6.25D) {
            brain.forget(MemoryModuleType.WALK_TARGET);
        } else {
            brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityLookTarget(player, false), this.getSpeedModifier(tuffGolem), 2));
        }

    }
}
