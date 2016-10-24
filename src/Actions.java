import java.util.concurrent.LinkedBlockingQueue;

class Actions {
  static LinkedBlockingQueue<int[]> requests = new LinkedBlockingQueue<>();
  static LinkedBlockingQueue<String[]> responses = new LinkedBlockingQueue<>();

  public static String[] look() {
    requests.add(new int[0]);
    try {
      return responses.take();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static int walk(int[] shift) {
    requests.add(new int[]{shift[0], shift[1], 0});
    try {
      if (responses.take().length == 0) return -1;
      return 0;
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return -1;
  }

  public static int guess(int[] coord) {
    requests.add(new int[]{coord[0], coord[1], 1});
    try {
      if (responses.take()[0].equals("1")) return 1;
      return 0;
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return -1;
  }

}