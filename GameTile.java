import javax.swing.*;
import java.awt.*;

public class GameTile extends Button 
{
    Piece piece; 
    int iX; 
    int jX; 
    volatile int iToX; 
    volatile int jToX; 
    
    volatile Boolean playerTurn; 
    double highestPossibleMove;

    public GameTile(Piece piece, int i, int j)
    {
        super(); //Button Functions 
        
        //Piece Attributes 
        this.piece = piece;
        this.iX = i; 
        this.jX = j; 
        this.iToX = i; //For NPC
        this.jToX = j; //For NPC

        //Atrributes for Board Calculations 
        this.playerTurn = true; 
        this.highestPossibleMove = -1.0; //Only NPC

        setVisible(true);
    }

    public Piece getPiece(){ return this.piece; }
    public int getiX(){ return this.iX; }
    public int getjX(){ return this.jX; }
    public int getiToX(){ return this.iToX; }
    public int getjToX(){ return this.jToX; }
    public Boolean getPlayerTurn(){ return this.playerTurn; }
    public Double getHighestPossibleMove(){ return this.highestPossibleMove; }

    public void setPiece(Piece piece)
    {
        this.piece = piece; 
        
        if(this.piece == Piece.NPC && this.iX == 8)
        {
            this.piece = Piece.NPCK; 
        }
        else if (this.piece == Piece.PLAYER && this.iX == 1)
        {
            this.piece = Piece.PLAYERK;
        }

        this.repaint();
    }

    public void setPieceStatus(Piece piece)
    {
        this.piece = piece; 
    }

    public void setX(int iX, int jX)
    {
        this.iX = iX; 
        this.jX = jX; 
    }

    public void setiX(int iX)
    {
        this.iX = iX; 
    }

    public void setjX(int jX)
    {
        this.jX = jX; 
    }

    public void setToX(int iToX, int jToX)
    {
        this.iToX = iToX;
        this.jToX = jToX; 
    }

    public void setPlayerTurn(Boolean playerTurn)
    {
        synchronized(this)
        {
            this.playerTurn = playerTurn; 
            this.notifyAll();
        }
    }

    public void setHighestPossibleMove(Double highestPossibleMove)
    {
        this.highestPossibleMove = highestPossibleMove; 
    }

    public void movePieceToX(GameTile[][] TILES)
    {
        GameTile playerTurnObject = TILES[Stat.STATUS.i()][Stat.TURN.i()];
            
        synchronized(playerTurnObject)
        {
            Piece pieceType = this.getPiece(); 
            Boolean pieceCanMove = TILES[this.iToX][this.jToX].pieceCanMoveFrom(TILES, this.iX, this.jX);
            if(pieceCanMove)
            {
                TILES[this.iX][this.jX].setPiece(Piece.TILE); 
                TILES[this.iToX][this.jToX].setPiece(pieceType);
                TILES[Stat.STATUS.i()][Stat.TURN.i()].setPlayerTurn(true);
            }
        }
    }

    public Boolean pieceCanMoveFrom(GameTile[][] TILES, int i, int j)
    {
        
        int iMove = i-this.iX; 
        int jMove = j-this.jX; 

        int casesSatisfied = 0;

        //Check iMove 
        if(TILES[Stat.STATUS.i()][Stat.TURN.i()].getPlayerTurn())
        {
            if(iMove == 1)
            {
                casesSatisfied++; 
            }
            else if(TILES[i][j].getPiece() == Piece.SELECTEDK && iMove == -1)
            {
                casesSatisfied++; 
            }
        }
        else 
        {
            if(iMove == -1) 
            {
                casesSatisfied++; 
            }
            else if(TILES[i][j].getPiece() == Piece.NPCK && iMove == 1)
            {
                casesSatisfied++; 
            }
        }

        //Check jMove
        if(jMove == 1 || jMove == -1)
        {
            casesSatisfied++; 
        }

        switch(casesSatisfied)
        {
            case 2: return true;
            case 1: return false; 
            default: //Do nothing
        }

        if(TILES[Stat.STATUS.i()][Stat.TURN.i()].getPlayerTurn())
        {
            if(iMove == 2)
            {
                casesSatisfied++; 
            }
            else if(TILES[i][j].getPiece() == Piece.SELECTEDK && iMove == -2)
            {
                casesSatisfied++; 
            }
        }
        else 
        {
            if(iMove == -2)
            {
                casesSatisfied++; 
            }
            else if(TILES[i][j].getPiece() == Piece.NPCK && iMove == 2)
            {
                casesSatisfied++; 
            }
        }

        if(jMove == 2 || jMove == -2)
        {
            casesSatisfied++; 
        }

        //If the piece is two spaces away, check if the opposing players piece is between
        if(casesSatisfied == 2)
        {
            Piece pieceType = TILES[i][j].getPiece(); 
            int iC = i-(iMove/2); 
            int jC = j-(jMove/2); 
            return pieceCanHopOver(TILES, pieceType, iC, jC); 
        }
        else
        {
            return false; 
        }
    }

