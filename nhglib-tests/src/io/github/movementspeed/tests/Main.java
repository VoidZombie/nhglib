package io.github.movementspeed.tests;

import com.artemis.BaseSystem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import io.github.movementspeed.nhglib.Nhg;
import io.github.movementspeed.nhglib.assets.Asset;
import io.github.movementspeed.nhglib.core.ecs.components.graphics.CameraComponent;
import io.github.movementspeed.nhglib.core.ecs.components.graphics.LightComponent;
import io.github.movementspeed.nhglib.core.ecs.components.scenes.NodeComponent;
import io.github.movementspeed.nhglib.core.ecs.systems.impl.InputSystem;
import io.github.movementspeed.nhglib.core.ecs.systems.impl.PhysicsSystem;
import io.github.movementspeed.nhglib.core.ecs.systems.impl.RenderingSystem;
import io.github.movementspeed.nhglib.core.entry.NhgEntry;
import io.github.movementspeed.nhglib.core.messaging.Message;
import io.github.movementspeed.nhglib.files.HDRData;
import io.github.movementspeed.nhglib.graphics.lights.LightProbe;
import io.github.movementspeed.nhglib.graphics.lights.NhgLight;
import io.github.movementspeed.nhglib.graphics.lights.NhgLightsAttribute;
import io.github.movementspeed.nhglib.graphics.scenes.Scene;
import io.github.movementspeed.nhglib.graphics.shaders.attributes.AmbientLightingAttribute;
import io.github.movementspeed.nhglib.graphics.shaders.attributes.GammaCorrectionAttribute;
import io.github.movementspeed.nhglib.graphics.shaders.attributes.IBLAttribute;
import io.github.movementspeed.nhglib.graphics.shaders.attributes.ShadowSystemAttribute;
import io.github.movementspeed.nhglib.graphics.shaders.particles.ParticleShader;
import io.github.movementspeed.nhglib.graphics.shaders.shadows.system.ShadowSystem;
import io.github.movementspeed.nhglib.graphics.shaders.shadows.system.classical.ClassicalShadowSystem;
import io.github.movementspeed.nhglib.graphics.shaders.shadows.system.classical.ClassicalShadowsRenderPass;
import io.github.movementspeed.nhglib.graphics.shaders.tiled.TiledPBRRenderPass;
import io.github.movementspeed.nhglib.graphics.worlds.NhgWorld;
import io.github.movementspeed.nhglib.graphics.worlds.strategies.impl.DefaultWorldStrategy;
import io.github.movementspeed.nhglib.input.enums.InputAction;
import io.github.movementspeed.nhglib.input.interfaces.InputListener;
import io.github.movementspeed.nhglib.input.models.base.NhgInput;
import io.github.movementspeed.nhglib.utils.data.Bounds;
import io.github.movementspeed.nhglib.utils.data.Strings;
import io.github.movementspeed.nhglib.utils.debug.NhgLogger;
import io.github.movementspeed.tests.systems.TestNodeSystem;
import io.reactivex.functions.Consumer;

/**
 * Created by Fausto Napoli on 26/10/2016.
 */
public class Main extends NhgEntry implements InputListener {
    private Scene scene;
    private NhgWorld world;
    private NodeComponent cameraNode;
    private CameraComponent cameraComponent;
    private RenderingSystem renderingSystem;
    private Environment environment;

    private NodeComponent target;
    private Vector3 targetVec;

    private LightProbe lightProbe;

