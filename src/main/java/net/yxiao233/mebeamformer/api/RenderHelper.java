package net.yxiao233.mebeamformer.api;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.yxiao233.mebeamformer.common.parts.PartBeamFormer;
import org.jetbrains.annotations.NotNull;

import java.util.WeakHashMap;

import static net.minecraft.client.renderer.blockentity.BeaconRenderer.BEAM_LOCATION;

@OnlyIn(Dist.CLIENT)
public class RenderHelper {
    private static final WeakHashMap<PartBeamFormer, StaticBloomMetadata> META_CACHE = new WeakHashMap<>();


    public static void renderDynamic(PoseStack poseStack, MultiBufferSource bufferSource, PartBeamFormer partBeamFormer, double x, double y, double z, float partialTicks) {
        if (!partBeamFormer.shouldRenderBeam()) {
            return;
        }

        var metadata = getBloomMetadata(partBeamFormer);
        var rgb = getColor(partBeamFormer);

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(metadata.yaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(metadata.pitch()));
        poseStack.translate(-0.5, 0.35, -0.5);

        ModernBeaconRenderer.renderBeaconBeam(
                poseStack,
                bufferSource,
                BEAM_LOCATION,
                partialTicks,
                -1.0F,
                partBeamFormer.getLevel().getGameTime(),
                0,
                partBeamFormer.getBeamLength() + 0.3F,
                rgb,
                0.2F,
                0.025F,
                false,
                true
        );
    }

    public static float @NotNull [] getColor(PartBeamFormer partBeamFormer) {
        var color = partBeamFormer.getColor();
        var scale = 255f;
        return new float[]{ ((color.mediumVariant >> 16) & 0xff) / scale,
                ((color.mediumVariant >> 8) & 0xff) / scale,
                (color.mediumVariant & 0xff) / scale };
    }

    @NotNull
    public static StaticBloomMetadata getBloomMetadata(PartBeamFormer partBeamFormer) {
        final StaticBloomMetadata metadata;
        if ((metadata = META_CACHE.getOrDefault(partBeamFormer, null)) != null) {
            return metadata;
        } else {

            var facing = partBeamFormer.getSide();
            final var dx = facing.getStepX();
            final var dy = facing.getStepY();
            final var dz = facing.getStepZ();
            final var pitch = (float) Math.atan2(Math.sqrt(dx * dx + dz * dz), dy) * (180F / (float) Math.PI);
            final var yaw = (float) (180 - Math.atan2(dz, dx) * (180F / (float) Math.PI) - 90.0F);

            var newMetadata = new StaticBloomMetadata(dx, dy, dz, pitch, yaw);
            META_CACHE.put(partBeamFormer, newMetadata);
            return newMetadata;
        }
    }

    public record StaticBloomMetadata(int dx, int dy, int dz, float pitch, float yaw) {}
}
