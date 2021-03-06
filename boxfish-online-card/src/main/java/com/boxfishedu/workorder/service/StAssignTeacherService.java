package com.boxfishedu.workorder.service;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.Collections3;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.dao.jpa.CourseScheduleRepository;
import com.boxfishedu.workorder.dao.jpa.StStudentApplyRecordsJpaRepository;
import com.boxfishedu.workorder.dao.jpa.StStudentSchemaJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.dao.mongo.WorkOrderLogMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.WorkOrderLog;
import com.boxfishedu.workorder.entity.mysql.*;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.servicex.assignTeacher.RemoteService;
import com.boxfishedu.workorder.servicex.studentrelated.AssignTeacherService;
import com.boxfishedu.workorder.web.param.ScheduleBatchReqSt;
import com.boxfishedu.workorder.web.param.bebase3.ScheduleModelSt;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by olly on 2016/12/20.
 */
@Component
public class StAssignTeacherService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    WorkOrderJpaRepository workOrderJpaRepository;
    @Autowired
    CourseScheduleRepository courseScheduleRepository;
    @Autowired
    StStudentApplyRecordsJpaRepository stStudentApplyRecordsJpaRepository;
    @Autowired
    StStudentSchemaJpaRepository stStudentSchemaJpaRepository;
    @Autowired
    WorkOrderLogMorphiaRepository workOrderLogMorphiaRepository;
    @Autowired
    ServiceSDK serviceSDK;
    @Autowired
    DataCollectorService dataCollectorService;
    @Autowired
    RemoteService remoteService;
    @Autowired
    CourseOnlineRequester courseOnlineRequester;
    @Autowired
    AssignTeacherService assignTeacherService;

    /**
     *
     * @param teacherId
     * @param studentId
     * @param aggressorCourseSchedules
     * @param channel
     * @param skuId
     */
    @Transactional
    public void doAssignTeacher(Long teacherId, Long studentId, List<CourseSchedule> aggressorCourseSchedules,List<CourseSchedule> alreadyCourseSchedules,
                                String channel, Integer skuId) {
        Date startTime = DateTime.now().plusHours(48).toDate();
        logger.info("@@@@assign 指定老师 stp-2:::初始化:::channel=={}======>>>APP端学生ID:{}===>>>>发起指定老师:{}" +
                        "===>>skuId:{}====>>鱼卡IDS:{}===>>>总共{}条",
                channel,studentId, teacherId, skuId, Collections3.extractToList(aggressorCourseSchedules,"workorderId").toArray(),
                aggressorCourseSchedules==null?0:aggressorCourseSchedules.size());

        logger.info("@@@@assign 指定老师 stp-2:::channel==>>{}alreadyCourseSchedules:::======>>>APP端学生ID:{}===>>>>发起指定老师:{}" +
                        "===>>skuId:{}====>>鱼卡IDS:{}===>>>总共{}条",
                channel,studentId, teacherId, skuId, Collections3.extractToList(alreadyCourseSchedules,"workorderId").toArray(),
                alreadyCourseSchedules==null?0:alreadyCourseSchedules.size());

        if(Collections3.isEmpty(aggressorCourseSchedules)){
            logger.info("@@@@assign 指定老师 stp-2:::channel====>>{} ==排除相同指定老师:::======>>>APP端学生ID:{}===>>>>发起指定老师:{}" +
                            "===>>skuId:{}=====>>channel:{}====>>没有数据!!!!!!!!!!",
                    channel,studentId, teacherId, skuId,channel);
            if(Collections3.isNotEmpty(alreadyCourseSchedules)){
                makeApplyRecords( teacherId,  studentId, null, alreadyCourseSchedules, skuId);
            }
            return;
        }

        //TODO 当前指定老师的其他学生的课表
        List<CourseSchedule> victimCourseSchedules =
                courseScheduleRepository.
                        findByTeacherIdAndStudentIdNotAndIsFreezeAndRoleIdAndStartTimeGreaterThan(teacherId, studentId, 0, skuId,startTime);

        logger.info("@@@@assign 指定老师 stp-2:::channel====>>{} :::======>>>APP端学生ID:{}===>>>>发起指定老师:{}" +
                        "===>>skuId:{}=====>>channel:{}====>>victimCourseSchedules==={}",
                channel,studentId, teacherId, skuId,channel,victimCourseSchedules);

        Map<String,CourseSchedule> victimCourseSchedulesFinalMap = Maps.newHashMap();
        Map<String,CourseSchedule> assignedCourseSchedulesMap = Maps.newHashMap();  //也指定过改老师,这样的鱼卡就放弃
        if (Collections3.isNotEmpty(victimCourseSchedules)) {
            StStudentSchema stStudentSchemaTmp = null;
            String key = null;
            for (CourseSchedule courseSchedule : victimCourseSchedules ) {
                for(CourseSchedule cs : aggressorCourseSchedules){
                    if(courseSchedule.getClassDate().compareTo(cs.getClassDate())==0
                            && courseSchedule.getTimeSlotId().intValue()==cs.getTimeSlotId().intValue()){
                        stStudentSchemaTmp = stStudentSchemaJpaRepository
                                .findByStudentIdAndTeacherIdAndSkuIdAndStSchema(courseSchedule.getStudentId(),
                                        courseSchedule.getTeacherId(), StStudentSchema.CourseType.getEnum(skuId),
                                        StStudentSchema.StSchema.assgin);
                        if (null == stStudentSchemaTmp) {
                            key = new DateTime(courseSchedule.getClassDate()).toString("yyyy-MM-dd") + "-"+ courseSchedule.getTimeSlotId();
                            if(victimCourseSchedulesFinalMap.get(key) == null){
                                victimCourseSchedulesFinalMap.put(key,courseSchedule);
                            }else { //TODO 一般不会发生,如果一旦发生先记录日志
                                logger.error("@@@@assignchannel====>>{}=====>>指定老师 出现同一老师{}不同学生重复学生::时间片{}",channel,teacherId,key);
                            }

                        }else{
                            assignedCourseSchedulesMap.put(key,courseSchedule);
                            logger.info("@@@@assign 指定老师 stp-2:::channel====>>{}=====>>发现不能抢的鱼卡:::======>>>APP端学生ID:{}===>>>>发起指定老师:{}" +
                                            "===>>skuId:{}====>>鱼卡ID:{}",
                                    channel,studentId, teacherId, skuId,courseSchedule.getWorkorderId());
                        }
                        break;
                    }
                }
            }

        }
        ScheduleBatchReqSt scheduleBatchReqSt = match(studentId, teacherId,skuId, aggressorCourseSchedules,
                victimCourseSchedulesFinalMap,assignedCourseSchedulesMap, channel);

        logger.info("@@@@assign 指定老师channel====>>{}=====>> stp-2:::请求师生运营:::======>>>APP端学生ID:{}===>>>>发起指定老师:{}" +
                        "===>>skuId:{}====>>(排除那些也指定过这个老师的之后)老师其他学生的鱼卡IDS:{}===>>>总共{}条",
                channel,studentId, teacherId, skuId, Collections3.extractToList(scheduleBatchReqSt.getScheduleModelList(),"workOrderId").toArray(),
                scheduleBatchReqSt.getScheduleModelList().size());

        //TODO 此处去请求师生运营
        ScheduleBatchReqSt responseScheduleBatchReqSt = null;
        try {
            responseScheduleBatchReqSt = remoteService.matchTeacher(scheduleBatchReqSt);
        }catch (BusinessException e){
            throw e;
        }
        //TODO 此处请求师生运营进行教师重新匹配
        //TODO 分为3中状态 匹配成功直接更新鱼卡和课表 不匹配不更新 无时间片 请求记录入库
        List<ScheduleModelSt> scheduleModelStList = responseScheduleBatchReqSt.getScheduleModelList();
        if (Collections3.isEmpty(scheduleModelStList)) {
            throw new BusinessException("请求师生运营系统匹配老师返回数据空");
        }
        logger.info("@@@@assign 指定老师channel====>>{}=====>> stp-2:::师生运营完成匹配:::======>>>APP端学生ID:{}====>>师生运营完成匹配,其中teacherId:{}===>>>匹配外层信息:{}",
                channel,studentId, responseScheduleBatchReqSt.getAssginTeacherId(), responseScheduleBatchReqSt.toString());


        List<Long> macthedWorkOrderIdList = Lists.newArrayList();
        List<Long> unmacthedWorkOrderIdList = Lists.newArrayList();
        List<Long> wait2applyWorkOrderIdList = Lists.newArrayList();
        List<ScheduleModelSt> macthedList = Lists.newArrayList();
        List<ScheduleModelSt> wait2applyList = Lists.newArrayList();
        for (ScheduleModelSt scheduleModelSt : scheduleModelStList) {
            if (scheduleModelSt.getMatchStatus() == StStudentApplyRecords.MatchStatus.matched) {
                logger.info("@@@@assign 指定老师 ::channel====>>{}=====>>stp-2:::师生运营完成匹配:::======>>>APP端学生ID:{}====>>师生运营完成匹配,其中MATCHED上信息:{}",
                        channel,studentId, scheduleModelSt.toString());
                macthedList.add(scheduleModelSt);
                macthedWorkOrderIdList.add(scheduleModelSt.getWorkOrderId());
            }else if (scheduleModelSt.getMatchStatus() == StStudentApplyRecords.MatchStatus.un_matched){
                wait2applyList.add(scheduleModelSt);
                unmacthedWorkOrderIdList.add(scheduleModelSt.getWorkOrderId());
            }else if (scheduleModelSt.getMatchStatus() == StStudentApplyRecords.MatchStatus.wait2apply){
                logger.info("@@@@assign 指定老师 stp-2:::channel====>>{}=====>>师生运营完成匹配:::======>>>APP端学生ID:{}====>>师生运营完成匹配,其中其中WAIT2APPLY上信息:{}",
                        channel,studentId, scheduleModelSt.toString());
                wait2applyList.add(scheduleModelSt);
                wait2applyWorkOrderIdList.add(scheduleModelSt.getWorkOrderId());
            }else{
                logger.error("@@@@assign 指定老师channel====>>{}=====>> stp-2:::师生运营完成匹配:::======>>>APP端学生ID:{}====>>师生运营完成匹配,其中鱼卡ID====>>{}=====>>异常匹配信息:{}",
                        channel,studentId, scheduleModelSt.getWorkOrderId(), scheduleModelSt.toString());
                scheduleModelSt.setMatchStatus(StStudentApplyRecords.MatchStatus.un_matched);
            }
        }

        logger.info("@@@@assign 指定老师 stp-2:::channel====>>{}=====>>师生运营完成匹配:::======>>>APP端学生ID:{}====>>师生运营完成匹配,其中MATCHED上--鱼卡IDS:{}",
                channel,studentId,  macthedWorkOrderIdList);
        logger.info("@@@@assign 指定老师 stp-2:::channel====>>{}=====>>师生运营完成匹配:::======>>>APP端学生ID:{}====>>师生运营完成匹配,其中WAIT2APPLY --鱼卡IDS:{}",
                channel,studentId, wait2applyWorkOrderIdList);
        logger.info("@@@@assign 指定老师 stp-2:::channel====>>{}=====>>师生运营完成匹配:::======>>>APP端学生ID:{}====>>师生运营完成匹配,其中UNMATCHED --鱼卡IDS:{}",
                channel,studentId, unmacthedWorkOrderIdList);
        if (channel.equals(ConstantUtil.STUDENT_CHANNLE)) {
            makeApplyRecords(teacherId, studentId, scheduleModelStList,alreadyCourseSchedules,skuId);
        } else if (channel.equals(ConstantUtil.TEACHER_CHANNLE)) {
            changeApplyRecords(studentId, teacherId, macthedList);
        } else if (channel.equals(ConstantUtil.TIMER_CHANNLE)) {
            makeApplyRecords(teacherId, studentId, scheduleModelStList,alreadyCourseSchedules, skuId);
        }
        if (Collections3.isNotEmpty(macthedWorkOrderIdList)) {
            List<WorkOrder> workOrders = workOrderJpaRepository.findByIdInAndIsFreeze(macthedWorkOrderIdList,0);
            List<CourseSchedule> courseSchedules = courseScheduleRepository.findByWorkorderIdInAndIsFreeze(macthedWorkOrderIdList,0);
            for (WorkOrder workOrder : workOrders) {
                workOrder.setTeacherId(teacherId);
                workOrder.setTeacherName(responseScheduleBatchReqSt.getAssginTeacherName());
                workOrder.setAssignTeacherTime(new Date());
            }

            List<Long> needFireWorkOrderIds= Lists.newArrayList();
            String matchKey = null;
            CourseSchedule needFireCourseSchedule = null;
            for (CourseSchedule courseSchedule : courseSchedules) {
                matchKey = new DateTime(courseSchedule.getClassDate()).toString("yyyy-MM-dd") + "-"+ courseSchedule.getTimeSlotId();
                needFireCourseSchedule = victimCourseSchedulesFinalMap.get(matchKey);
                if(null != needFireCourseSchedule){
                    needFireWorkOrderIds.add(needFireCourseSchedule.getWorkorderId());
                }
                courseSchedule.setTeacherId(teacherId);
                courseSchedule.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
            }
            logger.info("@@@@assign 指定老师channel====>>{}=====>> stp-2::::通知更新群组:::::======>>>APP端学生ID:{}===>>>>发起指定老师:{}===>>skuId:{}====>>鱼卡IDS{}",
                    channel,studentId, teacherId, skuId, Collections3.extractToList(workOrders,"id"));
            notifyOthers(workOrders);
            logger.info("@@@@assign 指定老师 channel====>>{}=====>>stp-2::::异步记录鱼卡日志:::::======>>>APP端学生ID:{}===>>>>发起指定老师:{}===>>skuId:{}====>>鱼卡IDS{}",
                    channel,studentId, teacherId, skuId, Collections3.extractToList(workOrders,"id"));
            changeTeacherLog(macthedList,teacherId);
            if(Collections3.isNotEmpty(needFireWorkOrderIds)){
                logger.info("@@@@assign ===channel====>>{}=====>> 指定老师 stp-3 needfire:::开始更新鱼卡和课表入库:::======>>>APP端学生ID:{}===>>>>发起指定老师:{}===>>skuId:{}====>>需要被释放的的鱼卡IDS{}",
                        channel,studentId, teacherId, skuId, needFireWorkOrderIds.toArray());
                List<WorkOrder> needFireWorkOrders = workOrderJpaRepository.findByIdInAndIsFreeze(needFireWorkOrderIds,0);
                List<CourseSchedule> needFireCourseSchedules = courseScheduleRepository.findByWorkorderIdInAndIsFreeze(needFireWorkOrderIds,0);
                for(WorkOrder workOrder :needFireWorkOrders ){
                    workOrder.setTeacherId(0L);
                    courseOnlineRequester.releaseGroup(workOrder);
                }
                for(CourseSchedule courseSchedule : needFireCourseSchedules ){
                    courseSchedule.setTeacherId(0L);
                }

                workOrders.addAll(needFireWorkOrders);
                courseSchedules.addAll(needFireCourseSchedules);

            }
            workOrderJpaRepository.save(workOrders);
            courseScheduleRepository.save(courseSchedules);

        }


    }

    /**
     * 组合生成请求
     *
     * @param studentId
     * @param teacherId
     * @param aggressorCourseSchedules
     * @param victimCourseSchedules
     * @return
     */
    private ScheduleBatchReqSt match(Long studentId, Long teacherId,Integer roleId, List<CourseSchedule> aggressorCourseSchedules,
                                     Map<String,CourseSchedule> victimCourseSchedules,Map<String,CourseSchedule> assignedCourseSchedulesMap,String channel) {
        ScheduleBatchReqSt scheduleBatchReqSt = new ScheduleBatchReqSt();
        List<ScheduleModelSt> scheduleModelSts = Lists.newArrayList();
        ScheduleModelSt scheduleModelSt = null;
        String matchKey = null;
        CourseSchedule victimCourseSchedule = null;
        CourseSchedule excludeSchedule = null;
        CourseSchedule courseSchedule = null;
        for (Iterator<CourseSchedule> iter = aggressorCourseSchedules.iterator(); iter.hasNext();) {
            courseSchedule = iter.next();
            scheduleModelSt = new ScheduleModelSt(courseSchedule);
            if (channel.equals(ConstantUtil.TEACHER_CHANNLE)) {
                scheduleModelSt.setMatchStatus(StStudentApplyRecords.MatchStatus.wait2apply);
            }
            matchKey = new DateTime(courseSchedule.getClassDate()).toString("yyyy-MM-dd") + "-"+ courseSchedule.getTimeSlotId();
            victimCourseSchedule = victimCourseSchedules.get(matchKey);
            excludeSchedule = assignedCourseSchedulesMap.get(matchKey);
            if(null != excludeSchedule){
                logger.info("@@@@assign-指定老师 channel====>>{}=====>>stp-match::::排除掉那些也指定改老师的同时间片的鱼卡:::::======>>>APP端学生ID:{}===>>>>发起指定老师:{}====>>>鱼卡ID{}====>>>不能抢鱼卡ID{}",
                        channel,studentId, teacherId, courseSchedule.getWorkorderId(),excludeSchedule.getWorkorderId());
                iter.remove();
                continue;
            }
            if(null != victimCourseSchedule){
                scheduleModelSt.setGrabedStudentId(victimCourseSchedule.getStudentId());
                scheduleModelSt.setGrabedId(victimCourseSchedule.getId());
                scheduleModelSt.setGrabedDay(victimCourseSchedule.getClassDate().getTime());
                scheduleModelSt.setGrabedcourseType(victimCourseSchedule.getCourseType());
                scheduleModelSt.setGrabedSlotId(victimCourseSchedule.getTimeSlotId());
                scheduleModelSt.setGrabedRoleId(victimCourseSchedule.getRoleId());
                scheduleModelSt.setGrabedWorkOrderId(victimCourseSchedule.getWorkorderId());
                logger.info("@@@@assign 指定老师 stp-match::::channel====>>{}=====>>异步记录鱼卡日志:::::======>>>APP端学生ID:{}===>>>>发起指定老师:{}====>>>抢单信息{}",
                        channel,studentId, teacherId, scheduleModelSt.toString());
            }

            scheduleModelSts.add(scheduleModelSt);
        }
        scheduleBatchReqSt.setRoleId(roleId);
        scheduleBatchReqSt.setUserId(studentId);
        scheduleBatchReqSt.setAssginTeacherId(teacherId);

        if (channel.equals(ConstantUtil.STUDENT_CHANNLE)) {
            scheduleBatchReqSt.setOperateType(ConstantUtil.MANUAL_OPERATOR);
        } else if (channel.equals(ConstantUtil.TEACHER_CHANNLE)) {
            scheduleBatchReqSt.setOperateType(ConstantUtil.MANUAL_TECH_OPERATOR);
        } else if (channel.equals(ConstantUtil.TIMER_CHANNLE)) {
            scheduleBatchReqSt.setOperateType(ConstantUtil.TIMER_OPERATOR);
        }
        scheduleBatchReqSt.setScheduleModelList(scheduleModelSts);
        return scheduleBatchReqSt;
    }

    /**
     * @param studentId
     * @param teacherId
     * @param skuId
     * @return
     */
