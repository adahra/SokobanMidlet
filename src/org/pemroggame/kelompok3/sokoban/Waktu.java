package org.pemroggame.kelompok3.sokoban;

import java.util.TimerTask;

/** Kelas Waktu, kelas yang digunakan untuk menangani animasi dalam game */
public class Waktu extends TimerTask {
	/** variabel yang digunakan untuk menyimpan nilai x dan y */
    int x, y;
    /** obyek yang digunakan untuk mendeteksi obyek dalam papan */
    Papan papan; 
    /** obyek yang digunakan untuk meletakkan semua obyek game */
    Kanvas canvas;
    /** Konstruktor dalam kelas Waktu, 
     * @param digunakan untuk menyimpan nilai posisi dalam canvas
     * @param digunakan untuk menyimpan nilai posisi papan
     * @param digunakan untuk menyimpan nilai x
     * @param digunakan untuk menyimpan nilai y
     */
    Waktu(Kanvas c, Papan b, int x, int y) {
		// memberi nilai canvas dengan nilai yang diberikan pada c
		canvas = c;
		// nilai papan sama dengan nilai b
		papan = b;
		// nilai x dalam kelas ini akan diberi nilai yang sama dengan
		// nilai x dalam parameter
		this.x = x;
		// nilay y dalam kelas akan diberi nilai yang sama dengan nilai
		// y dalam parameter konstruktor
		this.y = y;
    }
    
    /** Method overriding dari kelas TimerTask yang digunakan untuk 
     * menjalankan waktu/animasi dalam game, digunakan juga untuk
     * membuat kanvas menggambar ulang semua gambar dalam game
     */
    public void run() {
		// jika terjadi kesalahan, tangkap kesalahan
		try {
			// ambil lokasi dorong
			int pos = papan.getPusherLocation();
			// jalankan posisi ke x,y dan beri nilai 1
			int dir = papan.runTo(x, y, 1);
			// jika posisi kurang dari nol batalkan
			if (dir < 0) {
				cancel();
			} else {
				// posisi lebih dari nol maka lakukan penggambaran ulang
				// lokasi dorong dan posisi
				canvas.repaintNear(pos, dir);
			}
		} catch (Exception ex) { }
	}
}
