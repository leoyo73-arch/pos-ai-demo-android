# POS AI 데모 (PosAiDemo)

소상공인 POS 앱에 AI 상담과 업무 비서를 붙인 포트폴리오 프로젝트입니다. 모든 매뉴얼과 매출 데이터는 가상입니다.

## 구성

```
PosAiDemo/
├─ app/       Android 앱 (Kotlin, MVVM, Retrofit) — 상담 탭 / 비서 탭
└─ server/    Python 서버 (FastAPI, Gemini, Chroma, LangGraph)
```

| 탭 | 하는 일 | 서버 API |
|---|---|---|
| 상담 | 가상 매뉴얼 근거로 답변(RAG), 참고한 매뉴얼 표시 | `POST /chat` |
| 비서 | 매출·거래 조회, 결제 취소·손님 안내 요청(에이전트) | `POST /agent` |
| 승인 | 취소·안내는 확인 창에서 사장님이 승인해야 실행 | `GET /actions`, `POST /actions/{id}` |

## 서버 실행

```
cd server
.venv\Scripts\activate
pip install -r requirements.txt
python ingest.py            # 매뉴얼 → Chroma
python make_sales_data.py   # 가상 매출 DB
fastapi dev main.py
```

`main.py`에 비서 API를 연결하는 두 줄이 있어야 합니다.

```python
from agent_api import router as agent_router
app.include_router(agent_router)
```

## 앱 실행

Android Studio에서 `PosAiDemo` 폴더를 열고 에뮬레이터에서 실행합니다. 실제 폰은 `app/build.gradle.kts`의 `SERVER_URL`을 PC IP로 바꾸고 서버를 `--host 0.0.0.0`으로 실행합니다.
