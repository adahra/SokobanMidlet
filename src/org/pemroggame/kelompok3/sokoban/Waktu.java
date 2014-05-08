package org.pemroggame.kelompok3.sokoban;

import java.util.TimerTask;

public class Waktu extends TimerTask {
    int x, y;
    Papan papan; 
    Kanvas canvas;
    
    Waktu(Kanvas c, Papan b, int x, int y) {
		canvas = c;
		papan = b;
		this.x = x;
		this.y = y;
    }
    
    public void run() {
        try {
            int pos = papan.getPusherLocation();
            int dir = papan.runTo(x, y, 1);
            if (dir < 0) {
		cancel();
            } else {
		canvas.repaintNear(pos, dir);
            }
	} catch (Exception ex) { }
    }
}
