import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;  

public class Checkers //extends Runnable
{    
    public static void main(String args[]) 
    {
        final int HEIGHT = 640; 
        final int WIDTH = 640;

        GameBoard GAME = new GameBoard(); 
        JPanel BOARD = GAME.generateGameBoard();

        ///////////////////////
        //  CHECKERS GAME UI //
        ///////////////////////

        System.out.println("\n\nLaunching Application..."); 
        
        //Creating the Frame
        JFrame window = new JFrame("Checkers");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(HEIGHT, WIDTH);
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        //Configure Game Interface
        JPanel center = new JPanel(); //BOARD
        center.setLayout(new BoxLayout(center, BoxLayout.X_AXIS));
        center.setBackground(Color.white); 
        window.getContentPane().add(center, BorderLayout.CENTER); 
        window.setVisible(true);
        center.add(BOARD); 
        window.setVisible(true);



        ///////////////////////////
        //  CHECKERS GAME LOGIC  //
        ///////////////////////////

        Boolean gameRunning = true; 
        Boolean playerTurn = true; 

        while(gameRunning)
        {
            //PLAYER TURN
            GameTile playerTurnObject = GAME.getGameTiles()[Stat.STATUS.i()][Stat.TURN.i()]; 
            synchronized (playerTurnObject) 
            {
                while (playerTurn) 
                {
                    try 
                    {
                        playerTurnObject.wait();
                    } 
                    catch (InterruptedException e) {}

                    playerTurn = playerTurnObject.getPlayerTurn(); 
                }
            }

            //SLOW DOWN
            try 
            {
                Thread.sleep(500); 
            } catch(InterruptedException e) {}

            //COMPUTER TURN
            playerTurnObject = GAME.getGameTiles()[Stat.STATUS.i()][Stat.TURN.i()];
            synchronized (playerTurnObject) 
            {
                    while (!playerTurn) 
                    {
                        GameTile[][] TILES = GAME.getGameTiles(); 
                        GameTile[] npcPieces = new GameTile[0];

                        for(int i = 1; i < 9; i++)
                        {
                            for(int j = 1; j < 9; j++)
                            {
                                Piece piece = TILES[i][j].getPiece(); 

                                if(piece == Piece.NPC || piece == Piece.NPCK)
                                {
                                    npcPieces = addNPC(npcPieces, TILES[i][j]); 
                                }
                            }
                        }

                        //Threads for NPC Pieces
                        Thread[] programThreads = new Thread[npcPieces.length]; 
                        for(int i = 0; i < npcPieces.length; i++)
                        {
                            npcMoveThread npcThread = new npcMoveThread(TILES, npcPieces, i); 
                            programThreads[i] = new Thread(npcThread); 
                            programThreads[i].start(); 
                        }

                        try
                        {
                            for(int i = 0; i < programThreads.length; i++)
                            {
                                programThreads[i].join();
                            }
                        }
                        catch(Exception E)
                        {
                            System.out.println("Failed to join threads");
                        } 

                        double highestPossibleMove = -1.0; 
                        int pieceToMove = 0; 

                        for(int i = 0; i < npcPieces.length; i++)
                        {
                            if(Double.compare(npcPieces[i].getHighestPossibleMove(), highestPossibleMove) == 1)
                            {
                                highestPossibleMove = npcPieces[i].getHighestPossibleMove(); 
                                pieceToMove = i; 
                            }
                        }

                        npcPieces[pieceToMove].movePieceToX(TILES); //MOVE THE PIECE
                        playerTurn = playerTurnObject.getPlayerTurn(); 
                    }
            }

            //Check for defeat, just in case
            gameRunning = GAME.getRunningStatus();
        }

        System.out.println("\n\nGame Over."); 
    }

    public static GameTile[] addNPC(GameTile[] npcPieces, GameTile piece)
    {
        GameTile[] npcNew = new GameTile[npcPieces.length+1]; 

        int i = 0; 
        for(i = 0; i < npcPieces.length; i++)
        {
            npcNew[i] = npcPieces[i]; 
        }

        npcNew[i] = piece; 

        return npcNew; 
    }
}

class npcMoveThread implements Runnable
{
    private GameTile[][] TILES; 
    private GameTile[] npcPieces;
    private int moveIndex; 
    private double highestPossibleMove; 

    public npcMoveThread(GameTile[][] TILES, GameTile[] npcPieces, int moveIndex)
    {
        this.TILES = TILES; 
        this.npcPieces = npcPieces;  
        this.moveIndex = moveIndex; 
        this.highestPossibleMove = -100.0; 
    }

