package com.breakinblocks.nautec.client.screen;

import com.breakinblocks.nautec.api.client.screen.FluidTankRenderer;
import com.breakinblocks.nautec.capabilities.bacteria.DishPort;
import com.breakinblocks.nautec.content.conduits.ConduitChannel;
import com.breakinblocks.nautec.content.conduits.ConduitTapBlock;
import com.breakinblocks.nautec.content.conduits.ConduitTapBlockEntity;
import com.breakinblocks.nautec.content.conduits.FlowMode;
import com.breakinblocks.nautec.content.conduits.TapArm;
import com.breakinblocks.nautec.content.conduits.TapFace;
import com.breakinblocks.nautec.content.conduits.TapFilter;
import com.breakinblocks.nautec.content.conduits.TapSide;
import com.breakinblocks.nautec.content.menus.ConduitTapMenu;
import com.breakinblocks.nautec.network.ConduitTapEditPayload;
import com.breakinblocks.nautec.registries.NTItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class ConduitTapScreen extends AbstractContainerScreen<ConduitTapMenu> {
    private enum Page {
        SETUP,
        INPUT,
        OUTPUT;

        String key() {
            return "nautec.conduit.tab." + name().toLowerCase(Locale.ROOT);
        }
    }

    private static final Direction[] FACE_ORDER = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
    private static final String[] FACE_LETTERS = {"D", "U", "N", "S", "W", "E"};
    private static final int FACE_X = 8;
    private static final int FACE_Y = 20;
    private static final int FACE_PITCH = 18;
    private static final int TAB_X = 8;
    private static final int TAB_Y = 58;
    private static final int TAB_WIDTH = 56;
    private static final int TAB_PITCH = 13;
    private static final int RIGHT_X = 66;
    private static final int RIGHT_WIDTH = 102;
    private static final int ROW_Y = 22;
    private static final int ROW_PITCH = 13;
    private static final int ROW_HEIGHT = 12;
    private static final int READOUT_Y = 76;
    private static final int READOUT_HEIGHT = 36;
    private static final int ITEM_GRID_X = 8;
    private static final int ITEM_GRID_Y = 108;
    private static final int FLUID_GRID_X = 67;
    private static final int FLUID_GRID_Y = 64;
    private static final int FLUID_COLUMNS = 5;
    private static final int NEIGHBOUR_REFRESH_TICKS = 10;
    private static final int SELECTED = 0xFF2F5C8C;
    private static final int SELECTED_HOVER = 0xFF3C73A8;
    private static final int FACE_DISABLED = 0xFF8C2F2F;
    private static final int BOTH_COLOR = 0xFF2E6F78;
    private static final int BOTH_HOVER = 0xFF3A8994;
    private static final int INPUT_MARK = 0xFF5FE8B0;
    private static final int OUTPUT_MARK = 0xFF4CCBE0;
    private static final int EXACT_MARK = 0xFFFFD86B;
    private static final int LOCKED_FILL = 0xFF2A302D;
    private static final int LOCKED_TEXT = 0xFFE7E7D6;
    private static final int LEGEND_Y = 118;
    private static final ItemStack UPGRADE_GHOST = new ItemStack(NTItems.EDDY_UPGRADE.get());
    private static final ItemStack FILTER_GHOST = new ItemStack(NTItems.FILTER.get());

    private static Direction selected = Direction.NORTH;
    private static Page page = Page.SETUP;

    private final FluidTankRenderer fluidRenderer = new FluidTankRenderer(1, false, 16, 16);
    private final List<AbstractWidget> setupWidgets = new ArrayList<>();
    private final List<AbstractWidget> inputWidgets = new ArrayList<>();
    private final List<AbstractWidget> outputWidgets = new ArrayList<>();
    private final ItemStack[] icons = new ItemStack[FACE_ORDER.length];
    private final TapArm[] arms = new TapArm[FACE_ORDER.length];
    private final Component[] neighbourNames = new Component[FACE_ORDER.length];
    private final List<Component> readout = new ArrayList<>(3);
    private int readoutTier = -1;
    private int neighbourTicks;

    public ConduitTapScreen(ConduitTapMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 256);
        this.titleLabelY = 6;
        this.inventoryLabelY = ConduitTapMenu.INVENTORY_Y - 11;
    }

    private ConduitTapBlockEntity tap() {
        return this.menu.blockEntity;
    }

    private TapFace face() {
        return tap().face(selected);
    }

    private TapSide side() {
        return page == Page.OUTPUT ? TapSide.OUTPUT : TapSide.INPUT;
    }

    private TapFilter filter() {
        return face().filter(side());
    }

    private void send(TapSide side, int action, int index, int value) {
        ClientPacketDistributor.sendToServer(ConduitTapEditPayload.sided(this.menu.containerId, selected, side, action, index, value));
    }

    private static int modeColor(FlowMode mode) {
        return switch (mode) {
            case OFF -> PanelStyle.NEUTRAL;
            case INSERT -> PanelStyle.RECEIVE_COLOR;
            case EXTRACT -> PanelStyle.SEND_COLOR;
            case BOTH -> BOTH_COLOR;
        };
    }

    private static int modeHover(FlowMode mode) {
        return switch (mode) {
            case OFF -> PanelStyle.NEUTRAL_HOVER;
            case INSERT -> PanelStyle.RECEIVE_HOVER;
            case EXTRACT -> PanelStyle.SEND_HOVER;
            case BOTH -> BOTH_HOVER;
        };
    }

    private int rowY(int row) {
        return this.topPos + ROW_Y + row * ROW_PITCH;
    }

    private void button(@Nullable List<AbstractWidget> group, int x, int y, int width, Supplier<Component> label, Supplier<Integer> color,
                        Supplier<Integer> hover, Supplier<Component> tooltip, Runnable action) {
        NTPanelButton button = addRenderableWidget(new NTPanelButton(this.font, x, y, width, ROW_HEIGHT, label, color, hover, tooltip, action));
        if (group != null) {
            group.add(button);
        }
    }

    @Override
    protected void init() {
        super.init();
        setupWidgets.clear();
        inputWidgets.clear();
        outputWidgets.clear();
        for (Page tab : Page.values()) {
            button(null, this.leftPos + TAB_X, this.topPos + TAB_Y + tab.ordinal() * TAB_PITCH, TAB_WIDTH, () -> Component.translatable(tab.key()),
                    () -> page == tab ? SELECTED : PanelStyle.NEUTRAL, () -> page == tab ? SELECTED_HOVER : PanelStyle.NEUTRAL_HOVER,
                    () -> Component.translatable(tab.key() + ".desc"), () -> setPage(tab));
        }
        int x = this.leftPos + RIGHT_X;
        for (ConduitChannel channel : ConduitChannel.ALL) {
            button(setupWidgets, x, rowY(channel.ordinal()), RIGHT_WIDTH,
                    () -> Component.translatable("nautec.conduit.button.mode", Component.translatable(channel.translationKey()),
                            Component.translatable(face().mode(channel).translationKey())),
                    () -> modeColor(face().mode(channel)),
                    () -> modeHover(face().mode(channel)),
                    () -> Component.translatable(face().mode(channel).translationKey() + ".desc"),
                    () -> {
                        FlowMode next = face().mode(channel).next();
                        face().setMode(channel, next);
                        send(TapSide.INPUT, ConduitTapEditPayload.MODE, channel.ordinal(), next.ordinal());
                    });
        }
        button(setupWidgets, x, rowY(3), RIGHT_WIDTH,
                () -> Component.translatable(face().disabled() ? "nautec.conduit.connection.off" : "nautec.conduit.connection.on"),
                () -> face().disabled() ? FACE_DISABLED : PanelStyle.SEND_COLOR,
                () -> face().disabled() ? PanelStyle.DANGER_HOVER : PanelStyle.SEND_HOVER,
                () -> Component.translatable("nautec.conduit.connection.desc"),
                () -> {
                    face().setDisabled(!face().disabled());
                    send(TapSide.INPUT, ConduitTapEditPayload.DISABLED, 0, face().disabled() ? 1 : 0);
                });
        for (TapSide tapSide : TapSide.ALL) {
            List<AbstractWidget> group = tapSide == TapSide.INPUT ? inputWidgets : outputWidgets;
            String sideKey = tapSide.name().toLowerCase(Locale.ROOT);
            button(group, x, rowY(0), RIGHT_WIDTH,
                    () -> Component.translatable(face().redstone(tapSide).translationKey()),
                    () -> PanelStyle.NEUTRAL, () -> PanelStyle.NEUTRAL_HOVER,
                    () -> Component.translatable(face().redstone(tapSide).translationKey() + "." + sideKey),
                    () -> {
                        face().setRedstone(tapSide, face().redstone(tapSide).next());
                        send(tapSide, ConduitTapEditPayload.REDSTONE, 0, face().redstone(tapSide).ordinal());
                    });
            button(group, x, rowY(2), RIGHT_WIDTH,
                    () -> Component.translatable(face().filter(tapSide).whitelist() ? "nautec.conduit.filter.whitelist" : "nautec.conduit.filter.blacklist"),
                    () -> !tap().hasFilterUpgrade() ? PanelStyle.SLOT_EDGE : face().filter(tapSide).whitelist() ? PanelStyle.SEND_COLOR : PanelStyle.DANGER,
                    () -> !tap().hasFilterUpgrade() ? PanelStyle.SLOT_EDGE : face().filter(tapSide).whitelist() ? PanelStyle.SEND_HOVER : PanelStyle.DANGER_HOVER,
                    () -> Component.translatable(!tap().hasFilterUpgrade() ? "nautec.conduit.filter.locked"
                            : face().filter(tapSide).whitelist() ? "nautec.conduit.filter.whitelist." + sideKey : "nautec.conduit.filter.blacklist." + sideKey),
                    () -> {
                        if (tap().hasFilterUpgrade()) {
                            TapFilter filter = face().filter(tapSide);
                            filter.setWhitelist(!filter.whitelist());
                            send(tapSide, ConduitTapEditPayload.WHITELIST, 0, filter.whitelist() ? 1 : 0);
                        }
                    });
        }
        button(inputWidgets, x, rowY(1), RIGHT_WIDTH,
                () -> Component.translatable(face().distribution().translationKey()),
                () -> PanelStyle.NEUTRAL, () -> PanelStyle.NEUTRAL_HOVER,
                () -> Component.translatable(face().distribution().translationKey() + ".desc"),
                () -> {
                    face().setDistribution(face().distribution().next());
                    send(TapSide.INPUT, ConduitTapEditPayload.DISTRIBUTION, 0, face().distribution().ordinal());
                });
        button(outputWidgets, x, rowY(1), 14, () -> Component.literal("-"),
                () -> PanelStyle.NEUTRAL, () -> PanelStyle.NEUTRAL_HOVER, () -> Component.translatable("nautec.conduit.priority.down"),
                () -> changePriority(-1));
        button(outputWidgets, x + RIGHT_WIDTH - 14, rowY(1), 14, () -> Component.literal("+"),
                () -> PanelStyle.NEUTRAL, () -> PanelStyle.NEUTRAL_HOVER, () -> Component.translatable("nautec.conduit.priority.up"),
                () -> changePriority(1));
        setPage(page);
        refreshNeighbours();
        selectConnectedFace();
    }

    private void selectConnectedFace() {
        int current = faceIndex(selected);
        if (arms[current] == TapArm.MACHINE) {
            return;
        }
        for (int i = 0; i < FACE_ORDER.length; i++) {
            if (arms[i] == TapArm.MACHINE) {
                selected = FACE_ORDER[i];
                return;
            }
        }
    }

    private static int faceIndex(Direction direction) {
        for (int i = 0; i < FACE_ORDER.length; i++) {
            if (FACE_ORDER[i] == direction) {
                return i;
            }
        }
        return 0;
    }

    private void setPage(Page next) {
        page = next;
        for (AbstractWidget widget : setupWidgets) {
            widget.visible = next == Page.SETUP;
        }
        for (AbstractWidget widget : inputWidgets) {
            widget.visible = next == Page.INPUT;
        }
        for (AbstractWidget widget : outputWidgets) {
            widget.visible = next == Page.OUTPUT;
        }
    }

    private void refreshNeighbours() {
        BlockState own = tap().getBlockState();
        for (int i = 0; i < FACE_ORDER.length; i++) {
            Direction direction = FACE_ORDER[i];
            arms[i] = own.getBlock() instanceof ConduitTapBlock ? own.getValue(ConduitTapBlock.ARMS[direction.ordinal()]) : TapArm.NONE;
            BlockState neighbour = this.minecraft.level.getBlockState(tap().getBlockPos().relative(direction));
            icons[i] = arms[i] == TapArm.NONE ? ItemStack.EMPTY : new ItemStack(neighbour.getBlock().asItem());
            neighbourNames[i] = neighbour.getBlock().getName();
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (++neighbourTicks >= NEIGHBOUR_REFRESH_TICKS) {
            neighbourTicks = 0;
            refreshNeighbours();
        }
    }

    private void changePriority(int direction) {
        int step = this.minecraft.hasShiftDown() ? 10 : 1;
        face().setPriority(face().priority() + direction * step);
        send(TapSide.OUTPUT, ConduitTapEditPayload.PRIORITY, 0, face().priority());
    }

    public boolean filtersOpen() {
        return page != Page.SETUP && tap().hasFilterUpgrade();
    }

    public int itemFilterSlots() {
        return tap().itemFilterSlots();
    }

    public Rect2i itemSlotArea(int slot) {
        return new Rect2i(this.leftPos + ITEM_GRID_X + (slot % 9) * 18, this.topPos + ITEM_GRID_Y + (slot / 9) * 18, 16, 16);
    }

    public Rect2i fluidSlotArea(int slot) {
        return new Rect2i(this.leftPos + FLUID_GRID_X + (slot % FLUID_COLUMNS) * 18, this.topPos + FLUID_GRID_Y + (slot / FLUID_COLUMNS) * 18, 16, 16);
    }

    public void setItemFilter(int slot, ItemStack stack) {
        if (page == Page.SETUP || slot < 0 || slot >= itemFilterSlots()) {
            return;
        }
        ItemStack template = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        filter().setItem(slot, template);
        ClientPacketDistributor.sendToServer(ConduitTapEditPayload.item(this.menu.containerId, selected, side(), slot, template));
    }

    public void setFluidFilter(int slot, FluidStack stack) {
        if (page == Page.SETUP || slot < 0 || slot >= TapFilter.FLUID_SLOTS || !tap().hasFilterUpgrade()) {
            return;
        }
        filter().setFluid(slot, stack);
        ClientPacketDistributor.sendToServer(ConduitTapEditPayload.fluid(this.menu.containerId, selected, side(), slot, stack));
    }

    private static String matchKey(TapFilter filter, int slot) {
        if (!filter.exact(slot)) {
            return "nautec.conduit.filter.loose";
        }
        if (!filter.dish(slot)) {
            return "nautec.conduit.filter.exact";
        }
        return DishPort.colonyOf(filter.item(slot)).isEmpty() ? "nautec.conduit.filter.dish_empty" : "nautec.conduit.filter.dish_strain";
    }

    private void toggleExact(int slot) {
        TapFilter filter = filter();
        if (filter.item(slot).isEmpty()) {
            return;
        }
        boolean exact = !filter.exact(slot);
        filter.setExact(slot, exact);
        send(side(), ConduitTapEditPayload.COMPONENTS, slot, exact ? 1 : 0);
    }

    private int faceX(int index) {
        return this.leftPos + FACE_X + (index % 3) * FACE_PITCH + 1;
    }

    private int faceY(int index) {
        return this.topPos + FACE_Y + (index / 3) * FACE_PITCH + 1;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        PanelStyle.panel(graphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        for (int i = 0; i < FACE_ORDER.length; i++) {
            Direction direction = FACE_ORDER[i];
            int fill = tap().face(direction).disabled() ? FACE_DISABLED : direction == selected ? SELECTED : PanelStyle.SLOT_FILL;
            PanelStyle.slot(graphics, faceX(i), faceY(i), fill);
        }
        for (Slot slot : this.menu.slots) {
            PanelStyle.slot(graphics, this.leftPos + slot.x, this.topPos + slot.y);
        }
        if (page == Page.SETUP) {
            PanelStyle.screen(graphics, this.leftPos + RIGHT_X, this.topPos + READOUT_Y, RIGHT_WIDTH, READOUT_HEIGHT);
            return;
        }
        int open = itemFilterSlots();
        for (int slot = 0; slot < TapFilter.ITEM_SLOTS; slot++) {
            Rect2i area = itemSlotArea(slot);
            PanelStyle.slot(graphics, area.getX(), area.getY(), slot < open ? PanelStyle.SLOT_FILL : LOCKED_FILL);
        }
        boolean unlocked = tap().hasFilterUpgrade();
        for (int slot = 0; slot < TapFilter.FLUID_SLOTS; slot++) {
            Rect2i area = fluidSlotArea(slot);
            PanelStyle.slot(graphics, area.getX(), area.getY(), unlocked ? PanelStyle.SLOT_FILL : LOCKED_FILL);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        List<Component> tooltip = new ArrayList<>();
        renderFaces(graphics, mouseX, mouseY, tooltip);
        if (page == Page.SETUP) {
            renderSetup(graphics);
        } else {
            renderSide(graphics, mouseX, mouseY, tooltip);
        }
        for (Slot slot : this.menu.slots) {
            if (slot.index < ConduitTapBlockEntity.SLOTS && !slot.hasItem()) {
                int sx = this.leftPos + slot.x;
                int sy = this.topPos + slot.y;
                graphics.item(slot.index == ConduitTapBlockEntity.UPGRADE_SLOT ? UPGRADE_GHOST : FILTER_GHOST, sx, sy);
                graphics.nextStratum();
                graphics.fill(sx, sy, sx + 16, sy + 16, PanelStyle.GHOST_FADE);
            }
            if (slot.index < ConduitTapBlockEntity.SLOTS && !slot.hasItem() && PanelStyle.inside(mouseX, mouseY, this.leftPos + slot.x, this.topPos + slot.y, 16, 16)) {
                tooltip.add(Component.translatable(slot.index == ConduitTapBlockEntity.UPGRADE_SLOT ? "nautec.conduit.slot.upgrade" : "nautec.conduit.slot.filter"));
            }
        }
        if (!tooltip.isEmpty()) {
            graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY);
        }
    }

    private void renderFaces(GuiGraphicsExtractor graphics, int mouseX, int mouseY, List<Component> tooltip) {
        for (int i = 0; i < FACE_ORDER.length; i++) {
            Direction direction = FACE_ORDER[i];
            TapFace face = tap().face(direction);
            int x = faceX(i);
            int y = faceY(i);
            if (!icons[i].isEmpty()) {
                graphics.item(icons[i], x, y);
            }
            graphics.nextStratum();
            graphics.text(this.font, FACE_LETTERS[i], x + 1, y + 1, 0xFFFFFFFF, true);
            boolean machine = arms[i] == TapArm.MACHINE && !face.disabled();
            boolean inputs = machine && face.anyMode(TapSide.INPUT);
            boolean outputs = machine && face.anyMode(TapSide.OUTPUT);
            if (inputs) {
                graphics.fill(x + 13, y, x + 16, y + 3, INPUT_MARK);
            }
            if (outputs) {
                graphics.fill(x + 13, y + 13, x + 16, y + 16, OUTPUT_MARK);
            }
            if (PanelStyle.inside(mouseX, mouseY, x, y, 16, 16)) {
                tooltip.add(Component.translatable("nautec.conduit.face." + direction.getSerializedName()));
                tooltip.add(arms[i] == TapArm.MACHINE ? neighbourNames[i].copy().withStyle(ChatFormatting.AQUA)
                        : Component.translatable("nautec.conduit.arm." + arms[i].getSerializedName()).withStyle(ChatFormatting.GRAY));
                if (face.disabled()) {
                    tooltip.add(Component.translatable("nautec.conduit.face.off").withStyle(ChatFormatting.RED));
                }
                if (inputs) {
                    tooltip.add(Component.translatable("nautec.conduit.face.inputs").withStyle(ChatFormatting.GREEN));
                }
                if (outputs) {
                    tooltip.add(Component.translatable("nautec.conduit.face.outputs").withStyle(ChatFormatting.AQUA));
                }
                tooltip.add(Component.translatable("nautec.conduit.face.hint").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    private void renderSetup(GuiGraphicsExtractor graphics) {
        if (readoutTier != tap().tier()) {
            readoutTier = tap().tier();
            readout.clear();
            tap().rates().describe(readout::add);
        }
        int readoutX = this.leftPos + RIGHT_X + 3;
        int readoutY = this.topPos + READOUT_Y + 3;
        for (int line = 0; line < readout.size(); line++) {
            graphics.text(this.font, readout.get(line), readoutX, readoutY + line * 11, PanelStyle.READOUT, false);
        }
    }

    private void renderSide(GuiGraphicsExtractor graphics, int mouseX, int mouseY, List<Component> tooltip) {
        TapFilter filter = filter();
        if (page == Page.OUTPUT) {
            Component priority = Component.translatable("nautec.conduit.priority", face().priority());
            graphics.text(this.font, priority, this.leftPos + RIGHT_X + (RIGHT_WIDTH - this.font.width(priority)) / 2, rowY(1) + 2,
                    PanelStyle.LABEL, false);
        }
        int open = itemFilterSlots();
        boolean unlocked = tap().hasFilterUpgrade();
        if (open < TapFilter.ITEM_SLOTS) {
            int firstRow = open / 9;
            int top = this.topPos + ITEM_GRID_Y + firstRow * 18;
            int bottom = this.topPos + ITEM_GRID_Y + 3 * 18 - 2;
            Component note = Component.translatable(unlocked ? "nautec.conduit.filter.needs_intricate" : "nautec.conduit.filter.needs_filter");
            int noteX = this.leftPos + ITEM_GRID_X + (9 * 18 - this.font.width(note)) / 2;
            graphics.text(this.font, note, noteX, top + (bottom - top - 8) / 2, LOCKED_TEXT, true);
        }
        for (int slot = 0; slot < TapFilter.ITEM_SLOTS; slot++) {
            Rect2i area = itemSlotArea(slot);
            ItemStack template = filter.item(slot);
            if (!template.isEmpty()) {
                graphics.item(template, area.getX(), area.getY());
                if (filter.exact(slot)) {
                    graphics.nextStratum();
                    graphics.fill(area.getX() + 12, area.getY(), area.getX() + 16, area.getY() + 4, EXACT_MARK);
                }
            }
            if (PanelStyle.inside(mouseX, mouseY, area.getX(), area.getY(), 16, 16)) {
                if (slot >= open) {
                    tooltip.add(Component.translatable(unlocked ? "nautec.conduit.filter.locked.intricate" : "nautec.conduit.filter.locked").withStyle(ChatFormatting.GRAY));
                } else if (template.isEmpty()) {
                    tooltip.add(Component.translatable("nautec.conduit.filter.item.how").withStyle(ChatFormatting.GRAY));
                } else {
                    tooltip.add(template.getHoverName());
                    tooltip.add(Component.translatable(matchKey(filter, slot)).withStyle(filter.exact(slot) ? ChatFormatting.GOLD : ChatFormatting.AQUA));
                    tooltip.add(Component.translatable("nautec.conduit.filter.exact.toggle").withStyle(ChatFormatting.DARK_GRAY));
                    tooltip.add(Component.translatable("nautec.conduit.filter.clear").withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
        for (int slot = 0; slot < TapFilter.FLUID_SLOTS; slot++) {
            Rect2i area = fluidSlotArea(slot);
            FluidStack fluid = filter.fluid(slot);
            if (!fluid.isEmpty()) {
                fluidRenderer.render(graphics, area.getX(), area.getY(), fluid.copyWithAmount(1));
            }
            if (PanelStyle.inside(mouseX, mouseY, area.getX(), area.getY(), 16, 16)) {
                if (!unlocked) {
                    tooltip.add(Component.translatable("nautec.conduit.filter.locked").withStyle(ChatFormatting.GRAY));
                } else if (fluid.isEmpty()) {
                    tooltip.add(Component.translatable("nautec.conduit.filter.fluid.how").withStyle(ChatFormatting.GRAY));
                } else {
                    tooltip.add(fluid.getHoverName());
                    tooltip.add(Component.translatable("nautec.conduit.filter.clear").withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, PanelStyle.LABEL, false);
        if (page != Page.SETUP) {
            Component label = Component.translatable(page == Page.INPUT ? "nautec.conduit.filter.input_items" : "nautec.conduit.filter.output_items");
            graphics.text(this.font, label, ITEM_GRID_X, ITEM_GRID_Y - 10, PanelStyle.LABEL, false);
        } else {
            graphics.fill(ITEM_GRID_X, LEGEND_Y + 1, ITEM_GRID_X + 6, LEGEND_Y + 7, INPUT_MARK);
            graphics.text(this.font, Component.translatable("nautec.conduit.legend.input"), ITEM_GRID_X + 10, LEGEND_Y, PanelStyle.LABEL, false);
            graphics.fill(ITEM_GRID_X, LEGEND_Y + 13, ITEM_GRID_X + 6, LEGEND_Y + 19, OUTPUT_MARK);
            graphics.text(this.font, Component.translatable("nautec.conduit.legend.output"), ITEM_GRID_X + 10, LEGEND_Y + 12, PanelStyle.LABEL, false);
            graphics.text(this.font, Component.translatable("nautec.conduit.legend.hint"), ITEM_GRID_X, LEGEND_Y + 26, PanelStyle.LABEL, false);
        }
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, PanelStyle.LABEL, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        boolean right = event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
        for (int i = 0; i < FACE_ORDER.length; i++) {
            if (PanelStyle.inside(mouseX, mouseY, faceX(i), faceY(i), 16, 16)) {
                Direction direction = FACE_ORDER[i];
                if (right) {
                    TapFace face = tap().face(direction);
                    face.setDisabled(!face.disabled());
                    ClientPacketDistributor.sendToServer(ConduitTapEditPayload.value(this.menu.containerId, direction, ConduitTapEditPayload.DISABLED, 0,
                            face.disabled() ? 1 : 0));
                } else {
                    selected = direction;
                }
                return true;
            }
        }
        if (page != Page.SETUP) {
            ItemStack carried = this.menu.getCarried();
            int open = itemFilterSlots();
            for (int slot = 0; slot < TapFilter.ITEM_SLOTS; slot++) {
                Rect2i area = itemSlotArea(slot);
                if (PanelStyle.inside(mouseX, mouseY, area.getX(), area.getY(), 16, 16)) {
                    if (slot < open) {
                        if (right) {
                            setItemFilter(slot, ItemStack.EMPTY);
                        } else if (this.minecraft.hasShiftDown() || carried.isEmpty()) {
                            toggleExact(slot);
                        } else {
                            setItemFilter(slot, carried);
                        }
                    }
                    return true;
                }
            }
            for (int slot = 0; slot < TapFilter.FLUID_SLOTS; slot++) {
                Rect2i area = fluidSlotArea(slot);
                if (PanelStyle.inside(mouseX, mouseY, area.getX(), area.getY(), 16, 16)) {
                    if (right) {
                        setFluidFilter(slot, FluidStack.EMPTY);
                    } else {
                        FluidStack contained = DistributorScreen.contained(carried);
                        if (!contained.isEmpty()) {
                            setFluidFilter(slot, contained);
                        }
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
