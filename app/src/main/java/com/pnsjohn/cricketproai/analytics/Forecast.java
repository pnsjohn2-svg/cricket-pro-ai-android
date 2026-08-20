package com.pnsjohn.cricketproai.analytics;

public final class Forecast {
    public int central, low80, high80, low50, high50;
    public double nextOver, nextTwoOvers, boundaryNext, wicketNext, winProbability, confidence;
    public String momentum="Neutral", coach="";
}
