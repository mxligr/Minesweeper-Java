package com.project;

public class Position implements Comparable<Position> {
    private final int row, col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public int compareTo(Position p) {
        if (this.row > p.getRow())
            return 1;
        if (this.row == p.getRow()) {
            if (this.col == p.getCol())
                return 0;
            if (this.col > p.getCol())
                return 1;
        }
        return -1;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + col;
        result = prime * result + row;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Position other = (Position) obj;
        if (col != other.col)
            return false;
        if (row != other.row)
            return false;
        return true;
    }

    public String toString() {
        return "(" + this.row + ", " + this.col + ")";
    }
}
