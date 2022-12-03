package net.themajorn.tuffgolem.common.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.passive.AllayBrain;
import net.minecraft.entity.passive.GoatBrain;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.themajorn.tuffgolem.common.ai.behaviors.*;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import net.themajorn.tuffgolem.core.registry.ModActivities;
import net.themajorn.tuffgolem.core.registry.ModEntities;
import net.themajorn.tuffgolem.core.registry.ModMemoryModules;

import java.util.Optional;

public class TuffGolemAi {

    private static final UniformIntProvider TIME_BETWEEN_ANIMATE_OR_PETRIFY = UniformIntProvider.create(2400, 3600);
    private static final UniformIntProvider TIME_BETWEEN_GOING_TO_ITEM_FRAME = UniformIntProvider.create(1200, 1800);

    public static void initMemories(TuffGolemEntity tuffGolem, Random random) {
        tuffGolem.getBrain().remember(ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS, TIME_BETWEEN_ANIMATE_OR_PETRIFY.get(random));
        tuffGolem.getBrain().remember(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS, TIME_BETWEEN_GOING_TO_ITEM_FRAME.get(random));
    }

    public static Brain<?> makeBrain(Brain<TuffGolemEntity> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initTakeItemFromFrameActivity(brain);
        initReturnItemToFrameActivity(brain);
        initPetrifyOrAnimateActivity(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    private static void initCoreActivity(Brain<TuffGolemEntity> brain) {
        brain.setTaskList(Activity.CORE, 0,
            ImmutableList.of(
                new TuffGolemLookAtTargetSink(45, 90),
                new TuffGolemMoveToTargetSink(),
                new TemptationCooldownTask(ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS),
                new TemptationCooldownTask(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS),
                new TemptationCooldownTask(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS)
            ));
    }

    private static void initIdleActivity(Brain<TuffGolemEntity> brain) {
        brain.setTaskList(Activity.IDLE,
            ImmutableList.of(
                Pair.of(0, new GoToDroppedItem<>((tuffGolem) -> true, 1.15F, true, 10)),
                //Pair.of(1, new GiveInventoryToLookTargetTask(TuffGolemAi::getItemFramePosition, 18, 30, 1.15F)),
                Pair.of(0, new TuffGolemStack(ModEntities.TUFF_GOLEM, 1.15F)),
                Pair.of(1, new TuffGolemFollowTemptation((entity) -> 1.15F)),
                Pair.of(2, new TimeLimitedTask(new TuffGolemLookTarget((entity) -> true, 6.0F), UniformIntProvider.create(30, 60))),
                Pair.of(3, new RandomTask(
                            ImmutableList.of(
                            Pair.of(new TuffGolemRandomStroll(1.0F), 2),
                            Pair.of(new TuffGolemWalkTargetFromLookTarget(1.0F, 3), 2),
                            Pair.of(new WaitTask(30, 60), 1))))),
            ImmutableSet.of(
                Pair.of(ModMemoryModules.MID_ANIMATE_OR_PETRIFY, MemoryModuleState.VALUE_ABSENT)));
    }

    private static void initTakeItemFromFrameActivity(Brain<TuffGolemEntity> brain) {
        brain.setTaskList(ModActivities.TAKE_ITEM,
            ImmutableList.of(
                Pair.of(0, new GoAndTakeItemFromFrame<>(TIME_BETWEEN_GOING_TO_ITEM_FRAME, (tuffGolem) -> true, 1.15F, true, 20))),
                ImmutableSet.of(
                    Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryModuleState.VALUE_ABSENT),
                    Pair.of(ModMemoryModules.STACK_TARGET, MemoryModuleState.VALUE_ABSENT),
                    Pair.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_PRESENT),
                    Pair.of(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS, MemoryModuleState.VALUE_ABSENT)));
    }

