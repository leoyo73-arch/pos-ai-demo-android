# POS AI 데모 — Android 앱

소상공인 POS 앱에 **AI 상담(RAG)**과 **업무 비서(에이전트)**를 붙인 포트폴리오 프로젝트의 Android 앱입니다.
서버 구조, 설계 이유, 측정 결과는 [pos-ai-server](https://github.com/leoyo73-arch/pos-ai-server)에 정리했습니다.

> 모든 매뉴얼과 매출 데이터는 가상입니다.

## 화면

| 상담 탭 | 비서 탭 | 승인 확인 창 |
|---|---|---|
| (스크린샷) | (스크린샷) | (스크린샷) |

- **상담 탭**: 매뉴얼 근거로 답하고, 답변 아래에 참고한 매뉴얼을 표시
- **비서 탭**: 매출 조회와 취소·환불 업무 지시. 승인 대기 건수를 탭 배지로 표시
- **승인 확인 창**: 거래번호·금액·주문을 보여 주고 사장님이 직접 승인 또는 거절. 결제 취소는 승인 버튼을 빨간색으로 강조
- **예시 질문 칩**, 오류 종류별 안내와 **다시 시도** 버튼

## 구조

```
ui        MainActivity (TabLayout + ViewPager2)
          └─ ChatFragment × 2 (상담 / 비서 모드)
viewmodel ChatViewModel (SavedStateHandle로 모드 전달, 탭마다 독립)
data      ChatApi (Retrofit) — /chat, /agent, /actions
```

### 설계 포인트

- **한 화면, 두 모드**: 같은 `ChatFragment`와 `ChatViewModel`을 모드만 바꿔 재사용합니다. 모드는 Fragment arguments → `SavedStateHandle`로 전달해 별도 Factory가 필요 없습니다.
- **승인 대기를 잃지 않기**: 대기 목록은 ViewModel에 두어 화면 회전에도 유지하고, 승인 요청이 실패하면 서버 기준으로 목록을 다시 불러옵니다. 비서 탭을 열 때도 서버에 남은 대기 건을 복원합니다.
- **한 번에 하나씩 확인**: 여러 건이면 확인 창을 차례로 띄우고, 창 밖을 눌러도 닫히지 않아 반드시 승인 또는 거절을 고르게 했습니다.
- **대화 이어 가기**: 앱 실행마다 세션 ID를 만들어 서버가 대화를 따로 기억하므로 "그 손님들한테 안내해 줘" 같은 지시가 이어집니다.
- **오류 안내**: 연결 실패, 응답 지연, 서버 오류(AI 사용량 초과 등)를 구분해 안내합니다.

## 실행

1. [pos-ai-server](https://github.com/leoyo73-arch/pos-ai-server) 서버를 실행합니다.
2. Android Studio에서 이 프로젝트를 열고 에뮬레이터에서 실행합니다.
3. 실제 기기는 `app/build.gradle.kts`의 `SERVER_URL`을 PC의 IP로 바꾸고, 같은 Wi-Fi에 연결합니다.

## 기술 스택

Kotlin · MVVM · Jetpack (ViewModel, LiveData, ViewPager2) · Retrofit · OkHttp · Material Components · ViewBinding

## 만든 사람

백종철 · Android 개발 16년+ (금융 결제 POS 앱 PM/PL) · jcwhite@daum.net
