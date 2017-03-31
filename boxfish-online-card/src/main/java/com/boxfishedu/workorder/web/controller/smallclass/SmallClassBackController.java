package com.boxfishedu.workorder.web.controller.smallclass;

import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.servicex.bean.DayTimeSlots;
import com.boxfishedu.workorder.servicex.bean.TimeSlots;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassBackServiceX;
import com.boxfishedu.workorder.servicex.smallclass.PublicClassInfoQueryServiceX;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassLogServiceX;
import com.boxfishedu.workorder.servicex.smallclass.SmallClassQueryServiceX;
import com.boxfishedu.workorder.servicex.studentrelated.AutoTimePickerServiceX;
import com.boxfishedu.workorder.web.param.SmallClassParam;
import com.boxfishedu.workorder.web.param.StudentForSmallClassParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.PublicClassBuilderParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.PublicFilterParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.SmallClassAddStuParam;
import com.boxfishedu.workorder.web.param.fishcardcenetr.TrialSmallClassParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by hucl on 17/1/9.
 */
@RestController
@RequestMapping("/service/backend")
public class SmallClassBackController {
    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private SmallClassQueryServiceX smallClassQueryServiceX;

    @Autowired
    private PublicClassInfoQueryServiceX publicClassInfoQueryServiceX;

    @Autowired
    private SmallClassBackServiceX smallClassBackServiceX;

    @Autowired
    private SmallClassLogServiceX smallClassLogServiceX;

    @Autowired
    private AutoTimePickerServiceX addStudentForSmallClass;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping(value = "/smallclass/slot", method = RequestMethod.GET)
    public JsonResultModel publicSlots(String roleId) {
        DayTimeSlots dayTimeSlots = teacherStudentRequester.dayTimeSlotsTemplate(Long.parseLong(roleId));
        List<TimeSlots> timeSlotses = dayTimeSlots.getDailyScheduleTime();
        return JsonResultModel.newJsonResultModel(timeSlotses);
    }

    @RequestMapping(value = "/smallclassitem", method = RequestMethod.POST)
    public JsonResultModel buildPublicClass(@RequestBody PublicClassBuilderParam publicClassBuilderParam) {
        logger.debug("@buildPublicClass创建公开课,参数[{}]", publicClassBuilderParam);
        smallClassBackServiceX.configPublicClass(publicClassBuilderParam);
        return JsonResultModel.newJsonResultModel("OK");
    }

    @RequestMapping(value = "/smallclass", method = RequestMethod.PUT)
    public JsonResultModel updatePublicClass(PublicClassBuilderParam publicClassBuilderParam) {
        logger.debug("@updatePublicClass更新公开课,参数[{}]", publicClassBuilderParam);
        return JsonResultModel.newJsonResultModel("OK");
    }

    /**
     * 查询公开课(后台)
     *
     * @param publicFilterParam
     * @param pageable
     * @return
     */
    @RequestMapping(value = "/smallclass/public/listitem", method = RequestMethod.GET)
    public JsonResultModel list(PublicFilterParam publicFilterParam, Pageable pageable) {
        return smallClassQueryServiceX.listFishCardsByUnlimitedUserCond(publicFilterParam, pageable);
    }

    /**
     * 查询小班课(后台)
     *
     * @param publicFilterParam
     * @param pageable
     * @return
     */
    @RequestMapping(value = "/smallclass/small/listitem", method = RequestMethod.GET)
    public JsonResultModel smalllist(PublicFilterParam publicFilterParam, Pageable pageable) {
        return smallClassQueryServiceX.listFishCardsByUnlimitedUserCond(publicFilterParam, pageable);
    }

    /**
     * public_class_info 公开课明细查询
     * **smallClassId****
     * **studentId*******
     *
     * @param publicFilterParam
     * @param pageable
     * @return
     */
    @RequestMapping(value = "/smallclass/public/list", method = RequestMethod.GET)
    public JsonResultModel publiclist(PublicFilterParam publicFilterParam, Pageable pageable) {
        return publicClassInfoQueryServiceX.listFishCardsByUnlimitedUserCond(publicFilterParam, pageable);
    }

