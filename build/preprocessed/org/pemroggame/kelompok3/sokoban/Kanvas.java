package org.pemroggame.kelompok3.sokoban;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Screen;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

public class Kanvas extends Canvas {
    private int level = 1;
    private boolean solved;    
    private int cell = 1;	
    private int w, h;		
    private int bwidth, bheight;
    private final Papan board;
    private final Nilai score;
    private final Sokoban push;
    private final Display display;
    private Timer timer;
    private TimerTask timertask;
    private CommandListener listener;
    private TextBox levelText;	
    private static int wallColor =   0x7f7f7f;
    private static int groundColor = 0xffffff;
    private static int packetColor = 0x000000;
    private static int storeColor =  0x000000;
    private static int pusherColor = 0x000000;
    private static boolean useDottedLine = false;

    public Kanvas(Sokoban pushpuzzle, Nilai s) {
	this.push = pushpuzzle;
	display = Display.getDisplay(pushpuzzle);
	score = s;
	board = new Papan();
	initColors();
    }
    
    public void init() {
	h = getHeight();
	w = getWidth();
	level = score.getLevel();
	if (!readScreen(level)) {
	    level = 0;
	    readScreen(level);
	}
        
	repaint();
    }
	
    public void destroy() {
	cancelTo();
	if (timer != null) {
	    timer.cancel();
	    timer = null;
	}
    }

    private void initColors() {
	boolean isColor = display.isColor();
	int numColors = display.numColors();
	if (isColor) {
            setColors(0x006D55, 0xffffff, 0xff6d00, 0xb60055, 0x6d6dff);
	} else {
	    if (numColors > 2) {
		setColors(0x999999, 0xffffff, 0x666666, 0xbbbbbb, 0x000000);
	    } else {
		setColors(0x6a6a6a, 0xffffff, 0x6a6a6a, 0x000000, 0x000000);
                useDottedLine = true;
	    }
	}
    }
   
    private void setColors(int w, int g, int pa, int s, int pu) {
	if (w != -1)
	    wallColor =   w;
	if (g != -1)
	    groundColor = g;
	if (pa != -1)
	    packetColor = pa;
	if (s != -1)
	    storeColor =  s;
	if (pu != -1)
	    pusherColor = pu;
    }

    private int parseColor(String s) {
	if (s == null)
	    return -1;
	return Integer.parseInt(s, 16);
    }
    
    public void undoMove() {
	int dir = board.undoMove();
	if (dir >= 0) {
	    repaintNear(board.getPusherLocation(), dir);
	}
        
	solved = board.solved();
    }

    public void restartLevel() {
	readScreen(level);
	repaint();
	solved = false;
    }

    public boolean nextLevel(int offset) {
	updateScores();	
	if (level + offset >= 0 && readScreen(level+offset)) {
	    level += offset;
	    score.setLevel(level);
	    solved = false;
	    return true;
	}
        
	return false;
    }
  
    public int getLevel() {
	return level;
    }

    
    public Screen getLevelScreen() {
	if (levelText == null) {
	    levelText = new TextBox("Pilih Level", Integer.toString(level), 4, TextField.NUMERIC);
	} else {
	    levelText.setString(Integer.toString(level));
	
        }
        
	return levelText;
    }

    public boolean gotoLevel() {
	if (levelText != null) {
	    String s = levelText.getString();
	    int l = Integer.parseInt(s);
	    updateScores();
	    if (l >= 0 && readScreen(l)) {
		level = l;
		score.setLevel(level);
		solved = false;
		repaint();
		return true;
	    }
	}
        
	return false;
    }

    private boolean readScreen(int lev) {
	if (lev <= 0) {
	    board.screen0();	
	} else {
	    InputStream is = null;
	    try {
		is = getClass().getResourceAsStream("level." + lev);
		if (is != null) {
		    board.read(is, lev);
		    is.close();
		} else {
		    System.out.println("error" + lev);
		    return false;
		}
	    } catch (java.io.IOException ex) {
		return false;
	    }
	}
        
	bwidth = board.getWidth();
	bheight = board.getHeight();
	cell = ((h-14) / bheight < w / bwidth) ? (h-14) / bheight : w / bwidth;
	return true;
    }

    public Screen getScoreScreen() {
	Form wokeh = null;
	int currPushes = board.getPushes();
	int bestPushes = score.getPushes();
	int currMoves = board.getMoves();
	int bestMoves = score.getMoves();
	boolean newbest = solved && (bestPushes == 0 || currPushes < bestPushes);
	wokeh = new Form(null);
	wokeh.append(new StringItem(
            newbest ? "New Best:\n" : "Current:\n", 
            currPushes + " pushes\n" + 
            currMoves  + " moves"));

	wokeh.append(new StringItem(
            newbest ? "Old Best:\n" : "Best:\n",
            bestPushes + " pushes\n" + 
            bestMoves  + " moves"));
	String title = "Scores";
	if (newbest) {
	    title = "Congratulations";
	}
        
	wokeh.setTitle(title);
	return wokeh;
    }

