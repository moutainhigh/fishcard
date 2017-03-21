package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.MonitorUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

/**
 * Created by ansel on 2017/3/21.
 */
public interface MonitorUserJpaRepository extends JpaRepository<MonitorUser, Long>{

    @Query("select mu from MonitorUser mu where mu.enabled = 1 and mu.user_type = 'student'")
    List<MonitorUser> getEnabledUser();

    MonitorUser findByUserId(Long userId);

    @Modifying
    @Query("update MonitorUser mu set mu.enabled = 1,mu.updateTime = ?1 where mu.userId = ?2")
    void enabledMonitorUser(Date updateTime,Long userId);

    @Modifying
    @Query("update MonitorUser mu set mu.enabled = 0,mu.updateTime = ?1 where mu.userId = ?2")
    void disabledMonitorUser(Date updateTime,Long userId);


}
