package com.example.phrases

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.phrases.databinding.ActivityMainBinding
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val adapter = Adapter(PhrasesList.toMutableList())

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val recyclerView = binding.recyclerView
        val linearLayoutManager = LinearLayoutManager(this)

        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = adapter

        val fab = binding.fab

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && fab.visibility == View.VISIBLE) {
                    fab.hide()
                } else if (dy < 0 && fab.visibility != View.VISIBLE) {
                    fab.show()
                }
            }

        })

        val trashBinIcon = resources.getDrawable(
            R.drawable.ic_delete,
            null
        )

        val myCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                adapter.dataList.removeAt(viewHolder.adapterPosition)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
            }

            @SuppressLint("ResourceAsColor")
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                c.clipRect(
                    dX, viewHolder.itemView.top.toFloat(),
                    c.width.toFloat(), viewHolder.itemView.bottom.toFloat()
                )

                /*val background = GradientDrawable()
                val color = Color.parseColor("#91A7D0")
                val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }*/

                if (0 - dX < c.width / 3)
                    c.drawColor(Color.parseColor("#ffccd5"))
                else
                    c.drawColor(Color.parseColor("#ff8fa3"))


                val margin = resources.getDimension(R.dimen.text_margin)
                    .roundToInt()

                val height = viewHolder.itemView.bottom - viewHolder.itemView.top

                trashBinIcon.bounds = Rect(
                    c.width - trashBinIcon.intrinsicWidth - margin,
                    height - height / 2 - (trashBinIcon.intrinsicHeight / 2),
                    c.width - margin,
                    height - height / 2 + (trashBinIcon.intrinsicHeight / 2)
                )
                trashBinIcon.draw(c)

                super.onChildDraw(
                    c, recyclerView, viewHolder,
                    dX, dY, actionState, isCurrentlyActive
                )
            }
        }

        val myHelper = ItemTouchHelper(myCallback)
        myHelper.attachToRecyclerView(recyclerView)


    }
}