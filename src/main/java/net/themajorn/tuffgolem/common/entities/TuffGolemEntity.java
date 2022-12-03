package net.themajorn.tuffgolem.common.entities;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.themajorn.tuffgolem.common.ai.TuffGolemAi;
import net.themajorn.tuffgolem.common.ai.goals.MoveToRedstoneLampGoal;
import net.themajorn.tuffgolem.core.registry.ModMemoryModules;
import net.themajorn.tuffgolem.core.registry.ModSensors;
import net.themajorn.tuffgolem.core.registry.ModSounds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.example.entity.GeoExampleEntity;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

public class TuffGolemEntity extends GolemEntity implements IAnimatable, InventoryOwner {

    // === DATA ACCESSOR VARIABLES === //
    private static final TrackedData<Integer> DATA_CLOAK_COLOR;
    private static final TrackedData<Integer> DATA_STACK_SIZE;
    private static final TrackedData<Integer> DATA_HEIGHT_DIMENSION_STATE;
    private static final TrackedData<Integer> DATA_WIDTH_DIMENSION_STATE;
    protected static final TrackedData<Boolean> DATA_PLAYER_CREATED;
    private static final TrackedData<Boolean> DATA_IS_PETRIFIED;
    private static final TrackedData<Boolean> DATA_STATE_LOCKED;
    private static final TrackedData<Boolean> DATA_IS_ANIMATED;
    private static final TrackedData<Boolean> DATA_HAS_CLOAK;

    // === SENSOR INITIALIZATION === //

