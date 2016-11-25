package com.boxfishedu.workorder.dao.mongo;

import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mongo.InstantClassTimeRules;
import com.boxfishedu.workorder.entity.mongo.TimeLimitRules;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class InstantClassTimeRulesMorphiaRepository extends BaseMorphiaRepository<InstantClassTimeRules> {

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    public Optional<List<InstantClassTimeRules>> getByDate(Date date) {
        return this.getByDay(DateUtil.date2SimpleString(date));
    }

    public void delete(){

    }

    public Optional<List<InstantClassTimeRules>> getByDay(String day) {
        Query<InstantClassTimeRules> query = datastore.createQuery(InstantClassTimeRules.class);
        query.criteria("date").equal(day);
        return Optional.ofNullable(query.asList());
    }
}
