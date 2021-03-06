package io.github.movementspeed.nhglib.graphics.scenes;

import com.artemis.ComponentMapper;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.utils.Array;
import io.github.movementspeed.nhglib.assets.Asset;
import io.github.movementspeed.nhglib.assets.AssetPackage;
import io.github.movementspeed.nhglib.assets.Assets;
import io.github.movementspeed.nhglib.core.ecs.components.graphics.ModelComponent;
import io.github.movementspeed.nhglib.core.ecs.components.graphics.ParticleEffectComponent;
import io.github.movementspeed.nhglib.core.ecs.components.physics.RigidBodyComponent;
import io.github.movementspeed.nhglib.core.ecs.components.physics.VehicleComponent;
import io.github.movementspeed.nhglib.core.ecs.components.physics.WheelComponent;
import io.github.movementspeed.nhglib.core.ecs.components.scenes.NodeComponent;
import io.github.movementspeed.nhglib.core.ecs.systems.impl.ParticleRenderingSystem;
import io.github.movementspeed.nhglib.core.ecs.systems.impl.PhysicsSystem;
import io.github.movementspeed.nhglib.core.ecs.utils.Entities;
import io.github.movementspeed.nhglib.core.messaging.Message;
import io.github.movementspeed.nhglib.core.messaging.Messaging;
import io.github.movementspeed.nhglib.data.models.serialization.components.*;
import io.github.movementspeed.nhglib.files.HDRData;
import io.github.movementspeed.nhglib.graphics.shaders.attributes.PBRTextureAttribute;
import io.github.movementspeed.nhglib.graphics.utils.PBRMaterial;
import io.github.movementspeed.nhglib.physics.models.BvhTriangleMeshRigidBodyShape;
import io.github.movementspeed.nhglib.physics.models.ConvexHullRigidBodyShape;
import io.github.movementspeed.nhglib.physics.models.ConvexTriangleMeshRigidBodyShape;
import io.github.movementspeed.nhglib.physics.models.RigidBodyShape;
import io.github.movementspeed.nhglib.utils.data.Bundle;
import io.github.movementspeed.nhglib.utils.data.Strings;
import io.github.movementspeed.nhglib.utils.scenes.SceneMappings;
import io.reactivex.functions.Consumer;

/**
 * Created by Fausto Napoli on 08/12/2016.
 */
public class SceneManager {
    private Scene currentScene;
    private Messaging messaging;
    private Entities entities;
    private Assets assets;

    private ComponentMapper<NodeComponent> nodeMapper;
    private ComponentMapper<ModelComponent> modelMapper;
    private ComponentMapper<RigidBodyComponent> rigidBodyMapper;
    private ComponentMapper<VehicleComponent> vehicleMapper;
    private ComponentMapper<WheelComponent> vehicleWheelMapper;
    private ComponentMapper<ParticleEffectComponent> particleEffectMapper;

    public SceneManager(Messaging messaging, Entities entities, Assets assets) {
        this.messaging = messaging;
        this.entities = entities;
        this.assets = assets;

        nodeMapper = entities.getMapper(NodeComponent.class);
        modelMapper = entities.getMapper(ModelComponent.class);
        rigidBodyMapper = entities.getMapper(RigidBodyComponent.class);
        vehicleMapper = entities.getMapper(VehicleComponent.class);
        vehicleWheelMapper = entities.getMapper(WheelComponent.class);
        particleEffectMapper = entities.getMapper(ParticleEffectComponent.class);

        SceneMappings.addComponentJsonMapping("message", MessageComponentJson.class);
        SceneMappings.addComponentJsonMapping("camera", CameraComponentJson.class);
        SceneMappings.addComponentJsonMapping("light", LightComponentJson.class);
        SceneMappings.addComponentJsonMapping("model", ModelComponentJson.class);
        SceneMappings.addComponentJsonMapping("rigidBody", RigidBodyComponentJson.class);
        SceneMappings.addComponentJsonMapping("vehicle", VehicleComponentJson.class);
        SceneMappings.addComponentJsonMapping("wheel", WheelComponentJson.class);
        SceneMappings.addComponentJsonMapping("particleEffect", ParticleEffectComponentJson.class);
        SceneMappings.addComponentJsonMapping("ui", UiComponentJson.class);

        SceneMappings.addAssetClassMapping("model", Model.class);
        SceneMappings.addAssetClassMapping("texture", Texture.class);
        SceneMappings.addAssetClassMapping("particleEffect", ParticleEffect.class);
        SceneMappings.addAssetClassMapping("hdr", HDRData.class);
        SceneMappings.addAssetClassMapping("textureAtlas", TextureAtlas.class);
        SceneMappings.addAssetClassMapping("bitmapFont", BitmapFont.class);
    }

