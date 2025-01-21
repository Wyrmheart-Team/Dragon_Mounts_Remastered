package dmr.DragonMounts.abilities.scripting.wrappers;

public class LuaRandomWrapper {

	private final java.util.Random random;

	public LuaRandomWrapper(java.util.Random random) {
		this.random = random;
	}

	public int nextInt(int bound) {
		return random.nextInt(bound);
	}

	public double nextDouble() {
		return random.nextDouble();
	}

	public boolean nextBoolean() {
		return random.nextBoolean();
	}

	public long nextLong() {
		return random.nextLong();
	}

	public double nextGaussian() {
		return random.nextGaussian();
	}

	public void setSeed(long seed) {
		random.setSeed(seed);
	}
}
