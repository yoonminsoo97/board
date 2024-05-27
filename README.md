<div align="center">
  <h1>게시판 API 서버 프로젝트</h1>
  <h4>게시판 API 문서  <a href="https://yoonminsoo.site">바로가기</a></h4>
</div>

## 1. 사용 기술

### 1.1. 언어

- Java 17

### 1.2. 프레임워크

- Spring Framework

### 1.3. 라이브러리

- Spring Web MVC
- Spring Security
- Spring Data JPA
- Spring Rest Docs
- Querydsl
- Lombok
- Jakarta Validation API
- JJWT

### 1.4. 빌드

- Gradle

### 1.5. 데이터베이스

- Maraidb

## 2. 브랜치 전략 및 커밋 메시지 컨벤션

### 2.1. 브랜치 전략

Git-Flow를 참고했으며 프로젝트 규모에 맞춰 다음 3개의 브랜치만 사용했습니다.

main, develop, feature 브랜치를 사용했습니다.

- **main:** 프로젝트 배포 시 사용하는 브랜치입니다.
- **develop:** 다음 버전 개발을 위한 브랜치로 개발이 완료되면 main 브랜치로 병합됩니다.
- **feature:** 기능 하나를 개발하기 위한 브랜치로 develop 브랜치에서 분리됩니다. 기능 개발이 완료되면 다시 develop 브랜치로 병합됩니다.

### 2.2. 커밋 메시지 컨벤션

커밋 메시지 컨벤션은 **[Udacity Git Commit Message Style Guide](https://udacity.github.io/git-styleguide/)** 참고했습니다.

- **feat:** 새로운 기능
- **fix:** 버그 수정
- **docs:** 문서화 변경
- **style:** 코드 포맷, 세미콜론 누락 등
- **refactor:** 코드 리팩토링
- **test:** 테스트 추가, 테스트 코드 리팩토링
- **chore:** 빌드 작업, 패키지 관리자 구성 등 프로젝트의 기타 작업

## 3. ERD 설계

<div align="center">
  <img width="800" alt="게시판_ERD" src="https://github.com/yoonminsoo97/board/assets/163730288/67f50a12-484c-4bef-9a11-c06c5c78a650">
</div>

