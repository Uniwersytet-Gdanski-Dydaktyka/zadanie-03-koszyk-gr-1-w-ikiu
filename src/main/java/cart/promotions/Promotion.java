package cart.promotions;

import cart.Product;
import java.util.List;

// interfejs Promotion to serce wzorca projektowego Strategy uzytego do obslugi promocji.
// kazda konkretna promocja (np. ValueDiscountPromotion, BuyTwoGetOneFreePromotion) implementuje ten interfejs.
// interfejs przyjmuje aktualny stan listy produktow i zwraca NOWA liste z zaktualizowanymi cenami rabatowymi.
// zadna implementacja nie modyfikuje obiektow Product w miejscu — zamiast tego wywoluje withDiscountPrice()
// ktore tworzy nowe kopie. dzieki temu spelniana jest zasada OCP (Open/Closed Principle):
// dodanie nowej promocji wymaga tylko nowej klasy — Cart i pozostale promocje pozostaja bez zmian.
public interface Promotion {

    // przyjmuje pelna liste produktow w ich aktualnym stanie (z juz zastosowanymi wczesniejszymi rabatami)
    // i zwraca zmodyfikowana liste. lista wejsciowa NIE powinna byc mutowana — nalezy zwrocic nowa liste.
    List<Product> apply(List<Product> products);
}
