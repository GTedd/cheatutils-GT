package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.apache.http.MethodNotSupportedException;

public class BlockColorApi extends ApiBase {

    @Override
    public String getRoute() {
        return "block-color";
    }

    @Override
    public String get(String id) throws MethodNotSupportedException {
        Block block = ModApiWrapper.BLOCKS.getValue(new ResourceLocation(id));
        int color = Minecraft.getInstance().getBlockColors().getColor(block.defaultBlockState(), null, null, 0);
        return Integer.toString(color);
    }
}