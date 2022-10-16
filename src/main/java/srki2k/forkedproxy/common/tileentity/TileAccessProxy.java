package srki2k.forkedproxy.common.tileentity;

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

public class TileAccessProxy extends TileCableConnectableInventory implements IDirtyMarkListener, INetworkEventListener<AccessProxyNetworkElement> {

    public static final int SLOT_X = 0;
    public static final int SLOT_Y = 1;
    public static final int SLOT_Z = 2;
    public static final int SLOT_DISPLAY = 3;
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
    public int posMode = 0;

    public boolean posModeUpdated;
    public int[] display_rotations = new int[]{0, 0, 0, 0, 0, 0};
    private int[] redstone_powers = new int[]{0, 0, 0, 0, 0, 0};
    private int[] strong_powers = new int[]{0, 0, 0, 0, 0, 0};

    public boolean disable_render = false;

    public TileAccessProxy() {
        super(4, "variables", 1);
        inventory.addDirtyMarkListener(this);

        addCapabilityInternal(NetworkElementProviderConfig.CAPABILITY, new NetworkElementProviderSingleton() {
            @Override
            public INetworkElement createNetworkElement(World world, BlockPos blockPos) {
                return new AccessProxyNetworkElement(DimPos.of(world, blockPos));
            }
        });
        addCapabilityInternal(VariableContainerConfig.CAPABILITY, new VariableContainerDefault());
        evaluator_x = new InventoryVariableEvaluator<>(this, SLOT_X, ValueTypes.INTEGER);
        evaluator_y = new InventoryVariableEvaluator<>(this, SLOT_Y, ValueTypes.INTEGER);
        evaluator_z = new InventoryVariableEvaluator<>(this, SLOT_Z, ValueTypes.INTEGER);
        evaluator_display = new InventoryVariableEvaluator<>(this, SLOT_DISPLAY, ValueTypes.CATEGORY_ANY);

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
        tag.setIntArray("display_rotations", display_rotations);
        if (getDisplayValue() != null) {
            tag.setTag("displayValue", ValueHelpers.serialize(getDisplayValue()));
        }

        tag.setBoolean("posMode", posModeUpdated);
        tag.setInteger("posMode", posMode);
        tag.setIntArray("rs_power", redstone_powers);
        tag.setIntArray("strong_power", strong_powers);
        tag.setBoolean("disable_render", disable_render);

        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        evaluator_x.setErrors(NBTClassType.readNbt(List.class, "errors_x", tag));
        evaluator_y.setErrors(NBTClassType.readNbt(List.class, "errors_y", tag));
        evaluator_z.setErrors(NBTClassType.readNbt(List.class, "errors_z", tag));
        evaluator_display.setErrors(NBTClassType.readNbt(List.class, "errors_display", tag));
        display_rotations = tag.getIntArray("display_rotations");
        if (tag.hasKey("displayValue", MinecraftHelpers.NBTTag_Types.NBTTagCompound.ordinal())) {
            setDisplayValue(ValueHelpers.deserialize(tag.getCompoundTag("displayValue")));
        } else {
            setDisplayValue(null);
        }

        posMode = tag.getInteger("posMode");
        posModeUpdated = tag.getBoolean("posMode");
        redstone_powers = tag.getIntArray("rs_power");
        strong_powers = tag.getIntArray("strong_power");
        disable_render = tag.getBoolean("disable_render");

        shouldSendUpdateEvent = true;
    }

