package com.boxfishedu.workorder.entity.mongo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * Created by hucl on 16/8/23.
 */
@Data
@Entity(noClassnameStored = true)
public class TimeLimitRules {
    @Id
    @JsonIgnore
    private ObjectId id;
    //套餐类型:中教,外教,金币换套餐等只需要其中之一即可
    private String comboType;

    //mutex(互斥,只能多选一) range:范围限制
    private String rule;

    //开始时间
    private String from;

    //结束时间
    private String to;

    private Integer day;

    //可选时间段的早限制
    private String limitMini;

    //可选时间段的最晚限制
    private String limitMax;


    @Override
    public String toString(){
        return this.id + "#" + this.comboType;
    }

    public static String getCacheKey(String comboType,String rule,Integer day){
        return comboType+rule+day;
    }
}
