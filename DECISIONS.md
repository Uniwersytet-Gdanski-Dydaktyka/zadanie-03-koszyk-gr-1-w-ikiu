# decisions — uzasadnienie decyzji projektowych

## 1. niemutowalnosc klasy Product (uwaga 4)

**decyzja:** klasa Product jest niemutowalna (immutable).

wszystkie pola sa `final`. nie ma settera dla `discountPrice`. zamiast tego promocje uzywaja metody `withDiscountPrice()`, ktora zwraca NOWY obiekt Product z zaktualizowana cena. oryginalny obiekt pozostaje niezmieniony.

**dlaczego niemutowalnosc?**
gdyby Product byl mutowalny, dwie promocje aplikowane po sobie moglyby nadpisywac swoje wyniki zalezne od kolejnosci wywolania, co prowadzilby do trudnych do wykrycia bledow. niemutowalnosc eliminuje cala te klase problemow. kiedy trzymamy referencje do Product, mamy pewnosc ze obiekt nie zmieni sie "pod nogami".

**kompromis:** przy kazdym zastosowaniu rabatu tworzony jest nowy obiekt. dla skali koszyka zakupow (zwykle mniej niz 100 produktow) jest to w pelni akceptowalne.

---

## 2. wzorzec projektowy dla promocji (uwaga 2)

**decyzja:** wzorzec Strategy.

kazda promocja implementuje interfejs `Promotion` z jedna metoda `apply(List<Product>) -> List<Product>`. metoda `Cart.applyPromotion()` wywoluje te metode nie znajac konkretnego typu promocji.

**dlaczego Strategy, nie Command?**
- Command sluzy glownie do kolejkowania operacji i robienia "undo/redo". nasze promocje nie potrzebuja zadnej z tych funkcji.
- Strategy idealnie pasuje: rodzina wymienialnych algorytmow (roznych promocji), ktore mozna podmieniaczatki w czasie dzialania programu. gdy marketing wymysli nowa promocje, wystarczy dopisac nowa klase implementujaca `Promotion` — kodu klasy Cart i pozostalych promocji nie dotykamy. to jest zasada OCP (Open/Closed Principle) w praktyce.

---

## 3. sortowanie — zasada DIP i Comparator (uwaga 3)

**decyzja:** `Cart.sort()` przyjmuje `Comparator<Product>` jako parametr. gotowe komparatory sa zdefiniowane jako stale w klasie `ProductComparators`.

**dlaczego?**
Cart nie powinien decydowac o sposobie sortowania — to regula biznesowa, ktora moze sie zmienic. przyjmujac `Comparator` od wywolujacego, Cart zalezy od abstrakcji, a nie od konkretnej kolejnosci. dodanie nowego kryterium (np. sortowanie po kategorii) wymaga tylko jednej stalej w `ProductComparators` — klasa Cart nigdy nie jest dotykana. to realizacja zasady DIP (Dependency Inversion Principle).

---

## 4. optymalizator promocji — brute force permutacji (zadanie dodatkowe)

**decyzja:** `PromotionOptimizer` generuje wszystkie n! permutacji listy promocji, symuluje kazda z nich na swiezym tymczasowym koszyku i zwraca kolejnosc dajaca najnizsza sume.

**dlaczego brute force?**
promocje nie sa przemienne — zastosowanie rabatu A przed B moze dac inny wynik niz B przed A. bez matematycznej gwarancji optymalnej kolejnosci jedynym poprawnym podejsciem jest sprawdzenie wszystkich permutacji. dla typowego koszyka z 2-4 aktywnymi promocjami n! to co najwyzej 24 iteracje, co jest b. szybkie.

**dlaczego tymczasowy koszyk?**
oryginalny koszyk przekazany przez wywolujacego nie moze byc modyfikowany. dla kazdej permutacji klonujemy koszyk tworzac nowy Cart i kopiujac produkty przez `new Product(code, name, price)`, co resetuje discountPrice do ceny oryginalnej. dzieki temu kazda permutacja startuje z tego samego punktu startowego.

---

## 5. OptimizationResult jako obiekt wartosci

**decyzja:** `OptimizationResult` jest niemutowalnym obiektem wartosci z dwoma polami: `bestPrice` i `bestOrder`.

**dlaczego?**
optymalizator oblicza dwie powiazane informacje (cena + kolejnosc). zwrocenie ich jako para w dedykowanej klasie jest czytelniejsze niz zwracanie tablicy lub uzywanie parametrow wyjsciowych. oba pola sa `final`, a lista jest opakowana w `Collections.unmodifiableList()`, zeby wywolujacy nie mogl modyfikowac wyniku po jego otrzymaniu.

---

## 6. getProducts() zwraca widok tylko do odczytu

**decyzja:** `Cart.getProducts()` zwraca `Collections.unmodifiableList(products)`.

**dlaczego?**
hermetyzacja (enkapsulacja). gdybysmy zwracali surowa liste, kod zewnetrzny mogl by wywolac `.add()` lub `.remove()` bezposrednio, omijajac walidacje w `addProduct()` i logike promocji. opakowanie w unmodifiable blokuje strukturalne modyfikacje, jednoczesnie pozwalajac na odczyt (iteracja, size, get).
