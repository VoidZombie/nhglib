package io.github.movementspeed.nhglib.core.entry;

import com.artemis.BaseSystem;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.utils.Array;
import io.github.movementspeed.nhglib.Nhg;
import io.github.movementspeed.nhglib.core.fsm.base.EngineStates;
import io.github.movementspeed.nhglib.core.fsm.interfaces.EngineConfigurationListener;
import io.github.movementspeed.nhglib.core.fsm.interfaces.EngineStateListener;
import io.github.movementspeed.nhglib.core.messaging.Message;
import io.github.movementspeed.nhglib.utils.data.Strings;

/**
 * Created by Fausto Napoli on 02/11/2016.
 */
abstract class BaseGame implements
        ApplicationListener,
        EngineStateListener,
        EngineConfigurationListener {

    public Nhg nhg;

    private int width, height;
    private DefaultStateMachine<NhgEntry, EngineStates> fsm;

    @Override
    public final void create() {
        nhg = new Nhg();
        nhg.init();
    }

    @Override
    public final void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public final void render() {
        fsm.update();
    }

    @Override
    public final void pause() {
        fsm.changeState(EngineStates.PAUSED);
    }

    @Override
    public final void resume() {
        fsm.changeState(EngineStates.RUNNING);
    }

    @Override
    public final void dispose() {
        nhg.messaging.send(new Message(Strings.Events.engineDestroy));
        onDispose();
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onInitialized() {
        onResize(width, height);
    }

    @Override
    public void onUpdate(float delta) {
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onClose() {
    }

    @Override
    public void onResize(int width, int height) {

    }

    @Override
    public void onDispose() {

    }

    @Override
    public Array<BaseSystem> onConfigureEntitySystems() {
        return new Array<>();
    }

    void init(NhgEntry nhgEntry) {
        fsm = new DefaultStateMachine<>(nhgEntry, EngineStates.START);
    }

    public final StateMachine<NhgEntry, EngineStates> getFsm() {
        return fsm;
    }
}
