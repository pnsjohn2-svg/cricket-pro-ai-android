package com.pnsjohn.cricketproai;

import android.app.*; import android.os.*; import android.graphics.Color; import android.view.*; import android.widget.*;
import com.pnsjohn.cricketproai.analytics.*; import com.pnsjohn.cricketproai.data.CrexClient; import com.pnsjohn.cricketproai.model.*;
import java.text.*; import java.util.*; import java.util.concurrent.*;

public final class MainActivity extends Activity {
    private final ExecutorService io=Executors.newSingleThreadExecutor(); private final CrexClient client=new CrexClient();
    private Spinner matches; private TextView status,score,matchup,forecast,coach; private Button refresh; private List<LiveMatch> live=new ArrayList<>(); private Handler handler;
    private final Runnable auto=new Runnable(){public void run(){refreshState();handler.postDelayed(this,15000);}};
    @Override public void onCreate(Bundle b){super.onCreate(b);handler=new Handler(Looper.getMainLooper());buildUi();loadMatches();}
    private TextView tv(int size,int color){TextView v=new TextView(this);v.setTextSize(size);v.setTextColor(color);v.setPadding(18,10,18,10);return v;}
    private void buildUi(){
        ScrollView scroll=new ScrollView(this); LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(16,28,16,24);root.setBackgroundColor(Color.rgb(7,26,43));scroll.addView(root);
        TextView title=tv(25,Color.WHITE);title.setText("CRICKET PRO AI");title.setGravity(Gravity.CENTER);root.addView(title);
        status=tv(13,Color.rgb(93,220,177));status.setText("Finding live matches…");status.setGravity(Gravity.CENTER);root.addView(status);
        matches=new Spinner(this);root.addView(matches,new LinearLayout.LayoutParams(-1,-2));
        refresh=new Button(this);refresh.setText("REFRESH LIVE");refresh.setOnClickListener(v->refreshState());root.addView(refresh);
        score=tv(31,Color.WHITE);score.setGravity(Gravity.CENTER);root.addView(score);
        matchup=tv(15,Color.LTGRAY);root.addView(matchup);forecast=tv(17,Color.WHITE);root.addView(forecast);coach=tv(15,Color.rgb(210,225,236));root.addView(coach);setContentView(scroll);
        matches.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){} public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){refreshState();}});
    }
    private void loadMatches(){status.setText("CREX PRIMARY • discovering live matches");io.execute(()->{try{List<LiveMatch> x=client.liveMatches();runOnUiThread(()->{live=x;matches.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,x));status.setText(x.isEmpty()?"No parsable live matches found":"Live matches: "+x.size());if(!x.isEmpty())handler.post(auto);});}catch(Exception e){error("Match discovery",e);}});}
    private void refreshState(){int p=matches.getSelectedItemPosition();if(p<0||p>=live.size())return;LiveMatch m=live.get(p);refresh.setEnabled(false);io.execute(()->{try{LiveState s=client.state(m);Forecast f=new AnalyticsEngine().forecast(s);runOnUiThread(()->render(s,f));}catch(Exception e){error("Live refresh",e);}finally{runOnUiThread(()->refresh.setEnabled(true));}});}
    private void render(LiveState s,Forecast f){String time=new SimpleDateFormat("HH:mm:ss",Locale.getDefault()).format(new Date(s.fetchedAt));status.setText(s.status+" • "+time);score.setText(s.score+"/"+s.wickets+"  ("+s.overs()+")");matchup.setText("LIVE MATCHUP\nStriker: "+or(s.striker)+"\nNon-striker: "+or(s.nonStriker)+"\nBowler: "+or(s.bowler)+"\nVenue: "+or(s.venue)+"\n"+or(s.toss));forecast.setText("PROFESSIONAL FORECAST\nCentral: "+f.central+"\n80% range: "+f.low80+" – "+f.high80+"\n50% core: "+f.low50+" – "+f.high50+"\nNext over: "+f.nextOver+" runs\nNext 2 overs: "+f.nextTwoOvers+" runs\nBoundary next over: "+f.boundaryNext+"%\nWicket next over: "+f.wicketNext+"%"+(s.target>0?"\nWin probability: "+f.winProbability+"%":"")+"\nInput confidence: "+f.confidence+"%");coach.setText("COACH ANALYSIS\n"+f.coach);}
    private String or(String x){return x==null||x.isBlank()?"Not available":x;}
    private void error(String where,Exception e){runOnUiThread(()->status.setText(where+" failed: "+e.getMessage()));}
    @Override protected void onDestroy(){handler.removeCallbacks(auto);io.shutdownNow();super.onDestroy();}
}
