package com.inet.service;

import com.inet.entity.DisabledIp;
import com.inet.entity.School;
import com.inet.repository.DisabledIpRepository;
import com.inet.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisabledIpService {

    private final DisabledIpRepository disabledIpRepository;
    private final SchoolService schoolService;
    private final DeviceRepository deviceRepository;

    /**
     * 학교별 사용불가 IP 목록을 IP 주소 -> 사유 맵으로 반환
     */
    @Transactional(readOnly = true)
    public Map<String, String> getDisabledReasonsBySchool(Long schoolId) {
        if (schoolId == null) {
            return Map.of();
        }
        return disabledIpRepository.findBySchool_SchoolIdOrderByIpAddress(schoolId).stream()
                .collect(Collectors.toMap(DisabledIp::getIpAddress, d -> d.getReason() != null ? d.getReason() : ""));
    }

    /**
     * 옥텟과 숫자가 조합된 유효한 IP 형식인지 여부 (10.x.x.x, 각 옥텟 0-255).
     * USB, 미지정 등 텍스트는 false. 중복/사용불가 검사는 이 값이 true일 때만 수행.
     */
    public static boolean isValidIpFormat(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) return false;
        String s = ipAddress.trim();
        String[] parts = s.split("\\.");
        if (parts.length != 4) return false;
        if (s.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣].*") || s.toLowerCase().contains("usb")) return false;
        try {
            for (String p : parts) {
                int n = Integer.parseInt(p.trim());
                if (n < 0 || n > 255) return false;
            }
            return s.matches("^10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * IP 문자열을 정규화 (예: 10.101.36.008 → 10.101.36.8). 비교 시 사용.
     */
    public static String normalizeIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) return ipAddress;
        String[] parts = ipAddress.trim().split("\\.");
        if (parts.length != 4) return ipAddress.trim();
        try {
            return Integer.parseInt(parts[0]) + "." + Integer.parseInt(parts[1]) + "."
                + Integer.parseInt(parts[2]) + "." + Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            return ipAddress.trim();
        }
    }

    /**
     * 해당 학교의 특정 IP가 사용불가인지 여부 (입력 형식 10.101.36.008 / 10.101.36.8 동일 처리)
     */
    @Transactional(readOnly = true)
    public boolean isDisabled(Long schoolId, String ipAddress) {
        if (schoolId == null || ipAddress == null || ipAddress.isBlank()) {
            return false;
        }
        String normalized = normalizeIp(ipAddress);
        if (disabledIpRepository.existsBySchool_SchoolIdAndIpAddress(schoolId, normalized)) {
            return true;
        }
        return disabledIpRepository.findBySchool_SchoolIdOrderByIpAddress(schoolId).stream()
                .anyMatch(d -> normalizeIp(d.getIpAddress()).equals(normalized));
    }

    /**
     * IP를 사용불가로 지정 (사유 저장). 배정된 IP는 지정 불가.
     */
    @Transactional
    public void setDisabled(Long schoolId, String ipAddress, String reason) {
        School school = schoolService.findById(schoolId)
                .orElseThrow(() -> new IllegalArgumentException("학교를 찾을 수 없습니다: " + schoolId));
        if (ipAddress == null || ipAddress.isBlank()) {
            throw new IllegalArgumentException("IP 주소가 필요합니다.");
        }
        String ip = ipAddress.trim();
        if (deviceRepository.existsBySchool_SchoolIdAndIpAddress(schoolId, ip)) {
            throw new IllegalArgumentException("이미 장비에 배정된 IP는 사용불가로 지정할 수 없습니다.");
        }
        disabledIpRepository.findBySchool_SchoolIdAndIpAddress(schoolId, ip)
                .ifPresentOrElse(
                        existing -> {
                            existing.setReason(reason != null ? reason : "");
                            disabledIpRepository.save(existing);
                        },
                        () -> disabledIpRepository.save(new DisabledIp(school, ip, reason != null ? reason : ""))
                );
    }

    /**
     * 사용불가 해제
     */
    @Transactional
    public void removeDisabled(Long schoolId, String ipAddress) {
        if (schoolId == null || ipAddress == null || ipAddress.isBlank()) {
            return;
        }
        disabledIpRepository.deleteBySchoolIdAndIpAddress(schoolId, ipAddress.trim());
    }
}
