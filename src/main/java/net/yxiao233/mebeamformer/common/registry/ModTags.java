package net.yxiao233.mebeamformer.common.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;

public class ModTags {
    public static class Item{
        public static final TagKey<net.minecraft.world.item.Item> WRENCH = ItemTags.create(ResourceLocation.fromNamespaceAndPath("c","tools/wrench"));
    }
}
