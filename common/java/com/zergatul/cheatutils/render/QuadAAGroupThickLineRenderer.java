package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.platform.Window;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.render.gl.EspGroupTrianglesAAProgram;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;

public class QuadAAGroupThickLineRenderer implements GroupThickLineRenderer {

    private static final float FEATHER = 1.25f;

    private EspGroupTrianglesAAProgram program;
    private RenderWorldLastEvent event;
    private Vec3 view;
    private float lineWidth;
    private int viewportWidth;
    private int viewportHeight;
    private final Vector4f v1 = new Vector4f();
    private final Vector4f v2 = new Vector4f();
    private final Vector4f rect1 = new Vector4f();
    private final Vector4f rect2 = new Vector4f();
    private final Vector4f rect3 = new Vector4f();
    private final Vector4f rect4 = new Vector4f();

    @Override
    public void begin(RenderWorldLastEvent event, float width) {
        if (this.event != null) {
            throw new IllegalStateException("Rendered is already active");
        }

        this.event = event;
        this.view = event.getCamera().getPosition();

        if (program == null) {
            program = new EspGroupTrianglesAAProgram();
        }

        program.buffer.clear();

        Window window = Minecraft.getInstance().getWindow();
        viewportWidth = window.getWidth();
        viewportHeight = window.getHeight();
        lineWidth = width;
    }

    @Override
    public void line(double x1, double y1, double z1, double x2, double y2, double z2) {
        v1.set((float) (x1 - view.x), (float) (y1 - view.y), (float) (z1 - view.z), 1);
        v2.set((float) (x2 - view.x), (float) (y2 - view.y), (float) (z2 - view.z), 1);
        v1.mul(event.getMvp());
        v2.mul(event.getMvp());

        if (v1.w < 0 && v2.w < 0) {
            return;
        }

        if (v1.w < 0 || v2.w < 0) {
            // clipping
            float t = (v1.w - 0.0001f) / (v1.w - v2.w);
            if (v1.w <= 0) {
                v1.lerp(v2, t);
            } else {
                v2.lerp(v1, 1 - t);
            }
        }

        if (createRect()) {
            point(rect1, 0);
            point(rect2, 1);
            point(rect4, 1);

            point(rect1, 0);
            point(rect4, 1);
            point(rect3, 0);
        }
    }

    @Override
    public void end(float r, float g, float b, float a) {
        // set line settings
        GL30.glEnable(GL30.GL_BLEND);
        GL30.glBlendFuncSeparate(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA, GL30.GL_ONE, GL30.GL_ZERO);
        GL30.glDisable(GL30.GL_DEPTH_TEST);
        GL30.glDisable(GL30.GL_CULL_FACE);

        // draw with shader program
        program.draw(r, g, b, a, lineWidth, FEATHER);

        // reset settings
        GL30.glDisable(GL30.GL_BLEND);
        GL30.glEnable(GL30.GL_DEPTH_TEST);
        GL30.glEnable(GL30.GL_CULL_FACE);

        // reset renderer state
        this.event = null;
        this.view = null;
    }

    @Override
    public void close() {
        program.delete();
    }

    private boolean createRect() {
        // Step 1: Convert clip-space coordinates to NDC (Normalized Device Coordinates)
        float v1x = v1.x / v1.w;
        float v1y = v1.y / v1.w;
        float v1z = v1.z / v1.w;
        float v2x = v2.x / v2.w;
        float v2y = v2.y / v2.w;
        float v2z = v2.z / v2.w;

        // Step 2: Convert NDC to screen coordinates
        float x1_screen = (v1x + 1.0f) * 0.5f * viewportWidth;
        float y1_screen = (v1y + 1.0f) * 0.5f * viewportHeight;

        float x2_screen = (v2x + 1.0f) * 0.5f * viewportWidth;
        float y2_screen = (v2y + 1.0f) * 0.5f * viewportHeight;

        // Step 3: Calculate the direction vector of the line in screen space
        float dx = x2_screen - x1_screen;
        float dy = y2_screen - y1_screen;

        // Calculate the length to normalize the perpendicular vector
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length == 0.0f) {
            return false;
        }

        // Step 4: Compute the normalized perpendicular vector
        float px = -dy / length;
        float py = dx / length;

        // Step 5: Scale the perpendicular vector by half the line width to get the offset
        float offsetX = px * ((lineWidth + FEATHER) / 2.0f);
        float offsetY = py * ((lineWidth + FEATHER) / 2.0f);

        // Step 6: Calculate the four corner points of the rectangle in screen space
        float x0a = x1_screen + offsetX;
        float y0a = y1_screen + offsetY;

        float x0b = x1_screen - offsetX;
        float y0b = y1_screen - offsetY;

        float x1a = x2_screen + offsetX;
        float y1a = y2_screen + offsetY;

        float x1b = x2_screen - offsetX;
        float y1b = y2_screen - offsetY;

        // Step 7: Convert the rectangle's corner points back to NDC coordinates
        float x0a_ndc = (x0a / (viewportWidth * 0.5f)) - 1.0f;
        float y0a_ndc = (y0a / (viewportHeight * 0.5f)) - 1.0f;

        float x0b_ndc = (x0b / (viewportWidth * 0.5f)) - 1.0f;
        float y0b_ndc = (y0b / (viewportHeight * 0.5f)) - 1.0f;

        float x1a_ndc = (x1a / (viewportWidth * 0.5f)) - 1.0f;
        float y1a_ndc = (y1a / (viewportHeight * 0.5f)) - 1.0f;

        float x1b_ndc = (x1b / (viewportWidth * 0.5f)) - 1.0f;
        float y1b_ndc = (y1b / (viewportHeight * 0.5f)) - 1.0f;

        rect1.set(x0a_ndc, y0a_ndc, v1z, 1);
        rect2.set(x0b_ndc, y0b_ndc, v1z, 1);
        rect3.set(x1a_ndc, y1a_ndc, v2z, 1);
        rect4.set(x1b_ndc, y1b_ndc, v2z, 1);

        return true;
    }

    private void point(Vector4f v, float value) {
        program.buffer.add(v.x);
        program.buffer.add(v.y);
        program.buffer.add(v.z);
        program.buffer.add(value);
    }
}