package com.ejebring.cf

data class Login(val username: String = "", val passcode: String = "") {
    fun validate() {
        require(username.isNotEmpty()) { "Username must not be empty" }
        require(username.length <= 50) { "Username must be less then 50 characters" }
        require(passcode.length <= 50) { "Passcode must be less then 50 characters" }
    }
}