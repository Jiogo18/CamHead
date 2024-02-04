package fr.jarven.camhead.utils;

import org.bukkit.Location;

import java.util.UUID;

import fr.jarven.camhead.CamHead;

public class FakePlayerLib {
	private static byte pluginIsAvailable = 0;
	private static Class<?> fakePlayerClass = null;
	private Object fakePlayer;
    
	public FakePlayerLib(Location location, UUID skinUUID, String name) {
		if (!checkIfPluginIsAvailable()) {
			return;
		}

		try {
			fakePlayer = fakePlayerClass
					     .getDeclaredConstructor(Location.class, UUID.class, String.class)
					     .newInstance(location, skinUUID, name);
			fakePlayerClass.getMethod("setVisibleToAll").invoke(fakePlayer);
		} catch (Exception e) {
			onMethodError();
			e.printStackTrace();
		}
	}

	public void destroy() {
		if (!checkIfPluginIsAvailable()) {
			return;
		}
		try {
			fakePlayerClass.getMethod("setInvisibleToAll").invoke(fakePlayer);
		} catch (Exception e) {
			onMethodError();
			e.printStackTrace();
		}
	}

	public static boolean checkIfPluginIsAvailable() {
		if (pluginIsAvailable == 0) {
			boolean enabled = CamHead.getInstance().getConfig().getBoolean("spawnFakePlayer", true);
			if (!enabled) {
				pluginIsAvailable = 2;
				return false;
			}
			try {
				fakePlayerClass = Class.forName("fr.endit.fakeentity.entities.FakePlayer");
				CamHead.LOGGER.info("FakeEntity plugin is available, using it to create fake players");
				pluginIsAvailable = 1;
			} catch (ClassNotFoundException e) {
				CamHead.LOGGER.info("FakeEntity plugin is not available, no entity will be spawned");
				pluginIsAvailable = 2;
			} catch (Exception e) {
				e.printStackTrace();
				pluginIsAvailable = 2;
			}
		}
		return pluginIsAvailable == 1;
	}

    public static void load() {
        pluginIsAvailable = 0;
        fakePlayerClass = null;
        checkIfPluginIsAvailable();
    }

    private void onMethodError() {
        pluginIsAvailable = 2;
        fakePlayerClass = null;
        fakePlayer = null;
        CamHead.LOGGER.info("FakeEntity plugin is not compatible with this version, no entity will be spawned");
    }
}
