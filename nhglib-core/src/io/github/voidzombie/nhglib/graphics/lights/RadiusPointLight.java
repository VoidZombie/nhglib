package io.github.voidzombie.nhglib.graphics.lights;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by Fausto Napoli on 13/03/2017.
 */
public class RadiusPointLight extends PointLight {
    public float radius = 1.0f;

    public RadiusPointLight set(Color color, Vector3 position, float intensity, float radius) {
        this.radius = radius;
        super.set(color, position, intensity);

        return this;
    }
}