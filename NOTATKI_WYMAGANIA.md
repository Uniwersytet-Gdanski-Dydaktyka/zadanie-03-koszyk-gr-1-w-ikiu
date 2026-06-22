# Analiza wymagań — Koszyk Internetowy (JavaMarkt)

Poniżej zestawienie każdego wymagania z treści zadania oraz wskazanie, **gdzie i jak** jest ono zrealizowane w kodzie.

---

## 1. Promocje opisane w treści zadania

### 1.1 Wartość > 300 zł → 5% zniżki na wszystkie towary
**Plik:** `src/main/java/cart/promotions/ValueDiscountPromotion.java`

Klasa implementuje interfejs `Promotion`. Metoda `apply()` sumuje aktualne `discountPrice` wszystkich produktów i — jeśli suma przekracza `300.0` — tworzy nową listę, w której każdy produkt ma obniżoną cenę o 5% (mnożnik `0.95`). Sprawdzenie jest ścisłe: `total > MIN_VALUE`, co oznacza, że dokładnie 300 zł nie daje rabatu (test `shouldNotApplyValueDiscountWhenTotalAtOrBelowThreshold`).

### 1.2 Kup 3 produkty, najtańszy gratis (2+1)
**Plik:** `src/main/java/cart/promotions/BuyTwoGetOneFreePromotion.java`

Metoda `apply()` sortuje kopię koszyka malejąco po `discountPrice`, a następnie co trzeci element (indeks `i` taki, że `(i+1) % 3 == 0`) dostaje cenę `0.0` przez wywołanie `withDiscountPrice(0.0)`. Dla grup po 6 produktów gratis dostaną 2 najtańsze, itd. Jeśli koszyk ma mniej niż 3 produkty, lista wraca bez zmian.

### 1.3 Wartość > 200 zł → firmowy kubek gratis
**Plik:** `src/main/java/cart/promotions/FreeMugPromotion.java`

Metoda `apply()` sprawdza sumę `discountPrice` i obecność produktu o kodzie `MUG-01`. Jeśli suma > 200 i kubka jeszcze nie ma, dodaje nowy `Product("MUG-01", "firmowy kubek gratis", 0.0)` do listy. Dzięki sprawdzeniu kodu zapobiega duplikacji kubka przy wielokrotnym wywołaniu promocji.

### 1.4 Jednorazowy kupon 30% na wybrany produkt
**Plik:** `src/main/java/cart/promotions/CouponPromotion.java`

Klasa przyjmuje w konstruktorze `targetProductCode`. Metoda `apply()` iteruje po produktach i przy pierwszym trafieniu na produkt o tym kodzie aplikuje zniżkę (`discountPrice * 0.70`), po czym ustawia flagę `couponUsed = true` — kupon nie zostaje użyty po raz drugi, nawet jeśli koszyk zawiera dwa produkty o tym samym kodzie.

### 1.5 Promocje mogą się pojawiać i znikać dynamicznie
**Plik:** `src/main/java/cart/promotions/Promotion.java` (interfejs) + `src/main/java/cart/Cart.java` metoda `applyPromotion()`

Interfejs `Promotion` definiuje jeden kontrakt: `List<Product> apply(List<Product>)`. Metoda `Cart.applyPromotion(Promotion strategy)` przyjmuje **dowolną** implementację tego interfejsu — dzięki temu nowe promocje powstają przez dopisanie nowej klasy, bez modyfikowania `Cart`. Jest to realizacja zasady **OCP** (Open/Closed Principle).

---

## 2. Sortowanie towarów

### 2.1 Domyślny porządek: cena malejąco, przy remisie alfabetycznie po nazwie
**Plik:** `src/main/java/cart/ProductComparators.java`, pole `DEFAULT_ORDER`

```java
public static final Comparator<Product> DEFAULT_ORDER = Comparator
        .comparingDouble(Product::getPrice).reversed()
        .thenComparing(Product::getName);
```

Test: `shouldSortProductsByDefaultOrder` — dwa produkty w tej samej cenie 1000 zł, "playstation" jest przed "x-box".

### 2.2 Sortowanie może się zmieniać w czasie działania programu
**Plik:** `src/main/java/cart/Cart.java`, metoda `sort(Comparator<Product> comparator)`

Metoda przyjmuje dowolny `Comparator<Product>` — to realizacja **Dependency Inversion Principle** (Uwaga 3). Koszyk zależy od abstrakcji (`Comparator`), a nie od konkretnego sposobu porównywania. Dodatkowy przykład: `ProductComparators.BY_CODE` sortuje po kodzie produktu.

