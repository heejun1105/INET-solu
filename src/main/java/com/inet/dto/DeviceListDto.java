package com.inet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 장비 목록 API에서 테이블/카드 한 행에 필요한 데이터만 담는 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceListDto {
    private Long deviceId;
    private String schoolName;
    private String uidDisplay;
    private String manageDisplay;
    private String type;
    private String operatorPosition;
    private String operatorName;
    private String manufacturer;
    private String modelName;
    private String purchaseDateFormatted;
    private String ipAddress;
    /** 교실 ID (배치/맵 등에서 classroomId 기준 매칭용) */
    private Long classroomId;
    private String classroomName;
    private String purpose;
    private String setType;
    private String note;
    private Boolean unused;
    /** 검사 모드일 때만 사용 (confirmed / modified / unchecked) */
    private String inspectionStatus;
}
