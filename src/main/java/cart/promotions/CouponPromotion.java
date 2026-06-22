package cart.promotions;

import cart.Product;
import java.util.ArrayList;
import java.util.List;

// promocja: jednorazowy kupon rabatowy dajacy 30% znizki na jeden konkretny produkt (wskazany kodem).
// kupon jest "spalany" po pierwszym uzyciu — jesli ten sam kod produktu pojawia sie wiecej razy,
// tylko pierwsze wystapienie dostaje rabat, kolejne sa pomijane.
public class CouponPromotion implements Promotion {

    private final String targetProductCode; // kod produktu, na ktory dziala ten kupon
    private static final double DISCOUNT_MULTIPLIER = 0.70; // 30% znizki oznacza ze klient placi 70% ceny

    // konstruktor przyjmuje kod produktu, na ktory kupon ma dzialac.
    // dzieki temu CouponPromotion jest elastyczna — mozna wskazac dowolny produkt w czasie dzialania programu.
    public CouponPromotion(String targetProductCode) {
        this.targetProductCode = targetProductCode;
    }

    @Override
    public List<Product> apply(List<Product> products) {
        boolean couponUsed = false; // flaga sledzaca czy kupon zostal juz wykorzystany
        List<Product> result = new ArrayList<>();

        for (Product p : products) {
            // rabat aplikujemy tylko jesli kupon nie byl jeszcze uzyty ORAZ kod produktu pasuje
            if (!couponUsed && p.getCode().equals(targetProductCode)) {
                // stosujemy 30% rabatu mnozac aktualna cene rabatowa przez 0.70
                result.add(p.withDiscountPrice(p.getDiscountPrice() * DISCOUNT_MULTIPLIER));
                couponUsed = true; // "spalamy" kupon — kolejne dopasowania beda pomijane
            } else {
                // produkt nie pasuje lub kupon juz uzyty — dodajemy bez zmian
                result.add(p);
            }
        }
        return result;
    }
}
