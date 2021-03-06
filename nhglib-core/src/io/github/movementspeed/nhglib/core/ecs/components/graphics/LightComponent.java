package io.github.movementspeed.nhglib.core.ecs.components.graphics;

import com.artemis.PooledComponent;
import io.github.movementspeed.nhglib.enums.LightType;
import io.github.movementspeed.nhglib.graphics.lights.NhgLight;

/**
 * Created by Fausto Napoli on 14/03/2017.
 */
public class LightComponent extends PooledComponent {
    public NhgLight light;
    public LightType type;

    @Override
    protected void reset() {
        light = null;
        type = null;
    }
}
