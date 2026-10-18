package com.catholicraft.item;

import java.util.Arrays;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.component.WrittenBookContent;

/** Original short guides, rather than reproductions of copyrighted missal translations. */
public final class MissalPages {
    public static final WrittenBookContent MISSAL_1962 = book("Missale Romanum 1962",
        "MISSAL\n1962\n\nA short companion to the Roman liturgy according to the 1962 Missale Romanum.\n\nTurn the pages for the order of Mass, Latin prayers, and vestment colors.",
        "ORDER OF MASS\n\nPrayers at the foot of the altar\nIntroit\nKyrie\nGloria when appointed\nCollect\nEpistle\nGradual and Alleluia or Tract\nGospel\nSermon\nCreed when appointed",
        "AT THE ALTAR\n\nOffertory\nSecret\nPreface and Sanctus\nRoman Canon\nPater noster\nAgnus Dei\nCommunion\nPostcommunion\nDismissal and blessing\nLast Gospel",
        "KYRIE\n\nKyrie, eleison.\nChriste, eleison.\nKyrie, eleison.\n\nLord, have mercy.\nChrist, have mercy.\nLord, have mercy.",
        "SANCTUS\n\nSanctus, Sanctus, Sanctus Dominus Deus Sabaoth. Pleni sunt caeli et terra gloria tua. Hosanna in excelsis. Benedictus qui venit in nomine Domini. Hosanna in excelsis.",
        "AGNUS DEI\n\nAgnus Dei, qui tollis peccata mundi: miserere nobis.\n\nAgnus Dei, qui tollis peccata mundi: dona nobis pacem.",
        "LITURGICAL COLORS\n\nWhite: joyful feasts\nRed: martyrs and Pentecost\nGreen: Sundays after Epiphany and Pentecost\nViolet: penitential seasons\nRose: Gaudete and Laetare\nBlack: Masses for the dead",
        "IN YOUR CHAPEL\n\nPlace this book on a lectern. Add an altar, candles and crucifix, and keep a sanctuary lamp near the tabernacle.\n\nThis is an introductory companion, not a complete missal.");

    public static final WrittenBookContent MODERN_MISSAL = book("Roman Missal Companion",
        "ROMAN MISSAL\nModern companion\n\nA short guide to the ordinary form of the Roman Rite.\n\nRead the order of Mass and explore the chapel furnishings. This is an original introduction, not the complete missal.",
        "INTRODUCTORY RITES\n\nEntrance\nSign of the Cross\nGreeting\nPenitential Act\nKyrie\nGloria when appointed\nCollect\n\nThe assembly gathers to hear the Word and celebrate the Eucharist.",
        "LITURGY OF THE WORD\n\nFirst reading\nResponsorial psalm\nSecond reading when appointed\nGospel acclamation\nGospel\nHomily\nProfession of Faith when appointed\nUniversal Prayer",
        "LITURGY OF THE EUCHARIST\n\nPreparation of the gifts\nPrayer over the offerings\nPreface and Sanctus\nEucharistic Prayer\nLord's Prayer\nSign of Peace\nFraction and Agnus Dei\nCommunion",
        "CONCLUDING RITES\n\nPrayer after Communion\nBlessing\nDismissal\n\nThe faithful are sent to live their faith through prayer, service, and charity.",
        "THE CHAPEL\n\nAltar: the central table of the Eucharistic celebration.\n\nAmbo or lectern: the place for proclaiming Scripture.\n\nTabernacle: the place of reservation of the Blessed Sacrament.",
        "LITURGICAL COLORS\n\nWhite: joyful feasts\nRed: martyrs and Pentecost\nGreen: Ordinary Time\nViolet: Advent and Lent\nRose: Gaudete and Laetare\nBlack: an option for Masses for the dead",
        "IN YOUR CHAPEL\n\nUse a lectern for this missal, equip a chasuble in the chest slot, and decorate the altar with a chalice and candles.\n\nThe game depicts furnishings and devotion; it does not perform real sacraments.");

