package cz.muni.fi.group05.room03.ui;

import cz.muni.fi.group05.room03.ui.form.GuestForm;
import cz.muni.fi.group05.room03.ui.form.ReservationForm;
import cz.muni.fi.group05.room03.ui.table.GuestTable;
import cz.muni.fi.group05.room03.ui.table.ReservationTable;
import cz.muni.fi.group05.room03.ui.table.RoomTable;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.GridLayout;

public class ContentPanelController {

    private final I18N I18N = new I18N(ContentPanelController.class);;
    private final JPanel rootPanel;
    private final JMenuBar menuBar;
    private final CardLayout cards;
    private final ColoredButton reservationsButton;
    private final ColoredButton guestsButton;
    private final ColoredButton roomsButton;
    private ContentPanelNames currentActive;

    public ContentPanelController(ReservationTable reservationTable, GuestTable guestTable, RoomTable roomTable,
                                  ReservationForm reservationForm, GuestForm guestForm) {
        rootPanel = new JPanel(new CardLayout());
        cards = (CardLayout) rootPanel.getLayout();
        reservationsButton = createMenuButton(I18N.getString("reservations"), ContentPanelNames.RESERVATION_TABLE);
        guestsButton = createMenuButton(I18N.getString("guests"), ContentPanelNames.GUEST_TABLE);
        roomsButton = createMenuButton(I18N.getString("rooms"), ContentPanelNames.ROOM_TABLE);
        menuBar = new JMenuBar();
        menuBar.setLayout(new GridLayout(1, 3));
        menuBar.add(reservationsButton);
        menuBar.add(guestsButton);
        menuBar.add(roomsButton);
        addPanel(reservationTable.getPanel(), ContentPanelNames.RESERVATION_TABLE);
        addPanel(guestTable.getPanel(), ContentPanelNames.GUEST_TABLE);
        addPanel(roomTable.getPanel(), ContentPanelNames.ROOM_TABLE);
        addPanel(reservationForm.getPanel(), ContentPanelNames.RESERVATION_FORM);
        addPanel(guestForm.getPanel(), ContentPanelNames.GUEST_FORM);
    }

    private void addPanel(JPanel panel, ContentPanelNames name) {
        rootPanel.add(panel, name.name());
    }

    private ColoredButton createMenuButton(String name, ContentPanelNames pane) {
        var menu = ColoredButton.menu(name);
        menu.addActionListener(e -> switchPanel(pane));
        return menu;
    }

    public void switchPanel(ContentPanelNames panelName){
        boolean doSwitch = true;
        if (currentActive == ContentPanelNames.RESERVATION_FORM || currentActive == ContentPanelNames.GUEST_FORM) {
            doSwitch = Message.showYesNoDialog(I18N.getString("switchCheckTitle"),
                    I18N.getString("switchQuestionMessage") + '\n' + I18N.getString("switchWarningMessage"));
        }
        if (doSwitch) {
            switchPanelNow(panelName);
        }
    }

    public void switchPanelNow(ContentPanelNames panelName) {
        cards.show(rootPanel, panelName.name());
        reservationsButton.setActive(panelName == ContentPanelNames.RESERVATION_TABLE || panelName == ContentPanelNames.RESERVATION_FORM);
        guestsButton.setActive(panelName == ContentPanelNames.GUEST_TABLE || panelName == ContentPanelNames.GUEST_FORM);
        roomsButton.setActive(panelName == ContentPanelNames.ROOM_TABLE);
        currentActive = panelName;
    }

    public Runnable switchPanelAction(ContentPanelNames panelName) {
        return () -> switchPanel(panelName);
    }

    public JMenuBar getMenuBar() {
        return menuBar;
    }

    public JPanel getPanel(){
        return rootPanel;
    }

    public enum ContentPanelNames {

        RESERVATION_TABLE,
        RESERVATION_FORM,
        GUEST_TABLE,
        GUEST_FORM,
        ROOM_TABLE
    }
}
