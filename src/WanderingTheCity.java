import java.util.ArrayList;
import java.util.Collections;

class WanderingTheCity {
  class MatchingPlace implements Comparable<MatchingPlace> {
    int i, j;
    double matchingScore = 0.0;
    MatchingPlace(int i, int j) {
      this.i = i;
      this.j = j;
    }

    void setScore(double score) {
      this.matchingScore = score;
    }

    @Override
    public int compareTo(MatchingPlace o) {
      // スコアの降順にソート
      if (this.matchingScore > o.matchingScore) return -1;
      else if (this.matchingScore == o.matchingScore) return 0;
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
    return (x + S) % S;
  }

  private int m(char c) {
    return c == 'X' ? 1 : 0;
  }

  // X なら 1 . なら 0 まだ見ていなければ -1 を入れる
  private int[][] givenMap;
  private int[][] lookMap;

  private int curI = 0, curJ = 0;
  private int S;

  // いままで look した場所のリスト
  private ArrayList<int[]> lookPath = new ArrayList<>();

  // 答えの候補のリスト
  private ArrayList<MatchingPlace> candidates = new ArrayList<>();

  int whereAmI(String[] map, int W, int L, int G) {
    S = map.length;
    init(map);

    while (!mainProcess()) ;
    return 0;
  }

  private boolean mainProcess() {
    if (isAlreadyViewed(curI, curJ)) {
      walk(2, 0);
      return false;
    }

    addLookMap(Actions.look());
    lookPath.add(new int[]{curI, curJ});
    walk(2, 2);

    matchAndSort();

    if (candidates.size() < 0.15 * S * S) {
      int i = candidates.get(0).i;
      int j = candidates.get(0).j;
      candidates.remove(0);
      int response = Actions.guess(new int[]{i, j});
      if (response == 1) return true;
    }
    return false;
  }

  private void addLookMap(String[] look) {
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        lookMap[p(curI - 1 + i)][p(curJ - 1 + j)] = m(look[i].charAt(j));
      }
    }
  }

  private void init(String[] map) {
    givenMap = new int[S][S];
    lookMap = new int[S][S];
    for (int i = 0; i < S; i++)
      for (int j = 0; j < S; j++) {
        givenMap[i][j] = m(map[i].charAt(j));
        lookMap[i][j] = -1;
      }
    for (int i = 0; i < S; i++) {
      for (int j = 0; j < S; j++) {
        candidates.add(new MatchingPlace(i, j));
      }
    }
  }

  private void matchAndSort() {
    // あまり歩いていないうちはマッチングしない
    if (lookPath.size() < S / 2) return;

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
    while (candidates.get(candidates.size() - 1).matchingScore < 0.75)
      candidates.remove(candidates.size() - 1);
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

  private void walk(int di, int dj) {
    Actions.walk(new int[]{di, dj});
    curI = p(curI + di);
    curJ = p(curJ + dj);
  }
}
