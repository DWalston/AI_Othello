public class test {
    public static void main(String[] args) {
        AI buddy = new AI();
        System.out.println(buddy.game.toString());
        do {} while (!buddy.makeTurn());
    }
}