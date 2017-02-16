package com.boxfishedu.workorder.web.controller.callbacklog;

import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by hucl on 17/2/16.
 */
@RestController
@RequestMapping(value = "/callback/log")
public class CallBackLogController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    @RequestMapping(value = "/heartBeat", method = RequestMethod.POST)
    public Object heartBeat(@RequestBody Map<String, Object> param) {

        return JsonResultModel.newJsonResultModel(JacksonUtil.toJSon(param));

    }
}
