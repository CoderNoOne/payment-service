
# Payment Service

## Czym jest payment-service?

Payment-service to mikroserwis odpowiedzialny za obsługę płatności.
Integruje się z bramką TPay i informuje inne serwisy o wyniku płatności.

* działa w Dockerze
* posiada własną bazę MySQL
* komunikuje się przez REST

---

## Jak działa płatność?

### 1. Inicjalizacja płatności

Użytkownik klika „Zapłać”.

Aplikacja wysyła żądanie:

> klient chce zapłacić 199 zł za zamówienie abc-123

Payment-service:

* rejestruje płatność w TPay
* otrzymuje link do płatności
* zapisuje w bazie status **PENDING**
* zwraca link do aplikacji

Użytkownik zostaje przekierowany na stronę TPay.

---

### 2. Realizacja płatności

Klient płaci na stronie TPay (np. BLIK).
Cała komunikacja odbywa się między TPay a bankiem — nasz serwis nie bierze w tym udziału.

---

### 3. Webhook od TPay

Po zakończeniu płatności TPay wysyła żądanie:
`POST /payments/notification`

#### Weryfikacja

* sprawdzamy podpis MD5 → czy to naprawdę TPay
* jeśli nie → zwracamy 400

#### Idempotencja

* jeśli płatność ma już status **PAID** → ignorujemy

#### Sprawdzenie statusu

* dodatkowo weryfikujemy transakcję w API TPay

#### Aktualizacja

* sukces → status **PAID**
* porażka → status **FAILED**

W tej samej transakcji zapisujemy zdarzenie do `outbox_events`.

---

### 4. Powiadomienie innych serwisów (Outbox Pattern)

Zamiast wysyłać HTTP od razu:

* zapisujemy zdarzenie w bazie (atomowo ze statusem płatności)

Osobny proces (`OutboxProcessor`):

* co kilka sekund pobiera zdarzenia `PENDING`
* wysyła je do `order-service`
* sukces → status `SENT`
* błąd → ponawia próbę
* po kilku próbach → `FAILED`

Dzięki temu żadne zdarzenie nie ginie.

---

## Cały przepływ

```
Klik „Zapłać”
    ↓
POST /payments/init
    ↓
Rejestracja w TPay + zapis PENDING
    ↓
Płatność na stronie TPay
    ↓
Webhook od TPay
    ↓
Weryfikacja + idempotencja
    ↓
Double-check w TPay
    ↓
Status: PAID / FAILED + zapis outbox
    ↓
OutboxProcessor
    ↓
Powiadomienie order-service
```















# Architektura Hexagonalna 

## 1. Po co?
Architektura hexagonalna (Ports & Adapters) oddziela **logikę biznesową** od **technologii**, co daje:
- łatwe testowanie bez infrastruktury,
- możliwość wymiany technologii bez zmian w logice,
- większą czytelność kodu,
- zależności skierowane zawsze **do centrum**.

---

## 2. Główna idea
W centrum znajduje się **czysta logika biznesowa**, która:
- nie zna baz danych ani protokołów komunikacji,
- komunikuje się ze światem przez **porty**,
- korzysta z technologii poprzez **adaptery**.

---

## 3. Elementy

### Porty
- **Port wejściowy (driving)** — operacje oferowane światu; implementuje je serwis aplikacyjny.
- **Port wyjściowy (driven)** — wymagania wobec infrastruktury; implementują je adaptery.

### Adaptery
- **Wejściowe** — tłumaczą żądania (HTTP, kolejka, CLI) na wywołania portów wejściowych.
- **Wyjściowe** — implementują porty wyjściowe (baza danych, API, pamięć).

---

## 4. Zasada zależności
**Wszystkie zależności wskazują do domeny.**

- Domena nie zależy od niczego.
- Serwis aplikacyjny zależy tylko od portów.
- Adaptery zależą od portów, nigdy odwrotnie.

---

## 5. Korzyści
- **Testowalność** — logika testowana w izolacji.
- **Wymienialność technologii** — zmieniasz adapter, nie logikę.
- **Czytelność** — domena to czysta dokumentacja biznesu.
- **Izolacja zmian** — technologia nie wpływa na logikę.

---

