package srki2k.forkedproxy.common.tileentity;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.cyclops.cyclopscore.datastructure.DimPos;
import org.cyclops.cyclopscore.helper.MinecraftHelpers;
import org.cyclops.cyclopscore.persist.IDirtyMarkListener;
import org.cyclops.cyclopscore.persist.nbt.NBTClassType;
import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;
import org.cyclops.integrateddynamics.api.evaluate.EvaluationException;
import org.cyclops.integrateddynamics.api.evaluate.variable.IValue;
import org.cyclops.integrateddynamics.api.evaluate.variable.IVariable;
import org.cyclops.integrateddynamics.api.network.INetworkElement;
import org.cyclops.integrateddynamics.api.network.INetworkEventListener;
import org.cyclops.integrateddynamics.api.network.event.INetworkEvent;
import org.cyclops.integrateddynamics.api.part.PartPos;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderConfig;
import org.cyclops.integrateddynamics.capability.networkelementprovider.NetworkElementProviderSingleton;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerConfig;
import org.cyclops.integrateddynamics.capability.variablecontainer.VariableContainerDefault;
import org.cyclops.integrateddynamics.capability.variablefacade.VariableFacadeHolderConfig;
import org.cyclops.integrateddynamics.core.evaluate.InventoryVariableEvaluator;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueHelpers;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypeInteger;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;
import org.cyclops.integrateddynamics.core.network.event.VariableContentsUpdatedEvent;
import org.cyclops.integrateddynamics.core.tileentity.TileCableConnectableInventory;
import org.cyclops.integratedtunnels.core.part.PartTypeInterfacePositionedAddon;
import srki2k.forkedproxy.ForkedProxy;
import srki2k.forkedproxy.common.block.BlockAccessProxy;
import srki2k.forkedproxy.common.block.BlockAccessProxyConfig;
import srki2k.forkedproxy.common.datamanegmant.WorldProxyManager;
import srki2k.forkedproxy.common.id_network.AccessProxyNetworkElement;
import srki2k.forkedproxy.common.packet.*;
import srki2k.forkedproxy.util.Constants;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class TileAccessProxy extends TileCableConnectableInventory implements IDirtyMarkListener, INetworkEventListener<AccessProxyNetworkElement> {

    public static final int SLOT_X = 0;
    public static final int SLOT_Y = 1;
    public static final int SLOT_Z = 2;
    public static final int SLOT_DISPLAY = 3;
    private int tickCounter;
    public final InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluator_x;
    private int variableX = Integer.MAX_VALUE;
    public final InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluator_y;
    private int variableY = Integer.MAX_VALUE;
    public final InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluator_z;
    private int variableZ = Integer.MAX_VALUE;
    public final InventoryVariableEvaluator<IValue> evaluator_display;
    private IValue display_value;
    private boolean shouldSendUpdateEvent = false;

    public DimPos target;
    private boolean updateTargetRequired;
    public DimPos oldTarget;
    private boolean updateOldTargetRequired;

    public Block targetBlock;

    public int posMode = 0;

    public boolean posModeUpdated;
    public int[] display_rotations = new int[]{0, 0, 0, 0, 0, 0};
    private int[] redstone_powers = new int[]{0, 0, 0, 0, 0, 0};
    private int[] strong_powers = new int[]{0, 0, 0, 0, 0, 0};

    public boolean disable_render = false;

    public TileAccessProxy() {
        super(4, "variables", 1);
        this.inventory.addDirtyMarkListener(this);

        this.addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new AccessProxyNetworkElement(DimPos.of(world, blockPos));
            }
        });
        this.addCapabilityInternal(VariableContainerConfig.CAPABILITY, new VariableContainerDefault());
        this.evaluator_x = new InventoryVariableEvaluator<>(this, SLOT_X, ValueTypes.INTEGER);
        this.evaluator_y = new InventoryVariableEvaluator<>(this, SLOT_Y, ValueTypes.INTEGER);
        this.evaluator_z = new InventoryVariableEvaluator<>(this, SLOT_Z, ValueTypes.INTEGER);
        this.evaluator_display = new InventoryVariableEvaluator<>(this, SLOT_DISPLAY, ValueTypes.CATEGORY_ANY);

        tickCounter = ThreadLocalRandom.current().nextInt(BlockAccessProxyConfig.blockUpdateTicks);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return super.isItemValidForSlot(index, stack) && (stack.isEmpty() || stack.hasCapability(VariableFacadeHolderConfig.CAPABILITY, null));
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack, EnumFacing side) {
        return false;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack itemStack, EnumFacing side) {
        return false;
    }


    @Override
    public boolean hasEventSubscriptions() {
        return true;
    }

    @Override
    public void onEvent(INetworkEvent event, AccessProxyNetworkElement networkElement) {
        if (event instanceof VariableContentsUpdatedEvent) {
            refreshVariables(false);
        }
    }

    @Override
    public Set<Class<? extends INetworkEvent>> getSubscribedEvents() {
        Set<Class<? extends INetworkEvent>> set = new HashSet<>();
        set.add(VariableContentsUpdatedEvent.class);
        return set;
    }

    @Override
    public void afterNetworkReAlive() {
        refreshVariables(true);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag = super.writeToNBT(tag);
        NBTClassType.writeNbt(List.class, "errors_x", evaluator_x.getErrors(), tag);
        NBTClassType.writeNbt(List.class, "errors_y", evaluator_y.getErrors(), tag);
        NBTClassType.writeNbt(List.class, "errors_z", evaluator_z.getErrors(), tag);
        NBTClassType.writeNbt(List.class, "errors_display", evaluator_display.getErrors(), tag);
        tag.setIntArray("display_rotations", this.display_rotations);
        if (getDisplayValue() != null) {
            tag.setTag("displayValue", ValueHelpers.serialize(getDisplayValue()));
        }

        tag.setBoolean("posMode", this.posModeUpdated);
        tag.setInteger("posMode", this.posMode);
        tag.setIntArray("rs_power", this.redstone_powers);
        tag.setIntArray("strong_power", this.strong_powers);
        tag.setBoolean("disable_render", this.disable_render);

        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.evaluator_x.setErrors(NBTClassType.readNbt(List.class, "errors_x", tag));
        this.evaluator_y.setErrors(NBTClassType.readNbt(List.class, "errors_y", tag));
        this.evaluator_z.setErrors(NBTClassType.readNbt(List.class, "errors_z", tag));
        this.evaluator_display.setErrors(NBTClassType.readNbt(List.class, "errors_display", tag));
        this.display_rotations = tag.getIntArray("display_rotations");
        if (tag.hasKey("displayValue", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal())) {
            setDisplayValue(ValueHelpers.deserialize(tag.getCompoundTag("displayValue")));
        } else {
            setDisplayValue(null);
        }

        this.posMode = tag.getInteger("posMode");
        this.posModeUpdated = tag.getBoolean("posMode");
        this.redstone_powers = tag.getIntArray("rs_power");
        this.strong_powers = tag.getIntArray("strong_power");
        this.disable_render = tag.getBoolean("disable_render");

        this.shouldSendUpdateEvent = true;
    }

    @Override
    public void onDirty() {
        if (!this.world.isRemote) {
            refreshVariables(true);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!MinecraftHelpers.isClientSide()) {
            this.shouldSendUpdateEvent = true;
            WorldProxyManager.registerProxy(this);
        }
    }

    @Override
    public void onChunkUnload() {
        if (!world.isRemote) {
            WorldProxyManager.unRegisterProxy(this);
        }
    }

    private boolean haveVariablesUpdated() {
        boolean isDirty = false;

        try {
            int localVariableX = isVariableAvailable(this.evaluator_x) ? getVariableIntValue(this.evaluator_x) : 0;
            if (localVariableX != variableX) {
                variableX = localVariableX;
                isDirty = true;
            }
        } catch (EvaluationException ignored) {
            variableX = 0;
        }

        try {
            int localVariableY = isVariableAvailable(this.evaluator_y) ? getVariableIntValue(this.evaluator_y) : 0;
            if (localVariableY != variableY) {
                variableY = localVariableY;
                isDirty = true;
            }
        } catch (EvaluationException ignored) {
            variableY = 0;
        }

        try {
            int localVariableZ = isVariableAvailable(this.evaluator_z) ? getVariableIntValue(this.evaluator_z) : 0;
            if (localVariableZ != variableZ) {
                variableZ = localVariableZ;
                isDirty = true;
            }
        } catch (EvaluationException ignored) {
            variableZ = 0;
        }

        if (posModeUpdated) {
            isDirty = true;
            posModeUpdated = false;
        }

        return isDirty;
    }

    private boolean isTargetOutOfRange(BlockPos target) {
        if (BlockAccessProxyConfig.range < 0) {
            return false;
        }
        return Math.abs(target.getX() - this.pos.getX()) > BlockAccessProxyConfig.range ||
                Math.abs(target.getY() - this.pos.getY()) > BlockAccessProxyConfig.range ||
                Math.abs(target.getZ() - this.pos.getZ()) > BlockAccessProxyConfig.range;
    }

    private boolean isDisplayDirty() {
        if (disable_render) {
            return false;
        }

        IVariable<IValue> variable = this.evaluator_display.getVariable(getNetwork());
        try {
            if (variable == null) {
                if (getDisplayValue() == null) {
                    return false;
                }
            } else {
                IValue value = variable.getValue();
                if (value == getDisplayValue()) {
                    return false;
                }
                setDisplayValue(value);
            }

        } catch (EvaluationException ignored) {
            setDisplayValue(null);
        }

        ForkedProxy.INSTANCE.getPacketHandler().sendToAll(new UpdateProxyDisplayValuePacket(DimPos.of(this.world, this.pos), getDisplayValue()));
        return true;
    }

    private boolean updateProxyTarget() {
        boolean targetChanged = false;
        boolean blockTargetChanged = false;
        boolean isDirty = false;

        Block oldBlockTarget = this.targetBlock;

        if (haveVariablesUpdated()) {
            oldTarget = this.target == null ? null : DimPos.of(this.target.getDimensionId(), this.target.getBlockPos());
            if (this.posMode == 1) {
                this.target = DimPos.of(this.world, new BlockPos(variableX, variableY, variableZ));
            } else {
                this.target = DimPos.of(this.world, new BlockPos(variableX + this.pos.getX(), variableY + this.pos.getY(), variableZ + this.pos.getZ()));
            }

            targetChanged = !this.target.equals(oldTarget);
            if (targetChanged) {
                ForkedProxy.INSTANCE.getPacketHandler().sendToAll(new UpdateProxyRenderPacket(DimPos.of(this.world, this.pos), this.target));
                updateTargetRequired = true;
                updateOldTargetRequired = true;
            }
        }

        if (isTargetOutOfRange(this.target.getBlockPos())) {
            this.target = DimPos.of(this.world, this.pos);
            this.targetBlock = BlockAccessProxy.getInstance();
            isDirty = true;
        } else if (targetChanged || tickCounter == 0) {
            this.targetBlock = world.getBlockState(target.getBlockPos()).getBlock();
            blockTargetChanged = !this.targetBlock.equals(oldBlockTarget);
            if (blockTargetChanged) {
                if (!BlockAccessProxy.getInstance().equals(this.targetBlock)) {
                    updateTargetRequired = true;
                }
            }
        }

        if (targetChanged || blockTargetChanged) {
            updateProxyAfterTargetChange();
            refreshFacePartNetwork();
            isDirty = true;
        }

        return isDirty;
    }

    private void updateProxyData() {
        if (!getWorld().isRemote) {
            boolean isDisplayDirty = isDisplayDirty();
            boolean updateProxyTarget = updateProxyTarget();

            if (isDisplayDirty || updateProxyTarget) {
                markDirty();
            }
        }
    }

    private void updateTargets() {
        if (tickCounter == BlockAccessProxyConfig.blockUpdateTicks) {
            if (updateOldTargetRequired) {
                updateTargetBlock(oldTarget);
                updateOldTargetRequired = false;
            }

            if (updateTargetRequired) {
                updateTargetBlock(target);
                updateTargetRequired = false;
            }
            tickCounter = 0;
        } else {
            tickCounter++;
        }
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (this.shouldSendUpdateEvent && this.getNetwork() != null) {
            this.shouldSendUpdateEvent = false;
            this.refreshVariables(true);
        }
        updateProxyData();
        updateTargets();
    }

    public void updateProxyAfterTargetChange() {
        for (EnumFacing offset : EnumFacing.VALUES) {
            this.world.neighborChanged(this.pos.offset(offset), getBlockType(), this.pos);
        }
    }

    public void updateTargetBlock(DimPos target) {
        if (target != null) {
            BlockPos pos = target.getBlockPos();
            if (!this.world.isBlockLoaded(pos)) return;

            // TODO: 08/10/2022 see what this is doing
            for (EnumFacing facing : EnumFacing.VALUES) {
                this.world.neighborChanged(pos, this.world.getBlockState(pos).getBlock(), pos.offset(facing));
            }
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (this.world.getBlockState(pos.offset(facing)).getBlock() instanceof BlockAccessProxy) continue;
                this.world.neighborChanged(pos.offset(facing), this.world.getBlockState(pos.offset(facing)).getBlock(), pos);
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //IntegratedTunnels
// TODO: 13/10/2022 Possibly find a better way of doing this 
    public static void refreshFacePartNetwork(World world, BlockPos pos) { //refresh the network of parts on the 6 face of access proxy block
        if (Constants.isIntegratedTunnelsLoaded()) {
            for (EnumFacing offset : EnumFacing.VALUES) {
                try {
                    PartHelpers.PartStateHolder<?, ?> partStateHolder = PartHelpers.getPart(PartPos.of(world, pos.offset(offset), offset.getOpposite()));
                    if (partStateHolder != null && partStateHolder.getPart() instanceof PartTypeInterfacePositionedAddon) {
                        NetworkHelpers.initNetwork(world, pos.offset(offset), offset.getOpposite());
                    }
                } catch (NullPointerException | ConcurrentModificationException ignored) {
                }
            }
        }
    }

    public void refreshFacePartNetwork() { //refresh the network of parts on the 6 face of access proxy block
        refreshFacePartNetwork(world, pos);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //ContainerAccessProxy
    public boolean variableOk(InventoryVariableEvaluator<IValue> evaluator) {
        if (evaluator.getVariable(getNetwork()) == null) {
            return false;
        }
        return evaluator.hasVariable() &&
                evaluator.getErrors().isEmpty();
    }

    public boolean variableIntegerOk(InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluator) {
        if (evaluator.getVariable(getNetwork()) == null) {
            return false;
        }
        return evaluator.hasVariable() &&
                evaluator.getVariable(getNetwork()).getType() instanceof ValueTypeInteger &&
                evaluator.getErrors().isEmpty();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //BlockAccessProxy
    public void unRegisterProxyFromWorld() {
        WorldProxyManager.unRegisterProxy(this);
    }

    public void updateTargetBlock() {
        updateTargetBlock(target);
    }

    public static void updateAfterBlockDestroy(World world, BlockPos pos) {
        for (EnumFacing offset : EnumFacing.VALUES) {
            world.neighborChanged(pos.offset(offset), BlockAccessProxy.getInstance(), pos);
        }
        refreshFacePartNetwork(world, pos);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //ProxyRenderLogic
    public IValue getDisplayValue() {
        return this.display_value;
    }

    public void setDisplayValue(IValue displayValue) {
        this.display_value = displayValue;
    }

    public void rotateDisplayValue(EnumFacing side) {
        int ord = side.getIndex();
        this.display_rotations[ord]++;
        if (this.display_rotations[ord] >= 4) {
            this.display_rotations[ord] = 0;
        }
        markDirty();
        ForkedProxy.INSTANCE.getPacketHandler().sendToAll(new UpdateProxyDisplayRotationPacket(DimPos.of(this.world, this.pos), this.display_rotations));
    }

    public void changeDisableRender() {
        this.disable_render = !this.disable_render;
        markDirty();
        ForkedProxy.INSTANCE.getPacketHandler().sendToAll(new UpdateProxyDisableRenderPacket(DimPos.of(this.world, this.pos), this.disable_render));
    }

    public void sendRemoveRenderPacket() {
        if (!this.world.isRemote) {
            ForkedProxy.INSTANCE.getPacketHandler().sendToAll(new RemoveProxyRenderPacket(DimPos.of(this.world, this.pos)));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //ID Logic
    protected void refreshVariables(boolean sendVariablesUpdateEvent) {
        this.evaluator_x.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
        this.evaluator_y.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
        this.evaluator_z.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
        this.evaluator_display.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
    }

    public int getVariableIntValue(InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluator) throws EvaluationException {
        return evaluator.getVariable(getNetwork()).getValue().cast(ValueTypes.INTEGER).getRawValue();
    }

    private boolean isVariableAvailable(InventoryVariableEvaluator<ValueTypeInteger.ValueInteger> evaluator) {
        if (evaluator.getVariable(getNetwork()) == null) {
            return false;
        }
        return evaluator.hasVariable() && evaluator.getErrors().isEmpty();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Redstone

    public boolean setSideRedstonePower(EnumFacing side, IDynamicRedstone cap) {
        int[] old_strong = this.strong_powers.clone();
        int[] old_power = this.redstone_powers.clone();
        if (cap != null) {
            this.redstone_powers[side.getIndex()] = cap.getRedstoneLevel();
            if (cap.isStrong()) {
                this.strong_powers[side.getIndex()] = cap.getRedstoneLevel();
            } else {
                this.strong_powers[side.getIndex()] = 0;
            }
        } else {
            this.redstone_powers[side.getIndex()] = 0;
            this.strong_powers[side.getIndex()] = 0;
        }
        markDirty();
        return this.redstone_powers != old_power || this.strong_powers != old_strong;
    }

    public int getRedstonePowerForTarget() {
        int power = 0;
        for (int i : this.redstone_powers) {
            power = Math.max(power, i);
        }
        return power;
    }

    public int getStrongPowerForTarget() {
        int power = 0;
        for (int i : this.strong_powers) {
            power = Math.max(power, i);
        }
        return power;
    }

}
