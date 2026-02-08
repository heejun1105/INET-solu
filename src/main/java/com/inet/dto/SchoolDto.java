package com.inet.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 학교 API 응답용 DTO.
 * School 엔티티를 그대로 반환하지 않고 필요한 필드만 담아 반환한다.
 * JSON 필드명은 엔티티 직렬화와 동일(schoolId, schoolName, ip)하여 기존 클라이언트와 호환된다.
 */
@Getter
@Setter
@NoArgsConstructor
public class SchoolDto {
    private Long schoolId;
    private String schoolName;
    private Integer ip;

    public SchoolDto(Long schoolId, String schoolName, Integer ip) {
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        this.ip = ip;
    }
}
