

package com.Session.EmploiTempsV1.emploi.entities;


import lombok.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispoProfId implements Serializable {

    private Integer prof;
    private Integer jour;
    private String periode;

}
