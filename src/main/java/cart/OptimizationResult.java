package cart;

import cart.promotions.Promotion;
import java.util.Collections;
import java.util.List;

// niemutowalny obiekt wartosci (value object) zwracany przez PromotionOptimizer.getBestPrice().
// laczy dwie powiazane informacje w jeden obiekt:
//   - najnizsza osiagalna cena koszyka
//   - kolejnosc promocji, ktora te cene daje
// oba pola sa final, a lista jest opakowan w unmodifiableList,
// zeby wywolujacy nie mogl zmodyfikowac wyniku po jego otrzymaniu.
public class OptimizationResult {

    private final double bestPrice;          // minimalna cena lacza znaleziona wsrod wszystkich permutacji
    private final List<Promotion> bestOrder; // kolejnosc promocji producujaca bestPrice

    // konstruktor natychmiast owija bestOrder w widok tylko do odczytu,
    // wiec przechowywana lista nie moze byc pozniej zmieniana z zewnatrz.
    public OptimizationResult(double bestPrice, List<Promotion> bestOrder) {
        this.bestPrice = bestPrice;
        this.bestOrder = Collections.unmodifiableList(bestOrder); // zabezpieczenie — wywolujacy nie moze modyfikowac
    }

    // zwraca najnizsza cene laczna znaleziona przez optymalizator
    public double getBestPrice() { return bestPrice; }

    // zwraca kolejnosc promocji osiagajaca bestPrice.
    // lista jest tylko do odczytu — proba dodania/usuniecia elementow rzuci UnsupportedOperationException.
    public List<Promotion> getBestOrder() { return bestOrder; }
}
