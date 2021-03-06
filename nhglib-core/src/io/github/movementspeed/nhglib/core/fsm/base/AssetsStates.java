package io.github.movementspeed.nhglib.core.fsm.base;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import io.github.movementspeed.nhglib.assets.Assets;
import io.github.movementspeed.nhglib.core.fsm.states.assets.AssetStateIdle;
import io.github.movementspeed.nhglib.core.fsm.states.assets.AssetStateLoading;

/**
 * Created by Fausto Napoli on 19/10/2016.
 */
public enum AssetsStates implements State<Assets> {
    IDLE(new AssetStateIdle()),
    LOADING(new AssetStateLoading());

    public static final int ASSETS_GC = 0;
    private State<Assets> state;

    AssetsStates(State<Assets> state) {
        this.state = state;
    }

    @Override
    public void enter(Assets assets) {
        state.enter(assets);
    }

    @Override
    public void update(Assets assets) {
        state.update(assets);
    }

    @Override
    public void exit(Assets assets) {
        state.exit(assets);
    }

    @Override
    public boolean onMessage(Assets assets, Telegram telegram) {
        return state.onMessage(assets, telegram);
    }
}
