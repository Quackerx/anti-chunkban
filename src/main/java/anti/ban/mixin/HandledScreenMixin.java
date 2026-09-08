package anti.ban.mixin;

import anti.ban.Saver;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public class HandledScreenMixin extends Screen {


    // ADDS chunkban button to inventory

    protected HandledScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        int width = 115;
        int height = 20;

        this.addRenderableWidget(Button.builder(Component.literal("Anti-Chunkban: " + Saver.chunkban), b -> {
            Saver.chunkban = !Saver.chunkban;
            b.setMessage(Component.literal("Anti-Chunkban: " + Saver.chunkban));
        }).bounds(5, 10, width, height).build());
    }
}
