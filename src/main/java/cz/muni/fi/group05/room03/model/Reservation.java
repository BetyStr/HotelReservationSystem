package cz.muni.fi.group05.room03.model;

import cz.muni.fi.group05.room03.ui.I18N;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Reservation {

    private final String name;
    private final LocalDate dateFrom;
    private final LocalDate dateTo;
    private final String telephone;
    private final String email;
    private final Integer people;
    private final String info;
    private ReservationState state;
    private Long id;
    private int daysToPerform = 0;

    public Reservation(String name, LocalDate dateFrom, LocalDate dateTo, String telephone, String email,
                       Integer people, String info, ReservationState state, Long id) {
        this.name = name;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.telephone = telephone;
        this.email = email;
        this.people = people;
        this.info = info;
        this.state = state;
        this.id = id;
        calculateDaysToPerform();
    }

    public Reservation(String name, LocalDate dateFrom, LocalDate dateTo, String telephone,
                       String email, Integer people, String info, ReservationState state) {
        this(name, dateFrom, dateTo, telephone, email, people, info, state, null);
    }

    public String getName() {
        return name;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getEmail() {
        return email;
    }

    public Integer getPeople() {
        return people;
    }

    public String getInfo() {
        return info;
    }

    public ReservationState getState() {
        return state;
    }

    public void setState(ReservationState state) {
        this.state = state;
        calculateDaysToPerform();
    }

    public int getDaysToPerform() {
        return daysToPerform;
    }

    public void calculateDaysToPerform() {
        if (state == ReservationState.UPCOMING) {
            daysToPerform = (int) ChronoUnit.DAYS.between(LocalDate.now(), dateFrom);
        } else if (state == ReservationState.DOING) {
            daysToPerform = (int) ChronoUnit.DAYS.between(LocalDate.now(), dateTo);
        } else {
            daysToPerform = Integer.MAX_VALUE;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public enum ReservationState {

        DOING,
        UPCOMING,
        CANCELED,
        ENDED;

        private static final I18N I18N = new I18N(ReservationState.class);

        @Override
        public String toString() {
            return I18N.getString(this.name());
        }
    }
}
