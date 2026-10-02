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

import java.util.*;
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

        Callable<Character> charProvider2 = () -> (char) (rand.nextInt(0xD7FF - 0x20) + 0x20); // Generates a random charater

        Callable<String> pageGenerator = () -> {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 256; i++) { // 256 characters, bypasses grimAC
                builder.append(charProvider2.call());
            }
            return builder.toString();
        };

        Callable<String> pageGenerator2 = () -> {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < 512; i++) { // 512, Max for paper when using random chinese chars
                builder.append(charProvider2.call());
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

        List<String> generatedPages2 = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            try {
                generatedPages2.add(pageGenerator2.call());
            } catch (Exception e) {
                e.printStackTrace();
                generatedPages2.add("");
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
                        }).pos(15, 375).size(110, 20).build();


        this.addRenderableWidget(DropButton);

        // Chunkban book creation

        Button sButton = Button.builder(Component.nullToEmpty("75KB"), _ -> { // Fills the book with the generated data
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


        this.addRenderableWidget(sButton); // Add the button


        // 150KB PaperMC book creation


        Button PaperMaxButton2 = Button.builder(
                        Component.nullToEmpty("150KB"),
                        _ -> {
                            ServerboundEditBookPacket PaperMaxPacket;

                            if (signEnabled) {
                                PaperMaxPacket = new ServerboundEditBookPacket(this.minecraft.player.getInventory().getSelectedSlot(), generatedPages2, Optional.of("512 book #" + randomNum));
                            } else {
                                PaperMaxPacket = new ServerboundEditBookPacket(this.minecraft.player.getInventory().getSelectedSlot(), generatedPages2, Optional.empty());
                            }

                            this.minecraft.getConnection().send(PaperMaxPacket);
                            this.minecraft.setScreenAndShow(null);

                            if (dropsEnabled) {
                                int slot = this.minecraft.player.getInventory().getSelectedSlot();
                                int inventorySlot = slot + 36;
                                this.minecraft.gameMode.handleContainerInput(this.minecraft.player.containerMenu.containerId, inventorySlot, 1, ContainerInput.THROW, this.minecraft.player);
                            }

                        }
                ).pos(15, 325)
                .size(70, 20)
                .build();

        this.addRenderableWidget(PaperMaxButton2);


        // 200KB PaperMC book creation

        PrimitiveIterator.OfInt oneByte = rand.ints(0x21, 0x80).iterator();
        PrimitiveIterator.OfInt twoBytes = rand.ints(0x0080, 0x0800).iterator();
        PrimitiveIterator.OfInt threeBytes = rand.ints(0x0800, 0xD800).iterator();

        List<String> generatedPages4 = new ArrayList<>(); // Carefully loaded pages to reach the absolute maximum size
        StringBuilder page = new StringBuilder();

        for (int pageIndex = 0; pageIndex < 100; pageIndex++) {
            if (pageIndex < 50) {
                page.appendCodePoint(threeBytes.nextInt());
                for (int i = 1; i < 1024; i++) {
                    page.appendCodePoint(oneByte.nextInt());
                }
            } else if (pageIndex == 50) {
                for (int i = 0; i < 110; i++) {
                    page.appendCodePoint(threeBytes.nextInt());
                }
                page.appendCodePoint(twoBytes.nextInt());
                for (int i = 0; i < 913; i++) {
                    page.appendCodePoint(oneByte.nextInt());
                }
            } else {
                for (int i = 0; i < 1024; i++) {
                    page.appendCodePoint(threeBytes.nextInt());
                }
            }

            generatedPages4.add(page.toString());
            page.setLength(0);
        }

        Button PaperMaxButton = Button.builder(
                        Component.nullToEmpty("200KB"),
                        _ -> {
                            ServerboundEditBookPacket PaperMaxPacket;

                            if (signEnabled) {
                                PaperMaxPacket = new ServerboundEditBookPacket(this.minecraft.player.getInventory().getSelectedSlot(), generatedPages4, Optional.of("Max book #" + randomNum));
                            } else {
                                PaperMaxPacket = new ServerboundEditBookPacket(this.minecraft.player.getInventory().getSelectedSlot(), generatedPages4, Optional.empty());
                            }

                            this.minecraft.getConnection().send(PaperMaxPacket);
                            this.minecraft.setScreenAndShow(null);

                            if (dropsEnabled) {
                                int slot = this.minecraft.player.getInventory().getSelectedSlot();
                                int inventorySlot = slot + 36;
                                this.minecraft.gameMode.handleContainerInput(this.minecraft.player.containerMenu.containerId, inventorySlot, 1, ContainerInput.THROW, this.minecraft.player);
                            }

                        }
                ).pos(15, 350)
                .size(70, 20)
                .build();

        this.addRenderableWidget(PaperMaxButton);


    }
}
