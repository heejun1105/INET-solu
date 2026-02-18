package com.inet.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.inet.entity.School;
import com.inet.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.text.Collator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Log4j2
public class SchoolService {
    
    private final SchoolRepository schoolRepository;
    private final DataManagementService dataManagementService;
    
    // Create
    @Transactional
    public School saveSchool(School school) {
        log.info("Saving school: {}", school);
        return schoolRepository.save(school);
    }
    
    // Read (가나다순 정렬)
    public List<School> getAllSchools() {
        log.info("Getting all schools");
        Collator collator = Collator.getInstance(Locale.KOREAN);
        return schoolRepository.findAll().stream()
                .sorted((a, b) -> collator.compare(a.getSchoolName(), b.getSchoolName()))
                .collect(Collectors.toList());
    }
    
    public Optional<School> getSchoolById(Long id) {
        log.info("Getting school by id: {}", id);
        return schoolRepository.findById(id);
    }
    
    // Update
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public School updateSchool(School school) {
        log.info("Updating school: {}", school);
        return schoolRepository.save(school);
    }
    
    // Delete
    @Transactional
    public void deleteSchool(Long id) {
        log.info("Deleting school with id: {}", id);
        
        // 학교 존재 여부 확인
        School school = schoolRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학교입니다: " + id));
        
        // DataManagementService를 사용하여 학교와 관련된 모든 데이터를 안전하게 삭제
        try {
            dataManagementService.deleteSchoolData(id);
            log.info("Successfully deleted school and all related data for schoolId: {}", id);
        } catch (Exception e) {
            log.error("Error deleting school data for schoolId {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("학교 삭제 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public Optional<School> findById(Long schoolId) {
        return schoolRepository.findById(schoolId);
    }
    
    // 통계용 메서드
    public long countAllSchools() {
        return schoolRepository.count();
    }
} 