package com.yshs.searchonmcmod.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.yshs.searchonmcmod.SearchOnMcmod;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

import static com.yshs.searchonmcmod.KeyBindings.COPY_ITEM_NAME_KEY;
import static com.yshs.searchonmcmod.KeyBindings.SEARCH_ON_MCMOD_KEY;

/**
 * Mixin Screen 类，用于监听按键事件
 */
@Mixin(Screen.class)
@Slf4j
public class ScreenMixin {

    @Inject(at = @At("HEAD"), method = "keyPressed")
    @SneakyThrows
    private void on(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        handlePressedKey(SEARCH_ON_MCMOD_KEY, keyCode, true);
        handlePressedKey(COPY_ITEM_NAME_KEY, keyCode, false);
    }

    private static void handlePressedKey(net.minecraft.client.KeyMapping keyMapping, int keyCode, boolean searchKey) {
        InputConstants.Key key = ((KeyMappingMixin) keyMapping).getKey();
        if (key.getValue() != keyCode) {
            return;
        }
        if (searchKey) {
            SearchOnMcmod.searchKeyDown = true;
            log.info("SEARCH_ON_MCMOD_KEY按键已按下，searchKeyDown设置为true");
        } else {
            SearchOnMcmod.copyNameKeyDown = true;
            log.info("COPY_ITEM_NAME_KEY按键已按下，copyNameKeyDown设置为true");
        }
        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(100);
                if (searchKey) {
                    SearchOnMcmod.searchKeyDown = false;
                    log.info("SEARCH_ON_MCMOD_KEY按键自动释放，searchKeyDown设置为false");
                } else {
                    SearchOnMcmod.copyNameKeyDown = false;
                    log.info("COPY_ITEM_NAME_KEY按键自动释放，copyNameKeyDown设置为false");
                }
            } catch (InterruptedException e) {
                log.error("Delayed key up interrupted", e);
            }
            return null;
        });
    }
}
