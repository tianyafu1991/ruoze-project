package com.ruozedata.ruozeplatform.repository.cluster;

import com.ruozedata.ruozeplatform.domain.cluster.HDFSSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HDFSSummaryRepository extends JpaRepository<HDFSSummary,Long> {


    HDFSSummary findTop1ByIsDeleteFalseAndCreateTimeLessThanEqualOrderByCreateTimeDesc(Integer time);

    List<HDFSSummary> findByIsDeleteFalseAndCreateTimeBetweenOrderByCreateTimeAsc(Integer start,Integer end);

}
