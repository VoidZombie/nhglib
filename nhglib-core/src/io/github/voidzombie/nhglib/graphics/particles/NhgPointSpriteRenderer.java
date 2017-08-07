package io.github.voidzombie.nhglib.graphics.particles;

import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels.ColorInitializer;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels.Rotation2dInitializer;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels.ScaleInitializer;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels.TextureRegionInitializer;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerComponent;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ParticleControllerRenderer;
import com.badlogic.gdx.graphics.g3d.particles.renderers.PointSpriteControllerRenderData;

/**
 * A {@link ParticleControllerRenderer} which will render particles as point sprites to a {@link PointSpriteParticleBatch} .
 *
 * @author Inferno
 */
public class NhgPointSpriteRenderer extends ParticleControllerRenderer<PointSpriteControllerRenderData, PointSpriteSoftParticleBatch> {
    public NhgPointSpriteRenderer() {
        super(new PointSpriteControllerRenderData());
    }

    public NhgPointSpriteRenderer(PointSpriteSoftParticleBatch batch) {
        this();
        setBatch(batch);
    }

    @Override
    public void allocateChannels() {
        renderData.positionChannel = controller.particles.addChannel(ParticleChannels.Position);
        renderData.regionChannel = controller.particles.addChannel(ParticleChannels.TextureRegion, TextureRegionInitializer.get());
        renderData.colorChannel = controller.particles.addChannel(ParticleChannels.Color, ColorInitializer.get());
        renderData.scaleChannel = controller.particles.addChannel(ParticleChannels.Scale, ScaleInitializer.get());
        renderData.rotationChannel = controller.particles.addChannel(ParticleChannels.Rotation2D, Rotation2dInitializer.get());
    }

    @Override
    public boolean isCompatible(ParticleBatch<?> batch) {
        return batch instanceof PointSpriteSoftParticleBatch;
    }

    @Override
    public ParticleControllerComponent copy() {
        return new NhgPointSpriteRenderer(batch);
    }

}