    public void run()
    {
        Random random = new Random(); 
        GameTile npcPiece = this.npcPieces[moveIndex]; 

        GameTile[] possibleMoves = new GameTile[4]; 
        possibleMoves[0] = this.TILES[npcPiece.getiX()+1][npcPiece.getjX()-1]; // SW
        possibleMoves[1] = this.TILES[npcPiece.getiX()+1][npcPiece.getjX()+1]; // SE
        possibleMoves[2] = this.TILES[npcPiece.getiX()-1][npcPiece.getjX()-1]; // NW, KING
        possibleMoves[3] = this.TILES[npcPiece.getiX()-1][npcPiece.getjX()+1]; // NE, KING
        double[] possiblePoints = { -100.0, -100.0, -100.0, 100.0 }; 

        for(int i = 0; i < possibleMoves.length; i++)
        {
            int iMove = npcPiece.getiX()-possibleMoves[i].getiX(); 
            int jMove = npcPiece.getjX()-possibleMoves[i].getjX(); 

            switch(possibleMoves[i].getPiece())
            {
                case TILE: 
                    if(inBounds(possibleMoves[i].getiX(), possibleMoves[i].getjX()))
                    {
                        possiblePoints[i] = 1.0; //Move is possible
                    }
                break; 

                case PLAYER: 
                    possiblePoints[i] = -100.0; //Move MIGHT NOT be possible

                    if(inBounds(possibleMoves[i].getiX()-iMove, possibleMoves[i].getjX()-jMove))
                    {
                        possibleMoves[i] = TILES[possibleMoves[i].getiX()-iMove][possibleMoves[i].getjX()-jMove]; //Update Tile
                        if(possibleMoves[i].getPiece() == Piece.TILE)
                        {
                            possiblePoints[i] = 10.0; //Jump is possible
                        }
                    }
                break; 

                case PLAYERK: 
                    possiblePoints[i] = -100.0; //Move MIGHT NOT be possible

                    if(inBounds(possibleMoves[i].getiX()-iMove, possibleMoves[i].getjX()-jMove))
                    {
                        possibleMoves[i] = TILES[possibleMoves[i].getiX()-iMove][possibleMoves[i].getjX()-jMove]; //Update Tile
                        if(possibleMoves[i].getPiece() == Piece.TILE)
                        {
                            possiblePoints[i] = 15.0; //Jump is possible
                        }
                    }
                break; 

                default: possiblePoints[i] = -100.0; //Move NOT be possible
                break; 
            }
        }

        //FINAL CHECK: IF NOT A KING
        if(npcPiece.getPiece() != Piece.NPCK)
        {
            possiblePoints[2] = -100.0; // NW, KING ONLY MOVE
            possiblePoints[3] = -100.0; // NE, KING ONLY MOVE
        }
        else if(npcPiece.getPiece() == Piece.NPCK)
        {
            possiblePoints[2] = possiblePoints[2] + random.nextDouble(1.5); //NW, Advantageous King Move
            possiblePoints[3] = possiblePoints[3] + random.nextDouble(1.5); //NE, Advantageous King Move
        }

        //CALCULATE DECUTION
        for(int i = 0; i < possiblePoints.length; i++)
        {
            if(Double.compare(possiblePoints[i], -100.0) < 1)
            {
                possiblePoints[i] = possiblePoints[i] - calculateDeduction(npcPiece.getPiece(), possibleMoves[i]); 
            }
        }
        
        for(int i = 0; i < possiblePoints.length; i++)
        {
            if(Double.compare(possiblePoints[i], this.highestPossibleMove) == 1) //Only randomizing the high ones
            {
                this.highestPossibleMove = possiblePoints[i] + random.nextDouble(3.0); 
                npcPieces[this.moveIndex].setToX(possibleMoves[i].getiX(), possibleMoves[i].getjX()); //Set to i to move to
            }
        }
        
        npcPieces[moveIndex].setHighestPossibleMove(this.highestPossibleMove);
    }

    public double calculateDeduction(Piece npcPieceType, GameTile npcPiece)
    {
        Double deduction = 0.0; 

        GameTile[] possibleNeighbors = new GameTile[4]; 

        for(int i = 0; i < possibleNeighbors.length; i++)
        {
            try
            {
                switch(i)
                {
                    case 0: possibleNeighbors[0] = this.TILES[npcPiece.getiX()+1][npcPiece.getjX()-1]; // SW Neighbor
                    break;  
                    case 1: possibleNeighbors[1] = this.TILES[npcPiece.getiX()+1][npcPiece.getjX()+1]; // SE Neighbor
                    break; 
                    case 2: possibleNeighbors[2] = this.TILES[npcPiece.getiX()-1][npcPiece.getjX()-1]; // NW Neighbor
                    break; 
                    case 3: possibleNeighbors[3] = this.TILES[npcPiece.getiX()-1][npcPiece.getjX()+1]; // NE Neighbor
                    default:  
                    break;
                }
            } catch (IndexOutOfBoundsException e) {}

        }

        for(int i = 0; i < possibleNeighbors.length; i++)
        {
            try
            {
                if(inBounds(possibleNeighbors[i].getiX(), possibleNeighbors[i].getjX()))
                {
                    if(possibleNeighbors[i].getPiece() == Piece.PLAYER || possibleNeighbors[i].getPiece() == Piece.PLAYERK)
                    {
                        int iMove = possibleNeighbors[i].getiX()-npcPiece.getiX(); 
                        int jMove = possibleNeighbors[i].getjX()-npcPiece.getjX(); 

                        if(inBounds(possibleNeighbors[i].getiX()-iMove, possibleNeighbors[i].getjX()-jMove))
                        {
                            if(this.TILES[possibleNeighbors[i].getiX()-iMove][possibleNeighbors[i].getjX()-jMove].getPiece() == Piece.TILE)
                            {
                                if(npcPieceType == Piece.NPC)
                                {
                                    deduction = 5.0; 
                                }
                                else if(npcPieceType == Piece.NPCK)
                                {
                                    deduction = 10.0; 
                                }
                            }
                        }
                    }
                }
            } catch(NullPointerException e) {}
        }

        return deduction; 
    }

    public Boolean inBounds(int iMove, int jMove)
    {
        int casesSatisfied = 0;  

        if(0 < iMove && iMove < 9)
        {
            casesSatisfied++; 
        }

        if(0 < jMove && jMove < 9)
        {
            casesSatisfied++; 
        }

        if(casesSatisfied == 2)
        {
            return true; 
        }
        else 
        {
            return false; 
        }
    }
}