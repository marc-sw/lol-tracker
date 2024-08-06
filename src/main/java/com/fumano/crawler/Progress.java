package com.fumano.crawler;

public class Progress {

    private final int total;
    private final int stepSize;
    private int completed;

    public Progress(int total) {
        this.total = total;
        completed = 0;
        stepSize = total <= 100 ? 1: total / 100;
    }

    public void increase() {
        completed++;
        if (completed % stepSize == 0) {
            System.out.printf("%d%c\n",completed * 100 / total, '%');
        }
    }
}
