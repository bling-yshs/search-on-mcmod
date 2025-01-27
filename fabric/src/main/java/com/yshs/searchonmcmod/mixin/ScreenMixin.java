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
        // 使用Mixin获取key
        InputConstants.Key key = ((KeyMappingMixin) SEARCH_ON_MCMOD_KEY).getKey();

        // 检查按键是否匹配
        if (key.getValue() == keyCode) {
            SearchOnMcmod.keyDown = true;
            log.info("SEARCH_ON_MCMOD_KEY按键已按下，keyDown设置为true");

            // Java 8 的延迟执行方式
            CompletableFuture.supplyAsync(() -> {
                try {
                    Thread.sleep(100);
                    SearchOnMcmod.keyDown = false;
                    log.info("SEARCH_ON_MCMOD_KEY按键自动释放，keyDown设置为false");
                } catch (InterruptedException e) {
                    log.error("Delayed key up interrupted", e);
                }
                return null;
            });
        }
    }
}
