package com.hardermc.Objects;

import java.util.ArrayList;
import java.util.List;

public class MultiplierGroup {
    private final List<Double> multipliers = new ArrayList<>();
    private final double max;

    public MultiplierGroup(double max) {
        this.max = max;
        this.multipliers.add(1.0);
    }

    public void add(double multiplier) {
        multipliers.add(multiplier);
    }

    public void remove(double multiplier) {
        multipliers.remove(multiplier);
    }

    public double getTotal() {
        return Math.min(multipliers.stream().reduce(1.0, (a, b) -> a.doubleValue() * b.doubleValue()).doubleValue(),
                max);
    }
}