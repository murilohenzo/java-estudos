public class Main {
    public static void main(String[] args) {
        int[] vec = new int[5];
        for (int i = 0; i < 5; i++) {
            vec[i] = i + 1;
        }

        for (int element:
             vec) {
            System.out.println(element);
        }
    }
}