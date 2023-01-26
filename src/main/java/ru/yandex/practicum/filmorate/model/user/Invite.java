package ru.yandex.practicum.filmorate.model.user;

import lombok.Data;

@Data
public class Invite {
    private int inviteId;
    private int from;
    private int to;
    private InviteStatus status;
}
