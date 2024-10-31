package com.michelin.throughputfxproject.entities.servers;

import com.michelin.throughputfxproject.entities.Color;
import lombok.Getter;
import lombok.NonNull;


import java.util.Locale;

@Getter
public class ServerMove {

    private final Color serverColor;
    private final Color workstationColor;

    public ServerMove(@NonNull String serverColorString, @NonNull String workstationColorString) {
        this(Color.valueOf(serverColorString.toUpperCase(Locale.ROOT)), Color.valueOf(workstationColorString.toUpperCase(Locale.ROOT)));
    }

    public ServerMove(@NonNull Color serverColor, @NonNull Color workstationColor) {
        this.serverColor = serverColor;
        this.workstationColor = workstationColor;
    }

}
