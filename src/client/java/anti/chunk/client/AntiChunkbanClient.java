package anti.chunk.client;

import net.fabricmc.api.ClientModInitializer;

public class AntiChunkbanClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		System.out.println("Anti-Chunkban successfully initialized!"); // Just shows that it works
	}
}