    @Override
    public void onStart() {
        super.onStart();
        Nhg.debugLogs = true;
        Nhg.debugFpsLogs = true;
        Nhg.debugDrawPhysics = false;
        ParticleShader.softParticles = false;
        targetVec = new Vector3(0, 0.5f, 0);
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        world = new NhgWorld(nhg.messaging, nhg.entities, nhg.assets,
                new DefaultWorldStrategy(),
                new Bounds(2f, 2f, 2f));

        nhg.assets.queueAsset(new Asset("scene", "scenes/fel_lord_scene.json", Scene.class));

        InputSystem inputSystem = nhg.entities.getEntitySystem(InputSystem.class);
        inputSystem.loadMapping("input/input.json");
        inputSystem.setEnableContext("game", true);
        inputSystem.setEnableContext("menu", true);
        inputSystem.addInputListener(this);

        PhysicsSystem physicsSystem = nhg.entities.getEntitySystem(PhysicsSystem.class);
        physicsSystem.setGravity(new Vector3(0, -0.5f, 0));

        renderingSystem = nhg.entities.getEntitySystem(RenderingSystem.class);
        renderingSystem.setClearColor(Color.GRAY);

        environment = renderingSystem.getEnvironment();

        NhgLightsAttribute lightsAttribute = new NhgLightsAttribute();

        GammaCorrectionAttribute gammaCorrectionAttribute = new GammaCorrectionAttribute(true);
        AmbientLightingAttribute ambientLightingAttribute = new AmbientLightingAttribute(0.03f);

        ShadowSystem shadowSystem = new ClassicalShadowSystem();
        shadowSystem.init();

        ShadowSystemAttribute shadowSystemAttribute = new ShadowSystemAttribute(shadowSystem);

        environment.set(lightsAttribute);
        environment.set(gammaCorrectionAttribute);
        environment.set(ambientLightingAttribute);
        environment.set(shadowSystemAttribute);

        renderingSystem.setRenderPass(0, new ClassicalShadowsRenderPass());
        renderingSystem.setRenderPass(1, new TiledPBRRenderPass());

        // Subscribe to asset events
        nhg.messaging.get(Strings.Events.assetLoaded, Strings.Events.assetLoadingFinished, Strings.Events.sceneLoaded)
                .subscribe(new Consumer<Message>() {
                    @Override
                    public void accept(Message message) throws Exception {
                        if (message.is(Strings.Events.assetLoaded)) {
                            Asset asset = (Asset) message.data.get(Strings.Defaults.assetKey);

                            if (asset.is("scene")) {
                                scene = nhg.assets.get(asset);

                                world.loadScene(scene);
                                world.setReferenceEntity("camera");

                                Integer cameraEntity = scene.sceneGraph.getSceneEntity("camera");
                                cameraNode = nhg.entities.getComponent(
                                        cameraEntity, NodeComponent.class);

                                cameraComponent = nhg.entities.getComponent(cameraEntity, CameraComponent.class);

                                int targetEntity = scene.sceneGraph.getSceneEntity("target");
                                target = nhg.entities.getComponent(targetEntity, NodeComponent.class);
                            }
                        } else if (message.is(Strings.Events.sceneLoaded)) {
                            NhgLogger.log(this, "Scene loaded");
                            HDRData data = nhg.assets.get("newport_loft");

                            lightProbe = new LightProbe();
                            lightProbe.build(data,
                                    128f, 128f,
                                    32f, 32f,
                                    64f, 64f,
                                    128f, 128f);

                            IBLAttribute irradianceAttribute = IBLAttribute.createIrradiance(lightProbe.getIrradiance());
                            IBLAttribute prefilterAttribute = IBLAttribute.createPrefilter(lightProbe.getPrefilter());
                            IBLAttribute brdfAttribute = IBLAttribute.createBrdf(lightProbe.getBrdf());

                            environment.set(irradianceAttribute);
                            environment.set(prefilterAttribute);
                            environment.set(brdfAttribute);

                            for (int i = 0; i < 3; i++) {
                                int light = scene.sceneGraph.getSceneEntity("l" + i);
                                LightComponent lightComponent = nhg.entities.getComponent(light, LightComponent.class);
                                NhgLight nhgLight = lightComponent.light;
                                shadowSystem.addLight(nhgLight);
                            }
                        }
                    }
                });
    }

    @Override
    public void onUpdate(float delta) {
        super.onUpdate(delta);
        world.update();
        if (target != null) target.rotate(targetVec, true);
    }

    @Override
    public void onResize(int width, int height) {
        super.onResize(width, height);
    }

    @Override
    public void onDispose() {
        super.onDispose();
    }

    @Override
    public Array<BaseSystem> onConfigureEntitySystems() {
        Array<BaseSystem> systems = new Array<>();
        systems.add(new TestNodeSystem());

        return systems;
    }

    @Override
    public void onInput(NhgInput input) {
        InputAction action = input.getAction();

        if (action != null && action == InputAction.DOWN) {
            NhgLogger.log(this, "You pressed %s", input.getName());

            if (input.is("exit")) {
                nhg.messaging.send(new Message(Strings.Events.engineDestroy));
            }
        }
    }
}