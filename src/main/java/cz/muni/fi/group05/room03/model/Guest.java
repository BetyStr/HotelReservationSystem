package cz.muni.fi.group05.room03.model;

import cz.muni.fi.group05.room03.ui.I18N;

public class Guest {

    private final String name;
    private final String idCard;
    private final GuestGeneration generation;
    private final String info;
    private final Long reservationId;
    private Long id;
    private String room;

    public Guest(String name, String room, String idCard, GuestGeneration generation, String info, Long reservationId, Long id) {
        this.name = name;
        this.room = room;
        this.idCard = idCard;
        this.generation = generation;
        this.info = info;
        this.reservationId = reservationId;
        this.id = id;
    }

    public Guest(String name, String room, String idCard, GuestGeneration generation, String info, Long reservationId) {
        this(name, room, idCard, generation, info, reservationId, null);
    }

    public String getName() {
        return name;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getIdCard() {
        return idCard;
    }

    public GuestGeneration getGeneration() {
        return generation;
    }

    public String getInfo() {
        return info;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public enum GuestGeneration {

        ADULT,
        CHILD;

        private static final I18N I18N = new I18N(GuestGeneration.class);

        @Override
        public String toString() {
            return I18N.getString(this.name());
        }
    }
}
