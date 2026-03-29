package com.rzodeczko.paymentservice.application.port.input;

// Command = obiekt niosący dane potrzebne do wykonania operacji zmieniającej stan (write).
// Semantycznie oznacza żądanie wykonania akcji. Odróżnia się od Query (żądanie odczytu)
// i Event (coś co już się wydarzyło). NotificationCommand reprezentuje sytuację "chcę
// przetworzyć powiadomienie od TPay".
public record NotificationCommand(
        // Twój identyfikator sprzedawcy w TPay
        String merchantId,
        // ID transakcji po stronie TPay — to tym polem szukasz płatności w bazie
        // To jest Twój externalTransactionId w encji Payment. Bez tego nie wiesz której płatności
        // dotyczy webhook.
        String trId,
        // Data i czas transakcji
        String trDate,
        // W praktyce trCrc to dowolny string który Ty ustawiasz przy tworzeniu
        // transakcji i który TPay odsyła Ci bez zmian w webhooku.
        // Wyobraź sobie że tworzysz transakcję w TPay:
        // Ty → TPay: "stwórz transakcję na 99.99 PLN"
        // TPay → Ty: "ok, moje ID to tr_abc123"
        // Potem przychodzi webhook:
        // TPay → Ty: "transakcja tr_abc123 zapłacona"
        // Szukasz w bazie po externalTransactionId = "tr_abc123" i działa. Ale co jeśli
        // webhook przyszedł zanim zdążyłeś zapisać tr_abc123 do bazy? Albo chcesz od razu
        // wiedzieć którego zamówienia dotyczy bez dodatkowego zapytania?
        // Rozwiązanie — CRC jako Twój własny identyfikator
        String trCrc,
        // Kwota całego zamówienia
        String trAmount,
        // Kwota faktycznie zapłacona przez klienta
        String trPaid,
        // Opis transakcji który podałeś przy tworzeniu
        String trDesc,
        // Najważniejsze pole — wynik płatności -> "TRUE" albo "FALSE"
        String trStatus,
        // Kod błędu gdy płatność się nie powiodła
        String trError,
        // Email klienta który płacił
        String trEmail,
        // Podpis kryptograficzny całego webhooka
        String md5Sum
) { }

// Po co mi trId oraz trCrc (czym sie roznia)?
// trId — nadaje TPay. Ty nie masz na to wpływu.
// trCrc — nadajesz Ty przy tworzeniu transakcji. TPay tylko to przechowuje i odsyła.
