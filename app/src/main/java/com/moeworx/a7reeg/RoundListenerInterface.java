package com.moeworx.a7reeg;

public interface RoundListenerInterface {
    public void onNextTurn();
    public void playerHasDrawn();
    public void playerThrewReject(Card card);
    public void moveToFloor(CardSet set , RoundPlayer player);
    public void updateFloor(RoundPlayer player);
    public void endRound(RoundPlayer roundWinner);
}
