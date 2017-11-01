package io.github.movementspeed.nhglib.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.UBJsonReader;
import io.github.movementspeed.nhglib.Nhg;
import io.github.movementspeed.nhglib.assets.loaders.*;
import io.github.movementspeed.nhglib.core.fsm.base.AssetsStates;
import io.github.movementspeed.nhglib.core.messaging.Message;
import io.github.movementspeed.nhglib.files.HDRData;
import io.github.movementspeed.nhglib.graphics.scenes.Scene;
import io.github.movementspeed.nhglib.input.handler.InputProxy;
import io.github.movementspeed.nhglib.interfaces.Updatable;
import io.github.movementspeed.nhglib.utils.data.Bundle;
import io.github.movementspeed.nhglib.utils.data.Strings;
import io.github.movementspeed.nhglib.utils.debug.NhgLogger;
import io.reactivex.functions.Consumer;

/**
 * Created by Fausto Napoli on 19/10/2016.
 */
public class Assets implements Updatable, AssetErrorListener {
    public DefaultStateMachine<Assets, AssetsStates> fsm;
    public AssetManager assetManager;
    public AssetManager syncAssetManager;

    private Nhg nhg;

    private Array<Asset> assetQueue;
    private ArrayMap<String, Asset> assetCache;

    public void init(Nhg nhg) {
        this.nhg = nhg;
        fsm = new DefaultStateMachine<>(this, AssetsStates.IDLE);

        assetManager = new AssetManager();
        syncAssetManager = new AssetManager();

        FileHandleResolver resolver = assetManager.getFileHandleResolver();
        FileHandleResolver syncResolver = syncAssetManager.getFileHandleResolver();

        assetManager.setLoader(Scene.class, new SceneLoader(nhg, resolver));
        assetManager.setLoader(InputProxy.class, new InputLoader(resolver));
        assetManager.setLoader(JsonValue.class, new JsonLoader(resolver));
        assetManager.setLoader(HDRData.class, new HDRLoader(resolver));
        assetManager.setLoader(Model.class, ".g3db", new NhgG3dModelLoader(this,
                new UBJsonReader(), resolver));

        syncAssetManager.setLoader(Scene.class, new SceneLoader(nhg, syncResolver));
        syncAssetManager.setLoader(InputProxy.class, new InputLoader(syncResolver));
        syncAssetManager.setLoader(JsonValue.class, new JsonLoader(syncResolver));
        syncAssetManager.setLoader(HDRData.class, new HDRLoader(syncResolver));
        syncAssetManager.setLoader(Model.class, ".g3db", new NhgG3dModelLoader(this,
                new UBJsonReader(), syncResolver));

        assetManager.setErrorListener(this);
        syncAssetManager.setErrorListener(this);

        assetQueue = new Array<>();
        assetCache = new ArrayMap<>();

        Texture.setAssetManager(assetManager);
    }

    // Updatable
    @Override
    public void update() {
        fsm.update();
    }

    // AssetErrorListener
    @Override
    public void error(AssetDescriptor asset, Throwable throwable) {
        try {
            throw throwable;
        } catch (Throwable throwable1) {
            throwable1.printStackTrace();
        }
    }

    public void assetLoadingFinished() {
        nhg.messaging.send(new Message(Strings.Events.assetLoadingFinished));
    }

    public void assetLoaded(Asset asset) {
        Bundle bundle = new Bundle();
        bundle.put(Strings.Defaults.assetKey, asset);

        nhg.messaging.send(new Message(Strings.Events.assetLoaded, bundle));
    }

    public void assetUnloaded(Asset asset) {
        Bundle bundle = new Bundle();
        bundle.put(Strings.Defaults.assetKey, asset);

        nhg.messaging.send(new Message(Strings.Events.assetUnloaded, bundle));
    }

    public Array<Asset> getAssetQueue() {
        return assetQueue;
    }

