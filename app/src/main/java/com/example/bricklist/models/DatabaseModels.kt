package com.example.bricklist.models

class InventoriesPart(
    var itemType: String = "",
    var itemID: String = "",
    var qty: Int = 0,
    var color: Int = 0,
    var extra: String = "",
    var alternate: String = "",
    var matchID: Int = 0,
    var counterPart: String = ""
) { }

class PartsDetailed(
    var id: Int = 0,
    var name: String = "",
    var colorName: String = "",
    var code: String = "",
    var qtyInStock: Int = 0,
    var qtyInSet: Int = 0,
    var image: String = ""
)