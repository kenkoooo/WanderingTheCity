import java.security.NoSuchAlgorithmException;

public class SolutionRunner implements Runnable {
  private int W, L, G;
  private String[] map;
  SolutionRunner(String[] map, int W, int L, int G) {
    this.W = W;
    this.L = L;
    this.G = G;
    this.map = new String[map.length];
    System.arraycopy(map, 0, this.map, 0, map.length);
  }

  @Override
  public void run() {
    WanderingTheCity solution = null;
    try {
      solution = new WanderingTheCity();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    assert solution != null;
    solution.whereAmI(map, W, L, G);
  }
}