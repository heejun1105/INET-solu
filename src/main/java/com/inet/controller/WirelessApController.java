package com.inet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.inet.entity.WirelessAp;
import com.inet.entity.Classroom;
import com.inet.entity.School;
import com.inet.service.WirelessApService;
import com.inet.service.ClassroomService;
import com.inet.service.SchoolService;
import java.time.LocalDate;
import java.util.List;
import com.inet.entity.Feature;
import com.inet.entity.User;
import com.inet.service.PermissionService;
import com.inet.service.SchoolPermissionService;
import com.inet.service.UserService;
import com.inet.config.PermissionHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import java.io.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.LinkedHashMap;
import org.apache.poi.ss.util.CellRangeAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import com.inet.dto.WirelessApListDto;
import com.inet.dto.WirelessApListResponseDto;
import com.inet.dto.ClassroomDto;
import com.inet.dto.SchoolDto;
import com.inet.dto.WirelessApSummaryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/wireless-ap")
public class WirelessApController {

    private static final Logger log = LoggerFactory.getLogger(WirelessApController.class);

    private final WirelessApService wirelessApService;
    private final ClassroomService classroomService;
    private final SchoolService schoolService;
    private final PermissionService permissionService;
    private final SchoolPermissionService schoolPermissionService;
    private final UserService userService;
    private final PermissionHelper permissionHelper;
    private final com.inet.service.WirelessApHistoryService wirelessApHistoryService;

    public WirelessApController(WirelessApService wirelessApService,
                                ClassroomService classroomService,
                                SchoolService schoolService,
                                PermissionService permissionService,
                                SchoolPermissionService schoolPermissionService,
                                UserService userService,
                                PermissionHelper permissionHelper,
                                com.inet.service.WirelessApHistoryService wirelessApHistoryService) {
        this.wirelessApService = wirelessApService;
        this.classroomService = classroomService;
        this.schoolService = schoolService;
        this.permissionService = permissionService;
        this.schoolPermissionService = schoolPermissionService;
        this.userService = userService;
        this.permissionHelper = permissionHelper;
        this.wirelessApHistoryService = wirelessApHistoryService;
    }

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

    @GetMapping("/list")
    public String list(@RequestParam(value = "schoolId", required = false) Long schoolId,
                       @RequestParam(value = "classroomId", required = false) Long classroomId,
                       @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
                       Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        // 권한 체크 (학교별 권한 체크는 schoolId 또는 교실의 학교가 있을 때)
        User user;
        Long effectiveSchoolId = schoolId;
        if (classroomId != null) {
            Classroom classroom = classroomService.getClassroomById(classroomId)
                    .orElse(null);
            if (classroom != null && classroom.getSchool() != null) {
                effectiveSchoolId = classroom.getSchool().getSchoolId();
            }
        }
        if (effectiveSchoolId != null) {
            user = checkSchoolPermission(Feature.WIRELESS_AP_LIST, effectiveSchoolId, redirectAttributes);
        } else {
            user = checkPermission(Feature.WIRELESS_AP_LIST, redirectAttributes);
        }
        if (user == null) {
            return "redirect:/";
        }
        List<WirelessAp> wirelessAps;
        List<SchoolDto> schools = schoolPermissionService.getAccessibleSchools(user).stream()
                .map(s -> new SchoolDto(s.getSchoolId(), s.getSchoolName(), s.getIp()))
                .collect(Collectors.toList());
        
        if (classroomId != null) {
            Classroom classroom = classroomService.getClassroomById(classroomId)
                    .orElseThrow(() -> new RuntimeException("Classroom not found with id: " + classroomId));
            wirelessAps = wirelessApService.getWirelessApsByLocation(classroom);
            model.addAttribute("selectedSchoolId", classroom.getSchool() != null ? classroom.getSchool().getSchoolId() : null);
            model.addAttribute("selectedClassroomId", classroomId);
        } else if (schoolId != null) {
            School selectedSchool = schoolService.getSchoolById(schoolId)
                    .orElseThrow(() -> new RuntimeException("School not found with id: " + schoolId));
            wirelessAps = wirelessApService.getWirelessApsBySchool(selectedSchool);
            model.addAttribute("selectedSchoolId", schoolId);
            model.addAttribute("selectedClassroomId", null);
        } else {
            wirelessAps = wirelessApService.getAllWirelessAps();
            // 학교별로 정렬
            wirelessAps.sort((ap1, ap2) -> {
                String school1 = ap1.getSchool() != null ? ap1.getSchool().getSchoolName() : "미지정";
                String school2 = ap2.getSchool() != null ? ap2.getSchool().getSchoolName() : "미지정";
                int schoolComparison = school1.compareTo(school2);
                if (schoolComparison != 0) return schoolComparison;
                
                String location1 = ap1.getLocation() != null && ap1.getLocation().getRoomName() != null ? 
                                 ap1.getLocation().getRoomName() : "미지정 교실";
                String location2 = ap2.getLocation() != null && ap2.getLocation().getRoomName() != null ? 
                                 ap2.getLocation().getRoomName() : "미지정 교실";
                return location1.compareTo(location2);
            });
            model.addAttribute("selectedClassroomId", null);
        }
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            wirelessAps = wirelessApService.filterBySearchKeyword(wirelessAps, searchKeyword);
        }
        
