package com.inet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 무선 AP 목록 API에서 테이블/카드 한 행에 필요한 데이터만 담는 DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WirelessApListDto {
    private Long apId;
    private String schoolName;
    private String locationName;
    private String classroomType;
    private String newLabelNumber;
    private String deviceNumber;
    private String apYearFormatted;
    private String manufacturer;
    private String model;
    private String macAddress;
    private String prevLocation;
    private String prevLabelNumber;
    private String speed;
}
