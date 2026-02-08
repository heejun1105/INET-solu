# Railway에서 Dockerfile로 배포하기

지금은 **소스 코드만** 배포 중이라면, **Dockerfile**로 전환하면 엑셀 다운로드(폰트 라이브러리) 등이 포함된 환경으로 안정적으로 배포할 수 있습니다.

---

## 1. 준비 (로컬에서 할 일)

1. **Dockerfile**  
   프로젝트 루트(`INET-solu`)에 이미 있습니다.

2. **.dockerignore**  
   빌드 제외 파일이 설정되어 있어, 불필요한 파일이 이미지에 들어가지 않습니다.

3. **환경 변수**  
   DB URL, 비밀번호 등은 **Railway 대시보드**에서 설정합니다. Dockerfile에는 넣지 마세요.

---

## 2. Railway에서 Dockerfile 사용하도록 설정

### 2-1. 프로젝트 열기

1. [Railway 대시보드](https://railway.app/dashboard) 로그인
2. 해당 프로젝트(예: inet-solu-copy-production) 선택
3. **서비스(앱)** 클릭

### 2-2. 빌드 방식 변경

1. **Settings** 탭 클릭
2. **Build** 섹션 찾기
3. **Builder** 를 **Dockerfile** 로 변경  
   - 기본값: `Nixpacks` 또는 `Builder: Auto`  
   - 변경: **Dockerfile** 선택
4. **Dockerfile Path** 가 비어 있으면 그대로 두기 (기본값: `Dockerfile`)
5. **Start Command** / **Custom Start Command** / **Run Command** 가 있으면 **반드시 비우기**  
   - Nixpacks 사용 시 Railway가 `java -jar build/libs/INET-0.0.1-SNAPSHOT.jar` 같은 명령을 넣어 둔 경우가 있음  
   - Dockerfile 사용 시 JAR는 **/app/app.jar** 에만 있으므로, Start Command를 비워 두어 Dockerfile의 ENTRYPOINT(`java -jar /app/app.jar`)가 실행되도록 해야 함
6. **저장** 후 **Redeploy**

### 2-3. (선택) 루트 디렉터리

- 레포가 **monorepo**가 아니고, `INET-solu` 코드가 레포 루트라면 추가 설정 없음.
- 레포 루트가 다른 폴더이고 `INET-solu`가 그 안에 있다면:
  - **Root Directory** (또는 **Source**) 를 `INET-solu` 로 설정해, Railway가 그 안의 `Dockerfile` 을 사용하도록 합니다.

---

## 3. 환경 변수 (Railway)

Docker 이미지 안에서는 **DB 등은 환경 변수**로 넘깁니다.  
**Variables** 탭에서 다음을 설정하세요.

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `SPRING_DATASOURCE_URL` | MySQL JDBC URL | `jdbc:mysql://mysql.railway.internal:3306/inet` |
| `SPRING_DATASOURCE_USERNAME` | DB 사용자 | `root` |
| `SPRING_DATASOURCE_PASSWORD` | DB 비밀번호 | (Railway MySQL에서 확인) |
| `SPRING_PROFILES_ACTIVE` | 프로필 | `prod` (이미 Dockerfile 기본값) |

`PORT` 는 Railway가 자동으로 넣어 주므로 따로 설정하지 않아도 됩니다.

---

## 4. 배포

1. **Deploy** 버튼으로 배포 시작  
   또는 GitHub 연결 시 **푸시 시 자동 배포**
2. **Build** 로그에서 `Dockerfile` 로 빌드되는지 확인
3. 빌드 성공 후 **Deploy** 로그에서 앱 기동 확인
4. **공개 URL** 로 접속해 동작·엑셀 다운로드 테스트

---

## 5. 문제 해결

- **"Unable to access jarfile build/libs/INET-0.0.1-SNAPSHOT.jar"**  
  - Docker 이미지 안에는 `build/libs/` 경로가 없고, JAR는 **/app/app.jar** 에만 있습니다.  
  - **Settings → Deploy** (또는 **Build**) 에서 **Start Command** / **Custom Run Command** 를 찾아 **비우기** (삭제).  
  - 저장 후 **Redeploy** 하면 Dockerfile의 ENTRYPOINT(`java -jar /app/app.jar`)가 사용됩니다.

- **빌드 실패**  
  - 로그에서 `gradle clean bootJar` 단계 오류 확인  
  - 로컬에서 `./gradlew clean bootJar` 가 성공하는지 확인

- **엑셀 다운로드 500**  
  - Dockerfile에는 `freetype`, `fontconfig`, `font-dejavu` 가 포함되어 있음  
  - Dockerfile로 빌드된 이미지가 맞는지, **Settings → Builder: Dockerfile** 확인

- **DB 연결 실패**  
  - Railway MySQL 서비스와 같은 프로젝트에 있는지  
  - `SPRING_DATASOURCE_*` 변수가 **Variables** 에 올바르게 설정되었는지 확인

---

## 요약

1. Railway **Settings → Build → Builder** 를 **Dockerfile** 로 변경  
2. **Variables** 에 DB URL/계정/비밀번호 설정  
3. **Deploy** 로 재배포  
4. 배포 후 장비목록 엑셀 다운로드로 동작 확인  

이렇게 하면 **소스만 배포하던 방식**에서 **Dockerfile 기반 배포**로 전환된 상태로 Railway에 적용됩니다.