    public static void initReturnItemToFrameActivity(Brain<TuffGolemEntity> brain) {
        brain.setTaskList(ModActivities.RETURN_ITEM,
            ImmutableList.of(
                Pair.of(0, new GoAndReturnItemToFrame<>(TIME_BETWEEN_GOING_TO_ITEM_FRAME, (tuffGolem) -> true, 1.15F, true, 30))),
            ImmutableSet.of(
                Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryModuleState.VALUE_ABSENT),
                Pair.of(ModMemoryModules.STACK_TARGET, MemoryModuleState.VALUE_ABSENT),
                Pair.of(MemoryModuleType.WALK_TARGET, MemoryModuleState.VALUE_PRESENT),
                Pair.of(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS, MemoryModuleState.VALUE_ABSENT),
                Pair.of(ModMemoryModules.SELECTED_ITEM_FRAME, MemoryModuleState.VALUE_PRESENT)
            ));
    }

    private static void initPetrifyOrAnimateActivity(Brain<TuffGolemEntity> brain) {
        brain.setTaskList(ModActivities.ANIMATE_PETRIFY,
            ImmutableList.of(
                Pair.of(0, new PetrifiedTime(TIME_BETWEEN_ANIMATE_OR_PETRIFY)),
                Pair.of(1, new PetrifyOrAnimate<>(TIME_BETWEEN_ANIMATE_OR_PETRIFY, SoundEvents.BLOCK_GRINDSTONE_USE))),
            ImmutableSet.of(
                Pair.of(MemoryModuleType.TEMPTING_PLAYER, MemoryModuleState.VALUE_ABSENT),
                Pair.of(ModMemoryModules.NEAREST_VISIBLE_ITEM_FRAME, MemoryModuleState.VALUE_ABSENT),
                Pair.of(ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS, MemoryModuleState.VALUE_ABSENT)
            ));
    }

    public static void updateActivity(TuffGolemEntity tuffGolem) {
        tuffGolem.getBrain().resetPossibleActivities(
            ImmutableList.of(
                ModActivities.MOVE_TO_REDSTONE_LAMP,
                ModActivities.RETURN_ITEM,
                ModActivities.TAKE_ITEM,
                ModActivities.ANIMATE_PETRIFY,
                Activity.IDLE));
    }

    public static boolean isIdle(GolemEntity golem) {
        return golem.getBrain().hasActivity(Activity.IDLE);
    }

    private static void stopWalking(TuffGolemEntity tuffGolem) {
        tuffGolem.getBrain().forget(MemoryModuleType.WALK_TARGET);
        tuffGolem.getNavigation().stop();
    }

    private static ItemStack removeOneItemFromItemEntity(ItemEntity itemOnGround) {
        ItemStack itemstack = itemOnGround.getStack();
        ItemStack splitItem = itemstack.split(1);
        if (itemstack.isEmpty()) {
            itemOnGround.discard();
        } else {
            itemOnGround.setStack(itemstack);
        }
        return splitItem;
    }

    public static void pickUpItem(TuffGolemEntity tuffGolem, ItemEntity itemOnGround) {
        stopWalking(tuffGolem);
        ItemStack itemstack;
        if (!itemOnGround.getStack().isOf(Items.SPAWNER)) {
            tuffGolem.sendPickup(itemOnGround, itemOnGround.getStack().getCount());
            itemstack = itemOnGround.getStack();
            itemOnGround.discard();
        } else {
            tuffGolem.sendPickup(itemOnGround, 1);
            itemstack = removeOneItemFromItemEntity(itemOnGround);
        }
        boolean equipIfPossible = tuffGolem.tryEquip(itemstack);
        if (!equipIfPossible) {
            tuffGolem.setStackInHand(Hand.MAIN_HAND, itemstack);
        }
    }

    private static Optional<LookTarget> getItemFramePosition(LivingEntity tuffGolem) {
        Brain<?> brain = tuffGolem.getBrain();
        Optional<BlockPos> positionOptional = brain.getOptionalMemory(ModMemoryModules.SELECTED_ITEM_FRAME_POSITION);
        Optional<ItemFrameEntity> optional = brain.getOptionalMemory(ModMemoryModules.SELECTED_ITEM_FRAME);
        if (optional.isPresent()) {
            BlockPos globalPos = positionOptional.get();
            if (shouldReturnItem(tuffGolem, brain, globalPos)) {
                return Optional.of(new BlockPosLookTarget(globalPos.up()));
            }

            brain.forget(MemoryModuleType.LIKED_NOTEBLOCK);
        }

        return getItemFramePositionTracker(tuffGolem);
    }

    private static Optional<LookTarget> getItemFramePositionTracker(LivingEntity tuffGolem) {
        return getSelectedItemFrame(tuffGolem).map((itemFrame) -> new EntityLookTarget(itemFrame, true));
    }

    public static Optional<ItemFrameEntity> getSelectedItemFrame(LivingEntity livingEntity) {
        World level = livingEntity.getWorld();
        if (!level.isClient() && level instanceof ServerWorld) {
            return livingEntity.getBrain().getOptionalMemory(ModMemoryModules.SELECTED_ITEM_FRAME);
        }
        return Optional.empty();
    }

    private static boolean shouldReturnItem(LivingEntity entity, Brain<?> brain, BlockPos pos) {
        Optional<Integer> optional = brain.getOptionalMemory(ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS);
        World level = entity.getWorld();
        return level.getDimension() == entity.getWorld().getDimension() && optional.isEmpty();
    }

    public static Ingredient getTemptItems() {
        return Ingredient.ofItems(Items.COPPER_INGOT);
    }
}
