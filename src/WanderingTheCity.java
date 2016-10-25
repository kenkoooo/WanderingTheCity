import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;

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
   * @param x
   * @return 座標 [0..S-1]
   */
  private int p(int x) {
    if (x < 0) return x + S;
    if (x >= S) return x - S;
    return x;
  }

  private int m(char c) {
    return c == 'X' ? 1 : 0;
  }

  // X なら 1 . なら 0 まだ見ていなければ -1 を入れる
  private int[][] givenMap;
  private int[][] lookMap;

  private int curI = 0, curJ = 0;
  private int S, W, L, G;

  // いままで look した場所のリスト
  private ArrayList<int[]> lookPath = new ArrayList<>();

  // 答えの候補のリスト
  private ArrayList<MatchingPlace> candidates = new ArrayList<>();

  // 歩幅。2 以上で S の約数でない数字にしたい
  private int step;

  // あと何回 guess クエリを控えるか
  private int stopGuessing = 0;

  /**
   * 呼び出されるメソッド
   *
   * @param map
   * @param W
   * @param L
   * @param G
   * @return 特に意味のない数字(fuck)
   */
  int whereAmI(String[] map, int W, int L, int G) {
    S = map.length;
    this.W = W;
    this.L = L;
    this.G = G;
    init(map);

    while (!mainProcess()) ;
    return 0;
  }

  private boolean mainProcess() {
    if (isAlreadyViewed(curI, curJ)) {
      walk(step, 0);
      return false;
    }

    addLookMap(Actions.look());
    lookPath.add(new int[]{curI, curJ});
    if (!walk(step, step)) return true;

    if (stopGuessing > 0) {
      stopGuessing--;
      return false;
    }

    if (lookPath.size() % (S / 20) == 0) {
      matchAndSort();
    }

    // あまり歩いていないうちはマッチングしない
    if (lookPath.size() < S / 2) return false;
    matchAndSort();
    if (candidates.size() > 0.15 * S * S) return false;

    int i = candidates.get(0).i;
    int j = candidates.get(0).j;
    candidates.remove(0);
    int response = Actions.guess(new int[]{i, j});
    if (response == 1) return true;

    stopGuessing = (G / L / 2);
    return false;
  }

  private void init(String[] map) {
    givenMap = new int[S][S];
    lookMap = new int[S][S];
    for (int i = 0; i < S; i++)
      for (int j = 0; j < S; j++) {
        givenMap[i][j] = m(map[i].charAt(j));
        lookMap[i][j] = -1;
      }

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
      if (S % i != 0) {
        step = i;
        break;
      }
    }
  }

  private void addLookMap(String[] look) {
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        lookMap[p(curI - 1 + i)][p(curJ - 1 + j)] = m(look[i].charAt(j));
      }
    }
  }

  private void matchAndSort() {
    for (MatchingPlace place : candidates) {
      int i = place.i;
      int j = place.j;
      int okay = 0;
      int fail = 0;

      for (int[] p : lookPath) {
        int pi = p[0];
        int pj = p[1];

        for (int ai = -1; ai < 1; ai++)
          for (int aj = -1; aj < 1; aj++)
            if (matchGivenLook(i + pi + ai, j + pj + aj, pi + ai, pj + aj)) {
              okay++;
            } else {
              fail++;
            }
      }

      place.setScore((double) okay / (okay + fail));
    }

    Collections.sort(candidates);

    double cuttingScore = (double) leastMatch(lookPath.size() * 4, 0.99) / lookPath.size() / 4;
    while (candidates.get(candidates.size() - 1).matchingScore < cuttingScore)
      candidates.remove(candidates.size() - 1);
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

  private boolean isAlreadyViewed(int i, int j) {
    for (int ai = -1; ai < 1; ai++) {
      for (int aj = -1; aj < 1; aj++) {
        if (lookMap[p(i + ai)][p(j + aj)] != -1) return true;
      }
    }
    return false;
  }

  private boolean matchGivenLook(int gi, int gj, int li, int lj) {
    return givenMap[p(gi)][p(gj)] == lookMap[p(li)][p(lj)];
  }

  private boolean walk(int di, int dj) {
    int ret = Actions.walk(new int[]{di, dj});
    curI = p(curI + di);
    curJ = p(curJ + dj);
    return ret == 0;
  }
}
