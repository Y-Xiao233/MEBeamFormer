package net.yxiao233.mebeamformer.common.registry;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.yxiao233.mebeamformer.MeBeamFormer;
import net.yxiao233.mebeamformer.api.ModItemDefinition;
import net.yxiao233.mebeamformer.common.parts.PartBeamFormer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ModParts {
    public static final DeferredRegister<Item> DR = DeferredRegister.create(ForgeRegistries.ITEMS, MeBeamFormer.MODID);
    private static final List<ModItemDefinition<?>> ITEMS = new ArrayList<>();
    public static final ModItemDefinition<PartItem<PartBeamFormer>> PART = part("ME Beam Former", "beam_former", PartBeamFormer.class, PartBeamFormer::new);

    private static <T extends Item> ModItemDefinition<T> item(String englishName, String id, Function<Item.Properties, T> factory) {
        ModItemDefinition<T> definition = new ModItemDefinition(englishName, DR.register(id, () -> {
            return (Item)factory.apply(new Item.Properties());
        }));
        ITEMS.add(definition);
        return definition;
    }
    private static <T extends IPart> ModItemDefinition<PartItem<T>> part(String englishName, String id, Class<T> partClass, Function<IPartItem<T>, T> factory) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return item(englishName, id, (p) -> {
            return new PartItem<>(p, partClass, factory);
        });
    }
}