    protected void keyRepeated(int keyCode) {
        int action = getGameAction(keyCode);
        switch (action) {
        case Canvas.LEFT:
        case Canvas.RIGHT:
        case Canvas.UP:
        case Canvas.DOWN:
            keyPressed(keyCode);
	    break;
        default:
            break;
        }
    }
    
    protected void keyPressed(int keyCode) {
        boolean newlySolved = false;
	synchronized (board) {
	    cancelTo();
	    int action = getGameAction(keyCode);
	    int move = 0;
	    switch (action) { 
	    case Canvas.LEFT:
		move = Papan.LEFT;
		break;
	    case Canvas.RIGHT:
		move = Papan.RIGHT;
		break;
	    case Canvas.DOWN:
		move = Papan.DOWN;
		break;
	    case Canvas.UP:
		move = Papan.UP;
		break;
	    default:
		return;
	    }

	    int pos = board.getPusherLocation();
	    int dir = board.move(move);
	    repaintNear(pos, dir);  
	    if (!solved && board.solved()) {
		newlySolved = solved = true;
	    }
	} 

        if (newlySolved && listener != null) {
            listener.commandAction(List.SELECT_COMMAND, this);
        }
    }
        
    private void updateScores() {
	if (!solved) return;
	int sp = score.getPushes();
	int bp = board.getPushes();
	int bm = board.getMoves();
	if (sp == 0 || bp < sp) {
	    score.setLevelScore(bp, bm);
	}
    }

    private void animateTo(int x, int y) {
	if (timer == null) timer = new Timer();
	if (timertask != null) {
	    timertask.cancel();
	    timertask = null;
	}
        
	timertask = new Waktu(this, board, x, y);
	timer.schedule(timertask, (long)100, (long)100);
    }

    private void cancelTo() {
	if (timertask != null) timertask.cancel();
    }
    
    protected void pointerPressed(int x, int y) {
        animateTo(x/cell, y/cell);
    }
   
    public void setCommandListener(CommandListener l) {
	super.setCommandListener(l);
        listener = l;
    }
   
    public void repaintNear(int loc, int dir) {
	int x = loc & 0x7fff;
	int y = (loc >> 16) & 0x7fff;	
	int size = 1;
	if (dir >= 0) {
	    size += 1;
	    if ((dir & Papan.MOVEPACKET) != 0) {
		size += 1;
	    }
	}
        
	int dx = 1;
	int dy = 1;
	switch (dir & 3) {
	case Papan.UP: 
	    y -= size-1;
	    dy = size;
	    break;
	case Papan.DOWN:
	    dy = size;
	    break;
	case Papan.RIGHT:
	    dx = size;
	    break;
	case Papan.LEFT:
	    x -= size-1;
	    dx = size;
	    break;
	}
        
	repaint(x * cell, y * cell, dx * cell, dy * cell);
    }

    protected void paint(Graphics g) {
	synchronized (board) {
	    int x = 0, y =  0, x2 = bwidth, y2 = bheight;
	    int clipx = g.getClipX();
	    int clipy = g.getClipY();
	    int clipw = g.getClipWidth();
	    int cliph = g.getClipHeight();
	    x = clipx / cell;
	    y = clipy / cell;
	    x2 = (clipx + clipw + cell-1) / cell;
	    y2 = (clipy + cliph + cell-1) / cell;
	    if (x2 > bwidth)
		x2 = bwidth;
	    if (y2 > bheight)
		y2 = bheight;
	    g.setColor(groundColor);
	    g.fillRect(0, 0, w, h);
	    for (y = 0; y < y2; y++) {
		for (x = 0; x < x2; x++) {
		    byte v = board.get(x, y);
		    switch (v & ~Papan.PUSHER) {
		    case Papan.WALL:
			g.setColor(wallColor);
			g.fillRect(x*cell, y*cell, cell, cell);
			break;
		    case Papan.PACKET:
		    case Papan.PACKET | Papan.STORE:
			g.setColor(packetColor);
			g.fillRect(x*cell+1, y*cell+1, cell-2, cell-2);
			break;
		    case Papan.STORE:
			g.setColor(storeColor);
                        if (useDottedLine) {
                            g.setStrokeStyle(Graphics.DOTTED);
                        }
			g.drawRect(x*cell+1, y*cell+1, cell-2, cell-2);
			break;
		    case Papan.GROUND:
		    default:
			break;
		    }
                    
		    if ((v & Papan.PUSHER) != 0) {
			g.setColor(pusherColor);
			g.fillArc(x*cell, y*cell, cell, cell, 0, 360);
		    }
		}
	    }
            
	    g.drawString("Dorong Kotak Level " + level, 0, h-14,
			 Graphics.TOP|Graphics.LEFT);
	}
    }
}
