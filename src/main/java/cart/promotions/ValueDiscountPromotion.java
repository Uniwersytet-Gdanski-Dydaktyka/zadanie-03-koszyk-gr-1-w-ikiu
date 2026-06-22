package cart.promotions;

import cart.Product;
import java.util.List;
import java.util.stream.Collectors;

// promocja: jesli laczna wartosc koszyka przekracza 300 zl, kazdy produkt dostaje 5% znizki.
// rabat jest liczony od discountPrice (nie od oryginalnego price), wiec poprawnie sklada sie z innymi promocjami zastosowanymi przed nia.
public class ValueDiscountPromotion implements Promotion {

    private static final double MIN_VALUE = 300.0;         // prog: koszyk musi przekraczac te wartosc
    private static final double DISCOUNT_MULTIPLIER = 0.95; // 5% znizki oznacza ze placimy 95% ceny

    @Override
    public List<Product> apply(List<Product> products) {
        // sumujemy aktualne ceny rabatowe wszystkich produktow, zeby sprawdzic czy prog jest osiagniety
        double total = products.stream().mapToDouble(Product::getDiscountPrice).sum();

        if (total > MIN_VALUE) {
            // prog przekroczony: tworzymy nowa liste produktow gdzie kazdy ma obnizone discountPrice.
            // mnozymy przez DISCOUNT_MULTIPLIER (0.95) aby zastosowac 5% rabatu.
            // withDiscountPrice() zwraca nowy obiekt Product — oryginal nigdy nie jest modyfikowany.
            return products.stream()
                    .map(p -> p.withDiscountPrice(p.getDiscountPrice() * DISCOUNT_MULTIPLIER))
                    .collect(Collectors.toList());
        }

        // prog nie osiagniety: zwracamy oryginalna liste bez zadnych zmian
        return products;
    }
}
