package cart.promotions;

import cart.Product;
import java.util.List;

public interface Promotion {
    // metoda przyjmuje stan koszyka i zwraca jego nowy stan
    List<Product> apply(List<Product> products);
}