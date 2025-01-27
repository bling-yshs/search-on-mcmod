package com.yshs.searchonmcmod.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Mixin KeyMapping 类，用于获取具体的 key 值
 */
@Mixin(KeyMapping.class)
public interface KeyMappingMixin {
    /**
     * 获取私有属性 key 的值
     *
     * @return key 的值
     */
    @Accessor("key")
    InputConstants.Key getKey();
}
