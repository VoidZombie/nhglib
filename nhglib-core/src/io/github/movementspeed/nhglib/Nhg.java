package io.github.movementspeed.nhglib;

import io.github.movementspeed.nhglib.assets.Assets;
import io.github.movementspeed.nhglib.core.ecs.utils.Entities;
import io.github.movementspeed.nhglib.core.messaging.Messaging;
import io.github.movementspeed.nhglib.core.threading.Threading;
import io.github.movementspeed.nhglib.enums.OpenGLVersion;

/**
 * Created by Fausto Napoli on 17/10/2016.
 * Entry point for Nhg, where various parts of the engine will be exposed.
 */
public class Nhg {
    public Assets assets;
    public Messaging messaging;
    public Threading threading;
    public Entities entities;

    public static boolean debugLogs = false;
    public static boolean debugDrawPhysics = false;
    public static boolean debugFpsLogs = false;
    public static OpenGLVersion glVersion = OpenGLVersion.VERSION_3;

    public Nhg() {
        messaging = new Messaging();
        entities = new Entities();
        assets = new Assets();
        threading = new Threading();
    }

    public void init() {
        assets.init(this);
    }
}