package lando.systems.ld46.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.*;
import lando.systems.ld46.Assets;
import lando.systems.ld46.entities.GameEntity;
import lando.systems.ld46.physics.PhysicsComponent;
import lando.systems.ld46.utils.Utils;

import static lando.systems.ld46.entities.GameEntity.Direction.right;

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
    // ------------------------------------------------------------------------

    public void spawnBodyPartPickup(float x, float y) {
        int numParticles = 40;
        float angle = 0;
        float speed = 40f;
        float increment = 36f;
        float ttl = 1f;
        for (int i = 0; i < numParticles; ++i) {
            float startRotation = MathUtils.random(0f, 360f);
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(assets.particleSparkle)
                    .startPos(x, y)
                    .velocityDirection(angle, speed)
                    .startSize(40f)
                    .endSize(1f)
                    .startAlpha(1f)
                    .endAlpha(0f)
                    .timeToLive(ttl)
                    .startRotation(startRotation)
                    .endRotation(startRotation + MathUtils.random(-3f * 360f, 3f * 360f))
                    .init());
            angle += increment;
            speed += 5f;
//            ttl += 0.1f;
            increment -= 20f / numParticles;
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

    private static final Color[] punchWallExplosionColors = new Color[] {
              new Color(Color.YELLOW)
            , new Color(Color.GOLD)
            , new Color(Color.GOLDENROD)
            , new Color(Color.ORANGE)
            , new Color(Color.BROWN)
            , new Color(Color.TAN)
    };

    /**
     * @param punchDirection negative for a punch towards the left, positive for a punch towards the right
     */
    public void spawnPunchWallExplosion(GameEntity.Direction punchDirection, Rectangle rect) {
        TextureRegion keyframe = assets.whiteCircle;

        int numParticles = 500;
        for (int i = 0; i < numParticles; ++i) {
            float velAngle = (punchDirection == right)
                           ? MathUtils.random(-45f, 45f)   // 90 degree cone facing right
                           : MathUtils.random(135f, 225f); // 90 degree cone facing left
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(keyframe)
                    .startPos(MathUtils.random(rect.x, rect.x + rect.width), MathUtils.random(rect.y, rect.y + rect.height))
                    .velocityDirection(velAngle, MathUtils.random(300f, 600f))
                    .startSize(MathUtils.random(2f, 4f))
                    .endSize(0.1f)
                    .startAlpha(1f)
                    .endAlpha(0.25f)
                    .timeToLive(2.5f)
                    .startColor(punchWallExplosionColors[MathUtils.random(0, punchWallExplosionColors.length - 1)])
                    .makePhysicsWithCustomBounceScale(1.25f)
                    .init());
        }

        for (int i = 0; i < 100; i++){
            float g = MathUtils.random(.7f) + .3f;
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(assets.smokeTex)
                    .startPos(MathUtils.random(rect.x, rect.x + rect.width), MathUtils.random(rect.y, rect.y + rect.height))
                    .velocityDirection(MathUtils.random(360f), MathUtils.random(10f))
                    .startSize(MathUtils.random(20f, 40f))
                    .endSize(MathUtils.random(.1f, 10f))
                    .startAlpha(1f)
                    .endAlpha(0f)
                    .startRotation(MathUtils.random(40))
                    .endRotation(MathUtils.random(-40, 80))
                    .timeToLive(MathUtils.random(1f, 3f))
                    .startColor(g, g, g, 1)
                    .init());
        }
    }

    public void makeZombieBuildClouds(float x, float y) {
        for (int i = 0; i < 20; i++) {
            TextureRegion keyframe = assets.particleBlood1;
            switch (MathUtils.random(0, 2)) {
                case 0: keyframe = assets.particleBlood1; break;
                case 1: keyframe = assets.particleBlood2; break;
                case 2: keyframe = assets.particleBlood3; break;
            }
            activeParticles.get(Layer.middle).add(Particle.initializer(particlePool.obtain())
                    .keyframe(keyframe)
                    .startPos(x, y)
                    .velocityDirection(MathUtils.random(0f, 180f), MathUtils.random(1000f))
                    .startSize(MathUtils.random(10f, 20f))
                    .startAlpha(1f)
                    .endAlpha(0f)
                    .startRotation(MathUtils.random(40))
                    .endRotation(MathUtils.random(-40, 80))
                    .timeToLive(MathUtils.random(1f, 3f))
                    .makePhysics()
                    .init());
        }

        for (int i = 0; i < 100; i++){
            float g = MathUtils.random(.7f) + .3f;
            activeParticles.get(Layer.middle).add(Particle.initializer(particlePool.obtain())
                    .keyframe(assets.smokeTex)
                    .startPos(x, y)
                    .velocityDirection(MathUtils.random(0f, 180f), MathUtils.random(80f))
                    .startSize(MathUtils.random(20f, 40f))
                    .endSize(MathUtils.random(0.1f, 10f))
                    .startAlpha(1f)
                    .endAlpha(0f)
                    .startRotation(MathUtils.random(40))
                    .endRotation(MathUtils.random(-40, 80))
                    .timeToLive(MathUtils.random(1f, 3f))
                    .startColor(g, g, g, 1)
                    .init());
        }
    }

    public void makeSpawnClouds(float x, float y) {
        for (int i = 0; i < 50; i++){
            float g = MathUtils.random(.7f) + .3f;
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(assets.smokeTex)
                    .startPos(x + MathUtils.random(-30f, 30f), y + MathUtils.random(-30f, 30f))
                    .velocityDirection(MathUtils.random(360f), MathUtils.random(10f))
                    .startSize(MathUtils.random(20f, 40f))
                    .endSize(MathUtils.random(.1f, 10f))
                    .startAlpha(1f)
                    .endAlpha(0f)
                    .startRotation(MathUtils.random(40))
                    .endRotation(MathUtils.random(-40, 80))
                    .timeToLive(MathUtils.random(1f, 3f))
                    .startColor(g, g, g, 1)
                    .init());

        }
    }

    public void makeBloodParticles(float x, float y) {
        TextureRegion keyframe = assets.particleBloodSplat1;

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
                    .timeToLive(5f)
                    .startColor(Color.RED)
                    .makePhysicsWithCustomBounceScale(.4f)
                    .interpolation(Interpolation.fastSlow)
                    .init());
        }
    }

    public void makeBloodParticles(GameEntity.Direction direction, float x, float y) {
        TextureRegion keyframe = assets.particleBloodSplat1;

        int numParticles = 50;
        for (int i = 0; i < numParticles; ++i) {
            float xVelocity = direction == GameEntity.Direction.left ?  MathUtils.random(75f, 285f) : MathUtils.random(-105f, 105f);
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(keyframe)
                    .startPos(x, y)
                    .velocityDirection(xVelocity, MathUtils.random(10f, 300f))
                    .startSize(MathUtils.random(1f, 3f))
                    .endSize(1f)
                    .startAlpha(1f)
                    .endAlpha(1f)
                    .timeToLive(5f)
                    .makePhysicsWithCustomBounceScale(.4f)
                    .init());
        }
    }

    public void makeExplodingZombieParticles(float x, float y) {

        // TODO: just explode a bunch of viscera and bones instead of the actual parts here

//        for (int i = 0; i < 2; ++i) {
//            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
//                    .keyframe(assets.zombieRippedArm)
//                    .startPos(x, y)
//                    .velocityDirection(MathUtils.random(360f), MathUtils.random(30f, 3500f))
//                    .startSize(assets.zombieRippedArm.getRegionWidth(), assets.zombieRippedArm.getRegionHeight())
//                    .startAlpha(1f)
//                    .endAlpha(1f)
//                    .timeToLive(30f)
//                    .startRotation(0f)
//                    .endRotation(MathUtils.random(2200f, 3200f))
//                    .makePhysicsWithCustomBounceScale(.4f)
//                    .interpolation(new Interpolation.ExpOut(2, 50))
//                    .init());
//        }
//
//        activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
//                .keyframe(assets.zombieRippedHead)
//                .startPos(x, y)
//                .velocityDirection(MathUtils.random(360f), MathUtils.random(30f, 3500f))
//                .startSize(assets.zombieRippedHead.getRegionWidth(), assets.zombieRippedHead.getRegionHeight())
//                .startAlpha(1f)
//                .endAlpha(1f)
//                .timeToLive(30f)
//                .startRotation(0f)
//                .endRotation(MathUtils.random(2200f, 3200f))
//                .makePhysicsWithCustomBounceScale(.4f)
//                .interpolation(new Interpolation.ExpOut(2, 50))
//                .init());
//
//        for (int i = 0; i < 2; ++i) {
//            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
//                    .keyframe(assets.zombieRippedLeg)
//                    .startPos(x, y)
//                    .velocityDirection(MathUtils.random(360f), MathUtils.random(30f, 3500f))
//                    .startSize(assets.zombieRippedLeg.getRegionWidth(), assets.zombieRippedLeg.getRegionHeight())
//                    .startAlpha(1f)
//                    .endAlpha(1f)
//                    .timeToLive(30f)
//                    .startRotation(0f)
//                    .endRotation(MathUtils.random(2200f, 3200f))
//                    .makePhysicsWithCustomBounceScale(.4f)
//                    .interpolation(new Interpolation.ExpOut(2, 50))
//                    .init());
//        }
//
//        activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
//                .keyframe(assets.zombieRippedTorso)
//                .startPos(x, y)
//                .velocityDirection(MathUtils.random(360f), MathUtils.random(30f, 3500f))
//                .startSize(assets.zombieRippedTorso.getRegionWidth(), assets.zombieRippedTorso.getRegionHeight())
//                .startAlpha(1f)
//                .endAlpha(1f)
//                .timeToLive(30f)
//                .startRotation(0f)
//                .endRotation(MathUtils.random(2200f, 3200f))
//                .makePhysicsWithCustomBounceScale(.4f)
//                .interpolation(new Interpolation.ExpOut(2, 50))
//                .init());

        for (int i = 0; i < 200; ++i) {
            activeParticles.get(Layer.foreground).add(Particle.initializer(particlePool.obtain())
                    .keyframe(assets.particleBloodSplat1)
                    .startPos(x, y)
                    .velocityDirection(MathUtils.random(360f), MathUtils.random(30f, 500f))
                    .startSize(10f)
                    .endSize(1f)
                    .startAlpha(1f)
                    .endAlpha(1f)
                    .timeToLive(7f)
                    .makePhysicsWithCustomBounceScale(.4f)
                    .init());
        }

    }

}
