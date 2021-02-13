package edu.uidaho.electricblocks.guis;

import edu.uidaho.electricblocks.network.ElectricBlocksPacketHandler;
import edu.uidaho.electricblocks.network.TileEntityMessageToServer;
import edu.uidaho.electricblocks.utils.MetricUnit;
import edu.uidaho.electricblocks.tileentities.LoadTileEntity;
import edu.uidaho.electricblocks.utils.PlayerUtils;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.LogicalSide;

public class LoadScreen extends AbstractScreen {

    // Layout elements
    private TextFieldWidget textFieldMaxPower;
    private TextFieldWidget textFieldBusVoltage;
    private TextFieldWidget textFieldResultPower;
    private TextFieldWidget textFieldReactivePower;

    // Info needed to preload the form with data
    private LoadTileEntity loadTileEntity;

    public LoadScreen(LoadTileEntity loadTileEntity, PlayerEntity player) {
        super(new TranslationTextComponent("gui.electricblocks.loadscreen"));
        this.loadTileEntity = loadTileEntity;
        this.player = player;
    }

    @Override
    protected void init() {
        inService = loadTileEntity.isInService();
        textFieldMaxPower = new TextFieldWidget(font, (this.width - TEXT_INPUT_WIDTH) / 2 + (BUTTON_WIDTH - TEXT_INPUT_WIDTH) / 2, 25, TEXT_INPUT_WIDTH, TEXT_INPUT_HEIGHT, "");
        textFieldMaxPower.setText(String.format("%f", loadTileEntity.getMaxPower().getMega()));
        textFieldMaxPower.setFocused2(true);
        textFieldMaxPower.setVisible(true);
        addButton(textFieldMaxPower);
        setFocused(textFieldMaxPower);

        textFieldBusVoltage = new TextFieldWidget(font, (this.width - TEXT_INPUT_WIDTH) / 2 + (BUTTON_WIDTH - TEXT_INPUT_WIDTH) / 2, 55, TEXT_INPUT_WIDTH, TEXT_INPUT_HEIGHT, "");
        textFieldBusVoltage.setText(String.format("%f", loadTileEntity.getBusVoltage().getKilo()));
        textFieldBusVoltage.setVisible(true);
        addButton(textFieldBusVoltage);

        textFieldResultPower = new TextFieldWidget(font, (this.width - TEXT_INPUT_WIDTH) / 2 + (BUTTON_WIDTH - TEXT_INPUT_WIDTH) / 2, 90, TEXT_INPUT_WIDTH, TEXT_INPUT_HEIGHT, "");
        textFieldResultPower.setText(String.format("%f", loadTileEntity.getResultPower().getMega()));
        initializeResultField(textFieldResultPower);

        textFieldReactivePower = new TextFieldWidget(font, (this.width - TEXT_INPUT_WIDTH) / 2 + (BUTTON_WIDTH - TEXT_INPUT_WIDTH) / 2, 120, TEXT_INPUT_WIDTH, TEXT_INPUT_HEIGHT, "");
        textFieldReactivePower.setText(String.format("%f", loadTileEntity.getReactivePower().getMega()));
        initializeResultField(textFieldReactivePower);

        super.init();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        // Draw property labels
        this.drawString(this.font, "Max Power", (this.width - TEXT_INPUT_WIDTH) / 2 - (BUTTON_WIDTH - TEXT_INPUT_WIDTH) / 2, 25 + (this.font.FONT_HEIGHT / 2), 0xFFFFFF);
        this.drawString(this.font, "Bus Voltage", (this.width - TEXT_INPUT_WIDTH) / 2 - (BUTTON_WIDTH - TEXT_INPUT_WIDTH) / 2, 55 + (this.font.FONT_HEIGHT / 2), 0xFFFFFF);
        this.drawString(this.font, "Result Power", (this.width - TEXT_INPUT_WIDTH) / 2 - (BUTTON_WIDTH - TEXT_INPUT_WIDTH) / 2, 90 + (this.font.FONT_HEIGHT / 2), 0xFFFFFF);
        this.drawString(this.font, "Reactive Power", (this.width - TEXT_INPUT_WIDTH) / 2 - (BUTTON_WIDTH - TEXT_INPUT_WIDTH) / 2, 120 + (this.font.FONT_HEIGHT / 2), 0xFFFFFF);
        // Draw mw label
        this.drawString(this.font, "MW", (this.width / 2) + (TEXT_INPUT_WIDTH / 2) + 55, 25 + (this.font.FONT_HEIGHT / 2), 0xFFFFFF);
        this.drawString(this.font, "kV", (this.width / 2) + (TEXT_INPUT_WIDTH / 2) + 55, 55 + (this.font.FONT_HEIGHT / 2), 0xFFFFFF);
        this.drawString(this.font, "MW", (this.width / 2) + (TEXT_INPUT_WIDTH / 2) + 55, 90 + (this.font.FONT_HEIGHT / 2), 0xFFFFFF);
        this.drawString(this.font, "Mvar", (this.width / 2) + (TEXT_INPUT_WIDTH / 2) + 55, 120 + (this.font.FONT_HEIGHT / 2), 0xFFFFFF);
        // Draw separator
        this.drawCenteredString(this.font, "- - - - - - - - - - - - - - - - - - - -", this.width / 2, 75 + (this.font.FONT_HEIGHT / 2), 0xFFFFFF);
    }

    @Override
    protected void submitChanges() {
        boolean shouldUpdate = true;
        double maxPower = 0, busVoltage = 0;
        try {
            maxPower = Double.parseDouble(textFieldMaxPower.getText());
            busVoltage = Double.parseDouble(textFieldBusVoltage.getText());
        } catch (NumberFormatException e) {
            shouldUpdate = false;
            PlayerUtils.error(player, LogicalSide.CLIENT, "gui.electricblocks.err_invalid_number");
        }

        if (shouldUpdate) {
            PlayerUtils.sendMessage(player, LogicalSide.CLIENT, "command.electricblocks.viewmodify.submit");
            loadTileEntity.setInService(inService);
            loadTileEntity.setMaxPower(new MetricUnit(maxPower, MetricUnit.MetricPrefix.MEGA));
            loadTileEntity.setBusVoltage(new MetricUnit(busVoltage, MetricUnit.MetricPrefix.KILO));
            TileEntityMessageToServer teMSG = new TileEntityMessageToServer(loadTileEntity, player);
            ElectricBlocksPacketHandler.INSTANCE.sendToServer(teMSG);
        }
    }

}