---

## 3. Wymagana logika operacji na kolekcji produktów

### 3.1 Wyszukiwanie najtańszego / najdroższego produktu
**Plik:** `src/main/java/cart/Cart.java`, metody `getCheapest()` i `getMostExpensive()`

Zwracają `Optional<Product>` — jeśli koszyk jest pusty, zwracają `Optional.empty()` zamiast rzucać wyjątek. Testy: `shouldReturnCheapestProduct`, `shouldReturnMostExpensiveProduct`, `shouldReturnEmptyOptionalWhenCartIsEmpty`.

### 3.2 Wyszukiwanie n najtańszych / najdroższych produktów
**Plik:** `src/main/java/cart/Cart.java`, metody `getCheapest(int n)` i `getMostExpensive(int n)`

Używają `Stream.sorted().limit(n)`. Dla `n <= 0` zwracają pustą listę. Gdy `n` przekracza rozmiar koszyka, zwracają wszystkie produkty (test `shouldReturnAllProductsWhenNExceedsSize`). Dla pustego koszyka: `shouldReturnEmptyListWhenGettingCheapestFromEmptyCart`.

### 3.3 Sortowanie po cenie, nazwie i dowolnych kryteriach
**Plik:** `src/main/java/cart/Cart.java` + `src/main/java/cart/ProductComparators.java`

`Cart.sort()` przyjmuje dowolny `Comparator<Product>`. `ProductComparators` dostarcza gotowych komparatorów (`DEFAULT_ORDER`, `BY_CODE`) i pokazuje wzorzec rozszerzania bez modyfikowania istniejącego kodu.

### 3.4 Wyliczanie sumy cen
**Plik:** `src/main/java/cart/Cart.java`, metoda `getTotalPrice()`

```java
return products.stream().mapToDouble(Product::getDiscountPrice).sum();
```

Sumuje `discountPrice` (cenę po zniżce), więc poprawnie uwzględnia nałożone promocje.

### 3.5 Aplikowanie promocji
**Plik:** `src/main/java/cart/Cart.java`, metoda `applyPromotion(Promotion strategy)`

Wywołuje `strategy.apply(this.products)` i zastępuje wewnętrzną listę produktów wynikiem. Jeśli koszyk jest pusty, metoda wraca natychmiast (obsługa sytuacji brzegowej z Uwagi 5).

### 3.6 Dodawanie nowych promocji z zachowaniem SOLID
**Plik:** `src/main/java/cart/promotions/Promotion.java`

Interfejs `Promotion` z jedną metodą `apply()` to główny punkt rozszerzalności. Każda nowa promocja to nowa klasa implementująca ten interfejs — bez modyfikowania `Cart`, `Product` ani żadnej istniejącej klasy (OCP). Metoda `Cart.applyPromotion` nie wie nic o konkretnych typach promocji (DIP).

### 3.7 Zadanie dodatkowe (+3 pkt): najkorzystniejszy sposób stosowania promocji
**Plik:** `src/main/java/cart/PromotionOptimizer.java` + `src/main/java/cart/OptimizationResult.java`

`PromotionOptimizer.getBestPrice(Cart cart, List<Promotion> promotions)` generuje wszystkie permutacje podanej listy promocji, dla każdej tworzy tymczasową kopię koszyka (ze świeżymi, niezniżkowanymi produktami), aplikuje promocje w danej kolejności i porównuje sumy. Zwraca `OptimizationResult` z najniższą ceną i odpowiadającą jej kolejnością promocji.

Test `shouldFindBestPromotionOrder` weryfikuje konkretny przypadek:
- Koszyk: 3 produkty za 100 + 100 + 150 = 350 zł
- Wariant 1 (optymalny): najpierw -5% → 332,5 zł, potem 2+1 zeruje najtańszy (95 zł) → **237,5 zł**
- Wariant 2: najpierw 2+1 → suma 250 zł, próg > 300 niespełniony więc -5% nie działa → 250 zł
- Wynik: `bestPrice == 237.5`

---

## 4. Cechy klasy Product

**Plik:** `src/main/java/cart/Product.java`

| Pole | Typ | Dostęp |
|------|-----|--------|
| `code` | `String` | `private final`, getter publiczny |
| `name` | `String` | `private final`, getter publiczny |
| `price` | `double` | `private final`, getter publiczny |
| `discountPrice` | `double` | `private final`, getter publiczny |

