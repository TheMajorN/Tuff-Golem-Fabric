package net.themajorn.tuffgolem.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.world.World;
import net.themajorn.tuffgolem.common.ai.goals.TuffGolemTemptGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LlamaEntity.class)
public class LlamaMixin extends AbstractDonkeyEntity implements RangedAttackMob {

    protected LlamaMixin(EntityType<? extends AbstractDonkeyEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals()V", at = @At("HEAD"))
    private void injectMethod(CallbackInfo info) {
        this.goalSelector.add(4, new TuffGolemTemptGoal(this, 1.2, Ingredient.ofItems(Items.WHEAT), false));
    }

    @Override
    public void attack(LivingEntity target, float pullProgress) {

    }
}
