package io.github.voidzombie.nhglib.data.models.serialization;

import com.badlogic.gdx.utils.JsonValue;
import io.github.voidzombie.nhglib.graphics.scenes.SceneGraph;
import io.github.voidzombie.nhglib.interfaces.JsonParseable;
import io.github.voidzombie.nhglib.runtime.ecs.components.scenes.NodeComponent;
import io.github.voidzombie.nhglib.runtime.ecs.utils.Entities;
import io.github.voidzombie.nhglib.utils.scenes.SceneUtils;

/**
 * Created by Fausto Napoli on 19/12/2016.
 */
public class EntityJson implements JsonParseable<Integer> {
    private Integer parentEntity;
    private Integer output;

    private Entities entities;
    private SceneGraph sceneGraphRef;

    public EntityJson(Entities entities) {
        this.entities = entities;
    }

    @Override
    public void parse(JsonValue jsonValue) {
        String id = jsonValue.getString("id");
        int entity = sceneGraphRef.addSceneEntity(id, parentEntity);

        JsonValue componentsJson = jsonValue.get("components");

        for (JsonValue componentJsonValue : componentsJson) {
            String type = componentJsonValue.getString("type");
            ComponentJson componentJson = SceneUtils.componentJsonFromType(type);

            if (componentJson != null) {
                componentJson.entity = entity;
                componentJson.entities = entities;
                componentJson.parse(componentJsonValue);
            }
        }

        JsonValue entitiesJson = jsonValue.get("entities");

        if (entitiesJson != null) {
            for (JsonValue entityJsonValue : entitiesJson) {
                EntityJson entityJson = new EntityJson(entities);
                entityJson.sceneGraphRef = sceneGraphRef;
                entityJson.parentEntity = entity;
                entityJson.parse(entityJsonValue);
            }
        }

        TransformJson transformJson = new TransformJson();
        transformJson.parse(jsonValue.get("transform"));

        NodeComponent nodeComponent = entities.getComponent(entity, NodeComponent.class);

        nodeComponent.setTranslation(transformJson.position);
        nodeComponent.setRotation(transformJson.rotation);
        nodeComponent.setScale(transformJson.scale);

        output = entity;
    }

    public void setParentEntity(Integer parentEntity) {
        this.parentEntity = parentEntity;
    }

    public void setSceneGraph(SceneGraph sceneGraph) {
        this.sceneGraphRef = sceneGraph;
    }

    @Override
    public Integer get() {
        return output;
    }
}