    protected static final ImmutableList<SensorType<? extends Sensor<? super TuffGolemEntity>>> SENSOR_TYPES =
            ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES,
                            SensorType.NEAREST_PLAYERS,
                            SensorType.HURT_BY,
                            SensorType.NEAREST_ITEMS,
                            ModSensors.NEAREST_ITEM_FRAMES,
                            ModSensors.TUFF_GOLEM_TEMPTATIONS);

    // === MEMORY MODULE INITIALIZATION === //

    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES =
            ImmutableList.of(MemoryModuleType.PATH,
                    MemoryModuleType.LOOK_TARGET,
                    MemoryModuleType.WALK_TARGET,
                    MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
                    ModMemoryModules.NEAREST_VISIBLE_ITEM_FRAME,
                    ModMemoryModules.NEAREST_REDSTONE_LAMP_MEMORY,
                    ModMemoryModules.SELECTED_ITEM_FRAME_POSITION,
                    ModMemoryModules.SELECTED_ITEM_FRAME,
                    ModMemoryModules.ITEM_FRAME_POSITION,
                    ModMemoryModules.GO_TO_ITEM_FRAME_COOLDOWN_TICKS,
                    ModMemoryModules.ANIMATE_OR_PETRIFY_COOLDOWN_TICKS,
                    ModMemoryModules.GO_TO_REDSTONE_LAMP_COOLDOWN_TICKS,
                    ModMemoryModules.MID_ANIMATE_OR_PETRIFY,
                    MemoryModuleType.TEMPTING_PLAYER,
                    MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
                    MemoryModuleType.IS_TEMPTED,
                    ModMemoryModules.STACK_TARGET);

    // === VARIABLES === //

    private static final Vec3i TUFF_GOLEM_ITEM_PICKUP_REACH = new Vec3i(2, 1, 2);

    private final SimpleInventory inventory = new SimpleInventory(1);

    @javax.annotation.Nullable
    private UUID stackCause;

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);

    // === PRIMITIVE VARIABLES === //

    private double rideDimensions = 1.0D;

    private int age;

    private int wantsToStack;

    public final float bobOffs;
    public boolean isPetrifying;
    public boolean isAnimating;
    public boolean isGiving;
    public boolean isReceiving;


    // ============================================= CONSTRUCTOR ==================================================== //
    public TuffGolemEntity(EntityType<? extends GolemEntity> entityType, World level) {
        super(entityType, level);
        this.bobOffs = this.random.nextFloat() * (float)Math.PI * 2.0F;
    }

    // ============================================== ATTRIBUTES ==================================================== //

    public static DefaultAttributeContainer.Builder setAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.15F)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0D)
                .add(EntityAttributes.GENERIC_ARMOR, 2.0D);
    }

    public void initGoals() {
        this.goalSelector.add(0, new MoveToRedstoneLampGoal(this, 1.15, 20));
    }

    // ============================================= DATA SYNCING =================================================== //
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(DATA_PLAYER_CREATED, false);
        this.dataTracker.startTracking(DATA_CLOAK_COLOR, DyeColor.WHITE.getId());
        this.dataTracker.startTracking(DATA_STACK_SIZE, 1);
        this.dataTracker.startTracking(DATA_HEIGHT_DIMENSION_STATE, 1);
        this.dataTracker.startTracking(DATA_WIDTH_DIMENSION_STATE, 1);
        this.dataTracker.startTracking(DATA_IS_PETRIFIED, false);
        this.dataTracker.startTracking(DATA_STATE_LOCKED, false);
        this.dataTracker.startTracking(DATA_IS_ANIMATED, false);
        this.dataTracker.startTracking(DATA_HAS_CLOAK, false);
    }

    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        tag.put("Inventory", this.inventory.toNbtList());
        tag.putBoolean("PlayerCreated", this.isPlayerCreated());
        tag.putBoolean("isPetrified", this.isPetrified());
        tag.putBoolean("isAnimated", this.isAnimated());
        tag.putBoolean("hasCloak", this.hasCloak());
        tag.putBoolean("stateLocked", this.stateLocked());
        tag.putByte("CloakColor", (byte) this.getCloakColor().getId());
        tag.putByte("stackSize", (byte) this.getStackSize());
        tag.putInt("wantsToStack", this.wantsToStack);
        tag.putInt("heightDimensionState", this.getHeightDimensionState());
        tag.putInt("widthDimensionState", this.getWidthDimensionState());
        if (this.stackCause != null) {
            tag.putUuid("stackCause", this.stackCause);
        }
    }

    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        this.inventory.readNbtList(tag.getList("Inventory", 10));
        this.setPlayerCreated(tag.getBoolean("PlayerCreated"));
        this.setPetrified(tag.getBoolean("isPetrified"));
        this.lockState(tag.getBoolean("stateLocked"));
        this.setAnimated(tag.getBoolean("isAnimated"));
        this.setHeightDimensionState(Math.min(tag.getInt("heightDimensionState"), 2));
        this.setWidthDimensionState(Math.min(tag.getInt("widthDimensionState"), 2));
        this.setCloak(tag.getBoolean("hasCloak"));
        if (tag.contains("CloakColor", 99)) {
            this.setCloakColor(DyeColor.byId(tag.getInt("CloakColor")));
        }
        this.setStackSize(tag.getInt("stackSize"));
        this.wantsToStack = tag.getInt("wantsToStack");
        this.stackCause = tag.containsUuid("stackCause") ? tag.getUuid("stackCause") : null;
    }

    public void onTrackedDataSet(TrackedData<?> accessor) {
        if (DATA_HEIGHT_DIMENSION_STATE.equals(accessor) || DATA_WIDTH_DIMENSION_STATE.equals(accessor)) {
            this.calculateDimensions();
        }
        super.onTrackedDataSet(accessor);
    }

    static {
        DATA_HAS_CLOAK = DataTracker.registerData(TuffGolemEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        DATA_IS_PETRIFIED = DataTracker.registerData(TuffGolemEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        DATA_IS_ANIMATED = DataTracker.registerData(TuffGolemEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        DATA_PLAYER_CREATED = DataTracker.registerData(TuffGolemEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        DATA_STATE_LOCKED = DataTracker.registerData(TuffGolemEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        DATA_CLOAK_COLOR = DataTracker.registerData(TuffGolemEntity.class, TrackedDataHandlerRegistry.INTEGER);
        DATA_STACK_SIZE = DataTracker.registerData(TuffGolemEntity.class, TrackedDataHandlerRegistry.INTEGER);
        DATA_HEIGHT_DIMENSION_STATE = DataTracker.registerData(TuffGolemEntity.class, TrackedDataHandlerRegistry.INTEGER);
        DATA_WIDTH_DIMENSION_STATE = DataTracker.registerData(TuffGolemEntity.class, TrackedDataHandlerRegistry.INTEGER);
    }

    // ================================================ SOUNDS ====================================================== //

    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_IRON_GOLEM_STEP, 0.15F, 2.0F);
    }

    // ========================================= SPAWNING & EXISTENCE =============================================== //

    public EntityData initialize(ServerWorldAccess levelAccessor, LocalDifficulty difficulty,
                                 SpawnReason spawnType, @Nullable EntityData spawnGroupData, @Nullable NbtCompound tag) {
        net.minecraft.util.math.random.Random randomsource = levelAccessor.getRandom();
        TuffGolemAi.initMemories(this, randomsource);
        spawnGroupData = super.initialize(levelAccessor, difficulty, spawnType, spawnGroupData, tag);
        this.isPetrifying = false;
        this.isAnimating = false;
        this.isGiving = false;
        this.isReceiving = false;
        this.setCanPickUpLoot(false);
        this.setAnimated(true);
        this.setPetrified(false);
        this.lockState(false);
        this.setPassengersRidingOffset(0.9D);
        return spawnGroupData;
    }

    public void setPlayerCreated(boolean playerCreated) {
        Boolean b0 = this.dataTracker.get(DATA_PLAYER_CREATED);
        if (playerCreated) {
            this.dataTracker.set(DATA_PLAYER_CREATED, true);
        } else {
            this.dataTracker.set(DATA_PLAYER_CREATED, false);
        }
    }

    public boolean isPlayerCreated() { return this.dataTracker.get(DATA_PLAYER_CREATED); }

    public void tick() {
        super.tick();
        if (this.isAlive()) {
            if (this.age < 36000) {
                this.age++;
            } else {
                this.age = 1;
            }
        }
        if (isStacked()) {
            this.setYaw(this.getVehicle().getYaw());
            if (getTuffGolemBelow().isPetrified()) {
                this.setAnimated(false);
                this.setPetrified(true);
            } else {
                this.setPetrified(false);
                this.setAnimated(true);
            }
        }

        if (this.isPetrified()) {
            if (this.getYaw() >= 0 && this.getYaw() < 90) {
                this.setYaw(0.0F);
            }
            else if (this.getYaw() >= 90 && this.getYaw() < 179) {
                this.getYaw(90.0F);
            }
            else if (this.getYaw() <= -1 && this.getYaw() > -90) {
                this.getYaw(-0.1F);
            }
            else if (this.getYaw() <= -90 && this.getYaw() > -179) {
                this.getYaw(-90.0F);
            } else {
                this.getYaw(0.0F);
            }
        }

    }

    public boolean canBreatheUnderwater() { return true; }

    public Vec3i getPickupReach() { return TUFF_GOLEM_ITEM_PICKUP_REACH; }

    public int getAge() { return this.age; }

    public float getSpin(float i) { return ((float)this.getAge() + i) / 20.0F + this.bobOffs; }

    protected ActionResult interactMob(PlayerEntity player, @NotNull Hand hand) {
        ItemStack itemInPlayerHand = player.getStackInHand(hand);
        ItemStack itemInTuffGolemHand = this.getStackInHand(Hand.MAIN_HAND);
        Item specificItemInPlayerHand = itemInPlayerHand.getItem();

        // STACK AND UNSTACK
        if (itemInPlayerHand.isOf(Items.COPPER_INGOT)) {
            if (!this.world.isClient && this.canStack() && !this.hasVehicle() && !this.wantsToStack()) {
                this.usePlayerItem(player, hand, itemInPlayerHand);
                this.setWantsToStack(player);
                return ActionResult.SUCCESS;
            }
            else if (!this.world.isClient && this.hasVehicle() && !this.hasPassengers()) {
                TuffGolemEntity lowestGolem = this.getBottomTuffGolem(this);
                this.stopRiding();
                if (lowestGolem.getFirstPassenger() != null) {
                    lowestGolem.setHeightDimensionState(lowestGolem.getNumOfTuffGolemsAbove(lowestGolem, 1));
                    lowestGolem.setPassengersRidingOffset(lowestGolem.getNumOfTuffGolemsAbove(lowestGolem, 1));
                } else {
                    lowestGolem.setHeightDimensionState(1);
                    lowestGolem.setWidthDimensionState(1);
                }
                if (this.isPetrified()) {
                    animate();
                }
                return ActionResult.SUCCESS;
            } else {
                return ActionResult.FAIL;
            }
        }
        // ADD CLOAK
        else if (specificItemInPlayerHand instanceof BannerItem banner && !this.hasCloak()) {
            DyeColor color = banner.getColor();
            if (!player.getAbilities().creativeMode) {
                player.setStackInHand(hand, Items.STICK.getDefaultStack());
            }
            this.playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);
            this.setCloak(true);
            this.setCloakColor(color);
            return ActionResult.SUCCESS;
        }

        // CHANGE COLOR OF CLOAK
        else if (specificItemInPlayerHand instanceof DyeItem && player.isSneaking()) {
            DyeColor dyecolor = ((DyeItem)specificItemInPlayerHand).getColor();
            if (dyecolor != this.getCloakColor()) {
                this.playSound(SoundEvents.ITEM_DYE_USE, 1.0F, 1.0F);
                this.setCloakColor(dyecolor);
                if (!player.getAbilities().creativeMode) {
                    itemInPlayerHand.decrement(1);
                }
                return ActionResult.SUCCESS;
            } else {
                return ActionResult.FAIL;
            }
        }

        // REMOVE CLOAK
        else if (itemInPlayerHand.isOf(Items.STICK)
                && player.isSneaking()
                && itemInTuffGolemHand.isEmpty()
                && this.hasCloak()) {
            DyeColor color = getCloakColor();
            player.setStackInHand(hand, BannerItem.byRawId(color.getId()).asItem().getDefaultStack());
            this.playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);
            this.setCloak(false);
            return ActionResult.SUCCESS;
        }

        // ENABLE STATE CHANGE
        else if (itemInPlayerHand.isOf(Items.HONEYCOMB) && player.isSneaking() && !stateLocked()) {
            this.lockState(true);
            usePlayerItem(player, hand, itemInPlayerHand);
            playSound(SoundEvents.ITEM_HONEYCOMB_WAX_ON, 1.0F, 1.0F);
            return ActionResult.SUCCESS;
        }

        // DISABLE STATE CHANGE
        else if (itemInPlayerHand.isOf(Items.WATER_BUCKET) && player.isSneaking() && stateLocked()) {
            this.lockState(false);
            player.setStackInHand(hand, Items.BUCKET.asItem().getDefaultStack());
            playSound(SoundEvents.ITEM_BUCKET_EMPTY, 1.0F, 1.0F);
            return ActionResult.SUCCESS;
        }

        // ANIMATE AND PETRIFY - DEBUG ONLY - REMOVE FOR FINAL VERSION
        else if (itemInPlayerHand.isOf(Items.TUFF) && player.isSneaking()) {
            if (this.isAnimated()) {
                petrify();
            } else {
                animate();
            }
            return ActionResult.SUCCESS;
        }

        // GIVE ITEM TO TUFF GOLEM
        else if (itemInTuffGolemHand.isEmpty() && !itemInPlayerHand.isEmpty() && hasCloak()) {
            this.isReceiving = true;
            ItemStack playerItemCopy = itemInPlayerHand.copy();
            playerItemCopy.setCount(1);
            this.playSound(ModSounds.RECEIVE_SOUND, 0.3F, 1.0F);
            this.setStackInHand(Hand.MAIN_HAND, playerItemCopy);
            this.removeInteractionItem(player, itemInPlayerHand);
            return ActionResult.SUCCESS;
        }

        // TAKE ITEM FROM TUFF GOLEM
        else if (!itemInTuffGolemHand.isEmpty() && hand == Hand.MAIN_HAND && itemInPlayerHand.isEmpty()) {
            this.isGiving = true;
            this.playSound(ModSounds.GIVE_SOUND, 0.3F, 1.0F);
            this.swingHand(Hand.MAIN_HAND);
            this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            Iterator var5 = this.getInventory().clearToList().iterator();
            while(var5.hasNext()) {
                ItemStack itemStack4 = (ItemStack)var5.next();
                LookTargetUtil.give(this, itemStack4, this.getPos());
            }

            player.giveItemStack(itemInTuffGolemHand);
            return ActionResult.SUCCESS;
        } else {
            return super.interactMob(player, hand);
        }
    }

    // =========================================== INVENTORY & LOOT ================================================= //

    public SimpleInventory getInventory() { return this.inventory; }

    public boolean canPickUpLoot() { return !this.hasItemInHand() && this.hasCloak(); }

    public boolean hasItemInHand() { return !this.getStackInHand(Hand.MAIN_HAND).isEmpty(); }

    public boolean canTakeItem(@NotNull ItemStack stack) { return false; }

    protected void pickUpItem(ItemEntity itemOnGround) {
        this.triggerItemPickedUpByEntityCriteria(itemOnGround);
        TuffGolemAi.pickUpItem(this, itemOnGround);
    }

    public void pickOutItem() {
        Vec3i reach = this.getPickupReach();
        if (!this.world.isClient
                && this.canPickUpLoot()
                && this.isAlive()
                && !this.dead) {
            for (ItemFrameEntity itemFrame : this.world.getEntitiesByClass(ItemFrameEntity.class, this.getBoundingBox().expand(reach.getX(), reach.getY(), reach.getZ()), Predicates.alwaysTrue())) {
                if (!itemFrame.isRemoved() && !itemFrame.getHeldItemStack().isEmpty() && !this.hasItemInHand()) {
                    ItemStack itemstack = itemFrame.getHeldItemStack();
                    if (this.getBrain().getOptionalMemory(ModMemoryModules.NEAREST_VISIBLE_ITEM_FRAME).isPresent()) {
                        this.setStackInHand(Hand.MAIN_HAND, itemstack);
                        itemFrame.setHeldItemStack(ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    public void putBackItem() {
        Vec3i vec3i = this.getPickupReach();
        if (!this.world.isClient
                && !this.canPickUpLoot()
                && this.isAlive()
                && !this.dead) {
            for (ItemFrameEntity itemFrame : this.world.getEntitiesByClass(ItemFrameEntity.class, this.getBoundingBox().expand(vec3i.getX(), vec3i.getY(), vec3i.getZ()), Predicates.alwaysTrue())) {
                if (!itemFrame.isRemoved() && itemFrame.getHeldItemStack().isEmpty()) {
                    ItemStack itemstack = this.getStackInHand(Hand.MAIN_HAND);
                    if (this.getBrain().getOptionalMemory(ModMemoryModules.SELECTED_ITEM_FRAME).isPresent()) {
                        this.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
                        itemFrame.setHeldItemStack(itemstack);
                    }
                }
            }
        }
    }

    protected void usePlayerItem(PlayerEntity player, Hand hand, ItemStack itemStack) {
        if (!player.getAbilities().creativeMode) {
            itemStack.decrement(1);
        }
    }

    private void removeInteractionItem(PlayerEntity player, ItemStack stack) {
        if (!player.getAbilities().creativeMode) {
            stack.decrement(1);
        }
    }

    // =============================================== STACKING ===================================================== //

    public int getStackSize() { return this.dataTracker.get(DATA_STACK_SIZE); }

    public void setStackSize(int size) { this.dataTracker.set(DATA_STACK_SIZE, size); }

    public int getMaxStackSize() {
        return 5; }

    public boolean isStacked() {
        return this.hasVehicle() && this.getVehicle() instanceof TuffGolemEntity;
    }

    public TuffGolemEntity getTuffGolemBelow() {
        return (TuffGolemEntity) this.getVehicle();
    }

    public double getPassengersRidingOffset() {
        return rideDimensions;
    }

    public void setPassengersRidingOffset(double offset) {
        rideDimensions = offset;
    }

    public int getNumOfTuffGolemsAbove(TuffGolemEntity tuffGolem, int i) {
        if (!tuffGolem.hasPassengers()) {
            return i;
        } else {
            return getNumOfTuffGolemsAbove((TuffGolemEntity) tuffGolem.getFirstPassenger(), i + 1);
        }
    }

    public TuffGolemEntity getBottomTuffGolem(TuffGolemEntity tuffGolem) {
        while (tuffGolem.getVehicle() != null) {
            tuffGolem = (TuffGolemEntity) tuffGolem.getVehicle();
        }
        return tuffGolem;
    }

    public TuffGolemEntity getTopTuffGolem(TuffGolemEntity tuffGolem) {
        while (tuffGolem.getFirstPassenger() != null) {
            tuffGolem = (TuffGolemEntity) tuffGolem.getFirstPassenger();
        }
        return tuffGolem;
    }

    public int getHeightDimensionState() {
        return this.dataTracker.get(DATA_HEIGHT_DIMENSION_STATE);
    }

    public void setHeightDimensionState(int state) {
        this.dataTracker.set(DATA_HEIGHT_DIMENSION_STATE, state);
    }

    public int getWidthDimensionState() {
        return this.dataTracker.get(DATA_WIDTH_DIMENSION_STATE);
    }

    public void setWidthDimensionState(int state) {
        this.dataTracker.set(DATA_WIDTH_DIMENSION_STATE, state);
    }

    public void resetDimensionState() {
        this.setWidthDimensionState(1);
        this.setHeightDimensionState(1);
    }

    public EntityDimensions getDimensions(EntityPose pose) {
        return super.getDimensions(pose).scaled(getWidthScale(this.getWidthDimensionState()), getHeightScale(this.getHeightDimensionState()));
    }

    private static float getHeightScale(int scale) {
        return switch (scale) {
            case 1 -> 0.99F;
            case 2 -> 1.9F;
            case 3 -> 2.9F;
            case 4 -> 3.9F;
            case 5 -> 4.9F;
            default -> 0.99F;
        };
    }

    private static float getWidthScale(int scale) {
        return switch (scale) {
            case 1 -> 1.0F;
            case 2 -> 0.85F;
            default -> 1.0F;
        };
    }

    public boolean canStack() { return this.wantsToStack <= 0; }

    public void setWantsToStack(@javax.annotation.Nullable PlayerEntity player) {
        this.wantsToStack = 600;
        if (player != null) {
            this.stackCause = player.getUuid();
        }
        this.world.sendEntityStatus(this, (byte)18);
    }

    public boolean wantsToStack() { return this.wantsToStack > 0; }

    public void resetWantsToStack() {
        this.wantsToStack = 0;
    }

    // =========================================== ANIMATE & PETRIFY ================================================ //

    public void petrify() {
        this.isPetrifying = true;
        this.playSound(ModSounds.PETRIFY_SOUND, 0.3F, 1.0F);
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.0F);
        setAnimated(false);
        setPetrified(true);
        setCanPickUpLoot(false);
    }

    public void animate() {
        this.isAnimating = true;
        this.playSound(ModSounds.ANIMATE_SOUND, 0.3F, 1.0F);
        Objects.requireNonNull(this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)).setBaseValue(0.15F);
        setPetrified(false);
        setAnimated(true);
        setCanPickUpLoot(true);
    }

    public void setPetrified(boolean petrified) {
        Boolean b0 = this.dataTracker.get(DATA_IS_PETRIFIED);
        if (petrified) {
            this.dataTracker.set(DATA_IS_PETRIFIED, true);

        } else {
            this.dataTracker.set(DATA_IS_PETRIFIED, false);
        }
    }

    public void lockState(boolean locked) {
        Boolean b0 = this.dataTracker.get(DATA_STATE_LOCKED);
        if (locked) {
            this.dataTracker.set(DATA_STATE_LOCKED, true);
        } else {
            this.dataTracker.set(DATA_STATE_LOCKED, false);
        }
    }
    public boolean stateLocked() { return (this.dataTracker.get(DATA_STATE_LOCKED)); }

    public void setAnimated(boolean animated) {
        Boolean b0 = this.dataTracker.get(DATA_IS_ANIMATED);
        if (animated) {
            this.dataTracker.set(DATA_IS_ANIMATED, true);
        } else {
            this.dataTracker.set(DATA_IS_ANIMATED, false);
        }
    }

    public boolean isAnimated() { return (this.dataTracker.get(DATA_IS_ANIMATED)); }

    public boolean isPetrified() { return (this.dataTracker.get(DATA_IS_PETRIFIED)); }

    // ================================================ CLOAK ======================================================= //
    public boolean hasCloak() { return (this.dataTracker.get(DATA_HAS_CLOAK)); }

    public DyeColor getCloakColor() { return DyeColor.byId(this.dataTracker.get(DATA_CLOAK_COLOR)); }

    public void setCloakColor(DyeColor dyeColor) { this.dataTracker.set(DATA_CLOAK_COLOR, dyeColor.getId()); }

    // ========================================= GIVE & RECEIVE ITEMS =============================================== //


    // ============================================== AI & BRAIN ==================================================== //

    public void tickMovement() {
        super.tickMovement();
        if (this.getAge() != 0) {
            this.wantsToStack = 0;
        }

        BlockPos pos = this.getBlockPos().up();
            int i = (int) 7L;
            int j = (int) 7L;
            BlockPos.Mutable mutableBlockPos = new BlockPos.Mutable();

            if (this.world.getBlockState(pos).equals(Blocks.AIR.getDefaultState())
                    && this.getMainHandStack().isOf(Items.GLOWSTONE)
                    || this.getMainHandStack().isOf(Items.GLOW_BERRIES)
                    || this.getMainHandStack().isOf(Items.TORCH)
                    || this.getMainHandStack().isOf(Items.SOUL_TORCH)
                    || this.getMainHandStack().isOf(Items.SHROOMLIGHT)) {
                this.world.setBlockState(pos, Blocks.LIGHT.getDefaultState());
            }
            for(int k = 0; k <= j; k = k > 0 ? -k : 1 - k) {
                for(int l = 0; l < i; ++l) {
                    for(int i1 = 0; i1 <= l; i1 = i1 > 0 ? -i1 : 1 - i1) {
                        for(int j1 = i1 < l && i1 > -l ? l : 0; j1 <= l; j1 = j1 > 0 ? -j1 : 1 - j1) {
                            mutableBlockPos.set(pos, i1, k - 1, j1);
                            if (this.world.getBlockState(mutableBlockPos).isOf(Blocks.LIGHT) && !mutableBlockPos.equals(this.getBlockPos())) {
                                //blockPos = mutableBlockPos;
                                this.world.setBlockState(mutableBlockPos, Blocks.AIR.getDefaultState());
                            }
                        }
                    }
                }
            }

        if (!this.hasVehicle() && this.getNumOfTuffGolemsAbove(this, 1) != this.getHeightDimensionState()) {
            this.setHeightDimensionState(this.getNumOfTuffGolemsAbove(this, 1));
            if (this.getFirstPassenger() == null) {
                this.setWidthDimensionState(1);
            }
        }
    }

    protected void mobTick() {
        this.world.getProfiler().push("tuffGolemBrain");
        this.getBrain().tick((ServerWorld)this.world, this);
        this.world.getProfiler().pop();
        this.world.getProfiler().push("tuffGolemActivityUpdate");
        TuffGolemAi.updateActivity(this);
        this.world.getProfiler().pop();
        super.mobTick();
    }

    protected Brain.Profile<TuffGolemEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected Brain<?> deserializeBrain(@NotNull Dynamic<?> dynamic) {
        return TuffGolemAi.makeBrain(this.createBrainProfile().deserialize(dynamic));
    }

    public Brain<TuffGolemEntity> getBrain() {
        return (Brain<TuffGolemEntity>)super.getBrain();
    }


    // =============================================== ANIMATION ==================================================== //

    public void setCloak(boolean hasCloak) {
        Boolean b0 = this.dataTracker.get(DATA_HAS_CLOAK);
        if (hasCloak) {
            this.dataTracker.set(DATA_HAS_CLOAK, true);
        } else {
            this.dataTracker.set(DATA_HAS_CLOAK, false);
        }
    }

    private <E extends IAnimatable> PlayState defaultPredicate(AnimationEvent<E> event) {
        if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.walk", ILoopType.EDefaultLoopTypes.LOOP));
            return PlayState.CONTINUE;
        }
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.idle", ILoopType.EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState animatePredicate(AnimationEvent<E> event) {
        if (this.isAnimating && event.getController().getAnimationState().equals(AnimationState.Stopped)) {
            event.getController().markNeedsReload();
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.animate", ILoopType.EDefaultLoopTypes.PLAY_ONCE));
            this.isAnimating = false;
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState petrifyPredicate(AnimationEvent<E> event) {
        if (this.isPetrifying && event.getController().getAnimationState().equals(AnimationState.Stopped)) {
            event.getController().markNeedsReload();
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.petrify", ILoopType.EDefaultLoopTypes.PLAY_ONCE));
            this.isPetrifying = false;
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState receivePredicate(AnimationEvent<E> event) {
        if (this.isReceiving && event.getController().getAnimationState().equals(AnimationState.Stopped)) {
            event.getController().markNeedsReload();
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.receive", ILoopType.EDefaultLoopTypes.PLAY_ONCE));
            this.isReceiving = false;
        }
        return PlayState.CONTINUE;
    }

    private <E extends IAnimatable> PlayState givePredicate(AnimationEvent<E> event) {
        if (this.isGiving && event.getController().getAnimationState().equals(AnimationState.Stopped)) {
            event.getController().markNeedsReload();
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.tuff_golem.give", ILoopType.EDefaultLoopTypes.PLAY_ONCE));
            this.isGiving = false;
        }
        return PlayState.CONTINUE;
    }

    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller",
                0, this::defaultPredicate));
        data.addAnimationController(new AnimationController(this, "receiveController",
                0, this::receivePredicate));
        data.addAnimationController(new AnimationController(this, "giveController",
                0, this::givePredicate));
        data.addAnimationController(new AnimationController(this, "animateController",
                0, this::animatePredicate));
        data.addAnimationController(new AnimationController(this, "petrifyController",
                0, this::petrifyPredicate));
    }

    public AnimationFactory getFactory() { return this.factory; }

}
