package net.yxiao233.mebeamformer;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.yxiao233.mebeamformer.common.registry.ModCreativeModeTab;
import net.yxiao233.mebeamformer.common.registry.ModParts;

@Mod(MeBeamFormer.MODID)
public class MeBeamFormer {
    public static final String MODID = "mebeamformer";

    public MeBeamFormer(IEventBus modEventBus, ModContainer modContainer) {

        ModParts.DR.register(modEventBus);
        ModCreativeModeTab.CREATIVE_MODE_TAB.register(modEventBus);
    }
}
