package lando.systems.ld46.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import lando.systems.ld46.Assets;
import lando.systems.ld46.physics.PhysicsComponent;
import lando.systems.ld46.utils.Utils;

public class Particles implements Disposable {

    public enum Layer { background, middle, foreground }

    private static final int MAX_PARTICLES = 4000;

    private final Assets assets;
    private final ObjectMap<Layer, Array<Particle>> activeParticles;
    private final Pool<Particle> particlePool = Pools.get(Particle.class, MAX_PARTICLES);
    private Array<PhysicsComponent> physicsParticles;

    public Particles(Assets assets) {
        this.assets = assets;
        physicsParticles = new Array<>();
        this.activeParticles = new ObjectMap<>();
        int particlesPerLayer = MAX_PARTICLES / Layer.values().length;
        this.activeParticles.put(Layer.background, new Array<>(false, particlesPerLayer));
        this.activeParticles.put(Layer.middle,     new Array<>(false, particlesPerLayer));
        this.activeParticles.put(Layer.foreground, new Array<>(false, particlesPerLayer));
    }

    public void clear() {
        for (Layer layer : Layer.values()) {
            particlePool.freeAll(activeParticles.get(layer));
            activeParticles.get(layer).clear();
        }
    }

    public void update(float dt) {
        for (Layer layer : Layer.values()) {
            for (int i = activeParticles.get(layer).size - 1; i >= 0; --i) {
                Particle particle = activeParticles.get(layer).get(i);
                particle.update(dt);
                if (particle.isDead()) {
                    activeParticles.get(layer).removeIndex(i);
                    particlePool.free(particle);
                }
            }
        }
    }

    public Array<PhysicsComponent> getPhysicalParticles() {
        physicsParticles.clear();
        for (Layer layer : Layer.values()) {
            for (int i = activeParticles.get(layer).size -1; i >= 0; i--) {
                Particle particle = activeParticles.get(layer).get(i);
                if (particle.hasPhysics()) physicsParticles.add(particle);
            }
        }
        return physicsParticles;
    }

    public void draw(SpriteBatch batch, Layer layer) {
        activeParticles.get(layer).forEach(particle -> particle.draw(batch));
    }

    @Override
    public void dispose() {
        clear();
    }

    // ------------------------------------------------------------------------
    // Helper fields for particle spawner methods
    // ------------------------------------------------------------------------
    private final Color tempColor = new Color();
    private final Vector2 tempVec2 = new Vector2();
    // ------------------------------------------------------------------------
    // Spawners for different particle effects
    // NOTE: create spawner methods for each different particle effect, using the following as a template...
    //       see Particle.Initializer for all the possible parameters
    // ------------------------------------------------------------------------

    public void addParticles(float x, float y) {
        TextureRegion keyframe = assets.debugTexture;

        int numParticles = 100;
        for (int i = 0; i < numParticles; ++i) {
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(keyframe)
                    .startPos(x, y)
                    .velocityDirection(MathUtils.random(360f), MathUtils.random(300f, 40000f))
                    .startSize(10f, 10f)
                    .endSize(0f, 0f)
                    .startAlpha(1f)
                    .endAlpha(0f)
                    .startRotation(0f)
                    .endRotation(360f)
                    .timeToLive(1.5f)
                    .interpolation(Interpolation.slowFast)
                    .init());
        }
    }

    private Color testColor = new Color();
    public void makePhysicsParticles(float x, float y) {
        TextureRegion keyframe = assets.whiteCircle;

        int numParticles = 200;
        for (int i = 0; i < numParticles; ++i) {
            Utils.hsvToRgb(MathUtils.random(1f), 1f, .9f, testColor);
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(keyframe)
                    .startPos(x, y)
                    .velocityDirection(MathUtils.random(360f), MathUtils.random(30f, 1000f))
                    .startSize(5f)
                    .endSize(.1f)
                    .startAlpha(1f)
                    .endAlpha(0f)
                    .timeToLive(3f)
                    .startColor(testColor)
                    .makePhysics()
                    .interpolation(Interpolation.fastSlow)
                    .init());
        }
    }

    public void makeBloodParticles(float x, float y) {
        TextureRegion keyframe = assets.whiteCircle;

        int numParticles = 50;
        for (int i = 0; i < numParticles; ++i) {
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(keyframe)
                    .startPos(x, y)
                    .velocityDirection(MathUtils.random(200f), MathUtils.random(30f, 50f))
                    .startSize(5f)
                    .endSize(1f)
                    .startAlpha(1f)
                    .endAlpha(1f)
                    .timeToLive(7f)
                    .startColor(Color.RED)
                    .makePhysicsWithCustomBounceScale(.4f)
                    .init());
        }
    }

    public void makeExplodingZombieParticles(float x, float y) {
        activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                .keyframe(assets.zombieRippedArm)
                .startPos(x, y)
                .velocityDirection(MathUtils.random(100f), MathUtils.random(30f, 3500f))
                .startSize(assets.zombieRippedArm.getRegionWidth(), assets.zombieRippedArm.getRegionHeight())
                .startAlpha(1f)
                .endAlpha(1f)
                .timeToLive(30f)
                .startRotation(0f)
                .endRotation(2400f)
                .makePhysicsWithCustomBounceScale(.4f)
                .interpolation(new Interpolation.ExpOut(2, 50))
                .init());

        activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                .keyframe(assets.zombieRippedHead)
                .startPos(x, y)
                .velocityDirection(MathUtils.random(100f), MathUtils.random(30f, 3500f))
                .startSize(assets.zombieRippedHead.getRegionWidth(), assets.zombieRippedHead.getRegionHeight())
                .startAlpha(1f)
                .endAlpha(1f)
                .timeToLive(30f)
                .startRotation(0f)
                .endRotation(2400f)
                .makePhysicsWithCustomBounceScale(.4f)
                .interpolation(new Interpolation.ExpOut(2, 50))
                .init());

        activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                .keyframe(assets.zombieRippedLeg)
                .startPos(x, y)
                .velocityDirection(MathUtils.random(100f), MathUtils.random(30f, 3500f))
                .startSize(assets.zombieRippedLeg.getRegionWidth(), assets.zombieRippedLeg.getRegionHeight())
                .startAlpha(1f)
                .endAlpha(1f)
                .timeToLive(30f)
                .startRotation(0f)
                .endRotation(2400f)
                .makePhysicsWithCustomBounceScale(.4f)
                .interpolation(new Interpolation.ExpOut(2, 50))
                .init());

        activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                .keyframe(assets.zombieRippedTorso)
                .startPos(x, y)
                .velocityDirection(MathUtils.random(100f), MathUtils.random(30f, 3500f))
                .startSize(assets.zombieRippedTorso.getRegionWidth(), assets.zombieRippedTorso.getRegionHeight())
                .startAlpha(1f)
                .endAlpha(1f)
                .timeToLive(30f)
                .startRotation(0f)
                .endRotation(2400f)
                .makePhysicsWithCustomBounceScale(.4f)
                .interpolation(new Interpolation.ExpOut(2, 50))
                .init());

        for (int i = 0; i < 200; ++i) {
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(assets.whiteCircle)
                    .startPos(x, y)
                    .velocityDirection(MathUtils.random(200f), MathUtils.random(30f, 500f))
                    .startSize(5f)
                    .endSize(1f)
                    .startAlpha(1f)
                    .endAlpha(1f)
                    .timeToLive(7f)
                    .startColor(Color.RED)
                    .makePhysicsWithCustomBounceScale(.4f)
                    .init());
        }

    }

}
