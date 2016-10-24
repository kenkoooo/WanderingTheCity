import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.security.SecureRandom;

public class WanderingTheCityVis {
  private static final int minSz = 50;
  private static final int maxSz = 500;

  private static final double minProb = 0.05;
  private static final double maxProb = 0.5;

  private static final double minChangeProb = 0.05;
  private static final double maxChangeProb = 0.2;

  private static final double mixProb = 0.01;

  private int S;
  private char[][] playerViewMap;
  private char[][] realMap;
  private int[] startPos;
  // costs for 1 unit of walk and 1 call of look/guess
  private int W, L, G;

  private volatile int nLook, nGuess, nCorGuess, totalWalked;
  private volatile boolean correct;

  // limits for # of calls for each function
  private int maxLook, maxGuess, maxTotalWalk;

  private String errMessage;
  private volatile int[] curPos;
  private boolean ok;

  private void generate(long seed) {
    try {
      // generate test case
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
      random.setSeed(seed);
      S = random.nextInt(maxSz - minSz + 1) + minSz;
      double blackProb = random.nextDouble() * (maxProb - minProb) + minProb;
      double changeProb = random.nextDouble() * (maxChangeProb - minChangeProb) + minChangeProb;

      if (seed <= 3) {
        S = (int) seed * 20;
        blackProb = maxProb - minProb * 2 * seed;
      }
      if (seed == 4) {
        S = maxSz;
        blackProb = minProb;
      }

      // generate the map of the city
      playerViewMap = new char[S][S];
      realMap = new char[S][S];
      int[] reps = new int[S];
      int repCnt = 0;
      for (int i = 2; i <= S; i++) {
        if ((S % i) == 0) {
          reps[repCnt++] = i;
        }
      }

      int numBlack = 0;
      do {
        // 繰り返し周期を選ぶ
        int repeatI = reps[random.nextInt(repCnt)];
        int repeatJ = reps[random.nextInt(repCnt)];

        // 小マップを生成する
        for (int i = 0; i < repeatI; i++)
          for (int j = 0; j < repeatJ; j++) {
            realMap[i][j] = random.nextDouble() < blackProb ? 'X' : '.';
          }

        // 小マップを繰り返させる
        for (int i = 0; i < S; i++)
          for (int j = 0; j < S; j++) {
            realMap[i][j] = realMap[i % repeatI][j % repeatJ];
          }

        // 適当にマップのマスを選択し、再度生成する
        for (int i = 0; i < S; i++)
          for (int j = 0; j < S; j++) {
            if (random.nextDouble() < mixProb) {
              realMap[i][j] = random.nextDouble() < blackProb ? 'X' : '.';
            }
            if (realMap[i][j] == 'X') numBlack++;
          }

        // 各色最低でも 1 マス以上は存在するようにする
      } while (numBlack == 0 && numBlack != S * S);

      // ユーザーに渡すためにコピーする
      for (int i = 0; i < S; i++)
        for (int j = 0; j < S; j++) {
          playerViewMap[i][j] = realMap[i][j];
        }

      // ランダムに破壊する
      int nChange = 0;
      for (int i = 0; i < S; i++)
        for (int j = 0; j < S; j++) {
          if (random.nextDouble() < changeProb) {
            realMap[i][j] = (realMap[i][j] == '.' ? 'X' : '.');
            ++nChange;
          }
        }

      // 開始場所を決める
      startPos = new int[2];
      for (int i = 0; i < 2; ++i)
        startPos[i] = random.nextInt(S);

      // 各アクションのコストを設定する
      W = random.nextInt(10) + 1;
      L = random.nextInt(S / 2) + S / 2;
      G = random.nextInt(S * S / 2) + S * S / 2;

      // initialize limits
      maxGuess = maxLook = S * S;
      maxTotalWalk = 16 * S * S;

      if (debug) {
        System.out.println("S = " + S);
        System.out.println("Probability of black building = " + blackProb);
        System.out.println("Starting position: (" + startPos[0] + "," + startPos[1] + ")");
        System.out.println("Old map:");
        for (int i = 0; i < S; i++)
          System.out.println(new String(playerViewMap[i]));
        System.out.println("Changed cells = " + nChange);
        System.out.println("Cost of walking W = " + W);
        System.out.println("Cost of look() L = " + L);
        System.out.println("Cost of guess() G = " + G);
        System.out.println();
      }
    } catch (Exception e) {
      System.err.println("An exception occurred while generating test case.");
      e.printStackTrace();
    }
  }
  // -----------------------------------------
  private String[] look() {
    // no input, so no validation
    nLook++;
    if (nLook > maxLook) {
      errMessage = "You can do at most " + maxLook + " look() calls.";
      ok = false;
      return new String[2];
    }
    char[][] seen = new char[2][2];
    for (int i = 0; i < 2; ++i)
      for (int j = 0; j < 2; ++j) {
        seen[i][j] = realMap[(curPos[0] + S + i - 1) % S][(curPos[1] + S + j - 1) % S];
        if (vis)
          seenVis[(curPos[0] + S + i - 1) % S][(curPos[1] + S + j - 1) % S] = true;
      }
    String[] seenStr = new String[2];
    seenStr[0] = new String(seen[0]);
    seenStr[1] = new String(seen[1]);
    draw();
    return seenStr;
  }
  // -----------------------------------------
  private boolean validShift(int shift) {
    if (shift <= -S || shift >= S) {
      errMessage = "Value of shift (" + shift + ") must be between " + (-S + 1) + " and " + (S - 1) + ", inclusive.";
      ok = false;
      return false;
    }
    return true;
  }
  // -----------------------------------------
  private int applyShift(int cur, int shift) {
    return (cur + S + shift) % S;
  }
  // -----------------------------------------
  private int walk(int[] shift) {
    if (shift == null || shift.length != 2) {
      errMessage = "Shift must have exactly two elements";
      ok = false;
      return -1;
    }
    // restrict shifts to size of the city in each direction to avoid distance overflows
    for (int i = 0; i < 2; ++i)
      if (!validShift(shift[i]))
        return -1;
    for (int i = 0; i < 2; ++i)
      curPos[i] = applyShift(curPos[i], shift[i]);
    totalWalked += Math.abs(shift[0]) + Math.abs(shift[1]);
    if (totalWalked > maxTotalWalk) {
      errMessage = "You can walk at most " + maxTotalWalk + " distance.";
      ok = false;
      return -1;
    }
    return 0;
  }
  // -----------------------------------------
  private boolean validCoord(int coord) {
    if (coord < 0 || coord >= S) {
      errMessage = "Value of coordinate (" + coord + ") must be between 0 and " + (S - 1) + ", inclusive.";
      ok = false;
      return false;
    }
    return true;
  }
  // -----------------------------------------
  private int guess(int[] coord) {
    if (coord == null || coord.length != 2) {
      errMessage = "Coord must have exactly two elements";
      ok = false;
      return -1;
    }
    for (int i = 0; i < 2; ++i)
      if (!validCoord(coord[i]))
        return -1;
    nGuess++;
    if (nGuess > maxGuess) {
      errMessage = "You can do at most " + maxGuess + " guess() calls.";
      ok = false;
      return -1;
    }

    boolean res = (startPos[0] == coord[0] && startPos[1] == coord[1]);
    if (res)
      nCorGuess++;
    correct |= res;
    if (vis)
      guessedVis[coord[0]][coord[1]] = true;
    return res ? 1 : 0;
  }
  // -----------------------------------------
  private double runTest(long seed) {
    try {
      generate(seed);

      curPos = new int[2];
      for (int i = 0; i < 2; ++i)
        curPos[i] = startPos[i];
      totalWalked = 0;
      nLook = 0;
      nGuess = 0;
      nCorGuess = 0;
      correct = false;
      ok = true;

      if (vis) {
        Wv = (twomaps ? S * 2 + 2 : S) * SZ + 20;
        Hv = S * SZ + 40;
        seenVis = new boolean[S][S];
        guessedVis = new boolean[S][S];
        jf.setSize(Wv, Hv);
        jf.setVisible(true);
        draw();
      }

      String[] cityMapStr = new String[S];
      for (int i = 0; i < S; ++i)
        cityMapStr[i] = new String(playerViewMap[i]);

      // call the solution
      whereAmI(cityMapStr, W, L, G);

      if (!ok) {
        // something went wrong during library calls
        addFatalError(errMessage);
        return -1;
      }

      if (!correct) {
        // solution failed to guess correctly before returning
        addFatalError("Failed to guess the starting position correctly.");
        return -1;
      }

      // ignore the return value
      // calculate the score based on nLook, nGuess and totalWalked
      if (debug) {
        addFatalError("Total distance walked = " + totalWalked);
        addFatalError("Number of look() calls = " + nLook);
        addFatalError("Number of incorrect guess() calls = " + (nGuess - nCorGuess));
      }
      double score = 0;
      score += (double) (W) * totalWalked;
      score += (double) (L) * nLook;
      score += (double) (G) * (nGuess - nCorGuess);
      return score;
    } catch (Exception e) {
      System.err.println("An exception occurred while trying to get your program's results.");
      e.printStackTrace();
      return 0;
    }
  }
  // ------------- visualization part ------------
  private JFrame jf;
  private Vis v;
  private static boolean debug;
  private static boolean vis;
  private static boolean twomaps;
  private static int del;
  private static int SZ;
  private volatile boolean[][] seenVis;
  private volatile boolean[][] guessedVis;
  // -----------------------------------------
  private int whereAmI(String[] map, int W, int L, int G) throws IOException, NumberFormatException, InterruptedException {
    SolutionRunner runner = new SolutionRunner(map, W, L, G);
    Thread thread = new Thread(runner);
    thread.start();

    // simulate function calls
    while (true) {
      int[] request = Actions.requests.take();
      // get name of function invoked and read appropriate params
      if (request.length == 0) {
        // no params
        String[] looked = look();
        if (!ok)
          return 0;
        Actions.responses.add(looked);
      } else if (request[2] == 0) {
        // two integers
        int[] shift = new int[2];
        for (int i = 0; i < 2; ++i)
          shift[i] = request[i];
        walk(shift);
        if (!ok)
          return 0;

        Actions.responses.add(new String[]{"", ""});
      } else if (request[2] == 1) {
        // two integers
        int[] coord = new int[2];
        System.arraycopy(request, 0, coord, 0, 2);
        int result = guess(coord);
        if (!ok)
          return 0;
        Actions.responses.add(new String[]{String.valueOf(result), ""});
      } else {
        return 0;
      }
      draw();
    }
  }
  // -----------------------------------------
  private void draw() {
    if (!vis) return;
    v.repaint();
    try {
      Thread.sleep(del);
    } catch (Exception e) {

    }
  }
  // -----------------------------------------
  private int Wv, Hv;
  private BufferedImage oldMap;
  private void DrawOldMap() {
    oldMap = new BufferedImage(S * SZ + 1, S * SZ + 1, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = (Graphics2D) oldMap.getGraphics();
    // buildings on the map
    for (int i = 0; i < S; ++i)
      for (int j = 0; j < S; ++j) {
        g2.setColor(new Color(playerViewMap[i][j] == 'X' ? 0x444444 : 0xDDDDDD));
        g2.fillRect(j * SZ, i * SZ, SZ, SZ);
      }
    // lines between buildings for streets
    g2.setColor(new Color(0xAAAAAA));
    for (int i = 0; i <= S; i++)
      g2.drawLine(0, i * SZ, S * SZ, i * SZ);
    for (int i = 0; i <= S; i++)
      g2.drawLine(i * SZ, 0, i * SZ, S * SZ);
  }
  private static BufferedImage deepCopy(BufferedImage source) {
    BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
    Graphics g = b.getGraphics();
    g.drawImage(source, 0, 0, null);
    g.dispose();
    return b;
  }
  // -----------------------------------------
  private class Vis extends JPanel implements WindowListener {
    public void paint(Graphics g) {
      // do painting here
      // draw the given (old) map once (and cache it)
      if (oldMap == null)
        DrawOldMap();
      BufferedImage bi = deepCopy(oldMap);
      Graphics2D g2 = (Graphics2D) bi.getGraphics();
      if (!twomaps) {
        // overlay seen parts of the map, using real black and white and with a border:
        // red for cells which differ from map and green for cells which match
        for (int i = 0; i < S; ++i)
          for (int j = 0; j < S; ++j)
            if (seenVis[i][j]) {
              // border
              g2.setColor(realMap[i][j] == playerViewMap[i][j] ? Color.GREEN : Color.RED);
              g2.fillRect(j * SZ + 1, i * SZ + 1, SZ - 1, SZ - 1);
              // actual color of the building
              g2.setColor(realMap[i][j] == 'X' ? Color.BLACK : Color.WHITE);
              g2.fillRect(j * SZ + 2, i * SZ + 2, SZ - 3, SZ - 3);
            }
      }

      // mark positions which have been guessed incorrectly
      g2.setStroke(new BasicStroke(2.0f));
      for (int i = 0; i < S; ++i)
        for (int j = 0; j < S; ++j)
          if (guessedVis[i][j]) {
            g2.setColor(i == startPos[0] && j == startPos[1] ? Color.GREEN : Color.RED);
            g2.drawLine(j * SZ - 3, i * SZ - 3, j * SZ + 3, i * SZ + 3);
            g2.drawLine(j * SZ - 3, i * SZ + 3, j * SZ + 3, i * SZ - 3);
          }
      g.drawImage(bi, 0, 0, S * SZ + 1, S * SZ + 1, null);

      if (twomaps) {
        // draw currently seen parts of the map using fog of war - only show the cells observed
        BufferedImage bi2 = new BufferedImage(S * SZ + 1, S * SZ + 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g22 = (Graphics2D) bi2.getGraphics();
        g22.setColor(new Color(0xAAAAAA));
        g22.fillRect(0, 0, S * SZ, S * SZ);
        // buildings on the map
        for (int i = 0; i < S; ++i)
          for (int j = 0; j < S; ++j)
            if (seenVis[i][j]) {
              g22.setColor(realMap[i][j] == 'X' ? Color.BLACK : Color.WHITE);
              g22.fillRect(j * SZ, i * SZ, SZ, SZ);
            }
        // lines between buildings for streets
        g22.setColor(new Color(0xAAAAAA));
        for (int i = 0; i <= S; i++)
          g22.drawLine(0, i * SZ, S * SZ, i * SZ);
        for (int i = 0; i <= S; i++)
          g22.drawLine(i * SZ, 0, i * SZ, S * SZ);

        g.drawImage(bi2, (S + 2) * SZ, 0, S * SZ + 1, S * SZ + 1, null);
      }
    }
    // -------------------------------------
    Vis() {
      jf.addWindowListener(this);
    }
    // -------------------------------------
    //WindowListener
    public void windowClosing(WindowEvent e) {
      System.exit(0);
    }
    public void windowActivated(WindowEvent e) {
    }
    public void windowDeactivated(WindowEvent e) {
    }
    public void windowOpened(WindowEvent e) {
    }
    public void windowClosed(WindowEvent e) {
    }
    public void windowIconified(WindowEvent e) {
    }
    public void windowDeiconified(WindowEvent e) {
    }
  }
  // -----------------------------------------
  private WanderingTheCityVis(long seed) {
    try {
      //interface for runTest
      if (vis) {
        jf = new JFrame();
        v = new Vis();
        jf.getContentPane().add(v);
      }
      System.out.println("Score = " + runTest(seed));
      if (!ok) {
        System.out.println(errMessage);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  // -----------------------------------------
  public static void main(String[] args) {
    long seed = 1;
    vis = true;
    twomaps = true;
    del = 100;
    SZ = 10;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-delay"))
        del = Integer.parseInt(args[++i]);
      if (args[i].equals("-novis"))
        vis = false;
      if (args[i].equals("-size"))
        SZ = Integer.parseInt(args[++i]);
      if (args[i].equals("-debug"))
        debug = true;
      if (args[i].equals("-twomaps"))
        twomaps = true;
    }
    if (twomaps)
      vis = true;
    System.out.println("Start");
    new WanderingTheCityVis(seed);
    System.out.println("Done");
  }
  // -----------------------------------------
  private void addFatalError(String message) {
    System.out.println(message);
  }
}

