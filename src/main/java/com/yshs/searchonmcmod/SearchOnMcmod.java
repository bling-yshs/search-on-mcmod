package com.yshs.searchonmcmod;

import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.yshs.searchonmcmod.KeyBindings.SEARCH_ON_MCMOD_KEY;

/**
 * 主类
 */
@Mod(SearchOnMcmod.MOD_ID)
public class SearchOnMcmod {
    /**
     * MOD ID
     */
    public static final String MOD_ID = "searchonmcmod";
    private final AtomicBoolean keyPressedFlag = new AtomicBoolean(false);
    private final AtomicBoolean hasTriggeredSearch = new AtomicBoolean(false);
    private static final Logger log = LogManager.getLogger();

    /**
     * 构造函数
     */
    public SearchOnMcmod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * 在物品tooltip渲染事件时触发
     *
     * @param event 物品tooltip事件，渲染物品信息时触发
     */
    @SubscribeEvent
    public void onRenderTooltipEvent(ItemTooltipEvent event) {
        // 检查是否按下快捷键且还未触发过搜索
        if (!keyPressedFlag.get() || hasTriggeredSearch.get()) {
            return;
        }
        // 设置已触发标志，保证一次按键只触发一次
        hasTriggeredSearch.set(true);
        log.info("触发了MC百科搜索");
        if (event.getItemStack().isEmpty()) {
            return;
        }
        // 得到物品的本地化名称
        String searchKeyword = event.getItemStack().getHoverName().getString();
        if (StringUtils.isBlank(searchKeyword)) {
            return;
        }
        try {
            MainUtil.openSearchPage(searchKeyword);
        } catch (Exception e) {
            handleSearchFailure("MC百科搜索: 打开搜索页面失败", e);
        }
    }

    private static void handleSearchFailure(String message, Exception e) {
        log.error(message, e);
        showSearchFailedHint();
    }

    private static void showSearchFailedHint() {
        net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
        minecraft.execute(() -> net.minecraft.client.gui.toasts.SystemToast.addOrUpdate(
                minecraft.getToasts(),
                net.minecraft.client.gui.toasts.SystemToast.Type.WORLD_ACCESS_FAILURE,
                new net.minecraft.util.text.TranslationTextComponent("text.searchonmcmod.search_failed"),
                null
        ));
    }

    /**
     * @param event 键盘按下事件
     */
    @SubscribeEvent
    public void onKeyPressed(GuiScreenEvent.KeyboardKeyPressedEvent.Post event) {
        int eventKeyCode = event.getKeyCode();
        InputMappings.Input settingsKey = SEARCH_ON_MCMOD_KEY.getKey();
        if (eventKeyCode != settingsKey.getValue()) {
            return;
        }
        if (keyPressedFlag.get()) {
            return;
        }
        keyPressedFlag.set(true);
        // 重置触发标志
        hasTriggeredSearch.set(false);
        log.info("SEARCH_ON_MCMOD_KEY按键已按下，keyPressedFlag设置为true");
    }

    /**
     * @param event 键盘释放事件
     */
    @SubscribeEvent
    public void onKeyReleased(GuiScreenEvent.KeyboardKeyReleasedEvent.Post event) {
        int eventKeyCode = event.getKeyCode();
        InputMappings.Input settingsKey = SEARCH_ON_MCMOD_KEY.getKey();
        if (eventKeyCode != settingsKey.getValue()) {
            return;
        }
        keyPressedFlag.set(false);
        // 重置触发标志
        hasTriggeredSearch.set(false);
        log.info("SEARCH_ON_MCMOD_KEY按键已释放，keyPressedFlag设置为false");
    }

    /**
     * 客户端事件
     */
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        /**
         * @param event 事件
         */
        @SubscribeEvent
        public static void setup(final FMLCommonSetupEvent event) {
            ClientRegistry.registerKeyBinding(SEARCH_ON_MCMOD_KEY);
        }

    }

}
