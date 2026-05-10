package cart.promotions;

import cart.Product;
import java.util.ArrayList;
import java.util.List;

public class FreeMugPromotion implements Promotion {

    private static final double MIN_VALUE = 200.0;
    private static final String MUG_CODE = "MUG-01";

    @Override
    public List<Product> apply(List<Product> products) {
        double total = products.stream().mapToDouble(Product::getDiscountPrice).sum();

        // jesli koszyk jest pusty lub ma juz kubek, nie dodajemy drugiego
        boolean hasMug = products.stream().anyMatch(p -> p.getCode().equals(MUG_CODE));

        if (total > MIN_VALUE && !hasMug) {
            List<Product> newProducts = new ArrayList<>(products);
            // dodajemy kubek z cena 0.0
            newProducts.add(new Product(MUG_CODE, "firmowy kubek gratis", 0.0));
            return newProducts;
        }
        return products;
    }
}