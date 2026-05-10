package cart;

import cart.promotions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CartTest {

    private Cart cart;

    // ta metoda uruchamia sie przed kazdym testem - daje nam swiezy, pusty koszyk
    @BeforeEach
    void setUp() {
        cart = new Cart();
    }

    @Test
    void shouldAddProductToCart() {
        Product p = new Product("P01", "ksiazka", 50.0);
        cart.addProduct(p);

        assertEquals(1, cart.getProducts().size());
        assertEquals(50.0, cart.getTotalPrice());
    }

    @Test
    void shouldThrowExceptionWhenAddingNull() {
        // sytuacja brzegowa: proba dodania null do koszyka
        assertThrows(IllegalArgumentException.class, () -> cart.addProduct(null));
    }

    @Test
    void shouldReturnEmptyListWhenGettingCheapestFromEmptyCart() {
        // sytuacja brzegowa: szukanie w pustym koszyku
        assertTrue(cart.getCheapest(2).isEmpty());
    }

    @Test
    void shouldSortProductsByDefaultOrder() {
        // domyslne sortowanie: cena malejaco, nazwa alfabetycznie
        cart.addProduct(new Product("P01", "x-box", 1000.0));
        cart.addProduct(new Product("P02", "playstation", 1000.0));
        cart.addProduct(new Product("P03", "pad", 200.0));

        cart.sort(ProductComparators.DEFAULT_ORDER);
        List<Product> products = cart.getProducts();

        assertEquals("playstation", products.get(0).getName()); // wygrywa alfabetycznie przy tej samej cenie
        assertEquals("x-box", products.get(1).getName());
        assertEquals("pad", products.get(2).getName());
    }

    @Test
    void shouldGetCheapestAndMostExpensiveProducts() {
        cart.addProduct(new Product("P01", "tanie", 10.0));
        cart.addProduct(new Product("P02", "srednie", 50.0));
        cart.addProduct(new Product("P03", "drogie", 100.0));

        List<Product> cheapest = cart.getCheapest(2);
        assertEquals(2, cheapest.size());
        assertEquals(10.0, cheapest.get(0).getPrice()); // pierwsze najtansze

        List<Product> mostExpensive = cart.getMostExpensive(1);
        assertEquals(1, mostExpensive.size());
        assertEquals(100.0, mostExpensive.get(0).getPrice()); // pierwsze najdrozsze
    }

    @Test
    void shouldApplyValueDiscountPromotion() {
        // promocja: powyzej 300 zl rabat 5%
        cart.addProduct(new Product("P01", "laptop", 1000.0));
        cart.applyPromotion(new ValueDiscountPromotion());

        // 1000 * 0.95 = 950
        assertEquals(950.0, cart.getTotalPrice());
    }

    @Test
    void shouldApplyFreeMugPromotion() {
        // promocja: powyzej 200 zl kubek gratis
        cart.addProduct(new Product("P01", "klawiatura", 250.0));
        cart.applyPromotion(new FreeMugPromotion());

        assertEquals(2, cart.getProducts().size());
        assertTrue(cart.getProducts().stream().anyMatch(p -> p.getCode().equals("MUG-01")));
        assertEquals(250.0, cart.getTotalPrice()); // cena sie nie zmienia, dochodzi darmowy produkt
    }

    @Test
    void shouldApplyBuyTwoGetOneFreePromotion() {
        // promocja: 2+1 gratis (najtanszy)
        cart.addProduct(new Product("P01", "gra a", 100.0));
        cart.addProduct(new Product("P02", "myszka", 50.0));
        cart.addProduct(new Product("P03", "kabel", 20.0));

        cart.applyPromotion(new BuyTwoGetOneFreePromotion());

        // najtanszy produkt (kabel za 20) powinien miec cene 0.0 po promocji
        // suma = 100 + 50 + 0 = 150
        assertEquals(150.0, cart.getTotalPrice());
    }

    @Test
    void shouldApplyCouponPromotionToSingleProduct() {
        // promocja: 30% znizki na konkretny kod (P02)
        cart.addProduct(new Product("P01", "myszka", 100.0));
        cart.addProduct(new Product("P02", "klawiatura", 100.0)); // to ma byc przecenione o 30%
        cart.addProduct(new Product("P02", "druga klawiatura", 100.0)); // to juz nie, bo kupon jest jednorazowy

        cart.applyPromotion(new CouponPromotion("P02"));

        // suma: 100 + 70 (rabat) + 100 = 270
        assertEquals(270.0, cart.getTotalPrice());
    }

    @Test
    void shouldFindBestPromotionOrder() {
        // test zadania dodatkowego
        cart.addProduct(new Product("P01", "gra1", 100.0));
        cart.addProduct(new Product("P02", "gra2", 100.0));
        cart.addProduct(new Product("P03", "gra3", 150.0)); // suma przed promocjami = 350 zl

        List<Promotion> promos = List.of(
                new ValueDiscountPromotion(), // rabat 5% na calosc (bo koszyk > 300)
                new BuyTwoGetOneFreePromotion() // najtanszy gratis (czyli jedna gra za 100)
        );

        // sprawdzamy jaka jest najnizsza mozliwa cena
        double bestPrice = PromotionOptimizer.getBestPrice(cart, promos);

        // analiza:
        // wariant 1: najpierw -5%, potem 2+1
        // suma po -5%: 95 + 95 + 142.5 = 332.5. potem 2+1 zeruje najtanszy (95). koncowa: 237.5
        //
        // wariant 2: najpierw 2+1, potem -5% (ten jest lepszy!)
        // suma po 2+1: 0 + 100 + 150 = 250.
        // ale poniewaz suma spadla do 250 zl, to rabat -5% (ktory dziala od 300 zl) W OGOLE SIE NIE NALOZY! koncowa: 250.0
        //
        // wiec z punktu widzenia klienta, pierwszy wariant (237.5) jest najkorzystniejszy.
        assertEquals(237.5, bestPrice);
    }
}