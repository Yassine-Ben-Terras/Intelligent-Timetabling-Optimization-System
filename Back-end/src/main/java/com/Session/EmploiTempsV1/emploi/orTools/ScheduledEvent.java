// FICHIER : com/Session/EmploiTempsV1/emploi/grpSerV6/ScheduledEvent.java
package com.Session.EmploiTempsV1.emploi.orTools;

import com.Session.EmploiTempsV1.emploi.entities.Local;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScheduledEvent {
    private EventToSchedule eventDetails;
    private TimeSlot startTimeSlot;
    private Local local;
}