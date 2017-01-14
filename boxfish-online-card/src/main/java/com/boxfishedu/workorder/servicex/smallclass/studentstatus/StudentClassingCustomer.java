package com.boxfishedu.workorder.servicex.smallclass.studentstatus;

import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.servicex.smallclass.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.smallclass.event.StatusDealer;
import com.boxfishedu.workorder.servicex.smallclass.initstrategy.GroupInitStrategy;
import com.boxfishedu.workorder.servicex.smallclass.event.SmallClassEventCustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Created by hucl on 17/1/5.
 */
@Order(300)
@Component
public class StudentClassingCustomer extends SmallClassEventCustomer implements StatusDealer {

    @Autowired
    Map<String, GroupInitStrategy> groupInitStrategyMap;

    @PostConstruct
    public void initEvent() {
        this.setSmallClassCardStatus(PublicClassInfoStatusEnum.STUDENT_CLASSING);
    }

    @Override
    public void exec(SmallClassEvent smallClassEvent) {

    }
}
