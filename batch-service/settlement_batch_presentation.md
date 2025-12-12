# 정산 배치 시스템 아키텍처

## 📌 개요

본 발표자료는 Spring Batch 기반의 **정산 배치 시스템**에 대해 설명합니다.
결제 데이터를 조회하여 정산 데이터를 생성하고, 수수료를 계산하는 일괄 처리 시스템입니다.

---

## 🏗️ 시스템 아키텍처

```mermaid
flowchart TB
    subgraph BatchService["배치 서비스"]
        direction TB
        Job["Settlement Job"]
        Step1["Step 1: 정산 생성"]
        Step2["Step 2: 수수료 계산"]
        Job --> Step1 --> Step2
    end
    
    subgraph PaymentService["결제 서비스"]
        PaymentAPI["GET /api/payments/settlement"]
        PaymentDB[("결제 DB")]
        PaymentAPI --> PaymentDB
    end
    
    subgraph Database["정산 DB"]
        SettlementDB[("Settlements 테이블")]
    end
    
    Step1 -->|"Feign Client 호출"| PaymentAPI
    Step1 -->|"INSERT"| SettlementDB
    Step2 -->|"SELECT PENDING"| SettlementDB
    Step2 -->|"UPDATE COMPLETED"| SettlementDB
```

---

## 📊 배치 Job 흐름

```mermaid
sequenceDiagram
    participant Scheduler as 스케줄러
    participant Job as Settlement Job
    participant Step1 as Step 1: 정산 생성
    participant Step2 as Step 2: 수수료 계산
    participant Payment as Payment Service
    participant DB as Settlement DB

    Scheduler->>Job: Job 실행 (매일 02:00)
    activate Job
    
    Job->>Step1: Step 1 시작
    activate Step1
    Step1->>Payment: 결제 데이터 조회 (Feign)
    Payment-->>Step1: 결제 목록 반환
    Step1->>Step1: SettlementModel 변환
    Step1->>DB: 정산 데이터 INSERT (PENDING)
    Step1-->>Job: Step 1 완료
    deactivate Step1
    
    Job->>Step2: Step 2 시작
    activate Step2
    Step2->>DB: PENDING 상태 조회
    DB-->>Step2: 정산 목록 반환
    Step2->>Step2: 수수료 계산 (3%)
    Step2->>DB: 상태 UPDATE (COMPLETED)
    Step2-->>Job: Step 2 완료
    deactivate Step2
    
    Job-->>Scheduler: Job 완료
    deactivate Job
```

---

## 🔄 재시도 전략

### Feign 클라이언트 재시도

```mermaid
flowchart LR
    subgraph FeignRetry["Feign 재시도 전략"]
        direction TB
        Call1["1차 시도"] --> |실패| Wait1["1초 대기"]
        Wait1 --> Call2["2차 시도"]
        Call2 --> |실패| Wait2["2초 대기"]
        Wait2 --> Call3["3차 시도"]
        Call3 --> |실패| Wait3["3초 대기"]
        Wait3 --> Call4["최종 시도"]
        Call4 --> |실패| Error["예외 발생"]
        Call1 --> |성공| Success["성공"]
        Call2 --> |성공| Success
        Call3 --> |성공| Success
        Call4 --> |성공| Success
    end
```

| 설정 항목 | 값 | 설명 |
|----------|-----|------|
| 최대 재시도 | 3회 | 초기 호출 포함 총 4회 시도 |
| 초기 대기 시간 | 1초 | 첫 번째 재시도 전 대기 |
| 최대 대기 시간 | 3초 | 재시도 간 최대 대기 시간 |
| 연결 타임아웃 | 5초 | 서버 연결 제한 시간 |
| 읽기 타임아웃 | 10초 | 응답 수신 제한 시간 |

---

### 배치 Step 재시도

```mermaid
flowchart TB
    subgraph BatchRetry["배치 Step 재시도 전략"]
        direction TB
        
        subgraph Step1Retry["Step 1: 정산 생성"]
            direction LR
            S1R1["재시도 대상 예외"]
            S1R2["FeignException<br/>RetryableException<br/>ConnectException<br/>SocketTimeoutException<br/>TransientDataAccessException"]
            S1R1 --- S1R2
        end
        
        subgraph Step2Retry["Step 2: 수수료 계산"]
            direction LR
            S2R1["재시도 대상 예외"]
            S2R2["TransientDataAccessException"]
            S2R1 --- S2R2
        end
        
        Config["공통 설정<br/>재시도 3회 / 스킵 10건"]
        Config --> Step1Retry
        Config --> Step2Retry
    end
```

---

## 📈 정산 데이터 상태 흐름

