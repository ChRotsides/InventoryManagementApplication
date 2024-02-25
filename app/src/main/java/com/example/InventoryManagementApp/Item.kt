package com.example.InventoryManagementApp

import android.net.Uri

data class Item(
    var id: String,
    var name: String,
    var quantity: String,
    var price: String,
    var totalValue: String,
    var imageUri: Uri?
)
