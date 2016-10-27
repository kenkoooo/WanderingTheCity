class SolutionRunner implements Runnable {
  private int W, L, G;
  private String[] map;
  private long seed;
  SolutionRunner(String[] map, int W, int L, int G, long seed) {
    this.W = W;
    this.L = L;
    this.G = G;
    this.seed = seed;
    this.map = new String[map.length];
    System.arraycopy(map, 0, this.map, 0, map.length);
  }

  @Override
  public void run() {
    try {
      WanderingTheCity solution = new WanderingTheCity();
      solution.whereAmI(map, W, L, G);
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(seed);
    }
  }
}
