package ie.wit.tennisapp.ui.results

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ie.wit.tennisapp.R
import ie.wit.tennisapp.adapters.ResultAdapter
import ie.wit.tennisapp.adapters.ResultsListener
import ie.wit.tennisapp.databinding.FragmentResultsBinding
import ie.wit.tennisapp.main.MainApp
import ie.wit.tennisapp.models.ResultModel
import ie.wit.tennisapp.helpers.SwipeToDeleteCallback
import ie.wit.tennisapp.helpers.SwipeToEditCallback

class ResultsFragment : Fragment(), ResultsListener {

    lateinit var app: MainApp
    private var _fragBinding: FragmentResultsBinding? = null
    private val fragBinding get() = _fragBinding!!
    private lateinit var refreshIntentLauncher : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = activity?.application as MainApp
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentResultsBinding.inflate(inflater, container, false)
        val root = fragBinding.root
        activity?.title = getString(R.string.menu_results)


        val fab: FloatingActionButton = fragBinding.fab
        fab.setOnClickListener {
            val action = ResultsFragmentDirections.actionResultsFragmentToAddResultFragment()
            findNavController().navigate(action)
        }

        setEditAndDeleteSwipeFunctions()
        loadResults()
        registerRefreshCallback()

        render(app.results.findAll())

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ResultsFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    private fun setEditAndDeleteSwipeFunctions() {
        val swipeDeleteHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onResultDeleteSwiped(viewHolder.adapterPosition)
            }
        }
        val swipeEditHandler = object : SwipeToEditCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onResultEditSwiped(viewHolder.adapterPosition)
            }
        }
        val itemTouchDeleteHelper = ItemTouchHelper(swipeDeleteHandler)
        val itemTouchEditHelper = ItemTouchHelper(swipeEditHandler)
        itemTouchDeleteHelper.attachToRecyclerView(fragBinding.recyclerView)
        itemTouchEditHelper.attachToRecyclerView(fragBinding.recyclerView)
    }

    override fun onResultDeleteSwiped(resultPosition: Int) {
        val builder = context?.let { AlertDialog.Builder(it) }
        builder?.setMessage("Are you sure you want to delete this result?")?.setCancelable(false)
            ?.setPositiveButton("Yes") { _, _ ->
                var targetResult = app.results.findAll().elementAt(resultPosition)
                val adapter = fragBinding.recyclerView.adapter as ResultAdapter
                app.results.delete(targetResult)
                adapter.notifyItemRemoved(resultPosition)
                fragBinding.recyclerView.adapter?.notifyDataSetChanged()
            }?.setNegativeButton("No") { dialog, _ ->
                fragBinding.recyclerView.adapter?.notifyDataSetChanged()
                dialog.dismiss()
            }
        builder?.create()?.show()
    }

    override fun onResultEditSwiped(resultPosition: Int) {
        var targetResult = app.results.findAll().elementAt(resultPosition)
        val action = ResultsFragmentDirections.actionResultsFragmentToAddResultFragment(targetResult.id.toString())
        findNavController().navigate(action)
    }

    private fun registerRefreshCallback() {
        refreshIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { loadResults() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        fragBinding.recyclerView.adapter?.notifyDataSetChanged()
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun loadResults() {
        showResults(app.results.findAll())
    }

    private fun showResults (results: List<ResultModel>) {
        fragBinding.recyclerView.adapter = ResultAdapter(results, this)
        fragBinding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun render(resultsList: List<ResultModel>) {
        fragBinding.recyclerView.layoutManager = LinearLayoutManager(context)
        fragBinding.recyclerView.adapter = ResultAdapter(resultsList, this)
        if (resultsList.isEmpty()) {
            fragBinding.recyclerView.visibility = View.GONE
            fragBinding.resultsNotFound.visibility = View.VISIBLE
        } else {
            fragBinding.recyclerView.visibility = View.VISIBLE
            fragBinding.resultsNotFound.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragBinding = null
    }
}