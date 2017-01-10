package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * Created by LuoLiBing on 17/1/9.
 */
public interface SmallClassJpaRepository extends JpaRepository<SmallClass, Long> {

    List<SmallClass> findByClassDateAndSlotIdAndSmallClassType(Date classDate, Integer slotId, String classType);

    @Query("select s from SmallClass s order by s.startTime")
    Page<SmallClass> findPage(Pageable pageable);
}
