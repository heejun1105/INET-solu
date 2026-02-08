package com.inet.dto;

import com.inet.entity.UserRole;
import com.inet.entity.UserStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자 화면(대시보드, 사용자 관리)용 사용자 요약 DTO.
 * 비밀번호/보안답변 등 민감한 필드는 포함하지 않는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDto {

    private Long id;
    private String username;
    private String name;
    private String organization;
    private String position;
    private String phoneNumber;

    private UserRole role;
    private UserStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}

