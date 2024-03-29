package cz.muni.fi.group05.room03.ui;

import cz.muni.fi.group05.room03.data.HotelSystemDao;
import cz.muni.fi.group05.room03.data.ImportantDataDao;
import cz.muni.fi.group05.room03.ui.ContentPanelController.ContentPanelNames;
import cz.muni.fi.group05.room03.ui.form.GuestForm;
import cz.muni.fi.group05.room03.ui.table.GuestTable;
import cz.muni.fi.group05.room03.ui.form.ReservationForm;
import cz.muni.fi.group05.room03.ui.table.ReservationTable;
import cz.muni.fi.group05.room03.ui.table.RoomTable;

import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;

public class HotelSystemUI {

    private static final I18N I18N = new I18N(HotelSystemUI.class);
    private static HotelSystemUI instance;

    private final ReservationTable reservationsTable;
    private final GuestTable guestTable;
    private final RoomTable roomTable;
    private final ReservationForm reservationForm;
    private final GuestForm guestForm;
    private final Footer footer;
    private final ContentPanelController contentPanelController;

    private HotelSystemUI() {
        UIManager.getDefaults().put("Button.disabledText", Color.decode("#efefef"));
        JFrame frame = new JFrame(I18N.getString("hotelResSys"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BorderLayout());

        reservationsTable = new ReservationTable();
        guestTable = new GuestTable();
        roomTable = new RoomTable();
        reservationForm = new ReservationForm();
        guestForm = new GuestForm();
        contentPanelController = new ContentPanelController(reservationsTable, guestTable, roomTable, reservationForm, guestForm);
        contentPanelController.switchPanel(ContentPanelNames.RESERVATION_TABLE);
        contentPane.add(contentPanelController.getPanel(), BorderLayout.CENTER);

        JPanel headerWithMenu = new JPanel(new GridLayout(3,0));
        headerWithMenu.add(createUpperMenu());
        headerWithMenu.add(createLabelText(I18N.getString("hotelResSys"), FontsHotelUI.HOTEL_TITLE));
        headerWithMenu.add(contentPanelController.getMenuBar());
        contentPane.add(headerWithMenu, BorderLayout.PAGE_START);

        footer = new Footer(createLabelText("", FontsHotelUI.HOTEL_FOOTER));
        contentPane.add(footer.getLabel(), BorderLayout.PAGE_END);

        frame.setVisible(true);
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public static void create() {
        if (instance != null)
            throw new UnsupportedOperationException("HotelSystemUI Error: HotelSystemUI is already created!");
        instance = new HotelSystemUI();
    }

    public static ReservationTable getReservationsTable() {
        return getAssertedInstance().reservationsTable;
    }

    public static GuestTable getGuestTable() {
        return getAssertedInstance().guestTable;
    }

    public static RoomTable getRoomTable() {
        return getAssertedInstance().roomTable;
    }

    public static ReservationForm getReservationForm() {
        return getAssertedInstance().reservationForm;
    }

    public static GuestForm getGuestForm() {
        return getAssertedInstance().guestForm;
    }

    public static Footer getFooter() {
        return getAssertedInstance().footer;
    }

    public static ContentPanelController getPanelController() {
        return getAssertedInstance().contentPanelController;
    }

    private static HotelSystemUI getAssertedInstance() {
        if (instance == null)
            throw new UnsupportedOperationException(
                    "HotelSystemUI Error: HotelSystemUI is not yet created, call create() method first!");
        return instance;
    }

    private JLabel createLabelText(String text, Font font) {
        JLabel myLabel = new JLabel(text, SwingConstants.CENTER);
        myLabel.setFont(font);
        myLabel.setForeground(Color.decode("#83C28A"));
        myLabel.setBackground(Color.decode("#3B3B3B"));
        myLabel.setOpaque(true);
        return myLabel;
    }

    private JMenuBar createUpperMenu() {
        var menu = new JMenuBar();

        var mainMenu = new JMenu(I18N.getString("menu"));
        mainMenu.setMnemonic('M');
        mainMenu.add(Menu.createMenuItem(I18N.getString("createNewRes"), KeyEvent.VK_N, I18N.getString("createNewResDes"), 'N', contentPanelController.switchPanelAction(ContentPanelNames.RESERVATION_FORM)));
        mainMenu.add(Menu.createMenuItem(I18N.getString("showAllRes"), KeyEvent.VK_R, I18N.getString("showAllResDes"), 'R', contentPanelController.switchPanelAction(ContentPanelNames.RESERVATION_TABLE)));
        mainMenu.add(Menu.createMenuItem(I18N.getString("showAllGuests"), KeyEvent.VK_G, I18N.getString("showAllGuestsDes"), 'G', contentPanelController.switchPanelAction(ContentPanelNames.GUEST_TABLE)));
        mainMenu.add(Menu.createMenuItem(I18N.getString("showAllRooms"), KeyEvent.VK_O, I18N.getString("showAllRoomsDes"), 'O', contentPanelController.switchPanelAction(ContentPanelNames.ROOM_TABLE)));
        mainMenu.add(Menu.createMenuItem(I18N.getString("about"), KeyEvent.VK_A, I18N.getString("aboutDes"), 'A', this::showAboutInfo));
        menu.add(mainMenu);

        var settings = new JMenu(I18N.getString("settings"));
        settings.setMnemonic('S');
        settings.add(Menu.createMenuItem(I18N.getString("setTax"), KeyEvent.VK_T, I18N.getString("setTaxDes"), 'T', this::setTax));
        menu.add(settings);
        return menu;
    }

    private void showAboutInfo() {
        String[] creators = new String[]{"Dominik Dubovský", "Tomáš Janoušek", "Andrea Jonásová", "Alžbeta Strompová"};
        Collections.shuffle(Arrays.asList(creators));
        Message.showInformationDialog(I18N.getString("about"),
                I18N.getString("creators") + ": " + String.join(", ", creators));
    }

    private void setTax() {
        ImportantDataDao importantDataDao = HotelSystemDao.getImportantDataDao();
        SpinnerNumberModel sModel = new SpinnerNumberModel(
            Integer.parseInt(importantDataDao.findByKey("TAX")),
            0, 100, 1);
        JSpinner spinner = new JSpinner(sModel);
        for (Component c : spinner.getEditor().getComponents()) {
            JFormattedTextField field = (JFormattedTextField) c;
            field.addCaretListener(ce -> field.setBackground(
                field.getText().chars().allMatch(Character::isDigit)
                    && field.isEditValid() ? Color.WHITE : Color.PINK));
        }
        if (Message.showYesNoDialog(I18N.getString("enterTax"), spinner)) {
            importantDataDao.update("TAX", String.valueOf(spinner.getValue()));
        }
    }
}
