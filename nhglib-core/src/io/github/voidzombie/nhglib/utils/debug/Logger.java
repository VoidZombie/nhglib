package io.github.voidzombie.nhglib.utils.debug;

import com.badlogic.gdx.Gdx;
import io.github.voidzombie.nhglib.NHG;

/**
 * Created by Fausto Napoli on 19/10/2016.
 */
public class Logger {
    public void log(Object caller, String message) {
        if (NHG.debugLogs) {
            Gdx.app.log(getCallerString(caller), message);
        }
    }

    public void log(Object caller, String message, Object... objects) {
        if (NHG.debugLogs) {
            String formattedMessage = String.format(message, objects);
            log(caller, formattedMessage);
        }
    }

    private String getCallerString(Object caller) {
        String callerString = caller.getClass().getName();
        Integer lastIndexOfDot = callerString.lastIndexOf(".");

        return callerString.substring(lastIndexOfDot + 1, callerString.length());
    }
}
