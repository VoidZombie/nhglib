package io.github.voidzombie.nhglib.runtime.ecs.components;

import com.artemis.PooledComponent;
import com.badlogic.gdx.utils.Array;
import io.github.voidzombie.nhglib.NHG;
import io.github.voidzombie.nhglib.runtime.messaging.Message;
import io.reactivex.Observable;

/**
 * Created by Fausto Napoli on 08/12/2016.
 */
public class MessageComponent extends PooledComponent {
    private Array<Message> messages;
    private Array<String> filters;

    public MessageComponent() {
        messages = new Array<>();
        filters = new Array<>();
    }

    @Override
    protected void reset() {
        messages.clear();
        filters.clear();
    }

    public void subscribe(String ... filters) {
        NHG.messaging.subscribe((message -> messages.add(message)));
        this.filters.addAll(filters);
    }

    public Observable<Message> getMessages() {
        return Observable.fromIterable(messages).filter((message -> {
            boolean res = false;

            for (String filter : filters) {
                if (message.is(filter)) {
                    res = true;
                    break;
                }
            }

            return res;
        })).doFinally(() -> messages.clear());
    }
}
