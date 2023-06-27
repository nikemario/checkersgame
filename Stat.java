public enum Stat
{
    STATUS(0), TURN(0), TOMOVE(1), MOVEX(2), SELECTED(3), NPCCOUNT(4), PLAYERCOUNT(5); 

    private int index;

    Stat(int index) 
    {
        this.index = index;
    }

    public int i() 
    {
        return index;
    }

    public int status()
    {
        return 0; 
    }

    public int playerTurn()
    {
        return 0; 
    }

    public int pieceToMove()
    {
        return 1; 
    }

    public int moveToCoordinates()
    {
        return 2; 
    }

    public int selectedPiece()
    {
        return 3; 
    }
}