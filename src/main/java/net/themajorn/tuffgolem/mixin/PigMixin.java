package net.themajorn.tuffgolem.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.themajorn.tuffgolem.common.ai.goals.TuffGolemTemptGoal;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PigEntity.class)
public class PigMixin extends AnimalEntity implements ItemSteerable, Saddleable {

    private static final Ingredient PIG_FOOD = Ingredient.ofItems(Items.CARROT, Items.POTATO, Items.BEETROOT);

    protected PigMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals()V", at = @At("HEAD"))
    private void injectMethod(CallbackInfo info) {
        this.goalSelector.add(4, new TuffGolemTemptGoal(this, 1.2, PIG_FOOD, false));
    }

    @Override
    public boolean consumeOnAStickItem() {
        return false;
    }

    @Override
    public void setMovementInput(Vec3d movementInput) {

    }

    @Override
    public float getSaddledSpeed() {
        return 0;
    }

    @Override
    public boolean canBeSaddled() {
        return false;
    }

    @Override
    public void saddle(@Nullable SoundCategory sound) {

    }

    @Override
    public boolean isSaddled() {
        return false;
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }
}
