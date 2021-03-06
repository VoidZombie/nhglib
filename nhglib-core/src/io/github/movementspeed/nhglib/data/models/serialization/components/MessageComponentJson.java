package io.github.movementspeed.nhglib.data.models.serialization.components;

import com.badlogic.gdx.utils.JsonValue;
import io.github.movementspeed.nhglib.core.ecs.components.common.MessageComponent;
import io.github.movementspeed.nhglib.data.models.serialization.ComponentJson;

/**
 * Created by Fausto Napoli on 19/12/2016.
 */
public class MessageComponentJson extends ComponentJson {
    @Override
    public void parse(JsonValue jsonValue) {
        MessageComponent messageComponent =
                nhg.entities.createComponent(entity, MessageComponent.class);

        JsonValue filters = jsonValue.get("filters");
        messageComponent.subscribe(filters.asStringArray());

        output = messageComponent;
    }
}
