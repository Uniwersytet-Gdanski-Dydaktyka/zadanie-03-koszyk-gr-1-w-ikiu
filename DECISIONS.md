1. Mutowalność klasy Product (Uwaga 4)
   Wybieramy niemutowalność (immutability).

Dlaczego? Produkt w sklepie nie powinien zmieniać swojej ceny w trakcie działania programu. Jeśli nałożymy na niego promocję, znacznie bezpieczniej jest stworzyć nową kopię tego produktu ze zmienionym polem discountPrice, pozostawiając oryginał nietknięty. Zapobiega to błędom, gdy np. dwie różne promocje próbują nadpisać tę samą cenę.

2. Wzorzec projektowy dla promocji (Uwaga 2)
   Zastosujemy wzorzec Strategy.

Dlaczego nie Command? Wzorzec Command służy głównie do kolejkowania operacji i robienia "Undo". Naszym celem jest po prostu wzięcie koszyka i przepuszczenie go przez różne "filtry" cenowe.

Dlaczego Strategy? Pozwala on na zdefiniowanie rodziny algorytmów (różnych promocji), zamknięcie każdego z nich w osobnej klasie i używanie ich zamiennie. Idealnie spełnia to zasadę OCP (Open/Closed Principle) – jeśli marketing wymyśli nową promocję, po prostu dopiszemy nową klasę implementującą interfejs Promotion, bez dotykania kodu samego koszyka.