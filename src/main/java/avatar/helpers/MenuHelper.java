package avatar.helpers;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MenuHelper {
    private String title;
    private List<MenuHelper> subMenus;
    private int action;

    public void addAction(int action) {
        this.action = action;
    }

    public void addSubMenu(MenuHelper subMenu) {
        this.subMenus.add(subMenu);
    }

    public List<MenuHelper> getSubMenus() {
        return subMenus;
    }
}