        // 등록/수정 후 돌아올 때 필터 유지를 위한 현재 목록 URL
        String listUrl = request != null && request.getRequestURL() != null
                ? request.getRequestURL().toString() + (request.getQueryString() != null && !request.getQueryString().isEmpty() ? "?" + request.getQueryString() : "")
                : "/wireless-ap/list";
        String listUrlEncoded = "";
        try {
            listUrlEncoded = listUrl != null ? java.net.URLEncoder.encode(listUrl, java.nio.charset.StandardCharsets.UTF_8) : "";
        } catch (Exception e) {
            // ignore
        }
        
        // DTO 목록 및 API 응답 형식으로 전달
        List<WirelessApListDto> dtoList = wirelessApService.toListDtoList(wirelessAps);
        int total = dtoList.size();
        WirelessApListResponseDto listResponse = WirelessApListResponseDto.builder()
                .wirelessAps(dtoList)
                .currentPage(1)
                .pageSize(total > 0 ? total : 10)
                .totalPages(1)
                .startPage(1)
                .endPage(1)
                .listUrlEncoded(listUrlEncoded)
                .searchKeyword(searchKeyword != null ? searchKeyword : "")
                .build();
        
        model.addAttribute("wirelessApListResponse", listResponse);
        model.addAttribute("schools", schools);
        model.addAttribute("searchKeyword", searchKeyword != null ? searchKeyword : "");
        model.addAttribute("listUrl", listUrl);
        model.addAttribute("listUrlEncoded", listUrlEncoded);
        
        // 권한 정보 추가
        permissionHelper.addPermissionAttributes(user, model);
        
