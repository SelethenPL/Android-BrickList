package com.example.bricklist

class Inventory {
    var itemType: String = ""
    var itemID: String = ""
    var qty: Int = 0
    var color: Int = 0
    var extra: String = ""
    var alternate: String = ""
    var matchID: Int = 0
    var counterPart: String = ""

    constructor(itemType: String, itemID: String,
                qty: Int, color: Int,
                extra: String, alternate: String,
                matchID: Int, counterPart: String) {
        this.itemType = itemType
        this.itemID = itemID
        this.qty = qty
        this.color = color
        this.extra = extra
        this.alternate = alternate
        this.matchID = matchID
        this.counterPart = counterPart
    }
    constructor() {
        this.itemType = ""
        this.itemID = ""
        this.qty = 0
        this.color = 0
        this.extra = ""
        this.alternate = ""
        this.matchID = 0
        this.counterPart = ""
    }
}