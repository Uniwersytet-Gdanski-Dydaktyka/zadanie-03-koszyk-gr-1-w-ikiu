package cart.promotions;

import cart.Product;
import java.util.ArrayList;
import java.util.List;

public class CouponPromotion implements Promotion {

    private final String targetProductCode;
    private static final double DISCOUNT_MULTIPLIER = 0.70; // 30% znizki (placimy 70%)

    public CouponPromotion(String targetProductCode) {
        this.targetProductCode = targetProductCode;
    }

    @Override
    public List<Product> apply(List<Product> products) {
        boolean couponUsed = false;
        List<Product> result = new ArrayList<>();

        for (Product p : products) {
            // jesli znalezlismy produkt i kupon nie byl jeszcze uzyty
            if (!couponUsed && p.getCode().equals(targetProductCode)) {
                result.add(p.withDiscountPrice(p.getDiscountPrice() * DISCOUNT_MULTIPLIER));
                couponUsed = true; // to kupon jednorazowy, wiec go "spalamy"
            } else {
                result.add(p);
            }
        }
        return result;
    }
}