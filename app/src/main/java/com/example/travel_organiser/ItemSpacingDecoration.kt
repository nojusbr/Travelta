import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemSpacingDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        // Set equal margins on all sides of each item
        outRect.left = -space + 20
        outRect.right = space
        outRect.bottom = space

        // Optional: Add extra spacing for the top of the first item
        if (parent.getChildAdapterPosition(view) == 0) {
            outRect.top = space
        }
    }
}
