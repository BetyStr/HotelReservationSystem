package cz.muni.fi.group05.room03.ui;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static cz.muni.fi.group05.room03.ui.Menu.MenuCondition.EXACTLY_ONE;
import static cz.muni.fi.group05.room03.ui.Menu.MenuCondition.MORE_THAN_ZERO;

public class Menu {

    private final JPopupMenu menu;
    private final List<MenuCondition> conditions;

    private Menu(JPopupMenu menu, List<MenuCondition> conditions) {
        if (menu.getComponentCount() != conditions.size())
            throw new IllegalArgumentException("Menu has received two lists of different size!");
        this.menu = menu;
        this.conditions = conditions;
    }

    public static JMenuItem createMenuItem(String name, int keyEvent, String tooltip, char mnemonic, Runnable action) {
        var item = new JMenuItem(name);
        KeyStroke key = KeyStroke.getKeyStroke(keyEvent, KeyEvent.CTRL_DOWN_MASK);
        item.setAccelerator(key);
        item.setToolTipText(tooltip);
        item.setMnemonic(mnemonic);
        item.addActionListener(e -> action.run());
        return item;
    }

    public void updateActions(int selectedRowsNum) {
        for (var cId = 0; cId < menu.getComponentCount(); cId++) {
            if (conditions.get(cId) == MORE_THAN_ZERO) {
                menu.getComponent(cId).setEnabled(selectedRowsNum > 0);
            } else if (conditions.get(cId) == EXACTLY_ONE) {
                menu.getComponent(cId).setEnabled(selectedRowsNum == 1);
            }
        }
    }

    public JPopupMenu getPopMenu() {
        return menu;
    }

    public enum MenuCondition {

        MORE_THAN_ZERO,
        EXACTLY_ONE
    }

    public static class Builder {

        private final JPopupMenu menu = new JPopupMenu();
        private final List<MenuCondition> countList = new ArrayList<>();
        public Builder addMenuItem(JMenuItem menuItem, MenuCondition condition) {
            countList.add(condition);
            menu.add(menuItem);
            return this;
        }

        public Menu build() {
            return new Menu(menu, countList);
        }
    }
}
