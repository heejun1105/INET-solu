# INET

학교 네트워크 장비 관리 시스템

> 교육기관의 네트워크 장비를 체계적으로 관리하고 추적할 수 있는 통합 관리 시스템

## 📋 목차

- [프로젝트 소개](#-프로젝트-소개)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [프로젝트 구조](#-프로젝트-구조)
- [시작하기](#-시작하기)
- [배포](#-배포)
- [문서](#-문서)

## 🎯 프로젝트 소개

INET은 교육기관의 네트워크 장비(컴퓨터, 모니터, 무선AP 등)를 효율적으로 관리하기 위한 웹 기반 관리 시스템입니다. 학교별, 교실별로 장비를 분류하고 추적하며, QR 코드를 통한 빠른 식별과 평면도를 통한 시각적 관리가 가능합니다.

### 주요 특징

- 🏫 **다중 테넌트 아키텍처**: 여러 학교의 데이터를 단일 시스템에서 안전하게 관리
- 🔐 **세밀한 권한 관리**: 역할 기반 접근 제어 및 학교별 권한 분리
- 📊 **대량 데이터 처리**: 엑셀 업로드를 통한 일괄 등록 및 배치 처리
- 🎨 **평면도 시각화**: HTML5 Canvas 기반 실시간 평면도 편집 및 PPT 내보내기
- 📱 **QR 코드**: 장비 고유번호 QR 생성, 모바일 카메라로 스캔 후 즉시 검색

## ✨ 주요 기능

### 1. 장비 관리
- 장비 등록/수정/삭제
- 동적 관리번호 및 고유번호 자동 생성
- 장비 목록 조회 및 다중 필터링 (학교/교실/유형/검색어)
- 페이징 처리 및 교실별 그룹화
- 엑셀 업로드/다운로드
- 장비 검사 모드 및 상태 관리
- 장비 이력 추적
- **QR 코드 스캔 검색**: 모바일/태블릿에서 카메라로 QR 스캔 시 즉시 검색

### 2. 무선AP 관리
- 무선AP 등록/수정/삭제
- 교실별 배치 관리
- 엑셀 업로드/다운로드
- 수정 이력 추적

### 3. 평면도 관리
- HTML5 Canvas 기반 실시간 평면도 편집
- 교실, 네트워크 장비, 무선AP 배치
- 좌석 배치 및 레이아웃 관리
- 드래그 앤 드롭, 줌/팬 기능
- PPT 내보내기 기능

### 4. 학교/교실 관리
- 학교 정보 관리
- 교실 등록/수정/삭제
- 중복 교실 감지 및 병합
- 교실 순서 관리

### 5. QR 코드 생성
- 학교별 고유번호를 QR 코드로 변환
- A4 용지 최적화 엑셀 파일 생성
- 필터링된 장비만 선택하여 생성 가능

### 6. IP 대장
- 학교별 IP 주소 현황 조회
- IP 대역별 장비 배치 현황 시각화
- 엑셀 다운로드

### 7. 데이터 관리
- 선택적 데이터 삭제 (장비, 무선AP, 교실, 운영자 등)
- 기간별 이력 데이터 삭제
- 계층적 삭제 전략으로 데이터 무결성 보장

### 8. 사용자 및 권한 관리
- 사용자 회원가입 및 승인 시스템
- 역할 기반 접근 제어 (ADMIN, EMPLOYEE)
- 14개 기능별 세부 권한 관리
- 학교별 접근 권한 제어

## 🛠 기술 스택

### Backend
- **Java 21** - LTS 버전
- **Spring Boot 3.2.3** - 애플리케이션 프레임워크
- **Spring Data JPA** - 데이터 접근 계층
- **Spring Security** - 인증 및 인가
- **Spring AOP** - 권한 검사 및 관심사 분리
- **Flyway** - 데이터베이스 마이그레이션

### Database
- **MySQL 8** - 관계형 데이터베이스

### Frontend
- **Thymeleaf** - 서버 사이드 템플릿 엔진
- **HTML5 Canvas** - 평면도 편집기
- **JavaScript (ES6+)** - 클라이언트 사이드 로직
- **CSS3** - 스타일링

### Build & Tools
- **Gradle** - 빌드 도구
- **Lombok** - 보일러플레이트 코드 제거

### 주요 라이브러리
- **Apache POI 5.2.3** - 엑셀 파일 처리
- **Google ZXing** - QR 코드 생성
- **Cache2k** - 캐싱 라이브러리
- **jsQR** - 웹 카메라 QR 코드 스캔

## 📁 프로젝트 구조

```
INET/
├── src/
│   ├── main/
│   │   ├── java/com/inet/
│   │   │   ├── INETApplication.java      # 애플리케이션 진입점
│   │   │   ├── controller/              # REST API 및 페이지 컨트롤러
│   │   │   │   ├── DeviceController.java
│   │   │   │   ├── WirelessApController.java
│   │   │   │   ├── FloorPlanController.java
│   │   │   │   ├── SchoolController.java
│   │   │   │   ├── ClassroomController.java
│   │   │   │   ├── QrCodeController.java
│   │   │   │   ├── IpController.java
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   └── ...
│   │   │   ├── service/                 # 비즈니스 로직
│   │   │   │   ├── DeviceService.java
│   │   │   │   ├── WirelessApService.java
│   │   │   │   ├── FloorPlanService.java
│   │   │   │   ├── SchoolService.java
│   │   │   │   ├── QrCodeService.java
│   │   │   │   └── ...
│   │   │   ├── repository/              # 데이터 접근 계층 (JPA)
│   │   │   ├── entity/                  # JPA 엔티티
│   │   │   ├── dto/                     # 데이터 전송 객체
│   │   │   ├── config/                  # 설정 (Security, Views 등)
│   │   │   ├── aspect/                  # AOP (권한 검사)
│   │   │   └── util/                    # 유틸리티
│   │   └── resources/
│   │       ├── templates/               # Thymeleaf 템플릿
│   │       │   ├── device/              # 장비 관련 페이지
│   │       │   ├── wireless-ap/          # 무선AP 관련 페이지
│   │       │   ├── floorplan/            # 평면도 페이지
│   │       │   ├── school/               # 학교 관리
│   │       │   ├── classroom/            # 교실 관리
│   │       │   ├── admin/                # 관리자
│   │       │   ├── auth/                 # 로그인/회원가입
│   │       │   ├── ip/                   # IP 대장
│   │       │   ├── qr-code/              # QR 코드 생성
│   │       │   └── fragments/            # 공통 레이아웃
│   │       ├── static/                   # 정적 리소스
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   │   └── floorplan/        # 평면도 편집기 모듈
│   │       │   └── images/
│   │       ├── db/migration/             # Flyway 마이그레이션 (V1~V22)
│   │       └── application.properties
│   └── test/                             # 테스트 코드
├── docs/                                 # 프로젝트 문서
├── delivery/                             # 배포용 패키지
├── build.gradle
├── settings.gradle
└── README.md
```

## 🚀 시작하기

### 필수 요구사항

- Java 21 이상
- MySQL 8 이상
- Gradle 7.6 이상 (또는 Gradle Wrapper 사용)

### 설치 및 실행

1. **프로젝트 클론**
```bash
git clone https://github.com/your-username/INET.git
cd INET
```

2. **데이터베이스 설정**
```sql
CREATE DATABASE inet CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **환경 설정**

`src/main/resources/application.properties` 파일 수정:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/inet
spring.datasource.username=your_username
spring.datasource.password=your_password
```

4. **빌드**
```bash
./gradlew clean build
```

5. **실행**
```bash
./gradlew bootRun
```

6. **접속**
- 브라우저에서 `http://localhost:8082` 접속
- 기본 관리자 계정: `admin` / `admin123!` (초기 설정 후 변경 권장)

### 개발 환경 설정

```properties
# application.properties
spring.devtools.restart.enabled=true
spring.thymeleaf.cache=false
spring.jpa.show-sql=true
```

## 🌐 배포

### 현재 배포 환경

**Railway**에서 배포 중입니다. (유료 플랜)

- GitHub 연동 자동 배포
- MySQL 데이터베이스 연동
- SSL 자동 제공
- 환경 변수 기반 설정

### 프로덕션 설정

1. `application-prod.properties` 생성 (또는 `application-prod.properties.example` 참고)
2. 환경 변수 설정:
   - `SPRING_PROFILES_ACTIVE=prod`
   - `SPRING_DATASOURCE_URL`
   - `SPRING_DATASOURCE_USERNAME`
   - `SPRING_DATASOURCE_PASSWORD`
3. `server.port=${PORT:8082}` (Railway 등에서 PORT 자동 할당 시)

자세한 배포 방법은 [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md), [무료_배포_가이드.md](무료_배포_가이드.md) 참고

## 📚 문서

- [배포 가이드](DEPLOYMENT_GUIDE.md) - 서버 배포 상세 가이드
- [무료 배포 가이드](무료_배포_가이드.md) - 무료/유료 호스팅 플랫폼 배포 방법
- [페이지별 기술 설계](페이지별_기술_설계.md) - 각 페이지의 기술적 구현 상세
- [포트폴리오 기술 설계 요소](포트폴리오_기술_설계_요소.md) - 아키텍처 및 설계 패턴
- [docs/](docs/) - 추가 기술 문서

## 🔒 보안

- Spring Security 기반 인증/인가
- BCrypt 비밀번호 암호화
- 역할 기반 접근 제어 (RBAC)
- 학교별 데이터 분리
- SQL Injection 방지 (JPA 사용)
- XSS 방지 (Thymeleaf 자동 이스케이프)

## 📝 주요 엔티티

- **School** - 학교 정보
- **Classroom** - 교실 정보
- **Device** - 장비 정보
- **WirelessAp** - 무선AP 정보
- **Manage** - 관리번호
- **Uid** - 고유번호
- **Operator** - 운영자(담당자)
- **User** - 사용자 계정
- **Permission** - 권한 정보
- **FloorPlan** - 평면도 정보

## 🤝 기여

이 프로젝트는 개인 프로젝트입니다. 버그 리포트나 기능 제안은 이슈로 등록해주세요.

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 👤 개발자

개발 기간: 2024.06 ~ 2024.12 (6개월)

---

**INET** - 학교 네트워크 장비를 체계적으로 관리하세요.
