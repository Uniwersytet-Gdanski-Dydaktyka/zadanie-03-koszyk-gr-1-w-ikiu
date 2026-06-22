package cart.promotions;

import cart.Product;
import java.util.ArrayList;
import java.util.List;

// promocja: przy wartosci koszyka powyzej 200 zl klient dostaje firmowy kubek gratis.
// kubek jest dodawany jako produkt z cena 0.0, wiec nie wplywa na laczna cene koszyka.
// promocja jest idempotentna: jesli kubek jest juz w koszyku, drugi nie zostanie dodany.
public class FreeMugPromotion implements Promotion {

    private static final double MIN_VALUE = 200.0;   // prog: wartosc koszyka musi byc scisle wieksza niz ta kwota
    private static final String MUG_CODE = "MUG-01"; // unikalny kod uzywany do identyfikacji kubka w koszyku

    @Override
    public List<Product> apply(List<Product> products) {
        // sumujemy aktualne ceny rabatowe wszystkich produktow, zeby sprawdzic kwalifikowalnosc
        double total = products.stream().mapToDouble(Product::getDiscountPrice).sum();

        // sprawdzamy czy kubek jest juz w koszyku — zapobiega to dodaniu drugiego kubka
        // przy ponownym wywolaniu tej samej promocji (idempotentnosc)
        boolean hasMug = products.stream().anyMatch(p -> p.getCode().equals(MUG_CODE));

        if (total > MIN_VALUE && !hasMug) {
            // klient kwalifikuje sie: kopiujemy istniejaca liste i dopisujemy kubek na koncu
            List<Product> newProducts = new ArrayList<>(products);
            // kubek ma price = 0.0 i discountPrice = 0.0 — nie zmienia sumy koszyka
            newProducts.add(new Product(MUG_CODE, "firmowy kubek gratis", 0.0));
            return newProducts;
        }

        // prog nie osiagniety lub kubek juz jest — zwracamy oryginalna liste bez zmian
        return products;
    }
}
