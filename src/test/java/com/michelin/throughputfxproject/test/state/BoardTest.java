package com.michelin.throughputfxproject.test.state;

import com.michelin.throughputfxproject.entities.state.Board;
import com.michelin.throughputfxproject.entities.cards.BitCard;
import com.michelin.throughputfxproject.entities.actions.BoardAction;
import com.michelin.throughputfxproject.entities.actions.HelpAction;
import com.michelin.throughputfxproject.entities.actions.Trap;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;


class BoardTest {


    @Test
    void initializeInstance_and_getInstance() {
        Board board = Board.initializeInstance(6, 3, 4, 5);
        assertNotNull(board);
        assertEquals(6, board.getDieFaces());
        assertEquals(3, board.getStationCount());
        assertEquals(4, board.getRunPeriods());
        assertEquals(5, board.getRunTurns());
    }

    @Test
    void getInstance_withoutInit_throws() {
        Board.clearInstance();
        assertThrows(IllegalStateException.class, Board::getInstance);
    }

    @Test
    void augmentRunTurn_incrementsTurnAndPeriod() {
        Board board = Board.initializeInstance(6, 3, 2, 2);
        int origTurn = board.getCurrentRunTurn();
        board.augmentRunTurn();
        assertEquals(origTurn + 1, board.getCurrentRunTurn());
        // Simulate end of period
        board.augmentRunTurn();
        assertEquals(1, board.getCurrentRunTurn());
        assertEquals(2, board.getCurrentPeriod());
    }

    @Test
    void resetRunTurns_setsToOne() {
        Board.initializeInstance(6, 3, 2, 2);
        Board board = Board.getInstance();
        board.augmentRunTurn();
        board.resetRunTurns();
        assertEquals(1, board.getCurrentRunTurn());
    }

    @Test
    void gameIsOver_returnsTrueWhenPeriodExceeded() {
        Board.initializeInstance(6, 3, 2, 2);
        Board board = Board.getInstance();
        // Simulate periods -> 2 periods, 2 turns each
        board.augmentRunTurn();
        board.augmentRunTurn();
        board.augmentRunTurn();
        board.augmentRunTurn();
        assertTrue(board.gameIsOver());
    }

    @Test
    void discoverBitActions_null_returnsNull() {
        Board.initializeInstance(6, 3, 2, 2);
        Board board = Board.getInstance();
        assertNull(board.discoverBitActions(null, 1, 1));
    }

    @Test
    void discoverBitActions_action2_returnsHelpActionAddOne() {
        Board.initializeInstance(6, 3, 2, 2);
        Board board = Board.getInstance();
        BitCard card = BitCard.builder().action(2).build();
        BoardAction action = board.discoverBitActions(card, 1, 1);
        assertInstanceOf(HelpAction.class, action);
        assertEquals(HelpAction.HelpActionType.ADD_ONE, ((HelpAction)action).type());
    }

    @Test
    void discoverBitActions_action10_returnsTrap() {
        Board.initializeInstance(6, 3, 2, 2);
        Board board = Board.getInstance();
        BitCard card = BitCard.builder().action(10).build();
        BoardAction action = board.discoverBitActions(card, 2, 3);
        assertInstanceOf(Trap.class, action);
    }
}

