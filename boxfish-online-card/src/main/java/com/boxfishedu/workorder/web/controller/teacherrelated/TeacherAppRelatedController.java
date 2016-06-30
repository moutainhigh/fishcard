package com.boxfishedu.workorder.web.controller.teacherrelated;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.service.TimeLimitPolicy;
import com.boxfishedu.workorder.servicex.CommonServeServiceX;
import com.boxfishedu.workorder.servicex.teacherrelated.TeacherAppRelatedServiceX;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;


/**
 * Created by hucl on 16/3/31.
 * 与教师操作相关的操作,主要为ios教师端相关
 */
@CrossOrigin
@RestController
@RequestMapping("/service/teacher")
public class TeacherAppRelatedController {
    @Autowired
    private TeacherAppRelatedServiceX teacherAppRelatedServiceX;
    @Autowired
    private CommonServeServiceX commonServeServiceX;
    @Autowired
    private ServiceSDK serviceSDK;
    @Autowired
    private TimeLimitPolicy timeLimitPolicy;
    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    /**
     * 评论学生,应该还需要的参数还有评论的内容等,写成dto,方便后续扩展,以及操作者的id
     *
     * @param workOrderId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/evaluation/student/{work_order_id}}", method = RequestMethod.POST)
    public JsonResultModel evaluateForStudent(@PathVariable("work_order_id") Long workOrderId) {
        return null;
    }

    /**
     * 教师端获取一个月的课程表
     * @return 返回带课程标记的课程规划表
     */
//    @Cacheable(value = "teacher_schedule_month", key = "T(java.util.Objects).hash(#teacherId,#dateIntervalView)")
    @RequestMapping(value = "{teacher_id}/schedule/month", method = RequestMethod.GET)
    public JsonResultModel courseScheduleList(@PathVariable("teacher_id") Long teacherId,Long userId) {
        commonServeServiceX.checkToken(teacherId, userId);
        return teacherAppRelatedServiceX.getScheduleByIdAndDateRange(teacherId, DateUtil.createDateRangeForm());
    }

    @RequestMapping(value = "{teacher_id}/schedule/day", method = RequestMethod.GET)
    public JsonResultModel courseScheduleList(@PathVariable("teacher_id") Long teacherId, Long userId,
                                              @RequestParam(required = false)
                                              @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        commonServeServiceX.checkToken(teacherId, userId);
        return teacherAppRelatedServiceX.getScheduleByIdAndDate(teacherId, date);
    }

    //    @Cacheable(value = "teacher_schedule_assigned", key = "T(java.util.Objects).hash(#teacherId,#date)")
    @RequestMapping(value = "{teacher_id}/schedule_assigned/day", method = RequestMethod.GET)
    public JsonResultModel courseScheduleListAssign(@PathVariable("teacher_id") Long teacherId, Long userId,
                                                    @RequestParam(required = false)
                                                    @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        commonServeServiceX.checkToken(teacherId, userId);
        return teacherAppRelatedServiceX.getScheduleAssignedByIdAndDate(teacherId, date);
    }

    @RequestMapping(value = "{teacherId}/timeSlots/template")
    public JsonResultModel getDayTimeSlotsTemplate(@PathVariable Long teacherId, Long userId,
                                                   @RequestParam(required = false)
                                                   @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        commonServeServiceX.checkToken(teacherId, userId);
        return JsonResultModel.newJsonResultModel(timeLimitPolicy.limit(teacherStudentRequester.dayTimeSlotsTemplate(teacherId, date)));
    }

    @RequestMapping(value = "international/{teacherId}/timeSlots/template", method = RequestMethod.GET)
    public JsonResultModel getInternationalDayTimeSlotsTemplate(
            @PathVariable Long teacherId, Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date date) throws CloneNotSupportedException {
        commonServeServiceX.checkToken(teacherId, userId);
        return teacherAppRelatedServiceX.getInternationalDayTimeSlotsTemplate(teacherId, date);
    }


    @RequestMapping(value = "international/{teacher_id}/schedule/day", method = RequestMethod.GET)
    public JsonResultModel internationalCourseScheduleList(
            @PathVariable("teacher_id") Long teacherId, Long userId,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date date) throws CloneNotSupportedException {
        commonServeServiceX.checkToken(teacherId, userId);
        return teacherAppRelatedServiceX.getInternationalScheduleByIdAndDate(teacherId, date);
    }
}
