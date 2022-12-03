package net.themajorn.tuffgolem.client.models;

import net.minecraft.util.Identifier;
import net.themajorn.tuffgolem.TuffGolem;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class TuffGolemModel extends AnimatedGeoModel<TuffGolemEntity> {

    @Override
    public Identifier getModelResource(TuffGolemEntity object) {
        if (!object.hasItemInHand()) {
            return  new Identifier(TuffGolem.MOD_ID, "geo/tuff_golem.geo.json");
        } else {
            return  new Identifier(TuffGolem.MOD_ID, "geo/tuff_golem_holding.geo.json");
        }
    }

    @Override
    public Identifier getTextureResource(TuffGolemEntity object) {
        if (object.hasCloak()) {
            if (object.isPetrified()) {
                return new Identifier(TuffGolem.MOD_ID, "textures/entities/tuff_golem_petrified_cloaked.png");
            } else {
                return new Identifier(TuffGolem.MOD_ID, "textures/entities/tuff_golem_cloaked.png");
            }
        } else {
            if (object.isPetrified()) {
                return new Identifier(TuffGolem.MOD_ID, "textures/entities/tuff_golem_petrified.png");
            } else {
                return new Identifier(TuffGolem.MOD_ID, "textures/entities/tuff_golem.png");
            }
        }
    }


    @Override
    public Identifier getAnimationResource(TuffGolemEntity animatable) {
            if (!animatable.hasItemInHand()) {
                if (animatable.isPetrified()) {
                    return new Identifier(TuffGolem.MOD_ID, "animations/tuff_golem_petrified.animation.json");
                } else {
                    return new Identifier(TuffGolem.MOD_ID, "animations/tuff_golem.animation.json");
                }
            } else {
                return new Identifier(TuffGolem.MOD_ID, "animations/tuff_golem_holding.animation.json");
            }
    }
}
