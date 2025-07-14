package com.example.trainboard

class StationInformation {
    companion object {
        val stationCodesMap = mapOf("London Kings Cross" to "KGX", "Edinburgh" to "EDB", "Inverness" to "INV", "Hull" to "HUL", "Darlington" to "DAR")

        fun getStationNames(): List<String> {
            return ArrayList(stationCodesMap.keys)
        }

        fun getCrsFromName(name: String?): String? {
            return stationCodesMap[name]
        }
    }
}