package net.yxiao233.mebeamformer.common.registry;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yxiao233.mebeamformer.MeBeamFormer;
import net.yxiao233.mebeamformer.common.parts.PartBeamFormer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ModParts {
    public static final DeferredRegister.Items DR = DeferredRegister.createItems(MeBeamFormer.MODID);
    ;
    private static final List<ItemDefinition<?>> ITEMS = new ArrayList<>();
    public static final ItemDefinition<PartItem<PartBeamFormer>> PART = part("ME Beam Former", "beam_former", PartBeamFormer.class, PartBeamFormer::new);

    private static <T extends IPart> ItemDefinition<PartItem<T>> part(String englishName, String id, Class<T> partClass, Function<IPartItem<T>, T> factory) {
        PartModels.registerModels(PartModelsHelper.createModels(partClass));
        return item(englishName, id, (p) -> {
            return new PartItem<>(p, partClass, factory);
        });
    }

    private static <T extends Item> ItemDefinition<T> item(String englishName, String id, Function<Item.Properties, T> factory) {
        ItemDefinition<T> definition = new ItemDefinition<>(englishName, DR.registerItem(id, factory));
        ITEMS.add(definition);
        return definition;
    }
}
