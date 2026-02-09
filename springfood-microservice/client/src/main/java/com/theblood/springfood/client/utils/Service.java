package com.theblood.springfood.client.utils;

public enum Service {
    ID,
    AUTHOR,
    MANAGEMENT,
    MEDIA,
    NOTIFICATION,
    QUESTIONING,
    QUESTIONNAIRE,
    CONFERENCE,
    MEETING,
    SEARCH;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
