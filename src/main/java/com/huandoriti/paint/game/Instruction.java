package com.huandoriti.paint.game;

import java.io.Serializable;
import java.nio.ReadOnlyBufferException;

public enum Instruction implements Serializable {
    MY_RUOLO(),
    YOUR_RUOLO(),
    MY_ID(),
    YOUR_ID(),
    /**
     *
     */
    WORD(),
    NUMBER(),
    DISEGNATORE_SCONNECTED(),
    /**
     * Partita quando è finita, tempo terminato o tutti hanno indovinato
     */
    FINISH(),
    /**
     * FINE termina comunicazione socket
     */
    DONE,
}
