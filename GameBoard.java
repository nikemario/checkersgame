import javax.swing.*;
import java.awt.*;
import java.awt.event.*;  

public class GameBoard
{
    GameTile[][] TILES; 
    Boolean RUNNING;

    public GameBoard() 
    {
        this.TILES = generateGameTiles(); 
        this.RUNNING = true; 
    }

    public GameTile[][] getGameTiles(){ return this.TILES; }

    public Boolean getRunningStatus()
    { 
        double npcCount = TILES[Stat.STATUS.i()][Stat.NPCCOUNT.i()].getHighestPossibleMove();
        double playerCount = TILES[Stat.STATUS.i()][Stat.PLAYERCOUNT.i()].getHighestPossibleMove();

        if(Double.compare(npcCount, 0.0) == 0 || Double.compare(playerCount, 0.0) == 0)
        {
            this.RUNNING = false; 
        }

        return this.RUNNING; 
    }

    public int[] getNPCMove()
    {
        int[] moveSpecs = new int[4]; 
        moveSpecs[0] = TILES[Stat.STATUS.i()][Stat.TOMOVE.i()].getiX(); 
        moveSpecs[1] = TILES[Stat.STATUS.i()][Stat.TOMOVE.i()].getjX(); 
        moveSpecs[2] = TILES[Stat.STATUS.i()][Stat.MOVEX.i()].getiX(); 
        moveSpecs[3] = TILES[Stat.STATUS.i()][Stat.MOVEX.i()].getjX(); 
        return moveSpecs; 
    }

    public Boolean getSelectedStatus()
    {
        Piece piece = this.TILES[Stat.STATUS.i()][Stat.SELECTED.i()].getPiece(); 
        
        if(piece == Piece.SELECTED)
        {
            return true; 
        }
        else 
        {
            return false; 
        }
    }

    // Generates and returns the game board; calls generateGameTile and generateGamePieces
    // *Should only be called by the Checkers.java file when creating a new game*
    public JPanel generateGameBoard() 
    {
        JPanel BOARD = new JPanel(new GridLayout(10, 10));

        for(int i = 0; i < 10; i++) 
        {
            for(int j = 0; j < 10; j++) 
            {   
                BOARD.add(this.TILES[i][j]); 
            }
        } 

        return BOARD; 
    }

    public GameTile[][] generateGameTiles()
    {
        GameTile[][] TILES = new GameTile[10][10]; // Index 8 is board specifications+
        
        Boolean startingColor = true; 

        for(int i = 0; i < 10; i++) 
        {
            startingColor = !startingColor; 
            Boolean color = startingColor; 

            for(int j = 0; j < 10; j++) 
            {
                if(i != 0 && i != 9 && j != 0 && j != 9)
                {
                    if(color) 
                    {
                        TILES[i][j] = new GameTile(Piece.TILE, i, j);  
                        TILES[i][j].setBackground(Color.DARK_GRAY); 
                    } 
                    else 
                    {
                        TILES[i][j] = new GameTile(Piece.TILE, i, j);  
                        TILES[i][j].setBackground(Color.WHITE);
                    }
                    color = !color; 
                    TILES[i][j] = generateGameTile(TILES,i,j); 
                }
                else
                {
                    TILES[i][j] = new GameTile(Piece.TILE, i, j);   
                    TILES[i][j].setBackground(Color.LIGHT_GRAY); 
                    TILES[i][j] = generateGameTile(TILES,i,j);
                }
            }
        } 

        TILES = generateGamePieces(TILES);
        return TILES; 
    }

