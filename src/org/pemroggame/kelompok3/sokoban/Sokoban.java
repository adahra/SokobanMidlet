package org.pemroggame.kelompok3.sokoban;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Screen;
import javax.microedition.midlet.MIDlet;

public class Sokoban extends MIDlet implements CommandListener {
    Display tampil;
    private final Kanvas KANPAS;
    private final Nilai SKOR;
    private Screen LAYAR_SKOR;
    private Screen LAYAR_LEVEL;
    private final Alert ALERT;
    private final Command undoCommand = new Command("Undo", Command.BACK, 1);
    private final Command restartCommand =
	new Command("Start Over", Command.CANCEL, 21);
    private final Command exitCommand = new Command("Exit", Command.EXIT, 60);
    private final Command scoresCommand =
	new Command("Show Scores", Command.SCREEN, 25);
    private final Command okCommand = new Command("OK", Command.OK, 30);
    private final Command levelCommand =
	new Command("Change Level", Command.OK, 24);
    private final Command nextCommand =
	new Command("Next Level", Command.SCREEN, 22);
    private final Command prevCommand =
	new Command("Previous Level", Command.SCREEN, 23);
        public Sokoban() {
	tampil = Display.getDisplay(this);
	SKOR = new Nilai();
	KANPAS = new Kanvas(this, SKOR);
	ALERT = new Alert("Warning");
    }
    public void startApp() {
	if (!SKOR.open()) {
	    System.out.println("Score open failed");
	}

	KANPAS.init();
	KANPAS.addCommand(undoCommand);
	KANPAS.addCommand(scoresCommand);
	KANPAS.addCommand(restartCommand);
	KANPAS.addCommand(levelCommand);
	KANPAS.addCommand(exitCommand);
	KANPAS.addCommand(nextCommand);
	KANPAS.addCommand(prevCommand);
	KANPAS.setCommandListener(this);
	tampil.setCurrent(KANPAS);
    }
    
    public void pauseApp() {
        
    }
    
    public void destroyApp(boolean unconditional) {
	tampil.setCurrent(null);
	KANPAS.destroy();
	if (SKOR != null)
	    SKOR.close();
    }
    
    public void commandAction(Command c, Displayable s) {
	if (c == undoCommand) {
	    KANPAS.undoMove();
	} else if (c == restartCommand) {
	    KANPAS.restartLevel();
	} else if (c == levelCommand) {
	    LAYAR_LEVEL = KANPAS.getLevelScreen();
	    LAYAR_LEVEL.addCommand(okCommand);
	    LAYAR_LEVEL.setCommandListener(this);
	    tampil.setCurrent(LAYAR_LEVEL);
	} else if (c == okCommand && s == LAYAR_LEVEL) {
	    if (!KANPAS.gotoLevel()) {
		ALERT.setString("ERROR");
		tampil.setCurrent(ALERT, KANPAS);
	    } else {
		tampil.setCurrent(KANPAS);
	    }
	} else if (c == scoresCommand) {
	    LAYAR_SKOR = KANPAS.getScoreScreen();
	    LAYAR_SKOR.addCommand(okCommand);
	    LAYAR_SKOR.setCommandListener(this);
	    tampil.setCurrent(LAYAR_SKOR);
	} else if (c == okCommand && s == LAYAR_SKOR) {
	    tampil.setCurrent(KANPAS);
	} else if (c == exitCommand) {
	    destroyApp(false);
	    notifyDestroyed();
	} else if (c == List.SELECT_COMMAND && s == KANPAS) {
	    LAYAR_SKOR = KANPAS.getScoreScreen();
	    LAYAR_SKOR.addCommand(okCommand);
	    LAYAR_SKOR.setCommandListener(this);
	    tampil.setCurrent(LAYAR_SKOR);
	    KANPAS.nextLevel(1);
	} else if (c == nextCommand) {
	    if (!KANPAS.nextLevel(1)) {
		ALERT.setString("ERROR " + (KANPAS.getLevel() + 1));
		tampil.setCurrent(ALERT, KANPAS);
	    } else {
		tampil.setCurrent(KANPAS);
	    }
            
            if (s == KANPAS) {
                KANPAS.repaint();
            }
	} else if (c == prevCommand) {
	    if (!KANPAS.nextLevel(-1)) {
		ALERT.setString("ERROR " + (KANPAS.getLevel() - 1));
		tampil.setCurrent(ALERT, KANPAS);
	    } else {
		tampil.setCurrent(KANPAS);
            }
            
            if (s == KANPAS) {
                KANPAS.repaint();
            }
	} else {
	    System.out.println("ERROR " + c);
	}
    }
}
