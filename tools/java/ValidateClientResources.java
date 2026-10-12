import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import com.catholicraft.item.MissalPages;
import com.catholicraft.item.DevotionalPages;
import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.Style;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;

/** Validate client asset formats with Minecraft's parsers without creating a window. */
public final class ValidateClientResources {
    public static void main(String[] args) throws Exception {
        Path assets=Path.of(args[0]);
        int models=0,items=0,equipment=0,states=0;
        try(var paths=Files.walk(assets)) {
            for(Path path:paths.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                String relative=assets.relativize(path).toString();
                try(var reader=Files.newBufferedReader(path)) {
                    if(relative.startsWith("models/")) {
                        CuboidModel.fromStream(reader);
                        models++;
                    } else if(relative.startsWith("equipment/")) {
                        EquipmentClientInfo.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseReader(reader)).getOrThrow();
                        equipment++;
                    } else if(relative.startsWith("items/")) {
                        var json=JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("model");
                        if(!json.get("type").getAsString().equals("minecraft:model")) throw new IllegalStateException("Unexpected model type");
                        CuboidItemModelWrapper.Unbaked.MAP_CODEC.codec().parse(JsonOps.INSTANCE,json).getOrThrow();
                        items++;
                    } else if(relative.startsWith("blockstates/")) {
                        BlockStateModelDispatcher.VANILLA_CODEC.codec().parse(JsonOps.INSTANCE,JsonParser.parseReader(reader)).getOrThrow();
                        states++;
                    }
                } catch(Exception ex) { throw new IllegalStateException("Invalid resource "+relative,ex); }
            }
        }
        System.out.printf("PASS: Minecraft parsed %d models, %d client items, %d equipment assets, %d blockstate definitions.%n",models,items,equipment,states);
        var widths=JsonParser.parseString(Files.readString(Path.of(args[1]))).getAsJsonObject();
        var splitter=new StringSplitter((codepoint,style) -> {
            var width=widths.get(String.valueOf(codepoint));
            if(width==null) throw new IllegalStateException("Missing glyph metric: "+codepoint);
            return width.getAsFloat();
        });
        int pages=0,clipped=0;
        for(var book:java.util.List.of(MissalPages.MISSAL_1962,MissalPages.MODERN_MISSAL,MissalPages.GUIDE,DevotionalPages.PRAYERS,DevotionalPages.STATIONS)) {
            int pageIndex=0;
            for(var page:book.getPages(false)) {
                pageIndex++;
                int lines=splitter.splitLines(page,114,Style.EMPTY).size();
                if(lines>14) {
                    System.err.println(book.title().raw()+" page "+pageIndex+" clips at "+lines+" lines");
                    clipped++;
                }
                pages++;
            }
        }
        if(clipped>0) throw new IllegalStateException(clipped+" clipped book pages");
        System.out.printf("PASS: All %d book pages fit Minecraft's 114-pixel wide, 14-line reading area.%n",pages);
    }
}