    public void loadScene(final Scene scene) {
        currentScene = scene;
        processAssets(scene.assets);

        AssetPackage assetPackage = new AssetPackage(scene.name);
        assetPackage.addAssets(scene.assets);
        assets.queueAssetPackage(assetPackage);

        messaging.get(Strings.Events.assetPackageLoaded)
                .subscribe(new Consumer<Message>() {
                    @Override
                    public void accept(Message message) {
                        AssetPackage assetPackage = (AssetPackage) message.data.get(Strings.Defaults.assetPackageKey);

                        if (assetPackage.is(scene.name)) {
                            for (int entity : scene.sceneGraph.getEntities()) {
                                processEntityAssets(entity, true);
                            }

                            Bundle messageBundle = new Bundle();
                            messageBundle.put(Strings.Defaults.sceneKey, scene);
                            messaging.send(new Message(Strings.Events.sceneLoaded, messageBundle));
                        }
                    }
                });
    }

    public void unloadScene(final Scene scene) {
        for (int entity : scene.sceneGraph.getEntities()) {
            processEntityAssets(entity, false);
        }
    }

    public void reloadScene() {
        loadScene(currentScene);
    }

    public Scene getCurrentScene() {
        return currentScene;
    }

    private void processAssets(Array<Asset> assets) {
        for (Asset asset : assets) {
            if (asset.isType(ParticleEffect.class)) {
                ParticleRenderingSystem particleRenderingSystem = entities.getEntitySystem(ParticleRenderingSystem.class);
                asset.parameters = new ParticleEffectLoader.ParticleEffectLoadParameter(particleRenderingSystem.getParticleSystem().getBatches());
            }
        }
    }

    private void processEntityAssets(int entity, boolean load) {
        // Check if this entity has a model
        if (modelMapper.has(entity)) {
            processModelComponent(entity, load);
        }

        // Check if this entity has a rigid body
        if (rigidBodyMapper.has(entity)) {
            processRigidBodyComponent(entity, load);
        }

        // Check if this entity is a vehicle
        if (vehicleMapper.has(entity)) {
            processVehicleComponent(entity, load);
        }

        // Check if this entity is a wheel
        if (vehicleWheelMapper.has(entity)) {
            processWheelComponent(entity);
        }

        // Check if this entity has a particle effect
        if (particleEffectMapper.has(entity)) {
            processParticleEffectComponent(entity, load);
        }
    }

    private void processModelComponent(int entity, boolean load) {
        ModelComponent modelComponent = modelMapper.get(entity);
        NodeComponent nodeComponent = nodeMapper.get(entity);

        if (load) {
            Model model = assets.get(modelComponent.asset);
            modelComponent.buildWithModel(nodeComponent.node.scale, model);

            for (PBRMaterial pbrMaterial : modelComponent.pbrMaterials) {
                Texture.TextureFilter mag = Texture.TextureFilter.Linear;
                Texture.TextureFilter min = Texture.TextureFilter.Linear;
                Texture.TextureWrap wrapU = Texture.TextureWrap.Repeat;
                Texture.TextureWrap wrapV = Texture.TextureWrap.Repeat;

                if (pbrMaterial.albedo != null && !pbrMaterial.albedo.isEmpty()) {
                    Texture albedo = assets.get(pbrMaterial.albedo);

                    if (albedo != null) {
                        albedo.setFilter(mag, min);
                        albedo.setWrap(wrapU, wrapV);
                        pbrMaterial.set(PBRTextureAttribute.createAlbedo(albedo,
                                pbrMaterial.offsetU, pbrMaterial.offsetV, pbrMaterial.tilesU, pbrMaterial.tilesV));
                    }
                } else if (pbrMaterial.albedoColor != null) {
                    pbrMaterial.set(PBRTextureAttribute.createAlbedo(pbrMaterial.albedoColor));
                }

                if (pbrMaterial.normal != null && !pbrMaterial.normal.isEmpty()) {
                    Texture normal = assets.get(pbrMaterial.normal);

                    if (normal != null) {
                        normal.setFilter(mag, min);
                        normal.setWrap(wrapU, wrapV);
                        pbrMaterial.set(PBRTextureAttribute.createNormal(normal,
                                pbrMaterial.offsetU, pbrMaterial.offsetV, pbrMaterial.tilesU, pbrMaterial.tilesV));
                    }
                }

                if (pbrMaterial.rma != null && !pbrMaterial.rma.isEmpty()) {
                    Texture rma = assets.get(pbrMaterial.rma);

                    if (rma != null) {
                        rma.setFilter(mag, min);
                        rma.setWrap(wrapU, wrapV);
                        pbrMaterial.set(PBRTextureAttribute.createRMA(rma,
                                pbrMaterial.offsetU, pbrMaterial.offsetV, pbrMaterial.tilesU, pbrMaterial.tilesV));
                    }
                } else {
                    float roughness = 1, metalness = 0, ao = 1;

                    if (pbrMaterial.roughnessValue >= 0 && pbrMaterial.roughnessValue <= 1) {
                        roughness = pbrMaterial.roughnessValue;
                    }

                    if (pbrMaterial.metalnessValue >= 0 && pbrMaterial.metalnessValue <= 1) {
                        metalness = pbrMaterial.metalnessValue;
                    }

                    if (pbrMaterial.aoValue >= 0 && pbrMaterial.aoValue <= 1) {
                        ao = pbrMaterial.aoValue;
                    }

                    pbrMaterial.set(PBRTextureAttribute.createRMA(roughness, metalness, ao));
                }

                if (pbrMaterial.blended) {
                    pbrMaterial.set(new BlendingAttribute(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA));
                }

                // Clear other attributes in model's own materials
                /*for (Material m : modelComponent.model.materials) {
                    m.clear();
                }*/

                if (pbrMaterial.targetNode != null && !pbrMaterial.targetNode.isEmpty()) {
                    modelComponent.setPBRMaterial(pbrMaterial.targetNode, pbrMaterial);
                } else {
                    modelComponent.setPBRMaterial(pbrMaterial);
                }
            }
        } else {
            assets.unloadAsset(modelComponent.asset);

            for (PBRMaterial mat : modelComponent.pbrMaterials) {
                assets.unloadAsset(mat.albedo);
                assets.unloadAsset(mat.normal);
                assets.unloadAsset(mat.rma);
            }
        }
    }

