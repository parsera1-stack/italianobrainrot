package com.brainrot.italiano.ui.screens.parent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brainrot.italiano.R
import com.brainrot.italiano.domain.model.Word

class WordsAdapter(
    private val onDelete: (Word) -> Unit,
    private val onToggleLearned: (Word) -> Unit
) : ListAdapter<Word, WordsAdapter.WordViewHolder>(WordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_word, parent, false)
        return WordViewHolder(view)
    }

    override fun onBindViewHolder(holder: WordViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRussian: TextView = itemView.findViewById(R.id.tvRussian)
        private val tvEnglish: TextView = itemView.findViewById(R.id.tvEnglish)
        private val cbLearned: CheckBox = itemView.findViewById(R.id.cbLearned)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(word: Word) {
            tvRussian.text = word.russian
            tvEnglish.text = word.english

            // Сначала сбрасываем слушатель, потом меняем состояние
            cbLearned.setOnCheckedChangeListener(null)
            cbLearned.isChecked = word.isLearned

            cbLearned.setOnCheckedChangeListener { _, isChecked ->
                if (word.isLearned != isChecked) {
                    onToggleLearned(word.copy(isLearned = isChecked))
                }
            }

            btnDelete.setOnClickListener {
                onDelete(word)
            }
        }
    }

    class WordDiffCallback : DiffUtil.ItemCallback<Word>() {
        override fun areItemsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Word, newItem: Word): Boolean {
            return oldItem == newItem
        }
    }
}
