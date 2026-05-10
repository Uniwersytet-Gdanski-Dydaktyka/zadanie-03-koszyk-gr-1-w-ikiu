package cart;

import java.util.Comparator;

public class ProductComparators {

    // domyslne sortowanie: cena malejaco, a w przypadku remisu nazwa alfabetycznie
    public static final Comparator<Product> DEFAULT_ORDER = Comparator
            .comparingDouble(Product::getPrice).reversed()
            .thenComparing(Product::getName);

    // przyklad otwartosci na modyfikacje (dip) - mozemy dodac sortowanie po kodzie
    public static final Comparator<Product> BY_CODE = Comparator
            .comparing(Product::getCode);
}