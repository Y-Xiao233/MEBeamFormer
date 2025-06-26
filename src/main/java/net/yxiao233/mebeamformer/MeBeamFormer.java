package net.yxiao233.mebeamformer;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.yxiao233.mebeamformer.common.registry.ModCreativeModeTab;
import net.yxiao233.mebeamformer.common.registry.ModParts;

@Mod(MeBeamFormer.MODID)
public class MeBeamFormer {
    public static final String MODID = "mebeamformer";

    public MeBeamFormer() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModParts.DR.register(modEventBus);
        ModCreativeModeTab.CREATIVE_MODE_TAB.register(modEventBus);
    }
}
