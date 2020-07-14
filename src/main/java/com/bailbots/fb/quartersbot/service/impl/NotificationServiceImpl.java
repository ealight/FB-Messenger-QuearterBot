package com.bailbots.fb.quartersbot.service.impl;

import com.bailbots.fb.quartersbot.dao.ReserveHouse;
import com.bailbots.fb.quartersbot.service.MessageService;
import com.bailbots.fb.quartersbot.service.NotificationService;
import com.bailbots.fb.quartersbot.utils.CalendarUtil;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@EnableScheduling
public class NotificationServiceImpl implements NotificationService {
    private final static String GET_RESERVE_HOUSE = "SELECT * FROM `reserve_house` WHERE `facebook_id` " +
            "IN (SELECT `facebook_id` FROM `user` WHERE `notifications` = true)";

    private final MessageService messageService;
    private final EntityManager entityManager;

    public NotificationServiceImpl(MessageService messageService, EntityManager entityManager) {
        this.messageService = messageService;
        this.entityManager = entityManager;
    }

    @Override
    @Scheduled(cron = "0 0 7 * * *")
    public void sendNotificationAboutReserveHouse() {
        StringBuilder notificationText = new StringBuilder();

        Query query = entityManager.createNativeQuery(GET_RESERVE_HOUSE, ReserveHouse.class);
        List<ReserveHouse> resultList = (List<ReserveHouse>) query.getResultList();
        LinkedHashSet<String> singleFacebookIds = new LinkedHashSet<>();

        notificationText.append("⏰ Напоминание о вашем бронировании\n")
                .append("У вас есть забронированные дома:");

        resultList.forEach(reserveHouse -> {
            notificationText.append("\n\uD83C\uDFE0 Дом #").append(reserveHouse.getHouseId());
            notificationText.append(" - дата брони от: ")
                    .append(CalendarUtil.formatDate(reserveHouse.getDateFrom()))
                    .append(" до ")
                    .append(CalendarUtil.formatDate(reserveHouse.getDateTo()));
            singleFacebookIds.add(reserveHouse.getFacebookId());
        });

        singleFacebookIds.forEach(facebookId ->
                messageService.sendMessage(notificationText.toString(), facebookId));
    }
}
