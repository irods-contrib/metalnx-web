package com.emc.metalnx.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.emc.metalnx.services.interfaces.MonitoringService;

@Controller
@RequestMapping(value = "/monitor")
public class MonitoringController {

    @Autowired
    MonitoringService monitoringService;

    @RequestMapping(value = "/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getMachineInfo(@RequestParam("host") String host, @RequestParam("infoType") String infoType) {
        return monitoringService.getDataFromHost(infoType, host);
    }

}
