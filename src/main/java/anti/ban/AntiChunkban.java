package anti.ban;

import net.fabricmc.api.ModInitializer;

public class AntiChunkban implements ModInitializer {
	@Override
	public void onInitialize() {
		// Lowk is useless in this case but whatever, if you don't see this debug text, then something is wrong:
		System.out.println("AntiChunkban initialized");
	}
}
