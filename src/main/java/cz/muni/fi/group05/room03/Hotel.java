package cz.muni.fi.group05.room03;

import cz.muni.fi.group05.room03.data.HotelSystemDao;
import cz.muni.fi.group05.room03.ui.HotelSystemUI;

import java.awt.EventQueue;

public class Hotel {

    public static void main(String[] args) {
        EventQueue.invokeLater(HotelSystemDao::create);
        EventQueue.invokeLater(HotelSystemUI::create);
    }
}
