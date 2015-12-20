package me;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonLanguage;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.demo.DemoWorldServer;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;

public class GuiMeMainMenu extends GuiScreen implements GuiYesNoCallback
{
    private static final AtomicInteger field_175373_f = new AtomicInteger(0);
    private static final Logger logger = LogManager.getLogger();

    private GuiButton buttonResetDemo;

    private DynamicTexture viewportTexture;
    private boolean field_175375_v = true;

    private final Object threadLock = new Object();

    private String openGLWarning1;

    private String openGLWarning2;

    private String openGLWarningLink;

    public static final String field_96138_a = "Please click " + EnumChatFormatting.UNDERLINE + "here" + EnumChatFormatting.RESET + " for more information.";
    private int field_92024_r;
    private int field_92023_s;
    private int field_92022_t;
    private int field_92021_u;
    private int field_92020_v;
    private int field_92019_w;

    private GuiButton realmsButton;

    public GuiMeMainMenu()
    {
        this.openGLWarning2 = field_96138_a;
        BufferedReader bufferedreader = null;

        this.openGLWarning1 = "";

        if (!GLContext.getCapabilities().OpenGL20 && !OpenGlHelper.areShadersSupported())
        {
            this.openGLWarning1 = I18n.format("title.oldgl1", new Object[0]);
            this.openGLWarning2 = I18n.format("title.oldgl2", new Object[0]);
            this.openGLWarningLink = "https://help.mojang.com/customer/portal/articles/325948?ref=game";
        }
    }

    public boolean doesGuiPauseGame()
    {
        return false;
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
    }

    public void initGui()
    {
        this.viewportTexture = new DynamicTexture(256, 256);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        int i = 24;
        int j = this.height / 4 + 48;

        if (this.mc.isDemo())
        {
        }
        else
        {
            this.addSingleplayerMultiplayerButtons(j, 24);
        }

        this.buttonList.add(new GuiButton(0, this.width / 2 - 100, j + 72 + 12, 98, 20, I18n.format("menu.options", new Object[0])));
        this.buttonList.add(new GuiButton(4, this.width / 2 + 2, j + 72 + 12, 98, 20, I18n.format("menu.quit", new Object[0])));
        this.buttonList.add(new GuiButtonLanguage(5, this.width / 2 - 124, j + 72 + 12));

        synchronized (this.threadLock)
        {
            this.field_92023_s = this.fontRendererObj.getStringWidth(this.openGLWarning1);
            this.field_92024_r = this.fontRendererObj.getStringWidth(this.openGLWarning2);
            int k = Math.max(this.field_92023_s, this.field_92024_r);
            this.field_92022_t = (this.width - k) / 2;
            this.field_92021_u = ((GuiButton)this.buttonList.get(0)).yPosition - 24;
            this.field_92020_v = this.field_92022_t + k;
            this.field_92019_w = this.field_92021_u + 24;
        }

        this.mc.func_181537_a(false);
    }

    private void addSingleplayerMultiplayerButtons(int p_73969_1_, int p_73969_2_)
    {
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, p_73969_1_, I18n.format("menu.singleplayer", new Object[0])));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 100, p_73969_1_ + p_73969_2_ * 1, I18n.format("menu.multiplayer", new Object[0])));
        this.buttonList.add(this.realmsButton = new GuiButton(14, this.width / 2 - 100, p_73969_1_ + p_73969_2_ * 2, I18n.format("menu.online", new Object[0])));
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.id == 0)
        {
            this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
        }

        if (button.id == 5)
        {
            this.mc.displayGuiScreen(new GuiLanguage(this, this.mc.gameSettings, this.mc.getLanguageManager()));
        }

        if (button.id == 1)
        {
            this.mc.displayGuiScreen(new GuiSelectWorld(this));
        }

        if (button.id == 2)
        {
            this.mc.displayGuiScreen(new GuiMultiplayer(this));
        }

        if (button.id == 14 && this.realmsButton.visible)
        {
            this.switchToRealms();
        }

        if (button.id == 4)
        {
            this.mc.shutdown();
        }
    }

    private void switchToRealms()
    {
        RealmsBridge realmsbridge = new RealmsBridge();
        realmsbridge.switchToRealms(this);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        GlStateManager.disableAlpha();
        GlStateManager.enableAlpha();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        int i = 274;
        int j = this.width / 2 - i / 2;
        int k = 30;
        this.drawGradientRect(0, 0, this.width, this.height, -2130706433, 16777215);
        this.drawGradientRect(0, 0, this.width, this.height, 0, Integer.MIN_VALUE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        
        ScaledResolution scaledRes = new ScaledResolution(this.mc);
        this.mc.getTextureManager().bindTexture(new ResourceLocation("hackzz/bg.jpg"));
        Gui.drawScaledCustomSizeModalRect(0, 0, 0.0F, 0.0F, scaledRes.getScaledWidth(), scaledRes.getScaledHeight(), scaledRes.getScaledWidth(), scaledRes.getScaledHeight(), scaledRes.getScaledWidth(), scaledRes.getScaledHeight());
        

        GlStateManager.pushMatrix();
        GlStateManager.translate((float)(this.width / 2 + 90), 70.0F, 0.0F);
        GlStateManager.rotate(-20.0F, 0.0F, 0.0F, 1.0F);
        float f = 1.8F - MathHelper.abs(MathHelper.sin((float)(Minecraft.getSystemTime() % 1000L) / 1000.0F * (float)Math.PI * 2.0F) * 0.1F);
        GlStateManager.scale(f, f, f);
        GlStateManager.popMatrix();
        String s = "Minecraft 1.8.8";

        this.drawString(this.fontRendererObj, s, 2, this.height - 10, -1);
        String s1 = "Copyright Mojang AB. Do not distribute!";
        this.drawString(this.fontRendererObj, s1, this.width - this.fontRendererObj.getStringWidth(s1) - 2, this.height - 10, -1);
        
        float scale = 5.0F;
        GL11.glScalef(scale, scale, scale);
        drawString(this.fontRendererObj, "§cClient§fName", this.width / 2 / scale - 25.0F, this.height / 5 / scale - 0.0F, 16777215);
        GL11.glScalef(1.0F / scale, 1.0F / scale, 1.0F / scale);

        if (this.openGLWarning1 != null && this.openGLWarning1.length() > 0)
        {
            drawRect(this.field_92022_t - 2, this.field_92021_u - 2, this.field_92020_v + 2, this.field_92019_w - 1, 1428160512);
            this.drawString(this.fontRendererObj, this.openGLWarning1, this.field_92022_t, this.field_92021_u, -1);
            this.drawString(this.fontRendererObj, this.openGLWarning2, (this.width - this.field_92024_r) / 2, ((GuiButton)this.buttonList.get(0)).yPosition - 12, -1);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

}
