# OH100 — 알고리즘 문제 풀이 동기부여 앱

> solved.ac API + Firebase + Android 기반 문제풀이 관리 서비스  

## 1. 프로젝트 개요

OH100은 **solved.ac API**를 기반으로  
사용자의 **백준 문제 풀이 현황을 추적**하고,  
친구들의 활동을 함께 확인할 수 있는 **알고리즘 학습 동기부여 앱**입니다.

주요 특징:

-   solved.ac API 기반 유저 정보 조회  
-   매일 문제 풀이 여부 체크  
-   Firebase Cloud Messaging(FCM)을 이용한 푸시 알림  
-   SQLite + Cloud Firestore 데이터 관리  
-   앱 내부 타이머 기능 제공  
-   간단한 서버 프로그램을 통해 문제풀이 기록 검증

## 2. 기능 설명

### 친구 목록 (Main)

-   등록된 친구들의 solved.ac 정보를 표시  
-   내부 DB에 저장된 친구 ID + 문제 풀이 수 관리  
-   RecyclerView 기반 리스트 UI

### 유저 검색

-   solved.ac API로 유저 정보 조회  
-   Coil로 프로필 이미지 로드  
-   친구 등록 / 유저 등록 기능 수행

### 마이페이지

-   사용자 Baekjoon 아이디 등록 및 정보 표시  
-   ID 미설정 시 안내 메시지 출력  
-   ID 변경 기능 포함

### 타이머 기능

-   문제 풀이용 타이머 제공  
-   Coroutine 기반 백그라운드에서도 작동

### FCM 알림

-   매일 특정 시간(기본: 16시)  
-   사용자가 문제를 풀이했는지, 친구가 문제를 풀었는지 확인하여 알림
    전송  
-   Firestore에 토큰 및 참여 정보 저장

## 3. 기술 스택

### Android / Kotlin

-   Jetpack(Appcompat Theme, RecyclerView)  
-   Coroutine  
-   Retrofit + OkHttp3  
-   Coil 이미지 로딩  
-   SQLite

### Backend / Cloud

-   Firebase Cloud Firestore  
-   Firebase Cloud Messaging  
-   Custom Server for FCM Trigger

## 4. 주요 개발 내용

-   Coroutine 기반 타이머 및 네트워크 비동기 처리  
-   Retrofit 기반 solved.ac API 연동  
-   Jetpack 라이브러리 적극 활용  
-   SQLite 및 Firestore 동시 활용  
-   Firebase FCM + Solved.ac API 기능 구현

## 5. 마무리
알고리즘 학습을 꾸준히 이어가기 위한 동기부여 앱 OH100은 사용자가 매일 성장할 수 있도록 돕는 도구입니다.
함께 학습하며 발전해 나가길 바랍니다.
