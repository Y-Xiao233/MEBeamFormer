package net.yxiao233.mebeamformer.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yxiao233.mebeamformer.MeBeamFormer;

public class ModCreativeModeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MeBeamFormer.MODID);
    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> MBF_TAB = CREATIVE_MODE_TAB.register("mbf_tab", () -> CreativeModeTab.builder()
            .icon(() -> ModParts.PART.asItem().getDefaultInstance())
            .displayItems((parameters, output) -> {

                ModParts.DR.getEntries().forEach((reg) ->{
                    output.accept(reg.get());
                });
            })
            .title(Component.translatable("itemGroup.mebeamformer"))
            .build()
    );
}
