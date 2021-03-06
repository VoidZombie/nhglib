package io.github.movementspeed.nhglib.graphics.scenes;

import com.artemis.Archetype;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import io.github.movementspeed.nhglib.Nhg;
import io.github.movementspeed.nhglib.core.ecs.components.scenes.NodeComponent;

/**
 * Created by Fausto Napoli on 08/12/2016.
 */
public class SceneGraph {
    private int rootEntity;

    private Nhg nhg;
    private Archetype sceneEntityArchetype;
    private NodeComponent rootNodeComponent;

    private Array<Integer> entitiesArray;
    private ArrayMap<String, Integer> entityIds;

    @SuppressWarnings("unchecked")
    public SceneGraph(Nhg nhg, String rootId) {
        this.nhg = nhg;

        this.entitiesArray = new Array<>();
        entityIds = new ArrayMap<>();
        sceneEntityArchetype = nhg.entities.createArchetype(NodeComponent.class);

        rootEntity = createSceneEntity(rootId);
        rootNodeComponent = nhg.entities.getComponent(rootEntity, NodeComponent.class);
        rootNodeComponent.setId(rootEntity);

        this.entitiesArray.add(rootEntity);
    }

    public int getRootEntity() {
        return rootEntity;
    }

    public int createSceneEntity(String id) {
        int entity = nhg.entities.createEntity(sceneEntityArchetype);
        entityIds.put(id, entity);

        return entity;
    }

    public int addSceneEntity(int entity, int parentEntity) {
        NodeComponent nodeComponent = nhg.entities
                .getComponent(entity, NodeComponent.class);
        nodeComponent.setId(entity);

        NodeComponent parentNodeComponent = nhg.entities
                .getComponent(parentEntity, NodeComponent.class);

        parentNodeComponent.node.addChild(nodeComponent.node);
        nodeComponent.parentNodeComponent = parentNodeComponent;

        entitiesArray.add(entity);
        return entity;
    }

    public int addSceneEntity(int entity) {
        return addSceneEntity(entity, rootEntity);
    }

    public int addSceneEntity(String id) {
        return addSceneEntity(id, rootEntity);
    }

    public int addSceneEntity(String id, int parentEntity) {
        int entity = createSceneEntity(id);
        return addSceneEntity(entity, parentEntity);
    }

    public int getSceneEntity(String id) {
        return entityIds.get(id);
    }

    public Array<Integer> getEntities() {
        return entitiesArray;
    }
}
