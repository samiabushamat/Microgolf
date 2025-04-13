package com.example.projectfour;

public enum HoleGroups {

    GROUP_A(0, 9),
    GROUP_B(10, 19),
    GROUP_C(20, 29),
    GROUP_D(30, 39),
    GROUP_E(40, 49);

    private final int start;
    private final int end;

    HoleGroups(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public boolean contains(int hole) {
        return hole >= start && hole <= end;
    }
}

