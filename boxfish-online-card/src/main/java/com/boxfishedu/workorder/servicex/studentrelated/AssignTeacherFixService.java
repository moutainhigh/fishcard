package com.boxfishedu.workorder.servicex.studentrelated;

import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.Collections3;
import com.boxfishedu.workorder.dao.jpa.StStudentApplyRecordsJpaRepository;
import com.boxfishedu.workorder.entity.mysql.StStudentApplyRecords;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 指定老师修补
 * Created by jiaozijun on 16/12/14.
 */
@Component
public class AssignTeacherFixService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private AssignTeacherService assignTeacherService;

    @Autowired
    private StStudentApplyRecordsJpaRepository stStudentApplyRecordsJpaRepository;

    public void disableAssignWorkOrderOut(final Long workOrderId, final String reason) {
        threadPoolManager.execute(new Thread(() -> {
            assignTeacherService.disableAssignWorkOrderinner(workOrderId, reason);
        }));
    }


}
