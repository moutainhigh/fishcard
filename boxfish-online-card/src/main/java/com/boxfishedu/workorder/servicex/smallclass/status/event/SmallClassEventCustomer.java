package com.boxfishedu.workorder.servicex.smallclass.status.event;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.google.common.collect.Lists;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;


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


    public void exec(SmallClassEvent smallClassEvent) {
        SmallClass smallClass = smallClassEvent.getSource();
        this.execute(smallClass);
        this.postHandle(smallClass);
    }

    public abstract void execute(SmallClass smallClass);


    public void writeStatusBack2Card(SmallClass smallClass, FishCardStatusEnum fishCardStatusEnum) {
        List<WorkOrder> workOrders = this.getWorkOrders(smallClass);

        this.updateWorkStatuses(workOrders, fishCardStatusEnum);

        workOrders.forEach(workOrder -> {
            if (Objects.isNull(smallClass.getWriteBackDesc())) {
                this.getWorkOrderService().saveStatusForCardAndSchedule(workOrder);
            } else {
                this.getWorkOrderService().saveStatusForCardAndSchedule(
                        workOrder, smallClass.getWriteBackDesc());
            }
        });

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
