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

