package minesweeper

import java.util.*
import kotlin.random.Random

val scanner = Scanner(System.`in`)
fun main() {
    print("Please select board size (height width)")
    val height = scanner.nextInt()
    val width = scanner.nextInt()
    print("How many mines do you want on the field?")
    val mines = scanner.nextInt()
    var board = generateBoard(height,width, mines)
    board = setNeighbours(board)
    printCells(board)
    var gameover = false
    while (!gameover) {
        println("Set/delete mine marks (x and y coordinates):")
        val column = scanner.nextInt() - 1
        val line = scanner.nextInt() - 1
        if (column+1 !in 1..width || line+1 !in 1..height) {
            println("The point ($column,$line) is outside the grid")
            continue
        }
        val action = scanner.next()
        if (Actions.valueOf(action) == Actions.mine) {
            if (!board[column][line].revealed) {
                board[column][line].marked = !board[column][line].marked
            }
        } else if (Actions.valueOf(action) == Actions.free) {
            if (board[column][line].mine) {
                println("You stepped on a mine and failed!")
                printFailed(board)
                break
            } else {
                board = updateBoard(column, line, board)
            }
        }
        printCells(board)
        if (wonGame(board, mines)) {
            println("Congratulations! You found all the mines!")
            gameover = true
        }
    }
}

enum class Actions {
    mine, free
}

data class Cell(var mine: Boolean, var neighboors: Int = 0,
                var marked: Boolean = false, var revealed: Boolean = false, var safe: Boolean = !mine)

fun max (a: Int, b: Int): Int {
    return if (a > b) a else b
}

fun min (a: Int, b: Int): Int {
    return if (a < b) a else b
}

fun countNeighbours(x: Int, y: Int, cells: Array<Array<Cell>>):Int {
    if (cells[x][y].mine) {
        return 0
    }
    var counter = 0
    val width = cells[0].size
    val height = cells.size
    for (i in max(y - 1, 0)..min(y + 1,width - 1)) {
        for (j in max(x - 1,0)..min(x + 1,height - 1)) {
            if (x == j && y == i) {
                continue
            } else if (cells[j][i].mine) {
                counter++
            }
        }
    }
    return counter
}

fun generateBoard(height: Int, width: Int, minesNumber: Int): Array<Array<Cell>> {
    val cells = Array(height) {Array(width) {Cell(false,0)} }
    var copyMinesNumber = minesNumber
    while (copyMinesNumber > 0) {
        val x = Random.nextInt(0, width)
        val y = Random.nextInt(0, height)
        if (!cells[y][x].mine) {
            cells[y][x].mine = true
            copyMinesNumber--
        }
    }
    return cells
}

fun setNeighbours(cells: Array<Array<Cell>>): Array<Array<Cell>> {
    for (i in cells.indices) {
        for (j in cells[i].indices) {
            cells[j][i].neighboors = countNeighbours(j, i, cells)
        }
    }
    return cells
}

fun printCells(cells: Array<Array<Cell>>) {
    var str1 = " |"
    var str2 = "-|"
    for (i in cells[0].indices) {
        str1 += (i + 1).toString()
        str2 += "-"
    }
    str1 += "|"
    str2 += "|"
    println(str1)
    println(str2)
    for (i in cells.indices) {
        var string = "${i+1}|"
        for (j in cells[i].indices) {
            if (cells[j][i].marked && !cells[j][i].revealed) {
                string += "*"
            } else if (!cells[j][i].revealed) {
                string += "."
            } else if (cells[j][i].revealed && cells[j][i].neighboors == 0){
                string += "/"
            } else if (cells[j][i].revealed && cells[j][i].neighboors != 0) {
                string += cells[j][i].neighboors.toString()
            }
        }
        println("$string|")
    }
    println(str2)
}

fun updateBoard(column: Int, line: Int, board: Array<Array<Cell>>, width: Int = board[0].size, height: Int = board.size): Array<Array<Cell>> {
    if (board[line][column].neighboors != 0) {
        board[line][column].revealed = true
        return board
    }
    if (!board[line][column].mine) {
        board[line][column].revealed = true
        for (newLine in max(line - 1, 0)..min(line + 1,width - 1)) {
            for (newColumn in max(column - 1,0)..min(column + 1,height - 1)) {
                if (column == newColumn && line == newLine) {
                    continue
                } else if (!board[newLine][newColumn].revealed && board[newLine][newColumn].neighboors != 0){
                    board[newLine][newColumn].revealed = true
                    continue
                } else {
                    if (!board[newLine][newColumn].revealed) {
                        updateBoard(newColumn, newLine, board, width, height)
                    }
                }
            }
        }
    }
    return board
}

fun wonGameByMark(cells: Array<Array<Cell>>, minesNumber: Int): Boolean {
    var markedMines = 0
    for (i in cells.indices) {
        for (j in cells[i].indices) {
            if (cells[j][i].marked && cells[j][i].mine) {
                markedMines++
            }
        }
    }
    return minesNumber == markedMines
}

fun wonGameByExplore(cells: Array<Array<Cell>>, minesNumber: Int): Boolean {
    var safeExplored = 0
    for (i in cells.indices) {
        for (j in cells[i].indices) {
            if (cells[j][i].safe && cells[j][i].revealed) {
                safeExplored++
            }
        }
    }
    return safeExplored == cells[0].size * cells.size - minesNumber
}

fun wonGame(cells: Array<Array<Cell>>, minesNumber: Int): Boolean {
    return wonGameByExplore(cells, minesNumber) || wonGameByMark(cells, minesNumber)
}

fun printFailed(cells: Array<Array<Cell>>) {
    var str1 = " |"
    var str2 = "-|"
    for (i in cells[0].indices) {
        str1 += (i + 1).toString()
        str2 += "-"
    }
    str1 += "|"
    str2 += "|"
    println(str1)
    println(str2)
    for (i in cells.indices) {
        var string = "${i+1}|"
        for (j in cells[i].indices) {
            if (cells[j][i].neighboors != 0) {
                string += cells[j][i].neighboors.toString()
            } else if (cells[j][i].mine) {
                string += "X"
            } else if (cells[j][i].neighboors == 0) {
                string += "/"
            }
        }
        println("$string|")
    }
    println(str2)
}