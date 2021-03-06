package io.github.movementspeed.nhglib.core.fsm.states.assets;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.utils.Array;
import io.github.movementspeed.nhglib.assets.Asset;
import io.github.movementspeed.nhglib.assets.Assets;
import io.github.movementspeed.nhglib.core.fsm.base.AssetsStates;
import io.github.movementspeed.nhglib.utils.data.Strings;
import io.github.movementspeed.nhglib.utils.debug.NhgLogger;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

/**
 * Created by Fausto Napoli on 08/12/2016.
 */
public class AssetStateLoading implements State<Assets> {
    @Override
    public void enter(Assets assets) {
        NhgLogger.log(this, "Asset manager is loading.");
    }

    @Override
    public void update(Assets assets) {
        if (assets.updateAssetManagers()) {
            assets.fsm.changeState(AssetsStates.IDLE);
            MessageManager.getInstance().dispatchMessage(AssetsStates.ASSETS_GC);

            assets.assetLoadingFinished();
            publishLoadedAssets(assets);
        }
    }

    @Override
    public void exit(Assets assets) {
        NhgLogger.log(this, "Asset manager has finished loading.");
    }

    @Override
    public boolean onMessage(Assets entity, Telegram telegram) {
        return false;
    }

    private void publishLoadedAssets(final Assets assets) {
        Array<Asset> assetsCopy = new Array<>(assets.getAssetQueue());

        Observable.fromIterable(assetsCopy)
                .filter(new Predicate<Asset>() {
                    @Override
                    public boolean test(Asset asset) {
                        return assets.isAssetLoaded(asset);
                    }
                })
                .subscribe(new Consumer<Asset>() {
                    @Override
                    public void accept(Asset asset) {
                        NhgLogger.log(
                                this,
                                Strings.Messages.assetLoaded,
                                asset.source);

                        assets.assetLoaded(asset);
                        assets.dequeueAsset(asset);
                    }
                });
    }
}
