package a9lim.raiko;

import com.sedmelluq.discord.lavaplayer.container.mpeg.MpegAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

public class LTest {
    private Goblin g;

    public LTest(){
        g = new Goblin(0);
        int heartbeat = 1000;
        System.out.println(heartbeat);
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                g = new Goblin(g.getGoblinloot()+1);
                System.out.println(g.getGoblinloot());
            }
        }, heartbeat,heartbeat);
//        t.schedule(new TimerTask() {
//            public void run() {
//                track.setPosition(seek);
//                System.out.println("bigtime nico gaming");
//            }
//        }, heartbeat+10,heartbeat);
        System.out.println(g.getGoblinloot());
    }
    public static void main(String args[]){
        LTest t = new LTest();
    }
}

class Goblin {
    private int goblinloot;

    public Goblin (int i) {
        goblinloot = i;
    }

    public int getGoblinloot() {
        return goblinloot;
    }

    public void setGoblinloot(int goblinloot) {
        this.goblinloot = goblinloot;
    }
}
