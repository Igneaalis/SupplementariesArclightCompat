package net.mehvahdjukaar.supplementaries.client.tooltip;

import net.mehvahdjukaar.supplementaries.common.components.SelectableContainerContent;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class SelectableContainerTooltip implements ClientTooltipComponent {

    public SelectableContainerTooltip(SelectableContainerContent<?> content) {
    }

    @Override
    public int getHeight() {
        return this.gridSizeY() * 20 + 2 + 4;
    }

    @Override
    public int getWidth(Font font) {
        return 3 * 18 + 2;
    }

    @Override
    public void renderImage(Font font, int mouseX, int mouseY, GuiGraphics graphics) {
        int j = this.gridSizeY();
        int k = 0;
        for (int l = 0; l < j; ++l) {
        }
    }

    private void drawBorder(int x, int y, int slotWidth, int slotHeight, GuiGraphics graphics) {
        int i;
        this.blit(graphics, x, y, Texture.BORDER_CORNER_TOP);
        this.blit(graphics, x + slotWidth * 18 + 1, y, Texture.BORDER_CORNER_TOP);
        for (i = 0; i < slotWidth; ++i) {
            this.blit(graphics, x + 1 + i * 18, y, Texture.BORDER_HORIZONTAL_TOP);
            this.blit(graphics, x + 1 + i * 18, y + slotHeight * 20, Texture.BORDER_HORIZONTAL_BOTTOM);
        }
        for (i = 0; i < slotHeight; ++i) {
            this.blit(graphics, x, y + i * 20 + 1, Texture.BORDER_VERTICAL);
            this.blit(graphics, x + slotWidth * 18 + 1, y + i * 20 + 1, Texture.BORDER_VERTICAL);
        }
        this.blit(graphics, x, y + slotHeight * 20, Texture.BORDER_CORNER_BOTTOM);
        this.blit(graphics, x + slotWidth * 18 + 1, y + slotHeight * 20, Texture.BORDER_CORNER_BOTTOM);
    }

    private void blit(GuiGraphics guiGraphics, int x, int y, Texture texture) {
        guiGraphics.blit(ModTextures.QUIVER_TOOLTIP, x, y, 0, texture.x, texture.y, texture.w, texture.h, 128, 128);
    }


    private int gridSizeY() {
        return 1;
    }

    enum Texture {
        SLOT(0, 0, 18, 20),
        BLOCKED_SLOT(0, 40, 18, 20),
        BORDER_VERTICAL(0, 18, 1, 20),
        BORDER_HORIZONTAL_TOP(0, 20, 18, 1),
        BORDER_HORIZONTAL_BOTTOM(0, 60, 18, 1),
        BORDER_CORNER_TOP(0, 20, 1, 1),
        BORDER_CORNER_BOTTOM(0, 60, 1, 1);

        public final int x;
        public final int y;
        public final int w;
        public final int h;

        Texture(int j, int k, int l, int m) {
            this.x = j;
            this.y = k;
            this.w = l;
            this.h = m;
        }
    }
}