Klasa jest **niemutowalna** (Uwaga 4). Zmiana ceny po zniżce nie modyfikuje istniejącego obiektu — zamiast tego `withDiscountPrice(double)` zwraca nowy obiekt `Product` z nową wartością `discountPrice` (wzorzec "wither"). Uzasadnienie w `DECISIONS.md`: zapobiega błędom przy równoczesnym stosowaniu kilku promocji, które mogłyby nadpisywać sobie nawzajem ceny. `equals` i `hashCode` opierają się tylko na `code`.

---

## 5. Testy jednostkowe (Uwaga 1)

**Plik:** `test/java/cart/CartTest.java`

| Test | Co sprawdza |
|------|------------|
| `shouldAddProductToCart` | dodawanie produktu i podstawowe zliczanie |
| `shouldThrowExceptionWhenAddingNull` | sytuacja brzegowa: null |
| `shouldReturnEmptyListWhenGettingCheapestFromEmptyCart` | sytuacja brzegowa: pusty koszyk, `getCheapest(n)` |
| `shouldReturnEmptyListWhenGettingMostExpensiveFromEmptyCart` | sytuacja brzegowa: pusty koszyk, `getMostExpensive(n)` |
| `shouldReturnEmptyOptionalWhenCartIsEmpty` | sytuacja brzegowa: pusty koszyk, `getCheapest()` / `getMostExpensive()` |
| `shouldSortProductsByDefaultOrder` | sortowanie malejąco po cenie, alfabetycznie przy remisie |
| `shouldGetCheapestAndMostExpensiveProducts` | wyszukiwanie n najtańszych/najdroższych |
| `shouldReturnCheapestProduct` | wyszukiwanie 1 najtańszego |
| `shouldReturnMostExpensiveProduct` | wyszukiwanie 1 najdroższego |
| `shouldReturnAllProductsWhenNExceedsSize` | n większe niż rozmiar koszyka |
| `shouldHandleProductWithZeroPrice` | produkt z ceną 0 |
| `shouldApplyValueDiscountPromotion` | promocja 5% powyżej 300 zł |
| `shouldNotApplyValueDiscountWhenTotalAtOrBelowThreshold` | granica progu 300 zł |
| `shouldApplyFreeMugPromotion` | kubek gratis powyżej 200 zł |
| `shouldNotApplyFreeMugWhenTotalBelowThreshold` | brak kubka poniżej progu |
| `shouldApplyBuyTwoGetOneFreePromotion` | promocja 2+1, najtańszy gratis |
| `shouldApplyCouponPromotionToSingleProduct` | jednorazowy kupon 30%, tylko pierwszy trafiony produkt |
| `shouldFindBestPromotionOrder` | optymalizator kolejności promocji (+3 pkt) |

---

## 6. Wzorzec projektowy dla promocji (Uwaga 2)

**Pliki:** `src/main/java/cart/promotions/Promotion.java`, `DECISIONS.md`, `UML.puml` (nota na diagramie)

Wybrany wzorzec: **Strategy**. Uzasadnienie (szczegółowe w `DECISIONS.md`):
- Wzorzec Command służy głównie do undo/kolejkowania operacji — tu nie jest potrzebny.
- Strategy pozwala zamknąć każdy algorytm (rodzaj promocji) w osobnej klasie implementującej `Promotion`, a `Cart` może je stosować zamiennie bez wiedzy o typach — to czyste OCP.

---

## 7. Sortowanie a DIP i Comparator (Uwaga 3)

**Plik:** `src/main/java/cart/Cart.java` linia 34, `src/main/java/cart/ProductComparators.java`

`Cart.sort(Comparator<Product> comparator)` zależy od abstrakcji `Comparator` z JDK, a nie od konkretnych klas komparatorów. `ProductComparators` jest klasą pomocniczą z gotowymi stałymi — jej istnienie nie jest wymagane przez `Cart`, więc można dodawać nowe komparatory bez żadnych zmian w `Cart`.

---

## 8. Enkapsulacja i niemutowalność (Uwaga 4)

**Plik:** `src/main/java/cart/Product.java`, `DECISIONS.md`

Wszystkie pola `Product` są `private final`. Jedynym sposobem "zmiany" ceny jest wywołanie `withDiscountPrice()`, które zwraca nowy obiekt. `Cart.getProducts()` zwraca `Collections.unmodifiableList(products)` — lista wewnętrzna koszyka nie może być zmodyfikowana z zewnątrz.

---

## 9. Sytuacje brzegowe (Uwaga 5)