    private void processRigidBodyComponent(int entity, boolean load) {
        RigidBodyComponent rigidBodyComponent = rigidBodyMapper.get(entity);

        if (load) {
            rigidBodyComponent.build(assets);
            rigidBodyComponent.state = RigidBodyComponent.State.READY;
        } else {
            RigidBodyShape rigidBodyShape = rigidBodyComponent.rigidBodyShape;

            switch (rigidBodyShape.type) {
                case CONVEX_TRIANGLE_MESH:
                    ConvexTriangleMeshRigidBodyShape convexTriangleMeshRigidBodyShape = (ConvexTriangleMeshRigidBodyShape) rigidBodyComponent.rigidBodyShape;
                    assets.unloadAsset(convexTriangleMeshRigidBodyShape.asset);
                    break;

                case BVH_TRIANGLE_MESH:
                    BvhTriangleMeshRigidBodyShape bvhTriangleMeshRigidBodyShape = (BvhTriangleMeshRigidBodyShape) rigidBodyComponent.rigidBodyShape;
                    assets.unloadAsset(bvhTriangleMeshRigidBodyShape.asset);
                    break;

                case CONVEX_HULL:
                    ConvexHullRigidBodyShape convexHullRigidBodyShape = (ConvexHullRigidBodyShape) rigidBodyComponent.rigidBodyShape;
                    assets.unloadAsset(convexHullRigidBodyShape.asset);
                    break;
            }
        }
    }

    private void processVehicleComponent(int entity, boolean load) {
        VehicleComponent vehicleComponent = vehicleMapper.get(entity);

        if (load) {
            PhysicsSystem physicsSystem = entities.getEntitySystem(PhysicsSystem.class);
            vehicleComponent.build(assets, physicsSystem.getBulletWorld());
            vehicleComponent.state = RigidBodyComponent.State.READY;
        } else {
            RigidBodyShape rigidBodyShape = vehicleComponent.rigidBodyShape;

            switch (rigidBodyShape.type) {
                case CONVEX_TRIANGLE_MESH:
                    ConvexTriangleMeshRigidBodyShape convexTriangleMeshRigidBodyShape = (ConvexTriangleMeshRigidBodyShape) vehicleComponent.rigidBodyShape;
                    assets.unloadAsset(convexTriangleMeshRigidBodyShape.asset);
                    break;

                case BVH_TRIANGLE_MESH:
                    BvhTriangleMeshRigidBodyShape bvhTriangleMeshRigidBodyShape = (BvhTriangleMeshRigidBodyShape) vehicleComponent.rigidBodyShape;
                    assets.unloadAsset(bvhTriangleMeshRigidBodyShape.asset);
                    break;

                case CONVEX_HULL:
                    ConvexHullRigidBodyShape convexHullRigidBodyShape = (ConvexHullRigidBodyShape) vehicleComponent.rigidBodyShape;
                    assets.unloadAsset(convexHullRigidBodyShape.asset);
                    break;
            }
        }
    }

    private void processWheelComponent(int entity) {
        WheelComponent wheelComponent = vehicleWheelMapper.get(entity);
        wheelComponent.build();
        wheelComponent.state = WheelComponent.State.READY;
    }

    private void processParticleEffectComponent(int entity, boolean load) {
        ParticleEffectComponent particleEffectComponent = particleEffectMapper.get(entity);

        if (load) {
            ParticleRenderingSystem particleRenderingSystem = entities.getEntitySystem(ParticleRenderingSystem.class);
            particleEffectComponent.build(assets, particleRenderingSystem.getParticleEffectProvider());
            particleEffectComponent.state = ParticleEffectComponent.State.READY;
        } else {
            assets.unloadAsset(particleEffectComponent.asset);
        }
    }
}
