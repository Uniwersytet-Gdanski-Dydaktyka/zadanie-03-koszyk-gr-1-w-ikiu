package cart;

import cart.promotions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

// testy jednostkowe klasy Cart oraz wszystkich strategii promocji.
// kazdy test jest niezalezny: @BeforeEach tworzy swiezy koszyk przed kazdym testem.
class CartTest {

    private Cart cart;

    // metoda uruchamiana przed kazdym pojedynczym testem.
    // tworzy nowy pusty koszyk, zeby stan nie przeciekal miedzy testami.
    @BeforeEach
    void setUp() {
        cart = new Cart();
    }

    // podstawowy scenariusz: dodanie jednego produktu powinno sprawic,
    // ze getProducts() zwroci liste o rozmiarze 1, a getTotalPrice() zwroci cene tego produktu.
    @Test
    void shouldAddProductToCart() {
        Product p = new Product("P01", "ksiazka", 50.0);
        cart.addProduct(p);

        assertEquals(1, cart.getProducts().size());
        assertEquals(50.0, cart.getTotalPrice());
    }

    // sytuacja brzegowa: przekazanie null do addProduct() musi rzucic IllegalArgumentException.
    // null w liscie psulby wszystkie operacje na streamach.
    @Test
    void shouldThrowExceptionWhenAddingNull() {
        assertThrows(IllegalArgumentException.class, () -> cart.addProduct(null));
    }

    // sytuacja brzegowa: wywolanie getCheapest(n) na pustym koszyku powinno zwrocic pusta liste, nie rzucic wyjatku.
    @Test
    void shouldReturnEmptyListWhenGettingCheapestFromEmptyCart() {
        assertTrue(cart.getCheapest(2).isEmpty());
    }

    // sprawdza ze komparator DEFAULT_ORDER dziala poprawnie:
    // produkty z ta sama cena powinny byc posortowane alfabetycznie po nazwie.
    // "playstation" jest przed "x-box" alfabetycznie, wiec powinien byc pierwszy.
    // "pad" jest najtanszy (200 vs 1000), wiec laduje na koncu (sortowanie malejace po cenie).
    @Test
    void shouldSortProductsByDefaultOrder() {
        cart.addProduct(new Product("P01", "x-box", 1000.0));
        cart.addProduct(new Product("P02", "playstation", 1000.0));
        cart.addProduct(new Product("P03", "pad", 200.0));

        cart.sort(ProductComparators.DEFAULT_ORDER); // cena malejaco, potem nazwa A-Z
        List<Product> products = cart.getProducts();

        assertEquals("playstation", products.get(0).getName()); // ta sama cena co x-box, ale 'p' < 'x'
        assertEquals("x-box", products.get(1).getName());
        assertEquals("pad", products.get(2).getName()); // najtanszy, na koncu
    }

    // sprawdza getCheapest(n) i getMostExpensive(n) na koszyku z 3 produktami.
    // getCheapest(2) powinno zwrocic 2 najtansze posortowane rosnaco.
    // getMostExpensive(1) powinno zwrocic tylko 1 najdrozszy.
    @Test
    void shouldGetCheapestAndMostExpensiveProducts() {
        cart.addProduct(new Product("P01", "tanie", 10.0));
        cart.addProduct(new Product("P02", "srednie", 50.0));
        cart.addProduct(new Product("P03", "drogie", 100.0));

        List<Product> cheapest = cart.getCheapest(2);
        assertEquals(2, cheapest.size());
        assertEquals(10.0, cheapest.get(0).getPrice()); // najtanszy musi byc pierwszy

        List<Product> mostExpensive = cart.getMostExpensive(1);
        assertEquals(1, mostExpensive.size());
        assertEquals(100.0, mostExpensive.get(0).getPrice()); // najdrozszy musi byc pierwszy
    }

    // sprawdza ValueDiscountPromotion: gdy suma koszyka > 300, wszystkie produkty dostaja 5% rabatu.
    // 1000 * 0.95 = 950.0 — taka wartosc powinna zwrocic getTotalPrice().
    @Test
    void shouldApplyValueDiscountPromotion() {
        cart.addProduct(new Product("P01", "laptop", 1000.0));
        cart.applyPromotion(new ValueDiscountPromotion());

        assertEquals(950.0, cart.getTotalPrice()); // 1000 * 0.95 = 950
    }

