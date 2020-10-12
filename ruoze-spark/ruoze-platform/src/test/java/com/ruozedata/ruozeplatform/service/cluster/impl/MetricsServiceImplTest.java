package com.ruozedata.ruozeplatform.service.cluster.impl;

import com.ruozedata.ruozeplatform.domain.cluster.HDFSSummary;
import com.ruozedata.ruozeplatform.domain.cluster.YARNSummary;
import com.ruozedata.ruozeplatform.service.cluster.MetricsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MetricsServiceImplTest {

    @Autowired
    private MetricsService metricsService;

    @Test
    public void findHDFSSummary() {
        final HDFSSummary hdfsSummary = metricsService.findHDFSSummary(1602424550);
        System.out.println(hdfsSummary);
    }

    @Test
    public void findHDFSSummaries() {
        final List<HDFSSummary> hdfsSummaries = metricsService.findHDFSSummaries(1602502913, 1602502928);
        for(HDFSSummary hdfsSummary : hdfsSummaries) {
            System.out.println(hdfsSummary);
        }
    }

    @Test
    public void findYARNSummary() {
        YARNSummary yarnSummary = metricsService.findYARNSummary((int) (System.currentTimeMillis() / 1000));
        System.out.println("================"+yarnSummary);
    }
}