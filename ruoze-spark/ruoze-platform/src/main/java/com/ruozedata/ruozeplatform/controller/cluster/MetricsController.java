package com.ruozedata.ruozeplatform.controller.cluster;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.ruozedata.ruozeplatform.domain.cluster.HDFSSummary;
import com.ruozedata.ruozeplatform.domain.cluster.YARNSummary;
import com.ruozedata.ruozeplatform.exception.ErrorCodes;
import com.ruozedata.ruozeplatform.exception.JsonData;
import com.ruozedata.ruozeplatform.service.cluster.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@RestController
@RequestMapping("/metrics")
public class MetricsController {


    @Autowired
    private MetricsService metricsService;


    @GetMapping("/hdfs")
    public Object getHDFSSummary() {
        HDFSSummary hdfsSummary = metricsService.findHDFSSummary(DateUtil.toIntSecond(new Date()));
        return JsonData.buildSuccess(hdfsSummary, ErrorCodes.SYSTEM_SUCCESS);
    }


    @GetMapping("/hdfs2")
    public Object getHDFSSummaryList() {
        long current = System.currentTimeMillis();
        long zero = current / (1000 * 3600 * 24) * (1000 * 3600 * 24) - TimeZone.getDefault().getRawOffset();

        List<HDFSSummary> hdfsSummaries = metricsService.findHDFSSummaries((int) (zero / 1000), (int) (current / 1000));
        return JsonData.buildSuccess(hdfsSummaries, ErrorCodes.SYSTEM_SUCCESS);
    }

    @GetMapping("/yarn")
    public Object getYARNSummary() {
        YARNSummary yarnSummary = metricsService.findYARNSummary(DateUtil.toIntSecond(new Date()));
        return JsonData.buildSuccess(yarnSummary, ErrorCodes.SYSTEM_SUCCESS);
    }

}
