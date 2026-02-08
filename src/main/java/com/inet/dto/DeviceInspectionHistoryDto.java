package com.inet.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 장비 검사 이력 목록 뷰용 DTO.
 * inspection-history.html에서 사용하는 필드만 노출한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInspectionHistoryDto {
    private Long id;
    private LocalDateTime inspectionDate;
    private Long schoolId;
    private String schoolName;
    private Long inspectorId;
    private String inspectorName;
    private Integer confirmedCount;
    private Integer modifiedCount;
    private Integer unconfirmedCount;
    private Integer totalCount;
}
