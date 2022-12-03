package net.themajorn.tuffgolem.client.renderers;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.VillagerEntityRenderer;
import net.minecraft.client.render.entity.WolfEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BedItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import net.themajorn.tuffgolem.TuffGolem;
import net.themajorn.tuffgolem.client.models.TuffGolemModel;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.ExtendedGeoEntityRenderer;

public class TuffGolemRenderer extends ExtendedGeoEntityRenderer<TuffGolemEntity> {

    public TuffGolemRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new TuffGolemModel());
        this.addLayer(new TuffGolemCloakLayer(this));
        this.shadowRadius = 0.3F;
    }



    @NotNull
    @Override
    public Identifier getTextureLocation(@NotNull TuffGolemEntity instance) {
        if (instance.hasCloak()) {
            if (instance.isPetrified()) {
                return new Identifier(TuffGolem.MOD_ID, "textures/entities/tuff_golem_petrified_cloaked.png");
            } else {
                return new Identifier(TuffGolem.MOD_ID, "textures/entities/tuff_golem_cloaked.png");
            }
        } else {
            if (instance.isPetrified()) {
                return new Identifier(TuffGolem.MOD_ID, "textures/entities/tuff_golem_petrified.png");
            } else {
                return new Identifier(TuffGolem.MOD_ID, "textures/entities/tuff_golem.png");
            }
        }
    }

    @Override
    protected boolean isArmorBone(GeoBone bone) {
        return bone.getName().startsWith("armor");
    }

    @org.jetbrains.annotations.Nullable
    @Override
    protected Identifier getTextureForBone(String boneName, TuffGolemEntity currentEntity) {
        return null;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    protected ItemStack getHeldItemForBone(String boneName, TuffGolemEntity currentEntity) {
        if ("cape".equals(boneName)) {
            return currentEntity.getStackInHand(Hand.MAIN_HAND);
        }
        return null;
    }

    @Override
    protected net.minecraft.client.render.model.json.ModelTransformation.Mode getCameraTransformForItemAtBone(ItemStack boneItem, String boneName) {
        if ("cape".equals(boneName)) {
            return net.minecraft.client.render.model.json.ModelTransformation.Mode.GROUND;
        }
        return net.minecraft.client.render.model.json.ModelTransformation.Mode.NONE;
    }


    @org.jetbrains.annotations.Nullable
    @Override
    protected BlockState getHeldBlockForBone(String boneName, TuffGolemEntity currentEntity) {
        return null;
    }

    @Override
    protected void preRenderItem(MatrixStack matrixStack, ItemStack item, String boneName, TuffGolemEntity currentEntity, IBone bone) {
        if (shouldRenderAsBlock(item)) {
            matrixStack.translate(0.0, 0.05, -0.55);
            matrixStack.scale(1.1F, 1.1F, 1.1F);
        } else {
            matrixStack.translate(0.0, 0.22, -0.55);
            matrixStack.scale(0.7F, 0.7F, 0.7F);
            float f3 = currentEntity.getSpin(5.5F);
            matrixStack.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(f3));
        }
    }



    @Override
    protected void postRenderItem(MatrixStack matrixStack, ItemStack item, String boneName, TuffGolemEntity currentEntity, IBone bone) {

    }

    @Override
    protected void preRenderBlock(MatrixStack matrixStack, BlockState block, String boneName, TuffGolemEntity currentEntity) {

    }

    @Override
    protected void postRenderBlock(MatrixStack matrixStack, BlockState block, String boneName, TuffGolemEntity currentEntity) {

    }
    private boolean shouldRenderAsBlock(ItemStack item) {
        return item == this.mainHand
                && item.getItem() instanceof BlockItem
                && !(item.getItem() instanceof BedItem)
                && !item.isIn(ItemTags.SAPLINGS)
                && !item.isOf(Items.MANGROVE_PROPAGULE)
                && !item.isIn(ItemTags.FLOWERS)
                && !item.isIn(ItemTags.CANDLES)
                && !item.isIn(ItemTags.DOORS)
                && !item.isOf(Items.BAMBOO)
                && !item.isOf(Items.KELP)
                && !item.isOf(Items.LADDER)
                && !item.isOf(Items.TURTLE_EGG)
                && !item.isOf(Items.CAKE)
                && !item.isOf(Items.CAULDRON)

                && !item.isOf(Items.CHAIN)
                && !item.isOf(Items.COBWEB)
                && !item.isOf(Items.FERN)
                && !item.isOf(Items.LARGE_FERN)
                && !item.isOf(Items.GRASS)
                && !item.isOf(Items.TALL_GRASS)
                && !item.isOf(Items.SEAGRASS)
                && !item.isOf(Items.RAIL)
                && !item.isOf(Items.POWERED_RAIL)
                && !item.isOf(Items.DETECTOR_RAIL)
                && !item.isOf(Items.ACTIVATOR_RAIL)
                && !item.isOf(Items.HOPPER)
                && !item.isOf(Items.WEEPING_VINES)
                && !item.isOf(Items.VINE)
                && !item.isOf(Items.TWISTING_VINES)
                && !item.isOf(Items.WARPED_ROOTS)
                && !item.isOf(Items.WARPED_FUNGUS)
                && !item.isOf(Items.CRIMSON_ROOTS)
                && !item.isOf(Items.CRIMSON_FUNGUS)
                ;
    }
}
