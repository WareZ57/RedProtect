package br.net.fabiozumbi12.RedProtect.Sponge;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.animal.RideableHorse;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentType;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RPVHelper8 implements RPVHelper{

	RPVHelper8(){}

	@Override
	public Cause getCause(CommandSource p){
		if (p instanceof Player)
			return Cause.of(EventContext.builder().add(EventContextKeys.PLAYER, (Player)p).build(), p);
		else
			return Cause.of(EventContext.builder().add(EventContextKeys.PLUGIN, RedProtect.get().container).build(), p);
	}

	@Override
	public void closeInventory(Player p) {
		p.closeInventory();
	}

	@Override
	public void openInventory(Inventory inv, Player p) {
		p.openInventory(inv);
	}

	@Override
	public void setBlock(Location<World> loc, BlockState block) {
        loc.setBlock(block);
	}

	@Override
	public void digBlock(Player p, ItemStack item, Vector3i loc) {
		p.getWorld().digBlockWith(loc, item, p.getProfile());
	}

	@Override
	public void digBlock(Player p, Vector3i loc) {
		p.getWorld().digBlock(loc, p.getProfile());
	}

	@Override
	public void removeBlock(Location<World> loc) {
		loc.removeBlock();
	}

	@Override
	public boolean checkCause(Cause cause, String toCompare) {
        return RedProtect.get().game.getRegistry().getType(EventContextKey.class, toCompare).isPresent() && cause.contains(RedProtect.get().game.getRegistry().getType(EventContextKey.class, toCompare).get());
    }

	@Override
	public boolean checkHorseOwner(Entity ent, Player p) {
		if (ent instanceof RideableHorse && ((RideableHorse)ent).getHorseData().get(Keys.TAMED_OWNER).isPresent()){
			RideableHorse tam = (RideableHorse) ent;
			Player owner = RedProtect.get().serv.getPlayer(tam.getHorseData().get(Keys.TAMED_OWNER).get().get()).get();
			return owner.getName().equals(p.getName());
		}
		return false;
	}

	@Override
	public List<String> getAllEnchants() {
		return Sponge.getRegistry().getAllOf(EnchantmentType.class).stream().map(EnchantmentType::getId).collect(Collectors.toList());
	}

    @Override
    public ItemStack offerEnchantment(ItemStack item) {
        item.offer(Keys.ITEM_ENCHANTMENTS, Collections.singletonList(Enchantment.builder().type(EnchantmentTypes.UNBREAKING).level(1).build()));
        return item;
    }

	@Override
	public long getInvValue(Iterable<Inventory> inv){
		long value = 0;
		for (Inventory item:inv){
			if (item.peek().isEmpty()){
				continue;
			}
			ItemStack stack = item.peek();
			value += ((RedProtect.get().cfgs.getBlockCost(stack.getType().getId()) * stack.getQuantity()));
			if (stack.get(Keys.ITEM_ENCHANTMENTS).isPresent()){
				for (Enchantment enchant:stack.get(Keys.ITEM_ENCHANTMENTS).get()){
					value += ((RedProtect.get().cfgs.getEnchantCost(enchant.getType().getId()) * enchant.getLevel()));
				}
			}
		}
		return value;
	}

	@Override
	public Inventory query(Inventory inventory, int x, int y){
		return inventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(SlotPos.of(x,y)));
	}

	@Override
	public ItemStack getItemMainHand(Player player){
		if (!player.getItemInHand(HandTypes.MAIN_HAND).isEmpty())
			return player.getItemInHand(HandTypes.MAIN_HAND);

		return ItemStack.empty();
	}

	@Override
	public ItemStack getItemOffHand(Player player){
		if (!player.getItemInHand(HandTypes.OFF_HAND).isEmpty())
			return player.getItemInHand(HandTypes.OFF_HAND);

		return ItemStack.empty();
	}

	@Override
	public ItemType getItemInHand(Player player){
		if (!player.getItemInHand(HandTypes.MAIN_HAND).isEmpty()){
			return player.getItemInHand(HandTypes.MAIN_HAND).getType();
		} else if (!player.getItemInHand(HandTypes.OFF_HAND).isEmpty()){
			return player.getItemInHand(HandTypes.OFF_HAND).getType();
		}
		return ItemTypes.NONE;
	}
	@Override
	public ItemType getItemType(ItemStack itemStack){
		return itemStack.getType();
	}

	@Override
	public Inventory newInventory(int size, String name){
		return Inventory.builder().of(InventoryArchetypes.DOUBLE_CHEST)
				.property(InventoryDimension.of(new Vector2i(9,size/9)))
				.property(InventoryTitle.of(RPUtil.toText(name)))
				.build(RedProtect.get().container);
	}

	@Override
	public void removeGuiItem(Player p){
		p.getInventory().slots().forEach(slot -> {
			if (!slot.peek().isEmpty()){
				ItemStack pitem = slot.peek();
				if (RPUtil.removeGuiItem(pitem)){
					slot.poll();
				}
			}
		});
	}
}
