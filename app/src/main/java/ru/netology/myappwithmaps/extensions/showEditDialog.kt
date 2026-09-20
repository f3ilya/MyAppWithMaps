package ru.netology.myappwithmaps.extensions

import android.app.AlertDialog
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import ru.netology.myappwithmaps.R
import ru.netology.myappwithmaps.db.entity.PointEntity

fun Fragment.showEditDialog(
    point: PointEntity,
    onSave: (updatedPoint: PointEntity) -> Unit,
    onDelete: (deletePoint: PointEntity) -> Unit
) {
    val builder = AlertDialog.Builder(requireContext())
    val dialogView = layoutInflater.inflate(R.layout.dialog_edit_point, null)
    val titleEditText = dialogView.findViewById<EditText>(R.id.titleEditText)
    val descEditText = dialogView.findViewById<EditText>(R.id.descEditText)

    titleEditText.setText(point.title)
    descEditText.setText(point.description)

    builder.setView(dialogView)
        .setTitle(if (point.id == 0L) getString(R.string.new_point) else getString(R.string.editing_the_point))
        .setPositiveButton(getString(R.string.save)) { _, _ ->
            val title = titleEditText.text.toString().trim()
            val description = descEditText.text.toString().trim()

            if (title.isNotEmpty()) {
                val updatedPoint = point.copy(title = title, description = description)
                onSave(updatedPoint)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.the_title_cannot_be_empty),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    if (point.id != 0L) {
        builder.setNeutralButton(getString(R.string.delete)) { _, _ ->
            onDelete(point)
        }
    }

    builder.setNegativeButton(getString(R.string.cancel), null).show()
}