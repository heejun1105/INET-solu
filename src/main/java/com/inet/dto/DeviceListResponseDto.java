package com.inet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * 장비 목록 API 전체 응답 DTO.
 * 페이지네이션·검사모드·목록 URL 등 클라이언트 렌더링에 필요한 정보 포함.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceListResponseDto {
    private List<DeviceListDto> devices;
    private int currentPage;
    private int pageSize;
    private int totalPages;
    private int startPage;
    private int endPage;
    private boolean sortByPurpose;
    /** 수정/삭제 후 돌아올 목록 URL (인코딩된 값) */
    private String listUrlEncoded;
    private boolean inspectionMode;
    /** deviceId -> inspection status (confirmed / modified / unchecked) */
    private Map<Long, String> inspectionStatuses;
    private String searchKeyword;
}
