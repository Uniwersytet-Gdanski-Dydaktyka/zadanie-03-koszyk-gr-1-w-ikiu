[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/G5sYuaJg)
# Zadanie: Koszyk Internetowy

| Termin oddania | Punkty     |
|----------------|:-----------|
| 14.05.2026  23:00   |  20 (+3 extra)       |

--- 
Przekroczenie terminu o **n** zajęć wiąże się z karą:
- punkty uzyskania za realizację zadania są dzielone przez **2<sup>n</sup>**.

--- 

Twoja firma dostała zlecenie na stworzenie oprogramowania dla dużego sklepu internetowego *JavaMarkt*. 
Jedną z podstawowych funkcjonalności jest oprogramowanie koszyka zakupów, 
który potrafi uwzględniać różne promocje, rabaty oraz oferty specjalne, takie jak:
- jeśli wartość zamówienia jest większa niż 300 zł klient otrzymuje 5% zniżki na zakupione towary
- jeśli klient kupi 3 produkty to najtańszy z nich otrzymuje gratis (promocja 2+1)
- jeśli wartość zamówienia przekracza wartość 200 zł klient otrzymuje firmowy kubek gratis
- jednorazowy kupon rabatowy 30% na wybrany produkt
oraz wiele innych jeszcze nieznanych na tym etapie implementacji. 
Promocje mogą się zmieniać w trakcie działania programu, tj. mogą się pojawiać nowe a istniejące mogą znikać.

Towary w koszyku powinny być posegregowane malejąco według ceny, 
a potem według kolejności alfabetycznej nazw produktów. 
Sposób sortowania może produktów może się zmieniać w trakcie działania programu. 

Twoim zadaniem jest zaimplementowanie logiki, operującej na obiektach typu ``Product``, 
która umożliwiałaby:
1. Wyszukiwanie najtańszego/najdroższego produktu w zadanej kolekcji produktów
2. Wyszukiwanie n najtańszych/najdroższych produktów w zadanej kolekcji produktów
3. Sortowanie kolekcji produktów po cenie jak i po nazwie, a także otwarcie na sortowanie po dowolnych kryteriach w przyszłości
4. Wyliczanie sumy cen wszystkich zadanych produktów
5. Aplikowanie opisanych powyżej rodzajów promocji na zadanej kolekcji produktów w koszyku
6. Umożliwiać dodawanie nowych promocji, o jeszcze nieznanych cechach, z zachowaniem zasad SOLID
7. *(Zadanie dodatkowe, +3 pkt)* Proponować najkorzystniejszy sposób stosowania promocji — porównać wyniki zastosowania promocji w różnej kolejności i wybrać wariant najkorzystniejszy dla klienta.

Cechy związane z klasą ``Product``:
- kod produktu (code) - String
- nazwa produktu (name) - String
- cena produktu (price) – double
- cena produktu po uwzględnieniu promocji (discountPrice) - double

#### Uwaga o typie `double`
W tym zadaniu dla uproszczenia używamy typu ``double`` do przechowywania cen. 
Należy mieć świadomość, że w systemach produkcyjnych operujących na kwotach pieniężnych 
typ ``double`` jest **nieodpowiedni** ze względu na błędy precyzji arytmetyki zmiennoprzecinkowej 
(np. ``0.1 + 0.2 != 0.3``). W rzeczywistych aplikacjach finansowych stosuje się typ ``BigDecimal`` 
lub operuje na kwotach wyrażonych w groszach (typ ``long``).

W pierwszej wersji rozwiązania użyj **tablicy produktów** jako kolekcji, na której będziesz operował. 
Następnie dokonaj **refaktoryzacji** kodu, zamieniając tablicę na ``List<Product>``. 
Zwróć uwagę, jak zmiana struktury danych wpływa na pozostały kod -
to ćwiczenie z zasady *Open/Closed Principle* i programowania do interfejsów.

---

### Uwaga 1
Projekt powinien zawierać odpowiednie testy jednostkowe do implementowanej funkcjonalności.

### Uwaga 2
Implementując promocje, rozważ zastosowanie jednego z poniższych wzorców projektowych (lub ich kombinacji):

- **[Strategy](https://www.oodesign.com/strategy-pattern.html)** — każda promocja jako osobna strategia obliczania ceny. 
  Stosuj, gdy promocje wpływają na sposób wyliczania końcowej ceny koszyka i mogą być wymieniane dynamicznie.
- **[Command](https://www.oodesign.com/command-pattern.html)** — każda operacja na koszyku (dodanie produktu, zastosowanie promocji, cofnięcie promocji) jako osobne polecenie. 
  Stosuj, gdy chcesz umożliwić cofanie operacji (undo) lub kolejkowanie ich do późniejszego wykonania.

Uzasadnij swój wybór w komentarzu w kodzie lub w osobnym pliku ``DECISIONS.md``.

### Uwaga 3
Planując mechanizm sortowania zwróć uwagę na [Dependency Inversion Principle](https://www.oodesign.com/dependency-inversion-principle.html) oraz 
interfejsy dostępne w Java: ``Comparable`` oraz ``Comparator``.

### Uwaga 4
Zadbaj o poprawną **enkapsulację** — rozważ, czy klasa ``Product`` powinna być mutowalna, 
czy niemutowalna (immutable). Uzasadnij decyzję.

### Uwaga 5
Kod powinien poprawnie obsługiwać **sytuacje brzegowe**: 
pusty koszyk, produkty z ceną zerową, próba zastosowania promocji na pusty koszyk, ``null`` w kolekcji itp.

### Uwaga 6
Do rozwiązania dołącz **diagram klas UML** przedstawiający zaprojektowaną strukturę 
(w formacie graficznym lub tekstowym, np. PlantUML).
