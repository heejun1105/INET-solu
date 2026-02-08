package com.inet.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 무선AP 목록 API 응답용 DTO.
 * WirelessAp 엔티티의 location, school 등 지연 로딩 필드를 노출하지 않음.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WirelessApSummaryDto {
    private Long apId;
    private Long schoolId;
    private Long locationId;  // classroom id
    private String newLabelNumber;
    private String deviceNumber;
    private LocalDate apYear;
    private String manufacturer;
    private String model;
    private String macAddress;
    private String prevLocation;
    private String prevLabelNumber;
    private String classroomType;
    private String speed;
}
