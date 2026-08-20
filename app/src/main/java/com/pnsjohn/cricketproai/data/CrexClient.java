package com.pnsjohn.cricketproai.data;

import com.pnsjohn.cricketproai.model.*;
import java.io.*; import java.net.*; import java.nio.charset.StandardCharsets; import java.util.*; import java.util.regex.*;

public final class CrexClient {
    private static final String BASE="https://crex.com";
    private String get(String url) throws IOException {
        HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();
        c.setConnectTimeout(12000);c.setReadTimeout(12000);c.setInstanceFollowRedirects(true);
        c.setRequestProperty("User-Agent","Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/126 Mobile Safari/537.36");
        c.setRequestProperty("Accept-Language","en-US,en;q=0.9");
        try(InputStream in=c.getInputStream()){return new String(in.readAllBytes(),StandardCharsets.UTF_8);}
    }
    private static String text(String h){return h.replaceAll("(?is)<script.*?</script>"," ").replaceAll("(?is)<style.*?</style>"," ").replaceAll("<[^>]+>"," ").replace("&amp;","&").replaceAll("\\s+"," ").trim();}
    public List<LiveMatch> liveMatches() throws IOException {
        String h=get(BASE+"/cricket-live-score");
        Matcher m=Pattern.compile("href=\\\"([^\\\"]*(?:live-score|scorecard)[^\\\"]*)\\\"[^>]*>(.*?)</a>",Pattern.CASE_INSENSITIVE|Pattern.DOTALL).matcher(h);
        LinkedHashMap<String,LiveMatch> out=new LinkedHashMap<>();
        while(m.find()){
            String u=m.group(1); if(!u.startsWith("http"))u=BASE+(u.startsWith("/")?u:"/"+u);
            String title=text(m.group(2)); if(title.length()<5)continue;
            out.put(u,new LiveMatch(title.length()>90?title.substring(0,90):title,u));
        }
        return new ArrayList<>(out.values());
    }
    public LiveState state(LiveMatch match) throws IOException {
        String h=get(match.url), t=text(h); LiveState s=new LiveState(); s.match=match.title;
        Matcher sc=Pattern.compile("(?:^|\\s)(\\d{1,3})\\s*[/\\-]\\s*(\\d)\\s*(?:\\(|,|after|\\s)+(\\d{1,2})(?:\\.(\\d))?\\s*(?:ov|overs?)",Pattern.CASE_INSENSITIVE).matcher(t);
        if(sc.find()){s.score=Integer.parseInt(sc.group(1));s.wickets=Integer.parseInt(sc.group(2));s.balls=Integer.parseInt(sc.group(3))*6+(sc.group(4)==null?0:Integer.parseInt(sc.group(4)));s.valid=true;}
        Matcher toss=Pattern.compile("([A-Za-z][A-Za-z .&'-]{2,50} won the toss[^.]{0,90})",Pattern.CASE_INSENSITIVE).matcher(t); if(toss.find())s.toss=toss.group(1).trim();
        Matcher venue=Pattern.compile("Venue\\s*:?\\s*([A-Za-z0-9 ,.&'-]{4,80})",Pattern.CASE_INSENSITIVE).matcher(t);if(venue.find())s.venue=venue.group(1).trim();
        Matcher bats=Pattern.compile("([A-Z][A-Za-z .'-]{2,35})\\s+(\\d{1,3})\\s+(\\d{1,3})\\s+(?:\\d+\\s+){0,2}(\\d{2,3}\\.\\d+)").matcher(t);
        if(bats.find())s.striker=bats.group(1).trim(); if(bats.find())s.nonStriker=bats.group(1).trim();
        Matcher target=Pattern.compile("Target\\s*:?\\s*(\\d{2,3})",Pattern.CASE_INSENSITIVE).matcher(t);if(target.find())s.target=Integer.parseInt(target.group(1));
        s.status=s.valid?"CREX PRIMARY • live state received":"CREX page received • score markup not recognized"; s.fetchedAt=System.currentTimeMillis(); return s;
    }
}