/*    private StStudentSchema checkSchema(Long studentId, Long teacherId, Integer skuId) {
        StStudentSchema stStudentSchema = stStudentSchemaJpaRepository.findByStudentIdAndTeacherIdAndSkuId(studentId,
                teacherId, StStudentSchema.CourseType.getEnum(skuId));
        if (null == stStudentSchema) {
            logger.info("@@@@assign 指定老师 stp-2:::检查该学生的上课模式:::======>>>APP端学生ID:{}===>>>>发起指定老师:{}===>>skuId:{}====>>当前学生以前未指定过老师}",
                    studentId, teacherId, skuId);
            stStudentSchema = new StStudentSchema();
            stStudentSchema.setCreateTime(new Date());
            stStudentSchema.setUpdateTime(new Date());
            stStudentSchema.setStSchema(StStudentSchema.StSchema.assgin);
            stStudentSchema.setStudentId(studentId);
            stStudentSchema.setTeacherId(teacherId);
            stStudentSchema.setSkuId(StStudentSchema.CourseType.getEnum(skuId));
        } else {
            logger.info("@@@@assign 指定老师 stp-2:::检查该学生的上课模式:::======>>>APP端学生ID:{}===>>>>发起指定老师:{}===>>skuId:{}====>>当前学生以前定过老师:{}",
                    studentId, teacherId, skuId, stStudentSchema.getTeacherId());
            stStudentSchema.setStSchema(StStudentSchema.StSchema.assgin);
            stStudentSchema.setTeacherId(teacherId);
            stStudentSchema.setUpdateTime(new Date());
            stStudentSchema.setSkuId(StStudentSchema.CourseType.getEnum(skuId));
        }
        stStudentSchemaJpaRepository.save(stStudentSchema);
        return stStudentSchema;
    }*/

    /**
     * @param teacherId
     * @param studentId
     * @param matchlist
     */
    @Transactional
    public void makeApplyRecords(Long teacherId, Long studentId,List<ScheduleModelSt> matchlist,List<CourseSchedule> alreadyList,Integer skuId) {
        List<StStudentApplyRecords> stStudentApplyRecordsList = Lists.newArrayList();
        StStudentApplyRecords stStudentApplyRecords = null;
        Date now = new Date();
        boolean canPush = false;
        if(Collections3.isNotEmpty(matchlist)){
            for (ScheduleModelSt scheduleModelSt : matchlist) {
                stStudentApplyRecords = new StStudentApplyRecords();
                stStudentApplyRecords.setTeacherId(teacherId);
                stStudentApplyRecords.setStudentId(studentId);
                stStudentApplyRecords.setApplyTime(now);
                stStudentApplyRecords.setCreateTime(now);
                stStudentApplyRecords.setUpdateTime(now);
                stStudentApplyRecords.setSkuId(skuId);
                stStudentApplyRecords.setValid(StStudentApplyRecords.VALID.yes);
                stStudentApplyRecords.setApplyStatus(StStudentApplyRecords.ApplyStatus.pending);
                stStudentApplyRecords.setIsRead(StStudentApplyRecords.ReadStatus.no);
                stStudentApplyRecords.setWorkOrderId(scheduleModelSt.getWorkOrderId());
                stStudentApplyRecords.setCourseScheleId(scheduleModelSt.getId());
                stStudentApplyRecords.setMatchStatus(scheduleModelSt.getMatchStatus()==null? StStudentApplyRecords.MatchStatus.un_matched:scheduleModelSt.getMatchStatus());
                stStudentApplyRecordsList.add(stStudentApplyRecords);
                if(stStudentApplyRecords.getMatchStatus() == StStudentApplyRecords.MatchStatus.wait2apply){
                    canPush = true;
                }
            }
        }
        if(Collections3.isNotEmpty(alreadyList)){
            for (CourseSchedule courseSchedule : alreadyList) {
                stStudentApplyRecords = new StStudentApplyRecords();
                stStudentApplyRecords.setTeacherId(teacherId);
                stStudentApplyRecords.setStudentId(studentId);
                stStudentApplyRecords.setApplyTime(now);
                stStudentApplyRecords.setCreateTime(now);
                stStudentApplyRecords.setUpdateTime(now);
                stStudentApplyRecords.setSkuId(skuId);
                stStudentApplyRecords.setValid(StStudentApplyRecords.VALID.yes);
                stStudentApplyRecords.setApplyStatus(StStudentApplyRecords.ApplyStatus.agree);
                stStudentApplyRecords.setIsRead(StStudentApplyRecords.ReadStatus.yes);
                stStudentApplyRecords.setWorkOrderId(courseSchedule.getWorkorderId());
                stStudentApplyRecords.setCourseScheleId(courseSchedule.getId());
                stStudentApplyRecords.setMatchStatus(StStudentApplyRecords.MatchStatus.matched);
                stStudentApplyRecordsList.add(stStudentApplyRecords);
            }
        }
        //TODO 无时间片 请求记录入库 入库之前,先把之前的申请记录全部作废掉
        List<StStudentApplyRecords> invalidRecordsList = stStudentApplyRecordsJpaRepository.
                findByStudentIdAndTeacherIdAndValid(studentId, teacherId, StStudentApplyRecords.VALID.yes);
        if (Collections3.isNotEmpty(invalidRecordsList)) {
            for (StStudentApplyRecords studentApplyRecords : invalidRecordsList) {
                studentApplyRecords.setValid(StStudentApplyRecords.VALID.no);
                studentApplyRecords.setUpdateTime(now);
            }
            stStudentApplyRecordsJpaRepository.save(invalidRecordsList);
        }
        if(Collections3.isNotEmpty(stStudentApplyRecordsList)){
            stStudentApplyRecordsJpaRepository.save(stStudentApplyRecordsList);
        }
        if(canPush){
            assignTeacherService.pushTeacherList(teacherId);
        }

    }

    /**
     * 更新申请记录
     *
     * @param studentId
     * @param macthedList
     */
    @Transactional
    public void changeApplyRecords(Long studentId, Long teacherId, List<ScheduleModelSt> macthedList) {
        List<StStudentApplyRecords> invalidRecordsList = stStudentApplyRecordsJpaRepository.
                findByStudentIdAndTeacherIdAndValid(studentId, teacherId, StStudentApplyRecords.VALID.yes);
        if (Collections3.isNotEmpty(invalidRecordsList)) {
            boolean canPush = false;
            for (StStudentApplyRecords stStudentApplyRecords : invalidRecordsList) {
                for (ScheduleModelSt scheduleModelSt : macthedList) {
                    if (scheduleModelSt.getWorkOrderId().longValue() == stStudentApplyRecords.getWorkOrderId().longValue()) {
                        stStudentApplyRecords.setUpdateTime(new Date());
                        stStudentApplyRecords.setMatchStatus(scheduleModelSt.getMatchStatus()==null? StStudentApplyRecords.MatchStatus.un_matched:scheduleModelSt.getMatchStatus());
                        if(stStudentApplyRecords.getMatchStatus() == StStudentApplyRecords.MatchStatus.wait2apply){
                            canPush = true;
                        }
                        break;
                    }
                }
            }
            stStudentApplyRecordsJpaRepository.save(invalidRecordsList);
            if(canPush){
                assignTeacherService.pushTeacherList(teacherId);
            }
        }
    }

    /**tail
     *
     * @param macthedList
     * @param teacherId
     */
    @Async
    private void changeTeacherLog(List<ScheduleModelSt> macthedList,Long teacherId){
        List<WorkOrderLog> workOrderLogs = Lists.newArrayList();
        WorkOrderLog workOrderLog = null;
        WorkOrder workOrder = null;
        String content = null;
        for(ScheduleModelSt scheduleModelSt :macthedList){
            workOrder = workOrderJpaRepository.findOne(scheduleModelSt.getWorkOrderId());
            workOrderLog = new WorkOrderLog();
            workOrderLog.setCreateTime(new Date());
            workOrderLog.setWorkOrderId(scheduleModelSt.getWorkOrderId());
            workOrderLog.setStatus(workOrder.getStatus());
            content = MessageFormat.format("@@@@assign-指定更换教师:workOrderId:{0}status={1},指定老师={2},之前鱼卡老师信息信息{3},匹配老师oldteacher信息{4},指定老师{5}",scheduleModelSt.getWorkOrderId(),FishCardStatusEnum.getDesc(workOrder.getStatus()),teacherId,workOrder.getTeacherId(),scheduleModelSt.getOldTeacherId(),teacherId);
            workOrderLog.setContent(content);
            logger.info(content);
            workOrderLogs.add(workOrderLog);
        }
        workOrderLogMorphiaRepository.save(workOrderLogs);
    }

    /**
     *
     * @param workOrders
     */
    @Async
    private void notifyOthers(List<WorkOrder> workOrders){
        for(WorkOrder workOrder :workOrders){
            dataCollectorService.updateBothChnAndFnItemAsync(workOrder.getStudentId());
            //通知小马添加新的群组
            serviceSDK.createGroup(workOrder);
        }
    }
}
