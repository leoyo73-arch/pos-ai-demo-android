# POS AI 데모 (PosAiDemo)

Android 채팅 화면에서 질문을 보내면 FastAPI 서버가 Gemini로 답변을 만들어 돌려주는 1주차 결과물입니다.

## 폴더 구성

```
PosAiDemo/
├─ app/       Android 앱 (Kotlin, MVVM, Retrofit, RecyclerView 채팅 화면)
└─ server/    Python 서버 (FastAPI + Gemini)
   ├─ main.py          /chat API (재시도·예비 모델·타임아웃 포함)
   ├─ list_models.py   사용 가능한 모델 이름 확인
   ├─ requirements.txt
   └─ .env.example     .env 작성 예시
```

## 1. 서버 실행

```
cd server
py -3.13 -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
```

`.env.example`을 복사해 `.env`로 이름을 바꾸고 API 키를 넣은 뒤, 모델 이름을 확인합니다.

```
python list_models.py
```

`.env`의 `GEMINI_MODEL`, `GEMINI_FALLBACK_MODEL`을 채우고 서버를 켭니다.

```
fastapi dev main.py
```

브라우저에서 http://127.0.0.1:8000/docs 를 열어 `/chat`이 동작하는지 먼저 확인하세요.

## 2. 앱 실행

1. Android Studio에서 **File → Open**으로 `PosAiDemo` 폴더를 엽니다.
2. Gradle Sync가 끝나면 에뮬레이터에서 실행합니다.
3. 질문을 입력하고 **보내기**를 누르면 서버 답변이 표시됩니다.

## 실제 폰으로 테스트할 때

1. 서버를 `fastapi dev main.py --host 0.0.0.0`으로 실행합니다.
2. `app/build.gradle.kts`의 `SERVER_URL`을 PC IP로 바꿉니다. (예: `http://192.168.0.10:8000/`)
3. PC와 폰이 같은 Wi-Fi에 있어야 하고, Windows 방화벽이 8000 포트를 허용해야 합니다.

## 요청·응답 확인

Logcat에서 `okhttp`로 검색하면 주고받은 JSON을 볼 수 있습니다.