    @RequestMapping(value = "/smallclass/{smallclass_id}", method = RequestMethod.DELETE)
    public JsonResultModel delete(@PathVariable("smallclass_id") Long smallClassId) {
        smallClassBackServiceX.delete(smallClassId);
        return JsonResultModel.newJsonResultModel();
    }

    /**
     * 提供状态的查询列表
     */
    @RequestMapping(value = "/smallclass/status/list", method = RequestMethod.GET)
    public JsonResultModel listAllStatus() {
        return smallClassQueryServiceX.listAllStatus();
    }


    @RequestMapping(value = "/classlog/details", method = RequestMethod.GET)
    public JsonResultModel listCardDetail(SmallClassParam smallClassParam, Pageable pageable) throws Exception {
        return smallClassLogServiceX.listSmallClassLogByUnlimitedUserCond(smallClassParam, pageable);
    }

    /**
     * 鱼卡后台 小班课列表获取补课学生列表
     *
     * @param smallClassId
     * @return
     */
    @RequestMapping(value = "/stulist/{smallclass_id}", method = RequestMethod.GET)
    public JsonResultModel stulist(@PathVariable("smallclass_id") Long smallClassId) {
        return smallClassBackServiceX.getStudentList(smallClassId);
    }

    /**
     * 鱼卡后台 小班课列表查询所有补课学生level
     *
     * @return
     */
    @RequestMapping(value = "/stulevellist", method = RequestMethod.GET)
    public JsonResultModel stulistforlevel() {
        return smallClassBackServiceX.getStudentList();
    }


    /**
     * 批量给学生添加 订单forfree
     *
     * @param smallClassAddStuParam
     * @return
     */
    @RequestMapping(value = "/addStudents", method = RequestMethod.POST)
    public JsonResultModel addStudents(@RequestBody SmallClassAddStuParam smallClassAddStuParam) {
        if (CollectionUtils.isEmpty(smallClassAddStuParam.getStudentIds())) {
            return JsonResultModel.newJsonResultModel("fail");
        }

        return addStudentForSmallClass.addStudentForSmallClass(smallClassAddStuParam);
    }


    ///////   /service/backend

    /**
     * 删除候补学生名单
     *
     * @param studentId
     * @return
     */
    @RequestMapping(value = "/studentbackup/{student_id}", method = RequestMethod.DELETE)
    public JsonResultModel deleteStudent(@PathVariable("student_id") Long studentId) {
        smallClassBackServiceX.deletebackup(studentId);
        return JsonResultModel.newJsonResultModel();
    }

    /**
     * 获取候补学生名单
     *
     * @param studentId
     * @return
     */
    @RequestMapping(value = "studentbackup/getlist", method = RequestMethod.GET)
    public JsonResultModel getStudents(Long studentId, Pageable pageable) {
        return smallClassBackServiceX.getStudentBackUpList(studentId, pageable);
    }

    /**
     * 新增候补学生名单
     *
     * @param studentForSmallClassParam
     * @return
     */
    @RequestMapping(value = "/studentbackup/add", method = RequestMethod.POST)
    public JsonResultModel addBackUpStudent(@RequestBody StudentForSmallClassParam studentForSmallClassParam) {
        return JsonResultModel.newJsonResultModel(smallClassBackServiceX.addbackup(studentForSmallClassParam));
    }


    @RequestMapping(value = "/smallclass/trial", method = RequestMethod.POST)
    public JsonResultModel buildTrialSmallClass(@RequestBody TrialSmallClassParam trialSmallClassParam) {
        logger.debug("@buildTrialSmallClass创建试讲小班课,参数[{}]", JacksonUtil.toJSon(trialSmallClassParam));
//        smallClassBackServiceX.buildTrialSmallClass(trialSmallClassParam);
        return JsonResultModel.newJsonResultModel("OK");
    }

    /**
     * 解散小班课
     * @return
     */
    @RequestMapping(value="/{smallclass_id}/smallclassdismiss",method=RequestMethod.DELETE)
    public JsonResultModel dismissSmallClass(@PathVariable("smallclass_id") Long smallClassID){
        smallClassBackServiceX.dissmissSmallClass(smallClassID);
        return JsonResultModel.newJsonResultModel("OK");
    }

}