    public static final WrittenBookContent GUIDE = book("CatholiCraft Field Guide",
        "CATHOLICRAFT\nField Guide\n\nBuild a chapel, furnish an altar, collect devotional objects, and wear vestments.\n\nFind everything in the CatholiCraft creative tab or make it with survival recipes.",
        "GETTING STARTED\n\nCraft a field guide with a book and gold nugget.\n\nUse quartz for the altar and fonts, gold for vessels, and wool for vestments.\n\nMissals work on vanilla lecterns.",
        "TABERNACLE\n\nRight-click to open its nine storage slots. The door opens while someone is using it.\n\nItems persist when the world saves and drop when the block is broken.\n\nPlace a sanctuary lamp nearby.",
        "HOLY WATER\n\nWater bucket: fills three charges.\nGlass bottle: collects one charge.\n\nUse holy water to extinguish yourself and remove poison, weakness, slowness and nausea. Empty bottles are returned.",
        "LIGHT AND SOUND\n\nRight-click a candle or sanctuary lamp with flint and steel to light it; use an empty hand to extinguish it.\n\nRight-click the church bell to ring it. Redstone rising edges ring it too.",
        "VESTMENTS\n\nChasubles: white, red, green, violet, rose, black.\n\nAlb: legs\nBiretta or mitre: head\nShoes: feet\n\nRight-click to equip. Clothing gives no armor protection.",
        "DEVOTION\n\nMedals: equip in the off hand.\n\nRosary: use for the next mystery. Sneak-use switches sets.\n\nThurible: carry incense and use to release smoke.",
        "BUILDING TOOLS\n\nOperators can use:\n/catholicraft kit\n/catholicraft chapel\n\nThe chapel command builds an east-facing 11 by 15 chapel only if the entire building area is empty.\n\nUse in a clear area above solid ground.",
        "ALTAR CLOTHS\n\nUse a colored altar cloth on an altar to change its frontal. The top linen remains white.\n\nThe old frontal returns to your inventory. Applying the same color uses nothing.",
        "ADVENT WREATH\n\nUse flint and steel to light one more candle, up to four.\n\nUse an empty hand to extinguish one candle.\n\nThe third candle is rose; the others are violet.",
        "INCENSE BURNER\n\nUse incense on the burner. Each incense burns for one minute. It holds five minutes of fuel.\n\nUse an empty hand to check the time remaining. Fuel survives saving and loading.",
        "ASPERGILLUM\n\nCraft it empty, then use it on a filled font. One font charge fills eight uses.\n\nUse it to sprinkle yourself and nearby players. The blue item bar shows the water remaining.",
        "PEWS AND STORAGE\n\nUse an empty hand on a pew to sit. Sneak to leave the seat.\n\nThe sacristy cabinet has 27 slots for chapel supplies. Its doors work like the tabernacle's.",
        "STATIONS OF THE CROSS\n\nUse a station plaque to select I through XIV. Sneak-use selects the previous station.\n\nCraft a Stations companion for short original reflections on all fourteen stations.",
        "MORE VESTMENTS\n\nDalmatics come in six liturgical colors. A cassock and surplice add more clothing choices.\n\nEach uses the chest slot. Use an armor stand to display a complete set.",
        "DEVOTIONAL CORNER\n\nAdd statues of Our Lady and St. Joseph, a votive rack, and a prayer book.\n\nCarry a scapular or processional cross in the off hand. Use the altar bell for a soft chime.",
        "MASS PREPARATION\n\nWear an alb and chasuble.\n\nNear the altar: crucifix, chalice, two lit candles, and an ambo or lectern.\n\nBring altar bread and a wine cruet.\nCruet recipe: bottle, berries, sugar.",
        "CELEBRATING IN GAME\n\nUse a missal ON the altar: 1962 selects the traditional order; modern selects the modern order.\n\nUse again for each step. Nearby players see the guidance. Sneak-use ends your session. Empty hand shows status.",
        "GIFTS AND MEMORY\n\nOffertory uses one bread and one wine cruet, returning the bottle. Missing gifts use nothing.\n\nProgress survives saving. The player who began the service leads it. The liturgy is represented in roleplay.",
        "GREGORIAN CHANT\n\nThree full recordings: Veni Creator Spiritus, Veni Sancte Spiritus, and Dies Irae.\n\nPlay their discs in a jukebox. Eject to stop. The cathedral cabinet has all three. The performer released them to the public domain.",
        "SACRED ART AND MONKS\n\nCraft framed paintings by Fra Angelico, Raphael and Leonardo. Use a painting for its credit.\n\nMonks sell supplies for one emerald. Sneak-use a monk to choose a supply, then use an emerald to buy it.",
        "FIND A CATHEDRAL\n\nCathedrals appear in new temperate Overworld chunks, with three monks and stocked sacristies.\n\nOperators can use:\n/locate structure catholicraft:cathedral\n/catholicraft cathedral\n/catholicraft monk");

    private static WrittenBookContent book(String title, String... pages) {
        return new WrittenBookContent(Filterable.passThrough(title), "CatholiCraft", 0,
            Arrays.stream(pages).map(text -> Filterable.<Component>passThrough(Component.literal(text))).toList(), true);
    }

    private MissalPages() {}
}
