package com.inet.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 미배치 교실 API 응답용 DTO.
 * Classroom 엔티티를 그대로 반환하면 school/devices 지연 로딩으로 인해
 * 배포 환경에서 LazyInitializationException 등이 발생할 수 있어, 필요한 필드만 담아 반환한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnplacedClassroomDto {
    private Long classroomId;
    private String roomName;
    private Integer xCoordinate;
    private Integer yCoordinate;
    private Integer width;
    private Integer height;
    private Integer displayOrder;
}
