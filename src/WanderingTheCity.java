import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class WanderingTheCity {
  class MatchingPlace implements Comparable<MatchingPlace> {
    int i, j;
    int randomId;
    double matchingScore = 0.0;
    MatchingPlace(int i, int j, int id) {
      this.i = i;
      this.j = j;
      this.randomId = id;
    }

    void setScore(double score) {
      this.matchingScore = score;
    }

    @Override
    public int compareTo(MatchingPlace o) {
      // 同じスコア同士なら randomId を使ってタイブレーク
      if (this.matchingScore == o.matchingScore) {
        return this.randomId - o.randomId;
      }

      // スコアの降順にソート
      if (this.matchingScore > o.matchingScore) return -1;
      else return 1;
    }
  }
  /**
   * 座標を正しく返す
   *
   * @param x 適当な座標
   * @return 座標 [0..S-1]
   */
  private int p(int x) {
    while (x < 0) x += S;
    while (x >= S) x -= S;
    return x;
  }

  private int m(char c) {
    return c == 'X' ? 1 : 0;
  }

  /**
   * 周囲 4 マスの状況を入れる。
   * 例えば、
   * [(0, 1)][(0, 2)]
   * [(1, 1)][(1, 2)]
   * が
   * [X, X]
   * [., X]
   * のとき、
   * map[1][2]=1011 とする
   */
  private byte[][] givenMap;
  private byte[][] lookMap;

  private boolean[][] viewed;

  private int curI = 0, curJ = 0;
  private int S, W, L, G;

  // いままで look した場所のリスト
  private ArrayList<int[]> lookPath = new ArrayList<>();

  // 答えの候補のリスト
  private ArrayList<MatchingPlace> candidates = new ArrayList<>();

  // 歩幅。2 以上で S の約数でない数字にしたい
  private int step1 = 0, step2 = 0;

  // あと何回 guess クエリを控えるか
  private int stopGuessing = 0;
  private int stopMatching = 0;

  // マッチングに使い終わったパスの長さ
  private int prevMatchingPathSize = 0;

  // 空いている定義
  private int isEmpty = 0;

  // 残す候補数
  private final int WIDTH = 1000;

  // カットする時の信頼度
  private final double CUT_PROB = 0.99999;

  // クエリのカウント
  private int walkCount = 0;
  private int lookCount = 0;

  /**
   * 呼び出されるメソッド
   *
   * @param map 与えられるやつ
   * @param W   与えられるやつ
   * @param L   与えられるやつ
   * @param G   与えられるやつ
   * @return 特に意味のない数字(fuck)
   */
  int whereAmI(String[] map, int W, int L, int G) {
    S = map.length;
    this.W = W;
    this.L = L;
    this.G = G;
    init(map);

    while (true) {
      if (mainProcess()) break;
    }
    return 0;
  }

  private int[] whereToWalk() {
    int[] DI = new int[]{+1, -1, -1, +1};
    int[] DJ = new int[]{-1, -1, +1, +1};

    ArrayList<int[]> list = new ArrayList<>();
    while (isEmpty < 9) {
      for (int dist = step1 * 2; dist <= S; dist += step1) {
        int si = curI;
        int sj = curJ + dist;
        for (int edge = 0; edge < 4; edge++)
          for (int i = 0; i < dist; i++) {
            si += DI[edge];
            sj += DJ[edge];
            if (viewCount(si, sj) <= isEmpty) {
              int score = 0;
              for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 2; y++) {
                  score += viewCount(si + x, sj + y);
                }
              }
              list.add(new int[]{si - curI, sj - curJ, score});
            }
          }
        if (!list.isEmpty()) {
          Collections.sort(list, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
              return Integer.compare(o1[2], o2[2]);
            }
          });
          return new int[]{list.get(0)[0], list.get(0)[1]};
        }
      }
      isEmpty++;
    }
    return null;
  }

  private boolean mainProcess() {
    // 最初は歩かずに look
    if (lookCount > 0) {
      // (step1, step2) に行けるなら行く
      if (viewCount(curI + step1, curJ + step2) <= isEmpty) {
        if (!walk(step1, step2)) return true;
      } else {
        // 行けなければ歩く場所を決めてもらう
        int[] where = whereToWalk();
        if (where != null) {
          if (!walk(where[0], where[1])) return true;
        }
      }
    }

    if (!viewed[p(curI)][p(curJ)]) look();

    matchCandidates();
    // あまり歩いていないうちはカットしない
    if (lookPath.size() <= S * 1.3) return false;

    if (stopGuessing > 0) {
      stopGuessing--;
      return false;
    }

    Collections.sort(candidates);
    cutCandidates();

    double diff = candidates.get(0).matchingScore - candidates.get(1).matchingScore;
    if (diff < 0.000001 && lookPath.size() < 10 * S) return false;

    int i = candidates.get(0).i;
    int j = candidates.get(0).j;
    candidates.remove(0);
    int response = Actions.guess(new int[]{i, j});

    if (response == 1) return true;
    stopGuessing = (G / L / S * 10);

    return false;
  }

  private void init(String[] map) {
    givenMap = new byte[S][S];
    lookMap = new byte[S][S];
    for (int i = 0; i < S; i++)
      for (int j = 0; j < S; j++) {
        byte bit = 0;
        int cur = 0;
        for (int ai = -1; ai < 1; ai++) {
          for (int aj = -1; aj < 1; aj++) {
            bit |= (m(map[p(i + ai)].charAt(p(j + aj))) << cur);
            cur++;
          }
        }

        givenMap[i][j] = bit;
      }

    viewed = new boolean[S][S];

    SecureRandom random = null;
    try {
      random = SecureRandom.getInstance("SHA1PRNG");
      random.setSeed(114514);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    if (random == null) return;

    for (int i = 0; i < S; i++) {
      for (int j = 0; j < S; j++) {
        candidates.add(new MatchingPlace(i, j, random.nextInt(114514)));
      }
    }

    // 歩幅を決める
    for (int i = 2; ; i++) {
      if (gcd(S, i) == 1) {
        if (step1 == 0) {
          step1 = i;
          System.out.println("step1 = " + step1);
        } else if (gcd(step1, i) == 1) {
          step2 = i;
          System.out.println("step2 = " + step2);
          break;
        }
      }
    }
  }

  private void addLookMap(String[] look) {
    if (look[0].length() == 0) {
      System.out.println();
    }
    byte bit = 0;
    int cur = 0;
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        bit |= (m(look[i].charAt(j)) << cur);
        cur++;
      }
    }
    lookMap[p(curI)][p(curJ)] = bit;
  }

  private void cutCandidates() {
    double cuttingScore = (double) leastMatch(lookPath.size() * 4, CUT_PROB);
    cuttingScore /= (lookPath.size() * 4);
    while (candidates.size() > WIDTH) {
      int tail = candidates.size() - 1;
      if (candidates.get(tail).matchingScore < cuttingScore)
        candidates.remove(tail);
      else
        break;
    }
  }

  private void matchCandidates() {
    for (MatchingPlace place : candidates) {
      int i = place.i;
      int j = place.j;
      int okay = 0;
      for (int k = prevMatchingPathSize; k < lookPath.size(); k++) {
        int pi = lookPath.get(k)[0];
        int pj = lookPath.get(k)[1];

        if (matchGivenLook(i + pi, j + pj, pi, pj)) okay++;
      }

      double prevOkay = place.matchingScore * prevMatchingPathSize;
      double score = (prevOkay + okay) / lookPath.size();
      place.setScore(score);
    }
    prevMatchingPathSize = lookPath.size();
  }

  /**
   * もし正しい位置にいるならば, p 以上の確率で x 個見たときに少なくとも t 個は当たるだろうという個数を出す
   *
   * @param x
   * @param p
   * @return
   */
  private int leastMatch(int x, double p) {
    double change = 0.2;

    double sum = 0.0;
    for (int q = x; q >= 0; q--) {
      double tmp = 1.0;
      for (int i = 0; i < q; i++) {
        tmp *= (x - i);
        tmp /= (i + 1);
        tmp *= (1.0 - change);
      }
      for (int i = 0; i < x - q; i++) {
        tmp *= change;
      }
      sum += tmp;
      if (sum >= p) return q;
    }
    return 0;
  }

  private boolean matchGivenLook(int gi, int gj, int li, int lj) {
    return givenMap[p(gi)][p(gj)] == lookMap[p(li)][p(lj)];
  }

  private boolean walk(int di, int dj) {
    walkCount++;
    int ret = Actions.walk(new int[]{di, dj});
    curI = p(curI + di);
    curJ = p(curJ + dj);
    return ret == 0;
  }

  private void look() {
    lookCount++;
    addLookMap(Actions.look());
    lookPath.add(new int[]{curI, curJ});

    viewed[curI][curJ] = true;
  }

  private int viewCount(int i, int j) {
    int cnt = 0;
    for (int k = -1; k <= 1; k++)
      for (int l = -1; l <= 1; l++) {
        if (viewed[p(i + k)][p(j + l)]) cnt++;
      }
    return cnt;
  }
  private long gcd(long a, long b) {
    return b == 0 ? a : gcd(b, a % b);
  }
}
