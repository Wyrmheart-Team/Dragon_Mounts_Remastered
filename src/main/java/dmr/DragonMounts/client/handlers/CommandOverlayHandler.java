package dmr.DragonMounts.client.handlers;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import dmr.DragonMounts.DMR;
import dmr.DragonMounts.client.gui.CommandMenu.CommandMenuScreen;
import dmr.DragonMounts.common.handlers.DragonWhistleHandler;
import dmr.DragonMounts.config.ClientConfig;
import dmr.DragonMounts.network.packets.DragonCommandPacket;
import dmr.DragonMounts.network.packets.DragonCommandPacket.Command;
import java.awt.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Matrix4fStack;

/**
 * Based on code from the MineMenu mod,
 * <a href="https://github.com/GirafiStudios/MineMenu">Github</a>
 */
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = DMR.MOD_ID, value = Dist.CLIENT)
public class CommandOverlayHandler {

    public static Color selectedColor = new Color(163, 163, 163, 200);
    public static Color unselectedColor = new Color(98, 98, 98, 128);

    public static void tickTimer() {
        if (animationTimer > 0) {
            animationTimer -= 5;
        }
    }

    public static void resetTimer() {
        animationTimer = 20;
    }

    @FunctionalInterface
    public interface MenuItemClickListener {
        void onClick(LocalPlayer player);
    }

    public static double getMouseAngle() {
        Minecraft mc = Minecraft.getInstance();
        return getRelativeAngle(
                mc.getWindow().getScreenWidth() * 0.5D,
                mc.getWindow().getScreenHeight() * 0.5D,
                mc.mouseHandler.xpos(),
                mc.mouseHandler.ypos());
    }

    private static double getRelativeAngle(double originX, double originY, double x, double y) {
        double angle = -Math.toDegrees(Math.atan2(x - originX, y - originY));

        return correctAngle(angle);
    }

    public static double correctAngle(double angle) {
        if (angle < 0) {
            angle += 360;
        } else if (angle > 360) {
            angle -= 360;
        }
        return angle;
    }

    public record MenuItem(Component title, MenuItemClickListener clickListener) {}

    static final MenuItem[] activeArray = new MenuItem[] {
        new MenuItem(Component.translatable("dmr.command_mode.sit"), player -> {
            PacketDistributor.sendToServer(new DragonCommandPacket(Command.SIT));
        }),
        new MenuItem(Component.translatable("dmr.command_mode.follow"), player -> {
            PacketDistributor.sendToServer(new DragonCommandPacket(Command.FOLLOW));
        }),
        new MenuItem(Component.translatable("dmr.command_mode.wander"), player -> {
            PacketDistributor.sendToServer(new DragonCommandPacket(Command.WANDER));
        }),
        new MenuItem(Component.translatable("dmr.command_mode.whistle"), player -> {
            PacketDistributor.sendToServer(new DragonCommandPacket(Command.WHISTLE));
        }),
        new MenuItem(Component.translatable("dmr.command_mode.passive"), player -> {
            PacketDistributor.sendToServer(new DragonCommandPacket(Command.PASSIVE));
        }),
        new MenuItem(Component.translatable("dmr.command_mode.neutral"), player -> {
            PacketDistributor.sendToServer(new DragonCommandPacket(Command.NEUTRAL));
        }),
        new MenuItem(Component.translatable("dmr.command_mode.aggressive"), player -> {
            PacketDistributor.sendToServer(new DragonCommandPacket(Command.AGGRESSIVE));
        })
    };

    public static MenuItem[] getItems() {
        return activeArray;
    }

    public static final int MAX_ITEMS = activeArray.length;
    public static final double ANGLE_PER_ITEM = 360F / MAX_ITEMS;
    public static final float OUTER_RADIUS = 50;
    public static final float INNER_RADIUS = 10;
    public static int animationTimer = 20;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        tickTimer();

