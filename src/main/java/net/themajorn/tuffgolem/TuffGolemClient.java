package net.themajorn.tuffgolem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.themajorn.tuffgolem.client.renderers.TuffGolemRenderer;
import net.themajorn.tuffgolem.core.registry.ModEntities;

public class TuffGolemClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        EntityRendererRegistry.register(ModEntities.TUFF_GOLEM, (context) ->
        {
            return new TuffGolemRenderer(context);
        });
    }
}