    // sprawdza FreeMugPromotion: gdy suma koszyka > 200, dodawany jest darmowy kubek (cena 0.0).
    // laczna cena koszyka nie powinna wzrosnac, bo kubek nic nie kosztuje.
    @Test
    void shouldApplyFreeMugPromotion() {
        cart.addProduct(new Product("P01", "klawiatura", 250.0));
        cart.applyPromotion(new FreeMugPromotion());

        assertEquals(2, cart.getProducts().size()); // oryginalny produkt + darmowy kubek
        assertTrue(cart.getProducts().stream().anyMatch(p -> p.getCode().equals("MUG-01")));
        assertEquals(250.0, cart.getTotalPrice()); // kubek jest darmowy, cena sie nie zmienia
    }

    // sprawdza BuyTwoGetOneFreePromotion: przy 3 produktach najtanszy dostaje discountPrice = 0.
    // po posortowaniu malejaco: gra a (100), myszka (50), kabel (20) — kabel jest trzeci wiec gratis.
    // oczekiwana suma: 100 + 50 + 0 = 150.
    @Test
    void shouldApplyBuyTwoGetOneFreePromotion() {
        cart.addProduct(new Product("P01", "gra a", 100.0));
        cart.addProduct(new Product("P02", "myszka", 50.0));
        cart.addProduct(new Product("P03", "kabel", 20.0));

        cart.applyPromotion(new BuyTwoGetOneFreePromotion());

        // "kabel" za 20.0 jest najtanszy — jego discountPrice staje sie 0.0
        assertEquals(150.0, cart.getTotalPrice()); // 100 + 50 + 0 = 150
    }

    // sprawdza CouponPromotion: 30% rabat stosowany tylko na PIERWSZE wystapienie produktu z pasujacym kodem.
    // dwa produkty maja kod "P02", ale rabat dostaje tylko pierwszy z nich.
    // oczekiwana suma: 100 (P01 bez zmian) + 70 (P02: 100 * 0.70) + 100 (drugi P02: kupon juz spalony) = 270.
    @Test
    void shouldApplyCouponPromotionToSingleProduct() {
        cart.addProduct(new Product("P01", "myszka", 100.0));
        cart.addProduct(new Product("P02", "klawiatura", 100.0));        // ta dostanie 30% rabatu
        cart.addProduct(new Product("P02", "druga klawiatura", 100.0)); // kupon juz spalony, brak rabatu

        cart.applyPromotion(new CouponPromotion("P02"));

        assertEquals(270.0, cart.getTotalPrice()); // 100 + 70 + 100
    }

    // sprawdza PromotionOptimizer (zadanie dodatkowe):
    // probuje wszystkich kolejnosci dwoch promocji i wybiera te z najnizsza suma.
    //
    // koszyk: gra1 (100), gra2 (100), gra3 (150) — suma = 350
    //
    // kolejnosc A — najpierw ValueDiscount, potem BuyTwoGetOneFree:
    //   po -5% (350 > 300, wiec dziala): 95 + 95 + 142.5 = 332.5
    //   2+1 daje za darmo najtanszy (95): 142.5 + 95 + 0 = 237.5
    //
    // kolejnosc B — najpierw BuyTwoGetOneFree, potem ValueDiscount:
    //   najtanszy (100) staje sie 0: 150 + 100 + 0 = 250
    //   250 jest ponizej progu 300, wiec ValueDiscount nie dziala: suma = 250
    //
    // optymalizator powinien wybrac kolejnosc A z suma = 237.5
    @Test
    void shouldFindBestPromotionOrder() {
        cart.addProduct(new Product("P01", "gra1", 100.0));
        cart.addProduct(new Product("P02", "gra2", 100.0));
        cart.addProduct(new Product("P03", "gra3", 150.0));

        List<Promotion> promos = List.of(
                new ValueDiscountPromotion(),    // 5% rabatu jesli suma > 300
                new BuyTwoGetOneFreePromotion()  // najtanszy z kazdej trojki gratis
        );

        OptimizationResult result = PromotionOptimizer.getBestPrice(cart, promos);

        assertEquals(237.5, result.getBestPrice());
        assertEquals(2, result.getBestOrder().size()); // obie promocje sa w najlepszej kolejnosci
    }

