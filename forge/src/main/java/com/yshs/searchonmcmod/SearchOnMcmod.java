package com.yshs.searchonmcmod;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static com.yshs.searchonmcmod.KeyBindings.SEARCH_ON_MCMOD_KEY;

@Mod(SearchOnMcmod.MOD_ID)
@Slf4j
public class SearchOnMcmod {
    public static final String MOD_ID = "searchonmcmod";
    private static boolean keyDown = false;

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
        if (keyDown == false) {
            return;
        }
        keyDown = false;
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
        // 5. 查找并得到物品在MCMOD中的ID
        Optional<String> optionalItemMCMODID = MainUtil.fetchItemMCMODID(registryName);
        if (!optionalItemMCMODID.isPresent()) {
            return;
        }
        String itemMCMODID = optionalItemMCMODID.get();

        // 6. 如果mcmodItemID为0，则进行搜索
        if ("0".equals(itemMCMODID)) {
            // 得到物品的本地化名称
            String localizedName = event.getItemStack().getHoverName().getString();
            // 然后到https://search.mcmod.cn/s?key=%s去搜索
            MainUtil.openSearchPage(localizedName);
            return;
        }

        // 6. 打开MCMOD的物品页面
        MainUtil.openItemPage(itemMCMODID);

    }

    @SubscribeEvent
    public void onKeyPressed(ScreenEvent.KeyPressed.Post event) {
        int keyCode = event.getKeyCode();
        InputConstants.Key key = SEARCH_ON_MCMOD_KEY.getKey();
        if (keyCode == key.getValue() && keyDown == false) {
            keyDown = true;
            log.info("SEARCH_ON_MCMOD_KEY按键已按下，keyDown设置为true");
        }
    }

    @SubscribeEvent
    public void onKeyReleased(ScreenEvent.KeyReleased.Post event) {
        int keyCode = event.getKeyCode();
        if (keyCode == SEARCH_ON_MCMOD_KEY.getKey().getValue()) {
            keyDown = false;
            log.info("SEARCH_ON_MCMOD_KEY按键已释放，keyDown设置为false");
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void registerBindings(RegisterKeyMappingsEvent event) {
            event.register(SEARCH_ON_MCMOD_KEY);
        }

    }

}
