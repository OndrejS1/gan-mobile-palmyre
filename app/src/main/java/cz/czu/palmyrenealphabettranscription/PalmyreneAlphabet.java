package cz.czu.palmyrenealphabettranscription;

public enum PalmyreneAlphabet {
    aleph(3,"Aleph", "' / A", "\uD802\uDC60"),
    beth(5,"Beth", "B", "\uD802\uDC61"),
    gimel(7, "Gimel", "G", "\uD802\uDC62"),
    daleth(6, "Daleth", "D", "\uD802\uDC63"),
    he(8, "He", "H", "\uD802\uDC64"),
    waw(25,"Waw", "V", "\uD802\uDC65"),
    zayin(27,"Zayin", "Z", "\uD802\uDC66"),
    heth(9,"Heth", "KH", "\uD802\uDC67"),
    teth(24,"Teth", "T", "\uD802\uDC68"),
    yodh(26,"Yodh", "Y", "\uD802\uDC69"),
    kaph(10,"Kaph", "K / C", "\uD802\uDC6A"),
    lamedh(11,"Lamedh", "L", "\uD802\uDC6B"),
    mem(13,"Mem", "M", "\uD802\uDC6C"),
    nun_final(15,"Final nun", "N", "\uD802\uDC6D"),
    nun(14,"Nun", "N", "\uD802\uDC6E"),
    samekh(21,"Samekh", "S", "\uD802\uDC6F"),
    ayin(4,"Ayin", "' / E / 5", "\uD802\uDC70"),
    pe(16,"Pe", "P", "\uD802\uDC71"),
    sadhe(20,"Sadhe", "TS", "\uD802\uDC72"),
    qoph(17,"Qoph", "Q", "\uD802\uDC73"),
    resh(18,"Resh", "R", "\uD802\uDC74"),
    shin(22,"Shin", "SH", "\uD802\uDC75"),
    taw(23,"Taw", "TH", "\uD802\uDC76"),

    left(12,"Left", "<-", "\uD802\uDC77"),
    right(19,"Right", "->", "\uD802\uDC78"),

    one(0,"One", "1", "\uD802\uDC79"),
    two(40,"Two", "2", "\uD802\uDC7A"),
    three(41,"Three", "3", "\uD802\uDC7B"),
    four(42,"Four", "4", "\uD802\uDC7C"),
    five(43,"Five", "5", "\uD802\uDC7D"),
    ten(1,"Ten", "10, 100", "\uD802\uDC7E"),
    twenty(2,"Twenty", "20", "\uD802\uDC7F");

    private int index;
    private String name;
    private String transcription;
    private String symbol;

    private PalmyreneAlphabet(int index, String name, String transcription, String symbol) {
        this.index = index;
        this.name = name;
        this.transcription = transcription;
        this.symbol = symbol;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public String getTranscription() {
        return transcription;
    }

    public String getSymbol() {
        return symbol;
    }

    public static PalmyreneAlphabet fromIndex(int index) {
        for(PalmyreneAlphabet pa : values())
        {
            if (pa.index == index) return pa;
        }
        return null;
    }
}
