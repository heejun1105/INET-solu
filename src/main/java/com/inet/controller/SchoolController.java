package com.inet.controller;

import com.inet.entity.School;
import com.inet.dto.SchoolDto;
import com.inet.service.SchoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;
import com.inet.entity.Feature;
import com.inet.entity.User;
import com.inet.service.PermissionService;
import com.inet.service.SchoolPermissionService;
import com.inet.service.UserService;
import com.inet.config.PermissionHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/school")
@RequiredArgsConstructor
@Slf4j
public class SchoolController {

    private final SchoolService schoolService;
    private final PermissionService permissionService;
    private final SchoolPermissionService schoolPermissionService;
    private final UserService userService;
    private final PermissionHelper permissionHelper;

    // 권한 체크 메서드
    private User checkPermission(Feature feature, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return null;
        }
        
        User user = userService.findByUsername(auth.getName())
            .orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "사용자를 찾을 수 없습니다.");
            return null;
        }
        
        return permissionHelper.checkFeaturePermission(user, feature, redirectAttributes);
    }
    
    // 학교 권한 체크 메서드
    private User checkSchoolPermission(Feature feature, Long schoolId, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return null;
        }
        
        User user = userService.findByUsername(auth.getName())
            .orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "사용자를 찾을 수 없습니다.");
            return null;
        }
        
        return permissionHelper.checkSchoolPermission(user, feature, schoolId, redirectAttributes);
    }

    // 학교 목록 API (평면도 페이지용) - DTO로 반환하여 엔티티 노출·지연로딩 방지
    @GetMapping("/api/schools")
    @ResponseBody
    public ResponseEntity<List<SchoolDto>> getAllSchoolsApi() {
        try {
            List<School> schools = schoolService.getAllSchools();
            List<SchoolDto> dtos = schools.stream()
                .map(s -> new SchoolDto(s.getSchoolId(), s.getSchoolName(), s.getIp()))
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("학교 목록 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/manage")
    public String manageSchools(Model model, RedirectAttributes redirectAttributes) {
        // 권한 체크
        User user = checkPermission(Feature.SCHOOL_MANAGEMENT, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }
        model.addAttribute("schools", schoolService.getAllSchools().stream()
                .map(s -> new SchoolDto(s.getSchoolId(), s.getSchoolName(), s.getIp()))
                .collect(Collectors.toList()));
        model.addAttribute("newSchool", new School());
        
        // 권한 정보 추가
        permissionHelper.addPermissionAttributes(user, model);
        
        return "school/manage";
    }

    @PostMapping("/add")
    public String addSchool(@ModelAttribute School school,
                           @RequestParam(required = false) Integer ip,
                           RedirectAttributes redirectAttributes) {
        // 권한 체크
        User user = checkPermission(Feature.SCHOOL_MANAGEMENT, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }
        try {
            if (school.getSchoolName() == null || school.getSchoolName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "학교명을 입력해주세요.");
                return "redirect:/school/manage";
            }
            if (ip != null && ip >= 0 && ip <= 255) {
                school.setIp(ip);
            }
            
            schoolService.saveSchool(school);
            redirectAttributes.addFlashAttribute("success", "학교가 성공적으로 추가되었습니다.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", com.inet.util.UserMessageUtils.toUserFriendly(e, "학교 추가"));
        }
        
        return "redirect:/school/manage";
    }

    @PatchMapping("/{id}/ip")
    @ResponseBody
    public ResponseEntity<?> updateSchoolIp(@PathVariable Long id,
                                            @RequestParam(required = false) Integer ip) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null || !permissionService.hasPermission(user, Feature.SCHOOL_MANAGEMENT)) {
            return ResponseEntity.status(403).build();
        }
        try {
            School school = schoolService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학교입니다."));
            school.setIp((ip != null && ip >= 0 && ip <= 255) ? ip : null);
            schoolService.updateSchool(school);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("학교 IP 수정 실패: schoolId={}", id, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteSchool(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        // 권한 체크
        User user = checkPermission(Feature.SCHOOL_MANAGEMENT, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }
        try {
            schoolService.deleteSchool(id);
            redirectAttributes.addFlashAttribute("success", "학교가 성공적으로 삭제되었습니다.");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", com.inet.util.UserMessageUtils.toUserFriendly(e, "학교 삭제"));
        }
        
        return "redirect:/school/manage";
    }

    @GetMapping("/count")
    @ResponseBody
    public long getSchoolCount() {
        return schoolService.countAllSchools();
    }
} 