package dmr.DragonMounts.abilities.scripting.wrappers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;

public class WorldLuaWrapper {

	private static final TargetingConditions conditions = TargetingConditions.forCombat().ignoreLineOfSight();
	private final Level world;

	public WorldLuaWrapper(Level world) {
		this.world = world;
	}

	public String getDimension() {
		return world.dimension().location().toString();
	}

	public String getBiome(int x, int y, int z) {
		return world.getBiome(new BlockPos(x, y, z)).getRegisteredName();
	}

	public int getLightLevel(int x, int y, int z) {
		return world.getBrightness(LightLayer.BLOCK, new BlockPos(x, y, z));
	}

	public int getSkyLightLevel(int x, int y, int z) {
		return world.getBrightness(LightLayer.SKY, new BlockPos(x, y, z));
	}

	public boolean isRaining() {
		return world.isRaining();
	}

	public boolean isThundering() {
		return world.isThundering();
	}

	public boolean isDay() {
		return world.isDay();
	}

	public boolean isNight() {
		return world.isNight();
	}

	public PlayerLuaWrapper[] getPlayers() {
		return world.players().stream().map(PlayerLuaWrapper::new).toArray(PlayerLuaWrapper[]::new);
	}

	public EntityLuaWrapper<?>[] getEntitiesInRadius(int x, int y, int z, double radius) {
		return world
			.getEntitiesOfClass(LivingEntity.class, new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius))
			.stream()
			.map(EntityLuaWrapper::new)
			.toArray(EntityLuaWrapper[]::new);
	}

	public EntityLuaWrapper<?>[] getHostilesInRadius(EntityLuaWrapper<?> hostileTo, int x, int y, int z, double radius) {
		return world
			.getNearbyEntities(
				Monster.class,
				conditions,
				hostileTo.entity,
				new AABB(x - (radius / 2), y - (radius / 2), z - (radius / 2), x + (radius / 2), y + (radius / 2), z + (radius / 2))
			)
			.stream()
			.filter(entity -> !entity.isAlliedTo(hostileTo.entity))
			.map(EntityLuaWrapper::new)
			.toArray(EntityLuaWrapper[]::new);
	}

	public String getBlock(int x, int y, int z) {
		return world.getBlockState(new BlockPos(x, y, z)).getBlock().getDescriptionId();
	}

	public void setBlock(int x, int y, int z, String block) {
		var resourceLocation = ResourceLocation.tryParse(block);
		var blockIns = BuiltInRegistries.BLOCK.get(resourceLocation);
		world.setBlock(new BlockPos(x, y, z), blockIns.defaultBlockState(), 3);
	}

	public boolean isBlock(int x, int y, int z, String block) {
		var resourceLocation = ResourceLocation.tryParse(block);
		var blockIns = BuiltInRegistries.BLOCK.get(resourceLocation);
		return world.getBlockState(new BlockPos(x, y, z)).getBlock() == blockIns;
	}

	public boolean hasBlockTag(int x, int y, int z, String tag) {
		var resourceLocation = ResourceLocation.tryParse(tag);
		assert resourceLocation != null;
		var blockTag = BlockTags.create(resourceLocation);
		return world.getBlockState(new BlockPos(x, y, z)).is(blockTag);
	}

	public void breakBlock(int x, int y, int z) {
		world.destroyBlock(new BlockPos(x, y, z), true);
	}
}
