package com.boxfishedu.workorder.service.accountcardinfo;

import com.boxfishedu.workorder.common.bean.AccountCourseBean;
import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.dao.mongo.ScheduleCourseInfoMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.ScheduleCourseInfo;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.ScheduleCourseInfoService;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.WorkOrderService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hucl on 16/9/26.
 */
@Component
public class DataCollectorService {
    @Autowired
    private ServeService serveService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private ScheduleCourseInfoMorphiaRepository scheduleCourseInfoMorphiaRepository;

    public Integer getUnSelectedAmount(Long studentId){
        List<Service> serviceList=serveService.getUnselectedService(studentId);
        if (CollectionUtils.isEmpty(serviceList)){
            return 0;
        }
        return 0;
    }

    public Integer getSelectedLeftAmount(Long studentId){
        //中教:核心素养+金币换课中教
        List<WorkOrder> overallCards=workOrderService.getSelectedLeftAmount(studentId, ComboTypeEnum.OVERALL);
        List<WorkOrder> exchangeChineses=workOrderService.getSelectedLeftAmount(studentId, ComboTypeEnum.EXCHANGE,com.boxfishedu.card.bean.TeachingType.ZHONGJIAO);

        //外教+金币换课外教+终极梦想
        List<WorkOrder> foreignCards=workOrderService.getSelectedLeftAmount(studentId, ComboTypeEnum.FOREIGN);
        List<WorkOrder> finalDreamCards=workOrderService.getSelectedLeftAmount(studentId, ComboTypeEnum.CHINESE);
        List<WorkOrder> exchangeForeigns=workOrderService.getSelectedLeftAmount(studentId, ComboTypeEnum.EXCHANGE,com.boxfishedu.card.bean.TeachingType.WAIJIAO);


        return 0;
//        return workOrderService.getSelectedLeftAmount(studentId).size();
    }

    public Integer getTotalLeftAmount(Long studentId){
        return this.getSelectedLeftAmount(studentId)+this.getUnSelectedAmount(studentId);
    }

    public WorkOrder getCardToStart(Long studentId){
        return workOrderService.getCardToStart(studentId);
    }

    public ScheduleCourseInfo getCourseByWorkOrder(Long workOrderId){
            return scheduleCourseInfoMorphiaRepository.queryByWorkId(workOrderId);
    }

    public AccountCourseBean.CardCourseInfo scheduleCourseAdapter(ScheduleCourseInfo scheduleCourseInfo,WorkOrder workOrder){
        AccountCourseBean.CardCourseInfo cardCourseInfo=new AccountCourseBean.CardCourseInfo();
        cardCourseInfo.setThumbnail(scheduleCourseInfo.getThumbnail());
        cardCourseInfo.setCourseId(scheduleCourseInfo.getCourseId());
        cardCourseInfo.setCourseName(scheduleCourseInfo.getName());
        cardCourseInfo.setDifficulty(scheduleCourseInfo.getDifficulty());
        cardCourseInfo.setCourseType(scheduleCourseInfo.getCourseType());
        cardCourseInfo.setIsFreeze(workOrder.getIsFreeze());
        cardCourseInfo.setStatus(workOrder.getStatus());
        return cardCourseInfo;
    }



}