    // Assigns tile colors and creats actions for each tile
    // *Should never be directly called by the Checkers.java file*
    public GameTile generateGameTile(GameTile[][] TILES, int i, int j)
    {
        final int iFin = i; 
        final int jFin = j; 
        
        TILES[iFin][jFin].addActionListener(new ActionListener() 
        {    
            public void actionPerformed (ActionEvent e) 
            {
                if(iFin != 0 && iFin != 9 && jFin != 0 && jFin != 9)
                {
                    if(TILES[Stat.STATUS.i()][Stat.TURN.i()].getPlayerTurn())
                    {   
                        Piece selectionStatus = TILES[Stat.STATUS.i()][Stat.SELECTED.i()].getPiece(); 
                        Piece deselectType = TILES[Stat.STATUS.i()][Stat.SELECTED.i()].getPiece(); 
                        int selectediX = TILES[Stat.STATUS.i()][Stat.SELECTED.i()].getiX(); 
                        int selectedjX = TILES[Stat.STATUS.i()][Stat.SELECTED.i()].getjX();  
                        Boolean previousSelection = false; 
                        
                        if(selectionStatus == Piece.SELECTED)
                        {
                            previousSelection = true; 
                            deselectType = Piece.PLAYER; 
                        }
                        else if(selectionStatus == Piece.SELECTEDK)
                        {
                            previousSelection = true; 
                            deselectType = Piece.PLAYERK; 
                        }

                        Piece playerMove = TILES[iFin][jFin].getPiece(); 

                        switch(playerMove)
                        {
                            case TILE: 
                                if(previousSelection)
                                {
                                    Boolean pieceCanMove = TILES[iFin][jFin].pieceCanMoveFrom(TILES, selectediX, selectedjX);
                                    if(pieceCanMove)
                                    {
                                        TILES[Stat.STATUS.i()][Stat.SELECTED.i()].setPieceStatus(Piece.TILE); //Remove the stored selection
                                        TILES[selectediX][selectedjX].setPiece(Piece.TILE); //Set old place to tile
                                        TILES[iFin][jFin].setPiece(deselectType); //Set moved piece to PLAYER or PLAYERK 
                                        TILES[Stat.STATUS.i()][Stat.TURN.i()].setPlayerTurn(false); //Signal NPC Turn
                                    }
                                }
                            break; 
                            
                            case PLAYER: 
                                if(previousSelection)
                                {
                                    TILES[selectediX][selectedjX].setPiece(deselectType); //Deselect old piece back to PLAYER OR PLAYERK
                                    
                                }
                                TILES[Stat.STATUS.i()][Stat.SELECTED.i()].setPieceStatus(Piece.SELECTED);
                                TILES[Stat.STATUS.i()][Stat.SELECTED.i()].setX(iFin, jFin);
                                TILES[iFin][jFin].setPiece(Piece.SELECTED); //Select new piece
                            break; 

                            case PLAYERK: 
                            if(previousSelection)
                            {
                                TILES[selectediX][selectedjX].setPiece(deselectType); //Deselect old piece back to PLAYER OR PLAYERK
                                
                            }
                            TILES[Stat.STATUS.i()][Stat.SELECTED.i()].setPieceStatus(Piece.SELECTEDK);
                            TILES[Stat.STATUS.i()][Stat.SELECTED.i()].setX(iFin, jFin);
                            TILES[iFin][jFin].setPiece(Piece.SELECTEDK); //Select new piece
                            break; 
                            
                            case SELECTED: 
                                TILES[Stat.STATUS.i()][Stat.SELECTED.i()].setPieceStatus(Piece.TILE);
                                TILES[iFin][jFin].setPiece(Piece.PLAYER); //Remove selection if selected
                            break; 

                            case SELECTEDK: 
                                TILES[Stat.STATUS.i()][Stat.SELECTED.i()].setPieceStatus(Piece.TILE);
                                TILES[iFin][jFin].setPiece(Piece.PLAYERK); //Remove selection if selected
                            break; 

                            default: 
                            break; 
                        }
                    }
                }  
            }  
        });
        
        return TILES[iFin][jFin]; 
    }

    // Assigns player and computer pieces to the appropriate board tiles
    // *Should never be directly called by the Checkers.java file*
    public GameTile[][] generateGamePieces(GameTile[][] TILES) 
    {   
        Boolean startingTile = true; 

        for(int i = 1; i < 9; i++)
        {
            startingTile = !startingTile; 
            Boolean tile = startingTile; 

            for(int j = 1; j < 9; j++)
            { 
                if(tile && i<4)
                {
                    TILES[i][j].setPiece(Piece.NPC);
                } 
                else if(tile && i>5)
                {
                    TILES[i][j].setPiece(Piece.PLAYER);
                }
                tile = !tile; 
            }
        }

        TILES[Stat.STATUS.i()][Stat.NPCCOUNT.i()].setHighestPossibleMove(12.0);
        TILES[Stat.STATUS.i()][Stat.PLAYERCOUNT.i()].setHighestPossibleMove(12.0);

        this.TILES = TILES; 

        return TILES; 
    }
}