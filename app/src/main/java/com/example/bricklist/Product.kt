package com.example.bricklist

class Product {
    var id: Int = 0
    var productName: String? = null
    var qty: Int = 0

    constructor(id: Int, productName: String, qty: Int) {
        this.id = id
        this.productName = productName
        this.qty = qty
    }
    constructor(productName: String, qty: Int) {
        this.productName = productName
        this.qty = qty
    }
}