package cart;

import cart.promotions.Promotion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// glowna klasa reprezentujaca koszyk zakupow klienta.
// przechowuje mutowalna liste produktow i udostepnia metody do:
//   - dodawania produktow
//   - wyszukiwania najtanszych / najdrozszych produktow
//   - sortowania produktow dowolnym komparatorem
//   - aplikowania strategii promocji
//   - wyliczania aktualnej lacznie ceny koszyka
public class Cart {

    // wewnetrzna lista produktow; zastepowana (nie mutowana w miejscu) gdy aplikujemy promocje
    private List<Product> products;

    // zapamiętany komparator — używany do automatycznego sortowania po każdym addProduct()
    // domyślnie ustawiony na DEFAULT_ORDER (cena malejąco, nazwa A-Z)
    private Comparator<Product> currentComparator;

    // tworzy pusty koszyk bez zadnych produktow
    // ustawia domyslne kryterium sortowania
    public Cart() {
        this.products = new ArrayList<>();
        this.currentComparator = ProductComparators.DEFAULT_ORDER;
    }

    // dodaje produkt do koszyka i od razu sortuje liste wedlug aktualnego komparatora.
    // dzieki temu koszyk jest zawsze posortowany, nawet po dodaniu nowego produktu.
    // null jest odrzucany natychmiast — gdyby trafil do listy, psulby wszystkie operacje na streamach
    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("cannot add null product to cart");
        }
        products.add(product);
        products.sort(currentComparator); // automatyczne sortowanie po kazdym dodaniu
    }

    // wylicza sume cen rabatowych (discountPrice) wszystkich produktow w koszyku.
    // uzywamy discountPrice (nie price), zeby juz zastosowane promocje byly uwzglednione w sumie
    public double getTotalPrice() {
        return products.stream()
                .mapToDouble(Product::getDiscountPrice) // kazdy produkt doklada swoja aktualna cene po rabacie
                .sum();
    }

    // sortuje wewnetrzna liste uzywajac komparatora dostarczonego z zewnatrz
    // realizuje zasade DIP (Dependency Inversion Principle): Cart nie narzuca kolejnosci sortowania,
    // przyjmuje dowolny Comparator<Product> — np. ProductComparators.DEFAULT_ORDER.
    // zapamiętuje nowy komparator jako currentComparator, zeby kolejne addProduct() tez go uzywaly
    public void sort(Comparator<Product> comparator) {
        this.currentComparator = comparator; // zapamiętaj nowe kryterium na przyszłość
        products.sort(comparator);
    }

    // zwraca jeden najtanszy produkt wedlug ceny oryginalnej (price).
    // zwraca Optional.empty() jesli koszyk jest pusty, co wymusza obsluge tego przypadku przez wywolujacego - sytuacja brzegowa
    public Optional<Product> getCheapest() {
        return products.stream()
                .min(Comparator.comparingDouble(Product::getPrice));
    }

    // zwraca jeden najdrozszy produkt wedlug ceny oryginalnej (price).
    // zwraca Optional.empty() jesli koszyk jest pusty.
    public Optional<Product> getMostExpensive() {
        return products.stream()
                .max(Comparator.comparingDouble(Product::getPrice));
    }

    // zwraca liste n najtanszych produktow, posortowanych rosnaco wedlug ceny oryginalnej.
    // jesli n <= 0, zwracamy od razu pusta liste (klauzula strazy, bez wyjatku).
    // jesli n przekracza liczbe produktow, zwracane sa wszystkie produkty.
    public List<Product> getCheapest(int n) {
        if (n <= 0) return Collections.emptyList();
        return products.stream()
                .sorted(Comparator.comparingDouble(Product::getPrice)) // najtanszy na poczatku
                .limit(n) // bierzemy co najwyzej n elementow
                .collect(Collectors.toList());
    }

    // zwraca liste n najdrozszych produktow, posortowanych malejaco wedlug ceny oryginalnej.
    // lustrzane odbicie getCheapest(n), ale z odwroconym komparatorem.
    public List<Product> getMostExpensive(int n) {
        if (n <= 0) return Collections.emptyList();
        return products.stream()
                .sorted(Comparator.comparingDouble(Product::getPrice).reversed()) // najdrozszy na poczatku
                .limit(n)
                .collect(Collectors.toList());
    }

    // aplikuje promocje (wzorzec Strategy) na aktualnej liscie produktow.
    // promocja otrzymuje pelna liste i zwraca nowa liste z zaktualizowanymi cenami rabatowymi.
    // wewnetrzna lista jest ZASTEPOWANA wynikiem promocji — tak wlasnie dzialamy z niemutowalnymi Product.
    // jesli koszyk jest pusty, pomijamy wywolanie zeby uniknac zbednej pracy.
    public void applyPromotion(Promotion strategy) {
        if (strategy == null) throw new IllegalArgumentException("promocja nie moze byc null");
        if (products.isEmpty()) return;
        this.products = strategy.apply(this.products);
    }

    // zwraca widok listy produktow tylko do odczytu.
    // Collections.unmodifiableList() otacza liste tak, ze kod zewnetrzny nie moze dodawac/usuwac elementow.
    // odczyt (iteracja, size, get) nadal dziala — blokowane sa tylko strukturalne modyfikacje.
    public List<Product> getProducts() {
        return Collections.unmodifiableList(products);
    }
}