package com.gunz.chatbotg

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val items: List<ChatMessage>,
    private val onCommand: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = if (items[position].game != null) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == 1) GameVH(inf.inflate(R.layout.item_msg_game, parent, false))
        else TextVH(inf.inflate(R.layout.item_msg_text, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val m = items[position]
        if (holder is TextVH) holder.bind(m) else if (holder is GameVH) holder.bind(m)
    }

    inner class TextVH(v: View) : RecyclerView.ViewHolder(v) {
        private val bubble = v.findViewById<LinearLayout>(R.id.bubble)
        private val tvText = v.findViewById<TextView>(R.id.tvText)
        private val btns = v.findViewById<LinearLayout>(R.id.btns)
        private val tvTime = v.findViewById<TextView>(R.id.tvTime)

        fun bind(m: ChatMessage) {
            val ctx = itemView.context
            val lp = bubble.layoutParams as FrameLayout.LayoutParams
            val side = Util.dp(ctx, 40)
            if (m.fromUser) {
                lp.gravity = Gravity.END
                lp.marginStart = side; lp.marginEnd = 0
                bubble.setBackgroundResource(R.drawable.bg_bubble_user)
            } else {
                lp.gravity = Gravity.START
                lp.marginStart = 0; lp.marginEnd = side
                bubble.setBackgroundResource(R.drawable.bg_bubble_bot)
            }
            bubble.layoutParams = lp

            tvText.text = m.text
            tvTime.text = m.time

            btns.removeAllViews()
            for (b in m.buttons) {
                val tv = TextView(ctx).apply {
                    text = b.label
                    setTextColor(0xFFFFFFFF.toInt())
                    textSize = 14f
                    setBackgroundResource(R.drawable.bg_chip)
                    setPadding(Util.dp(ctx, 14), Util.dp(ctx, 10), Util.dp(ctx, 14), Util.dp(ctx, 10))
                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        val act = b.action
                        if (act != null) act.invoke() else b.command?.let(onCommand)
                    }
                }
                val blp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = Util.dp(ctx, 6) }
                btns.addView(tv, blp)
            }
        }
    }

    inner class GameVH(v: View) : RecyclerView.ViewHolder(v) {
        private val grid = v.findViewById<GridLayout>(R.id.grid)
        private val status = v.findViewById<TextView>(R.id.tvStatus)
        private val reset = v.findViewById<TextView>(R.id.tvReset)
        private val cells = ArrayList<TextView>()

        init {
            val ctx = v.context
            for (i in 0..8) {
                val tv = TextView(ctx).apply {
                    gravity = Gravity.CENTER
                    textSize = 28f
                    setBackgroundResource(R.drawable.bg_cell)
                    isClickable = true
                    isFocusable = true
                }
                val lp = GridLayout.LayoutParams().apply {
                    width = Util.dp(ctx, 64)
                    height = Util.dp(ctx, 64)
                    setMargins(Util.dp(ctx, 4), Util.dp(ctx, 4), Util.dp(ctx, 4), Util.dp(ctx, 4))
                }
                grid.addView(tv, lp)
                cells.add(tv)
                tv.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                    val g = items[pos].game ?: return@setOnClickListener
                    if (g.play(i)) notifyItemChanged(pos)
                }
            }
            reset.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                items[pos].game?.reset()
                notifyItemChanged(pos)
            }
        }

        fun bind(m: ChatMessage) {
            val g = m.game ?: return
            val win = g.winLine
            for (i in 0..8) {
                cells[i].text = when (g.board[i]) {
                    'X' -> "❌"
                    'O' -> "⭕"
                    else -> ""
                }
                cells[i].setBackgroundResource(
                    if (win != null && win.contains(i)) R.drawable.bg_cell_win else R.drawable.bg_cell
                )
            }
            status.text = g.status()
        }
    }
}
