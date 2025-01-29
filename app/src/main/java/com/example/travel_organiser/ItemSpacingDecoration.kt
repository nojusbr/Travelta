import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ItemSpacingDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view) // Item position
        val spanCount = (parent.layoutManager as GridLayoutManager).spanCount

        // Apply spacing to all sides
        outRect.left = space
        outRect.right = space
        outRect.bottom = space

        // Add extra spacing to the top of the first row
        if (position < spanCount) {
            outRect.top = space
        } else {
            outRect.top = 0
        }
    }
}