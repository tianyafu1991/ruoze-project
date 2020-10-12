package com.ruozedata.ruozeplatform.repository.cluster;

import com.ruozedata.ruozeplatform.domain.cluster.YARNSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface YarnSummaryRepository extends JpaRepository<YARNSummary,Long> {

    YARNSummary findTop1ByIsDeleteFalseAndCreateTimeLessThanEqualOrderByCreateTimeDesc(int time);
}
