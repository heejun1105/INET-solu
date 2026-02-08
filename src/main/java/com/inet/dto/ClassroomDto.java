package com.inet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 교실 선택/목록용 DTO.
 * 기존 JSON 구조(classroomId, roomName 등)를 유지해 JS 코드와 호환되도록 설계한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassroomDto {

    private Long classroomId;
    private String roomName;

    // 좌표/크기 정보는 현재 JS에서 직접 쓰지 않지만,
    // 향후 확장성을 위해 함께 포함해 둔다.
    private Integer xCoordinate;
    private Integer yCoordinate;
    private Integer width;
    private Integer height;

    /** 교실 관리 페이지 정렬 순서 (classroom/manage.html에서 사용) */
    private Integer displayOrder;
}

