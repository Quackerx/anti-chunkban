package anti.ban;

public final class ChunkPacketState {

    private static final ThreadLocal<Boolean> BAD_LIGHT_DATA = ThreadLocal.withInitial(() -> false);

    private static final ThreadLocal<Boolean> BAD_BLOCK_ENTITY_DATA = ThreadLocal.withInitial(() -> false);

    public static void markBadLightData() {
        BAD_LIGHT_DATA.set(true);
    }

    public static boolean consumeBadLightData() {
        boolean bad = BAD_LIGHT_DATA.get();
        BAD_LIGHT_DATA.set(false);
        return bad;
    }

    public static void markBadBlockEntityData() {
        BAD_BLOCK_ENTITY_DATA.set(true);
    }
}
