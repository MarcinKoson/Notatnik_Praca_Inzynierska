package com.thesis.note
//TODO documentation
class SearchValues{
    @Volatile private var instance: SearchValues? = null

    @Synchronized
    private fun createInstance() {
        if (instance == null) {
            instance = SearchValues()
        }
    }

    fun getInstance(): SearchValues? {
        if (instance == null) createInstance()
        return instance
    }

    var name: String? = null


}
object SearchValuesS{

     var name: String? = null
     var favorite:Boolean = false
     var group:Int? = null

}