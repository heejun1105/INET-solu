package com.inet.repository;

import com.inet.entity.DisabledIp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisabledIpRepository extends JpaRepository<DisabledIp, Long> {

    List<DisabledIp> findBySchool_SchoolIdOrderByIpAddress(Long schoolId);

    Optional<DisabledIp> findBySchool_SchoolIdAndIpAddress(Long schoolId, String ipAddress);

    boolean existsBySchool_SchoolIdAndIpAddress(Long schoolId, String ipAddress);

    @Modifying
    @Transactional
    @Query("DELETE FROM DisabledIp d WHERE d.school.schoolId = :schoolId AND d.ipAddress = :ipAddress")
    int deleteBySchoolIdAndIpAddress(@Param("schoolId") Long schoolId, @Param("ipAddress") String ipAddress);
}