    public Boolean pieceCanHopOver(GameTile[][] TILES, Piece piece, int i, int j)
    {
        Piece pieceType = TILES[i][j].getPiece(); 

        if(piece == Piece.NPC | piece == Piece.NPCK)
        {
            if(pieceType == Piece.PLAYER | pieceType == Piece.PLAYERK)
            {
                TILES[i][j].setPiece(Piece.TILE); //Kill the piece

                double count = TILES[Stat.STATUS.i()][Stat.PLAYERCOUNT.i()].getHighestPossibleMove();
                TILES[Stat.STATUS.i()][Stat.PLAYERCOUNT.i()].setHighestPossibleMove(count-1.0);

                return true; 
            }
        }

        if(piece == Piece.SELECTED | piece == Piece.SELECTEDK)
        {
            if(pieceType == Piece.NPC | pieceType == Piece.NPCK )
            {
                TILES[i][j].setPiece(Piece.TILE); //Kill the piece

                double count = TILES[Stat.STATUS.i()][Stat.NPCCOUNT.i()].getHighestPossibleMove();
                TILES[Stat.STATUS.i()][Stat.NPCCOUNT.i()].setHighestPossibleMove(count-1.0);

                return true; 
            }
        }

        return false; 
    }

    //PAINTING METHOD
    public void paint(Graphics g)
    {
        Color oldBg = getBackground();
        g.setColor(oldBg);
        g.fillRect(0, 0, getWidth(), getHeight());

        if(oldBg != Color.LIGHT_GRAY)
        {
            switch(this.piece)
            {
                case NPC:       
                g.setColor(Color.BLACK);
                g.fillOval(getWidth()*2/15, getHeight()*2/15, (getWidth()*11)/15, (getHeight()*11)/15);
                break; 

                case NPCK: 
                g.setColor(Color.BLACK);
                g.fillOval(getWidth()*2/15, getHeight()*2/15, (getWidth()*11)/15, (getHeight()*11)/15);
                g.setColor(Color.MAGENTA);
                g.fillOval(getWidth()*4/15, getHeight()*4/15, (getWidth()*7)/15, (getHeight()*7)/15); 
                break; 

                case PLAYER:    
                g.setColor(Color.BLACK);
                g.fillOval(getWidth()*2/15, getHeight()*2/15, (getWidth()*11)/15, (getHeight()*11)/15);
                g.setColor(Color.WHITE);
                g.fillOval(getWidth()*3/15, getHeight()*3/15, (getWidth()*9)/15, (getHeight()*9)/15);
                break; 

                case PLAYERK: 
                g.setColor(Color.BLACK);
                g.fillOval(getWidth()*2/15, getHeight()*2/15, (getWidth()*11)/15, (getHeight()*11)/15);
                g.setColor(Color.WHITE);
                g.fillOval(getWidth()*3/15, getHeight()*3/15, (getWidth()*9)/15, (getHeight()*9)/15);
                g.setColor(Color.MAGENTA);
                g.fillOval(getWidth()*4/15, getHeight()*4/15, (getWidth()*7)/15, (getHeight()*7)/15);
                break; 

                case SELECTED:  
                g.setColor(Color.YELLOW);
                g.fillOval(getWidth()/15, getHeight()/15, (getWidth()*13)/15, (getHeight()*13)/15);
                g.setColor(Color.BLACK);
                g.fillOval(getWidth()*2/15, getHeight()*2/15, (getWidth()*11)/15, (getHeight()*11)/15);
                g.setColor(Color.WHITE);
                g.fillOval(getWidth()*3/15, getHeight()*3/15, (getWidth()*9)/15, (getHeight()*9)/15);
                break; 

                case SELECTEDK:
                g.setColor(Color.YELLOW);
                g.fillOval(getWidth()/15, getHeight()/15, (getWidth()*13)/15, (getHeight()*13)/15);
                g.setColor(Color.BLACK);
                g.fillOval(getWidth()*2/15, getHeight()*2/15, (getWidth()*11)/15, (getHeight()*11)/15);
                g.setColor(Color.WHITE);
                g.fillOval(getWidth()*3/15, getHeight()*3/15, (getWidth()*9)/15, (getHeight()*9)/15);
                g.setColor(Color.MAGENTA);
                g.fillOval(getWidth()*4/15, getHeight()*4/15, (getWidth()*7)/15, (getHeight()*7)/15);
                break; 

                default: //Do Nothing
                break; 
            }
        } 
    }
}