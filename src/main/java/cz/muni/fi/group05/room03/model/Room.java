package cz.muni.fi.group05.room03.model;

import cz.muni.fi.group05.room03.ui.I18N;

public class Room {

    private final RoomType type;
    private final Double price;
    private final String key;
    private Integer beds;
    private RoomStatus status;

    public Room(String key, RoomType type, Integer beds, RoomStatus status, Double price) {
        this.key = key;
        this.type = type;
        this.beds = beds;
        this.status = status;
        this.price = price;
    }

    public String getKey() {
        return key;
    }

    public RoomType getType() {
        return type;
    }

    public Integer getBeds() {
        return beds;
    }

    public void setBeds(Integer beds) {
        this.beds = beds;
    }

    public Double getPrice() {
        return price;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public enum RoomStatus {

        NOT_OCCUPIED,
        OCCUPIED;

        private static final I18N I18N = new I18N(RoomStatus.class);

        @Override
        public String toString() {
            return I18N.getString(this.name());
        }
    }

    public enum RoomType {

        FAMILY,
        SINGLE,
        DOUBLE;

        private static final I18N I18N = new I18N(RoomType.class);

        @Override
        public String toString() {
            return I18N.getString(this.name());
        }
    }
}
