package cart;

import java.util.Objects;

public class Product {
    private final String code;
    private final String name;
    private final double price;
    private final double discountPrice;

    public Product(String code, String name, double price) {
        this(code, name, price, price); // domyslnie cena po znizce to cena regularna
    }

    private Product(String code, String name, double price, double discountPrice) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.discountPrice = discountPrice;
    }

    // wzorzec "wither" - zamiast settera, zwracamy nowy obiekt ze zmieniona cena.
    // to gwarantuje niemutowalnosc (immutability) klasy.
    public Product withDiscountPrice(double newDiscountPrice) {
        return new Product(this.code, this.name, this.price, newDiscountPrice);
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public double getDiscountPrice() { return discountPrice; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(code, product.code); // do unikalnosci wystarczy kod
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}