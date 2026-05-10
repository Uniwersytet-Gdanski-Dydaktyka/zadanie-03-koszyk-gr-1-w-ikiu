package cart.promotions;

import cart.Product;
import java.util.List;
import java.util.stream.Collectors;

public class ValueDiscountPromotion implements Promotion {

    private static final double MIN_VALUE = 300.0;
    private static final double DISCOUNT_MULTIPLIER = 0.95; // 5% znizki

    @Override
    public List<Product> apply(List<Product> products) {
        double total = products.stream().mapToDouble(Product::getDiscountPrice).sum();

        if (total > MIN_VALUE) {
            // tworzymy nowe produkty z obnizona cena
            return products.stream()
                    .map(p -> p.withDiscountPrice(p.getDiscountPrice() * DISCOUNT_MULTIPLIER))
                    .collect(Collectors.toList());
        }
        return products; // zwracamy bez zmian, jesli warunek nie jest spelniony
    }
}