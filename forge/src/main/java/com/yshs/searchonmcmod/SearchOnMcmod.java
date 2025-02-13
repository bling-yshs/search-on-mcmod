package com.yshs.searchonmcmod;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
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
    @SneakyThrows
    public void onRenderTooltipEvent(ItemTooltipEvent event) {
        // 检查是否按下快捷键且还未触发过搜索
        if (!keyPressedFlag.get() || hasTriggeredSearch.get()) {
            return;
        }
        // 设置已触发标志，保证一次按键只触发一次
        hasTriggeredSearch.set(true);
        log.info("触发了MC百科搜索");

        // 1. 得到物品的描述ID
        String descriptionId = event.getItemStack().getItem().getDescriptionId();
        if (StringUtils.isBlank(descriptionId)) {
            return;
        }
        // 2. 转换为注册表名
        String registryName = MainUtil.convertDescriptionIdToRegistryName(descriptionId);
        // 3. 如果注册表名为空气，则不进行搜索
        if ("minecraft:air".equals(registryName)) {
            return;
        }
        // 4. 如果注册表明为空，但是物品的描述ID不为空，则进行搜索
        if (StringUtils.isBlank(registryName) && StringUtils.isNotBlank(descriptionId)) {
            MainUtil.openSearchPage(descriptionId);
            return;
        }

        // 得到物品的本地化名称
        String localizedName = event.getItemStack().getHoverName().getString();

        CompletableFuture.runAsync(() -> {
            // 5. 查找并得到物品在MCMOD中的ID
            Optional<String> optionalItemMCMODID;
            try {
                optionalItemMCMODID = MainUtil.fetchItemMCMODID(registryName);
            } catch (Exception e) {
                log.error("MC百科搜索: 无法通过百科 API 获取物品 MCMOD ID，请检查您的网络情况", e);
                // 发送提示消息
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.sendMessage(
                            new TranslatableComponent("text.searchonmcmod.mcmodid_not_found"),
                            Minecraft.getInstance().player.getUUID());
                }
                return;
            }
            if (!optionalItemMCMODID.isPresent()) {
                return;
            }
            String itemMCMODID = optionalItemMCMODID.get();

            // 6. 如果mcmodItemID为0，则进行搜索
            if ("0".equals(itemMCMODID)) {
                // 然后到https://search.mcmod.cn/s?key=%s去搜索
                MainUtil.openSearchPage(localizedName);
                return;
            }

            // 7. 判断物品页面是否存在，如果不存在则进行搜索
            if (!MainUtil.itemPageExist(itemMCMODID)) {
                // 然后到https://search.mcmod.cn/s?key=%s去搜索
                MainUtil.openSearchPage(localizedName);
                return;
            }

            // 8. 打开MCMOD的物品页面
            MainUtil.openItemPage(itemMCMODID);
        });
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