        return "wireless-ap/list";
    }

    @GetMapping("/register")
    public String registerForm(@RequestParam(required = false) String returnUrl, Model model, RedirectAttributes redirectAttributes) {
        // 권한 체크
        User user = checkPermission(Feature.WIRELESS_AP_MANAGEMENT, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }
        
        model.addAttribute("wirelessAp", new WirelessAp());
        model.addAttribute("schools", schoolPermissionService.getAccessibleSchools(user).stream()
                .map(s -> new SchoolDto(s.getSchoolId(), s.getSchoolName(), s.getIp()))
                .collect(Collectors.toList()));
        if (returnUrl != null && !returnUrl.isBlank()) {
            model.addAttribute("returnUrl", returnUrl);
        }
        // 권한 정보 추가
        permissionHelper.addPermissionAttributes(user, model);
        
        return "wireless-ap/register";
    }

    @PostMapping("/register")
    public String register(WirelessAp wirelessAp, 
                          @RequestParam("schoolId") Long schoolId,
                          @RequestParam(value = "locationId", required = false) Long locationId,
                          @RequestParam(value = "locationName", required = false) String locationName,
                          @RequestParam(value = "apYear", required = false) Integer apYear,
                          @RequestParam(required = false) String returnUrl,
                          RedirectAttributes redirectAttributes) {
        // 권한 체크 (학교별 권한 체크)
        User user = checkSchoolPermission(Feature.WIRELESS_AP_MANAGEMENT, schoolId, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }
        
        log.info("Registering wireless AP: {}", wirelessAp);
        
        // 학교 설정
        School school = schoolService.getSchoolById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found with id: " + schoolId));
        wirelessAp.setSchool(school);
        
        // APYear 설정 (년도만 입력받아서 LocalDate로 변환)
        if (apYear != null && apYear >= 1900 && apYear <= 2100) {
            wirelessAp.setAPYear(LocalDate.of(apYear, 1, 1));
        }
        
        // 교실 설정: 셀렉트(locationId) 우선, 없으면 직접입력(locationName)으로 찾기/생성
        if (locationId != null && locationId > 0) {
            Classroom classroom = classroomService.getClassroomById(locationId)
                    .orElseThrow(() -> new RuntimeException("Classroom not found with id: " + locationId));
            wirelessAp.setLocation(classroom);
        } else if (locationName != null && !locationName.trim().isEmpty()) {
            var existingClassroom = classroomService.findByRoomNameAndSchool(locationName.trim(), schoolId);
            Classroom classroom;
            if (existingClassroom.isPresent()) {
                classroom = existingClassroom.get();
            } else {
                classroom = new Classroom();
                classroom.setRoomName(locationName.trim());
                classroom.setSchool(school);
                classroom.setXCoordinate(0);
                classroom.setYCoordinate(0);
                classroom.setWidth(100);
                classroom.setHeight(100);
                classroom = classroomService.saveClassroom(classroom);
            }
            wirelessAp.setLocation(classroom);
        } else {
            throw new IllegalArgumentException("교실을 선택하거나 직접 입력해주세요.");
        }
        
        wirelessApService.saveWirelessAp(wirelessAp);
        if (returnUrl != null && !returnUrl.isBlank()) {
            try {
                String decoded = java.net.URLDecoder.decode(returnUrl, java.nio.charset.StandardCharsets.UTF_8);
                return "redirect:" + decoded;
            } catch (Exception e) {
                return "redirect:" + returnUrl;
            }
        }
        return "redirect:/wireless-ap/list";
    }

    @GetMapping("/modify/{id}")
    public String modifyForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        // 무선AP 조회
        WirelessAp wirelessAp = wirelessApService.getWirelessApById(id)
                .orElseThrow(() -> new RuntimeException("Wireless AP not found with id: " + id));
        
        // 권한 체크 (학교별 권한 체크)
        User user = checkSchoolPermission(Feature.WIRELESS_AP_MANAGEMENT, wirelessAp.getSchool().getSchoolId(), redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }
        
        model.addAttribute("wirelessAp", wirelessAp);
        model.addAttribute("schools", schoolPermissionService.getAccessibleSchools(user).stream()
                .map(s -> new SchoolDto(s.getSchoolId(), s.getSchoolName(), s.getIp()))
                .collect(Collectors.toList()));
        // 이전 목록 URL: 쿼리 파라미터 returnUrl 우선, 없으면 Referer 사용
        String returnUrlParam = request != null ? request.getParameter("returnUrl") : null;
        if (returnUrlParam != null && !returnUrlParam.isBlank()) {
            model.addAttribute("returnUrl", returnUrlParam);
        } else {
            String referer = request != null ? request.getHeader("Referer") : null;
            if (referer != null && !referer.contains("/wireless-ap/modify")) {
                model.addAttribute("returnUrl", referer);
            }
        }
        // 권한 정보 추가
        permissionHelper.addPermissionAttributes(user, model);
        
        return "wireless-ap/modify";
    }

    @PostMapping("/modify")
    public String modify(WirelessAp wirelessAp, 
                        @RequestParam("schoolId") Long schoolId,
                        @RequestParam(value = "locationId", required = false) Long locationId,
                        @RequestParam(value = "locationName", required = false) String locationName,
                        @RequestParam(value = "apYear", required = false) Integer apYear,
                        @RequestParam(required = false) String returnUrl,
                        RedirectAttributes redirectAttributes) {
        // 권한 체크 (학교별 권한 체크)
        User user = checkSchoolPermission(Feature.WIRELESS_AP_MANAGEMENT, schoolId, redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }
        
        log.info("Modifying wireless AP: {}", wirelessAp);
        
        // 학교 설정
        School school = schoolService.getSchoolById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found with id: " + schoolId));
        wirelessAp.setSchool(school);
        
        // APYear 설정 (년도만 입력받아서 LocalDate로 변환)
        if (apYear != null && apYear >= 1900 && apYear <= 2100) {
            wirelessAp.setAPYear(LocalDate.of(apYear, 1, 1));
        }
        
        // 교실 처리: 셀렉트 선택(locationId) 우선, 없으면 직접입력(locationName)으로 찾기/생성
        if (locationId != null && locationId > 0) {
            Classroom classroom = classroomService.getClassroomById(locationId)
                    .orElseThrow(() -> new RuntimeException("Classroom not found with id: " + locationId));
            wirelessAp.setLocation(classroom);
        } else if (locationName != null && !locationName.trim().isEmpty()) {
            var existingClassroom = classroomService.findByRoomNameAndSchool(locationName.trim(), schoolId);
            Classroom classroom;
            if (existingClassroom.isPresent()) {
                classroom = existingClassroom.get();
            } else {
                classroom = new Classroom();
                classroom.setRoomName(locationName.trim());
                classroom.setSchool(school);
                classroom.setXCoordinate(0);
                classroom.setYCoordinate(0);
                classroom.setWidth(100);
                classroom.setHeight(100);
                classroom = classroomService.saveClassroom(classroom);
            }
            wirelessAp.setLocation(classroom);
        }
        
        wirelessApService.updateWirelessApWithHistory(wirelessAp, user);
        redirectAttributes.addFlashAttribute("success", "무선 AP가 성공적으로 수정되었습니다.");
        if (returnUrl != null && !returnUrl.isBlank()) {
            try {
                String decoded = java.net.URLDecoder.decode(returnUrl, java.nio.charset.StandardCharsets.UTF_8);
                return "redirect:" + decoded;
            } catch (Exception e) {
                return "redirect:" + returnUrl;
            }
        }
        return "redirect:/wireless-ap/list";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam("ap_id") Long apId, RedirectAttributes redirectAttributes) {
        // 무선AP 조회
        WirelessAp wirelessAp = wirelessApService.getWirelessApById(apId)
                .orElseThrow(() -> new RuntimeException("Wireless AP not found with id: " + apId));
        
        // 권한 체크 (학교별 권한 체크)
        User user = checkSchoolPermission(Feature.WIRELESS_AP_MANAGEMENT, wirelessAp.getSchool().getSchoolId(), redirectAttributes);
        if (user == null) {
            return "redirect:/";
        }
        
        log.info("Removing wireless AP with id: {}", apId);
        wirelessApService.deleteWirelessAp(apId);
        return "redirect:/wireless-ap/list";
    }

    /** 무선 AP 목록 API (DTO 반환, 검색/필터 지원) */
    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<?> listApi(
            @RequestParam(value = "schoolId", required = false) Long schoolId,
            @RequestParam(value = "classroomId", required = false) Long classroomId,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "50") int size,
            HttpServletRequest request) {
        User user;
        Long effectiveSchoolId = schoolId;
        if (classroomId != null) {
            Classroom classroom = classroomService.getClassroomById(classroomId).orElse(null);
            if (classroom != null && classroom.getSchool() != null) {
                effectiveSchoolId = classroom.getSchool().getSchoolId();
            }
        }
        if (effectiveSchoolId != null) {
            user = checkSchoolPermission(Feature.WIRELESS_AP_LIST, effectiveSchoolId, new RedirectAttributesModelMap());
        } else {
            user = checkPermission(Feature.WIRELESS_AP_LIST, new RedirectAttributesModelMap());
        }
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<WirelessAp> wirelessAps;
        if (classroomId != null) {
            Classroom classroom = classroomService.getClassroomById(classroomId)
                    .orElseThrow(() -> new RuntimeException("Classroom not found with id: " + classroomId));
            wirelessAps = wirelessApService.getWirelessApsByLocation(classroom);
        } else if (schoolId != null) {
            School selectedSchool = schoolService.getSchoolById(schoolId)
                    .orElseThrow(() -> new RuntimeException("School not found with id: " + schoolId));
            wirelessAps = wirelessApService.getWirelessApsBySchool(selectedSchool);
        } else {
            wirelessAps = wirelessApService.getAllWirelessAps();
            wirelessAps.sort((ap1, ap2) -> {
                String school1 = ap1.getSchool() != null ? ap1.getSchool().getSchoolName() : "미지정";
                String school2 = ap2.getSchool() != null ? ap2.getSchool().getSchoolName() : "미지정";
                int schoolComparison = school1.compareTo(school2);
                if (schoolComparison != 0) return schoolComparison;
                String location1 = ap1.getLocation() != null && ap1.getLocation().getRoomName() != null ? ap1.getLocation().getRoomName() : "미지정 교실";
                String location2 = ap2.getLocation() != null && ap2.getLocation().getRoomName() != null ? ap2.getLocation().getRoomName() : "미지정 교실";
                return location1.compareTo(location2);
            });
        }
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            wirelessAps = wirelessApService.filterBySearchKeyword(wirelessAps, searchKeyword);
        }
        List<WirelessApListDto> dtoList = wirelessApService.toListDtoList(wirelessAps);
        int total = dtoList.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 1;
        int safePage = Math.max(1, Math.min(page, totalPages));
        int from = (safePage - 1) * size;
        int to = Math.min(from + size, total);
        List<WirelessApListDto> pageList = from < total ? dtoList.subList(from, to) : List.of();
        int startPage = Math.max(1, safePage - 2);
        int endPage = Math.min(totalPages, safePage + 2);
        // 수정/등록 후 복귀 시 필터 유지를 위해 쿼리 포함
        StringBuilder listUrlSb = new StringBuilder("/wireless-ap/list");
        if (schoolId != null) {
            listUrlSb.append("?schoolId=").append(schoolId);
        }
        if (classroomId != null) {
            listUrlSb.append(listUrlSb.indexOf("?") >= 0 ? "&" : "?").append("classroomId=").append(classroomId);
        }
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            try {
                listUrlSb.append(listUrlSb.indexOf("?") >= 0 ? "&" : "?").append("searchKeyword=")
                        .append(java.net.URLEncoder.encode(searchKeyword.trim(), java.nio.charset.StandardCharsets.UTF_8));
            } catch (Exception e) {
                listUrlSb.append(listUrlSb.indexOf("?") >= 0 ? "&" : "?").append("searchKeyword=").append(searchKeyword.trim());
            }
        }
        String listUrlEncoded = "";
        try {
            listUrlEncoded = java.net.URLEncoder.encode(listUrlSb.toString(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            // ignore
        }
        WirelessApListResponseDto response = WirelessApListResponseDto.builder()
                .wirelessAps(pageList)
                .currentPage(safePage)
                .pageSize(size)
                .totalPages(totalPages)
                .startPage(startPage)
                .endPage(endPage)
                .listUrlEncoded(listUrlEncoded)
                .searchKeyword(searchKeyword != null ? searchKeyword : "")
                .build();
        return ResponseEntity.ok(response);
    }

    // 학교별 교실 목록 조회 API
    @GetMapping("/api/classrooms/{schoolId}")
    @ResponseBody
    public List<ClassroomDto> getClassroomsBySchool(@PathVariable Long schoolId) {
        log.info("Getting classrooms for school id: {}", schoolId);
        return classroomService.findBySchoolId(schoolId).stream()
                .map(c -> new ClassroomDto(
                        c.getClassroomId(),
                        c.getRoomName(),
                        c.getXCoordinate(),
                        c.getYCoordinate(),
                        c.getWidth(),
                        c.getHeight(),
                        c.getDisplayOrder()
                ))
                .collect(Collectors.toList());
    }
    
    @GetMapping("/api/classrooms")
    @ResponseBody
    public List<ClassroomDto> getAllClassrooms() {
        log.info("Getting all classrooms for wireless-ap module");
        return classroomService.getAllClassrooms().stream()
                .map(c -> new ClassroomDto(
                        c.getClassroomId(),
                        c.getRoomName(),
                        c.getXCoordinate(),
                        c.getYCoordinate(),
                        c.getWidth(),
                        c.getHeight(),
                        c.getDisplayOrder()
                ))
                .collect(Collectors.toList());
    }

    /** MAC 주소 중복 체크 API (등록/수정 시 사용, excludeApId: 수정 시 제외할 AP ID) */
    @GetMapping("/api/check-mac")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkMacDuplicate(
            @RequestParam("value") String value,
            @RequestParam(value = "excludeApId", required = false) Long excludeApId) {
        Map<String, Boolean> result = new HashMap<>();
        result.put("duplicate", wirelessApService.existsByMacAddress(value, excludeApId));
        return ResponseEntity.ok(result);
    }

    /** 새 라벨 번호 중복 체크 API (등록/수정 시 사용, excludeApId: 수정 시 제외할 AP ID) */
    @GetMapping("/api/check-label")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkLabelDuplicate(
            @RequestParam("value") String value,
            @RequestParam(value = "excludeApId", required = false) Long excludeApId) {
        Map<String, Boolean> result = new HashMap<>();
        result.put("duplicate", wirelessApService.existsByNewLabelNumber(value, excludeApId));
        return ResponseEntity.ok(result);
    }
    
    // 학교별 무선AP 목록 조회 API (평면도 뷰어용)
    @GetMapping("/api/wireless-aps/school/{schoolId}")
    @ResponseBody
    public ResponseEntity<List<WirelessApSummaryDto>> getWirelessApsBySchoolId(@PathVariable Long schoolId) {
        try {
            log.info("Getting wireless APs for school id: {}", schoolId);
            
            School school = schoolService.getSchoolById(schoolId)
                    .orElseThrow(() -> new RuntimeException("School not found with id: " + schoolId));
            
            List<WirelessAp> wirelessAps = wirelessApService.getWirelessApsBySchool(school);
            
            log.info("Found {} wireless APs for school: {}", wirelessAps.size(), school.getSchoolName());
            List<WirelessApSummaryDto> dtoList = wirelessAps.stream()
                    .map(ap -> new WirelessApSummaryDto(
                            ap.getAPId(),
                            ap.getSchool() != null ? ap.getSchool().getSchoolId() : null,
                            ap.getLocation() != null ? ap.getLocation().getClassroomId() : null,
                            ap.getNewLabelNumber(),
                            ap.getDeviceNumber(),
                            ap.getAPYear(),
                            ap.getManufacturer(),
                            ap.getModel(),
                            ap.getMacAddress(),
                            ap.getPrevLocation(),
                            ap.getPrevLabelNumber(),
                            ap.getClassroomType(),
                            ap.getSpeed()
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtoList);
            
        } catch (Exception e) {
            log.error("Error getting wireless APs for school id: {}", schoolId, e);
            return ResponseEntity.status(500).build();
        }
    }

    // 엑셀 다운로드
    @GetMapping("/excel")
    public ResponseEntity<ByteArrayResource> downloadExcel(
            @RequestParam(value = "schoolId", required = false) Long schoolId,
            @RequestParam(value = "classroomId", required = false) Long classroomId,
            RedirectAttributes redirectAttributes) {
        
        try {
            // 권한 체크
            User user = checkPermission(Feature.WIRELESS_AP_LIST, redirectAttributes);
            if (user == null) {
                return ResponseEntity.status(401).build();
            }

            List<WirelessAp> wirelessAps;
            String fileName = "무선AP_목록";
            String titleSchoolName = ""; // 1행 제목용: 필터링된 학교명

            if (schoolId != null) {
                School school = schoolService.getSchoolById(schoolId)
                        .orElseThrow(() -> new RuntimeException("School not found"));
                wirelessAps = wirelessApService.getWirelessApsBySchool(school);
                titleSchoolName = school.getSchoolName() != null ? school.getSchoolName() : "";
                fileName += "_" + school.getSchoolName();
                
                if (classroomId != null) {
                    Classroom classroom = classroomService.getClassroomById(classroomId)
                            .orElseThrow(() -> new RuntimeException("Classroom not found"));
                    wirelessAps = wirelessAps.stream()
                            .filter(ap -> ap.getLocation() != null && ap.getLocation().getClassroomId().equals(classroomId))
                            .toList();
                    fileName += "_" + classroom.getRoomName();
                }
            } else {
                wirelessAps = wirelessApService.getAllWirelessAps();
                if (!wirelessAps.isEmpty() && wirelessAps.get(0).getSchool() != null) {
                    titleSchoolName = wirelessAps.get(0).getSchool().getSchoolName() != null ? wirelessAps.get(0).getSchool().getSchoolName() : "";
                }
            }

            fileName += "_" + LocalDate.now() + ".xlsx";

            // 엑셀 파일 생성
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Workbook workbook = new XSSFWorkbook();
            
            // 첫 번째 시트: 총괄표
            createSummarySheet(workbook, wirelessAps, titleSchoolName);
            
            // 두 번째 시트: 무선AP 목록
            Sheet sheet = workbook.createSheet("무선AP 목록");

            // 헤더 스타일
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // 데이터 스타일
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.CENTER);
            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // 1행: 필터링된 학교이름 + "무선 AP 현황" 표기
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(titleSchoolName + " 무선 AP 현황");
            titleCell.setCellStyle(headerStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10)); // A1:K1 병합

            // 2행: AP 정보의 마지막 수정일자 표기 (A2에만 표시)
            // AP 정보의 마지막 수정일자 조회 (모든 AP 중 가장 최근 수정일자)
            java.time.LocalDateTime lastModifiedDateTime = null;
            for (WirelessAp ap : wirelessAps) {
                Optional<java.time.LocalDateTime> lastModified = wirelessApHistoryService.getLastModifiedDate(ap);
                if (lastModified.isPresent()) {
                    if (lastModifiedDateTime == null || lastModified.get().isAfter(lastModifiedDateTime)) {
                        lastModifiedDateTime = lastModified.get();
                    }
                }
            }
            
            // 작성일자: 마지막 수정일자가 있으면 그것을 사용, 없으면 현재 날짜
            String dateStr;
            if (lastModifiedDateTime != null) {
                dateStr = lastModifiedDateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else {
                dateStr = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            
            // 작성일자 스타일 생성 (배경색 없음)
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setAlignment(HorizontalAlignment.RIGHT);
            Font dateFont = workbook.createFont();
            dateFont.setBold(true);
            dateStyle.setFont(dateFont);
            // 배경색 제거 (기본 스타일 유지)
            
            // 두번째 행: 작성일자 (오른쪽 끝에 배치)
            int lastCol = 10; // K열 (마지막 컬럼)
            Row dateRow = sheet.createRow(1);
            Cell dateLabelCell = dateRow.createCell(lastCol - 1); // 마지막 컬럼에서 두 번째
            dateLabelCell.setCellValue("작성일자");
            dateLabelCell.setCellStyle(dateStyle);
            
            Cell dateValueCell = dateRow.createCell(lastCol); // 마지막 컬럼
            dateValueCell.setCellValue(dateStr);
            dateValueCell.setCellStyle(dateStyle);

            // 세번째 행: 헤더 (학교컬럼 제거)
            Row headerRow = sheet.createRow(2);
            String[] headers = {"현위치", "교실구분", "신규라벨번호", "장비번호", "도입년도", "제조사", "모델", "MAC주소", "기존위치", "기존라벨번호", "속도"};
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000);
            }

            // 데이터 행 생성 (3행부터 시작)
            int rowNum = 3;
            for (WirelessAp ap : wirelessAps) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(ap.getLocation() != null ? ap.getLocation().getRoomName() : "");
                row.createCell(1).setCellValue(ap.getClassroomType() != null ? ap.getClassroomType() : "");
                row.createCell(2).setCellValue(ap.getNewLabelNumber() != null ? ap.getNewLabelNumber() : "");
                row.createCell(3).setCellValue(ap.getDeviceNumber() != null ? ap.getDeviceNumber() : "");
                row.createCell(4).setCellValue(ap.getAPYear() != null ? String.valueOf(ap.getAPYear().getYear()) : "");
                row.createCell(5).setCellValue(ap.getManufacturer() != null ? ap.getManufacturer() : "");
                row.createCell(6).setCellValue(ap.getModel() != null ? ap.getModel() : "");
                row.createCell(7).setCellValue(ap.getMacAddress() != null ? ap.getMacAddress() : "");
                row.createCell(8).setCellValue(ap.getPrevLocation() != null ? ap.getPrevLocation() : "");
                row.createCell(9).setCellValue(ap.getPrevLabelNumber() != null ? ap.getPrevLabelNumber() : "");
                row.createCell(10).setCellValue(ap.getSpeed() != null ? ap.getSpeed() : "");
                
                // 모든 셀에 스타일 적용
                for (int i = 0; i < 11; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }

            workbook.write(outputStream);
            workbook.close();

            ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());

            HttpHeaders headers_response = new HttpHeaders();
            headers_response.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            headers_response.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers_response.add(HttpHeaders.PRAGMA, "no-cache");
            headers_response.add(HttpHeaders.EXPIRES, "0");

            return ResponseEntity.ok()
                    .headers(headers_response)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);

        } catch (Exception e) {
            log.error("Error generating Excel file: ", e);
            return ResponseEntity.status(500).build();
        }
    }

    private void createSummarySheet(Workbook workbook, List<WirelessAp> wirelessAps, String titleSchoolName) {
        Sheet summarySheet = workbook.createSheet("총괄표");
        
        // 스타일 정의
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // 1행: 필터링된 학교이름 + "무선 AP" 표기
        Row titleRow = summarySheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue((titleSchoolName != null ? titleSchoolName : "") + " 무선 AP");
        titleCell.setCellStyle(headerStyle);
        summarySheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6)); // A1:G1 병합

        // AP 정보의 마지막 수정일자 조회 (모든 AP 중 가장 최근 수정일자)
        java.time.LocalDate lastModifiedDate = null;
        for (WirelessAp ap : wirelessAps) {
            Optional<java.time.LocalDateTime> lastModified = wirelessApHistoryService.getLastModifiedDate(ap);
            if (lastModified.isPresent()) {
                java.time.LocalDate apModifiedDate = lastModified.get().toLocalDate();
                if (lastModifiedDate == null || apModifiedDate.isAfter(lastModifiedDate)) {
                    lastModifiedDate = apModifiedDate;
                }
            }
        }
        
        // 작성일자: 마지막 수정일자가 있으면 그것을 사용, 없으면 현재 날짜
        String dateStr;
        if (lastModifiedDate != null) {
            dateStr = lastModifiedDate.toString();
        } else {
            dateStr = java.time.LocalDate.now().toString();
        }
        
        // 2행: 마지막 수정일자 표기 (A2에만 표시)
        Row dateRow = summarySheet.createRow(1);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue(dateStr);
        dateCell.setCellStyle(headerStyle);
        // A2에만 날짜 표시, 병합 제거

        // 3행: 빈 행 (AP 제목 제거)

        // 데이터 그룹핑 및 처리 (도입년도 옛날순 정렬)
        Map<String, Map<String, Map<String, Map<String, Integer>>>> groupedData = new LinkedHashMap<>();
        Set<String> allClassroomTypes = new HashSet<>();
        
        for (WirelessAp ap : wirelessAps) {
            String year = ap.getAPYear() != null ? ap.getAPYear().getYear() + "년" : "";
            String manufacturer = ap.getManufacturer() != null ? ap.getManufacturer() : "";
            String model = ap.getModel() != null ? ap.getModel() : "";
            String classroomType = ap.getClassroomType() != null ? ap.getClassroomType() : "";
            
            if (!classroomType.isEmpty()) {
                allClassroomTypes.add(classroomType);
            }
            
            groupedData.computeIfAbsent(year, k -> new LinkedHashMap<>())
                      .computeIfAbsent(manufacturer, k -> new LinkedHashMap<>())
                      .computeIfAbsent(model, k -> new LinkedHashMap<>())
                      .merge(classroomType, 1, Integer::sum);
        }

        // classroomType을 숫자 순으로 정렬
        List<String> sortedClassroomTypes = allClassroomTypes.stream()
            .sorted((t1, t2) -> {
                try {
                    int num1 = Integer.parseInt(t1);
                    int num2 = Integer.parseInt(t2);
                    return Integer.compare(num1, num2);
                } catch (NumberFormatException e) {
                    return t1.compareTo(t2);
                }
            })
            .collect(java.util.stream.Collectors.toList());

        // 도입년도 옛날순으로 정렬
        List<String> sortedYears = groupedData.keySet().stream()
            .filter(year -> !year.isEmpty())
            .sorted((y1, y2) -> {
                int year1 = Integer.parseInt(y1.replace("년", ""));
                int year2 = Integer.parseInt(y2.replace("년", ""));
                return Integer.compare(year1, year2);
            })
            .collect(java.util.stream.Collectors.toList());

        // 4행: 헤더 (동적으로 classroomType 컬럼 생성)
        Row headerRow = summarySheet.createRow(3);
        List<String> headers = new ArrayList<>();
        headers.add("도입년도");
        headers.add("제조사");
        headers.add("모델명");
        headers.add("수량합계");
        
        // classroomType별 헤더 추가
        for (String classroomType : sortedClassroomTypes) {
            headers.add(classroomType);
        }
        
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
            summarySheet.setColumnWidth(i, 4000);
        }

        // 데이터 행 생성 (4행부터 시작)
        int rowNum = 4;
        Map<String, Integer> totalByClassroomType = new HashMap<>();
        
        for (String year : sortedYears) {
            Map<String, Map<String, Map<String, Integer>>> yearData = groupedData.get(year);
            
            for (Map.Entry<String, Map<String, Map<String, Integer>>> manufacturerEntry : yearData.entrySet()) {
                String manufacturer = manufacturerEntry.getKey();
                
                for (Map.Entry<String, Map<String, Integer>> modelEntry : manufacturerEntry.getValue().entrySet()) {
                    String model = modelEntry.getKey();
                    Map<String, Integer> classroomCounts = modelEntry.getValue();
                    
                    Row row = summarySheet.createRow(rowNum++);
                    
                    row.createCell(0).setCellValue(year);
                    row.createCell(1).setCellValue(manufacturer);
                    row.createCell(2).setCellValue(model);
                    
                    // 수량합계 계산
                    int total = classroomCounts.values().stream().mapToInt(Integer::intValue).sum();
                    row.createCell(3).setCellValue(total);
                    
                    // classroomType별 데이터 입력
                    for (int i = 0; i < sortedClassroomTypes.size(); i++) {
                        String classroomType = sortedClassroomTypes.get(i);
                        int count = classroomCounts.getOrDefault(classroomType, 0);
                        row.createCell(4 + i).setCellValue(count);
                        
                        // 총계 누적
                        totalByClassroomType.merge(classroomType, count, Integer::sum);
                    }
                    
                    // 모든 셀에 스타일 적용
                    for (int i = 0; i < headers.size(); i++) {
                        row.getCell(i).setCellStyle(dataStyle);
                    }
                }
            }
        }

        // 총계 행
        Row totalRow = summarySheet.createRow(rowNum);
        totalRow.createCell(0).setCellValue("총계");
        totalRow.createCell(1).setCellValue("");
        totalRow.createCell(2).setCellValue("");
        
        // 수량합계 총계
        int grandTotal = totalByClassroomType.values().stream().mapToInt(Integer::intValue).sum();
        totalRow.createCell(3).setCellValue(grandTotal);
        
        // classroomType별 총계
        for (int i = 0; i < sortedClassroomTypes.size(); i++) {
            String classroomType = sortedClassroomTypes.get(i);
            int total = totalByClassroomType.getOrDefault(classroomType, 0);
            totalRow.createCell(4 + i).setCellValue(total);
        }
        
        // 총계 행에 스타일 적용
        for (int i = 0; i < headers.size(); i++) {
            totalRow.getCell(i).setCellStyle(dataStyle);
        }
    }
} 