package com.oxytuned.aperture;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import me.jxl.kiosk.plugins.KsTheme;
import me.jxl.kiosk.plugins.OverlayFactory;
import me.jxl.kiosk.plugins.OverlaySpec;
import me.jxl.kiosk.plugins.PluginHost;

final class ApertureSkin {
    private static final String KEY="aperture_terminal";
    private static final int ORANGE=0xFFFFA13D;
    private PluginHost host;
    private int generation;
    private boolean selected, skinHeard, overlayHeard, appearanceHeard, textHeard;
    private String overlay="hidden", drawn;
    private volatile float level;
    private volatile String command="", answer="", tool="";
    private volatile String theme="auto";
    private volatile Float background;
    private volatile float textScale=1f;
    private volatile boolean reactive=true;
    private TerminalView view;

    synchronized void start(PluginHost next) {
        host=next; int gen=++generation;
        for(String e:new String[]{"voice.skin","voice.overlay","voice.level","voice.text","voice.appearance"}) host.subscribe(e);
        host.executeCommand("getVoiceState", Collections.<String,Object>emptyMap(), (ok,data,error)->{
            synchronized(ApertureSkin.this){ if(gen!=generation||!ok||!(data instanceof Map))return;
                Map<?,?> m=(Map<?,?>)data;
                if(!skinHeard)selected=Boolean.TRUE.equals(m.get("skinSelected"));
                if(!overlayHeard && m.get("overlay") instanceof String)overlay=(String)m.get("overlay");
                update();
            }
        });
        host.executeCommand("getVoiceAppearance",Collections.<String,Object>emptyMap(),(ok,data,error)->{
            synchronized(ApertureSkin.this){if(gen==generation&&ok&&data instanceof Map&&!appearanceHeard)appearance((Map<?,?>)data);}
        });
        host.executeCommand("getVoiceText",Collections.<String,Object>emptyMap(),(ok,data,error)->{
            synchronized(ApertureSkin.this){if(gen==generation&&ok&&data instanceof Map&&!textHeard)words((Map<?,?>)data);}
        });
    }
    synchronized void onEvent(String e,Map<String,Object> p){
        if(host==null||p==null)return;
        switch(e){
            case "ks.voice.skin":selected=Boolean.TRUE.equals(p.get("selected"));skinHeard=true;update();break;
            case "ks.voice.overlay":overlay=String.valueOf(p.get("overlay"));overlayHeard=true;update();break;
            case "ks.voice.level":level=p.get("level") instanceof Number?Math.max(0,Math.min(1,((Number)p.get("level")).floatValue())):0;break;
            case "ks.voice.text":textHeard=true;words(p);break;
            case "ks.voice.appearance":appearanceHeard=true;appearance(p);break;
        }
    }
    private void appearance(Map<?,?> p){
        theme=p.get("theme") instanceof String?(String)p.get("theme"):"auto";
        background=p.get("background") instanceof Number?((Number)p.get("background")).floatValue():null;
        textScale=p.get("textScale") instanceof Number?Math.max(.5f,Math.min(2f,((Number)p.get("textScale")).floatValue())):1f;
        reactive=!Boolean.FALSE.equals(p.get("reactive"));
        TerminalView v=view;if(v!=null)v.post(v::restyle);
    }
    private void words(Map<?,?> p){
        command=p.get("command") instanceof String?(String)p.get("command"):"";
        answer=p.get("answer") instanceof String?(String)p.get("answer"):"";
        Object tools=p.get("tools");tool=tools instanceof List&&!((List<?>)tools).isEmpty()?String.valueOf(((List<?>)tools).get(((List<?>)tools).size()-1)):"";
        TerminalView v=view;if(v!=null)v.post(v::refresh);
    }
    private void update(){
        String want=selected&&("full".equals(overlay)||"docked".equals(overlay))?overlay:null;
        if(want==null?drawn==null:want.equals(drawn))return;
        drawn=want;
        if(want==null){host.hideOverlay(KEY);level=0;return;}
        OverlaySpec spec="full".equals(want)?OverlaySpec.fullScreen().onTop(true).touchable(false).closeOnBack(false):OverlaySpec.at(OverlaySpec.BOTTOM).inset(18).onTop(true).touchable(false).closeOnBack(false);
        host.showOverlay(KEY,spec,new Factory("docked".equals(want)));
    }
    synchronized void stop(){
        if(host!=null)host.hideOverlay(KEY);
        host=null;generation++;selected=false;skinHeard=overlayHeard=appearanceHeard=textHeard=false;
        overlay="hidden";drawn=null;view=null;level=0;command=answer=tool="";
    }
    private final class Factory implements OverlayFactory{
        final boolean docked;Factory(boolean d){docked=d;}
        @Override public View create(Context c,KsTheme t){TerminalView v=new TerminalView(c,t,docked);view=v;return v;}
        @Override public void onThemeChanged(View v,KsTheme t){((TerminalView)v).setTheme(t);}
        @Override public void onDestroy(View v){if(view==v)view=null;}
    }
    private final class TerminalView extends FrameLayout {
        final boolean docked;
        KsTheme ksTheme;
        final Core core;
        final TextView heading,said,answered;
        final LinearLayout panel;
        TerminalView(Context c,KsTheme t,boolean dock){
            super(c);ksTheme=t;docked=dock;setClipChildren(false);
            panel=new LinearLayout(c);panel.setOrientation(dock?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL);panel.setGravity(Gravity.CENTER);
            core=new Core(c);
            heading=new TextView(c);said=new TextView(c);answered=new TextView(c);
            heading.setText("APERTURE SCIENCE  //  AI TERMINAL");heading.setGravity(Gravity.CENTER);
            said.setGravity(Gravity.CENTER);answered.setGravity(Gravity.CENTER);
            if(dock){
                panel.addView(core,new LinearLayout.LayoutParams(t.px(96),t.px(96)));
                LinearLayout words=new LinearLayout(c);words.setOrientation(LinearLayout.VERTICAL);
                words.addView(heading);words.addView(said);words.addView(answered);
                LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.leftMargin=t.px(12);panel.addView(words,lp);
                int width=Math.min(t.px(620),c.getResources().getDisplayMetrics().widthPixels-t.px(36));
                addView(panel,new FrameLayout.LayoutParams(width,-2,Gravity.CENTER));
                answered.setMaxLines(3);answered.setEllipsize(TextUtils.TruncateAt.END);
                said.setMaxLines(2);said.setEllipsize(TextUtils.TruncateAt.END);
            }else{
                int dim=Math.min(c.getResources().getDisplayMetrics().widthPixels,c.getResources().getDisplayMetrics().heightPixels);
                panel.addView(heading);
                panel.addView(core,new LinearLayout.LayoutParams((int)(dim*.58f),(int)(dim*.58f)));
                LinearLayout.LayoutParams line=new LinearLayout.LayoutParams(-1,-2);line.topMargin=t.px(10);
                panel.addView(said,line);panel.addView(answered,new LinearLayout.LayoutParams(-1,-2));
                panel.setPadding(t.px(22),t.px(12),t.px(22),t.px(12));
                addView(panel,new FrameLayout.LayoutParams(-1,-1));
                answered.setMaxLines(6);answered.setEllipsize(TextUtils.TruncateAt.END);
            }
            restyle();refresh();
        }
        void setTheme(KsTheme t){ksTheme=t;restyle();}
        void restyle(){
            boolean dark="dark".equals(theme)||(!"light".equals(theme)&&ksTheme.isDark());
            if(docked){
                GradientDrawable bg=new GradientDrawable();bg.setColor(dark?0xF508101B:0xF5E8EDF2);bg.setCornerRadius(ksTheme.px(16));bg.setStroke(ksTheme.px(1),0xCCB76B2C);panel.setBackground(bg);
                panel.setPadding(ksTheme.px(12),ksTheme.px(10),ksTheme.px(16),ksTheme.px(10));
            }else{
                float alpha=background==null?.94f:Math.max(0,Math.min(1,background));
                int base=dark?0xFF040A12:0xFFF1F3F6;
                setBackgroundColor(Color.argb(Math.round(255*alpha),Color.red(base),Color.green(base),Color.blue(base)));
            }
            int foreground=dark?0xFFE8F3F9:0xFF152434;
            heading.setTextColor(ORANGE);heading.setTextSize(11*textScale);heading.setLetterSpacing(.16f);
            said.setTextColor(dark?0xFF8EA7B6:0xFF4E6779);said.setTextSize((docked?12:17)*textScale);
            answered.setTextColor(foreground);answered.setTextSize((docked?14:22)*textScale);
            core.invalidate();
        }
        void refresh(){said.setText(command.isEmpty()?"":"> "+command);said.setVisibility(command.isEmpty()?GONE:VISIBLE);
            String response=answer.isEmpty()?tool:answer;answered.setText(response);answered.setVisibility(response.isEmpty()?GONE:VISIBLE);}
        @Override protected void onAttachedToWindow(){super.onAttachedToWindow();core.start();}
        @Override protected void onDetachedFromWindow(){core.stop();super.onDetachedFromWindow();}
    }
    private final class Core extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);final RectF r=new RectF();
        boolean running;float eased;long started;
        Core(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        void start(){running=true;started=SystemClock.uptimeMillis();postInvalidateOnAnimation();}
        void stop(){running=false;}
        void ring(Canvas c,float radius,float start,float sweep,int color,float stroke){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(stroke);p.setColor(color);p.setStrokeCap(Paint.Cap.ROUND);r.set(-radius,-radius,radius,radius);c.drawArc(r,start,sweep,false,p);}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            float size=Math.min(getWidth(),getHeight());if(size<=0)return;
            canvas.save();canvas.translate(getWidth()/2f,getHeight()/2f);canvas.scale(size/250f,size/250f);
            float t=(SystemClock.uptimeMillis()-started)/1000f;
            float target=reactive?level:0;eased+=(target-eased)*.16f;
            p.setStyle(Paint.Style.FILL);p.setColor(0xFF09131D);canvas.drawCircle(0,0,111,p);
            p.setColor(0x2223C9DF);for(int i=-100;i<=100;i+=20){canvas.drawRect(i,-102,i+0.6f,102,p);canvas.drawRect(-102,i,102,i+.6f,p);}
            ring(canvas,109,0,360,0x664B7384,1);
            ring(canvas,101,0,360,0xFF243F50,3);
            for(int i=0;i<12;i++){float a=i*30+(t*9)%360;ring(canvas,101,a,11,0x88FF9D34,2);}
            ring(canvas,91,-90+t*16,100,ORANGE,3);
            ring(canvas,91,90-t*12,56,0xFF4AD7E8,2);
            ring(canvas,77,-t*24,220,0xAAFFB76D,1.4f);
            for(int i=0;i<40;i++){
                float a=(float)(i*Math.PI*2/40);float radius=63;float height=5+eased*22*(.3f+.7f*(float)Math.abs(Math.sin(i*.77+t*5)));
                float x=(float)Math.cos(a),y=(float)Math.sin(a);
                p.setStrokeWidth(2.2f);p.setColor(i%5==0?0xFFFFC875:0xFFDB7B2B);canvas.drawLine(x*radius,y*radius,x*(radius+height),y*(radius+height),p);
            }
            p.setStyle(Paint.Style.FILL);p.setColor(0x44FF9E3B);canvas.drawCircle(0,0,42+eased*6,p);
            p.setColor(0xFFFFA13D);canvas.drawCircle(0,0,28+eased*5,p);
            p.setColor(0xFFFFE4AF);canvas.drawCircle(0,0,14+eased*3,p);
            p.setColor(0xFF2C1708);canvas.drawCircle(0,0,5,p);
            p.setTypeface(android.graphics.Typeface.create("monospace",android.graphics.Typeface.BOLD));p.setTextAlign(Paint.Align.CENTER);p.setTextSize(8);
            p.setColor(0xFFFFC179);canvas.drawText("A P E R T U R E",0,119,p);
            canvas.restore();if(running)postInvalidateDelayed(33);
        }
    }
}
