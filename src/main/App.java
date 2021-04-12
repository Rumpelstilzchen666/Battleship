public class App {
    public static void main(String[] args) {
        ShipType[] ruWiki = new ShipType[] {
                new ShipType("Линкор", 1, 4),
                new ShipType("Крейсер", 2, 3),
                new ShipType("Эсминец", 3, 2),
                new ShipType("Торпедный катер", 4, 1)};
        ShipType[] Hasbro = new ShipType[] {
                new ShipType("Авианосец", 1, 5),
                new ShipType("Линкор", 1, 4),
                new ShipType("Эсминец", 1, 3),
                new ShipType("Подлодка", 1, 3),
                new ShipType("Сторожевой корабль", 1, 2)};
        ShipType[] mine = new ShipType[] {
                new ShipType("Авианосец", 1, 4),
                new ShipType("Крейсер", 1, 3),
                new ShipType("Подлодка", 1, 3),
                new ShipType("Ракетный катер", 4, 2)};
        Battle battle = new Battle(10, ruWiki);
    }
}
