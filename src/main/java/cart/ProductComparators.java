package cart;

import java.util.Comparator;

// klasa narzędziowa przechowujaca gotowe komparatory do sortowania produktow.
// trzymanie komparatorow jako stalych (static final) zgodne jest z zasada DIP (Dependency Inversion Principle):
// metoda Cart.sort() przyjmuje dowolny Comparator z zewnatrz i sama nie decyduje o sposobie sortowania.
// dodanie nowego kryterium sortowania wymaga tylko nowej stalej tutaj — klasa Cart pozostaje bez zmian.
public class ProductComparators {

    // domyslne sortowanie wymagane przez specyfikacje projektu:
    // klucz glowny: cena malejaco (najdrozszy produkt na poczatku listy)
    // klucz drugorzedny: nazwa alfabetycznie rosnaco (uzywa sie gdy ceny sa rowne)
    public static final Comparator<Product> DEFAULT_ORDER = Comparator
            .comparingDouble(Product::getPrice).reversed() // wyzszy koszt idzie pierwszy
            .thenComparing(Product::getName);              // przy remisie cenowym — sortuj A-Z po nazwie

    // alternatywny komparator: sortuje produkty alfabetycznie po kodzie produktu.
    // pokazuje ze system jest otwarty na nowe strategie sortowania bez modyfikowania klasy Cart.
    public static final Comparator<Product> BY_CODE = Comparator
            .comparing(Product::getCode);
}
