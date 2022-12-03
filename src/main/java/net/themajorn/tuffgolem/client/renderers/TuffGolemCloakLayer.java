package net.themajorn.tuffgolem.client.renderers;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.themajorn.tuffgolem.TuffGolem;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class TuffGolemCloakLayer extends GeoLayerRenderer<TuffGolemEntity> {

    private static final Identifier LAYER = new Identifier(TuffGolem.MOD_ID, "textures/entities/tuff_golem_cloak.png");
    private static final Identifier DEFAULT_MODEL = new Identifier(TuffGolem.MOD_ID, "geo/tuff_golem.geo.json");
    private static final Identifier HOLDING_MODEL = new Identifier(TuffGolem.MOD_ID, "geo/tuff_golem_holding.geo.json");
    public TuffGolemCloakLayer(IGeoRenderer<TuffGolemEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, VertexConsumerProvider bufferIn, int packedLightIn, TuffGolemEntity tuffGolem, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (tuffGolem.hasCloak() && !tuffGolem.isInvisible()) {
            float[] diffuseColors = tuffGolem.getCloakColor().getColorComponents();
            RenderLayer cameo =  RenderLayer.getArmorCutoutNoCull(LAYER);
            matrixStackIn.push();
            matrixStackIn.scale(1.0f, 1.0f, 1.0f);
            matrixStackIn.translate(0.0d, 0.0d, 0.0d);
            if (!tuffGolem.hasItemInHand()) {
                this.getRenderer().render(this.getEntityModel().getModel(DEFAULT_MODEL), tuffGolem, partialTicks, cameo, matrixStackIn, bufferIn,
                        bufferIn.getBuffer(cameo), packedLightIn, OverlayTexture.DEFAULT_UV, diffuseColors[0], diffuseColors[1], diffuseColors[2], 1f);
            } else {
                this.getRenderer().render(this.getEntityModel().getModel(HOLDING_MODEL), tuffGolem, partialTicks, cameo, matrixStackIn, bufferIn,
                        bufferIn.getBuffer(cameo), packedLightIn, OverlayTexture.DEFAULT_UV, diffuseColors[0], diffuseColors[1], diffuseColors[2], 1f);
            }
            matrixStackIn.pop();
        }
    }
}
