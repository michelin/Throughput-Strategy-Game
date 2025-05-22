package com.michelin.throughputfxproject.controllerTests;


import com.michelin.throughputfxproject.ThroughputApplication;
import com.michelin.throughputfxproject.controllers.BITController;
import com.michelin.throughputfxproject.controllers.Prompts;
import com.michelin.throughputfxproject.entities.cards.BitCard;
import com.michelin.throughputfxproject.entities.state.Die;
import com.michelin.throughputfxproject.services.CardService;
import com.michelin.throughputfxproject.services.DiceService;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThroughputTests {


    @Mock
    private FXMLLoader loader;

    @Mock
    private BITController bitController;

    @Mock
    private TextArea gameBoardLog;

    @Mock
    private Pane container;

    @Test
    void testDrawBit_WhenRollIsDieFaces_ShouldDrawBitCard() throws IOException {
        // Mock dependencies
        int dieSides = 6;
        when(DiceService.getDie(dieSides)).thenReturn(Die.builder().sides(dieSides).build());
        when(DiceService.rollDie(any(Die.class))).thenReturn(Die.builder().sides(dieSides).build()); // Simulate rolling a 6
        BitCard bitCard = mock(BitCard.class, RETURNS_DEEP_STUBS);
        when(CardService.pickACardDestructively()).thenReturn(bitCard);
        when(loader.load()).thenReturn(new Pane());
        when(loader.getController()).thenReturn(bitController);
        when(ThroughputApplication.class.getResource("bit-card.fxml")).thenReturn(Objects.requireNonNull(getClass().getResource("bit-card.fxml")));
        when(ThroughputApplication.class.getResource(anyString())).thenReturn(getClass().getResource("test.png"));

        // Call the method
        BitCard result = MyPrompts.callDrawBit(container, dieSides, gameBoardLog, 4);

        // Verify interactions and assertions
        verify(loader).load();
        verify(bitController).getCardTitle();
        verify(bitController).getCardSubtitle();
        verify(bitController).getCardReason();
        verify(bitController).getCardInstructions();
        verify(bitController).getCardDescription();
        verify(bitController).getCardDescriptionTitle();
        verify(bitController).getDescriptionImage();
        assertNotNull(result);
    }

    @Test
    void testDrawBit_WhenRollIsNotDieFaces_ShouldNotDrawBitCard() throws IOException {
        // Mock dependencies
        int dieSides = 6;


        // Call the method
        BitCard result = MyPrompts.callDrawBit(container, dieSides, gameBoardLog, 4);

        // Verify interactions and assertions
        assertNull(result);
    }

    static class MyPrompts extends Prompts {
        MyPrompts() {
            super(); // Explicitly call the parent class constructor
        }

        static BitCard callDrawBit(@NonNull Pane container, int dieSides, @NonNull TextArea gameBoardLog, int numberRequiredForSuccess) throws IOException {
            return drawBit(container, dieSides, gameBoardLog, numberRequiredForSuccess);
        }
    }

    // Add more test cases to cover different scenarios and edge cases

}
