package com.inet.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 미배치 교실 API 전체 응답용 DTO.
 * Map&lt;String, Object&gt; 대신 타입이 정해진 DTO로 반환하여 API 스펙을 명확히 한다.
 */
@Getter
@Setter
@NoArgsConstructor
public class UnplacedClassroomsResponseDto {
    private boolean success;
    private String message;
    private List<UnplacedClassroomDto> classrooms;

    public UnplacedClassroomsResponseDto(boolean success, String message, List<UnplacedClassroomDto> classrooms) {
        this.success = success;
        this.message = message;
        this.classrooms = classrooms;
    }
}
