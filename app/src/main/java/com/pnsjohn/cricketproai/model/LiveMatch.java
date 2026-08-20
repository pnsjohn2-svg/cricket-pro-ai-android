package com.pnsjohn.cricketproai.model;

public final class LiveMatch {
    public final String title, url;
    public LiveMatch(String title, String url) { this.title=title; this.url=url; }
    @Override public String toString() { return title; }
}
