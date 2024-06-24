/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avatar.model;

import avatar.db.DbManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import org.apache.log4j.Logger;

/**
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public class FoodManager {

    private static final FoodManager instance = new FoodManager();
    private static final Logger logger = Logger.getLogger(FoodManager.class);

    public static FoodManager getInstance() {
        return instance;
    }

    @Getter
    private final List<Food> foods = new ArrayList<>();

    public void load() {
        foods.clear();
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM foods;");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                int img = rs.getInt("img");
                int shop = rs.getInt("shop");
                int percentHealth = rs.getInt("percent_health");
                int price = rs.getInt("price");
                Food food = Food.builder()
                        .id(id)
                        .name(name)
                        .description(description)
                        .shop(shop)
                        .icon(img)
                        .percentHelth(percentHealth)
                        .price(price)
                        .build();
                foods.add(food);
            }
        } catch (SQLException ex) {
            logger.error("load foods err ", ex);
        }
    }

    public Food findFoodByFoodID(int id) {
        for (Food food : foods) {
            if (food.getId() == id) {
                return food;
            }
        }
        return null;
    }
}
