package com.yshs.searchonmcmod;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.extern.slf4j.Slf4j;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.yshs.searchonmcmod.KeyBindings.SEARCH_ON_MCMOD_KEY;

/**
 * 主类
 */
@Mod(SearchOnMcmod.MOD_ID)
@Slf4j
public class SearchOnMcmod {
    /**
     * MOD ID
     */
    public static final String MOD_ID = "searchonmcmod";
    private final AtomicBoolean keyPressedFlag = new AtomicBoolean(false);
    private final AtomicBoolean hasTriggeredSearch = new AtomicBoolean(false);

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

        // 得到物品的本地化名称
        String localizedName = event.getItemStack().getHoverName().getString();
        // 1. 得到物品的描述ID
        String descriptionId = event.getItemStack().getItem().getDescriptionId();
        // 2. 转换为注册表名
        String registryName = StringUtils.isBlank(descriptionId) ? "" : MainUtil.convertDescriptionIdToRegistryName(descriptionId);
        // 3. 如果注册表名为空气，则不进行搜索
        if ("minecraft:air".equals(registryName)) {
            return;
        }
        // 4. 优先使用本地化名称搜索，名称为空时使用描述 ID 兜底
        String searchKeyword = StringUtils.isNotBlank(localizedName) ? localizedName : descriptionId;
        if (StringUtils.isBlank(searchKeyword)) {
            return;
        }
        MainUtil.openSearchPage(searchKeyword);
    }

    /**
     * @param event 键盘按下事件
     */
    @SubscribeEvent
    public void onKeyPressed(ScreenEvent.KeyboardKeyPressedEvent.Post event) {
        int eventKeyCode = event.getKeyCode();
        InputConstants.Key settingsKey = SEARCH_ON_MCMOD_KEY.getKey();
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
    public void onKeyReleased(ScreenEvent.KeyboardKeyReleasedEvent.Post event) {
        int eventKeyCode = event.getKeyCode();
        InputConstants.Key settingsKey = SEARCH_ON_MCMOD_KEY.getKey();
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
