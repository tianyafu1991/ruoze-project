package com.ruozedata.ruozeplatform.service.cluster;

import com.ruozedata.ruozeplatform.domain.cluster.HDFSSummary;
import com.ruozedata.ruozeplatform.domain.cluster.YARNSummary;

import java.util.List;

public interface MetricsService {

    void addHDFSSummary(HDFSSummary hdfsSummary);

    HDFSSummary findHDFSSummary(int time);

    List<HDFSSummary> findHDFSSummaries(Integer start, Integer end);


    void addYarnSummary(YARNSummary yarnSummary);

    YARNSummary findYARNSummary(int time);
}
