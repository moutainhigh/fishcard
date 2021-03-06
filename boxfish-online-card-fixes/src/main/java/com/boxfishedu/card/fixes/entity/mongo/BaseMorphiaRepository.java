package com.boxfishedu.card.fixes.entity.mongo;

import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by hucl on 16/7/18.
 */
public class BaseMorphiaRepository<T> {
    @Autowired
    protected Datastore datastore;

    public void save(T t) {
        datastore.save(t);
    }

    public void save(Iterable<T> ts){
        datastore.save(ts);
    }

}
