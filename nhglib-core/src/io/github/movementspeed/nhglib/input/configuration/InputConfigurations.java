package io.github.movementspeed.nhglib.input.configuration;

import com.badlogic.gdx.utils.Array;
import io.github.movementspeed.nhglib.input.configuration.impls.KeyInputConfiguration;
import io.github.movementspeed.nhglib.input.configuration.impls.MouseInputConfiguration;
import io.github.movementspeed.nhglib.input.configuration.impls.PointerInputConfiguration;
import io.github.movementspeed.nhglib.input.configuration.impls.StickInputConfiguration;
import io.github.movementspeed.nhglib.input.controllers.ControllerConfiguration;
import io.github.movementspeed.nhglib.input.enums.MouseSourceType;

/**
 * Created by Fausto Napoli on 25/01/2017.
 */
public class InputConfigurations {
    public final Array<KeyInputConfiguration> keyInputConfigurations;
    public final Array<PointerInputConfiguration> pointerInputConfigurations;
    public final Array<StickInputConfiguration> stickInputConfigurations;
    public final Array<ControllerConfiguration> controllerConfigurations;
    public final Array<MouseInputConfiguration> mouseInputConfigurations;

    public InputConfigurations() {
        keyInputConfigurations = new Array<>();
        pointerInputConfigurations = new Array<>();
        stickInputConfigurations = new Array<>();
        mouseInputConfigurations = new Array<>();
        controllerConfigurations = new Array<>();
    }

    public KeyInputConfiguration getKeyConfiguration(String name) {
        KeyInputConfiguration res = null;

        for (KeyInputConfiguration configuration : keyInputConfigurations) {
            if (configuration.getName().contentEquals(name)) {
                res = configuration;
            }
        }

        return res;
    }

    public PointerInputConfiguration getPointerConfiguration(String name) {
        PointerInputConfiguration res = null;

        for (PointerInputConfiguration configuration : pointerInputConfigurations) {
            if (configuration.getName().contentEquals(name)) {
                res = configuration;
            }
        }

        return res;
    }

    public StickInputConfiguration getStickConfiguration(String name) {
        StickInputConfiguration res = null;

        for (StickInputConfiguration configuration : stickInputConfigurations) {
            if (configuration.getName().contentEquals(name)) {
                res = configuration;
            }
        }

        return res;
    }

    public MouseInputConfiguration getMouseConfiguration(String name) {
        MouseInputConfiguration res = null;

        for (MouseInputConfiguration configuration : mouseInputConfigurations) {
            if (configuration.getName().contentEquals(name)) {
                res = configuration;
            }
        }

        return res;
    }

    public MouseInputConfiguration getMouseConfiguration(MouseSourceType sourceType) {
        MouseInputConfiguration res = null;

        for (MouseInputConfiguration configuration : mouseInputConfigurations) {
            if (configuration.getMouseSourceType() == sourceType) {
                res = configuration;
            }
        }

        return res;
    }

    public ControllerConfiguration getControllerConfiguration(int id) {
        ControllerConfiguration res = null;

        for (ControllerConfiguration configuration : controllerConfigurations) {
            if (configuration.getId() == id) {
                res = configuration;
            }
        }

        return res;
    }
}
