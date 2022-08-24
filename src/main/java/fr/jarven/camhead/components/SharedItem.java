package fr.jarven.camhead.components;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A shared item defines an item created for armor stands.
 * It stores the informations of the config file.
 */
public class SharedItem {
	private final Material material;
	private final int customModelData;

	public SharedItem(Material material, int customModelData) {
		this.material = material;
		this.customModelData = customModelData;
	}

	public static SharedItem fromConfig(YamlConfiguration config, String path) {
		if (config.get(path + ".material") == null) return null;
		Material material = Material.valueOf(config.getString(path + ".material"));
		int customModelData = config.getInt(path + ".custom_model_data", 0);
		return new SharedItem(material, customModelData);
	}

	public static void loadSharedItems(SharedItem[] items, YamlConfiguration config, String path) {
		items[0] = SharedItem.fromConfig(config, path + ".helmet");
		items[1] = SharedItem.fromConfig(config, path + ".chestplate");
		items[2] = SharedItem.fromConfig(config, path + ".leggings");
		items[3] = SharedItem.fromConfig(config, path + ".boots");
		if (items.length > 4) items[4] = SharedItem.fromConfig(config, path + ".mainHand");
		if (items.length > 5) items[5] = SharedItem.fromConfig(config, path + ".offHand");
	}

	public ItemStack createItem() {
		ItemStack item = new ItemStack(material);
		item.setAmount(1);
		if (customModelData != 0) {
			ItemMeta meta = item.getItemMeta();
			meta.setCustomModelData(customModelData);
			item.setItemMeta(meta);
		}
		return item;
	}

	public static ItemStack createItem(SharedItem shared) {
		return shared == null ? null : shared.createItem();
	}

	public static void createArmor(EntityEquipment equipment, SharedItem[] items) {
		equipment.setHelmet(createItem(items[0]));
		equipment.setChestplate(createItem(items[1]));
		equipment.setLeggings(createItem(items[2]));
		equipment.setBoots(createItem(items[3]));
		if (items.length > 4) equipment.setItemInMainHand(createItem(items[4]));
		if (items.length > 5) equipment.setItemInOffHand(createItem(items[5]));
	}
}
