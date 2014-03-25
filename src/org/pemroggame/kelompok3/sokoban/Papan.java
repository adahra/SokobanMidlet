package org.pemroggame.kelompok3.sokoban;

/** Kelas papan yang digunakan untuk membuat latar belakang game, untuk 
 * membentuk level baru, meletakkan player, pengaturan pergerakan soko
 * dan pergerakan kotak, mendeteksi key yang di inputkan oleh user
 */
public final class Papan {
    private int level;
    private byte[] array;
    private byte[] pathmap;
    private int width, height;
    private int pusher;
    private int packets = 0;
    private int stored = 0;
    private byte[] moves;
    private int nmoves;	
    private int npushes;

    
    public static final int LEFT = 0;
    public static final int RIGHT = 3;
    public static final int UP = 1;
    public static final int DOWN = 2;
    public static final int MOVEPACKET = 4;

    
    public static final byte GROUND = 0;
    public static final byte STORE = 1;	
    public static final byte PACKET = 2;
    public static final byte WALL = 4;	
    public static final byte PUSHER = 8;

    /** Konstruktor dari kelas Papan */
    public Papan() {
        moves = new byte[200];
        screen0();
    }

    
    public void screen0() {
        width = 9;
        height = 7;
        array = new byte[width * height];
        level = 0;
        nmoves = 0;
        npushes = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                byte t = (x == 0 || y == 0 ||
                x == width - 1 || y == height - 1) ?
                WALL : GROUND;
                set(x, y, t);
            }
        }
        packets = 2;
        stored = 0;
        set(2, 2, PACKET);
        set(4, 4, PACKET);
        set(4, 2, STORE);
        set(6, 4, STORE);
        pusher = index(1, 1);
    }

    /** Method yang digunakan untuk menggerakan semua obyek dalam game
     * @param inputan berupa integer
     * @return
     */
    public int move(int move) {
        int obj = pusher + indexOffset(move);
        if ((array[obj] & WALL) != 0)
            return -1;

        int m = movePacket(obj, move);
        if (m < 0)
            return -1;

        pusher = obj;
        saveMove(m);
		return m;
    }

    /** Method yang digunakan untuk menggerakan kotak yang ada pada game
     * @param index yang ada pada kotak
     * @param 
     * @return
     */
    private int movePacket(int index, int move) {
        if ((array[index] & PACKET) == 0)
            return move;

        int dest = index + indexOffset(move);
        if (array[dest] > STORE)
            return -1;

        
        array[index] &= ~PACKET;
        if ((array[index] & STORE) != 0)
            stored--;

        
        array[dest] |= PACKET;
        if ((array[dest] & STORE) != 0)
            stored++;

        npushes++;
        return move + MOVEPACKET;
    }
	
	/** Method yang digunakan untuk menyimpan pergerakan dari semua obyek
	 * yang ada pada game
	 * @param
	 */
    private void saveMove(int move) {
        if (nmoves >= moves.length) {
            byte[] n = new byte[moves.length + 50];
            System.arraycopy(moves, 0, n, 0, moves.length);
            moves = n;
        }
        moves[nmoves++] = (byte)move;
    }

	/** Method yang digunakan untuk membatalkan pergerakan yang lalu 
	 * @return
	 */
    public int undoMove() {
        if (nmoves <= 0)
            return -1;
        int move = moves[--nmoves];
        int rev = (move & 3) ^ 3;
        int back = pusher + indexOffset(rev);

        if ((move & MOVEPACKET) != 0) {
            npushes--;
            movePacket(pusher + indexOffset(move), rev);
        }
        pusher = back;
		return move;
    }

    /** Method yang digunakan untuk mengecek apakah semua sudah terpecah
     * kan atau belum
     * @return
     */
    public boolean solved() {
        return packets == stored;
    }
	
	/** Method yang digunakan untuk memindahkan target/obyek pada game di
	 * posisi yang sudah ditentukan berapa x dan berapa y
	 * @param nilai x
	 * @param nilai y
	 * @param batas maksimal dari nilai x dan y
	 * @return
	 */
    public int runTo(int x, int y, int max) {
        int target = index(x, y);
        if (target < 0 || target >= array.length)
            return -1;
        if (target == pusher)
            return -1;

	
        if (pathmap == null || pathmap.length != array.length) {
            pathmap = new byte[array.length];
        }
        
        for (int i = 0; i < pathmap.length; i++)
            pathmap[i] = 127;

        
        findTarget(target, (byte)0);

        if (pathmap[pusher] == 127) {
            return -1;
        } else {

            int pathlen = pathmap[pusher];
            int pathmin = pathlen - max;
	    int dir = -1;
            for (pathlen--; pathlen >= pathmin; pathlen--) {
                if (pathmap[pusher - 1] == pathlen) {
		    dir = LEFT;
                    saveMove(dir);
                    pusher--;
                } else if (pathmap[pusher + 1] == pathlen) {
		    dir = RIGHT;
                    saveMove(dir);
                    pusher++;
                } else if (pathmap[pusher - width] == pathlen) {
		    dir = UP;
                    saveMove(dir);
                    pusher -= width;
                } else if (pathmap[pusher + width] == pathlen) {
		    dir = DOWN;
                    saveMove(dir);
                    pusher += width;
                } else {

                    throw new RuntimeException("runTo abort");
                }
            }
	    return dir;
        }
    }

	/** Method yang digunakan untuk mencari target atau posisi kotak dan 
	 * soko
	 * @param
	 * @param
	 */
    private void findTarget(int t, byte pathlen) {
        if (array[t] > STORE)
            return;

	
        if (pathmap[t] <= pathlen)
                return;

        pathmap[t] = pathlen++;
        if (t == pusher)
            return;

        findTarget(t - 1, pathlen);
        findTarget(t + 1, pathlen);
        findTarget(t - width, pathlen);
        findTarget(t + width, pathlen);
    }

    /** Method yang digunakan untuk mengambil nilai x dan y
     * @param nilai x
     * @param nilai y
     * @return
     */
    public byte get(int x, int y) {
        int offset = index(x, y);
        if (offset == pusher)
            return (byte)(array[offset] | PUSHER);
        else
            return array[offset];
    }

    /** Method yang digunakan untuk mengatur nilai x dan y
     * @param
     * @param
     * @param
     */
    private void set(int x, int y, byte value) {
        array[index(x, y)] = value;
    }

    /** Digunakan untuk mengatur nilai index x dan y
     * @param nilai x
     * @param nilai y
     * @return
     */
    private int index(int x, int y) {
        if (x < 0 || x >= width ||
        y < 0 || y >= height)
            return -1;

        return y * width + x;
    }

    /** Digunakan untuk mengetahui lokasi dari posisi tujuan kotak yang 
     * mau digeser
     * @return
     */
    public int getPusherLocation() {
		int x = pusher % width;
		int y = pusher / width;
		return (y << 16) + x;
    }

    /** Digunakan untuk mengatur index offset
     * @param 
     * @return
     */
    private int indexOffset(int move) {
        switch (move & 3) {
            case LEFT:
                return -1;
            case RIGHT:
                return +1;
            case UP:
                return -width;
            case DOWN:
                return +width;
        }
        return 0;
    }

    /** Method yang digunakan untuk membaca level dan digunakan untuk 
     * menerjemahkan array level kedalam grafik
     * @param
     * @param
     */
    public void read(java.io.InputStream is, int l) {
        final int W = 20;
        final int H = 20;
        byte[] b = new byte[W * H];
    
        int c, w = 0;
        int x = 0, y = 0, xn = 0, yn = 0, npackets = 0;
        try {
            while ((c = is.read()) != -1) {
                switch (c) {
                    case '\n':
                        if (x > w) {
                            w = x;
                        }

                        y++;
                        x = 0;
                        break;

                    case '$':
                        b[y * W + x++] = PACKET;
                        npackets++;
                        break;

                    case '#':
                        b[y * W + x++] = WALL;
                        break;

                    case ' ':
                        b[y * W + x++] = GROUND;
                        break;

                    case '.':
                        b[y * W + x++] = STORE;
                        break;

                    case '+':
                        b[y * W + x++] = STORE;
                    case '@':
                        xn = x;
                        yn = y;
                        x++;
                        break;
                }
            }
        } catch (java.io.IOException ex) {
        }

        if (y > 0) {
            array = new byte[w * y];
            if (y > w) {	
                width = y;
                height = w;
                for (y = 0; y < width; y++) {
                    for (x = 0; x < w; x++) {
                        array[index(y, x)] = b[y * W + x];
                    }
                }
                pusher = index(yn, xn);
            } else {
                width = w;
                height = y;
                array = new byte[width * height];
                for (y = 0; y < height; y++) {
                    for (x = 0; x < width; x++) {
                        array[index(x, y)] = b[y * W + x];
                    }
                }
                pusher = index(xn, yn);
            }
            stored = 0;
            packets = npackets;
            level = l;
            nmoves = 0;
            npushes = 0;
        }
    }

    /** Digunakan untuk mengambil lebar canvas/level
     * @return lebar
     */
    public int getWidth() {
        return width;
    }

    /** Digunakan untuk mengambil tinggi canvas/level
     * @return tinggi
     */
    public int getHeight() {
        return height;
    }

    /** Digunakan untuk mengetahui berapa jumlah gerakan
     * @return gerakan
     */
    public int getMoves() {
        return nmoves;
    }

    /** Digunakan untuk mengetahui/mendapatkan keadaan lokasi tujuan
     * @return
     */
    public int getPushes() {
        return npushes;
    }

    /** Method yang digunakan untuk mengecek posisi soko dari nilai x
     * @param
     * @return
     */
    private int dx(int dir) {
        if (dir == LEFT)
            return -1;
        if (dir == RIGHT)
            return +1;
        return 0;
    }

    /** Method yang digunakan untuk mengecek posisi soko dari nilai y
     * @param
     * @return
     */
    private int dy(int dir) {
        if (dir == UP)
            return -1;
        if (dir == DOWN)
            return +1;
        return 0;
    }
}
