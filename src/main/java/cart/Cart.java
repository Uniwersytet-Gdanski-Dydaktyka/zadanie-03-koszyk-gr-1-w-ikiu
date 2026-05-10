package cart;

import cart.promotions.Promotion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Cart {
    private List<Product> products;

    public Cart() {
        this.products = new ArrayList<>();
    }

    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("nie mozna dodac null do koszyka");
        }
        products.add(product);
    }

    // wyliczanie sumy cen (z uwzglednieniem znizek)
    public double getTotalPrice() {
        return products.stream()
                .mapToDouble(Product::getDiscountPrice)
                .sum();
    }

    // sortowanie - pozwala wstrzyknac dowolny komparator
    public void sort(Comparator<Product> comparator) {
        products.sort(comparator);
    }

    // pobieranie n najtanszych produktow
    public List<Product> getCheapest(int n) {
        if (n <= 0) return Collections.emptyList();
        return products.stream()
                .sorted(Comparator.comparingDouble(Product::getPrice))
                .limit(n)
                .collect(Collectors.toList());
    }

    // pobieranie n najdrozszych produktow
    public List<Product> getMostExpensive(int n) {
        if (n <= 0) return Collections.emptyList();
        return products.stream()
                .sorted(Comparator.comparingDouble(Product::getPrice).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    // aplikuje znizke - nadpisuje aktualna liste nowa lista po przejsciu przez strategie
    public void applyPromotion(Promotion strategy) {
        if (products.isEmpty()) return; // obsługa sytuacji brzegowej
        this.products = strategy.apply(this.products);
    }

    public List<Product> getProducts() {
        return Collections.unmodifiableList(products); // zabezpieczenie przed modyfikacja z zewnatrz
    }
}