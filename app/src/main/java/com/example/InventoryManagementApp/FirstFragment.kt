package com.example.InventoryManagementApp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.Manifest
import android.app.AlertDialog
import android.net.Uri
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.InventoryManagementApp.databinding.FragmentFirstBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private var MY_PERMISSIONS_REQUEST_CAMERA = 1
    private var activeImageButton: ImageButton? = null // This will be used to reference the clicked ImageButton
    private var activeItemName: String = ""
    private var activeItemId: String=""
    private lateinit var itemlist: List<Item>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val getAction = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        val bitmap = it.data?.extras?.get("data") as Bitmap
        activeImageButton?.setImageBitmap(bitmap)
        var uri=saveImage(bitmap,activeItemName)
        val db= context?.let { it1 -> DBHandler(it1,null) }
        if (db != null) {
            db.updateItemEntry(activeItemId.toIntOrNull()?:0, uri.toString())
        }
//        activeImageButton = null // Reset activeImageView
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    @SuppressLint("Range")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(
                    arrayOf(Manifest.permission.CAMERA),
                    MY_PERMISSIONS_REQUEST_CAMERA)

                // MY_PERMISSIONS_REQUEST_CAMERA is an
                // app-defined int constant. The callback method gets the
                // result of the request.

            }
        } else {
            // Permission has already been granted


        }
        if (isEmpty()){
            binding.btAddItem.isEnabled=false
        }


        updateListView(view)
        val layout: View = view.findViewById(R.id.lOAddItems)
        val slideUp: Animation = AnimationUtils.loadAnimation(context, R.anim.slide_up)
        val slideDown: Animation = AnimationUtils.loadAnimation(context, R.anim.slide_down)
        slideUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                // The action to be performed after an animation ends.
                layout.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })


        binding.btExpand?.setOnClickListener{

            if (layout.visibility == View.VISIBLE) {
                // If visible, slide up and hide
                layout.startAnimation(slideUp)
                binding.btExpand.setIconResource(R.drawable.baseline_expand_more_black_24dp)
//                layout.visibility = View.GONE
            } else {
                // If not visible, slide down and show
                layout.startAnimation(slideDown)
                layout.visibility=View.VISIBLE
                binding.btExpand.setIconResource(R.drawable.baseline_expand_less_black_24dp)
            }
        }

        binding.etItemQuantity.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                updateTotalValue()
            }

            override fun afterTextChanged(s: Editable) {
                // Do something with the new text in s
                binding.btAddItem.isEnabled = !isEmpty()
            }
        })
        binding.etItemPrice.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                updateTotalValue()
            }

            override fun afterTextChanged(s: Editable) {
                // Do something with the new text in s
                binding.btAddItem.isEnabled = !isEmpty()
            }
        })
        binding.etSearchTerm.editText?.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val searchTerm=binding.etSearchTerm.editText?.text.toString()
                searchItem(view,searchTerm)

            }

            override fun afterTextChanged(s: Editable) {
                // Do something with the new text in s
                binding.btAddItem.isEnabled = !isEmpty()
            }
        })
        binding.btAddItem.setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            var itemName= binding.etItemName.editText?.text.toString()
            var itemQuantity= binding.etItemQuantity.editText?.text.toString()
            var itemPrice=binding.etItemPrice.editText?.text.toString()
            var totalValue=itemQuantity.toInt()*itemPrice.toFloat();

