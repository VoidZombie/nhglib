package io.github.voidzombie.nhglib.interfaces;

import com.artemis.WorldConfigurationBuilder;

/**
 * Created by Fausto Napoli on 02/11/2016.
 */
public interface EngineConfigurationListener {
    void onConfigureEntitySystems(WorldConfigurationBuilder configurationBuilder);
}