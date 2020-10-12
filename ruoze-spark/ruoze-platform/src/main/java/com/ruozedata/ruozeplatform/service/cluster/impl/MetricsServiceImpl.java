package com.ruozedata.ruozeplatform.service.cluster.impl;

import com.ruozedata.ruozeplatform.domain.cluster.HDFSSummary;
import com.ruozedata.ruozeplatform.domain.cluster.YARNSummary;
import com.ruozedata.ruozeplatform.repository.cluster.HDFSSummaryRepository;
import com.ruozedata.ruozeplatform.repository.cluster.YarnSummaryRepository;
import com.ruozedata.ruozeplatform.service.cluster.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MetricsServiceImpl implements MetricsService {

    @Autowired
    private HDFSSummaryRepository hdfsSummaryRepository;

    @Autowired
    private YarnSummaryRepository yarnSummaryRepository;

    @Override
    public void addHDFSSummary(HDFSSummary hdfsSummary) {
        hdfsSummaryRepository.save(hdfsSummary);
    }


    @Override
    public HDFSSummary findHDFSSummary(int time) {

        return hdfsSummaryRepository.findTop1ByIsDeleteFalseAndCreateTimeLessThanEqualOrderByCreateTimeDesc(time);
    }

    @Override
    public List<HDFSSummary> findHDFSSummaries(Integer start, Integer end) {
        return hdfsSummaryRepository.findByIsDeleteFalseAndCreateTimeBetweenOrderByCreateTimeAsc(start,end);
    }

    @Override
    public void addYarnSummary(YARNSummary yarnSummary) {
        yarnSummaryRepository.save(yarnSummary);
    }

    @Override
    public YARNSummary findYARNSummary(int time) {
        return yarnSummaryRepository.findTop1ByIsDeleteFalseAndCreateTimeLessThanEqualOrderByCreateTimeDesc(time);
    }
}
