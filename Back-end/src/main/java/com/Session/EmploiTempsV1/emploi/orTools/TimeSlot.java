// FICHIER : com/Session/EmploiTempsV1/emploi/grpSerV6/TimeSlot.java
package com.Session.EmploiTempsV1.emploi.orTools;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@EqualsAndHashCode // Important pour l'utilisation dans les Set et Map
public class TimeSlot {
    private final DayOfWeek day;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public TimeSlot(DayOfWeek day, LocalTime startTime, int durationInMinutes) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = startTime.plusMinutes(durationInMinutes);
    }

    @Override
    public String toString() {
        return day + " [" + startTime + "-" + endTime + "]";
    }
}