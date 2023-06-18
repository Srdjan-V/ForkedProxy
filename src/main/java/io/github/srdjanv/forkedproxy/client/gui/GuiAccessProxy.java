package io.github.srdjanv.forkedproxy.client.gui;

import io.github.srdjanv.forkedproxy.client.data.AccessProxyClientData;
import io.github.srdjanv.forkedproxy.client.data.ProxyPosData;
import io.github.srdjanv.forkedproxy.common.container.ContainerAccessProxy;
import io.github.srdjanv.forkedproxy.common.tileentity.TileAccessProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.cyclops.cyclopscore.client.gui.component.input.GuiNumberField;
import org.cyclops.cyclopscore.client.gui.container.GuiContainerConfigurable;
import org.cyclops.cyclopscore.helper.RenderHelpers;
import org.cyclops.cyclopscore.helper.ValueNotifierHelpers;
import org.cyclops.integrateddynamics.core.client.gui.container.DisplayErrorsComponent;
import org.cyclops.integrateddynamics.core.evaluate.variable.ValueTypes;

import java.io.IOException;

public class GuiAccessProxy extends GuiContainerConfigurable<ContainerAccessProxy> {
    public DisplayErrorsComponent errorX = new DisplayErrorsComponent();
    public DisplayErrorsComponent errorY = new DisplayErrorsComponent();
    public DisplayErrorsComponent errorZ = new DisplayErrorsComponent();
    public DisplayErrorsComponent errorDisplay = new DisplayErrorsComponent();

    private GuiNumberField numberFieldUpdateInterval = null;

    private static final int ERRORS_X = 20;
    private static final int ERRORS_Y = 99;

    public GuiAccessProxy(InventoryPlayer inventory, TileAccessProxy tile) {
        super(new ContainerAccessProxy(inventory, tile));
    }

    @Override
    public void initGui() {
        super.initGui();
        numberFieldUpdateInterval = new GuiNumberField(0, Minecraft.getMinecraft().fontRenderer, guiLeft + 92, guiTop + 24, 82, 14, true, true);
        numberFieldUpdateInterval.setPositiveOnly(true);
        numberFieldUpdateInterval.setMaxStringLength(64);
        numberFieldUpdateInterval.setMaxStringLength(15);
        numberFieldUpdateInterval.setVisible(true);
        numberFieldUpdateInterval.setMinValue(1);
        numberFieldUpdateInterval.setTextColor(16777215);
        numberFieldUpdateInterval.setCanLoseFocus(true);
        numberFieldUpdateInterval.setText(Integer.toString(getContainer().getLastUpdateValue()));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!this.checkHotbarKeys(keyCode)) {
            if (!this.numberFieldUpdateInterval.textboxKeyTyped(typedChar, keyCode)) {
                super.keyTyped(typedChar, keyCode);
            } else {
                onValueChanged();
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        numberFieldUpdateInterval.mouseClicked(mouseX, mouseY, mouseButton);
        onValueChanged();
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (RenderHelpers.isPointInRegion(offsetX + guiLeft + 44, offsetY + guiTop + 48, 90, 16, mouseX - offsetX, mouseY - offsetY) && state == 0) {
            ValueNotifierHelpers.setValue(getContainer(), getContainer().lastPosModeValueId, this.getContainer().getLastPosModeValue() == 0 ? 1 : 0);
        }
    }

    protected void onValueChanged() {
        int updateInterval = 1;
        try {
            updateInterval = numberFieldUpdateInterval.getInt();
        } catch (NumberFormatException ignored) {
        }
        ValueNotifierHelpers.setValue(getContainer(), getContainer().lastUpdateTickDelayID, updateInterval);
    }

    @Override
    public void onUpdate(int valueId, NBTTagCompound value) {
        if (valueId == getContainer().lastUpdateTickDelayID) {
            numberFieldUpdateInterval.setText(Integer.toString(getContainer().getLastUpdateValue()));
        }
    }

    @Override
    protected ResourceLocation constructResourceLocation() {
        return new ResourceLocation(getGuiTexture());
    }

    @Override
    public String getGuiTexture() {
        return "integrated_proxy:textures/gui/access_proxy_gui.png";
    }

    @Override
    protected int getBaseYSize() {
        return 198;
    }

    protected int getBaseXSize() {
        return 177;
    }


    @Override
    public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
        fontRendererIn.drawString(text, (float) (x - fontRendererIn.getStringWidth(text) / 2), (float) y, color, false);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);


        mc.getTextureManager().bindTexture(texture);
        drawTexturedModalRect(offsetX + guiLeft, offsetY + guiTop, 0, 0, getBaseXSize(), getBaseYSize());

