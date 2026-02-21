package com.yshs.searchonmcmod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SearchOnMcmodConfigScreen extends Screen {
    private static final int OPTION_WIDTH = 310;
    private static final int DONE_BUTTON_WIDTH = 200;

    private final Screen parent;
    private boolean showSearchingHint;
    private boolean showCopyToClipboardHint;

    public SearchOnMcmodConfigScreen(Screen parent) {
        super(Component.translatable("config.searchonmcmod.title"));
        this.parent = parent;
        this.showSearchingHint = SearchOnMcmodConfig.isSearchingHintEnabled();
        this.showCopyToClipboardHint = SearchOnMcmodConfig.isCopyToClipboardHintEnabled();
    }

    @Override
    protected void init() {
        int left = (this.width - OPTION_WIDTH) / 2;
        int top = this.height / 4;

        addRenderableWidget(CycleButton.onOffBuilder(this.showSearchingHint).create(
                left,
                top,
                OPTION_WIDTH,
                20,
                Component.translatable("config.searchonmcmod.show_searching_hint"),
                (button, value) -> {
                    this.showSearchingHint = value;
                    SearchOnMcmodConfig.updateClientOptions(this.showSearchingHint, this.showCopyToClipboardHint);
                }
        ));

        addRenderableWidget(CycleButton.onOffBuilder(this.showCopyToClipboardHint).create(
                left,
                top + 24,
                OPTION_WIDTH,
                20,
                Component.translatable("config.searchonmcmod.show_copy_to_clipboard_hint"),
                (button, value) -> {
                    this.showCopyToClipboardHint = value;
                    SearchOnMcmodConfig.updateClientOptions(this.showSearchingHint, this.showCopyToClipboardHint);
                }
        ));

        int actionsY = this.height - 27;
        int doneX = (this.width - DONE_BUTTON_WIDTH) / 2;
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(doneX, actionsY, DONE_BUTTON_WIDTH, 20)
                .build());
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
