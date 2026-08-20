package com.pnsjohn.cricketproai.analytics;

import com.pnsjohn.cricketproai.model.LiveState;
import java.util.*;

public final class AnalyticsEngine {
    private static double clamp(double x,double lo,double hi){return Math.max(lo,Math.min(hi,x));}
    private static int q(List<Integer> a,double p){return a.get((int)Math.round((a.size()-1)*p));}

    public Forecast forecast(LiveState s) {
        Forecast f=new Forecast();
        int total=120, rem=Math.max(0,total-s.balls);
        double inningsRpb=s.balls>0?s.score/(double)s.balls:1.25;
        double recentRpb=s.last12Runs>0?s.last12Runs/12.0:inningsRpb;
        double wicketPressure=clamp(s.wickets/10.0+s.last12Wickets*.10,0,1);
        double momentum=clamp((recentRpb-inningsRpb)*.22,-.22,.22);
        double future=clamp(inningsRpb*(1-wicketPressure*.22)+momentum,.42,2.35);
        double pBoundary=clamp(.11+s.last12Boundaries*.015+momentum*.08,.05,.30);
        double pWicket=clamp(.032+s.last12Wickets*.012+Math.max(0,s.wickets-5)*.006,.018,.16);
        f.momentum=recentRpb>inningsRpb*1.12?"Positive":recentRpb<inningsRpb*.86?"Negative":"Neutral";
        f.nextOver=Math.round(future*Math.min(6,rem)*10)/10.0;
        f.nextTwoOvers=Math.round(future*Math.min(12,rem)*10)/10.0;
        f.boundaryNext=Math.round((1-Math.pow(1-pBoundary,Math.min(6,rem)))*1000)/10.0;
        f.wicketNext=Math.round((1-Math.pow(1-pWicket,Math.min(6,rem)))*1000)/10.0;

        Random rng=new Random(Objects.hash(s.score,s.wickets,s.balls,s.last12Runs));
        List<Integer> totals=new ArrayList<>(1200);
        for(int n=0;n<1200;n++){
            int runs=s.score,lost=0;
            for(int b=0;b<rem && s.wickets+lost<10;b++){
                if(rng.nextDouble()<clamp(pWicket*(1+lost*.08),.01,.22)){lost++;continue;}
                double u=rng.nextDouble();
                if(u<pBoundary) runs+=rng.nextDouble()<.24?6:4;
                else { double dot=clamp(.60-future*.20,.28,.65); double v=rng.nextDouble(); runs+=v<dot?0:v<dot+.30?1:v<dot+.39?2:3; }
            }
            totals.add(runs);
        }
        Collections.sort(totals);
        f.central=q(totals,.50); f.low80=q(totals,.10); f.high80=q(totals,.90); f.low50=q(totals,.25); f.high50=q(totals,.75);
        if(s.target>0){ long wins=totals.stream().filter(x->x>=s.target).count(); f.winProbability=clamp(wins*100.0/totals.size(),1,99); }
        f.confidence=clamp(35+(s.valid?20:0)+(s.striker.isEmpty()?0:10)+(s.bowler.isEmpty()?0:10)+(s.balls>0?15:0),20,90);
        f.coach=coach(s,f,rem);
        return f;
    }

    private String coach(LiveState s,Forecast f,int rem){
        StringBuilder b=new StringBuilder();
        b.append("Current tempo is ").append(f.momentum.toLowerCase(Locale.ROOT)).append(". ");
        if(s.wickets>=6)b.append("Wicket loss creates collapse pressure. ");
        else if(s.wickets<=2)b.append("Wickets in hand support controlled acceleration. ");
        if(f.wicketNext>=30)b.append("The next over has meaningful wicket risk. ");
        if(f.boundaryNext>=55)b.append("At least one boundary in the next over is more likely than not. ");
        b.append("Central finish ").append(f.central).append(" with an 80% analytical band of ").append(f.low80).append("–").append(f.high80).append(". ");
        b.append(rem).append(" legal balls remain. Confidence reflects feed completeness, not guaranteed accuracy.");
        return b.toString();
    }
}
