package dmr.DragonMounts.client.particle;

import dmr.DragonMounts.util.MiscUtils;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.particle.ParticleRenderType;
import org.joml.Vector3f;

public class DragonBreathParticle extends FlameParticle {
    // Store the gradient colors
    private final List<Vector3f> gradientColors;

    public DragonBreathParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            List<Vector3f> gradientColors) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);

        this.gradientColors = gradientColors;

        // Initialize with first color in gradient
        Vector3f initialColor = gradientColors.getFirst();
        this.setColor(initialColor.x(), initialColor.y(), initialColor.z());

        // Particle properties
        this.scale(1.0F);
        this.lifetime = 20 + this.random.nextInt(12);

        // Initial transparency
        this.alpha = 1F;
        this.hasPhysics = true;
    }

    @Override
    public void tick() {
        super.tick();

        // Calculate life progress (0.0 to 1.0)
        float lifetimeRatio = (float) this.age / (float) this.lifetime;
        float alphaLifetime = (float) this.age / ((float) this.lifetime * 2);

        // Update alpha (fade out)
        this.alpha = 1 * (1.0F - alphaLifetime);

        // Update color based on age
        updateGradientColor(lifetimeRatio);

        // Make the particle grow as it travels
        this.scale(1.08F);
        this.yd += 0.004;
    }

    /**
     * Updates the particle color based on its age through the gradient
     * Uses HSV interpolation for natural color transitions
     */
    private void updateGradientColor(float progress) {
        if (gradientColors.size() == 1) {
            // Only one color in gradient, just use it
            Vector3f color = gradientColors.getFirst();
            this.setColor(color.x(), color.y(), color.z());
            return;
        }

        // Ensure progress is clamped between 0 and 1
        progress = Math.max(0.0F, Math.min(1.0F, progress));

        // Multiple colors - find the right segment
        float segmentCount = gradientColors.size() - 1;
        float segmentSize = 1.0f / segmentCount;

        // Find which segment of the gradient we're in
        int index = (int) (progress * segmentCount);

        // Clamp index to valid range
        index = Math.max(0, Math.min(index, gradientColors.size() - 2));

        // Calculate progress within this segment (0-1)
        float segmentProgress = (progress - (index * segmentSize)) / segmentSize;
        segmentProgress = Math.max(0.0F, Math.min(1.0F, segmentProgress));

        // Get the two colors to interpolate between
        Vector3f rgbA = gradientColors.get(index);
        Vector3f rgbB = gradientColors.get(index + 1);

        // Convert RGB to HSV
        float[] hsvA = MiscUtils.rgbToHsv(rgbA.x(), rgbA.y(), rgbA.z());
        float[] hsvB = MiscUtils.rgbToHsv(rgbB.x(), rgbB.y(), rgbB.z());

        // Interpolate in HSV space
        float[] hsvResult = new float[3];

        // Special handling for hue to ensure we go the shortest distance around the color wheel
        float hueDiff = hsvB[0] - hsvA[0];
        if (hueDiff > 0.5f) hsvA[0] += 1.0f;
        if (hueDiff < -0.5f) hsvB[0] += 1.0f;

        hsvResult[0] = hsvA[0] + segmentProgress * (hsvB[0] - hsvA[0]);
        hsvResult[1] = hsvA[1] + segmentProgress * (hsvB[1] - hsvA[1]);
        hsvResult[2] = hsvA[2] + segmentProgress * (hsvB[2] - hsvA[2]);

        // Normalize hue back to 0-1 range
        hsvResult[0] = hsvResult[0] % 1.0f;

        // Convert back to RGB
        float[] rgb = MiscUtils.hsvToRgb(hsvResult[0], hsvResult[1], hsvResult[2]);

        this.setColor(rgb[0], rgb[1], rgb[2]);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
