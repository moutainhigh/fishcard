package com.boxfishedu.workorder.service;

import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.mongo.ScheduleCourseInfoMorphiaRepository;
import com.boxfishedu.workorder.dao.mongo.TrialCourseMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.ScheduleCourseInfo;
import com.boxfishedu.workorder.entity.mongo.TrialCourse;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by hucl on 16/4/13.
 */
@SuppressWarnings("ALL")
@Component
public class ScheduleCourseInfoService {
    @Autowired
    private ScheduleCourseInfoMorphiaRepository scheduleCourseInfoMorphiaRepository;

    @Autowired
    private TrialCourseMorphiaRepository trialCourseMorphiaRepository;

    @Autowired
    private UrlConf urlConf;
    @Autowired
    private Datastore datastore;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void save(ScheduleCourseInfo scheduleCourseInfo) {
        logger.info("往mongo存入课程信息{}", JacksonUtil.toJSon(scheduleCourseInfo));
        scheduleCourseInfoMorphiaRepository.save(scheduleCourseInfo);
    }

    public void save(Iterable<ScheduleCourseInfo> scheduleCourseInfos) {
        logger.info("往mongo存入课程信息{}", JacksonUtil.toJSon(scheduleCourseInfos));
        scheduleCourseInfoMorphiaRepository.saveWithFsyncSafe(scheduleCourseInfos);
    }

    public Optional<ScheduleCourseInfo> getById(Long id) {
        return scheduleCourseInfoMorphiaRepository.getById(id);
    }

    public List<ScheduleCourseInfo> queryByCourseId(String courseId) {
        return scheduleCourseInfoMorphiaRepository.queryByCourseId(courseId);
    }

    public ScheduleCourseInfo queryByWorkId(Long workId) {
        return scheduleCourseInfoMorphiaRepository.queryByWorkId(workId);
    }

    public ScheduleCourseInfo queryByScheduleId(Long scheduleId) {
        ScheduleCourseInfo scheduleCourseInfo = scheduleCourseInfoMorphiaRepository.queryByScheduleId(scheduleId);
        logger.info("mongo-根据scheduleId[{}]获取对应的课程信息;获取结果为{}", scheduleId, JacksonUtil.toJSon(scheduleCourseInfo));
        return scheduleCourseInfo;
    }

    public void batchSaveCourseInfos(List<WorkOrder> workOrders, List<CourseSchedule> courseSchedules, Map<Integer, RecommandCourseView> courseViewMap) {
        logger.info("->>>>>>>>开始向mongodb插入课程信息");
        List<ScheduleCourseInfo> scheduleCourseInfos = Lists.newArrayList();
        Map<String, CourseSchedule> courseScheduleMap = Maps.newHashMap();
        for (CourseSchedule courseSchedule : courseSchedules) {
            courseScheduleMap.put(courseSchedule.getWorkorderId().toString(), courseSchedule);
        }
        for (int i = 0, size = workOrders.size(); i < size; i++) {
            WorkOrder workOrder = workOrders.get(i);
            RecommandCourseView courseView = courseViewMap.get(i);
            ScheduleCourseInfo scheduleCourseInfo = new ScheduleCourseInfo();
            scheduleCourseInfo.setCourseId(courseView.getCourseId());
            scheduleCourseInfo.setCourseType(courseView.getCourseType());
            scheduleCourseInfo.setEnglishName(courseView.getEnglishName());
            scheduleCourseInfo.setDifficulty(courseView.getDifficulty());
            scheduleCourseInfo.setName(courseView.getCourseName());
            String thumbnail = String.format("%s%s", urlConf.getThumbnail_server(), courseView.getCover());
            scheduleCourseInfo.setThumbnail(thumbnail);
            scheduleCourseInfo.setScheduleId(courseScheduleMap.get(workOrder.getId().toString()).getId());
            scheduleCourseInfo.setWorkOrderId(workOrder.getId());
            scheduleCourseInfos.add(scheduleCourseInfo);
        }
        save(scheduleCourseInfos);
    }

    public TrialCourse queryByCourseIdAndScheduleType(String courseId, String scheduleType) {
        TrialCourse trialCourse = null;
        try {
            trialCourse = trialCourseMorphiaRepository.queryByCourseIdAndScheduleType(courseId, scheduleType);
            if (null == trialCourse) {
                throw new BusinessException("不存在课程");
            }
        } catch (Exception ex) {
            throw new BusinessException("不存在对应的课程,请确认");
        }
        return trialCourse;
    }

    public void updateCourseIntoScheduleInfo(ScheduleCourseInfo mewScheduleCourseInfo) {
        Query<ScheduleCourseInfo> updateQuery = datastore.createQuery(ScheduleCourseInfo.class);
        updateQuery.criteria("workOrderId").equal(mewScheduleCourseInfo.getWorkOrderId());

        ScheduleCourseInfo scheduleCourseInfo= updateQuery.get();
        if(null!=scheduleCourseInfo){
            scheduleCourseInfo.setCourseId(mewScheduleCourseInfo.getCourseId());
            scheduleCourseInfo.setCourseType(mewScheduleCourseInfo.getCourseType());
            scheduleCourseInfo.setName(mewScheduleCourseInfo.getName());
            scheduleCourseInfo.setEnglishName(mewScheduleCourseInfo.getEnglishName());
            scheduleCourseInfo.setCourseType(mewScheduleCourseInfo.getCourseType());
            scheduleCourseInfo.setDifficulty(mewScheduleCourseInfo.getDifficulty());
            scheduleCourseInfo.setThumbnail(mewScheduleCourseInfo.getThumbnail());
            datastore.save(scheduleCourseInfo);
        }
    }
}
