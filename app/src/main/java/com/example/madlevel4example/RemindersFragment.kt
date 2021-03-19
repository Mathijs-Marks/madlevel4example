package com.example.madlevel4example

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madlevel4example.databinding.FragmentRemindersBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class RemindersFragment : Fragment() {

    private var _binding: FragmentRemindersBinding? = null
    private val binding get() = _binding!!

    private lateinit var remindersRepository: ReminderRepository

    private val reminders = arrayListOf<Reminder>()
    private val reminderAdapter = ReminderAdapter(reminders)

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRemindersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeAddReminderResult()

        remindersRepository = ReminderRepository(requireContext())
        getRemindersFromDatabase()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViews() {
        // Initialize the recycler view with a linear layout manager, adapter.
        binding.rvReminders.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.rvReminders.adapter = reminderAdapter
        createItemTouchHelper().attachToRecyclerView(binding.rvReminders)
    }

    private fun getRemindersFromDatabase() {
        val reminders = remindersRepository.getAllReminders()
        this@RemindersFragment.reminders.clear()
        this@RemindersFragment.reminders.addAll(reminders)
        reminderAdapter.notifyDataSetChanged()
    }

    private fun observeAddReminderResult() {
        setFragmentResultListener(REQ_REMINDER_KEY) { _, bundle ->
            bundle.getString(BUNDLE_REMINDER_KEY)?.let {
                val reminder = Reminder(it)

                //reminders.add(reminder)
                //reminderAdapter.notifyDataSetChanged()
                remindersRepository.insertReminder(reminder)
                getRemindersFromDatabase()
            }       // Throws an error log when there's no text.
                    ?: Log.e("RemindersFragment", "Request triggered, but empty reminder text!")
        }
    }

    /**
     * Create a touch helper to recognize when a user swipes an item from a recycler view.
     * An ItemTouchHelper enables touch behaviour (like swipe and move) on each ViewHolder,
     * and uses callbacks to signal when a user is performing these actions.
     */
    private fun createItemTouchHelper(): ItemTouchHelper {

        // Callback which is used to create the ItemTouch helper. Only enables left swipe.
        // Use ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) to also enable right swipe.
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            // Callback triggered when a user swiped an item.
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                //reminders.removeAt(position)
                //reminderAdapter.notifyDataSetChanged()

                val reminderToDelete = reminders[position]
                remindersRepository.deleteReminder(reminderToDelete)
                getRemindersFromDatabase()
            }
        }

        return ItemTouchHelper(callback)
    }
}