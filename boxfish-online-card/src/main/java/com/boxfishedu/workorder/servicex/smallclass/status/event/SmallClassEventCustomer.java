package com.boxfishedu.workorder.servicex.smallclass.status.event;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.smallclass.SmallClassLogService;
import com.google.common.collect.Lists;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Created by hucl on 17/1/5.
 */
@Data
public abstract class SmallClassEventCustomer {
    protected final Logger logger = LoggerFactory.getLogger("SmallClassEventCustomer");

    public final String prefix = "INIT_";

    protected PublicClassInfoStatusEnum smallClassCardStatus;

    protected abstract WorkOrderService getWorkOrderService();

    protected abstract void postHandle(SmallClass smallClass);

    protected abstract SmallClassLogService getSmallClassLogService();


    public void exec(SmallClassEvent smallClassEvent) {
        SmallClass smallClass = smallClassEvent.getSource();
        this.execute(smallClass);
        this.postHandle(smallClass);
    }

    public abstract void execute(SmallClass smallClass);

    public List<WorkOrder> filterStudentActed(List<WorkOrder> workOrders) {
        return workOrders
                .stream()
                .filter(workOrder -> this.getSmallClassLogService().studentActed(workOrder))
                .collect(Collectors.toList());
    }

    public void stuWriteStatusBack2Card(SmallClass smallClass, FishCardStatusEnum fishCardStatusEnum, WorkOrder workOrder) {
        List<WorkOrder> workOrders = Lists.newArrayList();
        workOrders.add(workOrder);

        this.writeStatusBack2Card(smallClass, workOrders, fishCardStatusEnum, false);
    }

    public void writeStatusBack2Card(SmallClass smallClass, FishCardStatusEnum fishCardStatusEnum) {
        this.writeStatusBack2Card(smallClass, fishCardStatusEnum, false);
    }

    public void writeStatusBack2Card(SmallClass smallClass, FishCardStatusEnum fishCardStatusEnum, boolean filterStuntActed) {
        List<WorkOrder> workOrders = this.getWorkOrders(smallClass);

        logger.debug("@writeStatusBack2Card,smallclass[{}],workorders[{}]"
                , JacksonUtil.toJSon(smallClass), JacksonUtil.toJSon(workOrders));

        this.writeStatusBack2Card(smallClass, workOrders, fishCardStatusEnum, filterStuntActed);
    }

    public void writeStatusBack2Card(SmallClass smallClass, List<WorkOrder> workOrders
            , FishCardStatusEnum fishCardStatusEnum, boolean filterStuntActed) {

        if (filterStuntActed) {
            workOrders = this.filterStudentActed(workOrders);
        }

//        this.updateWorkStatuses(workOrders, fishCardStatusEnum);

        for (WorkOrder workOrder : workOrders) {
            if (Objects.isNull(smallClass.getWriteBackDesc())) {
                this.getWorkOrderService().saveStatusForCardAndSchedule(workOrder, fishCardStatusEnum);
            } else {
                String desc = smallClass.getWriteBackDesc();

                try {
                    this.getWorkOrderService().saveStatusForCardAndSchedule(
                            workOrder, desc, fishCardStatusEnum);
                } catch (BusinessException ex) {
                    if (workOrders.size() == 1) {
                        throw new BusinessException(ex);
                    }
                    continue;
                }
            }
        }
    }

    private List<WorkOrder> getWorkOrders(SmallClass smallClass) {
        List<WorkOrder> workOrders;//system
        if (smallClass.getStatus() < PublicClassInfoStatusEnum.STUDENT_ENTER.getCode()) {
            workOrders = this.getWorkOrderService().findBySmallClassId(smallClass.getId());
        }
        //student
        else if (smallClass.getStatus() < PublicClassInfoStatusEnum.TEACHER_CARD_VALIDATED.getCode()) {
            WorkOrder workOrder
                    = this.getWorkOrderService()
                    .findBySmallClassIdAndStudentId(
                            smallClass.getId(), smallClass.getStatusReporter());
            workOrders = Arrays.asList(workOrder);
        }
        //teacher
        else {
            workOrders = this.getWorkOrderService().findBySmallClassId(smallClass.getId());
        }
        return workOrders;
    }

    private void updateWorkStatuses(List<WorkOrder> workOrders, FishCardStatusEnum fishCardStatusEnum) {
        workOrders.forEach(workOrder -> workOrder.setStatus(fishCardStatusEnum.getCode()));
    }


}
