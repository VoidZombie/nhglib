package io.github.voidzombie.nhglib.runtime.fsm.states.engine;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import io.github.voidzombie.nhglib.runtime.entry.NhgEntry;
import io.github.voidzombie.nhglib.utils.debug.Logger;

/**
 * Created by Fausto Napoli on 08/12/2016.
 */
public class EngineStatePaused implements State<NhgEntry> {
    @Override
    public void enter(final NhgEntry nhgEntry) {
        Logger.log(this, "Engine is paused.");
        nhgEntry.enginePaused();
    }

    @Override
    public void update(NhgEntry nhgEntry) {

    }

    @Override
    public void exit(NhgEntry entity) {
    }

    @Override
    public boolean onMessage(NhgEntry entity, Telegram telegram) {
        return false;
    }
}