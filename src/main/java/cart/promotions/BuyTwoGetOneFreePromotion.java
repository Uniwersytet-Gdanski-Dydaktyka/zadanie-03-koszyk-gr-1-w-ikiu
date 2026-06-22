package cart.promotions;

import cart.Product;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// promocja "2+1 gratis": przy zakupie co najmniej 3 produktow co trzeci najtanszy produkt jest darmowy.
// "co trzeci" oznacza ze przy sortowaniu malejacym po cenie, produkty na pozycjach 3., 6., 9., ...
// (czyli najtanszy w kazdej trojce) maja swoja cene rabatowa ustawiona na 0.0.
public class BuyTwoGetOneFreePromotion implements Promotion {

    @Override
    public List<Product> apply(List<Product> products) {
        // promocja wymaga co najmniej 3 produktow — przy mniejszej liczbie nikt nie dostaje gratisu
        if (products.size() < 3) {
            return products;
        }

        // tworzymy kopie listy i sortujemy malejaco po discountPrice (nie po oryginalnym price),
        // zeby szanowac efekty wczesniej zastosowanych promocji.
        // uzywamy new ArrayList<>() zeby nie modyfikowac listy wejsciowej.
        List<Product> sortedProducts = new ArrayList<>(products);
        sortedProducts.sort(Comparator.comparingDouble(Product::getDiscountPrice).reversed());

        List<Product> result = new ArrayList<>();

        // przechodzimy przez posortowana liste i oznaczamy co trzeci produkt jako gratis.
        // (i + 1) zamienia indeks 0-based na licznik 1-based,
        // wiec modulo 3 == 0 gdy licznik wynosi 3, 6, 9... (czyli pozycje: trzecia, szosta, dziewiata...)
        for (int i = 0; i < sortedProducts.size(); i++) {
            Product current = sortedProducts.get(i);

            if ((i + 1) % 3 == 0) {
                // ten produkt jest "gratisowym" w swojej trojce — ustawiamy jego cene rabatowa na 0.0.
                // withDiscountPrice(0.0) zwraca nowy obiekt Product — oryginal pozostaje niezmieniony.
                result.add(current.withDiscountPrice(0.0));
            } else {
                // produkt nie jest gratisem — dodajemy go bez zadnych zmian
                result.add(current);
            }
        }

        return result;
    }
}
