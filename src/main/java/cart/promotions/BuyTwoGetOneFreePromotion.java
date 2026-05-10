package cart.promotions;

import cart.Product;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BuyTwoGetOneFreePromotion implements Promotion {

    @Override
    public List<Product> apply(List<Product> products) {
        // jesli mamy mniej niz 3 produkty, promocja nie dziala
        if (products.size() < 3) {
            return products;
        }

        // tworzymy nowa liste i sortujemy ja malejaco po aktualnej cenie (discountprice)
        List<Product> sortedProducts = new ArrayList<>(products);
        sortedProducts.sort(Comparator.comparingDouble(Product::getDiscountPrice).reversed());

        List<Product> result = new ArrayList<>();

        // iterujemy po posortowanych produktach
        for (int i = 0; i < sortedProducts.size(); i++) {
            Product current = sortedProducts.get(i);

            // co trzeci produkt (indeksy 2, 5, 8...) to nasz gratis
            // (i + 1) pozwala nam liczyc od 1 do n, zamiast od 0
            if ((i + 1) % 3 == 0) {
                // uzywamy naszego withera - tworzymy kopie z cena 0.0
                result.add(current.withDiscountPrice(0.0));
            } else {
                // produkt nie jest gratisem, dodajemy go bez zmian
                result.add(current);
            }
        }

        return result;
    }
}