package com.palm.bank.common;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum Unit {
    WEI("wei", 0),

    KWEI("kwei", 3),

    WWEI("wwei", 4),

    MWEI("mwei", 6),

    LWEI("lwei", 8),

    GWEI("gwei", 9),

    SZABO("szabo", 12),

    FINNEY("finney", 15),

    ETHER("ether", 18),

    KETHER("kether", 21),

    METHER("mether", 24),

    GETHER("gether", 27);

    private final String name;
    private final BigDecimal weiFactor;

    Unit(String name, int factor) {
        this.name = name;
        this.weiFactor = BigDecimal.TEN.pow(factor);
    }

    public static Unit fromUnit(String name) {
        for (Unit unit : Unit.values()) {
            if (unit.getName().equalsIgnoreCase(name)) {
                return unit;
            }
        }
        return Unit.WEI;
    }
}
