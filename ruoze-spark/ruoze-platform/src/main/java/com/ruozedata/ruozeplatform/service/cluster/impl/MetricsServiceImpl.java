package com.ruozedata.ruozeplatform.service.cluster.impl;

import com.ruozedata.ruozeplatform.domain.cluster.HDFSSummary;
import com.ruozedata.ruozeplatform.repository.cluster.HDFSSummaryRepository;
import com.ruozedata.ruozeplatform.service.cluster.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MetricsServiceImpl implements MetricsService {

    @Autowired
    private HDFSSummaryRepository hdfsSummaryRepository;

    @Override
    public void addHDFSSummary(HDFSSummary hdfsSummary) {
        hdfsSummaryRepository.save(hdfsSummary);
    }
}
