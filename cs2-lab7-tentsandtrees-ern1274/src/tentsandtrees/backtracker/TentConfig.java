package tentsandtrees.backtracker;

import java.io.*;
import java.util.*;

/**
 *  The full representation of a configuration in the TentsAndTrees puzzle.
 *  It can read an initial configuration from a file, and supports the
 *  Configuration methods necessary for the Backtracker solver.
 *
 *  @author RIT CS
 *  @author Ethan Nunez
 */
public class TentConfig implements Configuration {
    // INPUT CONSTANTS
    /** An empty cell */
    public final static char EMPTY = '.';
    /** A cell occupied with grass */
    public final static char GRASS = '-';
    /** A cell occupied with a tent */
    public final static char TENT = '^';
    /** A cell occupied with a tree */
    public final static char TREE = '%';

    // OUTPUT CONSTANTS
    /** A horizontal divider */
    public final static char HORI_DIVIDE = '-';
    /** A vertical divider */
    public final static char VERT_DIVIDE = '|';

    // Elements for construction
    private boolean check = true;
    private int DIM;
    private char[][] board;
    private ArrayList<Integer> rows= new ArrayList<>();
    private ArrayList<Integer> columns = new ArrayList<>();
    private int row;
    private int col;
    /**
     * Construct the initial configuration from an input file whose contents
     * are, for example:<br>
     * <tt><br>
     * 3        # square dimension of field<br>
     * 2 0 1    # row looking values, top to bottom<br>
     * 2 0 1    # column looking values, left to right<br>
     * . % .    # row 1, .=empty, %=tree<br>
     * % . .    # row 2<br>
     * . % .    # row 3<br>
     * </tt><br>
     * @param filename the name of the file to read from
     * @throws IOException if the file is not found or there are errors reading
     */
    public TentConfig(String filename) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(filename));
        int ClmRow = Integer.parseInt(in.readLine());
        this.DIM = ClmRow;
        this.board = new char[ClmRow][ClmRow];
        for (int row = 0; row < ClmRow; row++) {
            for (int col = 0; col < ClmRow; col++) {
                board[row][col] = EMPTY;
            }
        }

        String R = in.readLine();
        String[] row = R.split(" ");
        for (String num:row) {
            rows.add(Integer.parseInt(num));
        }
        String C = in.readLine();
        String[] col = C.split(" ");
        for (String num:col) {
            columns.add(Integer.parseInt(num));
        }
        for (int i = 0; i < ClmRow; i++) {
            int f = 0;
            String str = in.readLine();
            String[] line = str.split(" ");
            for (String item:line) {
                if(item.charAt(0)==EMPTY){
                    board[i][f] = EMPTY;
                    f++;
                }
                else if(item.charAt(0)==TREE){
                    board[i][f] = TREE;
                    f++;
                }
            }
        }
        in.close();// <3 Jimmy
        this.row = 0;
        this.col = 0;

    }

    /**
     * Copy constructor.  Takes a config, other, and makes a full "deep" copy
     * of its instance data.
     * @param other the config to copy
     */
    private TentConfig(TentConfig other, int row,int col) {
        this.DIM = other.DIM;
        this.row = row;
        this.col = col;
        this.rows = other.rows;
        this.columns = other.columns;
        this.board = new char[other.DIM][other.DIM];
        for (int r=0; r<this.DIM; r++) {
            System.arraycopy(other.board[r], 0, this.board[r], 0, this.DIM);
        }
        this.board[this.row][this.col] = TENT;
    }

    /**
     * gets the childs of the configuration it is handed. Checks and skips any spot that has something that
     * is not EMPTY
     * @return Collection<Configuration> Successors: returns all the configurations possible from one configuration
     */
    @Override
    public Collection<Configuration> getSuccessors() {

        List<Configuration> successors = new ArrayList<>();
        if(check){
            clear();
        }
        for (int row = 0; row < this.DIM; row++) {
            while(this.col<DIM&&(this.board[row][this.col]!= EMPTY||numTents(this.col)==columns.get(this.col))){
                this.col++;
            }
            if(rows.get(row)!=0&&this.col<DIM) {
                TentConfig child = new TentConfig(this, row, this.col);
                successors.add(child);
                this.board[row][this.col] = GRASS;
            }
            this.col = 0;
        }
        return successors;
    }

    /**
     * checks if this configuration is valid by checking
     * if the tents are near each other in any direction
     * and returns false if it is.
     * also checks if the tent is near a tree. returns true if it is
     * @return boolean: true or false
     */
    @Override
    public boolean isValid() {
        int uprow = this.row;
        int downrow = this.row;
        uprow -= 1;
        downrow += 1;
        if(!diagonalCheck()){
            return false;
        }
        if ((this.col-1>=0&&this.board[this.row][col-1] == TENT) ||
                (uprow >= 0 && this.board[uprow][col] == TENT) ||
                (downrow < this.DIM && this.board[downrow][col] == TENT)||
                (this.col+1<DIM&&this.board[this.row][this.col+1] == TENT)) {
            return false;
        }
        else if((this.col-1>=0&&this.board[this.row][col-1] == TREE) ||
                (uprow >= 0 && this.board[uprow][col] == TREE) ||
                (downrow < this.DIM && this.board[downrow][col] == TREE)||
                (this.col+1<DIM&&this.board[this.row][this.col+1] == TREE)){
            if(wholeCheck()){
                fill();
            }
            return true;
        }
        return false;
    }

    /**
     * checks if the current configuration fits the goal and the parameters
     * to be the correct configuration
     * @return boolean true or false
     */
    @Override
    public boolean isGoal() {
        return wholeCheck()&&diagonalCheck();
    }

    /**
     * returns the visual form of the configuration
     * @return the visual form of the configuration
     */
    @Override
    public String toString() {
        String config = "";
        String topandbottom ="";
        for (int i = 0; i <= rows.size(); i++) {
            topandbottom += HORI_DIVIDE;
        }
        config+=" "+topandbottom+"\n";
        int placeholder = 0;
        for (char[] item:board) {
            config += VERT_DIVIDE;
            for (char cha:item) {
                config += Character.toString(cha);
                config += " ";
            }
            String config2 = config.substring(0,config.length()-1);
            config = config2;
            config+=VERT_DIVIDE+" "+rows.get(placeholder)+"\n";
            placeholder++;

        }
        config += " "+topandbottom+"\n ";
        for (Integer num:columns) {
            config+= num +" ";
        }
        return config;                 // replace
    }

    /**
     * makes an entire row or column grass if the Arraylist rows or columns contain 0 and look for
     * the said row that has no tents then fill the emptys with grass ignoring the trees
     */
    private void clear(){
        if(rows.contains(0)){
            for (int row = 0; row < rows.size(); row++) {
                if(rows.get(row)==0){
                    for (int col = 0; col < this.DIM; col++) {
                        if (this.board[row][col]!=TREE) {
                            this.board[row][col] = GRASS;
                        }
                    }
                }
            }
        }
        if(columns.contains(0)){
            for (int col = 0; col < columns.size(); col++) {
                if(columns.get(col)==0){
                    for (int row = 0; row < this.DIM; row++) {
                        if (this.board[row][col]!=TREE) {
                            this.board[row][col] = GRASS;
                        }
                    }
                }
            }
        }
        check = false;
    }

    /**
     * checks the diagonal sides of the tent of the current config
     * @return Boolean: true or false. false if there is a tent diagonal of that current tent
     */
    public boolean diagonalCheck(){
        for (int row = 0; row < DIM; row++) {
            for (int col = 0; col < DIM; col++) {
                if(this.board[row][col]==TENT){
                    if((row-1>=0&&col-1>=0)&&this.board[row-1][col-1]==TENT){
                        return false;
                    }
                    if((row+1<DIM&&col+1<DIM)&&this.board[row+1][col+1]==TENT){
                        return false;
                    }
                    if((row+1>DIM&&col-1>=0)&&this.board[row+1][col-1]==TENT){
                        return false;
                    }
                    if((row-1>=0&&col+1<DIM)&&this.board[row-1][col+1]==TENT){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * if all conditions satisfied, fill the configurations remaining emptys with grass to ready it for the isGoal check()
     */
    public void fill(){
        for (int row = 0; row < DIM; row++) {
            for (int col = 0; col < DIM; col++) {
                if(this.board[row][col]==EMPTY){
                    this.board[row][col]=GRASS;
                }
            }
        }
    }

    /**
     * checks the amount of tents in that column
     * @param col the column to be checked
     * @return int the number of tents
     */
    public int numTents(int col){
        int tents = 0;
        for (int row = 0; row < DIM; row++) {
            if(this.board[row][col]==TENT){
                tents++;
            }
        }
        return tents;
    }

    /**
     * same as numTents(int col) but with row
     * @param row the row to be checked
     * @return the number of tents in that row
     */
    private int rowTents(int row){
        int tents = 0;
        for (int r = 0; r < DIM; r++) {
            if(this.board[row][r]==TENT){
                tents++;
            }
        }
        return tents;
    }

    /**
     * checks every row and column to make all the tents match the amount of tents required
     * @return Boolean if it doesnt match then false otherwise true
     */
    private boolean wholeCheck(){
        for (int i = 0; i < DIM; i++) {
            if(numTents(i)!=columns.get(i)){
                return false;
            }
            if(rowTents(i)!=rows.get(i)){
                return false;
            }

        }
        return true;
    }

}

