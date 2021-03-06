package io.github.movementspeed.nhglib.data.models.serialization.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonValue;
import io.github.movementspeed.nhglib.core.ecs.components.graphics.LightComponent;
import io.github.movementspeed.nhglib.core.ecs.systems.impl.RenderingSystem;
import io.github.movementspeed.nhglib.data.models.serialization.ComponentJson;
import io.github.movementspeed.nhglib.enums.LightType;
import io.github.movementspeed.nhglib.graphics.lights.NhgLight;
import io.github.movementspeed.nhglib.graphics.lights.NhgLightsAttribute;

/**
 * Created by Fausto Napoli on 19/12/2016.
 */
public class LightComponentJson extends ComponentJson {
    @Override
    public void parse(JsonValue jsonValue) {
        RenderingSystem renderingSystem = nhg.entities.getEntitySystem(RenderingSystem.class);
        LightComponent lightComponent = nhg.entities.createComponent(entity, LightComponent.class);

        LightType lightType = LightType.fromString(jsonValue.getString("lightType"));

        //boolean shadowLight = jsonValue.getBoolean("shadowLight", false);

        float range = jsonValue.getFloat("range", 1f);
        float intensity = jsonValue.getFloat("intensity", 1f);
        float innerAngle = jsonValue.getFloat("innerAngle", 0f);
        float outerAngle = jsonValue.getFloat("outerAngle", 0f);

        if (innerAngle > outerAngle) {
            innerAngle = outerAngle;
        }

        JsonValue colorJson = jsonValue.get("color");
        Color color = new Color();

        if (colorJson != null) {
            color = new Color(
                    colorJson.getFloat("r", 1),
                    colorJson.getFloat("g", 1),
                    colorJson.getFloat("b", 1),
                    colorJson.getFloat("a", 1));
        }

        JsonValue directionJson = jsonValue.get("direction");
        Vector3 direction = new Vector3();

        if (directionJson != null) {
            direction = new Vector3(
                    directionJson.getFloat("x", 0),
                    directionJson.getFloat("y", 0),
                    directionJson.getFloat("z", 0));
        }

        NhgLight light = null;

        switch (lightType) {
            case DIRECTIONAL_LIGHT:
                light = NhgLight.directional(intensity, color);
                light.direction.set(direction);
                break;

            case POINT_LIGHT:
                light = NhgLight.point(intensity, range, color);
                break;

            case SPOT_LIGHT:
                light = NhgLight.spot(intensity, range, innerAngle, outerAngle, color);
                light.direction.set(direction);
                break;
        }

        if (light == null) return;

        light.enabled = jsonValue.getBoolean("enabled", true);

        Environment environment = renderingSystem.getEnvironment();
        NhgLightsAttribute attribute = (NhgLightsAttribute) environment
                .get(NhgLightsAttribute.Type);

        if (attribute == null) {
            attribute = new NhgLightsAttribute();
            environment.set(attribute);
        }

        attribute.lights.add(light);

        lightComponent.light = light;
        lightComponent.type = lightType;
        output = lightComponent;
    }
}
