package net.yxiao233.mebeamformer;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.api.parts.PartModels;
import appeng.items.parts.PartItem;
import appeng.items.parts.PartModelsHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yxiao233.mebeamformer.api.ModItemDefinition;
import net.yxiao233.mebeamformer.common.PartBeamFormer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Mod(MeBeamFormer.MODID)
public class MeBeamFormer {
    public static final String MODID = "mebeamformer";
    public static final TagKey<Item> WRENCH = ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge","tools/wrench"));
    public static final DeferredRegister<Item> DR = DeferredRegister.create(ForgeRegistries.ITEMS,MeBeamFormer.MODID);
    private static final List<ModItemDefinition<?>> ITEMS = new ArrayList<>();
    public static final ModItemDefinition<PartItem<PartBeamFormer>> PART = part("ME Beam Former", "beam_former", PartBeamFormer.class, PartBeamFormer::new);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MeBeamFormer.MODID);
    public static final RegistryObject<CreativeModeTab> MBF_TAB = CREATIVE_MODE_TAB.register("mbf_tab", () -> CreativeModeTab.builder()
            .icon(() -> PART.asItem().getDefaultInstance())
            .displayItems((parameters, output) -> {

                DR.getEntries().forEach((reg) ->{
                    output.accept(reg.get());
                });
            })
            .title(Component.translatable("itemGroup.mebeamformer"))
            .build()
    );

    public MeBeamFormer() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        DR.register(modEventBus);
        CREATIVE_MODE_TAB.register(modEventBus);
    }

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
