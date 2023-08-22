package se.sundsvall.emailreader.api.model;

import lombok.Builder;

@Builder(setterPrefix = "with")
public record Email(String to, String from, String subject, String message, String id) {
}
