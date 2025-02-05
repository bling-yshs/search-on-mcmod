package com.yshs.searchonmcmod;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CompletableFuture;

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
    private final AtomicBoolean allowOpenUrl = new AtomicBoolean(false);
    private final AtomicBoolean keyPressedFlag = new AtomicBoolean(false);

    /**
     * 构造函数
     */
    public SearchOnMcmod() {
        NeoForge.EVENT_BUS.register(this);
    }

    /**
     * 在物品tooltip渲染事件时触发
     *
     * @param event 物品tooltip事件，渲染物品信息时触发
     */
    @SubscribeEvent
    @SneakyThrows
    public void onRenderTooltipEvent(ItemTooltipEvent event) {
        if (!allowOpenUrl.getAndSet(false)) {
            return;
        }
        log.info("allowOpenUrl设置为false");
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
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    player.sendSystemMessage(Component.translatable("text.searchonmcmod.mcmodid_not_found"));
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
    public void onKeyPressed(ScreenEvent.KeyPressed.Post event) {
        int eventKeyCode = event.getKeyCode();
        InputConstants.Key settingsKey = SEARCH_ON_MCMOD_KEY.getKey();
        if (eventKeyCode != settingsKey.getValue()) {
            return;
        }
        if (keyPressedFlag.get()) {
            return;
        }
        keyPressedFlag.set(true);
        allowOpenUrl.set(true);
        log.info("SEARCH_ON_MCMOD_KEY按键已按下，keyPressedFlag，allowOpenUrl设置为true");
    }

    /**
     * 客户端MOD事件
     */
    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        /**
         * 注册按键绑定事件
         *
         * @param event 注册按键绑定事件，注册搜索按键绑定
         */
        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(SEARCH_ON_MCMOD_KEY);
        }

    }

}
