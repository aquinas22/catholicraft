package com.catholicraft.item;

import java.util.ArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.component.WrittenBookContent;

/** Historic Latin prayers and original explanatory text. */
public final class DevotionalPages {
    public static final WrittenBookContent PRAYERS=book("Catholic Prayer Book",
        "PRAYER BOOK\n\nA small collection of traditional Latin prayers for your chapel.\n\nThe following pages include the Sign of the Cross, Pater noster, Ave Maria and Gloria Patri.",
        "SIGN OF THE CROSS\n\nIn nomine Patris, et Filii, et Spiritus Sancti. Amen.\n\nBegin and end your time of prayer with the Sign of the Cross.",
        "PATER NOSTER\n\nPater noster, qui es in caelis, sanctificetur nomen tuum. Adveniat regnum tuum. Fiat voluntas tua, sicut in caelo et in terra.",
        "PATER NOSTER\nContinued\n\nPanem nostrum quotidianum da nobis hodie. Et dimitte nobis debita nostra, sicut et nos dimittimus debitoribus nostris.",
        "PATER NOSTER\nConclusion\n\nEt ne nos inducas in tentationem, sed libera nos a malo. Amen.",
        "AVE MARIA\n\nAve Maria, gratia plena, Dominus tecum. Benedicta tu in mulieribus, et benedictus fructus ventris tui, Iesus.\n\nSancta Maria, Mater Dei, ora pro nobis peccatoribus, nunc et in hora mortis nostrae. Amen.",
        "GLORIA PATRI\n\nGloria Patri, et Filio, et Spiritui Sancto. Sicut erat in principio, et nunc, et semper, et in saecula saeculorum. Amen.",
        "PRAYER IN YOUR CHAPEL\n\nSit on a pew, use a rosary, or follow the Stations of the Cross.\n\nA quiet corner with a crucifix and candles can make a little place of prayer in your Minecraft home.");
    public static final WrittenBookContent STATIONS=stations();
    private static WrittenBookContent stations() {
        var pages=new ArrayList<String>();
        pages.add("STATIONS OF THE CROSS\n\nAn original companion to the fourteen traditional stations.\n\nPlace fourteen station plaques in your chapel. Use each plaque to select its numeral; sneak-use goes backwards.");
        String[] reflections={
            "Consider justice, courage and mercy. Remember those who are judged unfairly.",
            "Consider patient love. Remember people carrying heavy burdens today.",
            "Consider perseverance. Remember those who need strength to begin again.",
            "Consider the love of a mother. Remember families facing sorrow.",
            "Consider practical kindness. Remember that a small act of help can matter.",
            "Consider compassion. Remember those who suffer without being noticed.",
            "Consider hope after failure. Remember people worn down by repeated hardship.",
            "Consider comfort offered in suffering. Remember those who mourn.",
            "Consider endurance. Remember those who feel that their strength is gone.",
            "Consider the dignity of each person. Remember those who are humiliated or excluded.",
            "Consider steadfast love. Remember victims of violence and cruelty.",
            "Consider Christ's self-giving love. Remember the dying and those who care for them.",
            "Consider tender care. Remember the bereaved and those who accompany them.",
            "Consider quiet trust and hope. Remember the dead, and pray for peace."
        };
        for(int i=0;i<14;i++) pages.add((i+1)+". "+StationNames.TITLES[i]+"\n\n"+reflections[i]);
        pages.add("AFTER THE STATIONS\n\nSpend a moment in silence.\n\nCarry compassion into the rest of your day through prayer, patience, and care for your neighbor.");
        return book("Stations of the Cross",pages.toArray(String[]::new));
    }
    private static WrittenBookContent book(String title,String... pages) {
        return new WrittenBookContent(Filterable.passThrough(title),"CatholiCraft",0,
            java.util.Arrays.stream(pages).map(p -> Filterable.<Component>passThrough(Component.literal(p))).toList(),true);
    }
    private DevotionalPages() {}
}
