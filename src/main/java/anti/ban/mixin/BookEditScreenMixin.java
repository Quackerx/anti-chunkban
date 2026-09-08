package anti.ban.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Callable;

import static anti.ban.Saver.dropsEnabled;
import static anti.ban.Saver.signEnabled;

@Mixin(BookEditScreen.class)
public class BookEditScreenMixin extends Screen {

    // Lets you fill the book, drop it and sign it

    protected BookEditScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void MaxButtonCooked(CallbackInfo ci) {

        Random rand = new Random();
        int randomNum = rand.nextInt(Integer.MAX_VALUE);

        Callable<Character> charProvider = () -> (char) (rand.nextInt(0xD7FF - 0x20) + 0x20); // Generates a random charater

        Callable<String> pageGenerator = () -> {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 256; i++) { // 256 characters, bypasses grimAC (most likely, unless I miscalculated)
                builder.append(charProvider.call());
            }
            return builder.toString();
        };

        List<String> generatedPages = new ArrayList<>();
        for (int i = 0; i < 100; i++) { // 100 Pages
            try {
                generatedPages.add(pageGenerator.call());
            } catch (Exception e) {
                e.printStackTrace();
                generatedPages.add("");
            }
        }

        Button SignButton = Button.builder(Component.nullToEmpty(signEnabled ? "Sign: ON" : "Sign: OFF"), btn -> {
                            signEnabled = !signEnabled;
                            btn.setMessage(Component.nullToEmpty("Sign: " + (signEnabled ? "ON" : "OFF")));
                        }).pos(15, 400).size(110, 20).build(); // IF you don't know what this is, then I don't know either

        this.addRenderableWidget(SignButton); // Adds the button

        Button DropButton = Button.builder(Component.nullToEmpty(dropsEnabled ? "Drop: ON" : "Drop: OFF"), btn -> {
                            dropsEnabled = !dropsEnabled;
                            btn.setMessage(Component.nullToEmpty("Drop: " + (dropsEnabled ? "ON" : "OFF")));
                        }).pos(15, 350).size(110, 20).build();


        this.addRenderableWidget(DropButton);

        Button sButton = Button.builder(Component.nullToEmpty("Fill Book"), _ -> { // Fills the book with the generated data
                            ServerboundEditBookPacket MaxPacket; // Packet define

                            if (signEnabled) {
                                MaxPacket = new ServerboundEditBookPacket(Minecraft.getInstance().player.getInventory().getSelectedSlot(), generatedPages, Optional.of("Book #" + randomNum)); // Generate the packet
                            } else {
                                MaxPacket = new ServerboundEditBookPacket(Minecraft.getInstance().player.getInventory().getSelectedSlot(), generatedPages, Optional.empty()); // Generate the packet (no sign)
                            }

                            Minecraft.getInstance().getConnection().send(MaxPacket); // Sends the packet
                            this.minecraft.setScreenAndShow(null); // closes the book

                            if (dropsEnabled) { // Drops the book if drop enabled
                                int slot = Minecraft.getInstance().player.getInventory().getSelectedSlot();
                                int inventorySlot = slot + 36;
                                Minecraft.getInstance().gameMode.handleContainerInput(Minecraft.getInstance().player.containerMenu.containerId, inventorySlot, 1, ContainerInput.THROW, Minecraft.getInstance().player);
                            }

                        }).pos(15, 300).size(70, 20).build();


        this.addRenderableWidget(sButton); // Add the button obviously

    }
}