    @Override
    public void onDirty() {
        if (!world.isRemote) {
            refreshVariables(true);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!MinecraftHelpers.isClientSide()) {
            shouldSendUpdateEvent = true;
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
            int localVariableX = isVariableAvailable(evaluator_x) ? getVariableIntValue(evaluator_x) : 0;
            if (localVariableX != variableX) {
                variableX = localVariableX;
                isDirty = true;
            }
        } catch (EvaluationException ignored) {
            variableX = 0;
        }

        try {
            int localVariableY = isVariableAvailable(evaluator_y) ? getVariableIntValue(evaluator_y) : 0;
            if (localVariableY != variableY) {
                variableY = localVariableY;
                isDirty = true;
            }
        } catch (EvaluationException ignored) {
            variableY = 0;
        }

        try {
            int localVariableZ = isVariableAvailable(evaluator_z) ? getVariableIntValue(evaluator_z) : 0;
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
        return Math.abs(target.getX() - pos.getX()) > BlockAccessProxyConfig.range ||
                Math.abs(target.getY() - pos.getY()) > BlockAccessProxyConfig.range ||
                Math.abs(target.getZ() - pos.getZ()) > BlockAccessProxyConfig.range;
    }

    private boolean isDisplayDirty() {
        if (disable_render) {
            return false;
        }

        IVariable<IValue> variable = evaluator_display.getVariable(getNetwork());
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

        ForkedProxy.INSTANCE.getPacketHandler().sendToAll(new UpdateProxyDisplayValuePacket(DimPos.of(world, pos), getDisplayValue()));
        return true;
    }

    private boolean updateProxyTarget() {
        boolean targetChanged = false;
        boolean isDirty = false;

        if (haveVariablesUpdated()) {
            DimPos oldTarget = target == null ? null : DimPos.of(target.getDimensionId(), target.getBlockPos());
            if (posMode == 1) {
                target = DimPos.of(world, new BlockPos(variableX, variableY, variableZ));
            } else {
                target = DimPos.of(world, new BlockPos(variableX + pos.getX(), variableY + pos.getY(), variableZ + pos.getZ()));
            }

            targetChanged = !target.equals(oldTarget);
            if (targetChanged) {
                updateTargetBlock(oldTarget);
                updateTargetBlock(target);
                ForkedProxy.INSTANCE.getPacketHandler().sendToAll(new UpdateProxyRenderPacket(DimPos.of(world, pos), target));
            }
        }

        if (isTargetOutOfRange(target.getBlockPos())) {
            target = DimPos.of(world, pos);
            isDirty = true;
        }

        if (targetChanged) {
            updateProxyAfterTargetChange();
            refreshFacePartNetwork();
            isDirty = true;
        }

        return isDirty;
    }

    private void updateProxyData() {
        boolean isDisplayDirty = isDisplayDirty();
        boolean updateProxyTarget = updateProxyTarget();

        if (isDisplayDirty || updateProxyTarget) {
            markDirty();
        }
    }

    @Override
    protected void updateTileEntity() {
        super.updateTileEntity();
        if (shouldSendUpdateEvent && getNetwork() != null) {
            shouldSendUpdateEvent = false;
            refreshVariables(true);
        }
        if (!world.isRemote) {
            updateProxyData();
        }
    }

    public void updateProxyAfterTargetChange() {
        for (EnumFacing offset : EnumFacing.VALUES) {
            world.neighborChanged(pos.offset(offset), getBlockType(), pos);
        }
    }

    public void updateTargetBlock(DimPos target) {
        if (target != null) {
            BlockPos pos = target.getBlockPos();
            if (!world.isBlockLoaded(pos)) return;

            // TODO: 08/10/2022 see what this is doing
            for (EnumFacing facing : EnumFacing.VALUES) {
                world.neighborChanged(pos, world.getBlockState(pos).getBlock(), pos.offset(facing));
            }
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (world.getBlockState(pos.offset(facing)).getBlock() instanceof BlockAccessProxy) continue;
                world.neighborChanged(pos.offset(facing), world.getBlockState(pos.offset(facing)).getBlock(), pos);
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
        return display_value;
    }

    public void setDisplayValue(IValue displayValue) {
        display_value = displayValue;
    }

    public void rotateDisplayValue(EnumFacing side) {
        int ord = side.getIndex();
        display_rotations[ord]++;
        if (display_rotations[ord] >= 4) {
            display_rotations[ord] = 0;
        }
        markDirty();
        ForkedProxy.INSTANCE.getPacketHandler().sendToAll(new UpdateProxyDisplayRotationPacket(DimPos.of(world, pos), display_rotations));
    }

    public void changeDisableRender() {
        disable_render = !disable_render;
        markDirty();
        ForkedProxy.INSTANCE.getPacketHandler().sendToAll(new UpdateProxyDisableRenderPacket(DimPos.of(world, pos), disable_render));
    }

    public void sendRemoveRenderPacket() {
        if (!world.isRemote) {
            ForkedProxy.INSTANCE.getPacketHandler().sendToAll(new RemoveProxyRenderPacket(DimPos.of(world, pos)));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //ID Logic
    protected void refreshVariables(boolean sendVariablesUpdateEvent) {
        evaluator_x.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
        evaluator_y.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
        evaluator_z.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
        evaluator_display.refreshVariable(getNetwork(), sendVariablesUpdateEvent);
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
        int[] old_strong = strong_powers.clone();
        int[] old_power = redstone_powers.clone();
        if (cap != null) {
            redstone_powers[side.getIndex()] = cap.getRedstoneLevel();
            if (cap.isStrong()) {
                strong_powers[side.getIndex()] = cap.getRedstoneLevel();
            } else {
                strong_powers[side.getIndex()] = 0;
            }
        } else {
            redstone_powers[side.getIndex()] = 0;
            strong_powers[side.getIndex()] = 0;
        }
        markDirty();
        return redstone_powers != old_power || strong_powers != old_strong;
    }

    public int getRedstonePowerForTarget() {
        int power = 0;
        for (int i : redstone_powers) {
            power = Math.max(power, i);
        }
        return power;
    }

    public int getStrongPowerForTarget() {
        int power = 0;
        for (int i : strong_powers) {
            power = Math.max(power, i);
        }
        return power;
    }

}