```mermaid
stateDiagram-v2
    [*] --> PENDING: Step 1 완료
    PENDING --> COMPLETED: Step 2 성공<br/>(수수료 계산 완료)
    PENDING --> FAILED: Step 2 실패<br/>(재시도 초과)
    COMPLETED --> [*]
    FAILED --> [*]
    
    note right of PENDING: 금액만 저장됨<br/>수수료 = 0
    note right of COMPLETED: 수수료 3% 적용<br/>순수익 계산됨
```

---

## 🛠️ 핵심 컴포넌트

### 1. SettlementCreateStep (정산 생성)

```mermaid
flowchart LR
    subgraph Reader["Reader"]
        R1["PaymentSettlementItemReader"]
        R2["Payment Service 호출"]
        R1 --> R2
    end
    
    subgraph Processor["Processor"]
        P1["SettlementCreateProcessor"]
        P2["PAID 상태만 필터링"]
        P3["SettlementModel 생성"]
        P1 --> P2 --> P3
    end
    
    subgraph Writer["Writer"]
        W1["JdbcBatchItemWriter"]
        W2["INSERT INTO settlements"]
        W1 --> W2
    end
    
    Reader --> Processor --> Writer
```

### 2. SettlementFeeStep (수수료 계산)

```mermaid
flowchart LR
    subgraph Reader["Reader"]
        R1["JdbcPagingItemReader"]
        R2["SELECT * FROM settlements<br/>WHERE status = 'PENDING'"]
        R1 --> R2
    end
    
    subgraph Processor["Processor"]
        P1["SettlementFeeProcessor"]
        P2["수수료 = 금액 × 3%"]
        P3["순수익 = 금액 - 수수료"]
        P1 --> P2 --> P3
    end
    
    subgraph Writer["Writer"]
        W1["JdbcBatchItemWriter"]
        W2["UPDATE settlements<br/>SET status = 'COMPLETED'"]
        W1 --> W2
    end
    
    Reader --> Processor --> Writer
```

---

## 🔧 기술 스택

| 구분 | 기술 |
|------|------|
| 프레임워크 | Spring Boot 3.5.7 |
| 배치 처리 | Spring Batch 5.x |
| HTTP 클라이언트 | OpenFeign |
| 데이터베이스 | MySQL / H2 (테스트) |
| 빌드 도구 | Gradle |
| Java 버전 | 21 (LTS) |

---

## 📋 수수료 계산 예시

```mermaid
flowchart LR
    subgraph Input["입력"]
        I1["결제 금액: 10,000원"]
    end
    
    subgraph Calculate["계산"]
        C1["수수료 = 10,000 × 3%<br/>= 300원"]
        C2["순수익 = 10,000 - 300<br/>= 9,700원"]
        C1 --> C2
    end
    
    subgraph Output["결과"]
        O1["금액: 10,000원<br/>수수료: 300원<br/>순수익: 9,700원<br/>상태: COMPLETED"]
    end
    
    Input --> Calculate --> Output
```

---

## 🚨 장애 대응 전략

```mermaid
flowchart TB
    subgraph ErrorHandling["장애 대응"]
        E1["예외 발생"]
        E2{"재시도 가능?"}
        E3["재시도 수행<br/>(최대 3회)"]
        E4{"성공?"}
        E5["처리 완료"]
        E6{"스킵 한도<br/>초과?"}
        E7["해당 건 스킵<br/>(최대 10건)"]
        E8["Step 실패 처리"]
        
        E1 --> E2
        E2 -->|Yes| E3
        E2 -->|No| E6
        E3 --> E4
        E4 -->|Yes| E5
        E4 -->|No| E6
        E6 -->|No| E7
        E7 --> E5
        E6 -->|Yes| E8
    end
```

| 장애 유형 | 대응 방식 |
|----------|----------|
| 네트워크 일시 장애 | Feign 재시도 (3회) + Step 재시도 (3회) |
| Payment Service 다운 | 연결 타임아웃 후 재시도, 최종 실패 시 스킵 |
| DB 락 충돌 | TransientDataAccessException 재시도 |
| 데이터 오류 | 해당 건 스킵 (최대 10건) |

---

## 📊 모니터링 포인트

- **Job 실행 상태**: COMPLETED / FAILED
- **처리 건수**: 읽기/쓰기/스킵 건수
- **실행 시간**: Step별 소요 시간
- **재시도 횟수**: Feign / Batch 재시도 로그

---

## 🎯 요약

> **정산 배치 시스템**은 결제 데이터를 기반으로 정산 데이터를 생성하고 수수료를 계산하는 **2-Step 배치 Job**입니다.

### 핵심 특징

1. ✅ **멀티 레벨 재시도**: Feign 클라이언트 + Batch Step 이중 보호
2. ✅ **Fault Tolerant**: 일부 실패해도 나머지 처리 계속
3. ✅ **청크 기반 처리**: 1,000건 단위로 트랜잭션 관리
4. ✅ **스케줄링**: 매일 02:00 자동 실행

---

*발표자료 생성일: 2025-12-11*
