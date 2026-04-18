package com.gtouming.void_dimension.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class KeyInputHandler {
    public static final KeyMapping OPEN_VOID_TERMINAL_KEY = new KeyMapping(
            "key.void_dimension.open_void_terminal",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_V,
            "category.void_dimension"
    );
}
