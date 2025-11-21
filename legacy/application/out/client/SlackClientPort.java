package com.ryuqq.crawlinghub.application.port.out.client;

/** Slack Client Port */
public interface SlackClientPort {

    void sendNotification(String message);
}
