package com.boxfishedu.workorder.servicex.smallclass.status.commonstatus;

import com.boxfishedu.workorder.common.bean.PublicClassInfoConstantStatus;
import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEventCustomer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by hucl on 17/1/5.
 */
@Order(PublicClassInfoConstantStatus.COURSE_ASSIGNED)
@Component
public class CourseAssignedCustomer extends SmallClassEventCustomer {
    @PostConstruct
    public void initEvent() {
        this.setSmallClassCardStatus(PublicClassInfoStatusEnum.COURSE_ASSIGNED);
    }

    @Override
    public void execute(SmallClass smallClass) {

    }
}
