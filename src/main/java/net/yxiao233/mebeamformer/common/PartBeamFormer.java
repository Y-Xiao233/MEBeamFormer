package net.yxiao233.mebeamformer.common;

import appeng.api.networking.*;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.core.AELog;
import appeng.items.parts.PartModels;
import appeng.me.GridNode;
import appeng.parts.PartModel;
import appeng.util.Platform;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.yxiao233.mebeamformer.MeBeamFormer;
import net.yxiao233.mebeamformer.api.ModBasePart;
import net.yxiao233.mebeamformer.api.RenderHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class PartBeamFormer extends ModBasePart implements IGridTickable{
    @PartModels
    public static final ResourceLocation PRISM_LOC =
            ResourceLocation.fromNamespaceAndPath(MeBeamFormer.MODID, "part/beam_former_prism");
    @PartModels
    public static final ResourceLocation STATUS_OFF_LOC =
            ResourceLocation.fromNamespaceAndPath(MeBeamFormer.MODID, "part/beam_former_status_off");
    @PartModels
    public static final ResourceLocation STATUS_ON_LOC =
            ResourceLocation.fromNamespaceAndPath(MeBeamFormer.MODID, "part/beam_former_status_on");
    @PartModels
    public static final ResourceLocation STATUS_BEAMING_LOC =
            ResourceLocation.fromNamespaceAndPath(MeBeamFormer.MODID, "part/beam_former_status_beaming");
    @PartModels
    private static final ResourceLocation MODEL_BASE_LOC =
            ResourceLocation.fromNamespaceAndPath(MeBeamFormer.MODID, "part/beam_former_base");

    public static final IPartModel MODEL_BEAMING = new PartModel(STATUS_BEAMING_LOC, MODEL_BASE_LOC);
    public static final IPartModel MODEL_ON = new PartModel(STATUS_ON_LOC, MODEL_BASE_LOC, PRISM_LOC);
    public static final IPartModel MODEL_OFF = new PartModel(STATUS_OFF_LOC, MODEL_BASE_LOC, PRISM_LOC);

    private Long2ObjectLinkedOpenHashMap<BlockPos> listenerLinkedList = new Long2ObjectLinkedOpenHashMap<>();
    private int beamLength = 0;
    private int maxLength = 32;
    @OnlyIn(Dist.CLIENT)
    private boolean paired;
    private PartBeamFormer otherBeamFormer = null;
    private boolean hideBeam;
    private IGridConnection connection;

    public PartBeamFormer(IPartItem<?> is) {
        super(is);
        this.getMainNode().addService(IGridTickable.class, this);
    }

    @Override
    public boolean onPartActivate(Player player, InteractionHand hand, Vec3 pos) {
        if(hand == InteractionHand.MAIN_HAND && player.getMainHandItem().is(MeBeamFormer.WRENCH)){
            if(this.getLevel().isClientSide()){
                if(hideBeam){
                    player.sendSystemMessage(Component.translatable("message.mabeamformer.show"));
                    hideBeam = false;
                }else {
                    player.sendSystemMessage(Component.translatable("message.mabeamformer.hide"));
                    hideBeam = true;
                }
            }
        }
        return super.onPartActivate(player, hand, pos);
    }

    public boolean shouldRenderBeam() {
        return !this.hideBeam && this.beamLength != 0 && this.isActive() && this.isPowered();
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderDynamic(float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int combinedLightIn, int combinedOverlayIn) {
        super.renderDynamic(partialTicks, poseStack, buffers, combinedLightIn, combinedOverlayIn);
        BlockPos pos = this.getBlockEntity().getBlockPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        RenderHelper.renderDynamic(poseStack,buffers,this, x, y, z, partialTicks);
    }

    @Override
    public boolean requireDynamicRender() {
        return true;
    }

    public int getBeamLength() {
        return beamLength;
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        this.getMainNode().ifPresent((grid, node) -> {
            grid.getTickManager().wakeDevice(node);
        });
        super.onMainNodeStateChanged(reason);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode iGridNode) {
        return new TickingRequest(2,20,false,true);
    }

    @Override
    public void removeFromWorld() {
        super.removeFromWorld();
        this.disconnect(null);
    }

    @Override
    public void setPartHostInfo(Direction side, IPartHost host, BlockEntity blockEntity) {
        super.setPartHostInfo(side, host, blockEntity);
    }

    @Override
    public int getLightLevel() {
        return !this.hideBeam
                && ((Platform.isClient() && this.paired) || this.beamLength != 0 || this.otherBeamFormer != null)
                && (this.isActive() && this.isPowered()) ? 15 : 0;
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 5f;
    }

    public boolean disconnect(@Nullable BlockPos breakPos) {
        if (this.connection == null) {
            return false;
        }

        var newBeamA = 0;
        var newBeamB = 0;

        if (breakPos != null && !this.listenerLinkedList.isEmpty()) {
            var iterator = this.listenerLinkedList.long2ObjectEntrySet().fastIterator();
            var hash = breakPos.asLong();
            while (iterator.hasNext()) {
                if (iterator.next().getLongKey() == hash) break;
                newBeamA++;
            }

            while (iterator.hasNext()) {
                iterator.next();
                newBeamB++;
            }
        }

        this.beamLength = newBeamA;
        if (this.connection != null) {
            this.connection.destroy();
            this.connection = null;
        }
        this.getHost().markForUpdate();
        this.getHost().markForSave();

        if (this.otherBeamFormer != null && this.otherBeamFormer.otherBeamFormer == this) {
            this.otherBeamFormer.beamLength = newBeamB;
            this.otherBeamFormer.connection = null;
            this.otherBeamFormer.otherBeamFormer = null;
            this.otherBeamFormer.getHost().markForUpdate();
            this.otherBeamFormer.getHost().markForSave();
            this.otherBeamFormer = null;
        }

        return true;
    }

    private void connect(PartBeamFormer potentialFormer, Iterable<BlockPos> poses){
        if(!canConnection(this,potentialFormer)){
            return;
        }
        var myNode = this.getGridNode();
        this.connection = GridHelper.createConnection(myNode,potentialFormer.getGridNode());

        potentialFormer.connection = this.connection;
        this.otherBeamFormer = potentialFormer;
        potentialFormer.otherBeamFormer = this;

        if (potentialFormer.hideBeam || this.hideBeam) {
            potentialFormer.hideBeam = true;
            this.hideBeam = true;
        }

        this.listenerLinkedList = new Long2ObjectLinkedOpenHashMap<>();
        for (var loc : poses)
            this.listenerLinkedList.put(loc.asLong(), loc);

        this.beamLength = this.listenerLinkedList.size();
        this.otherBeamFormer.beamLength = 0;

        this.otherBeamFormer.getMainNode().getGrid().getTickManager().sleepDevice(this.otherBeamFormer.getGridNode());

        this.getHost().markForUpdate();
        this.getHost().markForSave();
        this.otherBeamFormer.getHost().markForUpdate();
        this.otherBeamFormer.getHost().markForSave();
    }
    private boolean isTranslucent(BlockState newState) {
        return true;
    }
    @Override
    public TickRateModulation tickingRequest(IGridNode iGridNode, int m) {
        if (!this.getMainNode().isReady()) return TickRateModulation.SAME;

        var isConnectionValid = this.connection != null;

        var myNode = this.getGridNode();
        if (myNode == null) {
            AELog.error("what the hell, where's my node");
            return TickRateModulation.SLOWER;
        }
        var host = this.getHost();
        var side = this.getSide();

        var loc = host.getBlockEntity().getBlockPos();
        var dir = side.getNormal();
        var world = host.getLocation().getLevel();
        var opposite = side.getOpposite();
        var blockSet = new LinkedHashSet<BlockPos>();

        for (var i = 0; i < 32; i++) {
            loc = loc.offset(dir);

            var te = world.getBlockEntity(loc);
            if (te instanceof IPartHost ph) {
                var part = ph.getPart(opposite);
                if (part instanceof PartBeamFormer potentialFormer) {
                    if (isConnectionValid && potentialFormer == this.otherBeamFormer && this.otherBeamFormer.otherBeamFormer == this) {
                        return TickRateModulation.SLEEP;
                    }

                    var disconnected = this.disconnect(loc);

                    if (potentialFormer.getMainNode().isReady() && potentialFormer.otherBeamFormer == null) {
                        this.connect(potentialFormer, blockSet);
                        return TickRateModulation.SLEEP;
                    }
                    return disconnected ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
                }

                if (ph.getPart(side) instanceof PartBeamFormer) {
                    return this.disconnect(loc) ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
                }
            }

            var block = world.getBlockState(loc);
            if (!isTranslucent(block)) {
                return this.disconnect(loc) ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
            }

            blockSet.add(loc);
        }

        return TickRateModulation.SLOWER;
    }
    @Override
    public AEColor getColor() {
        return this.getHost().getColor();
    }

    @Override
    public Level getLevel() {
        return this.getHost().getBlockEntity().getLevel();
    }

    boolean hasConnection(IGridNode thisSide,IGridNode otherSide) {
        Iterator<IGridConnection> var2 = thisSide.getConnections().iterator();

        IGridConnection gc;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            gc = var2.next();
        } while(gc.a() != otherSide && gc.b() != otherSide);

        return true;
    }
    
    boolean canConnection(PartBeamFormer p0, PartBeamFormer p1){
        if(p0 == null || p1 == null){
            return false;
        }
        GridNode n0 = (GridNode) p0.getGridNode();
        GridNode n1 = (GridNode) p1.getGridNode();
        if(n0 == null || n1 == null){
            return false;
        }
        return !hasConnection(n0,n1) && !hasConnection(n1,n0);
    }


    public @NotNull IPartModel getStaticModels() {
        return !(this.isActive() && this.isPowered()) ? MODEL_OFF : (this.paired ? MODEL_BEAMING : MODEL_ON);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(10, 10, 12, 6, 6, 11);
        bch.addBox(10, 10, 13, 6, 6, 12);
        bch.addBox(10, 6, 14, 6, 5, 13);
        bch.addBox(11, 9, 17, 10, 7, 14);
        bch.addBox(9, 11, 17, 7, 10, 14);
        bch.addBox(6, 9, 17, 5, 7, 14);
        bch.addBox(9, 6, 17, 7, 5, 14);
        bch.addBox(10, 11, 14, 6, 10, 13);
        bch.addBox(6, 10, 14, 5, 6, 13);
        bch.addBox(11, 9, 13, 10, 7, 12);
        bch.addBox(6, 9, 13, 5, 7, 12);
        bch.addBox(9, 6, 13, 7, 5, 12);
        bch.addBox(9, 11, 13, 7, 10, 12);
        bch.addBox(11, 10, 14, 10, 6, 13);
    }

    public void markForUpdate() {
        this.getHost().markForUpdate();
    }

    @Override
    public boolean readFromStream(FriendlyByteBuf data){
        var shouldRedraw = super.readFromStream(data);

        this.beamLength = data.readInt();
        var wasPaired = this.paired;
        this.paired = data.readBoolean();
        // Kick rendering.
        if (this.paired != wasPaired) {
            var pos = this.getBlockEntity().getBlockPos();
            var x = pos.getX();
            var y = pos.getY();
            var z = pos.getZ();
            Minecraft.getInstance().levelRenderer.setSectionDirty(x,y,z);
        }
        this.hideBeam = data.readBoolean();

        return shouldRedraw;
    }

    @Override
    public void writeToStream(FriendlyByteBuf data){
        super.writeToStream(data);
        data.writeInt(this.beamLength);
        data.writeBoolean(this.otherBeamFormer != null);
        data.writeBoolean(this.hideBeam);
    }

    @Override
    public void writeToNBT(CompoundTag data) {
        super.writeToNBT(data);

        var part = data.getCompound("part");
        if (this.beamLength > 0) part.putInt("beamLength", this.beamLength);
        if (this.hideBeam) part.putBoolean("hideBeam", true);
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);

        var part = data.getCompound("part");

        if (part.get("beamLength") instanceof DoubleTag dbl) {
            this.beamLength = (int) dbl.getAsDouble();
        } else {
            this.beamLength = part.getInt("beamLength");
        }
        this.hideBeam = part.getBoolean("hideBeam");
    }
}