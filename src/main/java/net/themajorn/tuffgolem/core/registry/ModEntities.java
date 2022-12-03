package net.themajorn.tuffgolem.core.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.themajorn.tuffgolem.TuffGolem;
import net.themajorn.tuffgolem.common.entities.TuffGolemEntity;

public class ModEntities {

    public static final EntityType<TuffGolemEntity> TUFF_GOLEM = Registry.register(
            Registry.ENTITY_TYPE, new Identifier(TuffGolem.MOD_ID, "tuff_golem"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, TuffGolemEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6F, 0.9F)).build());
}