        GlStateManager.color(1.0F, 1.0F, 1.0F);
        TileAccessProxy tile = getContainer().getTile();
        errorX.drawBackground(tile.getEvaluatorX().getErrors(), ERRORS_X + 36 * 0 + 9, ERRORS_Y, ERRORS_X + 36 * 0 + 9, ERRORS_Y, this, guiLeft, guiTop, getContainer().variableOk(getContainer().lastXOkId));
        errorY.drawBackground(tile.getEvaluatorY().getErrors(), ERRORS_X + 36 * 1 + 9, ERRORS_Y, ERRORS_X + 36 * 1 + 9, ERRORS_Y, this, guiLeft, guiTop, getContainer().variableOk(getContainer().lastYOkId));
        errorZ.drawBackground(tile.getEvaluatorZ().getErrors(), ERRORS_X + 36 * 2 + 9, ERRORS_Y, ERRORS_X + 36 * 2 + 9, ERRORS_Y, this, guiLeft, guiTop, getContainer().variableOk(getContainer().lastZOkId));
        errorDisplay.drawBackground(tile.getEvaluatorDisplay().getErrors(), ERRORS_X + 36 * 3 + 9, ERRORS_Y, ERRORS_X + 36 * 3 + 9, ERRORS_Y, this, guiLeft, guiTop, getContainer().variableOk(getContainer().lastDisplayOkId));

        drawCenteredString(fontRenderer, I18n.format("integrated_proxy.gui.access_proxy.ticks"), offsetX + guiLeft + 27 + 9 + 11, offsetY + guiTop + 24 + 2, 4210752);
        numberFieldUpdateInterval.drawTextBox(Minecraft.getMinecraft(), mouseX - guiLeft, mouseY - guiTop);

        if (this.getContainer().getLastPosModeValue() == 0) {
            drawCenteredString(this.fontRenderer, I18n.format("integrated_proxy.gui.access_proxy.relative_mode"), offsetX + this.guiLeft + 88, offsetY + this.guiTop + 52, 4210752);
        } else {
            drawCenteredString(this.fontRenderer, I18n.format("integrated_proxy.gui.access_proxy.absolute_mode"), offsetX + this.guiLeft + 88, offsetY + this.guiTop + 52, 4210752);
        }
        drawCenteredString(fontRenderer, I18n.format("integrated_proxy.gui.access_proxy.x"), offsetX + guiLeft + 27 + 36 * 0 + 9, offsetY + guiTop + 70, 4210752);
        drawCenteredString(fontRenderer, I18n.format("integrated_proxy.gui.access_proxy.y"), offsetX + guiLeft + 27 + 36 * 1 + 9, offsetY + guiTop + 70, 4210752);
        drawCenteredString(fontRenderer, I18n.format("integrated_proxy.gui.access_proxy.z"), offsetX + guiLeft + 27 + 36 * 2 + 9, offsetY + guiTop + 70, 4210752);
        drawCenteredString(fontRenderer, I18n.format("integrated_proxy.gui.access_proxy.display_value"), offsetX + guiLeft + 27 + 36 * 3 + 9, offsetY + guiTop + 70, 4210752);

        ProxyPosData proxyPosData = AccessProxyClientData.getProxyData(getContainer().getTile().getWorld().provider.getDimension(), getContainer().getTile().getPos());

        String pos_str;

        if (proxyPosData != null && proxyPosData.getTarget() != null) {
            BlockPos target = proxyPosData.getTarget();
            pos_str = I18n.format(
                    "integrated_proxy.gui.access_proxy.display_pos",
                    target.getX(),
                    target.getY(),
                    target.getZ()
            );
        } else {
            pos_str = "Init";
        }
        RenderHelpers.drawScaledCenteredString(fontRenderer, pos_str, getGuiLeftTotal() + 94, getGuiTopTotal() + 11, 76, ValueTypes.INTEGER.getDisplayColor());
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        GlStateManager.translate(1, 1, 1);
        TileAccessProxy tile = getContainer().getTile();
        errorX.drawForeground(tile.getEvaluatorX().getErrors(), ERRORS_X + 36 * 0 + 9, ERRORS_Y, mouseX, mouseY, this, guiLeft, guiTop);
        errorY.drawForeground(tile.getEvaluatorY().getErrors(), ERRORS_X + 36 * 1 + 9, ERRORS_Y, mouseX, mouseY, this, guiLeft, guiTop);
        errorZ.drawForeground(tile.getEvaluatorZ().getErrors(), ERRORS_X + 36 * 2 + 9, ERRORS_Y, mouseX, mouseY, this, guiLeft, guiTop);
        errorDisplay.drawForeground(tile.getEvaluatorDisplay().getErrors(), ERRORS_X + 36 * 3 + 9, ERRORS_Y, mouseX, mouseY, this, guiLeft, guiTop);
    }
}
