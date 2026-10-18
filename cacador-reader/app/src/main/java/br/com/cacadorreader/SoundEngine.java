package br.com.cacadorreader;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/** External UI sound pack player. Assets from THISUX Sound Kit, MIT license. */
public final class SoundEngine {
 public static final int FOCUS=0, ACTIVATE=1, BACK=2, BOUNDARY=3, TOGGLE=4;
 public static final String[] PACK_NAMES={"Suave","Navegação","Clássico","Vivo"};
 private static final ExecutorService IO=Executors.newSingleThreadExecutor();
 private static final String BASE="https://raw.githubusercontent.com/thisuxhq/soundkit/main/low-volume-20db/";
 private static Context app; private static int selectedPack=0; private static float masterVolume=0.55f; private static MediaPlayer player;
 private SoundEngine(){}
 public static void init(Context c){app=c.getApplicationContext();}
 public static void configure(int pack,float volume){selectedPack=Math.max(0,Math.min(3,pack));masterVolume=Math.max(0f,Math.min(1f,volume));}
 public static void play(int kind,float volume){
  if(app==null)return; final int pack=selectedPack; final String path=pathFor(pack,kind);
  final float gain=Math.max(0f,Math.min(1f,volume));
  if(gain<0.01f)return;
  IO.execute(()->{try{
   File f=new File(app.getCacheDir(),"lume_sfx_"+path.replace('/','_'));
   if(!f.exists()||f.length()<500)download(path,f);
   new Handler(Looper.getMainLooper()).post(()->playFile(f,gain));
  }catch(Exception ignored){}});
 }
 private static String pathFor(int p,int k){
  switch(p){
   case 0:return k==ACTIVATE?"complete-and-success/success-1.m4a":k==BOUNDARY?"errors-and-cancel/error-1.m4a":k==BACK?"buttons-and-navigation/collapse.m4a":"buttons-and-navigation/button-1.m4a";
   case 1:return k==ACTIVATE?"buttons-and-navigation/expand.m4a":k==BOUNDARY?"errors-and-cancel/cancel-1.m4a":k==BACK?"buttons-and-navigation/tab-2.m4a":"buttons-and-navigation/tab-1.m4a";
   case 2:return k==ACTIVATE?"complete-and-success/complete-1.m4a":k==BOUNDARY?"errors-and-cancel/error-2.m4a":k==BACK?"buttons-and-navigation/button-3.m4a":"buttons-and-navigation/button-2.m4a";
   default:return k==ACTIVATE?"complete-and-success/success-3.m4a":k==BOUNDARY?"notifications-and-alerts/alert-1.m4a":k==BACK?"notifications-and-alerts/notification-2.m4a":"buttons-and-navigation/button-5.m4a";
  }
 }
 private static void download(String path,File dest)throws Exception{
  HttpURLConnection c=(HttpURLConnection)new URL(BASE+path).openConnection();c.setConnectTimeout(7000);c.setReadTimeout(7000);
  try{if(c.getResponseCode()!=200)throw new java.io.IOException("HTTP "+c.getResponseCode());
   File tmp=new File(dest.getAbsolutePath()+".tmp");
   try(InputStream in=c.getInputStream();FileOutputStream out=new FileOutputStream(tmp)){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}
   if(!tmp.renameTo(dest)){tmp.delete();throw new java.io.IOException("cache");}
  }finally{c.disconnect();}
 }
 private static void playFile(File f,float volume){
  try{
   if(player!=null){try{player.stop();}catch(Exception ignored){}player.release();}
   player=new MediaPlayer();
   AudioAttributes a=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
   player.setAudioAttributes(a);player.setDataSource(f.getAbsolutePath());player.setVolume(volume,volume);
   player.setOnCompletionListener(mp->{mp.release();if(player==mp)player=null;});
   player.setOnErrorListener((mp,w,e)->{mp.release();if(player==mp)player=null;return true;});
   player.prepare();player.start();
  }catch(Exception ignored){if(player!=null){try{player.release();}catch(Exception ignoredAgain){}player=null;}}
 }
}