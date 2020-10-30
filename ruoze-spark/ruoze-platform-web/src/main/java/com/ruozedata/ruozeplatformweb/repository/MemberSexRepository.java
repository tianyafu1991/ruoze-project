package com.ruozedata.ruozeplatformweb.repository;

import com.ruozedata.ruozeplatformweb.domain.MemberSex;
import com.ruozedata.ruozeplatformweb.id.MemberSexPK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberSexRepository extends JpaRepository<MemberSex, MemberSexPK> {
}
