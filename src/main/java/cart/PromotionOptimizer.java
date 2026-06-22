package cart;

import cart.promotions.Promotion;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// zadanie dodatkowe (+3 pkt): znajduje taka kolejnosc zastosowania promocji,
// ktora daje klientowi najnizsza mozliwa cene koszyka.
// promocje nie sa przemienne — zastosowanie A potem B moze dac inny wynik niz B potem A,
// wiec musimy wyprobowac kazda mozliwa kolejnosc (permutacje) i wybrac najtanszy wariant.
// to podejscie "brute force" — poprawne dla malych list promocji uzywanych w praktyce.
public class PromotionOptimizer {

    // glowna metoda: przyjmuje koszyk (nie zmodyfikowany przez caly czas) i liste dostepnych promocji.
    // zwraca OptimizationResult z najlepsza cena i kolejnoscia promocji, ktora ja osiaga.
    public static OptimizationResult getBestPrice(Cart cart, List<Promotion> availablePromotions) {
        // jesli nie ma zadnych promocji do wyprobowania, cena to po prostu aktualna suma koszyka
        if (availablePromotions == null || availablePromotions.isEmpty()) {
            return new OptimizationResult(cart.getTotalPrice(), Collections.emptyList());
        }

        // generujemy kazda mozliwa kolejnosc listy promocji.
        // dla n promocji istnieje n! permutacji (np. 3 promocje -> 6 kolejnosci do sprawdzenia).
        List<List<Promotion>> allPermutations = generatePermutations(new ArrayList<>(availablePromotions));

        // inicjalizujemy zmienne sledzace: zaczynamy od najgorszej mozliwej ceny
        double bestPrice = Double.MAX_VALUE;
        List<Promotion> bestOrder = allPermutations.get(0); // domyslnie pierwsza kolejnosc

        // sprawdzamy kazda permutacje i sledzymy ktora daje najnizsza sume
        for (List<Promotion> permutation : allPermutations) {
            // tworzymy swiezy tymczasowy koszyk, zeby nie modyfikowac oryginalu przekazanego przez wywolujacego
            Cart tempCart = new Cart();
            for (Product p : cart.getProducts()) {
                // kopiujemy kazdy produkt uzywajac jego oryginalnej ceny — resetuje to rabaty z poprzedniej iteracji
                tempCart.addProduct(new Product(p.getCode(), p.getName(), p.getPrice()));
            }

            // aplikujemy promocje w kolejnosci wyznaczonej przez ta permutacje
            for (Promotion promo : permutation) {
                tempCart.applyPromotion(promo);
            }

            // porownujemy uzyskana sume z dotychczas najlepsza
            double currentPrice = tempCart.getTotalPrice();
            if (currentPrice < bestPrice) {
                bestPrice = currentPrice;
                bestOrder = permutation; // zapamietujemy kolejnosc ktora pobila poprzedni rekord
            }
        }

        return new OptimizationResult(bestPrice, bestOrder);
    }

    // rekurencyjny algorytm generujacy wszystkie permutacje podanej listy.
    // podejscie: bierzemy pierwszy element, generujemy permutacje reszty listy,
    // nastepnie wstawiamy ten pierwszy element na kazda mozliwa pozycje w kazdej sub-permutacji.
    // przypadek bazowy: pusta lista ma dokladnie jedna permutacje — samaa siebie (pusta liste).
    private static List<List<Promotion>> generatePermutations(List<Promotion> original) {
        List<List<Promotion>> result = new ArrayList<>();

        // przypadek bazowy: pusta lista ma dokladnie jedna permutacje (sama pusta lista)
        if (original.isEmpty()) {
            result.add(new ArrayList<>());
            return result;
        }

        // usuwamy pierwszy element — bedziemy go wstawiac z powrotem na kazda pozycje w sub-permutacjach
        Promotion firstElement = original.remove(0);

        // rekurencyjnie pobieramy wszystkie permutacje pozostalych elementow
        List<List<Promotion>> recursiveReturn = generatePermutations(original);

        // dla kazdej sub-permutacji wstawiamy firstElement na kazdy mozliwy indeks (od 0 do size)
        for (List<Promotion> li : recursiveReturn) {
            for (int index = 0; index <= li.size(); index++) {
                List<Promotion> temp = new ArrayList<>(li); // kopiujemy zeby nie modyfikowac sub-permutacji
                temp.add(index, firstElement);              // wstawiamy pierwszy element na pozycje index
                result.add(temp);
            }
        }
        return result;
    }
}
