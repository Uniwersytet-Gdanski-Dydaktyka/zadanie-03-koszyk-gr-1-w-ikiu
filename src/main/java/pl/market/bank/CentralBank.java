package pl.market.bank;

import pl.market.market.Transaction;
import pl.market.observers.InflationObserver;
import pl.market.observers.InflationSubject;
import pl.market.observers.TransactionObserver;
import pl.market.simulation.SimulationConfig;

import java.util.ArrayList;
import java.util.List;

public class CentralBank implements InflationSubject, TransactionObserver {
    private double inflationRate;
    private final double taxRate;
    private double targetTaxRevenue;
    private final SimulationConfig config;
    private final List<InflationObserver> observers = new ArrayList<>();

    private double currentTurnRevenue;
    private double lastTurnTaxRevenue;
    private int currentTurnTransactionCount;

    public CentralBank(double initialInflationRate, double taxRate,
                       double targetTaxRevenue, SimulationConfig config) {
        this.inflationRate = initialInflationRate;
        this.taxRate = taxRate;
        this.targetTaxRevenue = targetTaxRevenue;
        this.config = config;
    }

    public void computeAndAnnounceInflation() {
        if (currentTurnTransactionCount > 0) {
            lastTurnTaxRevenue = currentTurnRevenue * taxRate;
            double band = config.getTaxRevenueToleranceBand();
            if (lastTurnTaxRevenue < targetTaxRevenue * (1.0 - band)) {
                inflationRate = Math.min(
                        inflationRate + config.getInflationAdjustmentStep(),
                        config.getMaxInflationRate());
            } else if (lastTurnTaxRevenue > targetTaxRevenue * (1.0 + band)) {
                inflationRate = Math.max(
                        inflationRate - config.getInflationAdjustmentStep(),
                        config.getMinInflationRate());
            }
        } else {
            // Frozen market: reduce inflation aggressively to restart trade
            inflationRate = Math.max(
                    inflationRate - config.getInflationAdjustmentStep() * 3,
                    config.getMinInflationRate());
        }
        currentTurnRevenue = 0;
        currentTurnTransactionCount = 0;
        notifyInflationObservers(inflationRate);
    }

    public void applyInflationShock(double shockAmount) {
        inflationRate = Math.max(config.getMinInflationRate(),
                Math.min(config.getMaxInflationRate(), inflationRate + shockAmount));
        notifyInflationObservers(inflationRate);
    }

    @Override
    public void onTransaction(Transaction transaction) {
        currentTurnRevenue += transaction.getTotalValue();
        currentTurnTransactionCount++;
    }

    @Override
    public void addInflationObserver(InflationObserver observer) { observers.add(observer); }

    @Override
    public void removeInflationObserver(InflationObserver observer) { observers.remove(observer); }

    @Override
    public void notifyInflationObservers(double rate) {
        observers.forEach(o -> o.onInflationChanged(rate));
    }

    public double getInflationRate() { return inflationRate; }
    public void setInflationRate(double rate) { this.inflationRate = rate; }
    public double getTaxRate() { return taxRate; }
    public double getTargetTaxRevenue() { return targetTaxRevenue; }
    public void setTargetTaxRevenue(double target) { this.targetTaxRevenue = target; }
    public double getCurrentTurnRevenue() { return currentTurnRevenue; }
    public double getLastTurnTaxRevenue() { return lastTurnTaxRevenue; }
    public int getCurrentTurnTransactionCount() { return currentTurnTransactionCount; }
}
