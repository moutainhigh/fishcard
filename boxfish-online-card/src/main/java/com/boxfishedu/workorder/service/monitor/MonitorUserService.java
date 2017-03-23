package com.boxfishedu.workorder.service.monitor;

import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.EntityQuery;
import com.boxfishedu.workorder.dao.jpa.MonitorUserCourseJpaRepository;
import com.boxfishedu.workorder.dao.jpa.MonitorUserJpaRepository;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.*;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.servicex.bean.StudentCourseSchedule;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by ansel on 2017/3/21.
 */
@Service
public class MonitorUserService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MonitorUserJpaRepository monitorUserJpaRepository;

    @Autowired
    CourseScheduleService courseScheduleService;

    @Autowired
    ServiceSDK serviceSDK;

    @Autowired
    TeacherStudentRequester teacherStudentRequester;

    @Autowired
    SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    MonitorUserCourseJpaRepository monitorUserCourseJpaRepository;

    @Autowired
    EntityManager entityManager;


    public List<MonitorUser> getAllSuperUser(){
        logger.info("@getAllSuperUser checking for login ...");
        return monitorUserJpaRepository.getEnabledUser();
    }

    public MonitorUser addMonitorUser(MonitorUserRequestForm monitorUserRequestForm){
        logger.info("@addSuperUser adding monitor user ...");
        int min = 0;
        if (Objects.nonNull(monitorUserJpaRepository.getMinAvgSum())){
            min = monitorUserJpaRepository.getMinAvgSum();
        }
        MonitorUser monitorUserNew = new MonitorUser(monitorUserRequestForm);
        monitorUserNew.setAvgSum(min);
        return monitorUserJpaRepository.save(monitorUserNew);
    }

    @Transactional
    public void enabledMonitorUser(Long userId){
        logger.info("@enabledMonitorUser userId:[{}]",userId);
        monitorUserJpaRepository.enabledMonitorUser(new Date(),userId);
    }

    @Transactional
    public void disabledMonitorUser(Long userId){
        logger.info("@disabledMonitorUser userId:[{}]",userId);
        monitorUserJpaRepository.disabledMonitorUser(new Date(),userId);
    }

    public MonitorUser updateUserInfo(MonitorUserRequestForm monitorUserRequestForm){
        logger.info("@updateUserInfo update user info, monitorUserRequestForm:[{}]",monitorUserRequestForm);
        MonitorUser monitorUser = monitorUserJpaRepository.findByUserId(monitorUserRequestForm.getUserId());
        if (Objects.nonNull(monitorUserRequestForm.getUserName())){
            monitorUser.setUserName(monitorUserRequestForm.getUserName());
        }
        if (Objects.nonNull(monitorUserRequestForm.getPassWord())){
            monitorUser.setPassWord(monitorUserRequestForm.getPassWord());
        }
        if (Objects.nonNull(monitorUserRequestForm.getAccessToken())){
            monitorUser.setAccessToken(monitorUserRequestForm.getAccessToken());
        }
        if (Objects.nonNull(monitorUserRequestForm.getUserType())){
            monitorUser.setUserType(monitorUserRequestForm.getUserType());
        }
        monitorUser.setUpdateTime(new Date());
        return monitorUserJpaRepository.save(monitorUser);
    }

    public Page<MonitorResponseForm> page(String classType,Date startTime,Date endTime,Long userId,Pageable pageable){
        logger.info("@page get class sum group by startTime ,userId:[{}]",userId);
        return monitorUserCourseJpaRepository.getClassPage(classType,startTime,endTime,userId,pageable);
    }

    public Object detailList(String classType, Date startTime, Date endTime,Long studentId,Pageable pageable){
        logger.info("@detailList get class table ... studentId:[{}],classType:[{}]",studentId,classType);
        return smallClassJpaRepository.findMonitorUserCourse(startTime,endTime,classType,studentId,pageable);
    }

    public void distributeClassToMonitor(SmallClass smallClass){
        List<MonitorUser> listUser = monitorUserJpaRepository.getMinAvgSumUser();
        if (Objects.nonNull(listUser)){
            logger.info("@distributeClassToMonitor distribute SmallClass:[{}] to userId:[{}]",smallClass,listUser.get(0).getUserId());
            MonitorUserCourse monitorUserCourse = new MonitorUserCourse();
            monitorUserCourse.setMonitorUserId(listUser.get(0).getId());
            monitorUserCourse.setUserId(listUser.get(0).getUserId());
            monitorUserCourse.setClassId(smallClass.getId());
            monitorUserCourse.setClassType(smallClass.getClassType());
            monitorUserCourse.setCourseId(smallClass.getCourseId());
            monitorUserCourse.setStartTime(smallClass.getStartTime());
            monitorUserCourse.setEndTime(smallClass.getEndTime());
            monitorUserCourse.setCreateTime(new Date());
            monitorUserCourseJpaRepository.save(monitorUserCourse);
            MonitorUser monitorUser = listUser.get(0);
            monitorUser.setAvgSum(listUser.get(0).getAvgSum() + 1);
            monitorUserJpaRepository.save(monitorUser);
        }else {
            logger.info("@distributeClassToMonitor System does not have any monitor user!");
        }
    }
}
