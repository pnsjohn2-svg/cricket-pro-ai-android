package com.pnsjohn.cricketproai.model;

public final class LiveState {
    public String match="", status="", batting="", bowling="", striker="", nonStriker="", bowler="", toss="", venue="";
    public int score=0, wickets=0, balls=0, target=0, last12Runs=0, last12Wickets=0, last12Boundaries=0;
    public long fetchedAt=System.currentTimeMillis();
    public boolean valid;
    public double runRate() { return balls==0 ? 0 : score*6.0/balls; }
    public String overs() { return (balls/6)+"."+(balls%6); }
}