    public <T> T get(String alias) {
        return get(assetCache.get(alias));
    }

    public <T> T get(Asset asset) {
        T t = null;

        if (assetManager.isLoaded(asset.source)) {
            t = assetManager.get(asset.source);
        }

        return t;
    }

    public Asset getAsset(String alias) {
        return assetCache.get(alias);
    }

    public ArrayMap.Values<Asset> getCachedAssets() {
        return assetCache.values();
    }

    /**
     * Loads an asset with a direct callback.
     *
     * @param asset    the asset.
     * @param listener a listener for the asset loading.
     */
    public void loadAsset(final Asset asset, final AssetListener listener) {
        queueAsset(asset);

        nhg.messaging.get(Strings.Events.assetLoaded)
                .subscribe(new Consumer<Message>() {
                    @Override
                    public void accept(Message message) throws Exception {
                        Asset loadedAsset = (Asset) message.data.get(Strings.Defaults.assetKey);

                        if (loadedAsset.is(asset)) {
                            listener.onAssetLoaded(asset);
                        }
                    }
                });
    }

    /**
     * Loads an asset in an asynchronous way.
     *
     * @param asset the asset.
     */
    @SuppressWarnings("unchecked")
    public void queueAsset(Asset asset) {
        assetCache.put(asset.alias, asset);

        if (!assetManager.isLoaded(asset.source)) {
            FileHandle fileHandle = Gdx.files.internal(asset.source);

            if (fileHandle.exists()) {
                if (asset.parameters == null) {
                    assetManager.load(asset.source, asset.assetClass);
                } else {
                    assetManager.load(asset.source, asset.assetClass, asset.parameters);
                }

                assetQueue.add(asset);
            } else {
                NhgLogger.log(this, Strings.Messages.cannotQueueAssetFileNotFound, asset.source);
            }
        } else {
            assetLoaded(asset);
        }
    }

    public void queueAssets(Array<Asset> assets) {
        if (assets != null) {
            for (Asset asset : assets) {
                queueAsset(asset);
            }
        }
    }

    public <T> T loadAssetSync(Asset asset) {
        T t = null;
        assetCache.put(asset.alias, asset);

        if (!syncAssetManager.isLoaded(asset.source)) {
            FileHandle fileHandle = Gdx.files.internal(asset.source);

            if (fileHandle.exists()) {
                if (asset.parameters == null) {
                    syncAssetManager.load(asset.source, asset.assetClass);
                } else {
                    syncAssetManager.load(asset.source, asset.assetClass, asset.parameters);
                }

                syncAssetManager.finishLoading();
                t = syncAssetManager.get(asset.source);
            } else {
                NhgLogger.log(this, Strings.Messages.cannotQueueAssetFileNotFound, asset.source);
            }
        } else {
            t = syncAssetManager.get(asset.source);
        }

        return t;
    }

    public void dequeueAsset(Asset asset) {
        assetQueue.removeValue(asset, true);
    }

    public void clear() {
        assetManager.clear();
    }

    public void unloadAsset(String alias) {
        unloadAsset(getAsset(alias));
    }

    public void unloadAsset(Asset asset) {
        if (asset != null) {
            if (assetManager.isLoaded(asset.source)) {
                assetManager.unload(asset.source);
                assetUnloaded(asset);
            }
        }
    }

    public void clearCompleted() {
        for (int i = 0; i < assetQueue.size; i++) {
            Asset asset = assetQueue.get(i);

            if (assetManager.isLoaded(asset.source)) {
                assetQueue.removeValue(asset, true);
            }
        }
    }

    public void clearQueue() {
        assetQueue.clear();
    }

    public void dispose() {
        if (assetManager != null) {
            assetManager.dispose();
            assetManager = null;
        }
    }

    public boolean assetInQueue(Asset asset) {
        return assetQueue.contains(asset, true);
    }

    public interface AssetListener {
        void onAssetLoaded(Asset asset);
    }
}
