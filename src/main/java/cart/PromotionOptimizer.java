package cart;

import cart.promotions.Promotion;
import java.util.ArrayList;
import java.util.List;

public class PromotionOptimizer {

    // glowna metoda do zadania dodatkowego
    // zwraca najnizsza mozliwa cene po przetestowaniu wszystkich kombinacji
    public static double getBestPrice(Cart cart, List<Promotion> availablePromotions) {
        if (availablePromotions == null || availablePromotions.isEmpty()) {
            return cart.getTotalPrice();
        }

        List<List<Promotion>> allPermutations = generatePermutations(new ArrayList<>(availablePromotions));
        double bestPrice = Double.MAX_VALUE;

        for (List<Promotion> permutation : allPermutations) {
            // tworzymy tymczasowy koszyk dla kazdej kombinacji, zeby nie zepsuc oryginalu
            Cart tempCart = new Cart();
            for (Product p : cart.getProducts()) {
                // wkladamy do niego swieze produkty (resetujac wczesniejsze znizki)
                tempCart.addProduct(new Product(p.getCode(), p.getName(), p.getPrice()));
            }

            // aplikujemy promocje w wylosowanej przez permutacje kolejnosci
            for (Promotion promo : permutation) {
                tempCart.applyPromotion(promo);
            }

            double currentPrice = tempCart.getTotalPrice();
            if (currentPrice < bestPrice) {
                bestPrice = currentPrice;
            }
        }

        return bestPrice;
    }

    // rekurencyjny algorytm generujacy wszystkie mozliwe kolejnosci z listy
    private static List<List<Promotion>> generatePermutations(List<Promotion> original) {
        List<List<Promotion>> result = new ArrayList<>();
        if (original.isEmpty()) {
            result.add(new ArrayList<>());
            return result;
        }

        Promotion firstElement = original.remove(0);
        List<List<Promotion>> recursiveReturn = generatePermutations(original);

        for (List<Promotion> li : recursiveReturn) {
            for (int index = 0; index <= li.size(); index++) {
                List<Promotion> temp = new ArrayList<>(li);
                temp.add(index, firstElement);
                result.add(temp);
            }
        }
        return result;
    }
}