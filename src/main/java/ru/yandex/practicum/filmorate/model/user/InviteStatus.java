package ru.yandex.practicum.filmorate.model.user;

public enum InviteStatus {
    ACCEPTED("ACCEPTED"),
    REFUSED("REFUSED"),
    UNCONFIRMED("UNCONFIRMED");

    private String status;

    public String getStatus() {
        return status;
    }

    InviteStatus(String status) {
        this.status = status;
    }
}
