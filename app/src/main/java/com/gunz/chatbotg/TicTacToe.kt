package com.gunz.chatbotg

import kotlin.random.Random

/** Pemain = X, Bot = O. Bot memakai minimax tapi 25% langkahnya acak supaya bisa dikalahkan. */
class TicTacToe {
    val board = CharArray(9) { ' ' }
    var over = false
    var winner = ' '            // 'X', 'O', atau 'D' (seri)
    var winLine: IntArray? = null

    private val lines = arrayOf(
        intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
        intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
        intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
    )

    fun reset() {
        board.fill(' ')
        over = false
        winner = ' '
        winLine = null
    }

    fun play(i: Int): Boolean {
        if (over || board[i] != ' ') return false
        board[i] = 'X'
        if (checkEnd()) return true
        botMove()
        checkEnd()
        return true
    }

    fun status(): String = when {
        !over -> "Giliranmu ❌"
        winner == 'X' -> "🎉 Kamu menang!"
        winner == 'O' -> "🤖 Bot menang!"
        else -> "🤝 Seri!"
    }

    private fun winnerOf(): Char {
        for (l in lines) {
            val a = board[l[0]]
            if (a != ' ' && a == board[l[1]] && a == board[l[2]]) return a
        }
        return ' '
    }

    private fun checkEnd(): Boolean {
        for (l in lines) {
            val a = board[l[0]]
            if (a != ' ' && a == board[l[1]] && a == board[l[2]]) {
                winner = a; over = true; winLine = l
                return true
            }
        }
        if (board.none { it == ' ' }) {
            winner = 'D'; over = true
            return true
        }
        return false
    }

    private fun botMove() {
        val empty = board.indices.filter { board[it] == ' ' }
        if (empty.isEmpty()) return
        if (Random.nextInt(100) < 25) {
            board[empty.random()] = 'O'
            return
        }
        var best = Int.MIN_VALUE
        var move = empty[0]
        for (i in empty) {
            board[i] = 'O'
            val s = minimax(false, 1)
            board[i] = ' '
            if (s > best) { best = s; move = i }
        }
        board[move] = 'O'
    }

    private fun minimax(botTurn: Boolean, depth: Int): Int {
        when (winnerOf()) {
            'O' -> return 10 - depth
            'X' -> return depth - 10
        }
        if (board.none { it == ' ' }) return 0
        var best = if (botTurn) Int.MIN_VALUE else Int.MAX_VALUE
        for (i in board.indices) {
            if (board[i] != ' ') continue
            board[i] = if (botTurn) 'O' else 'X'
            val s = minimax(!botTurn, depth + 1)
            board[i] = ' '
            best = if (botTurn) maxOf(best, s) else minOf(best, s)
        }
        return best
    }
}
