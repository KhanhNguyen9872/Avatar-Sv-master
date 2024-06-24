package avatar.play;

import avatar.db.DbManager;
import avatar.model.GameData;
import avatar.model.MapItem;
import avatar.model.MapItemType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Map {

    private byte id;
    private byte type;
    private String name;
    private List<Zone> zones;
    private List<MapItem> mapItems;
    private List<MapItemType> mapItemTypes;

    public Map(int id, int type, int maxEntrys) {
        this.id = (byte) id;
        this.type = (byte) type;
        this.zones = new ArrayList<>();
        this.mapItems = new ArrayList<>();
        this.mapItemTypes = new ArrayList<>();
        load();
        for (int i = 0; i < maxEntrys; ++i) {
            this.zones.add(new Zone(this, (byte) i));
        }
    }

    public void load() {
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `map_item` WHERE `id` = ?;");) {
            ps.setInt(1, this.id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int idType = rs.getInt("type_id");
                int type = rs.getInt("type");
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                MapItem mapItem = MapItem.builder().id((short) id).type((byte) type).typeID((short) idType).x((byte) x).y((byte) y).build();
                MapItemType mapItemType = GameData.getInstance().findMapItemType(idType);
                mapItems.add(mapItem);
                mapItemTypes.add(mapItemType);
            }
            rs.close();
        } catch (SQLException ex) {
            Logger.getLogger(Map.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void update() {

    }
}