| Sytuacja | Gdzie obsłużona |
|----------|----------------|
| `addProduct(null)` | `Cart.java:21` — rzuca `IllegalArgumentException` |
| Pusty koszyk w `applyPromotion()` | `Cart.java:68` — wczesny return |
| Pusty koszyk w `getCheapest()` / `getMostExpensive()` | `Optional.empty()` dzięki Stream API |
| `getCheapest(n)` / `getMostExpensive(n)` z `n <= 0` | `Cart.java:50,58` — zwraca pustą listę |
| `getCheapest(n)` gdzie `n > size` | `limit(n)` na strumieniu naturalnie zwraca wszystkie |
| Produkt z ceną 0 | obsługiwany poprawnie, `getTotalPrice()` sumuje 0 |
| `FreeMugPromotion` przy ponownym wywołaniu | sprawdzenie `hasMug` zapobiega duplikacji |
| `CouponPromotion` z wieloma produktami o tym samym kodzie | flaga `couponUsed` gwarantuje jednorazowość |
| `PromotionOptimizer` z pustą listą promocji | `PromotionOptimizer.java:13` — zwraca aktualną cenę bez zmian |

---

## 10. Diagram UML (Uwaga 6)

**Plik:** `UML.puml` (format PlantUML)

Diagram zawiera:
- klasę `Product` z adnotacją o niemutowalności
- interfejs `Promotion` z adnotacją o wzorcu Strategy
- klasę `Cart` z wszystkimi metodami publicznymi
- cztery implementacje promocji (`ValueDiscountPromotion`, `BuyTwoGetOneFreePromotion`, `FreeMugPromotion`, `CouponPromotion`)
- klasę pomocniczą `ProductComparators`
- klasy zadania dodatkowego: `PromotionOptimizer` i `OptimizationResult`
- relacje: kompozycja koszyka z produktami, zależności użycia, implementacje interfejsu

---

## 11. Refaktoryzacja tablica → List (treść zadania)

Projekt używa `List<Product>` (konkretnie `ArrayList`) jako wewnętrznej kolekcji `Cart`. Jest to wynik refaktoryzacji opisanej w treści zadania. Interfejsy Stream API, `Collections.unmodifiableList`, `Comparator` oraz metody promotion działają na interfejsie `List`, co ułatwia ewentualną zmianę na inną implementację listy.

---

## Podsumowanie zgodności z wymaganiami

| Wymaganie | Status |
|-----------|--------|
| Promocja: wartość > 300 zł → 5% zniżki | ✅ `ValueDiscountPromotion` |
| Promocja: 2+1 gratis (najtańszy) | ✅ `BuyTwoGetOneFreePromotion` |
| Promocja: wartość > 200 zł → kubek gratis | ✅ `FreeMugPromotion` |
| Promocja: jednorazowy kupon 30% | ✅ `CouponPromotion` |
| Dynamiczne dodawanie/usuwanie promocji | ✅ interfejs `Promotion` + `applyPromotion()` |
| Sortowanie malejąco po cenie, alfa przy remisie | ✅ `ProductComparators.DEFAULT_ORDER` |
| Sortowanie wymienne w czasie działania | ✅ `Cart.sort(Comparator)` |
| Najtańszy / najdroższy produkt | ✅ `getCheapest()`, `getMostExpensive()` |
| n najtańszych / najdroższych | ✅ `getCheapest(int)`, `getMostExpensive(int)` |
| Suma cen | ✅ `getTotalPrice()` |
| Aplikowanie promocji | ✅ `applyPromotion(Promotion)` |
| Rozszerzalność promocji (SOLID) | ✅ interfejs `Promotion`, zasada OCP |
| **Zadanie dodatkowe: optymalizacja kolejności** | ✅ `PromotionOptimizer` + `OptimizationResult` |
| Pola klasy `Product` zgodne ze specyfikacją | ✅ `code`, `name`, `price`, `discountPrice` |
| Testy jednostkowe | ✅ 18 testów w `CartTest.java` |
| Wzorzec Strategy + uzasadnienie | ✅ `DECISIONS.md` |
| DIP w sortowaniu | ✅ `Cart.sort(Comparator)` |
| Niemutowalność `Product` + uzasadnienie | ✅ `Product.withDiscountPrice()`, `DECISIONS.md` |
| Obsługa sytuacji brzegowych | ✅ (9 przypadków, tabela powyżej) |
| Diagram UML | ✅ `UML.puml` (PlantUML) |
| Refaktoryzacja tablica → List | ✅ `ArrayList<Product>` w `Cart` |
