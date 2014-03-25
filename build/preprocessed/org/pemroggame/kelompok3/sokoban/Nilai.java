package org.pemroggame.kelompok3.sokoban;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

/** kelas yang digunakan untuk menangani seputar nilai atau skor yg ada
 * pada game, dan untuk menyimpan daftar nilai yang terakhir kedalam 
 * recordstore handphone
 */
class Nilai {
    private int t;
    private final byte[] h;
    private static final int LEVEL_LEN = 5;
    private static final byte LEVEL_TAG = 1;
    private int gh; 
    private final byte[] jh;
    private static final int SCORE_LEN = 13;
    private static final byte SCORE_TAG = 2;
    private RecordStore ghk;
    
    /** Konstruktor dari kelas Nilai
     */
    Nilai() {
		ghk = null;
		t = 0;
		h = new byte[LEVEL_LEN];
		h[0] = LEVEL_TAG;
		putInt(h, 1, 0);
		gh = 0;
		jh = new byte[SCORE_LEN];
		jh[0] = SCORE_TAG;
		putInt(jh, 1, 0);
    }
    
    /** Method yang digunakan untuk membuka nilai/skor yang sudah ada
     * dan menampilkannya kedalam Screen/Canvas
     * @return
     */
    boolean open() {
		try {
			ghk = RecordStore.openRecordStore("PushScores", true);
		} catch (RecordStoreException ex) { }

		if (ghk == null) return false;

		try {
			t = 0;
			RecordEnumeration en = ghk.enumerateRecords(null, null, false);
			while (en.hasNextElement()) {
				int ndx = en.nextRecordId();
				if (ghk.getRecordSize(ndx) == LEVEL_LEN) {
					int l = ghk.getRecord(ndx, h, 0);
					if (l == LEVEL_LEN &&
						h[0] == LEVEL_TAG) {
						t = ndx;
						break;
					}
				} 
			}
		} catch (RecordStoreException ex) {
			return false;
		}

		return true;
	}
	
	/** Method yang digunakan untuk mengambil level, mengetahui level
	 * berapa sekarang
	 * @return
	 */	
	int getLevel() {
		return getInt(h, 1);
	}
	
	/** Method yang digunakan untuk mengatur level sekarang	
	 * @param masukan level yang akan di tuju
	 * @return 
	 */
	boolean setLevel(int level) {
		putInt(h, 1, level);
		putInt(jh, 1, level);
		if (ghk == null)
			return false;
		try {
			if (t == 0) {
				t = ghk.addRecord(h, 0, h.length);
			} else {
				ghk.setRecord(t, h, 0, h.length);
			}
		} catch (RecordStoreException ex) {
			System.out.println("RecordStoreException");
			return false;
		}
		readScore(level);
		return true;
	}
	
	/** Method yang digunakan untuk mendapatkan kotak yang sudah didorong
	 * @return 
	 */	
	int getPushes() {
		return getInt(jh, 5);
	}
	
	/** Method yang digunakan untuk mendapatkan berapa jumlah gerakan
	 * yang digunakan untuk memecahkan level
	 * @return
	 */	
	int getMoves() {
		return getInt(jh, 9);
	}
	
	/** Method yang digunakan untuk membaca nilai yang sudah pernah 
	 * di simpan sebelumnya, dan menulisnya lagi dengan nilai yang baru
	 * @param 
	 * @return
	 */	
	boolean readScore(int level) {
		try {
			gh = 0;
			RecordEnumeration en = ghk.enumerateRecords(null, null, false);
			while (en.hasNextElement()) {
				int ndx = en.nextRecordId();
				if (ghk.getRecordSize(ndx) == SCORE_LEN) {
					int l = ghk.getRecord(ndx, jh, 0);
					if (l == SCORE_LEN && jh[0] == SCORE_TAG && getInt(jh, 1) == level) {
						gh = ndx;
						return true;
					}
				} 
			}
		} catch (RecordStoreException ex) {
			return false;
		}

		jh[0] = SCORE_TAG;
		putInt(jh, 1, level);
		putInt(jh, 5, 0);
		putInt(jh, 9, 0);
		return true;
    }

	/** Method yang digunakan untuk mengatur nilai/skor level yang sudah
	 * dilalui saat ini
	 * @param berapa kali mendorong kotak
	 * @param berapa gerakan player memecahkan level
	 * @return
	 */
    boolean setLevelScore(int pushes, int moves) {

		putInt(jh, 5, pushes);
		putInt(jh, 9, moves);

		try {

			if (gh == 0) {
				gh = ghk.addRecord(jh, 0, jh.length);
			} else {
				ghk.setRecord(gh, jh, 0, jh.length);
			}

		} catch (RecordStoreException ex) {
			return false;
		}
		return true;
    }

	/** Method yang digunakan untuk mengambil nilai integer dari level 
	 * yang ada
	 * @return
	 */
    private int getInt(byte[] buf, int offset) {
		return (buf[offset+0] & 0xff) << 24 |
	    (buf[offset+1] & 0xff) << 16 |
	    (buf[offset+2] & 0xff) << 8 |
	    (buf[offset+3] & 0xff);
    }

    /** Method yang digunakan untuk mengatur nilai integer pada level
     * @return
     */
    private void putInt(byte[] buf, int offset, int value) {
		buf[offset+0] = (byte)((value >> 24) & 0xff);
		buf[offset+1] = (byte)((value >> 16) & 0xff);
		buf[offset+2] = (byte)((value >>  8) & 0xff);
		buf[offset+3] = (byte)((value >>  0) & 0xff);
    }

    /** Method yang digunakan untuk menutup RecordStore yang digunakan
     * untuk menyimpan nilai/skor
     */
    void close() {
		try {
			if (ghk != null) {
				ghk.closeRecordStore();
			}
		} catch (RecordStoreException ex) { }
    }

}
