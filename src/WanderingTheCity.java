import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.BitSet;

class WanderingTheCity {
  private static final int MAX_MEMO = (int) 1e8;
  private static final double MONKEY = 0.10;
  private static final double CAT = 0.90;

  private SecureRandom random;
  private BitSet[][] bitLayers;

  WanderingTheCity() throws NoSuchAlgorithmException {
    random = SecureRandom.getInstance("SHA1PRNG");
    random.setSeed(114514);
  }

  void initBitLayers(int layers, String[] map, int S) {
    bitLayers = new BitSet[layers][S];
    for (int l = 0; l < layers; l++)
      for (int s = 0; s < S; s++) {
        bitLayers[l][s] = new BitSet(S);
        for (int i = 0; i < S; i++)
          if ((map[s].charAt(i) == 'X' && random.nextDouble() < CAT)
            || (map[s].charAt(i) == '.' && random.nextDouble() < MONKEY))
            bitLayers[l][s].set(i);

      }
  }

  int whereAmI(String[] map, int W, int L, int G) {
    int S = map.length;
    int layers = Math.min(5000, MAX_MEMO / S / S);
    initBitLayers(layers, map, S);
    for (int i = 0; i < 100; i++) {

    }
    return 0;
  }
}