    // sprawdza wariant jednoelementowy getCheapest() — powinien zwrocic produkt z najnizsza cena.
    @Test
    void shouldReturnCheapestProduct() {
        cart.addProduct(new Product("P01", "tanie", 10.0));
        cart.addProduct(new Product("P02", "drogie", 100.0));

        Optional<Product> cheapest = cart.getCheapest();
        assertTrue(cheapest.isPresent());
        assertEquals(10.0, cheapest.get().getPrice());
    }

    // sprawdza wariant jednoelementowy getMostExpensive() — powinien zwrocic produkt z najwyzsza cena.
    @Test
    void shouldReturnMostExpensiveProduct() {
        cart.addProduct(new Product("P01", "tanie", 10.0));
        cart.addProduct(new Product("P02", "drogie", 100.0));

        Optional<Product> mostExpensive = cart.getMostExpensive();
        assertTrue(mostExpensive.isPresent());
        assertEquals(100.0, mostExpensive.get().getPrice());
    }

    // sytuacja brzegowa: getCheapest() i getMostExpensive() na pustym koszyku musza zwrocic Optional.empty().
    @Test
    void shouldReturnEmptyOptionalWhenCartIsEmpty() {
        assertTrue(cart.getCheapest().isEmpty());
        assertTrue(cart.getMostExpensive().isEmpty());
    }

    // sytuacja brzegowa: getMostExpensive(n) na pustym koszyku musi zwrocic pusta liste.
    @Test
    void shouldReturnEmptyListWhenGettingMostExpensiveFromEmptyCart() {
        assertTrue(cart.getMostExpensive(2).isEmpty());
    }

    // sytuacja brzegowa: jesli n jest wieksze od liczby produktow, zwracane sa wszystkie produkty (bez wyjatku).
    @Test
    void shouldReturnAllProductsWhenNExceedsSize() {
        cart.addProduct(new Product("P01", "a", 10.0));
        cart.addProduct(new Product("P02", "b", 20.0));

        // n=100 jest wieksze niz rozmiar koszyka (2), wiec zwracane sa oba produkty
        assertEquals(2, cart.getCheapest(100).size());
        assertEquals(2, cart.getMostExpensive(100).size());
    }

    // sytuacja brzegowa: produkt z cena 0.0 jest prawidlowy i powinien byc traktowany jako najtanszy.
    @Test
    void shouldHandleProductWithZeroPrice() {
        cart.addProduct(new Product("P01", "darmowy", 0.0));
        cart.addProduct(new Product("P02", "platny", 50.0));

        assertEquals(50.0, cart.getTotalPrice()); // 0.0 + 50.0 = 50.0
        assertEquals(0.0, cart.getCheapest().get().getPrice()); // produkt z cena 0 jest najtanszy
    }

    // sytuacja brzegowa: FreeMugPromotion NIE powinna dodac kubka gdy suma < 200.
    // produkt kosztuje 150, co jest ponizej wymaganego progu 200.
    @Test
    void shouldNotApplyFreeMugWhenTotalBelowThreshold() {
        cart.addProduct(new Product("P01", "produkt", 150.0)); // 150 < 200, brak kubka

        cart.applyPromotion(new FreeMugPromotion());

        assertEquals(1, cart.getProducts().size()); // kubek nie zostal dodany
    }

    // sytuacja brzegowa: ValueDiscountPromotion NIE powinna sie aplikowac gdy suma wynosi dokladnie 300.
    // warunek to total > 300 (scisle wiekszy), wiec 300 nie kwalifikuje sie.
    @Test
    void shouldNotApplyValueDiscountWhenTotalAtOrBelowThreshold() {
        cart.addProduct(new Product("P01", "produkt", 300.0)); // dokladnie 300 — nie spelnia warunku >300

        cart.applyPromotion(new ValueDiscountPromotion());

        assertEquals(300.0, cart.getTotalPrice()); // brak rabatu, cena bez zmian
    }
}