//            Log.d("Info: ", "Item Name: $itemName Item Quantity: $itemQuantity Item Price: $itemPrice")
            val db= context?.let { it1 -> DBHandler(it1,null) }
            if (db != null) {
                db.addItemEntry(itemName,itemQuantity.toInt(),itemPrice.toFloat(),totalValue,object:UpdateCallback{
                    override fun onUpdate(success: Boolean) {
                        if(success){
                            Toast.makeText(context, "$itemName added to database", Toast.LENGTH_LONG).show()
                            val layout= view.findViewById<LinearLayout>(R.id.llItemList)
                            val inflater=LayoutInflater.from(context)
                            itemlist = db.getAll()
                            addListItem(inflater,layout,
                                itemlist[itemlist.lastIndex].id,itemName,itemQuantity,itemPrice,totalValue.toString())
                            binding.etSearchTerm.editText?.setText(itemName.toString())
                            binding.etItemName.editText?.text?.clear()
                            binding.etItemPrice.editText?.text?.clear()
                            binding.etItemQuantity.editText?.text?.clear()
                            binding.tvTotalValue.text="Total Value: €0"
                            binding.btCalculateTotal.callOnClick()

                        }else{

                            Toast.makeText(requireContext(),"$itemName NOT Added to database: Item Name Exists",Toast.LENGTH_LONG).show()


                        }
                    }
                })


            }

        }
        binding.btCalculateTotal.setOnClickListener{
            var tvInventoryTotal=binding.tvInventoryTotal
            val total=calculateTotalInventoryValue()
            tvInventoryTotal.text="Inventory Total Value: €$total"
        }
        binding.btCalculateTotal.callOnClick()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun int_or_float(str: String): Any {
        try {
            return str.toFloat()
        }
        catch (e: Exception){
            return str.toInt()
        }
    }
    @SuppressLint("Range")
    fun addListItem(
        inflater: LayoutInflater,
        layout: LinearLayout,
        id: String,
        itemName: String,
        itemQuantity: String,
        itemPrice: String,
        totalValue: String,
        uri: Uri? = null){


        val customView =inflater.inflate(R.layout.list_item,layout,false)
        var name=customView.findViewById<TextView>(R.id.tvName)
        var quantity=customView.findViewById<TextView>(R.id.tvQuanity)
        var price=customView.findViewById<TextView>(R.id.tvPrice)
        var value=customView.findViewById<TextView>(R.id.tvValue)
        var tvId=customView.findViewById<TextView>(R.id.tvId)
        val btSubmit=customView.findViewById<TextView>(R.id.btEdit)
        btSubmit.setOnClickListener{
            openEditDialog(id,itemName,itemQuantity,itemPrice)
        }


        val list_item_image_bt=customView.findViewById<ImageButton>(R.id.ivImage).setOnClickListener{
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(requireActivity().packageManager) != null){
                activeImageButton = customView.findViewById<ImageButton>(R.id.ivImage) as ImageButton
                activeItemName=customView.findViewById<TextView>(R.id.tvName).text.toString()
                activeItemId=id
                getAction.launch(intent)
            }
        }
        val addQuantity=customView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btAddQuantity).setOnClickListener{
            val db = context?.let { it1 -> DBHandler(it1, null) }
            val searchTerm=itemName
//            Log.d("Add Quantity:","Active Item Name:$itemName")
            val cursor = db?.searchItemNameSpecific(searchTerm)
            if (cursor!=null) {
                if (cursor.moveToFirst()) {
                    val id= cursor.getString(cursor.getColumnIndex(DBHandler.ID_COL))
                    val quantity = cursor.getString(cursor.getColumnIndex(DBHandler.ITEM_QUANTITY))
                    val price= cursor.getFloat(cursor.getColumnIndex(DBHandler.ITEM_PRICE))
                    val newTotal=(quantity.toInt()+1)*price
                    db?.updateItemEntry(id.toInt(),quantity.toInt()+1,newTotal,object : UpdateCallback{
                        override fun onUpdate(success: Boolean) {
                            binding.btCalculateTotal.callOnClick()
                            if (binding.etSearchTerm.editText?.text.toString().equals("")){
                                updateListView(requireView())
                            }else{
                                searchItem(requireView(),searchTerm)
                            }
                        }
                    })
                }
            }

        }
        val removeQuantity=customView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btRemoveQuantity).setOnClickListener{
            val db = context?.let { it1 -> DBHandler(it1, null) }
            val searchTerm=itemName
            Log.d("Remove Quantity:","Active Item Name:$itemName")
            val cursor = db?.searchItemNameSpecific(searchTerm)
            if (cursor!=null) {
                if (cursor.moveToFirst()) {
                    val id= cursor.getString(cursor.getColumnIndex(DBHandler.ID_COL))
                    val quantity = cursor.getString(cursor.getColumnIndex(DBHandler.ITEM_QUANTITY))
                    val price= cursor.getFloat(cursor.getColumnIndex(DBHandler.ITEM_PRICE))
                    if (quantity.toInt()-1>=0){
                        val newTotal=(quantity.toInt()-1)*price
                        db?.updateItemEntry(id.toInt(),quantity.toInt()-1,newTotal,object : UpdateCallback{
                            override fun onUpdate(success: Boolean) {
                                binding.btCalculateTotal.callOnClick()
                                if(success){
                                    if (binding.etSearchTerm.editText?.text.toString().equals("")){
                                        updateListView(requireView())
                                    }else{
                                        searchItem(requireView(),searchTerm)
                                    }
                                }
                            }
                        })
                    }else{
                        Toast.makeText(requireContext(),"You have 0 Items",Toast.LENGTH_LONG).show()
                    }

                }
            }

        }

        val btDelete=customView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btDelete).setOnClickListener{
            val dbHandler = DBHandler(requireContext(), null)

            AlertDialog.Builder(context)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Yes") { dialog, _ ->
                    val success = dbHandler.removeItemEntry(customView.findViewById<TextView>(R.id.tvId).text.toString().toInt())
                    if (success) {
                        Toast.makeText(context, "Item removed successfully", Toast.LENGTH_SHORT).show()
                        updateListView(requireView())
                        binding.btCalculateTotal.callOnClick()
                    } else {
                        Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()

        }


        if (uri!=null) {
            customView.findViewById<ImageButton>(R.id.ivImage).setImageURI(uri)
        }
        else{
            customView.findViewById<ImageButton>(R.id.ivImage).setImageResource(R.drawable.defaultimage)
        }
        tvId.text=id
        name.text="Name: "+itemName
        quantity.text="Quantity: "+itemQuantity
        price.text="Price: €"+itemPrice
        value.text="Total Value: €"+totalValue
        layout.addView(customView)
    }

    private fun openEditDialog(id: String,itemName: String,itemQuantity: String,itemPrice:String){
        try {
            val dialog= Dialog(requireContext())
            dialog.setContentView(R.layout.edit_item_dialog)


            dialog.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.etItemName_dialog).editText?.setText(itemName)
            dialog.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.etItemQuantity_dialog).editText?.setText(itemQuantity)
            dialog.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.etItemPrice_dialog).editText?.setText(itemPrice)

            val dialog_itemName=dialog.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.etItemName_dialog).editText?.text
            val dialog_Quantity=dialog.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.etItemQuantity_dialog).editText?.text
            val dialog_Price=dialog.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.etItemPrice_dialog).editText?.text
            val dialog_total=dialog_Quantity.toString().toInt()*dialog_Price.toString().toFloat()
            dialog.findViewById<TextView>(R.id.tvTotalValue_dialog).text="Total Value: €"+(dialog_total).toString()


            dialog.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.etItemName_dialog).editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    try {
                        dialog.findViewById<Button>(R.id.btSubmitChanges).isEnabled=!isEmpty(dialog)
                    }catch (e: Exception){
                        Log.e("DialogOnTextChanged",e.stackTraceToString())
                    }

                }

                override fun afterTextChanged(s: Editable) {
                    // Do something with the new text in s
                    try{
                        dialog.findViewById<Button>(R.id.btSubmitChanges).isEnabled=!isEmpty(dialog)
                    }catch (e: Exception){
                        Log.e("DialogAfterTextChanged",e.stackTraceToString())
                    }
                }
            })

            dialog.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.etItemQuantity_dialog).editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    try {
                        dialog.findViewById<Button>(R.id.btSubmitChanges).isEnabled=!isEmpty(dialog)
                        val dialog_total=dialog_Quantity.toString().toInt()*dialog_Price.toString().toFloat()
                        dialog.findViewById<TextView>(R.id.tvTotalValue_dialog).text="Total Value: €"+(dialog_total).toString()

                    }catch (e: Exception){
                        Log.e("DialogOnTextChanged",e.stackTraceToString())
                    }

                }

                override fun afterTextChanged(s: Editable) {
                    // Do something with the new text in s
                    try{
                        dialog.findViewById<Button>(R.id.btSubmitChanges).isEnabled=!isEmpty(dialog)
                        val dialog_total=dialog_Quantity.toString().toInt()*dialog_Price.toString().toFloat()
                        dialog.findViewById<TextView>(R.id.tvTotalValue_dialog).text="Total Value: €"+(dialog_total).toString()

                    }catch (e: Exception){
                        Log.e("DialogAfterTextChanged",e.stackTraceToString())
                    }
                }
            })
            dialog.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.etItemPrice_dialog).editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    try {
                        dialog.findViewById<Button>(R.id.btSubmitChanges).isEnabled=!isEmpty(dialog)
                        val dialog_total=dialog_Quantity.toString().toInt()*dialog_Price.toString().toFloat()
                        dialog.findViewById<TextView>(R.id.tvTotalValue_dialog).text="Total Value: €"+(dialog_total).toString()

                    }catch (e: Exception){
                        Log.e("DialogOnTextChanged",e.stackTraceToString())
                    }

                }

                override fun afterTextChanged(s: Editable) {
                    // Do something with the new text in s
                    try{
                        dialog.findViewById<Button>(R.id.btSubmitChanges).isEnabled=!isEmpty(dialog)
                        val dialog_total=dialog_Quantity.toString().toInt()*dialog_Price.toString().toFloat()
                        dialog.findViewById<TextView>(R.id.tvTotalValue_dialog).text="Total Value: €"+(dialog_total).toString()

                    }catch (e: Exception){
                        Log.e("DialogAfterTextChanged",e.stackTraceToString())
                    }
                }
            })


            dialog.findViewById<Button>(R.id.btSubmitChanges).setOnClickListener{
                val db = context?.let { it1 -> DBHandler(it1, null) }
                val dialog_total=dialog_Quantity.toString().toInt()*dialog_Price.toString().toFloat()
                val success = db?.updateItemEntry(id.toInt(),dialog_itemName.toString(), dialog_Quantity.toString().toInt(),dialog_Price.toString().toFloat(),dialog_total.toString().toFloat()
                    ,object: UpdateCallback{
                        override fun onUpdate(success: Boolean) {
                            if (success) {
                                // The update was successful

                                Toast.makeText(context,"Update Success",Toast.LENGTH_LONG).show()

                                updateListView(requireView())
                                binding.btCalculateTotal.callOnClick()
                                dialog.dismiss()
                            } else {
                                // The update was not successful
                                Toast.makeText(context,"Update Fail: Item Name Exists",Toast.LENGTH_LONG).show()
                            }
                        }
                    })

            }
            dialog.findViewById<Button>(R.id.btCancel).setOnClickListener{
                dialog.dismiss()
            }
            dialog.findViewById<Button>(R.id.btSubmitChanges).isEnabled=!isEmpty(dialog)
            dialog.show()
        }catch (e: Exception)
        {
            Log.d("e: ",e.printStackTrace().toString())
        }
    }
    @SuppressLint("SuspiciousIndentation")
    fun updateTotalValue() {
        var quantity=binding.etItemQuantity.editText?.text.toString().toIntOrNull();
        var price=binding.etItemPrice.editText?.text.toString().toFloatOrNull();

            if (quantity != null && price !=null){
                binding.tvTotalValue.text= "Total Value: €"+(quantity*price).toString();
            }else{
                Log.e("updateTotalValue:","Price or Quantity are null")
            }
    }
    fun calculateTotalInventoryValue(): Float {
        val db = context?.let { it1 -> DBHandler(it1, null) }
        itemlist= db?.getAll()!!
        var totalInventoryValue=0f
        for (item in itemlist){
            totalInventoryValue += item.totalValue.toFloat()
        }
        return totalInventoryValue
    }
    @SuppressLint("Range")
    fun updateListView(view: View){

        try {
            val db = context?.let { it1 -> DBHandler(it1, null) }
            if (binding.etSearchTerm.editText?.text.toString().equals("")){
                    itemlist = db?.getAll()!! // Get the list of items
            }else{
                    itemlist = db?.searchItemName(binding.etSearchTerm.editText?.text.toString())!!
            }

            val layout= view.findViewById<LinearLayout>(R.id.llItemList)
            layout.removeAllViews()
            val inflater=LayoutInflater.from(context)
            for (item in itemlist){
                addListItem(inflater,layout,item.id,item.name,item.quantity,item.price,item.totalValue,item.imageUri)
            }
        }catch (e: Exception){
            Log.e("updateListView",e.stackTraceToString())
        }


    }
    @SuppressLint("Range")
    fun searchItem(view: View, searchTerm: String){
        try {
            val db = context?.let { it1 -> DBHandler(it1, null) }
            // below is the variable for cursor
            // we have called method to get
            // all names from our database
            // and add to name text view
            itemlist = db?.searchItemName(searchTerm)!!

            // moving the cursor to first position and
            // appending value in the text view
            val layout= view.findViewById<LinearLayout>(R.id.llItemList)
            layout.removeAllViews()
            val inflater=LayoutInflater.from(context)

            for (item in itemlist){
                addListItem(inflater,layout,item.id,item.name,item.quantity,item.price,item.totalValue,item.imageUri)
            }
        }catch (e:Exception){
            Toast.makeText(requireContext(),"Something went wrong when searching for items!",Toast.LENGTH_LONG).show()
            Log.e("searchItem",e.stackTraceToString())
        }

    }
    private fun saveImage(bitmap: Bitmap,itemName: String): Uri? {
        val imagesFolder = File(context?.cacheDir, "images")
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "$itemName.png")

            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()

            val appid=requireContext().packageName
            uri = FileProvider.getUriForFile(
                requireContext(),

                "$appid.fileprovider",
                file
            )
        } catch (e: IOException) {
            Log.e("saveImage",e.stackTraceToString())
        }

        return uri
    }
    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).invalidateOptionsMenu()
    }

    private fun isEmpty(): Boolean{
        return (binding.etItemName.editText?.text.toString().equals("")
                || binding.etItemPrice.editText?.text.toString().equals("")
                || binding.etItemQuantity.editText?.text.toString().equals(""))
    }
    private fun isEmpty(view: Dialog): Boolean{
        return (view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.etItemPrice_dialog).editText?.text.toString().equals("")
                || view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.etItemQuantity_dialog).editText?.text.toString().equals("")
                || view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.etItemName_dialog).editText?.text.toString().equals(""))
    }
    }
