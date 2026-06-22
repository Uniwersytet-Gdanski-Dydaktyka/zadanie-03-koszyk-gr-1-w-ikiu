package cart;

import java.util.Objects;

// klasa product reprezentuje pojedynczy produkt w koszyku zakupow.
// jest ona niemutowalna (immutable) — po utworzeniu zadne pole nie moze zostac zmienione.
// zamiast setterow uzywamy metody withDiscountPrice(), ktora zwraca NOWY obiekt z inna cena po rabacie.
public class Product {

    // wszystkie pola sa final — moga byc przypisane tylko raz, w konstruktorze
    private final String code;          // unikalny identyfikator produktu (np. "P01", "MUG-01")
    private final String name;          // nazwa wyswietlana (np. "laptop")
    private final double price;         // cena oryginalna — nigdy sie nie zmienia, uzywana do sortowan i porownania
    private final double discountPrice; // aktualna cena po zastosowaniu promocji; na poczatku rowna price

    // publiczny konstruktor uzywany przez kod zewnetrzny.
    // discountPrice jest ustawiana na price, bo na poczatku nie ma zadnego rabatu.
    public Product(String code, String name, double price) {
        this(code, name, price, price); // deleguje do prywatnego konstruktora z price == discountPrice
    }

    // prywatny konstruktor pozwala ustawic price i discountPrice niezaleznie.
    // uzywany tylko przez withDiscountPrice(), zeby kod zewnetrzny nie mogl obejsc walidacji.
    private Product(String code, String name, double price, double discountPrice) {
        this.code = code;
        this.name = name;
        this.price = price;
        this.discountPrice = discountPrice;
    }

    // wzorzec "wither" — zamiast settera zwraca zupelnie nowy obiekt Product z zaktualizowana cena rabatowa.
    // oryginalny obiekt pozostaje niezmieniony, co gwarantuje niemutowalnosc.
    // promocje wywoluja te metode, zeby stworzyc kopie produktu z rabatem bez modyfikowania oryginalu.
    public Product withDiscountPrice(double newDiscountPrice) {
        return new Product(this.code, this.name, this.price, newDiscountPrice);
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public double getPrice() { return price; }               // cena oryginalna, nigdy sie nie zmienia
    public double getDiscountPrice() { return discountPrice; } // cena po wszystkich zastosowanych promocjach

    // dwa produkty sa rowne jesli maja ten sam kod — kod jest unikalnym identyfikatorem biznesowym
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(code, product.code);
    }

    // hashCode jest spojny z equals — rowniez oparty tylko na kodzie produktu
    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
