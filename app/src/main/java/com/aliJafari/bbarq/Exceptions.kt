package com.aliJafari.bbarq

class BillIDNotFoundException : Exception()
class BillIDNot13Chars : Exception()
data class RequestUnsuccessful(val error : Exception? = null,val details : String = error?.message?:"Failed") : Exception()