        Minecraft mc = Minecraft.getInstance();
        if ((mc.level == null || mc.isPaused()) && CommandMenuScreen.active) {
            CommandMenuScreen.deactivate();
        }
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiLayerEvent.Post event) {
        if (!(event.getName().equals(VanillaGuiLayers.TAB_LIST))) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && !mc.options.hideGui && !mc.isPaused() && CommandMenuScreen.active) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            renderButtonBackgrounds();
            renderItems(guiGraphics);
        }
    }

    public static void renderButtonBackgrounds() {
        Minecraft mc = Minecraft.getInstance();
        var player = mc.player;
        assert player != null;
        var whistleItem = DragonWhistleHandler.getDragonWhistleItem(player);

        if (whistleItem == null) {
            return;
        }

        // Adjust the mouse angle so it falls between segments.
        double mouseAngle = correctAngle(getMouseAngle() + (1.5f * ANGLE_PER_ITEM));

        // Compute mouse distance from center in GUI coordinates.
        // We convert the raw screen coordinates into GUI coordinates.
        double mouseDistance = getMouseDistance();

        doSectionRender((bufferBuilder -> {
            // Loop over each segment.
            for (int i = 0; i < MAX_ITEMS; i++) {
                double trueSegStart = ANGLE_PER_ITEM * i;
                double trueSegEnd = ANGLE_PER_ITEM * (i + 1);

                // Determine whether the mouse is in this segment.
                boolean mouseIn = (mouseAngle > trueSegStart && mouseAngle < trueSegEnd);

                // If the mouse pointer is within the inner radius, force mouseIn to false.
                if (mouseDistance < INNER_RADIUS || mouseDistance > (OUTER_RADIUS * 1.3f)) {
                    mouseIn = false;
                }

                // Adjust inner and outer radii based on animation and mouse hover.
                float innerRadius = (((INNER_RADIUS - animationTimer) / 100F) * 130F);
                float outerRadius = (((OUTER_RADIUS - animationTimer) / 100F) * 130F);

                // Choose the proper color.
                var color = mouseIn ? selectedColor : unselectedColor;
                var bsColor = new Color(whistleItem.getColor().getTextureDiffuseColor());
                bsColor = new Color(bsColor.getRed(), bsColor.getGreen(), bsColor.getBlue(), color.getAlpha());
                var bgColor = ClientConfig.COLORED_WHISTLE_MENU ? bsColor : color;

                if (mouseIn) {
                    bgColor = bgColor.darker();
                }

                int subdivisions = 2;
                for (int j = 0; j < subdivisions; j++) {
                    double t1 = (double) j / subdivisions;
                    double t2 = (double) (j + 1) / subdivisions;

                    renderSection(
                            bufferBuilder,
                            bgColor,
                            outerRadius,
                            innerRadius,
                            trueSegStart + 0.25,
                            trueSegEnd - 0.5,
                            t1,
                            t2);
                    renderSection(bufferBuilder, bgColor.darker(), innerRadius, 10, trueSegStart, trueSegEnd, t1, t2);
                }
            }
        }));
    }

    public static double getMouseDistance() {
        Minecraft mc = Minecraft.getInstance();
        float centerX = (float) (mc.getWindow().getGuiScaledWidth() * 0.5D);
        float centerY = (float) (mc.getWindow().getGuiScaledHeight() * 0.5D);
        double scaleX = (double) mc.getWindow().getGuiScaledWidth()
                / (double) mc.getWindow().getScreenWidth();
        double scaleY = (double) mc.getWindow().getGuiScaledHeight()
                / (double) mc.getWindow().getScreenHeight();
        double guiMouseX = mc.mouseHandler.xpos() * scaleX;
        double guiMouseY = mc.mouseHandler.ypos() * scaleY;
        double dx = guiMouseX - centerX;
        double dy = guiMouseY - centerY;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static void doSectionRender(SectionRender runnable) {
        Minecraft mc = Minecraft.getInstance();

        Matrix4fStack matrix = RenderSystem.getModelViewStack();
        matrix.pushMatrix();

        // Center the origin.
        float centerX = (float) (mc.getWindow().getGuiScaledWidth() * 0.5D);
        float centerY = (float) (mc.getWindow().getGuiScaledHeight() * 0.5D);
        matrix.translate(centerX, centerY, 0);
        matrix.rotate(Axis.ZP.rotationDegrees(12.8f));
        RenderSystem.applyModelViewMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        runnable.render(bufferBuilder);

        try (var data = bufferBuilder.build()) {
            // Now upload the built vertices.
            if (data != null) {
                BufferUploader.drawWithShader(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        matrix.popMatrix();
        RenderSystem.applyModelViewMatrix();
    }

    @FunctionalInterface
    public interface SectionRender {
        void render(BufferBuilder bufferBuilder);
    }

    private static void renderSection(
            BufferBuilder bufferBuilder,
            Color color,
            float outerRadius,
            float innerRadius,
            double trueSegStart,
            double trueSegEnd) {
        renderSection(bufferBuilder, color, outerRadius, innerRadius, trueSegStart, trueSegEnd, 1, 2);
    }

    private static void renderSection(
            BufferBuilder bufferBuilder,
            Color color,
            float outerRadius,
            float innerRadius,
            double trueSegStart,
            double trueSegEnd,
            double t1,
            double t2) {
        double angle1 = Math.toRadians(trueSegStart + t1 * (trueSegEnd - trueSegStart));
        double angle2 = Math.toRadians(trueSegStart + t2 * (trueSegEnd - trueSegStart));

        float r = color.getRed() / 255F;
        float g = color.getGreen() / 255F;
        float b = color.getBlue() / 255F;
        float a = color.getAlpha() / 255F;

        // Compute the positions for outer and inner points.
        float xOuter1 = (float) (Math.cos(angle1) * outerRadius);
        float yOuter1 = (float) (Math.sin(angle1) * outerRadius);

        float xOuter2 = (float) (Math.cos(angle2) * outerRadius);
        float yOuter2 = (float) (Math.sin(angle2) * outerRadius);

        float xInner2 = (float) (Math.cos(angle2) * innerRadius);
        float yInner2 = (float) (Math.sin(angle2) * innerRadius);

        float xInner1 = (float) (Math.cos(angle1) * innerRadius);
        float yInner1 = (float) (Math.sin(angle1) * innerRadius);

        bufferBuilder.addVertex(xOuter1, yOuter1, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex(xOuter2, yOuter2, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex(xInner2, yInner2, 0).setColor(r, g, b, a);
        bufferBuilder.addVertex(xInner1, yInner1, 0).setColor(r, g, b, a);
    }

    public static void renderItems(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Window window = mc.getWindow();
        Font fontRenderer = mc.font;
        PoseStack poseStack = guiGraphics.pose();

        // Center the whole UI.
        poseStack.pushPose();
        poseStack.translate(window.getGuiScaledWidth() * 0.5D, window.getGuiScaledHeight() * 0.5D, 0);

        for (int i = 0; i < MAX_ITEMS; i++) {
            MenuItem item = getItems()[i];

            float innerRadius = (((INNER_RADIUS - animationTimer) / 100F) * 130F);
            float outerRadius = (((OUTER_RADIUS - animationTimer) / 100F) * 130F);
            innerRadius = ((outerRadius - innerRadius) / 2) + 10;

            double trueSegStart = ANGLE_PER_ITEM * i;
            double trueSegEnd = ANGLE_PER_ITEM * (i + 1);

            double angle1 = Math.toRadians(trueSegStart + (trueSegEnd - trueSegStart));
            double angle2 = Math.toRadians(trueSegStart + 2 * (trueSegEnd - trueSegStart));

            float xInner1 = (float) (Math.cos(angle1) * innerRadius);
            float yInner1 = (float) (Math.sin(angle1) * innerRadius);
            float xInner2 = (float) (Math.cos(angle2) * innerRadius);
            float yInner2 = (float) (Math.sin(angle2) * innerRadius);

            float dx = xInner2 - xInner1;
            float dy = yInner2 - yInner1;
            float w = ((float) Math.sqrt(dx * dx + dy * dy)) * 0.85f;
            float h = (outerRadius - innerRadius) * 0.85f;

            int width = (int) Math.ceil(w);
            int height = (int) Math.ceil(h);

            double angle = (ANGLE_PER_ITEM * i) - 12.8d;
            double textDistance = (OUTER_RADIUS - 5) - animationTimer;
            double textX = textDistance * Math.cos(Math.toRadians(angle)) - (width * 0.5f);
            double textY = textDistance * Math.sin(Math.toRadians(angle)) - (ANGLE_PER_ITEM * 0.25f);

            var string = item.title();

            poseStack.pushPose();
            poseStack.translate(textX, textY, 0);
            poseStack.scale(0.5F, 0.5F, 1.0F);

            var lines = ComponentRenderUtils.wrapComponents(string, (int) (width * 2.2f), fontRenderer);

            float maxLines = (height * 2f) / fontRenderer.lineHeight;
            float leftover = maxLines - lines.size();
            int yOffset = Math.round((leftover / 2f) * fontRenderer.lineHeight);

            int bColor = mc.options.getBackgroundColor(0.25F);
            if (bColor != 0) {
                int fontWidth =
                        lines.stream().mapToInt(fontRenderer::width).max().orElse(0);
                int linesHeight = lines.size() * fontRenderer.lineHeight;

                int minX = width - 2 - (fontWidth / 2);
                int minY = yOffset - 2;
                int maxX = width + 2 + (fontWidth / 2);
                int maxY = yOffset + linesHeight + 2;
                guiGraphics.fill(minX, minY, maxX, maxY, bColor);
            }

            for (FormattedCharSequence line : lines) {
                guiGraphics.drawCenteredString(fontRenderer, line, width, yOffset, 0xFFFFFF);
                yOffset += fontRenderer.lineHeight;
            }
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}
