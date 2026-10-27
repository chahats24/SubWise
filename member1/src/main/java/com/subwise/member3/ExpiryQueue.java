package com.subwise.member3;

import com.subwise.model.Subscription;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/** Builds a fresh priority queue so callers do not mutate the shared store. */
public final class ExpiryQueue {
    private ExpiryQueue() { }

    public static PriorityQueue<Subscription> from(List<Subscription> subscriptions) {
        Comparator<Subscription> byExpiry = Comparator
                .comparing(Subscription::getEndDate,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(Subscription::getSubscriptionId);
        PriorityQueue<Subscription> queue = new PriorityQueue<>(byExpiry);
        if (subscriptions == null) return queue;
        LocalDate today = LocalDate.now();
        for (Subscription s : subscriptions) {
            if (s != null && "Active".equalsIgnoreCase(s.getStatus())
                    && s.getEndDate() != null && !s.getEndDate().isBefore(today)) {
                queue.offer(s);
            }
        }
        return queue;
    }
}
