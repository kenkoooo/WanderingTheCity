import java.util.ArrayList;
import java.util.Arrays;

class WanderingTheCity {
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

  int whereAmI(String[] map, int W, int L, int G) {
    S = map.length;
    init(map);

    for (int t = 0; t < 100; t++) {
      String[] look = Actions.look();
      addLookMap(look);

      Actions.walk(new int[]{2, 2});
      curI = p(curI + 2);
      curJ = p(curJ + 2);
      ArrayList<int[]> check = checkCount();
      System.out.println(Arrays.toString(look));
    }
    return 0;
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
  }

  private ArrayList<int[]> checkCount() {
    ArrayList<int[]> list = new ArrayList<>();
    for (int i = 0; i < S; i++) {
      for (int j = 0; j < S; j++) {
        int[] cnt = new int[4];
        int okay = 0;
        int fail = 0;

        for (int k = 0; k < S * S; k += 2) {
          if (lookMap[p(k - 1)][p(k - 1)] == -1) break;

          for (int ai = -1; ai < 1; ai++)
            for (int aj = -1; aj < 1; aj++)
              if (matchGivenLook(i + k + ai, j + k + aj, k + ai, k + aj)) {
                okay++;
              } else {
                fail++;
              }
        }

        cnt[0] = i;
        cnt[1] = j;
        cnt[2] = okay;
        cnt[3] = fail;
        list.add(cnt);
      }
    }
    return list;
  }

  private boolean matchGivenLook(int gi, int gj, int li, int lj) {
    return givenMap[p(gi)][p(gj)] == lookMap[p(li)][p(lj)];
  }

}
