package com.boxfishedu.card.fixes.service;

import com.boxfishedu.card.fixes.entity.jpa.BaseTimeSlotJpaRepository;
import com.boxfishedu.card.fixes.entity.jpa.BaseTimeSlots;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by LuoLiBing on 16/11/23.
 */
@Service
public class BaseTimeSlotService {

    @Autowired
    private BaseTimeSlotJpaRepository baseTimeSlotJpaRepository;

    public void initBaseTimeSlots(int days) {
        Date date = baseTimeSlotJpaRepository.findMaxDate();
        if(date == null) {
            date = new Date();
        }
        LocalDate localDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
        for(int i = 1; i <= days; i++) {
            List<BaseTimeSlots> list = findByDate(localDate.plusDays(i), 0);
            baseTimeSlotJpaRepository.save(list);

            list = findByDate(localDate.plusDays(i), 1);
            baseTimeSlotJpaRepository.save(list);
        }
    }

    private List<BaseTimeSlots> findByDate(LocalDate localDate, int teachingType) {
        List<BaseTimeSlots> result = new ArrayList<>();
        int from, to;
        if(localDate.getDayOfWeek().getValue() > 5) {
            from = 1; to = 34;
        } else {
            from = 25; to = 34;
        }
        for(int i = from; i <=to; i++) {
            result.add(createBaseTimeSlots(i, localDate, teachingType));
        }
        return result;
    }


    private BaseTimeSlots createBaseTimeSlots(int slotId, LocalDate localDate, int teachingType) {
        BaseTimeSlots t1 = new BaseTimeSlots();
        t1.setClassDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        t1.setSlotId(slotId);
        t1.initTime();
        t1.setProbability(100);
        t1.setTeachingType(teachingType);
        t1.setClientType(0);
        return t1;
    }

    public static void main(String[] args) {
        System.out.println(LocalDate.now().getDayOfWeek().getValue());
    }
}
