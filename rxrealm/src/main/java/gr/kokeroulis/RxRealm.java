/*
 * Copyright (C) 2015 Antonis Tsiapaliokas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gr.kokeroulis;

import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public final class RxRealm {
    private static final Scheduler scheduler = Schedulers.from(Executors.newSingleThreadExecutor());

    private RxRealm() {
        // No instances
    }

    public static <E extends RealmObject, R> Observable<R>
        query(final Query<E> query, final Mapper<E,R> mapper) {
        return Observable.create(new Observable.OnSubscribe<R>() {
            @Override
            public void call(final Subscriber<? super R> subscriber) {
                if (subscriber.isUnsubscribed()) {
                    return;
                }

                Realm realm = Realm.getDefaultInstance();

                RealmResults<E> results = query.call(realm).findAll();
                for (E data : results) {
                    subscriber.onNext(mapper.call(data));
                }
                realm.close();
                subscriber.onCompleted();
            }
        })
        .subscribeOn(scheduler)
        .observeOn(AndroidSchedulers.mainThread());
    }

    public static Observable<Void> transaction(final Realm.Transaction transaction) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                if (subscriber.isUnsubscribed()) {
                    return;
                }

                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(transaction);
                realm.close();
                subscriber.onCompleted();
            }
        })
        .subscribeOn(scheduler)
        .observeOn(AndroidSchedulers.mainThread());
    }
}